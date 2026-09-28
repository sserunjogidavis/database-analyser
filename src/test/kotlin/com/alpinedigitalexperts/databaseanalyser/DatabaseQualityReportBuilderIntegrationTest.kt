package com.alpinedigitalexperts.databaseanalyser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DatabaseQualityReportBuilderIntegrationTest {

    @Test
    fun `builds structured report for reporting monthly sales`() {

        DatabaseConnector.connect().use { connection ->

            val analyzer =
                DatabaseAnalyzer(connection)

            val builder =
                DatabaseQualityReportBuilder(
                    analyzer
                )

            val report =
                builder.buildTableReport(
                    schemaName = "reporting",
                    tableName = "monthly_sales"
                )

            assertEquals(
                "companydb",
                report.databaseName
            )

            assertEquals(
                "reporting",
                report.schemaName
            )

            assertEquals(
                "monthly_sales",
                report.tableName
            )

            assertEquals(
                "companydb.reporting.monthly_sales",
                report.qualifiedName
            )

            assertEquals(
                3L,
                report.rowCount
            )

            assertEquals(
                4,
                report.columns.size
            )

            assertEquals(
                listOf("sale_id"),
                report.primaryKeys
            )

            assertTrue(
                report.uniqueColumns.isEmpty()
            )

            assertTrue(
                report.foreignKeys.isEmpty()
            )

            assertTrue(
                report.hasPrimaryKey
            )

            assertEquals(
                0L,
                report.potentialDuplicateRecordCount
            )

            // ====================================================
            // REGION
            // ====================================================

            val region =
                report.columns.single {
                    it.columnName == "region"
                }

            assertEquals(
                "character varying",
                region.dataType
            )

            assertEquals(
                false,
                region.nullable
            )

            assertEquals(
                0L,
                region.nullCount
            )

            assertEquals(
                3L,
                region.distinctCount
            )

            assertEquals(
                0L,
                region.duplicateValueCount
            )

            assertEquals(
                0L,
                region.emptyStringCount
            )

            assertEquals(
                0L,
                region.whitespaceOnlyCount
            )

            assertEquals(
                0L,
                region.leadingTrailingWhitespaceCount
            )

            assertEquals(
                null,
                region.numericStatistics
            )

            assertEquals(
                null,
                region.dateStatistics
            )

            // ====================================================
            // TOTAL AMOUNT
            // ====================================================

            val totalAmount =
                report.columns.single {
                    it.columnName == "total_amount"
                }

            assertEquals(
                "numeric",
                totalAmount.dataType
            )

            assertNotNull(
                totalAmount.numericStatistics
            )

            assertEquals(
                0L,
                totalAmount.negativeValueCount
            )

            assertEquals(
                0L,
                totalAmount.potentialNumericOutlierCount
            )

            // ====================================================
            // REPORT DATE
            // ====================================================

            val reportDate =
                report.columns.single {
                    it.columnName == "report_date"
                }

            assertEquals(
                "date",
                reportDate.dataType
            )

            assertNotNull(
                reportDate.dateStatistics
            )

            assertEquals(
                0L,
                reportDate.futureDateCount
            )

            // ====================================================
            // SALE ID
            // ====================================================

            val saleId =
                report.columns.single {
                    it.columnName == "sale_id"
                }

            assertEquals(
                "integer",
                saleId.dataType
            )

            assertEquals(
                null,
                saleId.numericStatistics
            )

            assertEquals(
                null,
                saleId.negativeValueCount
            )

            assertEquals(
                null,
                saleId.potentialNumericOutlierCount
            )

            assertEquals(
                null,
                saleId.whitespaceOnlyCount
            )

            assertEquals(
                null,
                saleId.leadingTrailingWhitespaceCount
            )
        }
    }


    @Test
    fun `builds complete database quality report`() {

        DatabaseConnector.connect().use { connection ->

            val analyzer =
                DatabaseAnalyzer(connection)

            val builder =
                DatabaseQualityReportBuilder(
                    analyzer
                )

            val report =
                builder.buildDatabaseReport()

            assertEquals(
                "companydb",
                report.databaseName
            )

            assertEquals(
                3,
                report.tables.size
            )

            assertTrue(
                report.tables.any {
                    it.schemaName == "public" &&
                        it.tableName == "departments"
                }
            )

            assertTrue(
                report.tables.any {
                    it.schemaName == "public" &&
                        it.tableName == "employees"
                }
            )

            assertTrue(
                report.tables.any {
                    it.schemaName == "reporting" &&
                        it.tableName == "monthly_sales"
                }
            )

            val summary =
                report.summary

            assertEquals(
                3,
                summary.tablesAnalysed
            )

            assertEquals(
                14,
                summary.columnsAnalysed
            )

            assertEquals(
                9L,
                summary.rowsAnalysed
            )

            assertEquals(
                0L,
                summary.primaryKeyIssues
            )

            assertEquals(
                0L,
                summary.foreignKeyIssues
            )

            assertEquals(
                0L,
                summary.duplicateValueIssues
            )

            assertEquals(
                0L,
                summary.potentialDuplicateRecordIssues
            )

            assertEquals(
                0L,
                summary.emptyStringIssues
            )

            assertEquals(
                0L,
                summary.whitespaceOnlyIssues
            )

            assertEquals(
                0L,
                summary.leadingTrailingWhitespaceIssues
            )

            assertEquals(
                0L,
                summary.emailFormatIssues
            )

            assertEquals(
                0L,
                summary.numericAnomalyIssues
            )

            assertEquals(
                0L,
                summary.potentialNumericOutlierIssues
            )

            assertEquals(
                0L,
                summary.dateAnomalyIssues
            )

            assertEquals(
                1L,
                summary.constantValueIssues
            )

            assertEquals(
                0L,
                summary.highNullPercentageIssues
            )

            assertEquals(
                1L,
                summary.totalIssues
            )

            assertEquals(
                DatabaseQualityStatus.REVIEW,
                summary.overallStatus
            )
        }
    }


    @Test
    fun `detects high null percentage in database summary`() {

        DatabaseConnector.connect().use { connection ->

            connection.autoCommit =
                false

            try {

                connection.prepareStatement(
                    """
                    UPDATE employees
                    SET salary = NULL
                    WHERE employee_id = 1
                    """.trimIndent()
                ).use { statement ->

                    statement.executeUpdate()
                }

                val analyzer =
                    DatabaseAnalyzer(
                        connection
                    )

                val builder =
                    DatabaseQualityReportBuilder(
                        analyzer
                    )

                val report =
                    builder.buildDatabaseReport()

                val employees =
                    report.tables.single {
                        it.schemaName == "public" &&
                            it.tableName == "employees"
                    }

                val salary =
                    employees.columns.single {
                        it.columnName == "salary"
                    }

                assertEquals(
                    1L,
                    salary.nullCount
                )

                assertTrue(
                    salary.nullPercentage >= 20.0
                )

                assertEquals(
                    1L,
                    report.summary.highNullPercentageIssues
                )

                assertEquals(
                    0L,
                    report.summary.whitespaceOnlyIssues
                )

                assertEquals(
                    0L,
                    report.summary.leadingTrailingWhitespaceIssues
                )

                assertEquals(
                    2L,
                    report.summary.totalIssues
                )

                assertEquals(
                    DatabaseQualityStatus.REVIEW,
                    report.summary.overallStatus
                )

            } finally {

                connection.rollback()
            }
        }
    }


    @Test
    fun `detects whitespace only text values in database summary`() {

        DatabaseConnector.connect().use { connection ->

            connection.autoCommit =
                false

            try {

                connection.prepareStatement(
                    """
                    UPDATE reporting.monthly_sales
                    SET region = '   '
                    WHERE sale_id = 1
                    """.trimIndent()
                ).use { statement ->

                    statement.executeUpdate()
                }

                val analyzer =
                    DatabaseAnalyzer(
                        connection
                    )

                val builder =
                    DatabaseQualityReportBuilder(
                        analyzer
                    )

                val report =
                    builder.buildDatabaseReport()

                val monthlySales =
                    report.tables.single {
                        it.schemaName == "reporting" &&
                            it.tableName == "monthly_sales"
                    }

                val region =
                    monthlySales.columns.single {
                        it.columnName == "region"
                    }

                assertEquals(
                    0L,
                    region.emptyStringCount
                )

                assertEquals(
                    1L,
                    region.whitespaceOnlyCount
                )

                assertEquals(
                    0L,
                    region.leadingTrailingWhitespaceCount
                )

                assertEquals(
                    1L,
                    report.summary.whitespaceOnlyIssues
                )

                assertEquals(
                    0L,
                    report.summary.leadingTrailingWhitespaceIssues
                )

                assertEquals(
                    2L,
                    report.summary.totalIssues
                )

                assertEquals(
                    DatabaseQualityStatus.REVIEW,
                    report.summary.overallStatus
                )

            } finally {

                connection.rollback()
            }
        }
    }

    @Test
    fun `detects leading and trailing whitespace in database summary`() {

        DatabaseConnector.connect().use { connection ->

            connection.autoCommit =
                false

            try {

                connection.prepareStatement(
                    """
                    UPDATE reporting.monthly_sales
                    SET region = ' East '
                    WHERE sale_id = 1
                    """.trimIndent()
                ).use { statement ->

                    statement.executeUpdate()
                }

                val analyzer =
                    DatabaseAnalyzer(
                        connection
                    )

                val builder =
                    DatabaseQualityReportBuilder(
                        analyzer
                    )

                val report =
                    builder.buildDatabaseReport()

                val monthlySales =
                    report.tables.single {
                        it.schemaName == "reporting" &&
                            it.tableName == "monthly_sales"
                    }

                val region =
                    monthlySales.columns.single {
                        it.columnName == "region"
                    }

                assertEquals(
                    0L,
                    region.emptyStringCount
                )

                assertEquals(
                    0L,
                    region.whitespaceOnlyCount
                )

                assertEquals(
                    1L,
                    region.leadingTrailingWhitespaceCount
                )

                assertEquals(
                    0L,
                    report.summary.emptyStringIssues
                )

                assertEquals(
                    0L,
                    report.summary.whitespaceOnlyIssues
                )

                assertEquals(
                    1L,
                    report.summary.leadingTrailingWhitespaceIssues
                )

                assertEquals(
                    2L,
                    report.summary.totalIssues
                )

                assertEquals(
                    DatabaseQualityStatus.REVIEW,
                    report.summary.overallStatus
                )

            } finally {

                connection.rollback()
            }
        }
    }

}

