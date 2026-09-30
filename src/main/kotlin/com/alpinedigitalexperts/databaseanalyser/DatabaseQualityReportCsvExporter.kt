package com.alpinedigitalexperts.databaseanalyser

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale

class DatabaseQualityReportCsvExporter {

    fun toCsv(
        report: DatabaseQualityReport
    ): String {

        val builder =
            StringBuilder()


        // ============================================================
        // COLUMN ANALYSIS HEADER
        // ============================================================

        appendRow(
            builder,
            listOf(
                "Database",
                "Schema",
                "Table",
                "Column",
                "Data Type",
                "Nullable",
                "Row Count",
                "Non-NULL Count",
                "NULL Count",
                "NULL Percentage",
                "Distinct Count",
                "Duplicate Value Count",
                "Empty String Count",
                "Whitespace-Only Count",
                "Leading/Trailing Whitespace Count",
                "Invalid Email Count",
                "Negative Value Count",
                "Potential Numeric Outlier Count",
                "Future Date Count",
                "Minimum",
                "Maximum",
                "Average",
                "Total",
                "Earliest Date",
                "Latest Date",
                "Primary Key",
                "Unique Column",
                "Foreign Key"
            )
        )


        // ============================================================
        // TABLE AND COLUMN DATA
        // ============================================================

        for (table in report.tables) {

            val foreignKeyColumns =
                table.foreignKeys.associateBy {
                    it.sourceColumnName
                }

            for (column in table.columns) {

                val isPrimaryKey =
                    column.columnName in
                        table.primaryKeys

                val isUniqueColumn =
                    column.columnName in
                        table.uniqueColumns

                val foreignKey =
                    foreignKeyColumns[
                        column.columnName
                    ]

                val foreignKeyDescription =
                    if (foreignKey != null) {

                        "${foreignKey.referencedSchemaName}." +
                            "${foreignKey.referencedTableName}." +
                            foreignKey.referencedColumnName

                    } else {

                        ""
                    }

                appendRow(
                    builder,
                    listOf(
                        table.databaseName,
                        table.schemaName,
                        table.tableName,
                        column.columnName,
                        column.dataType,
                        if (column.nullable) {
                            "YES"
                        } else {
                            "NO"
                        },
                        table.rowCount.toString(),
                        column.nonNullCount.toString(),
                        column.nullCount.toString(),
                        formatDouble(
                            column.nullPercentage
                        ),
                        column.distinctCount.toString(),
                        column.duplicateValueCount.toString(),
                        nullableLong(
                            column.emptyStringCount
                        ),
                        nullableLong(
                            column.whitespaceOnlyCount
                        ),
                        nullableLong(
                            column.leadingTrailingWhitespaceCount
                        ),
                        nullableLong(
                            column.invalidEmailCount
                        ),
                        nullableLong(
                            column.negativeValueCount
                        ),
                        nullableLong(
                            column.potentialNumericOutlierCount
                        ),
                        nullableLong(
                            column.futureDateCount
                        ),
                        column.numericStatistics
                            ?.minimum
                            ?: "",
                        column.numericStatistics
                            ?.maximum
                            ?: "",
                        column.numericStatistics
                            ?.average
                            ?: "",
                        column.numericStatistics
                            ?.total
                            ?: "",
                        column.dateStatistics
                            ?.earliest
                            ?: "",
                        column.dateStatistics
                            ?.latest
                            ?: "",
                        if (isPrimaryKey) {
                            "YES"
                        } else {
                            "NO"
                        },
                        if (isUniqueColumn) {
                            "YES"
                        } else {
                            "NO"
                        },
                        foreignKeyDescription
                    )
                )
            }
        }


        // ============================================================
        // GROUPED AGGREGATE ANALYSIS
        // ============================================================

        val hasGroupedAggregates =
            report.tables.any {
                it.groupedAggregates.isNotEmpty()
            }

        if (hasGroupedAggregates) {

            builder.appendLine()

            appendRow(
                builder,
                listOf(
                    "GROUPED AGGREGATE ANALYSIS"
                )
            )

            appendRow(
                builder,
                listOf(
                    "Database",
                    "Schema",
                    "Table",
                    "Group By Column",
                    "Numeric Column",
                    "Group Value",
                    "Row Count",
                    "Minimum",
                    "Maximum",
                    "Average",
                    "Total"
                )
            )

            for (table in report.tables) {

                for (aggregate in table.groupedAggregates) {

                    for (group in aggregate.groups) {

                        appendRow(
                            builder,
                            listOf(
                                table.databaseName,
                                table.schemaName,
                                table.tableName,
                                aggregate.groupByColumn,
                                aggregate.numericColumn,
                                group.groupValue ?: "",
                                group.rowCount.toString(),
                                group.minimum ?: "",
                                group.maximum ?: "",
                                group.average ?: "",
                                group.total ?: ""
                            )
                        )
                    }
                }
            }
        }


        // ============================================================
        // BLANK LINE BEFORE SUMMARY
        // ============================================================

        builder.appendLine()


        // ============================================================
        // DATABASE SUMMARY
        // ============================================================

        appendRow(
            builder,
            listOf(
                "SUMMARY"
            )
        )

        appendRow(
            builder,
            listOf(
                "Metric",
                "Value"
            )
        )

        val summary =
            report.summary

        appendSummaryRow(
            builder,
            "Database",
            report.databaseName
        )

        appendSummaryRow(
            builder,
            "Tables Analysed",
            summary.tablesAnalysed.toString()
        )

        appendSummaryRow(
            builder,
            "Columns Analysed",
            summary.columnsAnalysed.toString()
        )

        appendSummaryRow(
            builder,
            "Rows Analysed",
            summary.rowsAnalysed.toString()
        )


        // ============================================================
        // ISSUE SUMMARY
        // ============================================================

        appendSummaryRow(
            builder,
            "Primary Key Issues",
            summary.primaryKeyIssues.toString()
        )

        appendSummaryRow(
            builder,
            "Foreign Key Issues",
            summary.foreignKeyIssues.toString()
        )

        appendSummaryRow(
            builder,
            "Duplicate Value Issues",
            summary.duplicateValueIssues.toString()
        )

        appendSummaryRow(
            builder,
            "Potential Duplicate Record Issues",
            summary
                .potentialDuplicateRecordIssues
                .toString()
        )

        appendSummaryRow(
            builder,
            "Empty String Issues",
            summary.emptyStringIssues.toString()
        )

        appendSummaryRow(
            builder,
            "Whitespace-Only Issues",
            summary.whitespaceOnlyIssues.toString()
        )

        appendSummaryRow(
            builder,
            "Leading/Trailing Whitespace Issues",
            summary.leadingTrailingWhitespaceIssues.toString()
        )

        appendSummaryRow(
            builder,
            "Email Format Issues",
            summary.emailFormatIssues.toString()
        )

        appendSummaryRow(
            builder,
            "Numeric Anomaly Issues",
            summary.numericAnomalyIssues.toString()
        )

        appendSummaryRow(
            builder,
            "Potential Numeric Outlier Issues",
            summary
                .potentialNumericOutlierIssues
                .toString()
        )

        appendSummaryRow(
            builder,
            "Date Anomaly Issues",
            summary.dateAnomalyIssues.toString()
        )

        appendSummaryRow(
            builder,
            "Constant Value Issues",
            summary.constantValueIssues.toString()
        )

        appendSummaryRow(
            builder,
            "High NULL Percentage Issues",
            summary.highNullPercentageIssues.toString()
        )


        // ============================================================
        // OVERALL SUMMARY
        // ============================================================

        appendSummaryRow(
            builder,
            "Total Issues",
            summary.totalIssues.toString()
        )

        val statusText =
            when (
                summary.overallStatus
            ) {

                DatabaseQualityStatus.GOOD ->
                    "GOOD"

                DatabaseQualityStatus.REVIEW ->
                    "REVIEW"

                DatabaseQualityStatus.ATTENTION_REQUIRED ->
                    "ATTENTION REQUIRED"
            }

        appendSummaryRow(
            builder,
            "Overall Status",
            statusText
        )

        return builder.toString()
    }


