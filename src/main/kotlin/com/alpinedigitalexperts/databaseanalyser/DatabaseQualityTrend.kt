package com.alpinedigitalexperts.databaseanalyser

import kotlinx.serialization.Serializable


@Serializable
data class DatabaseQualityTrend(
    val databaseName: String,

    val snapshots: List<DatabaseQualityTrendSnapshot>,

    val firstTotalIssues: Long,
    val latestTotalIssues: Long,
    val totalIssueChange: Long,

    val firstStatus: DatabaseQualityStatus,
    val latestStatus: DatabaseQualityStatus,

    val outcome: DatabaseQualityTrendOutcome,

    val categoryChanges: List<DatabaseQualityTrendCategoryChange>
)


@Serializable
data class DatabaseQualityTrendSnapshot(
    val sequence: Int,

    val totalIssues: Long,
    val overallStatus: DatabaseQualityStatus,

    val primaryKeyIssues: Long,
    val foreignKeyIssues: Long,
    val duplicateValueIssues: Long,
    val potentialDuplicateRecordIssues: Long,
    val emptyStringIssues: Long,
    val whitespaceOnlyIssues: Long,
    val leadingTrailingWhitespaceIssues: Long,
    val emailFormatIssues: Long,
    val numericAnomalyIssues: Long,
    val potentialNumericOutlierIssues: Long,
    val dateAnomalyIssues: Long,
    val constantValueIssues: Long,
    val highNullPercentageIssues: Long
)


@Serializable
data class DatabaseQualityTrendCategoryChange(
    val category: String,
    val firstValue: Long,
    val latestValue: Long,
    val change: Long
)


@Serializable
enum class DatabaseQualityTrendOutcome {
    IMPROVING,
    WORSENING,
    STABLE
}


object DatabaseQualityTrendAnalyzer {

