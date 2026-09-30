package com.alpinedigitalexperts.databaseanalyser

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatabaseQualityReportHtmlExporterTest {

    @Test
    fun `converts database quality report to html`() {

        DatabaseConnector.connect().use { connection ->

            val analyzer =
                DatabaseAnalyzer(connection)

            val builder =
                DatabaseQualityReportBuilder(
                    analyzer
                )

            val exporter =
                DatabaseQualityReportHtmlExporter()

            val report =
                builder.buildDatabaseReport()

            val html =
                exporter.toHtml(
                    report
                )

            assertTrue(
                html.contains(
                    "<!DOCTYPE html>"
                )
            )

            assertTrue(
                html.contains(
                    "<title>Database Quality Report</title>"
                )
            )

            assertTrue(
                html.contains(
                    "companydb"
                )
            )

            assertTrue(
                html.contains(
                    "public.departments"
                )
            )

            assertTrue(
                html.contains(
                    "public.employees"
                )
            )

            assertTrue(
                html.contains(
                    "reporting.monthly_sales"
                )
            )

            assertTrue(
                html.contains(
                    "Tables Analysed"
                )
            )

            assertTrue(
                html.contains(
                    "Overall Status"
                )
            )

            assertTrue(
                html.contains(
                    "REVIEW"
                )
            )

            assertTrue(
                html.contains(
                    "Constant Value Issues"
                )
            )

            assertTrue(
                html.contains(
                    "High NULL Percentage Issues"
                )
            )

            assertTrue(
                html.contains(
                    "Whitespace-Only Issues"
                )
            )

            assertTrue(
                html.contains(
                    "Whitespace-Only Values"
                )
            )

            assertTrue(
                html.contains(
                    "Leading/Trailing Whitespace Issues"
                )
            )

            assertTrue(
                html.contains(
                    "Leading/Trailing Whitespace"
                )
            )

            assertTrue(
                html.contains(
                    "1500000.00"
                )
            )

            assertTrue(
                html.contains(
                    "2026-08-01"
                )
            )

            // ====================================================
            // GROUPED AGGREGATE HTML
            // ====================================================

            assertTrue(
                html.contains(
                    "Grouped Aggregate Analysis"
                )
            )

            assertTrue(
                html.contains(
                    "<strong>Group By:</strong> region"
                )
            )

            assertTrue(
                html.contains(
                    "<strong>Numeric Column:</strong> total_amount"
                )
            )

            assertTrue(
                html.contains(
                    "Central"
                )
            )

            assertTrue(
                html.contains(
                    "Eastern"
                )
            )

            assertTrue(
                html.contains(
                    "Western"
                )
            )

            assertTrue(
                html.contains(
                    "2100000.00"
                )
            )

            assertTrue(
                html.contains(
                    "1800000.00"
                )
            )
        }
    }


    @Test
    fun `writes database quality report to html file`() {

        DatabaseConnector.connect().use { connection ->

            val analyzer =
                DatabaseAnalyzer(connection)

            val builder =
                DatabaseQualityReportBuilder(
                    analyzer
                )

            val exporter =
                DatabaseQualityReportHtmlExporter()

            val report =
                builder.buildDatabaseReport()

            val temporaryDirectory =
                Files.createTempDirectory(
                    "database-analyser-html-test"
                )

            val outputPath =
                temporaryDirectory.resolve(
                    "database-quality-report.html"
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

            val html =
                Files.readString(
                    outputPath
                )

            assertTrue(
                html.contains(
                    "Database Quality Report"
                )
            )

            assertTrue(
                html.contains(
                    "reporting.monthly_sales"
                )
            )

            assertTrue(
                html.contains(
                    "High NULL Percentage Issues"
                )
            )

            assertTrue(
                html.contains(
                    "Whitespace-Only Issues"
                )
            )

            assertTrue(
                html.contains(
                    "Whitespace-Only Values"
                )
            )

            assertTrue(
                html.contains(
                    "Leading/Trailing Whitespace Issues"
                )
            )

            assertTrue(
                html.contains(
                    "Leading/Trailing Whitespace"
                )
            )

            assertTrue(
                html.contains(
                    "REVIEW"
                )
            )

            // ====================================================
            // GROUPED AGGREGATE FILE HTML
            // ====================================================

            assertTrue(
                html.contains(
                    "Grouped Aggregate Analysis"
                )
            )

            assertTrue(
                html.contains(
                    "<strong>Group By:</strong> region"
                )
            )

            assertTrue(
                html.contains(
                    "<strong>Numeric Column:</strong> total_amount"
                )
            )

            assertTrue(
                html.contains(
                    "Central"
                )
            )

            assertTrue(
                html.contains(
                    "Eastern"
                )
            )

            assertTrue(
                html.contains(
                    "Western"
                )
            )

            assertTrue(
                html.contains(
                    "1500000.00"
                )
            )

            assertTrue(
                html.contains(
                    "2100000.00"
                )
            )

            assertTrue(
                html.contains(
                    "1800000.00"
                )
            )
        }
    }
}
