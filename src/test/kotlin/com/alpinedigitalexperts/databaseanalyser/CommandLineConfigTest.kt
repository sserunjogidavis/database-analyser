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
