package com.alpinedigitalexperts.databaseanalyser

class DatabaseQualityReportBuilder(
    private val analyzer: DatabaseAnalyzer
) {

    private val textTypes =
        setOf(
            "character varying",
            "character",
            "text"
        )

    private val numericTypes =
        setOf(
            "smallint",
            "integer",
            "bigint",
            "decimal",
            "numeric",
            "real",
            "double precision",
            "smallserial",
            "serial",
            "bigserial",
            "int2",
            "int4",
            "int8",
            "float4",
            "float8"
        )

    private val dateTypes =
        setOf(
            "date",
            "timestamp",
            "timestamp without time zone",
            "timestamp with time zone"
        )


    // ============================================================
    // BUILD COMPLETE DATABASE QUALITY REPORT
    // ============================================================

    fun buildDatabaseReport(): DatabaseQualityReport {

        val discoveredTables =
            analyzer.discoverTables()

        val tableReports =
            discoveredTables.map { table ->

                buildTableReport(
                    schemaName = table.schemaName,
                    tableName = table.tableName
                )
            }

        val databaseName =
            discoveredTables
                .firstOrNull()
                ?.databaseName
                ?: "unknown"

        val summary =
            buildDatabaseSummary(
                tableReports
            )

        return DatabaseQualityReport(
            databaseName = databaseName,
            tables = tableReports,
            summary = summary
        )
    }


    // ============================================================
    // BUILD DATABASE QUALITY SUMMARY
    // ============================================================

    private fun buildDatabaseSummary(
        tables: List<TableQualityReport>
    ): DatabaseQualitySummary {

        val tablesAnalysed =
            tables.size

        val columnsAnalysed =
            tables.sumOf {
                it.columns.size
            }

        val rowsAnalysed =
            tables.sumOf {
                it.rowCount
            }


        // ========================================================
        // PRIMARY KEY ISSUES
        // ========================================================

        val primaryKeyIssues =
            tables.count {
                !it.hasPrimaryKey
            }.toLong()


        // ========================================================
        // FOREIGN KEY ISSUES
        // ========================================================

        val foreignKeyIssues =
            tables.sumOf { table ->

                table.foreignKeys.sumOf {
                    it.invalidReferenceCount
                }
            }


        // ========================================================
        // DUPLICATE VALUE ISSUES
        // ========================================================

        val duplicateValueIssues =
            tables.sumOf { table ->

                table.columns.sumOf { column ->

                    if (
                        column.columnName in table.primaryKeys ||
                        column.columnName in table.uniqueColumns
                    ) {

                        column.duplicateValueCount

                    } else {

                        0L
                    }
                }
            }


        // ========================================================
        // POTENTIAL DUPLICATE RECORD ISSUES
        // ========================================================

        val potentialDuplicateRecordIssues =
            tables.sumOf {
                it.potentialDuplicateRecordCount
            }


        // ========================================================
        // EMPTY STRING ISSUES
        // ========================================================

        val emptyStringIssues =
            tables.sumOf { table ->

                table.columns.sumOf { column ->

                    column.emptyStringCount
                        ?: 0L
                }
            }


        // ========================================================
        // WHITESPACE-ONLY ISSUES
        // ========================================================

        val whitespaceOnlyIssues =
            tables.sumOf { table ->

                table.columns.sumOf { column ->

                    column.whitespaceOnlyCount
                        ?: 0L
                }
            }


        // ========================================================
        // LEADING/TRAILING WHITESPACE ISSUES
        // ========================================================

        val leadingTrailingWhitespaceIssues =
            tables.sumOf { table ->

                table.columns.sumOf { column ->

                    column.leadingTrailingWhitespaceCount
                        ?: 0L
                }
            }


        // ========================================================
        // EMAIL FORMAT ISSUES
        // ========================================================

        val emailFormatIssues =
            tables.sumOf { table ->

                table.columns.sumOf { column ->

                    column.invalidEmailCount
                        ?: 0L
                }
            }


        // ========================================================
        // NUMERIC ANOMALY ISSUES
        // ========================================================

        val numericAnomalyIssues =
            tables.sumOf { table ->

                table.columns.sumOf { column ->

                    column.negativeValueCount
                        ?: 0L
                }
            }


        // ========================================================
        // POTENTIAL NUMERIC OUTLIER ISSUES
        // ========================================================

        val potentialNumericOutlierIssues =
            tables.sumOf { table ->

                table.columns.sumOf { column ->

                    column.potentialNumericOutlierCount
                        ?: 0L
                }
            }


        // ========================================================
        // DATE ANOMALY ISSUES
        // ========================================================

        val dateAnomalyIssues =
            tables.sumOf { table ->

                table.columns.sumOf { column ->

                    column.futureDateCount
                        ?: 0L
                }
            }


        // ========================================================
        // CONSTANT VALUE ISSUES
        // ========================================================

        val constantValueIssues =
            tables.sumOf { table ->

                val foreignKeyColumns =
                    table.foreignKeys
                        .map {
                            it.sourceColumnName
                        }
                        .toSet()

                table.columns.count { column ->

                    val isKeyColumn =
                        column.columnName in table.primaryKeys ||
                            column.columnName in table.uniqueColumns ||
                            column.columnName in foreignKeyColumns

                    table.rowCount > 1L &&
                        column.distinctCount == 1L &&
                        !isKeyColumn
                }.toLong()
            }


        // ========================================================
        // HIGH NULL PERCENTAGE ISSUES
        // ========================================================

        val highNullPercentageIssues =
            tables.sumOf { table ->

                table.columns.count { column ->

                    column.nullPercentage >= 20.0
                }.toLong()
            }


        // ========================================================
        // TOTAL ISSUES
        // ========================================================

        val totalIssues =
            primaryKeyIssues +
                foreignKeyIssues +
                duplicateValueIssues +
                potentialDuplicateRecordIssues +
                emptyStringIssues +
                whitespaceOnlyIssues +
                leadingTrailingWhitespaceIssues +
                emailFormatIssues +
                numericAnomalyIssues +
                potentialNumericOutlierIssues +
                dateAnomalyIssues +
                constantValueIssues +
                highNullPercentageIssues


        // ========================================================
        // OVERALL STATUS
        // ========================================================

        val overallStatus =
            when {

                totalIssues == 0L ->
                    DatabaseQualityStatus.GOOD

                totalIssues <= 5L ->
                    DatabaseQualityStatus.REVIEW

                else ->
                    DatabaseQualityStatus.ATTENTION_REQUIRED
            }


        return DatabaseQualitySummary(
            tablesAnalysed =
                tablesAnalysed,

            columnsAnalysed =
                columnsAnalysed,

            rowsAnalysed =
                rowsAnalysed,

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
    }


    // ============================================================
    // BUILD ONE TABLE QUALITY REPORT
    // ============================================================

    fun buildTableReport(
        schemaName: String,
        tableName: String
    ): TableQualityReport {

        val discoveredTable =
            analyzer
                .discoverTables()
                .single {
                    it.schemaName == schemaName &&
                        it.tableName == tableName
                }

        val databaseName =
            discoveredTable.databaseName

        val columns =
            analyzer.getColumns(
                schemaName = schemaName,
                tableName = tableName
            )

        val primaryKeys =
            analyzer.getPrimaryKeys(
                schemaName = schemaName,
                tableName = tableName
            )

        val uniqueColumns =
            analyzer.getUniqueColumns(
                schemaName = schemaName,
                tableName = tableName
            )

        val foreignKeys =
            analyzer.getForeignKeys(
                schemaName = schemaName,
                tableName = tableName
            )

        val rowCount =
            analyzer.getRowCount(
                schemaName = schemaName,
                tableName = tableName
            )

        val columnReports =
            columns.map { column ->

                buildColumnReport(
                    schemaName = schemaName,
                    tableName = tableName,
                    column = column
                )
            }

        val foreignKeyReports =
            foreignKeys.map { foreignKey ->

                ForeignKeyQualityReport(
                    foreignKeyName =
                        foreignKey.foreignKeyName,

                    sourceColumnName =
                        foreignKey.columnName,

                    referencedSchemaName =
                        foreignKey.referencedSchema,

                    referencedTableName =
                        foreignKey.referencedTable,

                    referencedColumnName =
                        foreignKey.referencedColumn,

                    invalidReferenceCount =
                        analyzer.getInvalidForeignKeyCount(
                            schemaName = schemaName,
                            tableName = tableName,
                            foreignKey = foreignKey
                        )
                )
            }

        val duplicateComparisonColumns =
            columns
                .map {
                    it.name
                }
                .filterNot {
                    it in primaryKeys ||
                        it in uniqueColumns
                }

        val potentialDuplicateRecordCount =
            if (
                duplicateComparisonColumns.size < 2
            ) {

                0L

            } else {

                analyzer.getPotentialDuplicateRecordCount(
                    schemaName = schemaName,
                    tableName = tableName,
                    comparisonColumns =
                        duplicateComparisonColumns
                )
            }

        return TableQualityReport(
            databaseName =
                databaseName,

            schemaName =
                schemaName,

            tableName =
                tableName,

            rowCount =
                rowCount,

            columns =
                columnReports,

            primaryKeys =
                primaryKeys,

            uniqueColumns =
                uniqueColumns,

            foreignKeys =
                foreignKeyReports,

            hasPrimaryKey =
                analyzer.hasPrimaryKey(
                    schemaName = schemaName,
                    tableName = tableName
                ),

            potentialDuplicateRecordCount =
                potentialDuplicateRecordCount
        )
    }


    // ============================================================
    // BUILD COLUMN QUALITY REPORT
    // ============================================================

    private fun buildColumnReport(
        schemaName: String,
        tableName: String,
        column: ColumnInfo
    ): ColumnQualityReport {

        val dataType =
            column.dataType.lowercase()

        val isText =
            dataType in textTypes

        val isNumeric =
            dataType in numericTypes

        val isDate =
            dataType in dateTypes

        val isEmail =
            column.name
                .lowercase()
                .contains("email")

        val isIdentifier =
            analyzer.isIdentifierColumn(
                schemaName = schemaName,
                tableName = tableName,
                columnName = column.name
            )


        // ========================================================
        // NULL AND NON-NULL ANALYSIS
        // ========================================================

        val nullCount =
            analyzer.getNullCount(
                schemaName = schemaName,
                tableName = tableName,
                columnName = column.name
            )

        val nonNullCount =
            analyzer.getNonNullValueCount(
                schemaName = schemaName,
                tableName = tableName,
                columnName = column.name
            )

        val nullPercentage =
            analyzer.getNullPercentage(
                schemaName = schemaName,
                tableName = tableName,
                columnName = column.name
            )


        // ========================================================
        // DISTINCT AND DUPLICATE ANALYSIS
        // ========================================================

        val distinctCount =
            analyzer.getDistinctCount(
                schemaName = schemaName,
                tableName = tableName,
                columnName = column.name
            )

        val duplicateValueCount =
            analyzer.getDuplicateValueCount(
                schemaName = schemaName,
                tableName = tableName,
                columnName = column.name
            )


        // ========================================================
        // EMPTY STRING ANALYSIS
        // ========================================================

        val emptyStringCount =
            if (isText) {

                analyzer.getEmptyStringCount(
                    schemaName = schemaName,
                    tableName = tableName,
                    columnName = column.name
                )

            } else {

                null
            }


        // ========================================================
        // WHITESPACE-ONLY ANALYSIS
        // ========================================================

        val whitespaceOnlyCount =
            if (isText) {

                analyzer.getWhitespaceOnlyValueCount(
                    schemaName = schemaName,
                    tableName = tableName,
                    columnName = column.name
                )

            } else {

                null
            }


        // ========================================================
        // LEADING/TRAILING WHITESPACE ANALYSIS
        // ========================================================

        val leadingTrailingWhitespaceCount =
            if (isText) {

                analyzer.getLeadingTrailingWhitespaceCount(
                    schemaName = schemaName,
                    tableName = tableName,
                    columnName = column.name
                )

            } else {

                null
            }


        // ========================================================
        // EMAIL ANALYSIS
        // ========================================================

        val invalidEmailCount =
            if (isText && isEmail) {

                analyzer.getInvalidEmailCount(
                    schemaName = schemaName,
                    tableName = tableName,
                    columnName = column.name
                )

            } else {

                null
            }


        // ========================================================
        // NUMERIC STATISTICS
        // ========================================================

        val numericStatistics =
            if (
                isNumeric &&
                !isIdentifier
            ) {

                val statistics =
                    analyzer.getNumericStatistics(
                        schemaName = schemaName,
                        tableName = tableName,
                        columnName = column.name
                    )

                NumericStatisticsReport(
                    minimum =
                        statistics.minimum
                            ?.toPlainString(),

                    maximum =
                        statistics.maximum
                            ?.toPlainString(),

                    average =
                        statistics.average
                            ?.toPlainString(),

                    total =
                        statistics.total
                            ?.toPlainString()
                )

            } else {

                null
            }


        // ========================================================
        // NEGATIVE VALUE ANALYSIS
        // ========================================================

        val negativeValueCount =
            if (
                isNumeric &&
                !isIdentifier
            ) {

                analyzer.getNegativeValueCount(
                    schemaName = schemaName,
                    tableName = tableName,
                    columnName = column.name
                )

            } else {

                null
            }


        // ========================================================
        // NUMERIC OUTLIER ANALYSIS
        // ========================================================

        val potentialNumericOutlierCount =
            if (
                isNumeric &&
                !isIdentifier
            ) {

                analyzer.getPotentialNumericOutlierCount(
                    schemaName = schemaName,
                    tableName = tableName,
                    columnName = column.name
                )

            } else {

                null
            }


        // ========================================================
        // DATE STATISTICS
        // ========================================================

        val dateStatistics =
            if (isDate) {

                val statistics =
                    analyzer.getDateStatistics(
                        schemaName = schemaName,
                        tableName = tableName,
                        columnName = column.name
                    )

                DateStatisticsReport(
                    earliest =
                        statistics.earliest
                            ?.toString(),

                    latest =
                        statistics.latest
                            ?.toString()
                )

            } else {

                null
            }


        // ========================================================
        // FUTURE DATE ANALYSIS
        // ========================================================

        val futureDateCount =
            if (isDate) {

                analyzer.getFutureDateCount(
                    schemaName = schemaName,
                    tableName = tableName,
                    columnName = column.name
                )

            } else {

                null
            }


        // ========================================================
        // BUILD COLUMN REPORT
        // ========================================================

        return ColumnQualityReport(
            columnName =
                column.name,

            dataType =
                column.dataType,

            nullable =
                column.nullable == "YES",

            defaultValue =
                column.defaultValue,

            nullCount =
                nullCount,

            nonNullCount =
                nonNullCount,

            nullPercentage =
                nullPercentage,

            distinctCount =
                distinctCount,

            duplicateValueCount =
                duplicateValueCount,

            emptyStringCount =
                emptyStringCount,

            whitespaceOnlyCount =
                whitespaceOnlyCount,

            leadingTrailingWhitespaceCount =
                leadingTrailingWhitespaceCount,

            invalidEmailCount =
                invalidEmailCount,

            negativeValueCount =
                negativeValueCount,

            potentialNumericOutlierCount =
                potentialNumericOutlierCount,

            futureDateCount =
                futureDateCount,

            numericStatistics =
                numericStatistics,

            dateStatistics =
                dateStatistics
        )
    }
}
