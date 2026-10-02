package com.alpinedigitalexperts.databaseanalyser

import kotlin.test.Test
import kotlin.test.assertEquals

class QualityRulesIntegrationTest {

    @Test
    fun `custom null thresholds change null quality assessment`() {

        DatabaseConnector.connect().use { connection ->

            connection.autoCommit = false

            try {

                DatabaseTestFixture.loadQualityFixture(
                    connection
                )

                val relaxedAnalyzer =
                    DatabaseAnalyzer(
                        connection = connection,
                        qualityRules =
                            QualityRules(
                                highNullWarningThreshold = 40.0,
                                highNullCriticalThreshold = 60.0
                            )
                    )

                assertEquals(
                    30.0,
                    relaxedAnalyzer.getNullPercentage(
                        schemaName = "quality_test",
                        tableName = "employees",
                        columnName = "salary"
                    )
                )

                assertEquals(
                    "[INFO] Some NULL values",
                    relaxedAnalyzer.assessNullQuality(
                        schemaName = "quality_test",
                        tableName = "employees",
                        columnName = "salary"
                    )
                )

                val strictAnalyzer =
                    DatabaseAnalyzer(
                        connection = connection,
                        qualityRules =
                            QualityRules(
                                highNullWarningThreshold = 10.0,
                                highNullCriticalThreshold = 25.0
                            )
                    )

                assertEquals(
                    "[CRITICAL] Very high NULL percentage",
                    strictAnalyzer.assessNullQuality(
                        schemaName = "quality_test",
                        tableName = "employees",
                        columnName = "salary"
                    )
                )

            } finally {

                connection.rollback()
            }
        }
    }


    @Test
    fun `custom outlier threshold changes numeric outlier detection`() {

        DatabaseConnector.connect().use { connection ->

            connection.autoCommit = false

            try {

                DatabaseTestFixture.loadQualityFixture(
                    connection
                )

                val defaultAnalyzer =
                    DatabaseAnalyzer(
                        connection
                    )

                assertEquals(
                    1L,
                    defaultAnalyzer.getPotentialNumericOutlierCount(
                        schemaName = "quality_test",
                        tableName = "employees",
                        columnName = "salary"
                    )
                )

                val relaxedAnalyzer =
                    DatabaseAnalyzer(
                        connection = connection,
                        qualityRules =
                            QualityRules(
                                numericOutlierModifiedZScoreThreshold = 1000.0
                            )
                    )

                assertEquals(
                    0L,
                    relaxedAnalyzer.getPotentialNumericOutlierCount(
                        schemaName = "quality_test",
                        tableName = "employees",
                        columnName = "salary"
                    )
                )

            } finally {

                connection.rollback()
            }
        }
    }


    @Test
    fun `custom minimum sample size can disable small sample outlier analysis`() {

        DatabaseConnector.connect().use { connection ->

            connection.autoCommit = false

            try {

                DatabaseTestFixture.loadQualityFixture(
                    connection
                )

                val analyzer =
                    DatabaseAnalyzer(
                        connection = connection,
                        qualityRules =
                            QualityRules(
                                minimumValuesForOutlierAnalysis = 100L
                            )
                    )

                assertEquals(
                    0L,
                    analyzer.getPotentialNumericOutlierCount(
                        schemaName = "quality_test",
                        tableName = "employees",
                        columnName = "salary"
                    )
                )

            } finally {

                connection.rollback()
            }
        }
    }
}
