package com.alpinedigitalexperts.databaseanalyser

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatabaseQualityComparisonJsonExporterTest {

    @Test
    fun `converts database quality comparison to json`() {

        val comparison =
            comparison()

        val exporter =
            DatabaseQualityComparisonJsonExporter()

        val json =
            exporter.toJson(
                comparison
            )

        assertTrue(
            json.contains(
                "\"databaseName\": \"companydb\""
            )
        )

        assertTrue(
            json.contains(
                "\"previousTotalIssues\": 5"
            )
        )

        assertTrue(
            json.contains(
                "\"currentTotalIssues\": 2"
            )
        )

        assertTrue(
            json.contains(
                "\"totalIssueChange\": -3"
            )
        )

        assertTrue(
            json.contains(
                "\"outcome\": \"IMPROVED\""
            )
        )

        assertTrue(
            json.contains(
                "\"category\": \"Primary Key Issues\""
            )
        )

        assertTrue(
            json.contains(
                "\"change\": -1"
            )
        )
    }


    @Test
    fun `writes database quality comparison to json file`() {

        val comparison =
            comparison()

        val temporaryDirectory =
            Files.createTempDirectory(
                "database-quality-comparison-exporter-test"
            )

        val outputPath =
            temporaryDirectory.resolve(
                "database-quality-comparison.json"
            )

        try {

            val exporter =
                DatabaseQualityComparisonJsonExporter()

            val writtenPath =
                exporter.writeToFile(
                    comparison = comparison,
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
                    "\"outcome\": \"IMPROVED\""
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
    fun `creates parent directory when writing comparison file`() {

        val comparison =
            comparison()

        val temporaryDirectory =
            Files.createTempDirectory(
                "database-quality-comparison-parent-test"
            )

        val nestedDirectory =
            temporaryDirectory.resolve(
                "nested"
            )

        val outputPath =
            nestedDirectory.resolve(
                "database-quality-comparison.json"
            )

        try {

            val exporter =
                DatabaseQualityComparisonJsonExporter()

            exporter.writeToFile(
                comparison = comparison,
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


    private fun comparison(): DatabaseQualityComparison {

        return DatabaseQualityComparison(
            databaseName = "companydb",

            previousTotalIssues = 5L,
            currentTotalIssues = 2L,
            totalIssueChange = -3L,

            previousStatus =
                DatabaseQualityStatus.REVIEW,

            currentStatus =
                DatabaseQualityStatus.GOOD,

            outcome =
                DatabaseQualityComparisonOutcome.IMPROVED,

            categoryChanges =
                listOf(
                    DatabaseQualityCategoryChange(
                        category = "Primary Key Issues",
                        previousValue = 1L,
                        currentValue = 0L,
                        change = -1L
                    ),
                    DatabaseQualityCategoryChange(
                        category = "Foreign Key Issues",
                        previousValue = 2L,
                        currentValue = 1L,
                        change = -1L
                    ),
                    DatabaseQualityCategoryChange(
                        category = "Email Format Issues",
                        previousValue = 2L,
                        currentValue = 1L,
                        change = -1L
                    )
                )
        )
    }
}
