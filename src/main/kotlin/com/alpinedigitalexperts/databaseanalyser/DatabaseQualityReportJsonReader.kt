package com.alpinedigitalexperts.databaseanalyser

import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path


class DatabaseQualityReportJsonReader {

    private val json =
        Json {
            ignoreUnknownKeys = true
        }


    fun readFromFile(
        inputPath: Path
    ): DatabaseQualityReport {

        require(
            Files.exists(inputPath)
        ) {
            "Database quality report file does not exist: $inputPath"
        }

        require(
            Files.isRegularFile(inputPath)
        ) {
            "Database quality report path is not a file: $inputPath"
        }

        val content =
            Files.readString(
                inputPath
            )

        return json.decodeFromString(
            DatabaseQualityReport.serializer(),
            content
        )
    }
}
