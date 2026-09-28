package com.alpinedigitalexperts.databaseanalyser

data class QualityFinding(
    val severity: QualitySeverity,
    val category: String,
    val tableName: String,
    val columnName: String? = null,
    val message: String,
    val affectedRowCount: Long? = null
)

enum class QualitySeverity {
    INFO,
    WARNING,
    CRITICAL
}
