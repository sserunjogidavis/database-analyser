package com.alpinedigitalexperts.databaseanalyser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// TODO(@sserunjogidavis): The test data is too small to validate the quality analysis.
// public.departments and public.employees hold ~3 rows each (docs section 39), and
// aggregate_analysis_test below has 3 rows. At that size:
//   - outlier detection (median/MAD, modified z > 3.5) needs >= 3 values to run at all
//     and practically never fires on 3, so "0 outliers" proves nothing;
//   - the high-NULL thresholds (20% / 50%) move in 33% steps, so one NULL = WARNING;
//   - grouped aggregates and duplicate-record checks collapse to 1-2 rows per group.
// Suggestion: seed fixtures with planted defects so every finding type has a case that
// must fire and one that must not, e.g. ~50-100 salaries with 2 known outliers, a column
// that is ~30% NULL, near-duplicate employees, malformed emails, and an orphaned FK row
// (insert under a NOT VALID constraint). Assert the exact expected counts.
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

            // ====================================================
            // GROUPED AGGREGATE ANALYSIS
            // ====================================================

            assertEquals(
                1,
                report.groupedAggregates.size
            )

            val aggregate =
                report.groupedAggregates.single()

            assertEquals(
                "region",
                aggregate.groupByColumn
            )

            assertEquals(
                "total_amount",
                aggregate.numericColumn
            )

            assertEquals(
                3,
                aggregate.groups.size
            )

            val central =
                aggregate.groups.single {
                    it.groupValue == "Central"
                }

            assertEquals(
                1L,
                central.rowCount
            )

            assertEquals(
                "1500000.00",
                central.total
            )

            val eastern =
                aggregate.groups.single {
                    it.groupValue == "Eastern"
                }

            assertEquals(
                1L,
                eastern.rowCount
            )

            assertEquals(
                "2100000.00",
                eastern.total
            )

            val western =
                aggregate.groups.single {
                    it.groupValue == "Western"
                }

            assertEquals(
                1L,
                western.rowCount
            )

            assertEquals(
                "1800000.00",
                western.total
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


    @Test
    fun `automatically discovers grouped aggregate analysis for a generic table`() {

        DatabaseConnector.connect().use { connection ->

            connection.autoCommit =
                false

            try {

                connection.prepareStatement(
                    """
                    CREATE TABLE public.aggregate_analysis_test (
                        test_id INTEGER PRIMARY KEY,
                        category VARCHAR(100) NOT NULL,
                        amount NUMERIC(12, 2) NOT NULL
                    )
                    """.trimIndent()
                ).use { statement ->

                    statement.executeUpdate()
                }

                connection.prepareStatement(
                    """
                    INSERT INTO public.aggregate_analysis_test (
                        test_id,
                        category,
                        amount
                    )
                    VALUES
                        (1, 'Food', 100.00),
                        (2, 'Food', 200.00),
                        (3, 'Books', 50.00)
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
                    builder.buildTableReport(
                        schemaName = "public",
                        tableName = "aggregate_analysis_test"
                    )

                assertEquals(
                    1,
                    report.groupedAggregates.size
                )

                val aggregate =
                    report.groupedAggregates.single()

                assertEquals(
                    "category",
                    aggregate.groupByColumn
                )

                assertEquals(
                    "amount",
                    aggregate.numericColumn
                )

                assertEquals(
                    2,
                    aggregate.groups.size
                )

                val food =
                    aggregate.groups.single {
                        it.groupValue == "Food"
                    }

                assertEquals(
                    2L,
                    food.rowCount
                )

                assertEquals(
                    "100.00",
                    food.minimum
                )

                assertEquals(
                    "200.00",
                    food.maximum
                )

                assertEquals(
                    "300.00",
                    food.total
                )

                assertTrue(
                    food.average
                        ?.toBigDecimal()
                        ?.compareTo(
                            "150.00".toBigDecimal()
                        ) == 0
                )

                val books =
                    aggregate.groups.single {
                        it.groupValue == "Books"
                    }

                assertEquals(
                    1L,
                    books.rowCount
                )

                assertEquals(
                    "50.00",
                    books.minimum
                )

                assertEquals(
                    "50.00",
                    books.maximum
                )

                assertEquals(
                    "50.00",
                    books.total
                )

                assertTrue(
                    books.average
                        ?.toBigDecimal()
                        ?.compareTo(
                            "50.00".toBigDecimal()
                        ) == 0
                )

            } finally {

                connection.rollback()
            }
        }
    }


}
