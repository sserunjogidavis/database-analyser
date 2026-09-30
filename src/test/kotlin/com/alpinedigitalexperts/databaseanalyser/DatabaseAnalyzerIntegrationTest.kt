package com.alpinedigitalexperts.databaseanalyser

import java.sql.Connection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatabaseAnalyzerIntegrationTest {

    // ============================================================
    // TEST HELPER
    // ============================================================

    private fun withRollback(
        testBlock: (
            Connection,
            DatabaseAnalyzer
        ) -> Unit
    ) {

        DatabaseConnector
            .connect()
            .use { connection ->

                connection.autoCommit =
                    false

                try {

                    val analyzer =
                        DatabaseAnalyzer(
                            connection
                        )

                    testBlock(
                        connection,
                        analyzer
                    )

                } finally {

                    // Every database change made by a test
                    // is undone automatically.
                    connection.rollback()
                }
            }
    }


    // ============================================================
    // 1. BASIC DATABASE STRUCTURE TEST
    // ============================================================

    @Test
    fun databaseStructureCanBeRead() {

        DatabaseConnector
            .connect()
            .use { connection ->

                val analyzer =
                    DatabaseAnalyzer(
                        connection
                    )

                val tables =
                    analyzer.getTables()

                assertTrue(
                    "employees" in tables
                )

                assertTrue(
                    "departments" in tables
                )

                val employeePrimaryKeys =
                    analyzer.getPrimaryKeys(
                        "employees"
                    )

                assertTrue(
                    "employee_id" in
                        employeePrimaryKeys
                )

                val employeeUniqueColumns =
                    analyzer.getUniqueColumns(
                        "employees"
                    )

                assertTrue(
                    "email" in
                        employeeUniqueColumns
                )
            }
    }


    // ============================================================
    // 2. EMPTY STRING DETECTION TEST
    // ============================================================

    @Test
    fun detectsEmptyString() {

        withRollback {
                connection,
                analyzer ->

            connection
                .prepareStatement(
                    """
                    UPDATE employees
                    SET first_name = ''
                    WHERE employee_id = 2
                    """.trimIndent()
                )
                .use { statement ->

                    statement.executeUpdate()
                }

            val emptyCount =
                analyzer.getEmptyStringCount(
                    "employees",
                    "first_name"
                )

            assertEquals(
                1L,
                emptyCount
            )
        }
    }


    // ============================================================
    // 3. INVALID EMAIL DETECTION TEST
    // ============================================================

    @Test
    fun detectsInvalidEmail() {

        withRollback {
                connection,
                analyzer ->

            connection
                .prepareStatement(
                    """
                    UPDATE employees
                    SET email = 'invalid-email-format'
                    WHERE employee_id = 2
                    """.trimIndent()
                )
                .use { statement ->

                    statement.executeUpdate()
                }

            val invalidEmailCount =
                analyzer.getInvalidEmailCount(
                    "employees",
                    "email"
                )

            assertEquals(
                1L,
                invalidEmailCount
            )
        }
    }


    // ============================================================
    // 4. NEGATIVE NUMERIC VALUE TEST
    // ============================================================

    @Test
    fun detectsNegativeSalary() {

        withRollback {
                connection,
                analyzer ->

            connection
                .prepareStatement(
                    """
                    UPDATE employees
                    SET salary = -3200000
                    WHERE employee_id = 2
                    """.trimIndent()
                )
                .use { statement ->

                    statement.executeUpdate()
                }

            val negativeCount =
                analyzer.getNegativeValueCount(
                    "employees",
                    "salary"
                )

            assertEquals(
                1L,
                negativeCount
            )
        }
    }


    // ============================================================
    // 5. FUTURE DATE TEST
    // ============================================================

    @Test
    fun detectsFutureHireDate() {

        withRollback {
                connection,
                analyzer ->

            connection
                .prepareStatement(
                    """
                    UPDATE employees
                    SET hire_date = CURRENT_DATE + INTERVAL '1 year'
                    WHERE employee_id = 2
                    """.trimIndent()
                )
                .use { statement ->

                    statement.executeUpdate()
                }

            val futureDateCount =
                analyzer.getFutureDateCount(
                    "employees",
                    "hire_date"
                )

            assertEquals(
                1L,
                futureDateCount
            )
        }
    }


    // ============================================================
    // 6. POTENTIAL DUPLICATE RECORD TEST
    // ============================================================

    @Test
    fun detectsPotentialDuplicateRecord() {

        withRollback {
                connection,
                analyzer ->

            connection
                .prepareStatement(
                    """
                    INSERT INTO employees (
                        first_name,
                        last_name,
                        email,
                        salary,
                        hire_date,
                        department_id
                    )
                    SELECT
                        first_name,
                        last_name,
                        'integration.test@example.com',
                        salary,
                        hire_date,
                        department_id
                    FROM employees
                    WHERE employee_id = 1
                    """.trimIndent()
                )
                .use { statement ->

                    statement.executeUpdate()
                }

            val comparisonColumns =
                listOf(
                    "first_name",
                    "last_name",
                    "salary",
                    "hire_date",
                    "department_id"
                )

            val potentialDuplicateCount =
                analyzer
                    .getPotentialDuplicateRecordCount(
                        "employees",
                        comparisonColumns
                    )

            assertTrue(
                potentialDuplicateCount >= 1L
            )
        }
    }


    // ============================================================
    // 7. NUMERIC OUTLIER TEST
    // ============================================================

    @Test
    fun detectsPotentialSalaryOutlier() {

        withRollback {
                connection,
                analyzer ->

            connection
                .prepareStatement(
                    """
                    UPDATE employees
                    SET salary = 50000000
                    WHERE employee_id = 2
                    """.trimIndent()
                )
                .use { statement ->

                    statement.executeUpdate()
                }

            val outlierCount =
                analyzer
                    .getPotentialNumericOutlierCount(
                        "employees",
                        "salary"
                    )

            assertEquals(
                1L,
                outlierCount
            )
        }
    }


    // ============================================================
    // 8. CLEAN BASELINE TEST
    // ============================================================

    @Test
    fun cleanDatabaseHasNoKnownQualityProblems() {

        DatabaseConnector
            .connect()
            .use { connection ->

                val analyzer =
                    DatabaseAnalyzer(
                        connection
                    )

                assertEquals(
                    0L,
                    analyzer.getEmptyStringCount(
                        "employees",
                        "first_name"
                    )
                )

                assertEquals(
                    0L,
                    analyzer.getInvalidEmailCount(
                        "employees",
                        "email"
                    )
                )

                assertEquals(
                    0L,
                    analyzer.getNegativeValueCount(
                        "employees",
                        "salary"
                    )
                )

                assertEquals(
                    0L,
                    analyzer.getFutureDateCount(
                        "employees",
                        "hire_date"
                    )
                )

                assertEquals(
                    0L,
                    analyzer.getPotentialNumericOutlierCount(
                        "employees",
                        "salary"
                    )
                )

                val comparisonColumns =
                    listOf(
                        "first_name",
                        "last_name",
                        "salary",
                        "hire_date",
                        "department_id"
                    )

                assertEquals(
                    0L,
                    analyzer
                        .getPotentialDuplicateRecordCount(
                            "employees",
                            comparisonColumns
                        )
                )
            }
    }

    // ============================================================
    // 9. GROUPED NUMERIC AGGREGATE TEST
    // ============================================================

    @Test
    fun groupsMonthlySalesByRegion() {

        DatabaseConnector
            .connect()
            .use { connection ->

                val analyzer =
                    DatabaseAnalyzer(
                        connection
                    )

                val aggregates =
                    analyzer
                        .getGroupedNumericAggregates(
                            schemaName = "reporting",
                            tableName = "monthly_sales",
                            groupByColumn = "region",
                            numericColumn = "total_amount"
                        )

                assertEquals(
                    3,
                    aggregates.size
                )

                val central =
                    aggregates.single {
                        it.groupValue == "Central"
                    }

                assertEquals(
                    1L,
                    central.rowCount
                )

                assertEquals(
                    "1500000.00",
                    central.minimum
                        ?.toPlainString()
                )

                assertEquals(
                    "1500000.00",
                    central.maximum
                        ?.toPlainString()
                )

                assertEquals(
                    "1500000.000000000000",
                    central.average
                        ?.toPlainString()
                )

                assertEquals(
                    "1500000.00",
                    central.total
                        ?.toPlainString()
                )


                val eastern =
                    aggregates.single {
                        it.groupValue == "Eastern"
                    }

                assertEquals(
                    1L,
                    eastern.rowCount
                )

                assertEquals(
                    "2100000.00",
                    eastern.total
                        ?.toPlainString()
                )


                val western =
                    aggregates.single {
                        it.groupValue == "Western"
                    }

                assertEquals(
                    1L,
                    western.rowCount
                )

                assertEquals(
                    "1800000.00",
                    western.total
                        ?.toPlainString()
                )
            }
    }


}
