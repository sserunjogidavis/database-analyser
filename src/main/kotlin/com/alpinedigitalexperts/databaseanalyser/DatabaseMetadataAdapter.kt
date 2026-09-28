package com.alpinedigitalexperts.databaseanalyser

interface DatabaseMetadataAdapter {

    fun getDatabaseName(): String

    fun discoverSchemas(): List<DiscoveredSchema>

    fun discoverTables(): List<DiscoveredTable>

    fun discoverViews(): List<DiscoveredView>

    fun discoverMaterializedViews(): List<DiscoveredView>

    fun discoverSequences(): List<DiscoveredSequence>

    fun discoverFunctions(): List<DiscoveredRoutine>

    fun discoverStoredProcedures(): List<DiscoveredRoutine>

    fun discoverColumns(
        schemaName: String,
        tableName: String
    ): List<DiscoveredColumn>

    fun discoverPrimaryKeys(
        schemaName: String,
        tableName: String
    ): List<DiscoveredKey>

    fun discoverUniqueKeys(
        schemaName: String,
        tableName: String
    ): List<DiscoveredKey>

    fun discoverForeignKeys(
        schemaName: String,
        tableName: String
    ): List<DiscoveredForeignKey>

    fun discoverIndexes(
        schemaName: String,
        tableName: String
    ): List<DiscoveredIndex>
}


data class DiscoveredSchema(
    val databaseName: String,
    val schemaName: String
) {
    val qualifiedName: String
        get() = "$databaseName.$schemaName"
}


data class DiscoveredTable(
    val databaseName: String,
    val schemaName: String,
    val tableName: String
) {
    val qualifiedName: String
        get() = "$databaseName.$schemaName.$tableName"
}


enum class DiscoveredViewKind {
    VIEW,
    MATERIALIZED_VIEW
}


data class DiscoveredView(
    val databaseName: String,
    val schemaName: String,
    val viewName: String,
    val kind: DiscoveredViewKind,
    val definition: String?
) {
    val qualifiedName: String
        get() = "$databaseName.$schemaName.$viewName"
}


data class DiscoveredSequence(
    val databaseName: String,
    val schemaName: String,
    val sequenceName: String,
    val dataType: String,
    val startValue: Long,
    val incrementBy: Long,
    val minimumValue: Long,
    val maximumValue: Long,
    val cacheSize: Long,
    val cycles: Boolean
) {
    val qualifiedName: String
        get() = "$databaseName.$schemaName.$sequenceName"
}


enum class DiscoveredRoutineKind {
    FUNCTION,
    STORED_PROCEDURE
}


data class DiscoveredRoutine(
    val databaseName: String,
    val schemaName: String,
    val routineName: String,
    val kind: DiscoveredRoutineKind,
    val language: String,
    val identityArguments: String,
    val resultType: String?
) {

    /**
     * PostgreSQL permits overloaded routines with the same name.
     * Including the identity argument types keeps the discovered
     * identity deterministic and unambiguous.
     */
    val qualifiedName: String
        get() =
            "$databaseName.$schemaName.$routineName($identityArguments)"
}


data class DiscoveredColumn(
    val databaseName: String,
    val schemaName: String,
    val tableName: String,
    val columnName: String,
    val dataType: String,
    val databaseVendorType: String,
    val nullable: Boolean,
    val defaultValue: String?,
    val ordinalPosition: Int
) {
    val qualifiedName: String
        get() = "$databaseName.$schemaName.$tableName.$columnName"
}


enum class DiscoveredKeyKind {
    PRIMARY_KEY,
    UNIQUE_KEY
}


data class DiscoveredKeyMember(
    val columnName: String,
    val memberOrder: Int
)


data class DiscoveredKey(
    val databaseName: String,
    val schemaName: String,
    val tableName: String,
    val keyName: String,
    val kind: DiscoveredKeyKind,
    val members: List<DiscoveredKeyMember>
) {
    val qualifiedName: String
        get() = "$databaseName.$schemaName.$tableName.$keyName"
}


data class DiscoveredForeignKeyMember(
    val sourceColumnName: String,
    val targetColumnName: String,
    val memberOrder: Int
)


data class DiscoveredForeignKey(
    val databaseName: String,
    val schemaName: String,
    val tableName: String,
    val foreignKeyName: String,
    val referencedSchemaName: String,
    val referencedTableName: String,
    val referencedKeyName: String?,
    val members: List<DiscoveredForeignKeyMember>
) {
    val qualifiedName: String
        get() = "$databaseName.$schemaName.$tableName.$foreignKeyName"

    val referencedTableQualifiedName: String
        get() = "$databaseName.$referencedSchemaName.$referencedTableName"
}


data class DiscoveredIndexMember(
    val columnName: String?,
    val memberDefinition: String,
    val memberOrder: Int,
    val isKeyMember: Boolean
)


data class DiscoveredIndex(
    val databaseName: String,
    val schemaName: String,
    val tableName: String,
    val indexName: String,
    val isUnique: Boolean,
    val isClustered: Boolean,
    val isPrimaryKeyBackingIndex: Boolean,
    val accessMethod: String,
    val predicate: String?,
    val members: List<DiscoveredIndexMember>
) {
    val qualifiedName: String
        get() = "$databaseName.$schemaName.$tableName.$indexName"
}
