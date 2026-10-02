package com.alpinedigitalexperts.databaseanalyser

import kotlinx.serialization.Serializable


@Serializable
data class DatabaseQualityComparison(
    val databaseName: String,

    val previousTotalIssues: Long,
    val currentTotalIssues: Long,
    val totalIssueChange: Long,

    val previousStatus: DatabaseQualityStatus,
    val currentStatus: DatabaseQualityStatus,

    val outcome: DatabaseQualityComparisonOutcome,

    val categoryChanges: List<DatabaseQualityCategoryChange>
)


@Serializable
data class DatabaseQualityCategoryChange(
    val category: String,
    val previousValue: Long,
    val currentValue: Long,
    val change: Long
)


@Serializable
enum class DatabaseQualityComparisonOutcome {
    IMPROVED,
    REGRESSED,
    UNCHANGED
}


object DatabaseQualityComparator {

    fun compare(
        previous: DatabaseQualityReport,
        current: DatabaseQualityReport
    ): DatabaseQualityComparison {

        require(
            previous.databaseName ==
                current.databaseName
        ) {
            "Cannot compare reports from different databases."
        }

        val previousSummary =
            previous.summary

        val currentSummary =
            current.summary

        val categoryChanges =
            listOf(
                categoryChange(
                    category = "Primary Key Issues",
                    previousValue =
                        previousSummary.primaryKeyIssues,
                    currentValue =
                        currentSummary.primaryKeyIssues
                ),
                categoryChange(
                    category = "Foreign Key Issues",
                    previousValue =
                        previousSummary.foreignKeyIssues,
                    currentValue =
                        currentSummary.foreignKeyIssues
                ),
                categoryChange(
                    category = "Duplicate Value Issues",
                    previousValue =
                        previousSummary.duplicateValueIssues,
                    currentValue =
                        currentSummary.duplicateValueIssues
                ),
                categoryChange(
                    category = "Potential Duplicate Record Issues",
                    previousValue =
                        previousSummary.potentialDuplicateRecordIssues,
                    currentValue =
                        currentSummary.potentialDuplicateRecordIssues
                ),
                categoryChange(
                    category = "Empty String Issues",
                    previousValue =
                        previousSummary.emptyStringIssues,
                    currentValue =
                        currentSummary.emptyStringIssues
                ),
                categoryChange(
                    category = "Whitespace-Only Issues",
                    previousValue =
                        previousSummary.whitespaceOnlyIssues,
                    currentValue =
                        currentSummary.whitespaceOnlyIssues
                ),
                categoryChange(
                    category = "Leading/Trailing Whitespace Issues",
                    previousValue =
                        previousSummary.leadingTrailingWhitespaceIssues,
                    currentValue =
                        currentSummary.leadingTrailingWhitespaceIssues
                ),
                categoryChange(
                    category = "Email Format Issues",
                    previousValue =
                        previousSummary.emailFormatIssues,
                    currentValue =
                        currentSummary.emailFormatIssues
                ),
                categoryChange(
                    category = "Numeric Anomaly Issues",
                    previousValue =
                        previousSummary.numericAnomalyIssues,
                    currentValue =
                        currentSummary.numericAnomalyIssues
                ),
                categoryChange(
                    category = "Potential Numeric Outlier Issues",
                    previousValue =
                        previousSummary.potentialNumericOutlierIssues,
                    currentValue =
                        currentSummary.potentialNumericOutlierIssues
                ),
                categoryChange(
                    category = "Date Anomaly Issues",
                    previousValue =
                        previousSummary.dateAnomalyIssues,
                    currentValue =
                        currentSummary.dateAnomalyIssues
                ),
                categoryChange(
                    category = "Constant Value Issues",
                    previousValue =
                        previousSummary.constantValueIssues,
                    currentValue =
                        currentSummary.constantValueIssues
                ),
                categoryChange(
                    category = "High NULL Percentage Issues",
                    previousValue =
                        previousSummary.highNullPercentageIssues,
                    currentValue =
                        currentSummary.highNullPercentageIssues
                )
            )

        val totalIssueChange =
            currentSummary.totalIssues -
                previousSummary.totalIssues

        val outcome =
            when {
                totalIssueChange < 0 ->
                    DatabaseQualityComparisonOutcome.IMPROVED

                totalIssueChange > 0 ->
                    DatabaseQualityComparisonOutcome.REGRESSED

                else ->
                    DatabaseQualityComparisonOutcome.UNCHANGED
            }

        return DatabaseQualityComparison(
            databaseName =
                current.databaseName,

            previousTotalIssues =
                previousSummary.totalIssues,

            currentTotalIssues =
                currentSummary.totalIssues,

            totalIssueChange =
                totalIssueChange,

            previousStatus =
                previousSummary.overallStatus,

            currentStatus =
                currentSummary.overallStatus,

            outcome =
                outcome,

            categoryChanges =
                categoryChanges
        )
    }


    private fun categoryChange(
        category: String,
        previousValue: Long,
        currentValue: Long
    ): DatabaseQualityCategoryChange {

        return DatabaseQualityCategoryChange(
            category = category,
            previousValue = previousValue,
            currentValue = currentValue,
            change = currentValue - previousValue
        )
    }
}
