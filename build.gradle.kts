import java.security.MessageDigest
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.language.base.plugins.LifecycleBasePlugin

plugins {
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.serialization") version "2.3.20"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("com.networknt:json-schema-validator:3.0.7")
}

@CacheableTask
abstract class VerifyWorkbenchCatalogContract : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val lockFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val manifestFile: RegularFileProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val snapshotDirectory: DirectoryProperty

    @TaskAction
    fun verify() {

        val lock =
            lockFile.get()
                .asFile
                .readText(Charsets.UTF_8)

        val manifest =
            manifestFile.get().asFile

        val snapshot =
            snapshotDirectory.get()
                .asFile
                .toPath()
                .toAbsolutePath()
                .normalize()

        fun jsonString(
            name: String
        ): String =

            Regex(
                "\\\"${Regex.escape(name)}\\\"\\s*:\\s*\\\"([^\\\"]+)\\\""
            )
                .find(lock)
                ?.groupValues
                ?.get(1)
                ?: throw GradleException(
                    "Contract lock string is missing: $name"
                )

        fun jsonInt(
            name: String
        ): Int =

            Regex(
                "\\\"${Regex.escape(name)}\\\"\\s*:\\s*(\\d+)"
            )
                .find(lock)
                ?.groupValues
                ?.get(1)
                ?.toInt()
                ?: throw GradleException(
                    "Contract lock integer is missing: $name"
                )

        fun sha256(
            bytes: ByteArray
        ): String =

            MessageDigest
                .getInstance("SHA-256")
                .digest(bytes)
                .joinToString("") {

                    (it.toInt() and 0xff)
                        .toString(16)
                        .padStart(2, '0')
                }

        fun normalizedTextSha256(
            file: File
        ): String {

            val normalizedBytes =
                file
                    .readText(Charsets.UTF_8)
                    .replace("\r\n", "\n")
                    .replace("\r", "\n")
                    .toByteArray(Charsets.UTF_8)

            return sha256(
                normalizedBytes
            )
        }

        val sourceCommit =
            jsonString("commit")

        require(
            sourceCommit.matches(
                Regex("[0-9a-f]{40}")
            )
        ) {

            "The pinned Workbench commit must be a full lowercase Git commit hash."
        }

        require(
            jsonString(
                "hashAlgorithm"
            ) == "SHA-256"
        ) {

            "Only SHA-256 contract manifests are supported."
        }

        val expectedManifestHash =
            jsonString(
                "manifestSha256"
            )

        val actualManifestHash =
            normalizedTextSha256(
                manifest
            )

        require(
            actualManifestHash ==
                expectedManifestHash
        ) {

            "Contract manifest hash mismatch. " +
                "Expected $expectedManifestHash, " +
                "found $actualManifestHash."
        }

        val entryPattern =
            Regex(
                "^([0-9a-f]{64})  (.+)$"
            )

        val expectedFiles =
            linkedMapOf<String, String>()

        manifest
            .readLines(Charsets.UTF_8)
            .forEachIndexed { index, line ->

                if (line.isBlank()) {
                    return@forEachIndexed
                }

                val match =
                    entryPattern.matchEntire(
                        line
                    )
                        ?: throw GradleException(
                            "Invalid manifest entry at line ${index + 1}."
                        )

                val expectedHash =
                    match.groupValues[1]

                val relativePath =
                    match.groupValues[2]

                val pathSegments =
                    relativePath.split('/')

                require(
                    relativePath.isNotBlank() &&
                        !relativePath.startsWith('/') &&
                        '\\' !in relativePath
                ) {

                    "Unsafe contract path in manifest: $relativePath"
                }

                require(
                    ".." !in pathSegments &&
                        "." !in pathSegments
                ) {

                    "Unsafe contract path in manifest: $relativePath"
                }

                require(
                    expectedFiles.put(
                        relativePath,
                        expectedHash
                    ) == null
                ) {

                    "Duplicate contract path in manifest: $relativePath"
                }

                val candidate =
                    snapshot
                        .resolve(relativePath)
                        .normalize()

                require(
                    candidate.startsWith(
                        snapshot
                    )
                ) {

                    "Contract path escapes the snapshot: $relativePath"
                }

                require(
                    candidate
                        .toFile()
                        .isFile
                ) {

                    "Pinned contract file is missing: $relativePath"
                }

                val actualHash =
                    normalizedTextSha256(
                        candidate.toFile()
                    )

                require(
                    actualHash ==
                        expectedHash
                ) {

                    "Pinned contract file hash mismatch: $relativePath"
                }
            }

        val declaredFileCount =
            jsonInt(
                "fileCount"
            )

        require(
            expectedFiles.size ==
                declaredFileCount
        ) {

            "Contract file count mismatch. " +
                "Lock declares $declaredFileCount, " +
                "manifest lists ${expectedFiles.size}."
        }

        val actualFiles =
            snapshot
                .toFile()
                .walkTopDown()
                .filter {
                    it.isFile
                }
                .map {

                    snapshot
                        .relativize(
                            it.toPath()
                                .toAbsolutePath()
                                .normalize()
                        )
                        .toString()
                        .replace(
                            '\\',
                            '/'
                        )
                }
                .toSet()

        require(
            actualFiles ==
                expectedFiles.keys
        ) {

            val missing =
                expectedFiles.keys -
                    actualFiles

            val unlisted =
                actualFiles -
                    expectedFiles.keys

            "Contract snapshot inventory mismatch. " +
                "Missing: $missing; " +
                "unlisted: $unlisted"
        }

        logger.lifecycle(
            "Verified Workbench Application Catalog contract " +
                "at commit $sourceCommit " +
                "(${expectedFiles.size} files)."
        )
    }
}

val verifyWorkbenchCatalogContract by
tasks.registering(
    VerifyWorkbenchCatalogContract::class
) {

    group =
        LifecycleBasePlugin
            .VERIFICATION_GROUP

    description =
        "Verifies the pinned Workbench Application Catalog contract snapshot."

    val contractDirectory =
        layout.projectDirectory.dir(
            "contracts/workbench-catalog"
        )

    lockFile.set(
        contractDirectory.file(
            "contract-lock.json"
        )
    )

    manifestFile.set(
        contractDirectory.file(
            "workbench-catalog.sha256"
        )
    )

    snapshotDirectory.set(
        contractDirectory.dir(
            "snapshot"
        )
    )
}

tasks.named("check") {
    dependsOn(
        verifyWorkbenchCatalogContract
    )
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

application {

    mainClass.set(
        "com.alpinedigitalexperts.databaseanalyser.MainKt"
    )
}
