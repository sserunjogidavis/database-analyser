package com.alpinedigitalexperts.databaseanalyser

data class QualityRules(
    val highNullWarningThreshold: Double = 20.0,
    val highNullCriticalThreshold: Double = 50.0,
    val numericOutlierModifiedZScoreThreshold: Double = 3.5,
    val minimumValuesForOutlierAnalysis: Long = 3
) {

    init {

        require(
            highNullWarningThreshold >= 0.0
        ) {
            "highNullWarningThreshold must be greater than or equal to 0."
        }

        require(
            highNullCriticalThreshold >=
                highNullWarningThreshold
        ) {
            "highNullCriticalThreshold must be greater than or equal to highNullWarningThreshold."
        }

        require(
            highNullCriticalThreshold <= 100.0
        ) {
            "highNullCriticalThreshold must be less than or equal to 100."
        }

        require(
            numericOutlierModifiedZScoreThreshold > 0.0
        ) {
            "numericOutlierModifiedZScoreThreshold must be greater than 0."
        }

        require(
            minimumValuesForOutlierAnalysis >= 3L
        ) {
            "minimumValuesForOutlierAnalysis must be at least 3."
        }
    }
}
