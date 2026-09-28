package com.alpinedigitalexperts.databaseanalyser

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DatabaseQualityReportJsonExporter {

    private val json =
        Json {
            prettyPrint = true
            encodeDefaults = true
        }

    fun toJson(
        report: DatabaseQualityReport
    ): String {

        return json.encodeToString(
            report
        )
    }

    fun writeToFile(
        report: DatabaseQualityReport,
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
            toJson(report),
            StandardCharsets.UTF_8
        )

        return outputPath
    }
}
