package com.alpinedigitalexperts.databaseanalyser

data class RuntimeConfig(
    val host: String,
    val port: String,
    val databaseName: String,
    val user: String,
    val password: String,
    val qualityRules: QualityRules
) {

    val url: String
        get() =
            "jdbc:postgresql://$host:$port/$databaseName"


    companion object {

        fun resolve(
            commandLineConfig: CommandLineConfig
        ): RuntimeConfig {

            val baseQualityRules =
                DatabaseConfig.QUALITY_RULES

            val resolvedQualityRules =
                QualityRules(
                    highNullWarningThreshold =
                        commandLineConfig
                            .nullWarningThreshold
                            ?: baseQualityRules
                                .highNullWarningThreshold,

                    highNullCriticalThreshold =
                        commandLineConfig
                            .nullCriticalThreshold
                            ?: baseQualityRules
                                .highNullCriticalThreshold,

                    numericOutlierModifiedZScoreThreshold =
                        commandLineConfig
                            .outlierZScoreThreshold
                            ?: baseQualityRules
                                .numericOutlierModifiedZScoreThreshold,

                    minimumValuesForOutlierAnalysis =
                        commandLineConfig
                            .outlierMinimumSampleSize
                            ?: baseQualityRules
                                .minimumValuesForOutlierAnalysis
                )

            return RuntimeConfig(
                host =
                    commandLineConfig.host
                        ?: DatabaseConfig.HOST,

                port =
                    commandLineConfig.port
                        ?: DatabaseConfig.PORT,

                databaseName =
                    commandLineConfig.databaseName
                        ?: DatabaseConfig.DATABASE_NAME,

                user =
                    commandLineConfig.user
                        ?: DatabaseConfig.USER,

                password =
                    DatabaseConfig.PASSWORD,

                qualityRules =
                    resolvedQualityRules
            )
        }
    }
}