    // ============================================================
    // WRITE CSV TO FILE
    // ============================================================

    fun writeToFile(
        report: DatabaseQualityReport,
        outputPath: Path
    ): Path {

        val parentDirectory =
            outputPath.parent

        if (parentDirectory != null) {

            Files.createDirectories(
                parentDirectory
            )
        }

        Files.writeString(
            outputPath,
            toCsv(report),
            StandardCharsets.UTF_8
        )

        return outputPath
    }


    // ============================================================
    // APPEND NORMAL CSV ROW
    // ============================================================

    private fun appendRow(
        builder: StringBuilder,
        values: List<String>
    ) {

        builder.append(
            values.joinToString(",") {
                escapeCsvValue(it)
            }
        )

        builder.appendLine()
    }


    // ============================================================
    // APPEND SUMMARY ROW
    // ============================================================

    private fun appendSummaryRow(
        builder: StringBuilder,
        metric: String,
        value: String
    ) {

        appendRow(
            builder,
            listOf(
                metric,
                value
            )
        )
    }


    // ============================================================
    // ESCAPE CSV VALUES
    // ============================================================

    private fun escapeCsvValue(
        value: String
    ): String {

        val escapedValue =
            value.replace(
                "\"",
                "\"\""
            )

        return if (
            escapedValue.contains(",") ||
            escapedValue.contains("\"") ||
            escapedValue.contains("\n") ||
            escapedValue.contains("\r")
        ) {

            "\"$escapedValue\""

        } else {

            escapedValue
        }
    }


    // ============================================================
    // NULLABLE LONG
    // ============================================================

    private fun nullableLong(
        value: Long?
    ): String {

        return value?.toString()
            ?: ""
    }


    // ============================================================
    // FORMAT DOUBLE
    // ============================================================

    private fun formatDouble(
        value: Double
    ): String {

        return String.format(
            Locale.US,
            "%.2f",
            value
        )
    }
}
