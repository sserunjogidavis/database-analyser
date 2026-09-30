package com.alpinedigitalexperts.databaseanalyser

import kotlinx.serialization.Serializable


@Serializable
data class DatabaseQualityReport(
    val databaseName: String,
    val tables: List<TableQualityReport>,
    val summary: DatabaseQualitySummary
)


@Serializable
data class DatabaseQualitySummary(
    val tablesAnalysed: Int,
    val columnsAnalysed: Int,
    val rowsAnalysed: Long,

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
    val highNullPercentageIssues: Long,

    val totalIssues: Long,
    val overallStatus: DatabaseQualityStatus
)


@Serializable
enum class DatabaseQualityStatus {
    GOOD,
    REVIEW,
    ATTENTION_REQUIRED
}


@Serializable
data class TableQualityReport(
    val databaseName: String,
    val schemaName: String,
    val tableName: String,

    val rowCount: Long,
    val columns: List<ColumnQualityReport>,

    val primaryKeys: List<String>,
    val uniqueColumns: List<String>,
    val foreignKeys: List<ForeignKeyQualityReport>,

    val hasPrimaryKey: Boolean,
    val potentialDuplicateRecordCount: Long,

    val groupedAggregates: List<GroupedNumericAggregateReport> =
        emptyList()
) {

    val qualifiedName: String
        get() =
            "$databaseName.$schemaName.$tableName"
}


@Serializable
data class ColumnQualityReport(
    val columnName: String,
    val dataType: String,
    val nullable: Boolean,
    val defaultValue: String?,

    val nullCount: Long,
    val nonNullCount: Long,
    val nullPercentage: Double,
    val distinctCount: Long,
    val duplicateValueCount: Long,

    val emptyStringCount: Long?,
    val whitespaceOnlyCount: Long?,
    val leadingTrailingWhitespaceCount: Long?,
    val invalidEmailCount: Long?,
    val negativeValueCount: Long?,
    val potentialNumericOutlierCount: Long?,
    val futureDateCount: Long?,

    val numericStatistics: NumericStatisticsReport?,
    val dateStatistics: DateStatisticsReport?
)


@Serializable
data class NumericStatisticsReport(
    val minimum: String?,
    val maximum: String?,
    val average: String?,
    val total: String?
)


@Serializable
data class DateStatisticsReport(
    val earliest: String?,
    val latest: String?
)


@Serializable
data class ForeignKeyQualityReport(
    val foreignKeyName: String?,
    val sourceColumnName: String,

    val referencedSchemaName: String,
    val referencedTableName: String,
    val referencedColumnName: String,

    val invalidReferenceCount: Long
) {

    val referencedQualifiedName: String
        get() =
            "$referencedSchemaName." +
                "$referencedTableName." +
                referencedColumnName
}


// ================================================================
// GROUPED AGGREGATE REPORT
// ================================================================

@Serializable
data class GroupedNumericAggregateReport(
    val groupByColumn: String,
    val numericColumn: String,
    val groups: List<GroupedNumericAggregateValueReport>
)


@Serializable
data class GroupedNumericAggregateValueReport(
    val groupValue: String?,
    val rowCount: Long,

    val minimum: String?,
    val maximum: String?,
    val average: String?,
    val total: String?
)
