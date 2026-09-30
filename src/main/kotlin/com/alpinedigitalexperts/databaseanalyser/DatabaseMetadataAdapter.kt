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

    /*
     * Dependency discovery has a default empty implementation.
     *
     * This keeps existing metadata adapters and test fakes compatible
     * while allowing adapters such as PostgreSqlDatabaseAdapter to
     * provide full dependency discovery.
     */
    fun discoverDependencies(): List<DiscoveredDependency> =
        emptyList()
}


// ============================================================
// SCHEMA
// ============================================================

data class DiscoveredSchema(
    val databaseName: String,
    val schemaName: String
) {

    val qualifiedName: String
        get() =
            "$databaseName.$schemaName"
}


// ============================================================
// TABLE
// ============================================================

data class DiscoveredTable(
    val databaseName: String,
    val schemaName: String,
    val tableName: String
) {

    val qualifiedName: String
        get() =
            "$databaseName.$schemaName.$tableName"
}


// ============================================================
// VIEW
// ============================================================

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
        get() =
            "$databaseName.$schemaName.$viewName"
}


// ============================================================
// SEQUENCE
// ============================================================

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
        get() =
            "$databaseName.$schemaName.$sequenceName"
}


// ============================================================
// ROUTINE
// ============================================================

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
     *
     * Including the identity argument types keeps the discovered
     * identity deterministic and unambiguous.
     */
    val qualifiedName: String
        get() =
            "$databaseName.$schemaName.$routineName($identityArguments)"
}


// ============================================================
// COLUMN
// ============================================================

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
        get() =
            "$databaseName.$schemaName.$tableName.$columnName"
}


// ============================================================
// KEYS
// ============================================================

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
        get() =
            "$databaseName.$schemaName.$tableName.$keyName"
}


// ============================================================
// FOREIGN KEYS
// ============================================================

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
        get() =
            "$databaseName.$schemaName.$tableName.$foreignKeyName"

    val referencedTableQualifiedName: String
        get() =
            "$databaseName.$referencedSchemaName.$referencedTableName"
}


// ============================================================
// INDEXES
// ============================================================

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
        get() =
            "$databaseName.$schemaName.$tableName.$indexName"
}


// ============================================================
// DEPENDENCY DISCOVERY
// ============================================================

enum class DiscoveredDependencyObjectKind {
    TABLE,
    VIEW,
    MATERIALIZED_VIEW,
    SEQUENCE,
    FUNCTION,
    STORED_PROCEDURE,
    COLUMN,
    UNKNOWN
}


enum class DiscoveredDependencyKind {
    FOREIGN_KEY,
    VIEW_REFERENCE,
    MATERIALIZED_VIEW_REFERENCE,
    ROUTINE_REFERENCE,
    SEQUENCE_REFERENCE,
    OTHER
}


data class DiscoveredDependency(

    val databaseName: String,

    // ========================================================
    // SOURCE OBJECT
    // ========================================================

    val sourceSchemaName: String,

    /*
     * Keep sourceObjectName as the physical database object name.
     *
     * Example:
     *
     * calculate_total
     *
     * Do not place "(integer)" or other routine arguments here.
     */
    val sourceObjectName: String,

    val sourceObjectKind:
    DiscoveredDependencyObjectKind,


    // ========================================================
    // TARGET OBJECT
    // ========================================================

    val targetSchemaName: String,

    val targetObjectName: String,

    val targetObjectKind:
    DiscoveredDependencyObjectKind,


    // ========================================================
    // DEPENDENCY TYPE
    // ========================================================

    val dependencyKind:
    DiscoveredDependencyKind,


    // ========================================================
    // OPTIONAL SUB-OBJECT MEMBERS
    // ========================================================

    val sourceSubObjectName: String? = null,

    val targetSubObjectName: String? = null,


    // ========================================================
    // ROUTINE IDENTITY
    // ========================================================

    /*
     * PostgreSQL permits overloaded functions and procedures.
     *
     * Keep the argument signature separate from the routine name.
     *
     * Examples:
     *
     * null
     * ""
     * "integer"
     * "integer, text"
     *
     * Default null values keep existing dependency constructors
     * and test fakes compatible.
     */
    val sourceIdentityArguments: String? = null,

    val targetIdentityArguments: String? = null,


    // ========================================================
    // OPTIONAL DATABASE DEPENDENCY NAME
    // ========================================================

    val dependencyName: String? = null

) {

    // ========================================================
    // GENERAL SOURCE QUALIFIED NAME
    // ========================================================

    /*
     * Keep the original behaviour.
     *
     * Existing tests currently expect routine dependencies such as:
     *
     * companydb.public.some_function
     *
     * rather than:
     *
     * companydb.public.some_function()
     */
    val sourceQualifiedName: String
        get() =
            buildString {

                append(databaseName)
                append(".")
                append(sourceSchemaName)
                append(".")
                append(sourceObjectName)

                if (
                    !sourceSubObjectName
                        .isNullOrBlank()
                ) {

                    append(".")
                    append(
                        sourceSubObjectName
                    )
                }
            }


    // ========================================================
    // GENERAL TARGET QUALIFIED NAME
    // ========================================================

    val targetQualifiedName: String
        get() =
            buildString {

                append(databaseName)
                append(".")
                append(targetSchemaName)
                append(".")
                append(targetObjectName)

                if (
                    !targetSubObjectName
                        .isNullOrBlank()
                ) {

                    append(".")
                    append(
                        targetSubObjectName
                    )
                }
            }


    // ========================================================
    // OVERLOAD-SAFE SOURCE ROUTINE QUALIFIED NAME
    // ========================================================

    val sourceRoutineQualifiedName: String?
        get() {

            if (
                sourceObjectKind !=
                DiscoveredDependencyObjectKind.FUNCTION &&
                sourceObjectKind !=
                DiscoveredDependencyObjectKind.STORED_PROCEDURE
            ) {

                return null
            }

            val arguments =
                sourceIdentityArguments
                    ?: return null

            return "$databaseName." +
                "$sourceSchemaName." +
                "$sourceObjectName($arguments)"
        }


    // ========================================================
    // OVERLOAD-SAFE TARGET ROUTINE QUALIFIED NAME
    // ========================================================

    val targetRoutineQualifiedName: String?
        get() {

            if (
                targetObjectKind !=
                DiscoveredDependencyObjectKind.FUNCTION &&
                targetObjectKind !=
                DiscoveredDependencyObjectKind.STORED_PROCEDURE
            ) {

                return null
            }

            val arguments =
                targetIdentityArguments
                    ?: return null

            return "$databaseName." +
                "$targetSchemaName." +
                "$targetObjectName($arguments)"
        }
}
