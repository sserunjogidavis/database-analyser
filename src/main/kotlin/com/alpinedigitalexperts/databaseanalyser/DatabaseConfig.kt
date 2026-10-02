package com.alpinedigitalexperts.databaseanalyser

object DatabaseConfig {

    val HOST: String =
        System.getenv("DB_HOST")
            ?: "localhost"

    val PORT: String =
        System.getenv("DB_PORT")
            ?: "5432"

    val DATABASE_NAME: String =
        System.getenv("DB_NAME")
            ?: "companydb"

    val USER: String =
        System.getenv("DB_USER")
            ?: "analyst"

    val PASSWORD: String =
        System.getenv("DB_PASSWORD")
            ?: error(
                "DB_PASSWORD environment variable is required."
            )

    val URL: String =
        "jdbc:postgresql://$HOST:$PORT/$DATABASE_NAME"


    val QUALITY_RULES: QualityRules =
        QualityRules(
            highNullWarningThreshold =
                environmentDouble(
                    name = "QUALITY_NULL_WARNING_THRESHOLD",
                    defaultValue = 20.0
                ),
            highNullCriticalThreshold =
                environmentDouble(
                    name = "QUALITY_NULL_CRITICAL_THRESHOLD",
                    defaultValue = 50.0
                ),
            numericOutlierModifiedZScoreThreshold =
                environmentDouble(
                    name = "QUALITY_OUTLIER_ZSCORE_THRESHOLD",
                    defaultValue = 3.5
                ),
            minimumValuesForOutlierAnalysis =
                environmentLong(
                    name = "QUALITY_OUTLIER_MIN_SAMPLE_SIZE",
                    defaultValue = 3L
                )
        )


    private fun environmentDouble(
        name: String,
        defaultValue: Double
    ): Double {

        val value =
            System.getenv(name)
                ?: return defaultValue

        return value.toDoubleOrNull()
            ?: error(
                "$name must be a valid decimal number."
            )
    }


    private fun environmentLong(
        name: String,
        defaultValue: Long
    ): Long {

        val value =
            System.getenv(name)
                ?: return defaultValue

        return value.toLongOrNull()
            ?: error(
                "$name must be a valid whole number."
            )
    }
}
