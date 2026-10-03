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
    fun `copies category issue counts into trend snapshots`() {

        val reports =
            listOf(
                report(
                    databaseName = "companydb",
                    totalIssues = 13L,
                    overallStatus =
                        DatabaseQualityStatus.ATTENTION_REQUIRED,

                    primaryKeyIssues = 1L,
                    foreignKeyIssues = 2L,
                    duplicateValueIssues = 3L,
                    potentialDuplicateRecordIssues = 4L,
                    emptyStringIssues = 5L,
                    whitespaceOnlyIssues = 6L,
                    leadingTrailingWhitespaceIssues = 7L,
                    emailFormatIssues = 8L,
                    numericAnomalyIssues = 9L,
                    potentialNumericOutlierIssues = 10L,
                    dateAnomalyIssues = 11L,
                    constantValueIssues = 12L,
                    highNullPercentageIssues = 13L
                )
            )

        val trend =
            DatabaseQualityTrendAnalyzer.analyze(
                reports
            )

        val snapshot =
            trend.snapshots.single()

        assertEquals(
            1L,
            snapshot.primaryKeyIssues
        )

        assertEquals(
            2L,
            snapshot.foreignKeyIssues
        )

        assertEquals(
            3L,
            snapshot.duplicateValueIssues
        )

        assertEquals(
            4L,
            snapshot.potentialDuplicateRecordIssues
        )

        assertEquals(
            5L,
            snapshot.emptyStringIssues
        )

        assertEquals(
            6L,
            snapshot.whitespaceOnlyIssues
        )

        assertEquals(
            7L,
            snapshot.leadingTrailingWhitespaceIssues
        )

        assertEquals(
            8L,
            snapshot.emailFormatIssues
        )

        assertEquals(
            9L,
            snapshot.numericAnomalyIssues
        )

        assertEquals(
            10L,
            snapshot.potentialNumericOutlierIssues
        )

        assertEquals(
            11L,
            snapshot.dateAnomalyIssues
        )

        assertEquals(
            12L,
            snapshot.constantValueIssues
        )

        assertEquals(
            13L,
            snapshot.highNullPercentageIssues
        )
    }


    @Test
    fun `preserves category values independently across snapshots`() {

        val reports =
            listOf(
                report(
                    databaseName = "companydb",
                    totalIssues = 4L,
                    overallStatus =
                        DatabaseQualityStatus.REVIEW,
                    foreignKeyIssues = 3L,
                    emailFormatIssues = 1L
                ),
                report(
                    databaseName = "companydb",
                    totalIssues = 2L,
                    overallStatus =
                        DatabaseQualityStatus.REVIEW,
                    foreignKeyIssues = 0L,
                    emailFormatIssues = 2L
                )
            )

        val trend =
            DatabaseQualityTrendAnalyzer.analyze(
                reports
            )

        assertEquals(
            3L,
            trend.snapshots[0].foreignKeyIssues
        )

        assertEquals(
            1L,
            trend.snapshots[0].emailFormatIssues
        )

        assertEquals(
            0L,
            trend.snapshots[1].foreignKeyIssues
        )

        assertEquals(
            2L,
            trend.snapshots[1].emailFormatIssues
        )
    }


    @Test
    fun `calculates first to latest category changes`() {

        val reports =
            listOf(
                report(
                    databaseName = "companydb",
                    totalIssues = 6L,
                    overallStatus =
                        DatabaseQualityStatus.REVIEW,

                    primaryKeyIssues = 2L,
                    foreignKeyIssues = 3L,
                    emailFormatIssues = 1L
                ),

                report(
                    databaseName = "companydb",
                    totalIssues = 5L,
                    overallStatus =
                        DatabaseQualityStatus.REVIEW,

                    primaryKeyIssues = 1L,
                    foreignKeyIssues = 2L,
                    emailFormatIssues = 2L
                ),

                report(
                    databaseName = "companydb",
                    totalIssues = 3L,
                    overallStatus =
                        DatabaseQualityStatus.REVIEW,

                    primaryKeyIssues = 0L,
                    foreignKeyIssues = 1L,
                    emailFormatIssues = 2L
                )
            )

        val trend =
            DatabaseQualityTrendAnalyzer.analyze(
                reports
            )

        assertEquals(
            13,
            trend.categoryChanges.size
        )


        val primaryKeyChange =
            trend.categoryChanges.single {
                it.category == "Primary Key Issues"
            }

        assertEquals(
            2L,
            primaryKeyChange.firstValue
        )

        assertEquals(
            0L,
            primaryKeyChange.latestValue
        )

        assertEquals(
            -2L,
            primaryKeyChange.change
        )


        val foreignKeyChange =
            trend.categoryChanges.single {
                it.category == "Foreign Key Issues"
            }

        assertEquals(
            3L,
            foreignKeyChange.firstValue
        )

        assertEquals(
            1L,
            foreignKeyChange.latestValue
        )

        assertEquals(
            -2L,
            foreignKeyChange.change
        )


        val emailChange =
            trend.categoryChanges.single {
                it.category == "Email Format Issues"
            }

        assertEquals(
            1L,
            emailChange.firstValue
        )

        assertEquals(
            2L,
            emailChange.latestValue
        )

        assertEquals(
            1L,
            emailChange.change
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
        overallStatus: DatabaseQualityStatus,

        primaryKeyIssues: Long = 0L,
        foreignKeyIssues: Long = 0L,
        duplicateValueIssues: Long = 0L,
        potentialDuplicateRecordIssues: Long = 0L,
        emptyStringIssues: Long = 0L,
        whitespaceOnlyIssues: Long = 0L,
        leadingTrailingWhitespaceIssues: Long = 0L,
        emailFormatIssues: Long = 0L,
        numericAnomalyIssues: Long = 0L,
        potentialNumericOutlierIssues: Long = 0L,
        dateAnomalyIssues: Long = 0L,
        constantValueIssues: Long = 0L,
        highNullPercentageIssues: Long = 0L
    ): DatabaseQualityReport {

        return DatabaseQualityReport(
            databaseName = databaseName,
            tables = emptyList(),
            summary =
                DatabaseQualitySummary(
                    tablesAnalysed = 0,
                    columnsAnalysed = 0,
                    rowsAnalysed = 0L,

                    primaryKeyIssues =
                        primaryKeyIssues,

                    foreignKeyIssues =
                        foreignKeyIssues,

                    duplicateValueIssues =
                        duplicateValueIssues,

                    potentialDuplicateRecordIssues =
                        potentialDuplicateRecordIssues,

                    emptyStringIssues =
                        emptyStringIssues,

                    whitespaceOnlyIssues =
                        whitespaceOnlyIssues,

                    leadingTrailingWhitespaceIssues =
                        leadingTrailingWhitespaceIssues,

                    emailFormatIssues =
                        emailFormatIssues,

                    numericAnomalyIssues =
                        numericAnomalyIssues,

                    potentialNumericOutlierIssues =
                        potentialNumericOutlierIssues,

                    dateAnomalyIssues =
                        dateAnomalyIssues,

                    constantValueIssues =
                        constantValueIssues,

                    highNullPercentageIssues =
                        highNullPercentageIssues,

                    totalIssues =
                        totalIssues,

                    overallStatus =
                        overallStatus
                )
        )
    }
}
