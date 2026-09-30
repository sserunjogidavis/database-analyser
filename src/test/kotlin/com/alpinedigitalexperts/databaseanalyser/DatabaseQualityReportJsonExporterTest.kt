package com.alpinedigitalexperts.databaseanalyser

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatabaseQualityReportJsonExporterTest {

    @Test
    fun `converts database quality report to json`() {

        DatabaseConnector.connect().use { connection ->

            val analyzer =
                DatabaseAnalyzer(connection)

            val builder =
                DatabaseQualityReportBuilder(
                    analyzer
                )

            val exporter =
                DatabaseQualityReportJsonExporter()

            val report =
                builder.buildDatabaseReport()

            val json =
                exporter.toJson(
                    report
                )

            assertTrue(
                json.contains(
                    "\"databaseName\": \"companydb\""
                )
            )

            assertTrue(
                json.contains(
                    "\"tablesAnalysed\": 3"
                )
            )

            assertTrue(
                json.contains(
                    "\"columnsAnalysed\": 14"
                )
            )

            assertTrue(
                json.contains(
                    "\"rowsAnalysed\": 9"
                )
            )

            assertTrue(
                json.contains(
                    "\"constantValueIssues\": 1"
                )
            )

            assertTrue(
                json.contains(
                    "\"highNullPercentageIssues\": 0"
                )
            )

            assertTrue(
                json.contains(
                    "\"whitespaceOnlyIssues\": 0"
                )
            )

            assertTrue(
                json.contains(
                    "\"leadingTrailingWhitespaceIssues\": 0"
                )
            )

            assertTrue(
                json.contains(
                    "\"totalIssues\": 1"
                )
            )

            assertTrue(
                json.contains(
                    "\"overallStatus\": \"REVIEW\""
                )
            )

            assertTrue(
                json.contains(
                    "\"schemaName\": \"reporting\""
                )
            )

            assertTrue(
                json.contains(
                    "\"tableName\": \"monthly_sales\""
                )
            )

            assertTrue(
                json.contains(
                    "\"whitespaceOnlyCount\": 0"
                )
            )

            assertTrue(
                json.contains(
                    "\"leadingTrailingWhitespaceCount\": 0"
                )
            )

            assertTrue(
                json.contains(
                    "\"minimum\": \"1500000.00\""
                )
            )

            assertTrue(
                json.contains(
                    "\"earliest\": \"2026-08-01\""
                )
            )

            // ====================================================
            // GROUPED AGGREGATE JSON
            // ====================================================

            assertTrue(
                json.contains(
                    "\"groupedAggregates\""
                )
            )

            assertTrue(
                json.contains(
                    "\"groupByColumn\": \"region\""
                )
            )

            assertTrue(
                json.contains(
                    "\"numericColumn\": \"total_amount\""
                )
            )

            assertTrue(
                json.contains(
                    "\"groupValue\": \"Central\""
                )
            )

            assertTrue(
                json.contains(
                    "\"groupValue\": \"Eastern\""
                )
            )

            assertTrue(
                json.contains(
                    "\"groupValue\": \"Western\""
                )
            )

            assertTrue(
                json.contains(
                    "\"total\": \"1500000.00\""
                )
            )

            assertTrue(
                json.contains(
                    "\"total\": \"2100000.00\""
                )
            )

            assertTrue(
                json.contains(
                    "\"total\": \"1800000.00\""
                )
            )
        }
    }


    @Test
    fun `writes database quality report to json file`() {

        DatabaseConnector.connect().use { connection ->

            val analyzer =
                DatabaseAnalyzer(connection)

            val builder =
                DatabaseQualityReportBuilder(
                    analyzer
                )

            val exporter =
                DatabaseQualityReportJsonExporter()

            val report =
                builder.buildDatabaseReport()

            val temporaryDirectory =
                Files.createTempDirectory(
                    "database-analyser-json-test"
                )

            val outputPath =
                temporaryDirectory.resolve(
                    "database-quality-report.json"
                )

            val writtenPath =
                exporter.writeToFile(
                    report = report,
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

            val json =
                Files.readString(
                    outputPath
                )

            assertTrue(
                json.contains(
                    "\"databaseName\": \"companydb\""
                )
            )

            assertTrue(
                json.contains(
                    "\"constantValueIssues\": 1"
                )
            )

            assertTrue(
                json.contains(
                    "\"highNullPercentageIssues\": 0"
                )
            )

            assertTrue(
                json.contains(
                    "\"whitespaceOnlyIssues\": 0"
                )
            )

            assertTrue(
                json.contains(
                    "\"leadingTrailingWhitespaceIssues\": 0"
                )
            )

            assertTrue(
                json.contains(
                    "\"whitespaceOnlyCount\": 0"
                )
            )

            assertTrue(
                json.contains(
                    "\"leadingTrailingWhitespaceCount\": 0"
                )
            )

            assertTrue(
                json.contains(
                    "\"totalIssues\": 1"
                )
            )

            assertTrue(
                json.contains(
                    "\"overallStatus\": \"REVIEW\""
                )
            )

            // ====================================================
            // GROUPED AGGREGATE FILE JSON
            // ====================================================

            assertTrue(
                json.contains(
                    "\"groupedAggregates\""
                )
            )

            assertTrue(
                json.contains(
                    "\"groupByColumn\": \"region\""
                )
            )

            assertTrue(
                json.contains(
                    "\"numericColumn\": \"total_amount\""
                )
            )

            assertTrue(
                json.contains(
                    "\"groupValue\": \"Central\""
                )
            )

            assertTrue(
                json.contains(
                    "\"groupValue\": \"Eastern\""
                )
            )

            assertTrue(
                json.contains(
                    "\"groupValue\": \"Western\""
                )
            )

            assertTrue(
                json.contains(
                    "\"total\": \"1500000.00\""
                )
            )

            assertTrue(
                json.contains(
                    "\"total\": \"2100000.00\""
                )
            )

            assertTrue(
                json.contains(
                    "\"total\": \"1800000.00\""
                )
            )
        }
    }
}
