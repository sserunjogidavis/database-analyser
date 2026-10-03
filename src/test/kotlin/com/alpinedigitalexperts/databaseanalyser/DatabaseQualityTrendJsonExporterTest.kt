package com.alpinedigitalexperts.databaseanalyser

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatabaseQualityTrendJsonExporterTest {

    @Test
    fun `converts database quality trend to json`() {

        val trend =
            trend()

        val exporter =
            DatabaseQualityTrendJsonExporter()

        val json =
            exporter.toJson(
                trend
            )

        assertTrue(
            json.contains(
                "\"databaseName\": \"companydb\""
            )
        )

        assertTrue(
            json.contains(
                "\"firstTotalIssues\": 8"
            )
        )

        assertTrue(
            json.contains(
                "\"latestTotalIssues\": 2"
            )
        )

        assertTrue(
            json.contains(
                "\"totalIssueChange\": -6"
            )
        )

        assertTrue(
            json.contains(
                "\"outcome\": \"IMPROVING\""
            )
        )

        assertTrue(
            json.contains(
                "\"sequence\": 1"
            )
        )

        assertTrue(
            json.contains(
                "\"sequence\": 3"
            )
        )
    }


    @Test
    fun `writes database quality trend to json file`() {

        val trend =
            trend()

        val temporaryDirectory =
            Files.createTempDirectory(
                "database-quality-trend-exporter-test"
            )

        val outputPath =
            temporaryDirectory.resolve(
                "database-quality-trend.json"
            )

        try {

            val exporter =
                DatabaseQualityTrendJsonExporter()

            val writtenPath =
                exporter.writeToFile(
                    trend = trend,
                    outputPath = outputPath
                )

            assertEquals(
                outputPath,
                writtenPath
            )

            assertTrue(
                Files.exists(
                    outputPath
                )
            )

            val content =
                Files.readString(
                    outputPath
                )

            assertTrue(
                content.contains(
                    "\"databaseName\": \"companydb\""
                )
            )

            assertTrue(
                content.contains(
                    "\"outcome\": \"IMPROVING\""
                )
            )

        } finally {

            Files.deleteIfExists(
                outputPath
            )

            Files.deleteIfExists(
                temporaryDirectory
            )
        }
    }


    @Test
    fun `creates parent directory when writing trend file`() {

        val trend =
            trend()

        val temporaryDirectory =
            Files.createTempDirectory(
                "database-quality-trend-parent-test"
            )

        val nestedDirectory =
            temporaryDirectory.resolve(
                "nested"
            )

        val outputPath =
            nestedDirectory.resolve(
                "database-quality-trend.json"
            )

        try {

            val exporter =
                DatabaseQualityTrendJsonExporter()

            exporter.writeToFile(
                trend = trend,
                outputPath = outputPath
            )

            assertTrue(
                Files.exists(
                    outputPath
                )
            )

        } finally {

            Files.deleteIfExists(
                outputPath
            )

            Files.deleteIfExists(
                nestedDirectory
            )

            Files.deleteIfExists(
                temporaryDirectory
            )
        }
    }


    private fun trend(): DatabaseQualityTrend {

        return DatabaseQualityTrend(
            databaseName = "companydb",

            snapshots =
                listOf(
                    DatabaseQualityTrendSnapshot(
                        sequence = 1,
                        totalIssues = 8L,
                        overallStatus =
                            DatabaseQualityStatus.ATTENTION_REQUIRED
                    ),
                    DatabaseQualityTrendSnapshot(
                        sequence = 2,
                        totalIssues = 5L,
                        overallStatus =
                            DatabaseQualityStatus.REVIEW
                    ),
                    DatabaseQualityTrendSnapshot(
                        sequence = 3,
                        totalIssues = 2L,
                        overallStatus =
                            DatabaseQualityStatus.REVIEW
                    )
                ),

            firstTotalIssues = 8L,
            latestTotalIssues = 2L,
            totalIssueChange = -6L,

            firstStatus =
                DatabaseQualityStatus.ATTENTION_REQUIRED,

            latestStatus =
                DatabaseQualityStatus.REVIEW,

            outcome =
                DatabaseQualityTrendOutcome.IMPROVING
        )
    }
}
