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

        assertTrue(
            json.contains(
                "\"primaryKeyIssues\": 2"
            )
        )

        assertTrue(
            json.contains(
                "\"foreignKeyIssues\": 1"
            )
        )

        assertTrue(
            json.contains(
                "\"emailFormatIssues\": 1"
            )
        )

        assertTrue(
            json.contains(
                "\"constantValueIssues\": 1"
            )
        )

        assertTrue(
            json.contains(
                "\"category\": \"Primary Key Issues\""
            )
        )

        assertTrue(
            json.contains(
                "\"firstValue\": 2"
            )
        )

        assertTrue(
            json.contains(
                "\"latestValue\": 0"
            )
        )

        assertTrue(
            json.contains(
                "\"change\": -2"
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

            assertTrue(
                content.contains(
                    "\"category\": \"Foreign Key Issues\""
                )
            )

            assertTrue(
                content.contains(
                    "\"change\": -1"
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
                    snapshot(
                        sequence = 1,
                        totalIssues = 8L,
                        overallStatus =
                            DatabaseQualityStatus.ATTENTION_REQUIRED,

                        primaryKeyIssues = 2L,
                        foreignKeyIssues = 1L,
                        duplicateValueIssues = 1L,
                        potentialDuplicateRecordIssues = 1L,
                        emptyStringIssues = 1L,
                        whitespaceOnlyIssues = 0L,
                        leadingTrailingWhitespaceIssues = 0L,
                        emailFormatIssues = 1L,
                        numericAnomalyIssues = 0L,
                        potentialNumericOutlierIssues = 0L,
                        dateAnomalyIssues = 0L,
                        constantValueIssues = 1L,
                        highNullPercentageIssues = 0L
                    ),

                    snapshot(
                        sequence = 2,
                        totalIssues = 5L,
                        overallStatus =
                            DatabaseQualityStatus.REVIEW,

                        primaryKeyIssues = 1L,
                        foreignKeyIssues = 1L,
                        duplicateValueIssues = 1L,
                        potentialDuplicateRecordIssues = 0L,
                        emptyStringIssues = 1L,
                        whitespaceOnlyIssues = 0L,
                        leadingTrailingWhitespaceIssues = 0L,
                        emailFormatIssues = 1L,
                        numericAnomalyIssues = 0L,
                        potentialNumericOutlierIssues = 0L,
                        dateAnomalyIssues = 0L,
                        constantValueIssues = 0L,
                        highNullPercentageIssues = 0L
                    ),

                    snapshot(
                        sequence = 3,
                        totalIssues = 2L,
                        overallStatus =
                            DatabaseQualityStatus.REVIEW,

                        primaryKeyIssues = 0L,
                        foreignKeyIssues = 0L,
                        duplicateValueIssues = 0L,
                        potentialDuplicateRecordIssues = 0L,
                        emptyStringIssues = 0L,
                        whitespaceOnlyIssues = 0L,
                        leadingTrailingWhitespaceIssues = 0L,
                        emailFormatIssues = 1L,
                        numericAnomalyIssues = 0L,
                        potentialNumericOutlierIssues = 0L,
                        dateAnomalyIssues = 0L,
                        constantValueIssues = 1L,
                        highNullPercentageIssues = 0L
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
                DatabaseQualityTrendOutcome.IMPROVING,

            categoryChanges =
                listOf(
                    DatabaseQualityTrendCategoryChange(
                        category = "Primary Key Issues",
                        firstValue = 2L,
                        latestValue = 0L,
                        change = -2L
                    ),
                    DatabaseQualityTrendCategoryChange(
                        category = "Foreign Key Issues",
                        firstValue = 1L,
                        latestValue = 0L,
                        change = -1L
                    ),
                    DatabaseQualityTrendCategoryChange(
                        category = "Duplicate Value Issues",
                        firstValue = 1L,
                        latestValue = 0L,
                        change = -1L
                    ),
                    DatabaseQualityTrendCategoryChange(
                        category = "Potential Duplicate Record Issues",
                        firstValue = 1L,
                        latestValue = 0L,
                        change = -1L
                    ),
                    DatabaseQualityTrendCategoryChange(
                        category = "Empty String Issues",
                        firstValue = 1L,
                        latestValue = 0L,
                        change = -1L
                    ),
                    DatabaseQualityTrendCategoryChange(
                        category = "Whitespace-Only Issues",
                        firstValue = 0L,
                        latestValue = 0L,
                        change = 0L
                    ),
                    DatabaseQualityTrendCategoryChange(
                        category = "Leading/Trailing Whitespace Issues",
                        firstValue = 0L,
                        latestValue = 0L,
                        change = 0L
                    ),
                    DatabaseQualityTrendCategoryChange(
                        category = "Email Format Issues",
                        firstValue = 1L,
                        latestValue = 1L,
                        change = 0L
                    ),
                    DatabaseQualityTrendCategoryChange(
                        category = "Numeric Anomaly Issues",
                        firstValue = 0L,
                        latestValue = 0L,
                        change = 0L
                    ),
                    DatabaseQualityTrendCategoryChange(
                        category = "Potential Numeric Outlier Issues",
                        firstValue = 0L,
                        latestValue = 0L,
                        change = 0L
                    ),
                    DatabaseQualityTrendCategoryChange(
                        category = "Date Anomaly Issues",
                        firstValue = 0L,
                        latestValue = 0L,
                        change = 0L
                    ),
                    DatabaseQualityTrendCategoryChange(
                        category = "Constant Value Issues",
                        firstValue = 1L,
                        latestValue = 1L,
                        change = 0L
                    ),
                    DatabaseQualityTrendCategoryChange(
                        category = "High NULL Percentage Issues",
                        firstValue = 0L,
                        latestValue = 0L,
                        change = 0L
                    )
                )
        )
    }


    private fun snapshot(
        sequence: Int,
        totalIssues: Long,
        overallStatus: DatabaseQualityStatus,

        primaryKeyIssues: Long,
        foreignKeyIssues: Long,
        duplicateValueIssues: Long,
        potentialDuplicateRecordIssues: Long,
        emptyStringIssues: Long,
        whitespaceOnlyIssues: Long,
        leadingTrailingWhitespaceIssues: Long,
        emailFormatIssues: Long,
        numericAnomalyIssues: Long,
        potentialNumericOutlierIssues: Long,
        dateAnomalyIssues: Long,
        constantValueIssues: Long,
        highNullPercentageIssues: Long
    ): DatabaseQualityTrendSnapshot {

        return DatabaseQualityTrendSnapshot(
            sequence = sequence,

            totalIssues = totalIssues,
            overallStatus = overallStatus,

            primaryKeyIssues = primaryKeyIssues,
            foreignKeyIssues = foreignKeyIssues,
            duplicateValueIssues = duplicateValueIssues,
            potentialDuplicateRecordIssues =
                potentialDuplicateRecordIssues,
            emptyStringIssues = emptyStringIssues,
            whitespaceOnlyIssues = whitespaceOnlyIssues,
            leadingTrailingWhitespaceIssues =
                leadingTrailingWhitespaceIssues,
            emailFormatIssues = emailFormatIssues,
            numericAnomalyIssues = numericAnomalyIssues,
            potentialNumericOutlierIssues =
                potentialNumericOutlierIssues,
            dateAnomalyIssues = dateAnomalyIssues,
            constantValueIssues = constantValueIssues,
            highNullPercentageIssues =
                highNullPercentageIssues
        )
    }
}
