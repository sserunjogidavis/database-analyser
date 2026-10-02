package com.alpinedigitalexperts.databaseanalyser

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DatabaseQualityReportJsonReaderTest {

    @Test
    fun `reads exported database quality report from json file`() {

        val report =
            DatabaseQualityReport(
                databaseName = "companydb",
                tables = emptyList(),
                summary =
                    DatabaseQualitySummary(
                        tablesAnalysed = 3,
                        columnsAnalysed = 14,
                        rowsAnalysed = 9L,

                        primaryKeyIssues = 0L,
                        foreignKeyIssues = 0L,
                        duplicateValueIssues = 0L,
                        potentialDuplicateRecordIssues = 0L,
                        emptyStringIssues = 0L,
                        whitespaceOnlyIssues = 0L,
                        leadingTrailingWhitespaceIssues = 0L,
                        emailFormatIssues = 0L,
                        numericAnomalyIssues = 0L,
                        potentialNumericOutlierIssues = 0L,
                        dateAnomalyIssues = 0L,
                        constantValueIssues = 1L,
                        highNullPercentageIssues = 0L,

                        totalIssues = 1L,
                        overallStatus =
                            DatabaseQualityStatus.REVIEW
                    )
            )

        val temporaryDirectory =
            Files.createTempDirectory(
                "database-quality-reader-test"
            )

        val reportPath =
            temporaryDirectory.resolve(
                "database-quality-report.json"
            )

        try {

            val exporter =
                DatabaseQualityReportJsonExporter()

            exporter.writeToFile(
                report = report,
                outputPath = reportPath
            )

            val reader =
                DatabaseQualityReportJsonReader()

            val loadedReport =
                reader.readFromFile(
                    reportPath
                )

            assertEquals(
                report,
                loadedReport
            )

        } finally {

            Files.deleteIfExists(
                reportPath
            )

            Files.deleteIfExists(
                temporaryDirectory
            )
        }
    }


    @Test
    fun `rejects missing report file`() {

        val temporaryDirectory =
            Files.createTempDirectory(
                "database-quality-reader-missing-test"
            )

        val missingPath =
            temporaryDirectory.resolve(
                "missing-report.json"
            )

        try {

            val reader =
                DatabaseQualityReportJsonReader()

            assertFailsWith<IllegalArgumentException> {

                reader.readFromFile(
                    missingPath
                )
            }

        } finally {

            Files.deleteIfExists(
                temporaryDirectory
            )
        }
    }


    @Test
    fun `rejects directory instead of report file`() {

        val temporaryDirectory =
            Files.createTempDirectory(
                "database-quality-reader-directory-test"
            )

        try {

            val reader =
                DatabaseQualityReportJsonReader()

            assertFailsWith<IllegalArgumentException> {

                reader.readFromFile(
                    temporaryDirectory
                )
            }

        } finally {

            Files.deleteIfExists(
                temporaryDirectory
            )
        }
    }


    @Test
    fun `rejects invalid json report`() {

        val temporaryDirectory =
            Files.createTempDirectory(
                "database-quality-reader-invalid-json-test"
            )

        val reportPath =
            temporaryDirectory.resolve(
                "invalid-report.json"
            )

        try {

            Files.writeString(
                reportPath,
                """
                {
                  "databaseName": "companydb",
                  "tables": [
                }
                """.trimIndent()
            )

            val reader =
                DatabaseQualityReportJsonReader()

            assertFailsWith<Exception> {

                reader.readFromFile(
                    reportPath
                )
            }

        } finally {

            Files.deleteIfExists(
                reportPath
            )

            Files.deleteIfExists(
                temporaryDirectory
            )
        }
    }
}
