package com.alpinedigitalexperts.databaseanalyser

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class DatabaseQualityReportHtmlExporter {

    fun toHtml(
        report: DatabaseQualityReport
    ): String {

        val builder =
            StringBuilder()

        val summary =
            report.summary

        val statusClass =
            when (
                summary.overallStatus
            ) {

                DatabaseQualityStatus.GOOD ->
                    "status-good"

                DatabaseQualityStatus.REVIEW ->
                    "status-review"

                DatabaseQualityStatus.ATTENTION_REQUIRED ->
                    "status-attention"
            }

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

        builder.appendLine("<!DOCTYPE html>")
        builder.appendLine("<html lang=\"en\">")
        builder.appendLine("<head>")
        builder.appendLine("    <meta charset=\"UTF-8\">")
        builder.appendLine(
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
        )
        builder.appendLine(
            "    <title>Database Quality Report</title>"
        )

        builder.appendLine(
            """
            <style>
                body {
                    font-family: Arial, sans-serif;
                    margin: 0;
                    padding: 0;
                    background: #f5f7fa;
                    color: #222;
                }

                .container {
                    max-width: 1200px;
                    margin: 0 auto;
                    padding: 32px 20px;
                }

                h1,
                h2,
                h3 {
                    margin-top: 0;
                }

                .header,
                .table-card {
                    background: white;
                    border-radius: 12px;
                    padding: 24px;
                    margin-bottom: 24px;
                    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
                }

                .summary-grid {
                    display: grid;
                    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
                    gap: 16px;
                    margin-top: 20px;
                }

                .summary-card {
                    background: white;
                    border-radius: 10px;
                    padding: 18px;
                    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
                }

                .summary-label {
                    font-size: 13px;
                    color: #666;
                    margin-bottom: 6px;
                }

                .summary-value {
                    font-size: 24px;
                    font-weight: bold;
                }

                .status-good,
                .ok {
                    color: #1f7a1f;
                    font-weight: bold;
                }

                .status-review,
                .warning {
                    color: #b36b00;
                    font-weight: bold;
                }

                .status-attention,
                .critical {
                    color: #b00020;
                    font-weight: bold;
                }

                table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-top: 12px;
                    margin-bottom: 24px;
                }

                th,
                td {
                    border: 1px solid #dfe3e8;
                    padding: 10px 12px;
                    text-align: left;
                    vertical-align: top;
                }

                th {
                    background: #f0f2f5;
                }

                .section-title {
                    margin-top: 28px;
                    margin-bottom: 10px;
                    border-bottom: 2px solid #e4e7eb;
                    padding-bottom: 6px;
                }

                .muted {
                    color: #666;
                }

                .mono {
                    font-family: Consolas, monospace;
                }

                .table-scroll {
                    overflow-x: auto;
                }

                .footer {
                    margin-top: 32px;
                    text-align: center;
                    color: #777;
                    font-size: 13px;
                }
            </style>
            """.trimIndent()
        )

        builder.appendLine("</head>")
        builder.appendLine("<body>")
        builder.appendLine("<div class=\"container\">")


        // ========================================================
        // HEADER
        // ========================================================

        builder.appendLine("<div class=\"header\">")
        builder.appendLine("<h1>Database Quality Report</h1>")

        builder.appendLine(
            "<p><strong>Database:</strong> ${escape(report.databaseName)}</p>"
        )

        builder.appendLine("<div class=\"summary-grid\">")

        appendSummaryCard(
            builder,
            "Tables Analysed",
            summary.tablesAnalysed.toString()
        )

        appendSummaryCard(
            builder,
            "Columns Analysed",
            summary.columnsAnalysed.toString()
        )

        appendSummaryCard(
            builder,
            "Rows Analysed",
            summary.rowsAnalysed.toString()
        )

        appendSummaryCard(
            builder,
            "Total Issues",
            summary.totalIssues.toString()
        )

        builder.appendLine(
            """
            <div class="summary-card">
                <div class="summary-label">Overall Status</div>
                <div class="summary-value $statusClass">$statusText</div>
            </div>
            """.trimIndent()
        )

        builder.appendLine("</div>")
        builder.appendLine("</div>")


        // ========================================================
        // ISSUE SUMMARY
        // ========================================================

        builder.appendLine("<div class=\"table-card\">")
        builder.appendLine("<h2>Issue Summary</h2>")

        builder.appendLine("<table>")
        builder.appendLine("<thead>")
        builder.appendLine("<tr>")
        builder.appendLine("<th>Issue Type</th>")
        builder.appendLine("<th>Count</th>")
        builder.appendLine("</tr>")
        builder.appendLine("</thead>")
        builder.appendLine("<tbody>")

        appendIssueRow(
            builder,
            "Primary Key Issues",
            summary.primaryKeyIssues
        )

        appendIssueRow(
            builder,
            "Foreign Key Issues",
            summary.foreignKeyIssues
        )

        appendIssueRow(
            builder,
            "Duplicate Value Issues",
            summary.duplicateValueIssues
        )

        appendIssueRow(
            builder,
            "Potential Duplicate Record Issues",
            summary.potentialDuplicateRecordIssues
        )

        appendIssueRow(
            builder,
            "Empty String Issues",
            summary.emptyStringIssues
        )

        appendIssueRow(
            builder,
            "Whitespace-Only Issues",
            summary.whitespaceOnlyIssues
        )

        appendIssueRow(
            builder,
            "Leading/Trailing Whitespace Issues",
            summary.leadingTrailingWhitespaceIssues
        )

        appendIssueRow(
            builder,
            "Email Format Issues",
            summary.emailFormatIssues
        )

        appendIssueRow(
            builder,
            "Numeric Anomaly Issues",
            summary.numericAnomalyIssues
        )

        appendIssueRow(
            builder,
            "Potential Numeric Outlier Issues",
            summary.potentialNumericOutlierIssues
        )

        appendIssueRow(
            builder,
            "Date Anomaly Issues",
            summary.dateAnomalyIssues
        )

        appendIssueRow(
            builder,
            "Constant Value Issues",
            summary.constantValueIssues
        )

        appendIssueRow(
            builder,
            "High NULL Percentage Issues",
            summary.highNullPercentageIssues
        )

        builder.appendLine("</tbody>")
        builder.appendLine("</table>")
        builder.appendLine("</div>")


        // ========================================================
        // TABLE REPORTS
        // ========================================================

        for (table in report.tables) {

            builder.appendLine("<div class=\"table-card\">")

            builder.appendLine(
                "<h2>${escape(table.schemaName)}.${escape(table.tableName)}</h2>"
            )

            builder.appendLine(
                "<p><strong>Qualified Name:</strong> " +
                    "<span class=\"mono\">${escape(table.qualifiedName)}</span></p>"
            )

            builder.appendLine(
                "<p><strong>Rows:</strong> ${table.rowCount}</p>"
            )


            // ====================================================
            // KEY ANALYSIS
            // ====================================================

            builder.appendLine(
                "<h3 class=\"section-title\">Key Analysis</h3>"
            )

            builder.appendLine(
                "<p><strong>Primary Keys:</strong> ${
                    if (table.primaryKeys.isEmpty()) {
                        "<span class=\"muted\">None</span>"
                    } else {
                        table.primaryKeys
                            .joinToString(", ") {
                                escape(it)
                            }
                    }
                }</p>"
            )

            builder.appendLine(
                "<p><strong>Unique Columns:</strong> ${
                    if (table.uniqueColumns.isEmpty()) {
                        "<span class=\"muted\">None</span>"
                    } else {
                        table.uniqueColumns
                            .joinToString(", ") {
                                escape(it)
                            }
                    }
                }</p>"
            )

            builder.appendLine(
                "<p><strong>Primary Key Quality:</strong> ${
                    if (table.hasPrimaryKey) {
                        "<span class=\"ok\">OK</span>"
                    } else {
                        "<span class=\"warning\">WARNING</span>"
                    }
                }</p>"
            )


            // ====================================================
            // FOREIGN KEYS
            // ====================================================

            builder.appendLine(
                "<h3 class=\"section-title\">Foreign Keys</h3>"
            )

            if (table.foreignKeys.isEmpty()) {

                builder.appendLine(
                    "<p class=\"muted\">No foreign keys</p>"
                )

            } else {

                builder.appendLine("<table>")
                builder.appendLine("<thead>")
                builder.appendLine("<tr>")
                builder.appendLine("<th>Source Column</th>")
                builder.appendLine("<th>Referenced Column</th>")
                builder.appendLine("<th>Invalid References</th>")
                builder.appendLine("</tr>")
                builder.appendLine("</thead>")
                builder.appendLine("<tbody>")

                for (foreignKey in table.foreignKeys) {

                    builder.appendLine("<tr>")

                    builder.appendLine(
                        "<td>${escape(foreignKey.sourceColumnName)}</td>"
                    )

                    builder.appendLine(
                        "<td>${escape(foreignKey.referencedQualifiedName)}</td>"
                    )

                    builder.appendLine(
                        "<td>${foreignKey.invalidReferenceCount}</td>"
                    )

                    builder.appendLine("</tr>")
                }

                builder.appendLine("</tbody>")
                builder.appendLine("</table>")
            }


            // ====================================================
            // COLUMN ANALYSIS
            // ====================================================

            builder.appendLine(
                "<h3 class=\"section-title\">Column Analysis</h3>"
            )

            builder.appendLine("<div class=\"table-scroll\">")
            builder.appendLine("<table>")
            builder.appendLine("<thead>")
            builder.appendLine("<tr>")
            builder.appendLine("<th>Column</th>")
            builder.appendLine("<th>Type</th>")
            builder.appendLine("<th>Nullable</th>")
            builder.appendLine("<th>Row Count</th>")
            builder.appendLine("<th>Non-NULL Count</th>")
            builder.appendLine("<th>NULL Count</th>")
            builder.appendLine("<th>NULL %</th>")
            builder.appendLine("<th>Distinct</th>")
            builder.appendLine("<th>Duplicates</th>")
            builder.appendLine("</tr>")
            builder.appendLine("</thead>")
            builder.appendLine("<tbody>")

            for (column in table.columns) {

                builder.appendLine("<tr>")

                builder.appendLine(
                    "<td>${escape(column.columnName)}</td>"
                )

                builder.appendLine(
                    "<td>${escape(column.dataType)}</td>"
                )

                builder.appendLine(
                    "<td>${if (column.nullable) "YES" else "NO"}</td>"
                )

                builder.appendLine(
                    "<td>${table.rowCount}</td>"
                )

                builder.appendLine(
                    "<td>${column.nonNullCount}</td>"
                )

                builder.appendLine(
                    "<td>${column.nullCount}</td>"
                )

                builder.appendLine(
                    "<td>${String.format("%.2f", column.nullPercentage)}%</td>"
                )

                builder.appendLine(
                    "<td>${column.distinctCount}</td>"
                )

                builder.appendLine(
                    "<td>${column.duplicateValueCount}</td>"
                )

                builder.appendLine("</tr>")
            }

            builder.appendLine("</tbody>")
            builder.appendLine("</table>")
            builder.appendLine("</div>")


            // ====================================================
            // TEXT AND EMAIL QUALITY
            // ====================================================

            builder.appendLine(
                "<h3 class=\"section-title\">Text and Email Quality</h3>"
            )

            val textColumns =
                table.columns.filter {
                    it.emptyStringCount != null
                }

            if (textColumns.isEmpty()) {

                builder.appendLine(
                    "<p class=\"muted\">No text columns</p>"
                )

            } else {

                builder.appendLine("<table>")
                builder.appendLine("<thead>")
                builder.appendLine("<tr>")
                builder.appendLine("<th>Column</th>")
                builder.appendLine("<th>Empty Strings</th>")
                builder.appendLine("<th>Whitespace-Only Values</th>")
                builder.appendLine("<th>Leading/Trailing Whitespace</th>")
                builder.appendLine("<th>Invalid Emails</th>")
                builder.appendLine("</tr>")
                builder.appendLine("</thead>")
                builder.appendLine("<tbody>")

                for (column in textColumns) {

                    builder.appendLine("<tr>")

                    builder.appendLine(
                        "<td>${escape(column.columnName)}</td>"
                    )

                    builder.appendLine(
                        "<td>${column.emptyStringCount ?: "-"}</td>"
                    )

                    builder.appendLine(
                        "<td>${column.whitespaceOnlyCount ?: "-"}</td>"
                    )

                    builder.appendLine(
                        "<td>${column.leadingTrailingWhitespaceCount ?: "-"}</td>"
                    )

                    builder.appendLine(
                        "<td>${column.invalidEmailCount ?: "-"}</td>"
                    )

                    builder.appendLine("</tr>")
                }

                builder.appendLine("</tbody>")
                builder.appendLine("</table>")
            }


            // ====================================================
            // NUMERIC ANALYSIS
            // ====================================================

            builder.appendLine(
                "<h3 class=\"section-title\">Numeric Analysis</h3>"
            )

            val numericColumns =
                table.columns.filter {
                    it.numericStatistics != null
                }

            if (numericColumns.isEmpty()) {

                builder.appendLine(
                    "<p class=\"muted\">No numeric business columns</p>"
                )

            } else {

                builder.appendLine("<div class=\"table-scroll\">")
                builder.appendLine("<table>")
                builder.appendLine("<thead>")
                builder.appendLine("<tr>")
                builder.appendLine("<th>Column</th>")
                builder.appendLine("<th>Non-NULL Count</th>")
                builder.appendLine("<th>Distinct Count</th>")
                builder.appendLine("<th>Minimum</th>")
                builder.appendLine("<th>Maximum</th>")
                builder.appendLine("<th>Average</th>")
                builder.appendLine("<th>Total</th>")
                builder.appendLine("<th>Negative Values</th>")
                builder.appendLine("<th>Potential Outliers</th>")
                builder.appendLine("</tr>")
                builder.appendLine("</thead>")
                builder.appendLine("<tbody>")

                for (column in numericColumns) {

                    val statistics =
                        column.numericStatistics!!

                    builder.appendLine("<tr>")

                    builder.appendLine(
                        "<td>${escape(column.columnName)}</td>"
                    )

                    builder.appendLine(
                        "<td>${column.nonNullCount}</td>"
                    )

                    builder.appendLine(
                        "<td>${column.distinctCount}</td>"
                    )

                    builder.appendLine(
                        "<td>${escape(statistics.minimum ?: "")}</td>"
                    )

                    builder.appendLine(
                        "<td>${escape(statistics.maximum ?: "")}</td>"
                    )

                    builder.appendLine(
                        "<td>${escape(statistics.average ?: "")}</td>"
                    )

                    builder.appendLine(
                        "<td>${escape(statistics.total ?: "")}</td>"
                    )

                    builder.appendLine(
                        "<td>${column.negativeValueCount ?: "-"}</td>"
                    )

                    builder.appendLine(
                        "<td>${column.potentialNumericOutlierCount ?: "-"}</td>"
                    )

                    builder.appendLine("</tr>")
                }

                builder.appendLine("</tbody>")
                builder.appendLine("</table>")
                builder.appendLine("</div>")
            }


            // ====================================================
            // GROUPED AGGREGATE ANALYSIS
            // ====================================================

            builder.appendLine(
                "<h3 class=\"section-title\">Grouped Aggregate Analysis</h3>"
            )

            if (table.groupedAggregates.isEmpty()) {

                builder.appendLine(
                    "<p class=\"muted\">No grouped aggregate analysis configured</p>"
                )

            } else {

                for (aggregate in table.groupedAggregates) {

                    builder.appendLine(
                        "<p><strong>Group By:</strong> " +
                            "${escape(aggregate.groupByColumn)} " +
                            "&nbsp; <strong>Numeric Column:</strong> " +
                            "${escape(aggregate.numericColumn)}</p>"
                    )

                    builder.appendLine("<div class=\"table-scroll\">")
                    builder.appendLine("<table>")
                    builder.appendLine("<thead>")
                    builder.appendLine("<tr>")
                    builder.appendLine("<th>Group</th>")
                    builder.appendLine("<th>Rows</th>")
                    builder.appendLine("<th>Minimum</th>")
                    builder.appendLine("<th>Maximum</th>")
                    builder.appendLine("<th>Average</th>")
                    builder.appendLine("<th>Total</th>")
                    builder.appendLine("</tr>")
                    builder.appendLine("</thead>")
                    builder.appendLine("<tbody>")

                    for (group in aggregate.groups) {

                        builder.appendLine("<tr>")

                        builder.appendLine(
                            "<td>${escape(group.groupValue ?: "NULL")}</td>"
                        )

                        builder.appendLine(
                            "<td>${group.rowCount}</td>"
                        )

                        builder.appendLine(
                            "<td>${escape(group.minimum ?: "")}</td>"
                        )

                        builder.appendLine(
                            "<td>${escape(group.maximum ?: "")}</td>"
                        )

                        builder.appendLine(
                            "<td>${escape(group.average ?: "")}</td>"
                        )

                        builder.appendLine(
                            "<td>${escape(group.total ?: "")}</td>"
                        )

                        builder.appendLine("</tr>")
                    }

                    builder.appendLine("</tbody>")
                    builder.appendLine("</table>")
                    builder.appendLine("</div>")
                }
            }


            // ====================================================
            // DATE ANALYSIS
            // ====================================================

            builder.appendLine(
                "<h3 class=\"section-title\">Date Analysis</h3>"
            )

            val dateColumns =
                table.columns.filter {
                    it.dateStatistics != null
                }

            if (dateColumns.isEmpty()) {

                builder.appendLine(
                    "<p class=\"muted\">No date columns</p>"
                )

            } else {

                builder.appendLine("<table>")
                builder.appendLine("<thead>")
                builder.appendLine("<tr>")
                builder.appendLine("<th>Column</th>")
                builder.appendLine("<th>Earliest</th>")
                builder.appendLine("<th>Latest</th>")
                builder.appendLine("<th>Future Dates</th>")
                builder.appendLine("</tr>")
                builder.appendLine("</thead>")
                builder.appendLine("<tbody>")

                for (column in dateColumns) {

                    val statistics =
                        column.dateStatistics!!

                    builder.appendLine("<tr>")

                    builder.appendLine(
                        "<td>${escape(column.columnName)}</td>"
                    )

                    builder.appendLine(
                        "<td>${escape(statistics.earliest ?: "")}</td>"
                    )

                    builder.appendLine(
                        "<td>${escape(statistics.latest ?: "")}</td>"
                    )

                    builder.appendLine(
                        "<td>${column.futureDateCount ?: "-"}</td>"
                    )

                    builder.appendLine("</tr>")
                }

                builder.appendLine("</tbody>")
                builder.appendLine("</table>")
            }


            // ====================================================
            // POTENTIAL DUPLICATE RECORDS
            // ====================================================

            builder.appendLine(
                "<h3 class=\"section-title\">" +
                    "Potential Duplicate Records</h3>"
            )

            if (
                table.potentialDuplicateRecordCount == 0L
            ) {

                builder.appendLine(
                    "<p class=\"ok\">" +
                        "No potential duplicate records found</p>"
                )

            } else {

                builder.appendLine(
                    "<p class=\"warning\">" +
                        "${table.potentialDuplicateRecordCount} " +
                        "potential duplicate record(s) found</p>"
                )
            }

            builder.appendLine("</div>")
        }

        builder.appendLine(
            "<div class=\"footer\">Generated by Database Analyser</div>"
        )

        builder.appendLine("</div>")
        builder.appendLine("</body>")
        builder.appendLine("</html>")

        return builder.toString()
    }


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
            toHtml(report),
            StandardCharsets.UTF_8
        )

        return outputPath
    }


    private fun appendSummaryCard(
        builder: StringBuilder,
        label: String,
        value: String
    ) {

        builder.appendLine(
            """
            <div class="summary-card">
                <div class="summary-label">${escape(label)}</div>
                <div class="summary-value">${escape(value)}</div>
            </div>
            """.trimIndent()
        )
    }


    private fun appendIssueRow(
        builder: StringBuilder,
        label: String,
        count: Long
    ) {

        val cssClass =
            if (count == 0L) {
                "ok"
            } else {
                "warning"
            }

        builder.appendLine("<tr>")

        builder.appendLine(
            "<td>${escape(label)}</td>"
        )

        builder.appendLine(
            "<td class=\"$cssClass\">$count</td>"
        )

        builder.appendLine("</tr>")
    }


    private fun escape(
        value: String
    ): String {

        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