    fun analyze(
        reports: List<DatabaseQualityReport>
    ): DatabaseQualityTrend {

        require(
            reports.isNotEmpty()
        ) {
            "At least one database quality report is required."
        }

        val databaseName =
            reports.first().databaseName

        require(
            reports.all {
                it.databaseName == databaseName
            }
        ) {
            "All reports must belong to the same database."
        }

        val snapshots =
            reports.mapIndexed { index, report ->

                val summary =
                    report.summary

                DatabaseQualityTrendSnapshot(
                    sequence = index + 1,

                    totalIssues =
                        summary.totalIssues,

                    overallStatus =
                        summary.overallStatus,

                    primaryKeyIssues =
                        summary.primaryKeyIssues,

                    foreignKeyIssues =
                        summary.foreignKeyIssues,

                    duplicateValueIssues =
                        summary.duplicateValueIssues,

                    potentialDuplicateRecordIssues =
                        summary.potentialDuplicateRecordIssues,

                    emptyStringIssues =
                        summary.emptyStringIssues,

                    whitespaceOnlyIssues =
                        summary.whitespaceOnlyIssues,

                    leadingTrailingWhitespaceIssues =
                        summary.leadingTrailingWhitespaceIssues,

                    emailFormatIssues =
                        summary.emailFormatIssues,

                    numericAnomalyIssues =
                        summary.numericAnomalyIssues,

                    potentialNumericOutlierIssues =
                        summary.potentialNumericOutlierIssues,

                    dateAnomalyIssues =
                        summary.dateAnomalyIssues,

                    constantValueIssues =
                        summary.constantValueIssues,

                    highNullPercentageIssues =
                        summary.highNullPercentageIssues
                )
            }

        val firstReport =
            reports.first()

        val latestReport =
            reports.last()

        val firstSummary =
            firstReport.summary

        val latestSummary =
            latestReport.summary

        val firstTotalIssues =
            firstSummary.totalIssues

        val latestTotalIssues =
            latestSummary.totalIssues

        val totalIssueChange =
            latestTotalIssues -
                firstTotalIssues

        val outcome =
            when {
                totalIssueChange < 0 ->
                    DatabaseQualityTrendOutcome.IMPROVING

                totalIssueChange > 0 ->
                    DatabaseQualityTrendOutcome.WORSENING

                else ->
                    DatabaseQualityTrendOutcome.STABLE
            }

        val categoryChanges =
            listOf(
                categoryChange(
                    category = "Primary Key Issues",
                    firstValue =
                        firstSummary.primaryKeyIssues,
                    latestValue =
                        latestSummary.primaryKeyIssues
                ),

                categoryChange(
                    category = "Foreign Key Issues",
                    firstValue =
                        firstSummary.foreignKeyIssues,
                    latestValue =
                        latestSummary.foreignKeyIssues
                ),

                categoryChange(
                    category = "Duplicate Value Issues",
                    firstValue =
                        firstSummary.duplicateValueIssues,
                    latestValue =
                        latestSummary.duplicateValueIssues
                ),

                categoryChange(
                    category = "Potential Duplicate Record Issues",
                    firstValue =
                        firstSummary.potentialDuplicateRecordIssues,
                    latestValue =
                        latestSummary.potentialDuplicateRecordIssues
                ),

                categoryChange(
                    category = "Empty String Issues",
                    firstValue =
                        firstSummary.emptyStringIssues,
                    latestValue =
                        latestSummary.emptyStringIssues
                ),

                categoryChange(
                    category = "Whitespace-Only Issues",
                    firstValue =
                        firstSummary.whitespaceOnlyIssues,
                    latestValue =
                        latestSummary.whitespaceOnlyIssues
                ),

                categoryChange(
                    category = "Leading/Trailing Whitespace Issues",
                    firstValue =
                        firstSummary.leadingTrailingWhitespaceIssues,
                    latestValue =
                        latestSummary.leadingTrailingWhitespaceIssues
                ),

                categoryChange(
                    category = "Email Format Issues",
                    firstValue =
                        firstSummary.emailFormatIssues,
                    latestValue =
                        latestSummary.emailFormatIssues
                ),

                categoryChange(
                    category = "Numeric Anomaly Issues",
                    firstValue =
                        firstSummary.numericAnomalyIssues,
                    latestValue =
                        latestSummary.numericAnomalyIssues
                ),

                categoryChange(
                    category = "Potential Numeric Outlier Issues",
                    firstValue =
                        firstSummary.potentialNumericOutlierIssues,
                    latestValue =
                        latestSummary.potentialNumericOutlierIssues
                ),

                categoryChange(
                    category = "Date Anomaly Issues",
                    firstValue =
                        firstSummary.dateAnomalyIssues,
                    latestValue =
                        latestSummary.dateAnomalyIssues
                ),

                categoryChange(
                    category = "Constant Value Issues",
                    firstValue =
                        firstSummary.constantValueIssues,
                    latestValue =
                        latestSummary.constantValueIssues
                ),

                categoryChange(
                    category = "High NULL Percentage Issues",
                    firstValue =
                        firstSummary.highNullPercentageIssues,
                    latestValue =
                        latestSummary.highNullPercentageIssues
                )
            )

        return DatabaseQualityTrend(
            databaseName =
                databaseName,

            snapshots =
                snapshots,

            firstTotalIssues =
                firstTotalIssues,

            latestTotalIssues =
                latestTotalIssues,

            totalIssueChange =
                totalIssueChange,

            firstStatus =
                firstSummary.overallStatus,

            latestStatus =
                latestSummary.overallStatus,

            outcome =
                outcome,

            categoryChanges =
                categoryChanges
        )
    }


    private fun categoryChange(
        category: String,
        firstValue: Long,
        latestValue: Long
    ): DatabaseQualityTrendCategoryChange {

        return DatabaseQualityTrendCategoryChange(
            category = category,
            firstValue = firstValue,
            latestValue = latestValue,
            change = latestValue - firstValue
        )
    }
}
