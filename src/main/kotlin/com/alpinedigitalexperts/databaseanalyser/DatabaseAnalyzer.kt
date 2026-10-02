package com.alpinedigitalexperts.databaseanalyser

import java.math.BigDecimal
import java.sql.Connection

class DatabaseAnalyzer(
    private val connection: Connection,
    private val metadataAdapter: DatabaseMetadataAdapter =
        PostgreSqlDatabaseAdapter(connection),
    private val qualityRules: QualityRules =
        QualityRules()
) {

    // ============================================================
    // 1. GET ALL TABLES
    // ============================================================

    fun getTables(): List<String> {

        return metadataAdapter
            .discoverTables()
            .filter {
                it.schemaName == "public"
            }
            .map {
                it.tableName
            }
    }


    fun getTables(
        schemaName: String
    ): List<String> {

        return metadataAdapter
            .discoverTables()
            .filter {
                it.schemaName == schemaName
            }
            .map {
                it.tableName
            }
    }


    // ============================================================
    // 2. GET COLUMNS
    // ============================================================

    fun getColumns(
        tableName: String
    ): List<ColumnInfo> {

        return getColumns(
            schemaName = "public",
            tableName = tableName
        )
    }


    fun getColumns(
        schemaName: String,
        tableName: String
    ): List<ColumnInfo> {

        return metadataAdapter
            .discoverColumns(
                schemaName = schemaName,
                tableName = tableName
            )
            .map { column ->

                ColumnInfo(
                    name = column.columnName,
                    dataType = column.dataType,
                    nullable =
                        if (column.nullable) {
                            "YES"
                        } else {
                            "NO"
                        },
                    defaultValue = column.defaultValue
                )
            }
    }


    // ============================================================
    // 3. GET PRIMARY KEYS
    // ============================================================

    fun getPrimaryKeys(
        tableName: String
    ): List<String> {

        return getPrimaryKeys(
            schemaName = "public",
            tableName = tableName
        )
    }


    fun getPrimaryKeys(
        schemaName: String,
        tableName: String
    ): List<String> {

        return metadataAdapter
            .discoverPrimaryKeys(
                schemaName = schemaName,
                tableName = tableName
            )
            .flatMap { key ->

                key.members
                    .sortedBy {
                        it.memberOrder
                    }
                    .map {
                        it.columnName
                    }
            }
    }


    // ============================================================
    // 4. GET UNIQUE COLUMNS
    // ============================================================

    fun getUniqueColumns(
        tableName: String
    ): List<String> {

        return getUniqueColumns(
            schemaName = "public",
            tableName = tableName
        )
    }


    fun getUniqueColumns(
        schemaName: String,
        tableName: String
    ): List<String> {

        return metadataAdapter
            .discoverUniqueKeys(
                schemaName = schemaName,
                tableName = tableName
            )
            .filter {
                it.members.size == 1
            }
            .map {
                it.members.first().columnName
            }
    }


    // ============================================================
    // 5. GET FOREIGN KEYS
    // ============================================================

    fun getForeignKeys(
        tableName: String
    ): List<ForeignKeyInfo> {

        return getForeignKeys(
            schemaName = "public",
            tableName = tableName
        )
    }


    fun getForeignKeys(
        schemaName: String,
        tableName: String
    ): List<ForeignKeyInfo> {

        return metadataAdapter
            .discoverForeignKeys(
                schemaName = schemaName,
                tableName = tableName
            )
            .flatMap { foreignKey ->

                foreignKey.members
                    .sortedBy {
                        it.memberOrder
                    }
                    .map { member ->

                        ForeignKeyInfo(
                            columnName =
                                member.sourceColumnName,
                            referencedTable =
                                foreignKey.referencedTableName,
                            referencedColumn =
                                member.targetColumnName,
                            schemaName =
                                schemaName,
                            referencedSchema =
                                foreignKey.referencedSchemaName,
                            foreignKeyName =
                                foreignKey.foreignKeyName
                        )
                    }
            }
    }


    // ============================================================
    // 6. GET INVALID FOREIGN KEY COUNT
    // ============================================================

    fun getInvalidForeignKeyCount(
        tableName: String,
        foreignKey: ForeignKeyInfo
    ): Long {

        return getInvalidForeignKeyCount(
            schemaName =
                foreignKey.schemaName,
            tableName =
                tableName,
            foreignKey =
                foreignKey
        )
    }


    fun getInvalidForeignKeyCount(
        schemaName: String,
        tableName: String,
        foreignKey: ForeignKeyInfo
    ): Long {

        val sourceTable =
            qualifiedTable(
                schemaName,
                tableName
            )

        val targetTable =
            qualifiedTable(
                foreignKey.referencedSchema,
                foreignKey.referencedTable
            )

        val sourceColumn =
            quoteIdentifier(
                foreignKey.columnName
            )

        val targetColumn =
            quoteIdentifier(
                foreignKey.referencedColumn
            )

        val sql = """
            SELECT COUNT(*)
            FROM $sourceTable child
            LEFT JOIN $targetTable parent
                ON child.$sourceColumn =
                   parent.$targetColumn
            WHERE child.$sourceColumn IS NOT NULL
              AND parent.$targetColumn IS NULL
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                if (resultSet.next()) {
                    return resultSet.getLong(1)
                }
            }
        }

        return 0
    }


    // ============================================================
    // 7. ASSESS FOREIGN KEY QUALITY
    // ============================================================

    fun assessForeignKeyQuality(
        tableName: String,
        foreignKey: ForeignKeyInfo
    ): String {

        val invalidReferences =
            getInvalidForeignKeyCount(
                tableName,
                foreignKey
            )

        return foreignKeyQualityMessage(
            invalidReferences
        )
    }


    fun assessForeignKeyQuality(
        schemaName: String,
        tableName: String,
        foreignKey: ForeignKeyInfo
    ): String {

        val invalidReferences =
            getInvalidForeignKeyCount(
                schemaName = schemaName,
                tableName = tableName,
                foreignKey = foreignKey
            )

        return foreignKeyQualityMessage(
            invalidReferences
        )
    }


    private fun foreignKeyQualityMessage(
        invalidReferences: Long
    ): String {

        return when {

            invalidReferences == 0L ->
                "[OK] All foreign-key values are valid"

            invalidReferences == 1L ->
                "[WARNING] 1 invalid foreign-key reference found"

            else ->
                "[WARNING] $invalidReferences invalid foreign-key references found"
        }
    }


    // ============================================================
    // 8. GET ROW COUNT
    // ============================================================

    fun getRowCount(
        tableName: String
    ): Long {

        return getRowCount(
            schemaName = "public",
            tableName = tableName
        )
    }


    fun getRowCount(
        schemaName: String,
        tableName: String
    ): Long {

        val sql = """
            SELECT COUNT(*)
            FROM ${qualifiedTable(schemaName, tableName)}
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                if (resultSet.next()) {
                    return resultSet.getLong(1)
                }
            }
        }

        return 0
    }


    // ============================================================
    // 9. GET NULL COUNT
    // ============================================================

    fun getNullCount(
        tableName: String,
        columnName: String
    ): Long {

        return getNullCount(
            schemaName = "public",
            tableName = tableName,
            columnName = columnName
        )
    }


    fun getNullCount(
        schemaName: String,
        tableName: String,
        columnName: String
    ): Long {

        val column =
            quoteIdentifier(
                columnName
            )

        val sql = """
            SELECT COUNT(*)
            FROM ${qualifiedTable(schemaName, tableName)}
            WHERE $column IS NULL
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                if (resultSet.next()) {
                    return resultSet.getLong(1)
                }
            }
        }

        return 0
    }


    // ============================================================
    // 10. GET DISTINCT COUNT
    // ============================================================

    fun getDistinctCount(
        tableName: String,
        columnName: String
    ): Long {

        return getDistinctCount(
            schemaName = "public",
            tableName = tableName,
            columnName = columnName
        )
    }


    fun getDistinctCount(
        schemaName: String,
        tableName: String,
        columnName: String
    ): Long {

        val column =
            quoteIdentifier(
                columnName
            )

        val sql = """
            SELECT COUNT(DISTINCT $column)
            FROM ${qualifiedTable(schemaName, tableName)}
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                if (resultSet.next()) {
                    return resultSet.getLong(1)
                }
            }
        }

        return 0
    }


    // ============================================================
    // 11. GET NULL PERCENTAGE
    // ============================================================

    fun getNullPercentage(
        tableName: String,
        columnName: String
    ): Double {

        return getNullPercentage(
            schemaName = "public",
            tableName = tableName,
            columnName = columnName
        )
    }


    fun getNullPercentage(
        schemaName: String,
        tableName: String,
        columnName: String
    ): Double {

        val rowCount =
            getRowCount(
                schemaName,
                tableName
            )

        if (rowCount == 0L) {
            return 0.0
        }

        val nullCount =
            getNullCount(
                schemaName,
                tableName,
                columnName
            )

        return (
            nullCount.toDouble() /
                rowCount.toDouble()
            ) * 100.0
    }


    // ============================================================
    // 12. ASSESS NULL QUALITY
    // ============================================================

    fun assessNullQuality(
        tableName: String,
        columnName: String
    ): String {

        return assessNullQuality(
            schemaName = "public",
            tableName = tableName,
            columnName = columnName
        )
    }


    fun assessNullQuality(
        schemaName: String,
        tableName: String,
        columnName: String
    ): String {

        val nullPercentage =
            getNullPercentage(
                schemaName,
                tableName,
                columnName
            )

        return when {

            nullPercentage == 0.0 ->
                "[OK] No NULL values"

            nullPercentage <
                qualityRules.highNullWarningThreshold ->
                "[INFO] Some NULL values"

            nullPercentage <=
                qualityRules.highNullCriticalThreshold ->
                "[WARNING] High NULL percentage"

            else ->
                "[CRITICAL] Very high NULL percentage"
        }
    }


    // ============================================================
    // 13. ASSESS UNIQUENESS
    // ============================================================

    fun assessUniqueness(
        tableName: String,
        columnName: String
    ): String {

        return assessUniqueness(
            schemaName = "public",
            tableName = tableName,
            columnName = columnName
        )
    }


    fun assessUniqueness(
        schemaName: String,
        tableName: String,
        columnName: String
    ): String {

        val rowCount =
            getRowCount(
                schemaName,
                tableName
            )

        val distinctCount =
            getDistinctCount(
                schemaName,
                tableName,
                columnName
            )

        if (rowCount == 0L) {
            return "[INFO] Table contains no rows"
        }

        return when {

            distinctCount == rowCount ->
                "[INFO] All values are unique"

            distinctCount == 1L ->
                "[WARNING] All rows contain the same value"

            else ->
                "[INFO] Column contains repeated values"
        }
    }


    // ============================================================
    // 14. GET NUMERIC STATISTICS
    // ============================================================

    fun getNumericStatistics(
        tableName: String,
        columnName: String
    ): NumericStatistics {

        return getNumericStatistics(
            schemaName = "public",
            tableName = tableName,
            columnName = columnName
        )
    }


    fun getNumericStatistics(
        schemaName: String,
        tableName: String,
        columnName: String
    ): NumericStatistics {

        val column =
            quoteIdentifier(
                columnName
            )

        val sql = """
            SELECT
                MIN($column) AS minimum,
                MAX($column) AS maximum,
                AVG($column) AS average,
                SUM($column) AS total
            FROM ${qualifiedTable(schemaName, tableName)}
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                if (resultSet.next()) {

                    return NumericStatistics(
                        minimum =
                            resultSet.getBigDecimal(
                                "minimum"
                            ),
                        maximum =
                            resultSet.getBigDecimal(
                                "maximum"
                            ),
                        average =
                            resultSet.getBigDecimal(
                                "average"
                            ),
                        total =
                            resultSet.getBigDecimal(
                                "total"
                            )
                    )
                }
            }
        }

        return NumericStatistics(
            minimum = null,
            maximum = null,
            average = null,
            total = null
        )
    }


    // ============================================================
    // 15. GET DATE STATISTICS
    // ============================================================

    fun getDateStatistics(
        tableName: String,
        columnName: String
    ): DateStatistics {

        return getDateStatistics(
            schemaName = "public",
            tableName = tableName,
            columnName = columnName
        )
    }


    fun getDateStatistics(
        schemaName: String,
        tableName: String,
        columnName: String
    ): DateStatistics {

        val column =
            quoteIdentifier(
                columnName
            )

        val sql = """
            SELECT
                MIN($column) AS earliest,
                MAX($column) AS latest
            FROM ${qualifiedTable(schemaName, tableName)}
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                if (resultSet.next()) {

                    return DateStatistics(
                        earliest =
                            resultSet.getDate(
                                "earliest"
                            ),
                        latest =
                            resultSet.getDate(
                                "latest"
                            )
                    )
                }
            }
        }

        return DateStatistics(
            earliest = null,
            latest = null
        )
    }


    // ============================================================
    // 16. CHECK IF COLUMN IS AN IDENTIFIER
    // ============================================================

    fun isIdentifierColumn(
        tableName: String,
        columnName: String
    ): Boolean {

        return isIdentifierColumn(
            schemaName = "public",
            tableName = tableName,
            columnName = columnName
        )
    }


    fun isIdentifierColumn(
        schemaName: String,
        tableName: String,
        columnName: String
    ): Boolean {

        val lowerColumnName =
            columnName.lowercase()

        if (
            lowerColumnName == "id" ||
            lowerColumnName.endsWith("_id") ||
            lowerColumnName.endsWith("id")
        ) {
            return true
        }

        val primaryKeys =
            getPrimaryKeys(
                schemaName,
                tableName
            )

        if (columnName in primaryKeys) {
            return true
        }

        val foreignKeys =
            getForeignKeys(
                schemaName,
                tableName
            )

        return foreignKeys.any {
            it.columnName == columnName
        }
    }


    // ============================================================
    // 17. CHECK IF TABLE HAS PRIMARY KEY
    // ============================================================

    fun hasPrimaryKey(
        tableName: String
    ): Boolean {

        return hasPrimaryKey(
            schemaName = "public",
            tableName = tableName
        )
    }


    fun hasPrimaryKey(
        schemaName: String,
        tableName: String
    ): Boolean {

        return getPrimaryKeys(
            schemaName,
            tableName
        ).isNotEmpty()
    }


    // ============================================================
    // 18. GET DUPLICATE VALUE COUNT
    // ============================================================

    fun getDuplicateValueCount(
        tableName: String,
        columnName: String
    ): Long {

        return getDuplicateValueCount(
            schemaName = "public",
            tableName = tableName,
            columnName = columnName
        )
    }


    fun getDuplicateValueCount(
        schemaName: String,
        tableName: String,
        columnName: String
    ): Long {

        val column =
            quoteIdentifier(
                columnName
            )

        val sql = """
            SELECT COUNT(*)
            FROM (
                SELECT $column
                FROM ${qualifiedTable(schemaName, tableName)}
                WHERE $column IS NOT NULL
                GROUP BY $column
                HAVING COUNT(*) > 1
            ) AS duplicate_values
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                if (resultSet.next()) {
                    return resultSet.getLong(1)
                }
            }
        }

        return 0
    }


    // ============================================================
    // 19. GET EMPTY STRING COUNT
    // ============================================================

    fun getEmptyStringCount(
        tableName: String,
        columnName: String
    ): Long {

        return getEmptyStringCount(
            schemaName = "public",
            tableName = tableName,
            columnName = columnName
        )
    }


    fun getEmptyStringCount(
        schemaName: String,
        tableName: String,
        columnName: String
    ): Long {

        val column =
            quoteIdentifier(
                columnName
            )

        val sql = """
            SELECT COUNT(*)
            FROM ${qualifiedTable(schemaName, tableName)}
            WHERE $column = ''
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                if (resultSet.next()) {
                    return resultSet.getLong(1)
                }
            }
        }

        return 0
    }


    // ============================================================
    // 20. GET INVALID EMAIL COUNT
    // ============================================================

    fun getInvalidEmailCount(
        tableName: String,
        columnName: String
    ): Long {

        return getInvalidEmailCount(
            schemaName = "public",
            tableName = tableName,
            columnName = columnName
        )
    }


    fun getInvalidEmailCount(
        schemaName: String,
        tableName: String,
        columnName: String
    ): Long {

        val column =
            quoteIdentifier(
                columnName
            )

        val sql = """
            SELECT COUNT(*)
            FROM ${qualifiedTable(schemaName, tableName)}
            WHERE $column IS NOT NULL
              AND $column <> ''
              AND $column !~
                  '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                if (resultSet.next()) {
                    return resultSet.getLong(1)
                }
            }
        }

        return 0
    }


    // ============================================================
    // 21. GET NEGATIVE VALUE COUNT
    // ============================================================

    fun getNegativeValueCount(
        tableName: String,
        columnName: String
    ): Long {

        return getNegativeValueCount(
            schemaName = "public",
            tableName = tableName,
            columnName = columnName
        )
    }


    fun getNegativeValueCount(
        schemaName: String,
        tableName: String,
        columnName: String
    ): Long {

        val column =
            quoteIdentifier(
                columnName
            )

        val sql = """
            SELECT COUNT(*)
            FROM ${qualifiedTable(schemaName, tableName)}
            WHERE $column < 0
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                if (resultSet.next()) {
                    return resultSet.getLong(1)
                }
            }
        }

        return 0
    }


    // ============================================================
    // 22. GET FUTURE DATE COUNT
    // ============================================================

    fun getFutureDateCount(
        tableName: String,
        columnName: String
    ): Long {

        return getFutureDateCount(
            schemaName = "public",
            tableName = tableName,
            columnName = columnName
        )
    }


    fun getFutureDateCount(
        schemaName: String,
        tableName: String,
        columnName: String
    ): Long {

        val column =
            quoteIdentifier(
                columnName
            )

        val sql = """
            SELECT COUNT(*)
            FROM ${qualifiedTable(schemaName, tableName)}
            WHERE $column > CURRENT_DATE
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                if (resultSet.next()) {
                    return resultSet.getLong(1)
                }
            }
        }

        return 0
    }


    // ============================================================
    // 23. GET POTENTIAL DUPLICATE RECORD COUNT
    // ============================================================

    fun getPotentialDuplicateRecordCount(
        tableName: String,
        comparisonColumns: List<String>
    ): Long {

        return getPotentialDuplicateRecordCount(
            schemaName = "public",
            tableName = tableName,
            comparisonColumns = comparisonColumns
        )
    }


    fun getPotentialDuplicateRecordCount(
        schemaName: String,
        tableName: String,
        comparisonColumns: List<String>
    ): Long {

        if (comparisonColumns.size < 2) {
            return 0
        }

        val columnList =
            comparisonColumns.joinToString(
                ", "
            ) {
                quoteIdentifier(it)
            }

        val sql = """
            SELECT COALESCE(
                SUM(group_count - 1),
                0
            )
            FROM (
                SELECT COUNT(*) AS group_count
                FROM ${qualifiedTable(schemaName, tableName)}
                GROUP BY $columnList
                HAVING COUNT(*) > 1
            ) AS duplicate_groups
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                if (resultSet.next()) {
                    return resultSet.getLong(1)
                }
            }
        }

        return 0
    }


    // ============================================================
    // 24. GET NON-NULL VALUE COUNT
    // ============================================================

    fun getNonNullValueCount(
        tableName: String,
        columnName: String
    ): Long {

        return getNonNullValueCount(
            schemaName = "public",
            tableName = tableName,
            columnName = columnName
        )
    }


    fun getNonNullValueCount(
        schemaName: String,
        tableName: String,
        columnName: String
    ): Long {

        val column =
            quoteIdentifier(
                columnName
            )

        val sql = """
            SELECT COUNT($column)
            FROM ${qualifiedTable(schemaName, tableName)}
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                if (resultSet.next()) {
                    return resultSet.getLong(1)
                }
            }
        }

        return 0
    }


    // ============================================================
    // 25. GET POTENTIAL NUMERIC OUTLIER COUNT
    // ============================================================

    fun getPotentialNumericOutlierCount(
        tableName: String,
        columnName: String
    ): Long {

        return getPotentialNumericOutlierCount(
            schemaName = "public",
            tableName = tableName,
            columnName = columnName
        )
    }


    fun getPotentialNumericOutlierCount(
        schemaName: String,
        tableName: String,
        columnName: String
    ): Long {

        val nonNullCount =
            getNonNullValueCount(
                schemaName,
                tableName,
                columnName
            )

        if (
            nonNullCount <
            qualityRules.minimumValuesForOutlierAnalysis
        ) {
            return 0
        }

        val column =
            quoteIdentifier(
                columnName
            )

        val table =
            qualifiedTable(
                schemaName,
                tableName
            )

        val sql = """
            WITH median_stats AS (
                SELECT
                    percentile_cont(0.5)
                    WITHIN GROUP (
                        ORDER BY $column
                    ) AS median_value
                FROM $table
                WHERE $column IS NOT NULL
            ),
            deviations AS (
                SELECT
                    ABS(
                        $column::double precision -
                        median_stats.median_value
                    ) AS deviation
                FROM $table
                CROSS JOIN median_stats
                WHERE $column IS NOT NULL
            ),
            mad_stats AS (
                SELECT
                    percentile_cont(0.5)
                    WITHIN GROUP (
                        ORDER BY deviation
                    ) AS mad_value
                FROM deviations
            )
            SELECT COUNT(*)
            FROM $table
            CROSS JOIN median_stats
            CROSS JOIN mad_stats
            WHERE $column IS NOT NULL
              AND mad_stats.mad_value > 0
              AND (
                  0.6745 *
                  ABS(
                      $column::double precision -
                      median_stats.median_value
                  ) /
                  mad_stats.mad_value
              ) > ${qualityRules.numericOutlierModifiedZScoreThreshold}
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                if (resultSet.next()) {
                    return resultSet.getLong(1)
                }
            }
        }

        return 0
    }


    // ============================================================
    // 26. GET WHITESPACE-ONLY VALUE COUNT
    // ============================================================

    fun getWhitespaceOnlyValueCount(
        tableName: String,
        columnName: String
    ): Long {

        return getWhitespaceOnlyValueCount(
            schemaName = "public",
            tableName = tableName,
            columnName = columnName
        )
    }


    fun getWhitespaceOnlyValueCount(
        schemaName: String,
        tableName: String,
        columnName: String
    ): Long {

        val column =
            quoteIdentifier(
                columnName
            )

        val sql = """
            SELECT COUNT(*)
            FROM ${qualifiedTable(schemaName, tableName)}
            WHERE $column IS NOT NULL
              AND $column <> ''
              AND BTRIM($column) = ''
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                if (resultSet.next()) {
                    return resultSet.getLong(1)
                }
            }
        }

        return 0
    }


    // ============================================================
    // 27. GET LEADING OR TRAILING WHITESPACE COUNT
    // ============================================================

    fun getLeadingTrailingWhitespaceCount(
        tableName: String,
        columnName: String
    ): Long {

        return getLeadingTrailingWhitespaceCount(
            schemaName = "public",
            tableName = tableName,
            columnName = columnName
        )
    }


    fun getLeadingTrailingWhitespaceCount(
        schemaName: String,
        tableName: String,
        columnName: String
    ): Long {

        val column =
            quoteIdentifier(
                columnName
            )

        val sql = """
            SELECT COUNT(*)
            FROM ${qualifiedTable(schemaName, tableName)}
            WHERE $column IS NOT NULL
              AND $column <> ''
              AND BTRIM($column) <> ''
              AND BTRIM($column) <> $column
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                if (resultSet.next()) {
                    return resultSet.getLong(1)
                }
            }
        }

        return 0
    }


    // ============================================================
    // 28. DISCOVER SCHEMAS
    // ============================================================

    fun discoverSchemas(): List<DiscoveredSchema> {

        return metadataAdapter
            .discoverSchemas()
    }


    // ============================================================
    // 29. DISCOVER TABLES
    // ============================================================

    fun discoverTables(): List<DiscoveredTable> {

        return metadataAdapter
            .discoverTables()
    }


    // ============================================================
    // 30. DISCOVER VIEWS
    // ============================================================

    fun discoverViews(): List<DiscoveredView> {

        return metadataAdapter
            .discoverViews()
    }


    // ============================================================
    // 31. DISCOVER MATERIALIZED VIEWS
    // ============================================================

    fun discoverMaterializedViews(): List<DiscoveredView> {

        return metadataAdapter
            .discoverMaterializedViews()
    }


    // ============================================================
    // 32. DISCOVER SEQUENCES
    // ============================================================

    fun discoverSequences(): List<DiscoveredSequence> {

        return metadataAdapter
            .discoverSequences()
    }


    // ============================================================
    // 33. DISCOVER FUNCTIONS
    // ============================================================

    fun discoverFunctions(): List<DiscoveredRoutine> {

        return metadataAdapter
            .discoverFunctions()
    }


    // ============================================================
    // 34. DISCOVER STORED PROCEDURES
    // ============================================================

    fun discoverStoredProcedures(): List<DiscoveredRoutine> {

        return metadataAdapter
            .discoverStoredProcedures()
    }


    // ============================================================
    // 35. DISCOVER COLUMNS
    // ============================================================

    fun discoverColumns(
        schemaName: String,
        tableName: String
    ): List<DiscoveredColumn> {

        return metadataAdapter
            .discoverColumns(
                schemaName = schemaName,
                tableName = tableName
            )
    }


    // ============================================================
    // 36. DISCOVER PRIMARY KEYS
    // ============================================================

    fun discoverPrimaryKeys(
        schemaName: String,
        tableName: String
    ): List<DiscoveredKey> {

        return metadataAdapter
            .discoverPrimaryKeys(
                schemaName = schemaName,
                tableName = tableName
            )
    }


    // ============================================================
    // 37. DISCOVER UNIQUE KEYS
    // ============================================================

    fun discoverUniqueKeys(
        schemaName: String,
        tableName: String
    ): List<DiscoveredKey> {

        return metadataAdapter
            .discoverUniqueKeys(
                schemaName = schemaName,
                tableName = tableName
            )
    }


    // ============================================================
    // 38. DISCOVER FOREIGN KEYS
    // ============================================================

    fun discoverForeignKeys(
        schemaName: String,
        tableName: String
    ): List<DiscoveredForeignKey> {

        return metadataAdapter
            .discoverForeignKeys(
                schemaName = schemaName,
                tableName = tableName
            )
    }


    // ============================================================
    // 39. DISCOVER INDEXES
    // ============================================================

    fun discoverIndexes(
        schemaName: String,
        tableName: String
    ): List<DiscoveredIndex> {

        return metadataAdapter
            .discoverIndexes(
                schemaName = schemaName,
                tableName = tableName
            )
    }


    // ============================================================
    // INTERNAL IDENTIFIER HELPERS
    // ============================================================

    private fun quoteIdentifier(
        identifier: String
    ): String {

        return "\"${
            identifier.replace(
                "\"",
                "\"\""
            )
        }\""
    }


    private fun qualifiedTable(
        schemaName: String,
        tableName: String
    ): String {

        return "${
            quoteIdentifier(schemaName)
        }.${
            quoteIdentifier(tableName)
        }"
    }
    // ============================================================
    // 40. GET GROUPED NUMERIC AGGREGATES
    // ============================================================

    fun getGroupedNumericAggregates(
        tableName: String,
        groupByColumn: String,
        numericColumn: String
    ): List<GroupedNumericAggregate> {

        return getGroupedNumericAggregates(
            schemaName = "public",
            tableName = tableName,
            groupByColumn = groupByColumn,
            numericColumn = numericColumn
        )
    }


    fun getGroupedNumericAggregates(
        schemaName: String,
        tableName: String,
        groupByColumn: String,
        numericColumn: String
    ): List<GroupedNumericAggregate> {

        val groupColumn =
            quoteIdentifier(
                groupByColumn
            )

        val valueColumn =
            quoteIdentifier(
                numericColumn
            )

        val sql = """
            SELECT
                $groupColumn AS group_value,
                COUNT(*) AS row_count,
                MIN($valueColumn) AS minimum,
                MAX($valueColumn) AS maximum,
                AVG($valueColumn) AS average,
                SUM($valueColumn) AS total
            FROM ${qualifiedTable(schemaName, tableName)}
            GROUP BY $groupColumn
            ORDER BY $groupColumn
        """.trimIndent()

        val results =
            mutableListOf<GroupedNumericAggregate>()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                while (resultSet.next()) {

                    results.add(
                        GroupedNumericAggregate(
                            groupValue =
                                resultSet.getObject(
                                    "group_value"
                                )
                                    ?.toString(),
                            rowCount =
                                resultSet.getLong(
                                    "row_count"
                                ),
                            minimum =
                                resultSet.getBigDecimal(
                                    "minimum"
                                ),
                            maximum =
                                resultSet.getBigDecimal(
                                    "maximum"
                                ),
                            average =
                                resultSet.getBigDecimal(
                                    "average"
                                ),
                            total =
                                resultSet.getBigDecimal(
                                    "total"
                                )
                        )
                    )
                }
            }
        }

        return results
    }


    // ============================================================
    // 41. DISCOVER DATABASE DEPENDENCIES
    // ============================================================

    fun discoverDependencies(): List<DiscoveredDependency> {

        return metadataAdapter
            .discoverDependencies()
    }


}


// ================================================================
// DATA CLASSES
// ================================================================

data class ColumnInfo(
    val name: String,
    val dataType: String,
    val nullable: String,
    val defaultValue: String?
)


data class ForeignKeyInfo(
    val columnName: String,
    val referencedTable: String,
    val referencedColumn: String,
    val schemaName: String = "public",
    val referencedSchema: String = "public",
    val foreignKeyName: String? = null
)


data class NumericStatistics(
    val minimum: BigDecimal?,
    val maximum: BigDecimal?,
    val average: BigDecimal?,
    val total: BigDecimal?
)


data class DateStatistics(
    val earliest: java.sql.Date?,
    val latest: java.sql.Date?
)


data class GroupedNumericAggregate(
    val groupValue: String?,
    val rowCount: Long,
    val minimum: BigDecimal?,
    val maximum: BigDecimal?,
    val average: BigDecimal?,
    val total: BigDecimal?
)
