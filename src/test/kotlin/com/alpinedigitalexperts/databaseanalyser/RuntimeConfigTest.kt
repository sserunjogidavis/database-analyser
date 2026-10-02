package com.alpinedigitalexperts.databaseanalyser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RuntimeConfigTest {

    @Test
    fun `uses database configuration when command line is empty`() {

        val runtimeConfig =
            RuntimeConfig.resolve(
                CommandLineConfig()
            )

        assertEquals(
            DatabaseConfig.HOST,
            runtimeConfig.host
        )

        assertEquals(
            DatabaseConfig.PORT,
            runtimeConfig.port
        )

        assertEquals(
            DatabaseConfig.DATABASE_NAME,
            runtimeConfig.databaseName
        )

        assertEquals(
            DatabaseConfig.USER,
            runtimeConfig.user
        )

        assertEquals(
            DatabaseConfig.PASSWORD,
            runtimeConfig.password
        )

        assertEquals(
            DatabaseConfig.QUALITY_RULES,
            runtimeConfig.qualityRules
        )
    }


    @Test
    fun `command line database settings override database configuration`() {

        val runtimeConfig =
            RuntimeConfig.resolve(
                CommandLineConfig(
                    host = "db.example.com",
                    port = "5544",
                    databaseName = "analytics",
                    user = "reporter"
                )
            )

        assertEquals(
            "db.example.com",
            runtimeConfig.host
        )

        assertEquals(
            "5544",
            runtimeConfig.port
        )

        assertEquals(
            "analytics",
            runtimeConfig.databaseName
        )

        assertEquals(
            "reporter",
            runtimeConfig.user
        )

        assertEquals(
            DatabaseConfig.PASSWORD,
            runtimeConfig.password
        )
    }


    @Test
    fun `command line quality rules override configured quality rules`() {

        val runtimeConfig =
            RuntimeConfig.resolve(
                CommandLineConfig(
                    nullWarningThreshold = 15.0,
                    nullCriticalThreshold = 45.0,
                    outlierZScoreThreshold = 5.0,
                    outlierMinimumSampleSize = 10L
                )
            )

        assertEquals(
            15.0,
            runtimeConfig
                .qualityRules
                .highNullWarningThreshold
        )

        assertEquals(
            45.0,
            runtimeConfig
                .qualityRules
                .highNullCriticalThreshold
        )

        assertEquals(
            5.0,
            runtimeConfig
                .qualityRules
                .numericOutlierModifiedZScoreThreshold
        )

        assertEquals(
            10L,
            runtimeConfig
                .qualityRules
                .minimumValuesForOutlierAnalysis
        )
    }


    @Test
    fun `command line can override one quality rule while keeping the others`() {

        val runtimeConfig =
            RuntimeConfig.resolve(
                CommandLineConfig(
                    nullWarningThreshold = 25.0
                )
            )

        assertEquals(
            25.0,
            runtimeConfig
                .qualityRules
                .highNullWarningThreshold
        )

        assertEquals(
            DatabaseConfig
                .QUALITY_RULES
                .highNullCriticalThreshold,
            runtimeConfig
                .qualityRules
                .highNullCriticalThreshold
        )

        assertEquals(
            DatabaseConfig
                .QUALITY_RULES
                .numericOutlierModifiedZScoreThreshold,
            runtimeConfig
                .qualityRules
                .numericOutlierModifiedZScoreThreshold
        )

        assertEquals(
            DatabaseConfig
                .QUALITY_RULES
                .minimumValuesForOutlierAnalysis,
            runtimeConfig
                .qualityRules
                .minimumValuesForOutlierAnalysis
        )
    }


    @Test
    fun `builds jdbc url from resolved database configuration`() {

        val runtimeConfig =
            RuntimeConfig.resolve(
                CommandLineConfig(
                    host = "database.internal",
                    port = "5433",
                    databaseName = "warehouse"
                )
            )

        assertEquals(
            "jdbc:postgresql://database.internal:5433/warehouse",
            runtimeConfig.url
        )
    }


    @Test
    fun `password is not supplied through command line configuration`() {

        val runtimeConfig =
            RuntimeConfig.resolve(
                CommandLineConfig(
                    host = "another-host",
                    user = "another-user"
                )
            )

        assertEquals(
            DatabaseConfig.PASSWORD,
            runtimeConfig.password
        )
    }


    @Test
    fun `rejects command line quality rules that violate validation`() {

        assertFailsWith<IllegalArgumentException> {

            RuntimeConfig.resolve(
                CommandLineConfig(
                    nullWarningThreshold = 60.0,
                    nullCriticalThreshold = 40.0
                )
            )
        }
    }
}
