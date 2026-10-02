package com.alpinedigitalexperts.databaseanalyser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DatabaseQualityComparatorTest {

    @Test
    fun `fewer total issues is improved`() {

        val previous =
            report(
                databaseName = "companydb",
                totalIssues = 10L,
                overallStatus =
                    DatabaseQualityStatus.ATTENTION_REQUIRED
            )

        val current =
            report(
                databaseName = "companydb",
                totalIssues = 4L,
                overallStatus =
                    DatabaseQualityStatus.REVIEW
            )

        val comparison =
            DatabaseQualityComparator.compare(
                previous = previous,
                current = current
            )

        assertEquals(
            10L,
            comparison.previousTotalIssues
        )

        assertEquals(
            4L,
            comparison.currentTotalIssues
        )

        assertEquals(
            -6L,
            comparison.totalIssueChange
        )

        assertEquals(
            DatabaseQualityComparisonOutcome.IMPROVED,
            comparison.outcome
        )

        assertEquals(
            DatabaseQualityStatus.ATTENTION_REQUIRED,
            comparison.previousStatus
        )

        assertEquals(
            DatabaseQualityStatus.REVIEW,
            comparison.currentStatus
        )
    }


    @Test
    fun `more total issues is regressed`() {

        val previous =
            report(
                databaseName = "companydb",
                totalIssues = 2L,
                overallStatus =
                    DatabaseQualityStatus.GOOD
            )

        val current =
            report(
                databaseName = "companydb",
                totalIssues = 7L,
                overallStatus =
                    DatabaseQualityStatus.REVIEW
            )

        val comparison =
            DatabaseQualityComparator.compare(
                previous = previous,
                current = current
            )

        assertEquals(
            5L,
            comparison.totalIssueChange
        )

        assertEquals(
            DatabaseQualityComparisonOutcome.REGRESSED,
            comparison.outcome
        )
    }


    @Test
    fun `same total issue count is unchanged`() {

        val previous =
            report(
                databaseName = "companydb",
                totalIssues = 3L,
                overallStatus =
                    DatabaseQualityStatus.REVIEW
            )

        val current =
            report(
                databaseName = "companydb",
                totalIssues = 3L,
                overallStatus =
                    DatabaseQualityStatus.REVIEW
            )

        val comparison =
            DatabaseQualityComparator.compare(
                previous = previous,
                current = current
            )

        assertEquals(
            0L,
            comparison.totalIssueChange
        )

        assertEquals(
            DatabaseQualityComparisonOutcome.UNCHANGED,
            comparison.outcome
        )
    }


    @Test
    fun `calculates category issue changes`() {

        val previous =
            report(
                databaseName = "companydb",
                totalIssues = 8L,
                overallStatus =
                    DatabaseQualityStatus.REVIEW,
                primaryKeyIssues = 2L,
                foreignKeyIssues = 3L,
                emailFormatIssues = 3L
            )

        val current =
            report(
                databaseName = "companydb",
                totalIssues = 5L,
                overallStatus =
                    DatabaseQualityStatus.REVIEW,
                primaryKeyIssues = 1L,
                foreignKeyIssues = 4L,
                emailFormatIssues = 0L
            )

        val comparison =
            DatabaseQualityComparator.compare(
                previous = previous,
                current = current
            )

        val primaryKeyChange =
            comparison.categoryChanges.single {
                it.category == "Primary Key Issues"
            }

        assertEquals(
            2L,
            primaryKeyChange.previousValue
        )

        assertEquals(
            1L,
            primaryKeyChange.currentValue
        )

        assertEquals(
            -1L,
            primaryKeyChange.change
        )


        val foreignKeyChange =
            comparison.categoryChanges.single {
                it.category == "Foreign Key Issues"
            }

        assertEquals(
            3L,
            foreignKeyChange.previousValue
        )

        assertEquals(
            4L,
            foreignKeyChange.currentValue
        )

        assertEquals(
            1L,
            foreignKeyChange.change
        )


        val emailChange =
            comparison.categoryChanges.single {
                it.category == "Email Format Issues"
            }

        assertEquals(
            3L,
            emailChange.previousValue
        )

        assertEquals(
            0L,
            emailChange.currentValue
        )

        assertEquals(
            -3L,
            emailChange.change
        )
    }


    @Test
    fun `comparison contains all supported categories`() {

        val comparison =
            DatabaseQualityComparator.compare(
                previous =
                    report(
                        databaseName = "companydb",
                        totalIssues = 0L,
                        overallStatus =
                            DatabaseQualityStatus.GOOD
                    ),
                current =
                    report(
                        databaseName = "companydb",
                        totalIssues = 0L,
                        overallStatus =
                            DatabaseQualityStatus.GOOD
                    )
            )

        assertEquals(
            13,
            comparison.categoryChanges.size
        )
    }


    @Test
    fun `rejects reports from different databases`() {

        val previous =
            report(
                databaseName = "companydb",
                totalIssues = 1L,
                overallStatus =
                    DatabaseQualityStatus.REVIEW
            )

        val current =
            report(
                databaseName = "anotherdb",
                totalIssues = 1L,
                overallStatus =
                    DatabaseQualityStatus.REVIEW
            )

        assertFailsWith<IllegalArgumentException> {

            DatabaseQualityComparator.compare(
                previous = previous,
                current = current
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
