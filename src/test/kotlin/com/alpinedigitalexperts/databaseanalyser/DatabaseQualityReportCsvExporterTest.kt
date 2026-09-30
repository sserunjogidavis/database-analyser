package com.alpinedigitalexperts.databaseanalyser

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatabaseQualityReportCsvExporterTest {

    @Test
    fun `exports database quality report to csv`() {

        DatabaseConnector.connect().use { connection ->

            val analyzer =
                DatabaseAnalyzer(
                    connection
                )

            val reportBuilder =
                DatabaseQualityReportBuilder(
                    analyzer
                )

            val report =
                reportBuilder
                    .buildDatabaseReport()

            val exporter =
                DatabaseQualityReportCsvExporter()

            val csv =
                exporter.toCsv(
                    report
                )

            assertTrue(
                csv.contains(
                    "Database,Schema,Table,Column"
                )
            )

            assertTrue(
                csv.contains(
                    "Whitespace-Only Count"
                )
            )

            assertTrue(
                csv.contains(
                    "Leading/Trailing Whitespace Count"
                )
            )

            assertTrue(
                csv.contains(
                    "companydb"
                )
            )

            assertTrue(
                csv.contains(
                    "employees"
                )
            )

            assertTrue(
                csv.contains(
                    "salary"
                )
            )

            assertTrue(
                csv.contains(
                    "email"
                )
            )

            assertTrue(
                csv.contains(
                    "reporting"
                )
            )

            assertTrue(
                csv.contains(
                    "monthly_sales"
                )
            )

            // ====================================================
            // GROUPED AGGREGATE CSV
            // ====================================================

            assertTrue(
                csv.contains(
                    "GROUPED AGGREGATE ANALYSIS"
                )
            )

            assertTrue(
                csv.contains(
                    "Group By Column,Numeric Column,Group Value,Row Count,Minimum,Maximum,Average,Total"
                )
            )

            assertTrue(
                csv.contains(
                    "companydb,reporting,monthly_sales,region,total_amount,Central,1"
                )
            )

            assertTrue(
                csv.contains(
                    "companydb,reporting,monthly_sales,region,total_amount,Eastern,1"
                )
            )

            assertTrue(
                csv.contains(
                    "companydb,reporting,monthly_sales,region,total_amount,Western,1"
                )
            )

            assertTrue(
                csv.contains(
                    "1500000.00"
                )
            )

            assertTrue(
                csv.contains(
                    "2100000.00"
                )
            )

            assertTrue(
                csv.contains(
                    "1800000.00"
                )
            )

            assertTrue(
                csv.contains(
                    "SUMMARY"
                )
            )

            assertTrue(
                csv.contains(
                    "Metric,Value"
                )
            )

            assertTrue(
                csv.contains(
                    "Tables Analysed,3"
                )
            )

            assertTrue(
                csv.contains(
                    "Columns Analysed,14"
                )
            )

            assertTrue(
                csv.contains(
                    "Rows Analysed,9"
                )
            )

            assertTrue(
                csv.contains(
                    "Whitespace-Only Issues,0"
                )
            )

            assertTrue(
                csv.contains(
                    "Leading/Trailing Whitespace Issues,0"
                )
            )

            assertTrue(
                csv.contains(
                    "Constant Value Issues,1"
                )
            )

            assertTrue(
                csv.contains(
                    "High NULL Percentage Issues,0"
                )
            )

            assertTrue(
                csv.contains(
                    "Total Issues,1"
                )
            )

            assertTrue(
                csv.contains(
                    "Overall Status,REVIEW"
                )
            )
        }
    }


    @Test
    fun `writes database quality report to csv file`() {

        DatabaseConnector.connect().use { connection ->

            val analyzer =
                DatabaseAnalyzer(
                    connection
                )

            val reportBuilder =
                DatabaseQualityReportBuilder(
                    analyzer
                )

            val report =
                reportBuilder
                    .buildDatabaseReport()

            val exporter =
                DatabaseQualityReportCsvExporter()

            val temporaryDirectory =
                Files.createTempDirectory(
                    "database-analyser-csv-test"
                )

            val outputPath =
                temporaryDirectory.resolve(
                    "database-quality-report.csv"
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

            val csv =
                Files.readString(
                    outputPath
                )

            assertTrue(
                csv.contains(
                    "Whitespace-Only Count"
                )
            )

            assertTrue(
                csv.contains(
                    "Leading/Trailing Whitespace Count"
                )
            )

            // ====================================================
            // GROUPED AGGREGATE FILE CSV
            // ====================================================

            assertTrue(
                csv.contains(
                    "GROUPED AGGREGATE ANALYSIS"
                )
            )

            assertTrue(
                csv.contains(
                    "Group By Column,Numeric Column,Group Value,Row Count,Minimum,Maximum,Average,Total"
                )
            )

            assertTrue(
                csv.contains(
                    "companydb,reporting,monthly_sales,region,total_amount,Central,1"
                )
            )

            assertTrue(
                csv.contains(
                    "companydb,reporting,monthly_sales,region,total_amount,Eastern,1"
                )
            )

            assertTrue(
                csv.contains(
                    "companydb,reporting,monthly_sales,region,total_amount,Western,1"
                )
            )

            assertTrue(
                csv.contains(
                    "1500000.00"
                )
            )

            assertTrue(
                csv.contains(
                    "2100000.00"
                )
            )

            assertTrue(
                csv.contains(
                    "1800000.00"
                )
            )

            assertTrue(
                csv.contains(
                    "SUMMARY"
                )
            )

            assertTrue(
                csv.contains(
                    "Whitespace-Only Issues,0"
                )
            )

            assertTrue(
                csv.contains(
                    "Leading/Trailing Whitespace Issues,0"
                )
            )

            assertTrue(
                csv.contains(
                    "Constant Value Issues,1"
                )
            )

            assertTrue(
                csv.contains(
                    "High NULL Percentage Issues,0"
                )
            )

            assertTrue(
                csv.contains(
                    "Total Issues,1"
                )
            )

            assertTrue(
                csv.contains(
                    "Overall Status,REVIEW"
                )
            )
        }
    }
}
