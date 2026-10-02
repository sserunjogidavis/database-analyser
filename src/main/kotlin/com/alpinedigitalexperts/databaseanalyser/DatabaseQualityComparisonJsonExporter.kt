package com.alpinedigitalexperts.databaseanalyser

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path


class DatabaseQualityComparisonJsonExporter {

    private val json =
        Json {
            prettyPrint = true
            encodeDefaults = true
        }


    fun toJson(
        comparison: DatabaseQualityComparison
    ): String {

        return json.encodeToString(
            comparison
        )
    }


    fun writeToFile(
        comparison: DatabaseQualityComparison,
        outputPath: Path
    ): Path {

        val parent =
            outputPath.parent

        if (parent != null) {
            Files.createDirectories(parent)
        }

        Files.writeString(
            outputPath,
            toJson(comparison)
        )

        return outputPath
    }
}
