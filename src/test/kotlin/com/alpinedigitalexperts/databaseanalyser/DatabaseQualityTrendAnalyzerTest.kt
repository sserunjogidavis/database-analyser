package com.alpinedigitalexperts.databaseanalyser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DatabaseQualityTrendAnalyzerTest {

    @Test
    fun `classifies decreasing issue count as improving`() {

        val reports =
            listOf(
                report(
                    databaseName = "companydb",
                    totalIssues = 8L,
                    overallStatus =
                        DatabaseQualityStatus.ATTENTION_REQUIRED
                ),
                report(
                    databaseName = "companydb",
                    totalIssues = 5L,
                    overallStatus =
                        DatabaseQualityStatus.REVIEW
                ),
                report(
                    databaseName = "companydb",
                    totalIssues = 2L,
                    overallStatus =
                        DatabaseQualityStatus.REVIEW
                )
            )

        val trend =
            DatabaseQualityTrendAnalyzer.analyze(
                reports
            )

        assertEquals(
            "companydb",
            trend.databaseName
        )

        assertEquals(
            8L,
            trend.firstTotalIssues
        )

        assertEquals(
            2L,
            trend.latestTotalIssues
        )

        assertEquals(
            -6L,
            trend.totalIssueChange
        )

        assertEquals(
            DatabaseQualityTrendOutcome.IMPROVING,
            trend.outcome
        )
    }


    @Test
    fun `classifies increasing issue count as worsening`() {

        val reports =
            listOf(
                report(
                    databaseName = "companydb",
                    totalIssues = 1L,
                    overallStatus =
                        DatabaseQualityStatus.GOOD
                ),
                report(
                    databaseName = "companydb",
                    totalIssues = 4L,
                    overallStatus =
                        DatabaseQualityStatus.REVIEW
                ),
                report(
                    databaseName = "companydb",
                    totalIssues = 7L,
                    overallStatus =
                        DatabaseQualityStatus.ATTENTION_REQUIRED
                )
            )

        val trend =
            DatabaseQualityTrendAnalyzer.analyze(
                reports
            )

        assertEquals(
            6L,
            trend.totalIssueChange
        )

        assertEquals(
            DatabaseQualityTrendOutcome.WORSENING,
            trend.outcome
        )
    }


    @Test
    fun `classifies equal first and latest issue counts as stable`() {

        val reports =
            listOf(
                report(
                    databaseName = "companydb",
                    totalIssues = 3L,
                    overallStatus =
                        DatabaseQualityStatus.REVIEW
                ),
                report(
                    databaseName = "companydb",
                    totalIssues = 6L,
                    overallStatus =
                        DatabaseQualityStatus.ATTENTION_REQUIRED
                ),
                report(
                    databaseName = "companydb",
                    totalIssues = 3L,
                    overallStatus =
                        DatabaseQualityStatus.REVIEW
                )
            )

        val trend =
            DatabaseQualityTrendAnalyzer.analyze(
                reports
            )

        assertEquals(
            0L,
            trend.totalIssueChange
        )

        assertEquals(
            DatabaseQualityTrendOutcome.STABLE,
            trend.outcome
        )
    }


    @Test
    fun `preserves report ordering in trend snapshots`() {

        val reports =
            listOf(
                report(
                    databaseName = "companydb",
                    totalIssues = 9L,
                    overallStatus =
                        DatabaseQualityStatus.ATTENTION_REQUIRED
                ),
                report(
                    databaseName = "companydb",
                    totalIssues = 5L,
                    overallStatus =
                        DatabaseQualityStatus.REVIEW
                ),
                report(
                    databaseName = "companydb",
                    totalIssues = 1L,
                    overallStatus =
                        DatabaseQualityStatus.GOOD
                )
            )

        val trend =
            DatabaseQualityTrendAnalyzer.analyze(
                reports
            )

        assertEquals(
            3,
            trend.snapshots.size
        )

        assertEquals(
            1,
            trend.snapshots[0].sequence
        )

        assertEquals(
            9L,
            trend.snapshots[0].totalIssues
        )

        assertEquals(
            2,
            trend.snapshots[1].sequence
        )

        assertEquals(
            5L,
            trend.snapshots[1].totalIssues
        )

        assertEquals(
            3,
            trend.snapshots[2].sequence
        )

        assertEquals(
            1L,
            trend.snapshots[2].totalIssues
        )
    }


    @Test
    fun `captures first and latest statuses`() {

        val reports =
            listOf(
                report(
                    databaseName = "companydb",
                    totalIssues = 8L,
                    overallStatus =
                        DatabaseQualityStatus.ATTENTION_REQUIRED
                ),
                report(
                    databaseName = "companydb",
                    totalIssues = 0L,
                    overallStatus =
                        DatabaseQualityStatus.GOOD
                )
            )

        val trend =
            DatabaseQualityTrendAnalyzer.analyze(
                reports
            )

        assertEquals(
            DatabaseQualityStatus.ATTENTION_REQUIRED,
            trend.firstStatus
        )

        assertEquals(
            DatabaseQualityStatus.GOOD,
            trend.latestStatus
        )
    }


    @Test
    fun `single report produces stable trend`() {

        val trend =
            DatabaseQualityTrendAnalyzer.analyze(
                listOf(
                    report(
                        databaseName = "companydb",
                        totalIssues = 2L,
                        overallStatus =
                            DatabaseQualityStatus.REVIEW
                    )
                )
            )

        assertEquals(
            1,
            trend.snapshots.size
        )

        assertEquals(
            0L,
            trend.totalIssueChange
        )

        assertEquals(
            DatabaseQualityTrendOutcome.STABLE,
            trend.outcome
        )
    }


    @Test
    fun `rejects empty report list`() {

        assertFailsWith<IllegalArgumentException> {

            DatabaseQualityTrendAnalyzer.analyze(
                emptyList()
            )
        }
    }


    @Test
    fun `rejects reports from different databases`() {

        val reports =
            listOf(
                report(
                    databaseName = "companydb",
                    totalIssues = 2L,
                    overallStatus =
                        DatabaseQualityStatus.REVIEW
                ),
                report(
                    databaseName = "anotherdb",
                    totalIssues = 1L,
                    overallStatus =
                        DatabaseQualityStatus.GOOD
                )
            )

        assertFailsWith<IllegalArgumentException> {

            DatabaseQualityTrendAnalyzer.analyze(
                reports
            )
        }
    }


    private fun report(
        databaseName: String,
        totalIssues: Long,
        overallStatus: DatabaseQualityStatus
    ): DatabaseQualityReport {

        return DatabaseQualityReport(
            databaseName = databaseName,
            tables = emptyList(),
            summary =
                DatabaseQualitySummary(
                    tablesAnalysed = 0,
                    columnsAnalysed = 0,
                    rowsAnalysed = 0L,

                    primaryKeyIssues = 0L,
                    foreignKeyIssues = 0L,
                    duplicateValueIssues = 0L,
                    potentialDuplicateRecordIssues = 0L,
                    emptyStringIssues = 0L,
                    whitespaceOnlyIssues = 0L,
                    leadingTrailingWhitespaceIssues = 0L,
                    emailFormatIssues = 0L,
                    numericAnomalyIssues = 0L,
                    potentialNumericOutlierIssues = 0L,
                    dateAnomalyIssues = 0L,
                    constantValueIssues = 0L,
                    highNullPercentageIssues = 0L,

                    totalIssues = totalIssues,

                    overallStatus =
                        overallStatus
                )
        )
    }
}
