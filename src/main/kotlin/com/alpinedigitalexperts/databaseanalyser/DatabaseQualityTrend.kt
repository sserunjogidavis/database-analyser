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
    val outcome: DatabaseQualityTrendOutcome
)


@Serializable
data class DatabaseQualityTrendSnapshot(
    val sequence: Int,
    val totalIssues: Long,
    val overallStatus: DatabaseQualityStatus
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

                DatabaseQualityTrendSnapshot(
                    sequence = index + 1,
                    totalIssues =
                        report.summary.totalIssues,
                    overallStatus =
                        report.summary.overallStatus
                )
            }

        val firstReport =
            reports.first()

        val latestReport =
            reports.last()

        val firstTotalIssues =
            firstReport.summary.totalIssues

        val latestTotalIssues =
            latestReport.summary.totalIssues

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
                firstReport.summary.overallStatus,

            latestStatus =
                latestReport.summary.overallStatus,

            outcome =
                outcome
        )
    }
}
