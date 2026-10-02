package com.alpinedigitalexperts.databaseanalyser

import kotlin.test.Test
import kotlin.test.assertFailsWith

class QualityRulesTest {

    @Test
    fun `rejects negative null warning threshold`() {

        assertFailsWith<IllegalArgumentException> {

            QualityRules(
                highNullWarningThreshold = -1.0
            )
        }
    }


    @Test
    fun `rejects critical null threshold below warning threshold`() {

        assertFailsWith<IllegalArgumentException> {

            QualityRules(
                highNullWarningThreshold = 40.0,
                highNullCriticalThreshold = 30.0
            )
        }
    }


    @Test
    fun `rejects critical null threshold above one hundred`() {

        assertFailsWith<IllegalArgumentException> {

            QualityRules(
                highNullCriticalThreshold = 101.0
            )
        }
    }


    @Test
    fun `rejects non positive numeric outlier threshold`() {

        assertFailsWith<IllegalArgumentException> {

            QualityRules(
                numericOutlierModifiedZScoreThreshold = 0.0
            )
        }
    }


    @Test
    fun `rejects outlier sample size below three`() {

        assertFailsWith<IllegalArgumentException> {

            QualityRules(
                minimumValuesForOutlierAnalysis = 2L
            )
        }
    }
}
