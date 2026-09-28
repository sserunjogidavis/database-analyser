package com.alpinedigitalexperts.databaseanalyser

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NonPublicSchemaIntegrationTest {

    @Test
    fun `discovers and analyses reporting monthly sales table`() {

        DatabaseConnector.connect().use { connection ->

            val adapter =
                PostgreSqlDatabaseAdapter(
                    connection
                )

            val analyzer =
                DatabaseAnalyzer(
                    connection = connection,
                    metadataAdapter = adapter
                )


            // ====================================================
            // 1. REPORTING SCHEMA EXISTS
            // ====================================================

            val schemas =
                adapter.discoverSchemas()

            assertTrue(
                schemas.any {
                    it.schemaName == "reporting"
                }
            )


            // ====================================================
            // 2. MONTHLY SALES TABLE EXISTS
            // ====================================================

            val tables =
                adapter.discoverTables()

            assertTrue(
                tables.any {
                    it.schemaName == "reporting" &&
                        it.tableName == "monthly_sales"
                }
            )


            // ====================================================
            // 3. TABLE QUALIFIED NAME
            // ====================================================

            val monthlySalesTable =
                tables.single {
                    it.schemaName == "reporting" &&
                        it.tableName == "monthly_sales"
                }

            assertEquals(
                "companydb.reporting.monthly_sales",
                monthlySalesTable.qualifiedName
            )


            // ====================================================
            // 4. COLUMNS
            // ====================================================

            val columns =
                adapter.discoverColumns(
                    schemaName = "reporting",
                    tableName = "monthly_sales"
                )

            assertEquals(
                listOf(
                    "sale_id",
                    "region",
                    "total_amount",
                    "report_date"
                ),
                columns.map {
                    it.columnName
                }
            )


            // ====================================================
            // 5. SALE ID COLUMN
            // ====================================================

            val saleIdColumn =
                columns.single {
                    it.columnName == "sale_id"
                }

            assertEquals(
                "integer",
                saleIdColumn.dataType
            )

            assertEquals(
                false,
                saleIdColumn.nullable
            )


            // ====================================================
            // 6. REGION COLUMN
            // ====================================================

            val regionColumn =
                columns.single {
                    it.columnName == "region"
                }

            assertEquals(
                "character varying",
                regionColumn.dataType
            )

            assertEquals(
                false,
                regionColumn.nullable
            )


            // ====================================================
            // 7. TOTAL AMOUNT COLUMN
            // ====================================================

            val totalAmountColumn =
                columns.single {
                    it.columnName == "total_amount"
                }

            assertEquals(
                "numeric",
                totalAmountColumn.dataType
            )

            assertEquals(
                true,
                totalAmountColumn.nullable
            )


            // ====================================================
            // 8. REPORT DATE COLUMN
            // ====================================================

            val reportDateColumn =
                columns.single {
                    it.columnName == "report_date"
                }

            assertEquals(
                "date",
                reportDateColumn.dataType
            )


            // ====================================================
            // 9. PRIMARY KEY
            // ====================================================

            val primaryKeys =
                adapter.discoverPrimaryKeys(
                    schemaName = "reporting",
                    tableName = "monthly_sales"
                )

            assertEquals(
                1,
                primaryKeys.size
            )

            val primaryKey =
                primaryKeys.single()

            assertEquals(
                DiscoveredKeyKind.PRIMARY_KEY,
                primaryKey.kind
            )

            assertEquals(
                listOf(
                    "sale_id"
                ),
                primaryKey.members
                    .sortedBy {
                        it.memberOrder
                    }
                    .map {
                        it.columnName
                    }
            )


            // ====================================================
            // 10. ROW COUNT
            // ====================================================

            val rowCount =
                analyzer.getRowCount(
                    schemaName = "reporting",
                    tableName = "monthly_sales"
                )

            assertEquals(
                3L,
                rowCount
            )


            // ====================================================
            // 11. DISTINCT REGIONS
            // ====================================================

            val distinctRegions =
                analyzer.getDistinctCount(
                    schemaName = "reporting",
                    tableName = "monthly_sales",
                    columnName = "region"
                )

            assertEquals(
                3L,
                distinctRegions
            )


            // ====================================================
            // 12. NULL COUNT
            // ====================================================

            val nullAmountCount =
                analyzer.getNullCount(
                    schemaName = "reporting",
                    tableName = "monthly_sales",
                    columnName = "total_amount"
                )

            assertEquals(
                0L,
                nullAmountCount
            )


            // ====================================================
            // 13. NULL QUALITY
            // ====================================================

            val nullQuality =
                analyzer.assessNullQuality(
                    schemaName = "reporting",
                    tableName = "monthly_sales",
                    columnName = "total_amount"
                )

            assertEquals(
                "[OK] No NULL values",
                nullQuality
            )


            // ====================================================
            // 14. NUMERIC STATISTICS
            // ====================================================

            val numericStatistics =
                analyzer.getNumericStatistics(
                    schemaName = "reporting",
                    tableName = "monthly_sales",
                    columnName = "total_amount"
                )

            assertNotNull(
                numericStatistics.minimum
            )

            assertNotNull(
                numericStatistics.maximum
            )

            assertNotNull(
                numericStatistics.average
            )

            assertNotNull(
                numericStatistics.total
            )


            // ====================================================
            // 15. MINIMUM AMOUNT
            // ====================================================

            assertEquals(
                0,
                numericStatistics.minimum
                    ?.compareTo(
                        BigDecimal(
                            "1500000.00"
                        )
                    )
            )


            // ====================================================
            // 16. MAXIMUM AMOUNT
            // ====================================================

            assertEquals(
                0,
                numericStatistics.maximum
                    ?.compareTo(
                        BigDecimal(
                            "2100000.00"
                        )
                    )
            )


            // ====================================================
            // 17. AVERAGE AMOUNT
            // ====================================================

            assertEquals(
                0,
                numericStatistics.average
                    ?.compareTo(
                        BigDecimal(
                            "1800000.00"
                        )
                    )
            )


            // ====================================================
            // 18. TOTAL AMOUNT
            // ====================================================

            assertEquals(
                0,
                numericStatistics.total
                    ?.compareTo(
                        BigDecimal(
                            "5400000.00"
                        )
                    )
            )


            // ====================================================
            // 19. NEGATIVE VALUE COUNT
            // ====================================================

            val negativeValues =
                analyzer.getNegativeValueCount(
                    schemaName = "reporting",
                    tableName = "monthly_sales",
                    columnName = "total_amount"
                )

            assertEquals(
                0L,
                negativeValues
            )


            // ====================================================
            // 20. DATE STATISTICS
            // ====================================================

            val dateStatistics =
                analyzer.getDateStatistics(
                    schemaName = "reporting",
                    tableName = "monthly_sales",
                    columnName = "report_date"
                )

            assertNotNull(
                dateStatistics.earliest
            )

            assertNotNull(
                dateStatistics.latest
            )

            assertEquals(
                "2026-08-01",
                dateStatistics.earliest
                    ?.toString()
            )

            assertEquals(
                "2026-08-01",
                dateStatistics.latest
                    ?.toString()
            )


            // ====================================================
            // 21. FUTURE DATE COUNT
            // ====================================================

            val futureDates =
                analyzer.getFutureDateCount(
                    schemaName = "reporting",
                    tableName = "monthly_sales",
                    columnName = "report_date"
                )

            assertEquals(
                0L,
                futureDates
            )


            // ====================================================
            // 22. DUPLICATE REGION VALUES
            // ====================================================

            val duplicateRegions =
                analyzer.getDuplicateValueCount(
                    schemaName = "reporting",
                    tableName = "monthly_sales",
                    columnName = "region"
                )

            assertEquals(
                0L,
                duplicateRegions
            )


            // ====================================================
            // 23. TABLE HAS PRIMARY KEY
            // ====================================================

            assertTrue(
                analyzer.hasPrimaryKey(
                    schemaName = "reporting",
                    tableName = "monthly_sales"
                )
            )


            // ====================================================
            // 24. SALE ID IS IDENTIFIER
            // ====================================================

            assertTrue(
                analyzer.isIdentifierColumn(
                    schemaName = "reporting",
                    tableName = "monthly_sales",
                    columnName = "sale_id"
                )
            )


            // ====================================================
            // 25. GET TABLES FROM REPORTING SCHEMA
            // ====================================================

            val reportingTables =
                analyzer.getTables(
                    schemaName = "reporting"
                )

            assertTrue(
                "monthly_sales" in reportingTables
            )
        }
    }
}
