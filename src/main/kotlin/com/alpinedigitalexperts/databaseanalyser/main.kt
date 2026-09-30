package com.alpinedigitalexperts.databaseanalyser

import java.nio.file.Path

fun main() {

    println("============================================================")
    println("DATABASE ANALYSER")
    println("============================================================")
    println()

    DatabaseConnector.connect().use { connection ->

        println("[OK] Database connected successfully.")
        println()

        val analyzer =
            DatabaseAnalyzer(connection)

        val reportBuilder =
            DatabaseQualityReportBuilder(
                analyzer
            )

        val report =
            reportBuilder.buildDatabaseReport()


        // ========================================================
        // APPLICATION CATALOG
        // ========================================================

        val catalogBuilder =
            DatabaseCatalogBuilder(
                analyzer
            )

        val applicationCatalog =
            catalogBuilder.buildCatalog()

        val catalogExporter =
            ApplicationCatalogJsonExporter()

        val catalogOutputPath =
            Path.of(
                "output",
                "application-catalog.json"
            )

        val writtenCatalogPath =
            catalogExporter.writeToFile(
                catalog = applicationCatalog,
                outputPath = catalogOutputPath
            )


        // ========================================================
        // JSON EXPORT
        // ========================================================

        val jsonExporter =
            DatabaseQualityReportJsonExporter()

        val jsonOutputPath =
            Path.of(
                "output",
                "database-quality-report.json"
            )

        val writtenJsonPath =
            jsonExporter.writeToFile(
                report = report,
                outputPath = jsonOutputPath
            )


        // ========================================================
        // HTML EXPORT
        // ========================================================

        val htmlExporter =
            DatabaseQualityReportHtmlExporter()

        val htmlOutputPath =
            Path.of(
                "output",
                "database-quality-report.html"
            )

        val writtenHtmlPath =
            htmlExporter.writeToFile(
                report = report,
                outputPath = htmlOutputPath
            )


        // ========================================================
        // CSV EXPORT
        // ========================================================

        val csvExporter =
            DatabaseQualityReportCsvExporter()

        val csvOutputPath =
            Path.of(
                "output",
                "database-quality-report.csv"
            )

        val writtenCsvPath =
            csvExporter.writeToFile(
                report = report,
                outputPath = csvOutputPath
            )


        // ========================================================
        // DATABASE SUMMARY
        // ========================================================

        println("============================================================")
        println("DATABASE SUMMARY")
        println("============================================================")
        println()

        println(
            "Database: ${report.databaseName}"
        )

        println(
            "Tables found: ${report.tables.size}"
        )

        for (table in report.tables) {

            println(
                "  - ${table.schemaName}.${table.tableName}"
            )
        }

        println()


        // ========================================================
        // TABLE REPORTS
        // ========================================================

        for (table in report.tables) {

            println("============================================================")
            println(
                "TABLE: ${table.schemaName}.${table.tableName}"
            )
            println("============================================================")
            println()


            // ====================================================
            // SCHEMA ANALYSIS
            // ====================================================

            println("------------------------------------------------------------")
            println("SCHEMA ANALYSIS")
            println("------------------------------------------------------------")

            println("Database:")
            println("  ${table.databaseName}")

            println()

            println("Schema:")
            println("  ${table.schemaName}")

            println()

            println("Qualified Name:")
            println("  ${table.qualifiedName}")

            println()

            println("Columns:")

            for (column in table.columns) {

                val defaultValue =
                    column.defaultValue
                        ?: "none"

                val nullable =
                    if (column.nullable) {
                        "YES"
                    } else {
                        "NO"
                    }

                println(
                    "  ${column.columnName} | " +
                        "${column.dataType} | " +
                        "Nullable: $nullable | " +
                        "Default: $defaultValue"
                )
            }

            println()

            println("Row Count:")
            println(
                "  ${table.rowCount}"
            )

            println()


            // ====================================================
            // KEY ANALYSIS
            // ====================================================

            println("------------------------------------------------------------")
            println("KEY ANALYSIS")
            println("------------------------------------------------------------")

            println("Primary Keys:")

            if (table.primaryKeys.isEmpty()) {

                println("  None")

            } else {

                for (primaryKey in table.primaryKeys) {

                    println(
                        "  $primaryKey"
                    )
                }
            }

            println()

            println("Unique Columns:")

            if (table.uniqueColumns.isEmpty()) {

                println("  None")

            } else {

                for (uniqueColumn in table.uniqueColumns) {

                    println(
                        "  $uniqueColumn"
                    )
                }
            }

            println()

            println("Foreign Keys:")

            if (table.foreignKeys.isEmpty()) {

                println("  None")

            } else {

                for (foreignKey in table.foreignKeys) {

                    println(
                        "  ${foreignKey.sourceColumnName} -> " +
                            foreignKey.referencedQualifiedName
                    )
                }
            }

            println()

            println("Primary Key Quality:")

            if (table.hasPrimaryKey) {

                println(
                    "  [OK] Primary key exists"
                )

            } else {

                println(
                    "  [WARNING] No primary key found"
                )
            }

            println()

            println("Foreign Key Quality:")

            if (table.foreignKeys.isEmpty()) {

                println(
                    "  No foreign keys to analyse"
                )

            } else {

                for (foreignKey in table.foreignKeys) {

                    println(
                        "  ${foreignKey.sourceColumnName} -> " +
                            foreignKey.referencedQualifiedName
                    )

                    if (
                        foreignKey.invalidReferenceCount == 0L
                    ) {

                        println(
                            "    [OK] All foreign-key values are valid"
                        )

                    } else {

                        println(
                            "    [WARNING] " +
                                "${foreignKey.invalidReferenceCount} " +
                                "invalid foreign-key reference(s) found"
                        )
                    }
                }
            }

            println()


            // ====================================================
            // NULL AND COUNT ANALYSIS
            // ====================================================

            println("------------------------------------------------------------")
            println("NULL AND COUNT ANALYSIS")
            println("------------------------------------------------------------")

            for (column in table.columns) {

                println(
                    "  ${column.columnName}:"
                )

                println(
                    "    Row Count: ${table.rowCount}"
                )

                println(
                    "    Non-NULL Count: ${column.nonNullCount}"
                )

                println(
                    "    NULL Count: ${column.nullCount}"
                )

                println(
                    "    NULL Percentage: ${
                        String.format(
                            "%.2f",
                            column.nullPercentage
                        )
                    }%"
                )

                when {

                    column.nullPercentage == 0.0 ->

                        println(
                            "    [OK] No NULL values"
                        )

                    column.nullPercentage < 20.0 ->

                        println(
                            "    [INFO] Some NULL values"
                        )

                    column.nullPercentage <= 50.0 ->

                        println(
                            "    [WARNING] High NULL percentage"
                        )

                    else ->

                        println(
                            "    [CRITICAL] Very high NULL percentage"
                        )
                }
            }

            println()


            // ====================================================
            // DISTINCT VALUE ANALYSIS
            // ====================================================

            println("------------------------------------------------------------")
            println("DISTINCT VALUE ANALYSIS")
            println("------------------------------------------------------------")

            for (column in table.columns) {

                println(
                    "  ${column.columnName}: " +
                        "${column.distinctCount} distinct value(s)"
                )

                when {

                    table.rowCount == 0L ->

                        println(
                            "    [INFO] Table contains no rows"
                        )

                    column.distinctCount ==
                        table.rowCount ->

                        println(
                            "    [INFO] All values are unique"
                        )

                    column.distinctCount == 1L ->

                        println(
                            "    [WARNING] All rows contain the same value"
                        )

                    else ->

                        println(
                            "    [INFO] Column contains repeated values"
                        )
                }
            }

            println()


            // ====================================================
            // DUPLICATE VALUE ANALYSIS
            // ====================================================

            println("------------------------------------------------------------")
            println("DUPLICATE VALUE ANALYSIS")
            println("------------------------------------------------------------")

            val foreignKeyColumns =
                table.foreignKeys
                    .map {
                        it.sourceColumnName
                    }
                    .toSet()

            for (column in table.columns) {

                val isPrimaryKey =
                    column.columnName in
                        table.primaryKeys

                val isUniqueColumn =
                    column.columnName in
                        table.uniqueColumns

                val isForeignKey =
                    column.columnName in
                        foreignKeyColumns

                println(
                    "  ${column.columnName}:"
                )

                when {

                    isPrimaryKey -> {

                        if (
                            column.duplicateValueCount == 0L
                        ) {

                            println(
                                "    [OK] No duplicate primary-key values"
                            )

                        } else {

                            println(
                                "    [CRITICAL] " +
                                    "${column.duplicateValueCount} duplicated " +
                                    "primary-key value(s) found"
                            )
                        }
                    }

                    isUniqueColumn -> {

                        if (
                            column.duplicateValueCount == 0L
                        ) {

                            println(
                                "    [OK] No duplicate values " +
                                    "(UNIQUE constraint)"
                            )

                        } else {

                            println(
                                "    [CRITICAL] " +
                                    "${column.duplicateValueCount} duplicated " +
                                    "value(s) found in a UNIQUE column"
                            )
                        }
                    }

                    isForeignKey -> {

                        if (
                            column.duplicateValueCount == 0L
                        ) {

                            println(
                                "    [INFO] No repeated foreign-key values"
                            )

                        } else {

                            println(
                                "    [INFO] " +
                                    "${column.duplicateValueCount} repeated " +
                                    "foreign-key value(s) found; " +
                                    "repetition is allowed"
                            )
                        }
                    }

                    else -> {

                        if (
                            column.duplicateValueCount == 0L
                        ) {

                            println(
                                "    [INFO] No repeated values"
                            )

                        } else {

                            println(
                                "    [INFO] " +
                                    "${column.duplicateValueCount} repeated " +
                                    "value(s) found"
                            )
                        }
                    }
                }
            }

            println()


            // ====================================================
            // POTENTIAL DUPLICATE RECORD ANALYSIS
            // ====================================================

            println("------------------------------------------------------------")
            println("POTENTIAL DUPLICATE RECORD ANALYSIS")
            println("------------------------------------------------------------")

            if (
                table.potentialDuplicateRecordCount == 0L
            ) {

                println(
                    "  [OK] No potential duplicate records found"
                )

            } else {

                println(
                    "  [WARNING] " +
                        "${table.potentialDuplicateRecordCount} " +
                        "potential duplicate record(s) found"
                )
            }

            println()


            // ====================================================
            // TEXT DATA QUALITY
            // ====================================================

            println("------------------------------------------------------------")
            println("TEXT DATA QUALITY")
            println("------------------------------------------------------------")

            val textColumns =
                table.columns.filter {
                    it.emptyStringCount != null
                }

            if (textColumns.isEmpty()) {

                println(
                    "  No text columns found"
                )

            } else {

                for (column in textColumns) {

                    println(
                        "  ${column.columnName}:"
                    )

                    if (
                        column.emptyStringCount == 0L
                    ) {

                        println(
                            "    [OK] No empty strings"
                        )

                    } else {

                        println(
                            "    [WARNING] " +
                                "${column.emptyStringCount} " +
                                "empty string value(s) found"
                        )
                    }

                    if (
                        column.whitespaceOnlyCount == 0L
                    ) {

                        println(
                            "    [OK] No whitespace-only values"
                        )

                    } else {

                        println(
                            "    [WARNING] " +
                                "${column.whitespaceOnlyCount} " +
                                "whitespace-only value(s) found"
                        )
                    }

                    if (
                        column.leadingTrailingWhitespaceCount == 0L
                    ) {

                        println(
                            "    [OK] No leading/trailing whitespace"
                        )

                    } else {

                        println(
                            "    [WARNING] " +
                                "${column.leadingTrailingWhitespaceCount} " +
                                "value(s) with leading/trailing whitespace found"
                        )
                    }
                }
            }

            println()


            // ====================================================
            // EMAIL FORMAT ANALYSIS
            // ====================================================

            println("------------------------------------------------------------")
            println("EMAIL FORMAT ANALYSIS")
            println("------------------------------------------------------------")

            val emailColumns =
                table.columns.filter {
                    it.invalidEmailCount != null
                }

            if (emailColumns.isEmpty()) {

                println(
                    "  No email columns found"
                )

            } else {

                for (column in emailColumns) {

                    println(
                        "  ${column.columnName}:"
                    )

                    if (
                        column.invalidEmailCount == 0L
                    ) {

                        println(
                            "    [OK] No invalid email formats"
                        )

                    } else {

                        println(
                            "    [WARNING] " +
                                "${column.invalidEmailCount} " +
                                "invalid email format(s) found"
                        )
                    }
                }
            }

            println()


            // ====================================================
            // NUMERIC ANALYSIS
            // ====================================================

            println("------------------------------------------------------------")
            println("NUMERIC ANALYSIS")
            println("------------------------------------------------------------")

            val numericColumns =
                table.columns.filter {
                    it.numericStatistics != null
                }

            if (numericColumns.isEmpty()) {

                println(
                    "  No numeric business columns found"
                )

            } else {

                for (column in numericColumns) {

                    val statistics =
                        column.numericStatistics!!

                    println(
                        "  ${column.columnName}:"
                    )

                    println(
                        "    Non-NULL Count: ${column.nonNullCount}"
                    )

                    println(
                        "    Distinct Count: ${column.distinctCount}"
                    )

                    println(
                        "    Minimum: ${statistics.minimum}"
                    )

                    println(
                        "    Maximum: ${statistics.maximum}"
                    )

                    println(
                        "    Average: ${statistics.average}"
                    )

                    println(
                        "    Total: ${statistics.total}"
                    )

                    if (
                        column.negativeValueCount == 0L
                    ) {

                        println(
                            "    [OK] No negative values"
                        )

                    } else {

                        println(
                            "    [WARNING] " +
                                "${column.negativeValueCount} " +
                                "negative value(s) found; " +
                                "review whether they are valid"
                        )
                    }

                    if (
                        column.potentialNumericOutlierCount == 0L
                    ) {

                        println(
                            "    [OK] No potential statistical outliers"
                        )

                    } else {

                        println(
                            "    [WARNING] " +
                                "${column.potentialNumericOutlierCount} " +
                                "potential statistical outlier(s) found"
                        )
                    }
                }
            }

            println()


            // ====================================================
            // DATE ANALYSIS
            // ====================================================

            println("------------------------------------------------------------")
            println("DATE ANALYSIS")
            println("------------------------------------------------------------")

            val dateColumns =
                table.columns.filter {
                    it.dateStatistics != null
                }

            if (dateColumns.isEmpty()) {

                println(
                    "  No date columns found"
                )

            } else {

                for (column in dateColumns) {

                    val statistics =
                        column.dateStatistics!!

                    println(
                        "  ${column.columnName}:"
                    )

                    println(
                        "    Earliest: ${statistics.earliest}"
                    )

                    println(
                        "    Latest: ${statistics.latest}"
                    )

                    if (
                        column.futureDateCount == 0L
                    ) {

                        println(
                            "    [OK] No future dates found"
                        )

                    } else {

                        println(
                            "    [WARNING] " +
                                "${column.futureDateCount} " +
                                "future date value(s) found; " +
                                "review whether they are valid"
                        )
                    }
                }
            }

            println()
        }


        // ========================================================
        // OVERALL DATABASE QUALITY REPORT
        // ========================================================

        val summary =
            report.summary

        println("============================================================")
        println("OVERALL DATABASE QUALITY REPORT")
        println("============================================================")
        println()

        println("DATABASE SUMMARY")
        println("------------------------------------------------------------")

        println(
            "Tables analysed: ${summary.tablesAnalysed}"
        )

        println(
            "Columns analysed: ${summary.columnsAnalysed}"
        )

        println(
            "Rows analysed: ${summary.rowsAnalysed}"
        )

        println()

        println("DATA QUALITY ISSUES")
        println("------------------------------------------------------------")

        println(
            "Primary Key Issues: " +
                summary.primaryKeyIssues
        )

        println(
            "Foreign Key Issues: " +
                summary.foreignKeyIssues
        )

        println(
            "Duplicate Value Issues: " +
                summary.duplicateValueIssues
        )

        println(
            "Potential Duplicate Record Issues: " +
                summary.potentialDuplicateRecordIssues
        )

        println(
            "Empty String Issues: " +
                summary.emptyStringIssues
        )

        println(
            "Whitespace-Only Issues: " +
                summary.whitespaceOnlyIssues
        )

        println(
            "Leading/Trailing Whitespace Issues: " +
                summary.leadingTrailingWhitespaceIssues
        )

        println(
            "Email Format Issues: " +
                summary.emailFormatIssues
        )

        println(
            "Numeric Anomaly Issues: " +
                summary.numericAnomalyIssues
        )

        println(
            "Potential Numeric Outlier Issues: " +
                summary.potentialNumericOutlierIssues
        )

        println(
            "Date Anomaly Issues: " +
                summary.dateAnomalyIssues
        )

        println(
            "Constant Value Issues: " +
                summary.constantValueIssues
        )

        println(
            "High NULL Percentage Issues: " +
                summary.highNullPercentageIssues
        )

        println()

        println("OVERALL SUMMARY")
        println("------------------------------------------------------------")

        println(
            "Total Issues: ${summary.totalIssues}"
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

        println(
            "Overall Status: $statusText"
        )

        println()


        // ========================================================
        // EXPORTED REPORTS
        // ========================================================

        println("EXPORTED REPORTS")
        println("------------------------------------------------------------")

        println(
            "JSON report: ${
                writtenJsonPath
                    .toAbsolutePath()
                    .normalize()
            }"
        )

        println(
            "HTML report: ${
                writtenHtmlPath
                    .toAbsolutePath()
                    .normalize()
            }"
        )

        println(
            "CSV report: ${
                writtenCsvPath
                    .toAbsolutePath()
                    .normalize()
            }"
        )

        println(
            "Application Catalog: ${
                writtenCatalogPath
                    .toAbsolutePath()
                    .normalize()
            }"
        )

        println()

        println("============================================================")
        println("DATABASE ANALYSIS COMPLETE")
        println("============================================================")
    }
}
