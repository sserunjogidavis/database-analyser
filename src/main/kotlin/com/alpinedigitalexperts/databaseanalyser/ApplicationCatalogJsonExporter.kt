package com.alpinedigitalexperts.databaseanalyser

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ApplicationCatalogJsonExporter {

    private val json =
        Json {
            prettyPrint = true

            // Important for the Workbench schema:
            // optional null properties should be omitted.
            explicitNulls = false

            encodeDefaults = true
        }


    // ============================================================
    // CONVERT CATALOG TO JSON
    // ============================================================

    fun toJson(
        catalog: ApplicationCatalog
    ): String {

        return json.encodeToString(
            catalog
        )
    }


    // ============================================================
    // WRITE CATALOG TO FILE
    // ============================================================

    fun writeToFile(
        catalog: ApplicationCatalog,
        outputPath: Path
    ): Path {

        val parentDirectory =
            outputPath.parent

        if (parentDirectory != null) {

            Files.createDirectories(
                parentDirectory
            )
        }

        Files.writeString(
            outputPath,
            toJson(catalog),
            StandardCharsets.UTF_8
        )

        return outputPath
    }
}
