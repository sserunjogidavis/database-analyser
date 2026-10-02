package com.alpinedigitalexperts.databaseanalyser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class CommandLineConfigTest {

    @Test
    fun `parses empty command line`() {

        val config =
            CommandLineConfig.parse(
                emptyArray()
            )

        assertNull(config.host)
        assertNull(config.port)
        assertNull(config.databaseName)
        assertNull(config.user)
        assertNull(config.nullWarningThreshold)
        assertNull(config.nullCriticalThreshold)
        assertNull(config.outlierZScoreThreshold)
        assertNull(config.outlierMinimumSampleSize)
        assertNull(config.compareWith)
    }


    @Test
    fun `parses database connection options`() {

        val config =
            CommandLineConfig.parse(
                arrayOf(
                    "--host",
                    "database.example.com",
                    "--port",
                    "5433",
                    "--database",
                    "analytics",
                    "--user",
                    "reporter"
                )
            )

        assertEquals(
            "database.example.com",
            config.host
        )

        assertEquals(
            "5433",
            config.port
        )

        assertEquals(
            "analytics",
            config.databaseName
        )

        assertEquals(
            "reporter",
            config.user
        )
    }


    @Test
    fun `parses quality rule options`() {

        val config =
            CommandLineConfig.parse(
                arrayOf(
                    "--null-warning-threshold",
                    "15.0",
                    "--null-critical-threshold",
                    "40.0",
                    "--outlier-zscore-threshold",
                    "4.5",
                    "--outlier-min-sample-size",
                    "10"
                )
            )

        assertEquals(
            15.0,
            config.nullWarningThreshold
        )

        assertEquals(
            40.0,
            config.nullCriticalThreshold
        )

        assertEquals(
            4.5,
            config.outlierZScoreThreshold
        )

        assertEquals(
            10L,
            config.outlierMinimumSampleSize
        )
    }


    @Test
    fun `parses historical comparison report path`() {

        val config =
            CommandLineConfig.parse(
                arrayOf(
                    "--compare-with",
                    "output/previous-database-quality-report.json"
                )
            )

        assertEquals(
            "output/previous-database-quality-report.json",
            config.compareWith
        )
    }


    @Test
    fun `parses comparison option with other command line options`() {

        val config =
            CommandLineConfig.parse(
                arrayOf(
                    "--host",
                    "localhost",
                    "--database",
                    "companydb",
                    "--compare-with",
                    "history/baseline.json"
                )
            )

        assertEquals(
            "localhost",
            config.host
        )

        assertEquals(
            "companydb",
            config.databaseName
        )

        assertEquals(
            "history/baseline.json",
            config.compareWith
        )
    }


    @Test
    fun `rejects unknown command line option`() {

        assertFailsWith<IllegalStateException> {

            CommandLineConfig.parse(
                arrayOf(
                    "--unknown",
                    "value"
                )
            )
        }
    }


    @Test
    fun `rejects option without value`() {

        assertFailsWith<IllegalStateException> {

            CommandLineConfig.parse(
                arrayOf(
                    "--host"
                )
            )
        }
    }


    @Test
    fun `rejects compare with option without value`() {

        assertFailsWith<IllegalStateException> {

            CommandLineConfig.parse(
                arrayOf(
                    "--compare-with"
                )
            )
        }
    }


    @Test
    fun `rejects invalid decimal quality rule`() {

        assertFailsWith<IllegalStateException> {

            CommandLineConfig.parse(
                arrayOf(
                    "--null-warning-threshold",
                    "invalid"
                )
            )
        }
    }


    @Test
    fun `rejects invalid whole number quality rule`() {

        assertFailsWith<IllegalStateException> {

            CommandLineConfig.parse(
                arrayOf(
                    "--outlier-min-sample-size",
                    "3.5"
                )
            )
        }
    }
}
