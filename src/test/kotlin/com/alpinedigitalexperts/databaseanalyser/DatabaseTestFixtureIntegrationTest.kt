package com.alpinedigitalexperts.databaseanalyser

import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseTestFixtureIntegrationTest {

    @Test
    fun `loads reproducible database quality fixture`() {

        DatabaseConnector.connect().use { connection ->

            connection.autoCommit = false

            try {

                DatabaseTestFixture.loadQualityFixture(
                    connection
                )

                val analyzer =
                    DatabaseAnalyzer(
                        connection
                    )

                // ====================================================
                // ROW COUNT
                // ====================================================

                assertEquals(
                    20L,
                    analyzer.getRowCount(
                        schemaName = "quality_test",
                        tableName = "employees"
                    )
                )

                // ====================================================
                // NULL COUNT + NULL PERCENTAGE
                // ====================================================

                assertEquals(
                    6L,
                    analyzer.getNullCount(
                        schemaName = "quality_test",
                        tableName = "employees",
                        columnName = "salary"
                    )
                )

                assertEquals(
                    30.0,
                    analyzer.getNullPercentage(
                        schemaName = "quality_test",
                        tableName = "employees",
                        columnName = "salary"
                    )
                )

                // ====================================================
                // INVALID EMAILS
                // ====================================================

                assertEquals(
                    2L,
                    analyzer.getInvalidEmailCount(
                        schemaName = "quality_test",
                        tableName = "employees",
                        columnName = "email"
                    )
                )

                // ====================================================
                // FUTURE DATE
                // ====================================================

                assertEquals(
                    1L,
                    analyzer.getFutureDateCount(
                        schemaName = "quality_test",
                        tableName = "employees",
                        columnName = "hire_date"
                    )
                )

                // ====================================================
                // POTENTIAL NUMERIC OUTLIER
                // ====================================================

                assertEquals(
                    1L,
                    analyzer.getPotentialNumericOutlierCount(
                        schemaName = "quality_test",
                        tableName = "employees",
                        columnName = "salary"
                    )
                )

                // ====================================================
                // POTENTIAL DUPLICATE RECORD
                // ====================================================

                assertEquals(
                    1L,
                    analyzer.getPotentialDuplicateRecordCount(
                        schemaName = "quality_test",
                        tableName = "employees",
                        comparisonColumns =
                            listOf(
                                "first_name",
                                "last_name",
                                "email",
                                "salary",
                                "hire_date",
                                "department_id"
                            )
                    )
                )

                // ====================================================
                // INVALID FOREIGN KEY REFERENCE
                // ====================================================

                val foreignKey =
                    analyzer
                        .getForeignKeys(
                            schemaName = "quality_test",
                            tableName = "employees"
                        )
                        .single()

                assertEquals(
                    1L,
                    analyzer.getInvalidForeignKeyCount(
                        schemaName = "quality_test",
                        tableName = "employees",
                        foreignKey = foreignKey
                    )
                )

                // ====================================================
                // GROUPED AGGREGATE FIXTURE ROW COUNT
                // ====================================================

                assertEquals(
                    15L,
                    analyzer.getRowCount(
                        schemaName = "quality_test",
                        tableName = "aggregate_analysis"
                    )
                )

            } finally {

                connection.rollback()
            }
        }
    }
}
