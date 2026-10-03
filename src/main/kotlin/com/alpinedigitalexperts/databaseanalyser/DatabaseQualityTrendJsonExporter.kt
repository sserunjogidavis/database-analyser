package com.alpinedigitalexperts.databaseanalyser

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path


class DatabaseQualityTrendJsonExporter {

    private val json =
        Json {
            prettyPrint = true
            encodeDefaults = true
        }


    fun toJson(
        trend: DatabaseQualityTrend
    ): String {

        return json.encodeToString(
            trend
        )
    }


    fun writeToFile(
        trend: DatabaseQualityTrend,
        outputPath: Path
    ): Path {

        val parent =
            outputPath.parent

        if (parent != null) {
            Files.createDirectories(parent)
        }

        Files.writeString(
            outputPath,
            toJson(trend)
        )

        return outputPath
    }
}
