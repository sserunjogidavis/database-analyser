package com.alpinedigitalexperts.databaseanalyser

import java.sql.Connection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DatabaseAnalyzerMetadataAdapterTest {

    // ============================================================
    // TABLE DISCOVERY
    // ============================================================

    @Test
    fun `getTables uses metadata adapter`() {

        val adapter =
            FakeDatabaseMetadataAdapter(
                tables = listOf(
                    DiscoveredTable(
                        databaseName = "companydb",
                        schemaName = "public",
                        tableName = "departments"
                    ),
                    DiscoveredTable(
                        databaseName = "companydb",
                        schemaName = "public",
                        tableName = "employees"
                    )
                )
            )

        val analyzer =
            DatabaseAnalyzer(
                connection = fakeConnection(),
                metadataAdapter = adapter
            )

        val result =
            analyzer.getTables()

        assertEquals(
            listOf(
                "departments",
                "employees"
            ),
            result
        )

        assertTrue(
            adapter.discoverTablesCalled
        )
    }


    // ============================================================
    // COLUMN DISCOVERY
    // ============================================================

    @Test
    fun `getColumns uses metadata adapter`() {

        val adapter =
            FakeDatabaseMetadataAdapter(
                columns = listOf(
                    DiscoveredColumn(
                        databaseName = "companydb",
                        schemaName = "public",
                        tableName = "employees",
                        columnName = "employee_id",
                        dataType = "integer",
                        databaseVendorType = "int4",
                        nullable = false,
                        defaultValue =
                            "nextval('employees_employee_id_seq'::regclass)",
                        ordinalPosition = 1
                    ),
                    DiscoveredColumn(
                        databaseName = "companydb",
                        schemaName = "public",
                        tableName = "employees",
                        columnName = "email",
                        dataType = "character varying",
                        databaseVendorType = "varchar",
                        nullable = true,
                        defaultValue = null,
                        ordinalPosition = 2
                    )
                )
            )

        val analyzer =
            DatabaseAnalyzer(
                connection = fakeConnection(),
                metadataAdapter = adapter
            )

        val result =
            analyzer.getColumns(
                "employees"
            )

        assertEquals(
            2,
            result.size
        )

        assertEquals(
            "employee_id",
            result[0].name
        )

        assertEquals(
            "integer",
            result[0].dataType
        )

        assertEquals(
            "NO",
            result[0].nullable
        )

        assertEquals(
            "email",
            result[1].name
        )

        assertEquals(
            "YES",
            result[1].nullable
        )

        assertEquals(
            "public",
            adapter.lastSchemaName
        )

        assertEquals(
            "employees",
            adapter.lastTableName
        )
    }


    // ============================================================
    // PRIMARY KEY DISCOVERY
    // ============================================================

    @Test
    fun `getPrimaryKeys uses metadata adapter`() {

        val adapter =
            FakeDatabaseMetadataAdapter(
                primaryKeys = listOf(
                    DiscoveredKey(
                        databaseName = "companydb",
                        schemaName = "public",
                        tableName = "employees",
                        keyName = "employees_pkey",
                        kind =
                            DiscoveredKeyKind.PRIMARY_KEY,
                        members = listOf(
                            DiscoveredKeyMember(
                                columnName = "employee_id",
                                memberOrder = 0
                            )
                        )
                    )
                )
            )

        val analyzer =
            DatabaseAnalyzer(
                connection = fakeConnection(),
                metadataAdapter = adapter
            )

        val result =
            analyzer.getPrimaryKeys(
                "employees"
            )

        assertEquals(
            listOf(
                "employee_id"
            ),
            result
        )

        assertTrue(
            adapter.discoverPrimaryKeysCalled
        )
    }


    // ============================================================
    // COMPOSITE PRIMARY KEY ORDER
    // ============================================================

    @Test
    fun `getPrimaryKeys preserves composite key member order`() {

        val adapter =
            FakeDatabaseMetadataAdapter(
                primaryKeys = listOf(
                    DiscoveredKey(
                        databaseName = "companydb",
                        schemaName = "public",
                        tableName = "employee_projects",
                        keyName =
                            "employee_projects_pkey",
                        kind =
                            DiscoveredKeyKind.PRIMARY_KEY,
                        members = listOf(
                            DiscoveredKeyMember(
                                columnName =
                                    "project_id",
                                memberOrder = 1
                            ),
                            DiscoveredKeyMember(
                                columnName =
                                    "employee_id",
                                memberOrder = 0
                            )
                        )
                    )
                )
            )

        val analyzer =
            DatabaseAnalyzer(
                connection = fakeConnection(),
                metadataAdapter = adapter
            )

        val result =
            analyzer.getPrimaryKeys(
                "employee_projects"
            )

        assertEquals(
            listOf(
                "employee_id",
                "project_id"
            ),
            result
        )
    }


    // ============================================================
    // UNIQUE KEY DISCOVERY
    // ============================================================

    @Test
    fun `getUniqueColumns returns single column unique keys`() {

        val adapter =
            FakeDatabaseMetadataAdapter(
                uniqueKeys = listOf(
                    DiscoveredKey(
                        databaseName = "companydb",
                        schemaName = "public",
                        tableName = "employees",
                        keyName =
                            "employees_email_key",
                        kind =
                            DiscoveredKeyKind.UNIQUE_KEY,
                        members = listOf(
                            DiscoveredKeyMember(
                                columnName = "email",
                                memberOrder = 0
                            )
                        )
                    )
                )
            )

        val analyzer =
            DatabaseAnalyzer(
                connection = fakeConnection(),
                metadataAdapter = adapter
            )

        val result =
            analyzer.getUniqueColumns(
                "employees"
            )

        assertEquals(
            listOf(
                "email"
            ),
            result
        )

        assertTrue(
            adapter.discoverUniqueKeysCalled
        )
    }


    // ============================================================
    // COMPOSITE UNIQUE KEYS
    // ============================================================

    @Test
    fun `getUniqueColumns does not treat composite unique key as individually unique columns`() {

        val adapter =
            FakeDatabaseMetadataAdapter(
                uniqueKeys = listOf(
                    DiscoveredKey(
                        databaseName = "companydb",
                        schemaName = "public",
                        tableName = "employees",
                        keyName =
                            "employees_name_key",
                        kind =
                            DiscoveredKeyKind.UNIQUE_KEY,
                        members = listOf(
                            DiscoveredKeyMember(
                                columnName =
                                    "first_name",
                                memberOrder = 0
                            ),
                            DiscoveredKeyMember(
                                columnName =
                                    "last_name",
                                memberOrder = 1
                            )
                        )
                    )
                )
            )

        val analyzer =
            DatabaseAnalyzer(
                connection = fakeConnection(),
                metadataAdapter = adapter
            )

        val result =
            analyzer.getUniqueColumns(
                "employees"
            )

        assertTrue(
            result.isEmpty()
        )
    }


    // ============================================================
    // FOREIGN KEY DISCOVERY
    // ============================================================

    @Test
    fun `getForeignKeys uses metadata adapter`() {

        val adapter =
            FakeDatabaseMetadataAdapter(
                foreignKeys = listOf(
                    DiscoveredForeignKey(
                        databaseName = "companydb",
                        schemaName = "public",
                        tableName = "employees",
                        foreignKeyName =
                            "employees_department_id_fkey",
                        referencedSchemaName =
                            "public",
                        referencedTableName =
                            "departments",
                        referencedKeyName =
                            "departments_pkey",
                        members = listOf(
                            DiscoveredForeignKeyMember(
                                sourceColumnName =
                                    "department_id",
                                targetColumnName =
                                    "department_id",
                                memberOrder = 0
                            )
                        )
                    )
                )
            )

        val analyzer =
            DatabaseAnalyzer(
                connection = fakeConnection(),
                metadataAdapter = adapter
            )

        val result =
            analyzer.getForeignKeys(
                "employees"
            )

        assertEquals(
            1,
            result.size
        )

        val foreignKey =
            result.first()

        assertEquals(
            "department_id",
            foreignKey.columnName
        )

        assertEquals(
            "departments",
            foreignKey.referencedTable
        )

        assertEquals(
            "department_id",
            foreignKey.referencedColumn
        )

        assertEquals(
            "public",
            foreignKey.schemaName
        )

        assertEquals(
            "public",
            foreignKey.referencedSchema
        )

        assertEquals(
            "employees_department_id_fkey",
            foreignKey.foreignKeyName
        )

        assertTrue(
            adapter.discoverForeignKeysCalled
        )
    }


    // ============================================================
    // SCHEMA DISCOVERY
    // ============================================================

    @Test
    fun `discoverSchemas delegates to metadata adapter`() {

        val expected =
            listOf(
                DiscoveredSchema(
                    databaseName = "companydb",
                    schemaName = "public"
                ),
                DiscoveredSchema(
                    databaseName = "companydb",
                    schemaName = "reporting"
                )
            )

        val adapter =
            FakeDatabaseMetadataAdapter(
                schemas = expected
            )

        val analyzer =
            DatabaseAnalyzer(
                connection = fakeConnection(),
                metadataAdapter = adapter
            )

        assertEquals(
            expected,
            analyzer.discoverSchemas()
        )

        assertTrue(
            adapter.discoverSchemasCalled
        )
    }


    // ============================================================
    // VIEW DISCOVERY
    // ============================================================

    @Test
    fun `discoverViews delegates to metadata adapter`() {

        val expected =
            listOf(
                DiscoveredView(
                    databaseName = "companydb",
                    schemaName = "public",
                    viewName =
                        "employee_directory",
                    kind =
                        DiscoveredViewKind.VIEW,
                    definition =
                        "SELECT * FROM employees"
                )
            )

        val adapter =
            FakeDatabaseMetadataAdapter(
                views = expected
            )

        val analyzer =
            DatabaseAnalyzer(
                connection = fakeConnection(),
                metadataAdapter = adapter
            )

        assertEquals(
            expected,
            analyzer.discoverViews()
        )

        assertTrue(
            adapter.discoverViewsCalled
        )
    }


    // ============================================================
    // MATERIALIZED VIEW DISCOVERY
    // ============================================================

    @Test
    fun `discoverMaterializedViews delegates to metadata adapter`() {

        val expected =
            listOf(
                DiscoveredView(
                    databaseName = "companydb",
                    schemaName = "reporting",
                    viewName =
                        "employee_summary",
                    kind =
                        DiscoveredViewKind.MATERIALIZED_VIEW,
                    definition =
                        "SELECT * FROM employees"
                )
            )

        val adapter =
            FakeDatabaseMetadataAdapter(
                materializedViews = expected
            )

        val analyzer =
            DatabaseAnalyzer(
                connection = fakeConnection(),
                metadataAdapter = adapter
            )

        assertEquals(
            expected,
            analyzer.discoverMaterializedViews()
        )

        assertTrue(
            adapter.discoverMaterializedViewsCalled
        )
    }


    // ============================================================
    // SEQUENCE DISCOVERY
    // ============================================================

    @Test
    fun `discoverSequences delegates to metadata adapter`() {

        val expected =
            listOf(
                DiscoveredSequence(
                    databaseName = "companydb",
                    schemaName = "public",
                    sequenceName =
                        "employees_employee_id_seq",
                    dataType = "integer",
                    startValue = 1,
                    incrementBy = 1,
                    minimumValue = 1,
                    maximumValue =
                        2147483647,
                    cacheSize = 1,
                    cycles = false
                )
            )

        val adapter =
            FakeDatabaseMetadataAdapter(
                sequences = expected
            )

        val analyzer =
            DatabaseAnalyzer(
                connection = fakeConnection(),
                metadataAdapter = adapter
            )

        assertEquals(
            expected,
            analyzer.discoverSequences()
        )

        assertTrue(
            adapter.discoverSequencesCalled
        )
    }


    // ============================================================
    // FUNCTION DISCOVERY
    // ============================================================

    @Test
    fun `discoverFunctions delegates to metadata adapter`() {

        val expected =
            listOf(
                DiscoveredRoutine(
                    databaseName = "companydb",
                    schemaName = "public",
                    routineName =
                        "employee_count",
                    kind =
                        DiscoveredRoutineKind.FUNCTION,
                    language = "sql",
                    identityArguments = "",
                    resultType = "bigint"
                )
            )

        val adapter =
            FakeDatabaseMetadataAdapter(
                functions = expected
            )

        val analyzer =
            DatabaseAnalyzer(
                connection = fakeConnection(),
                metadataAdapter = adapter
            )

        assertEquals(
            expected,
            analyzer.discoverFunctions()
        )

        assertTrue(
            adapter.discoverFunctionsCalled
        )
    }


    // ============================================================
    // STORED PROCEDURE DISCOVERY
    // ============================================================

    @Test
    fun `discoverStoredProcedures delegates to metadata adapter`() {

        val expected =
            listOf(
                DiscoveredRoutine(
                    databaseName = "companydb",
                    schemaName = "public",
                    routineName =
                        "refresh_employee_data",
                    kind =
                        DiscoveredRoutineKind.STORED_PROCEDURE,
                    language = "plpgsql",
                    identityArguments = "",
                    resultType = null
                )
            )

        val adapter =
            FakeDatabaseMetadataAdapter(
                storedProcedures = expected
            )

        val analyzer =
            DatabaseAnalyzer(
                connection = fakeConnection(),
                metadataAdapter = adapter
            )

        assertEquals(
            expected,
            analyzer.discoverStoredProcedures()
        )

        assertTrue(
            adapter.discoverStoredProceduresCalled
        )
    }


    // ============================================================
    // INDEX DISCOVERY
    // ============================================================

    @Test
    fun `discoverIndexes delegates to metadata adapter`() {

        val expected =
            listOf(
                DiscoveredIndex(
                    databaseName = "companydb",
                    schemaName = "public",
                    tableName = "employees",
                    indexName =
                        "employees_email_key",
                    isUnique = true,
                    isClustered = false,
                    isPrimaryKeyBackingIndex =
                        false,
                    accessMethod = "btree",
                    predicate = null,
                    members = listOf(
                        DiscoveredIndexMember(
                            columnName = "email",
                            memberDefinition =
                                "email",
                            memberOrder = 0,
                            isKeyMember = true
                        )
                    )
                )
            )

        val adapter =
            FakeDatabaseMetadataAdapter(
                indexes = expected
            )

        val analyzer =
            DatabaseAnalyzer(
                connection = fakeConnection(),
                metadataAdapter = adapter
            )

        val result =
            analyzer.discoverIndexes(
                schemaName = "public",
                tableName = "employees"
            )

        assertEquals(
            expected,
            result
        )

        assertTrue(
            adapter.discoverIndexesCalled
        )

        assertEquals(
            "public",
            adapter.lastSchemaName
        )

        assertEquals(
            "employees",
            adapter.lastTableName
        )
    }


    // ============================================================
    // FAKE CONNECTION
    // ============================================================

    /*
     * These metadata tests do not execute JDBC queries.
     *
     * We use a dynamic proxy so that we do not need a mocking
     * dependency such as Mockito or MockK.
     */
    private fun fakeConnection(): Connection {

        return java.lang.reflect.Proxy.newProxyInstance(
            Connection::class.java.classLoader,
            arrayOf(
                Connection::class.java
            )
        ) { _, method, _ ->

            when (method.name) {

                "isClosed" ->
                    false

                "close" ->
                    null

                "toString" ->
                    "FakeConnection"

                else ->
                    throw UnsupportedOperationException(
                        "JDBC method ${
                            method.name
                        } should not be called by this metadata test."
                    )
            }
        } as Connection
    }


    // ============================================================
    // FAKE DATABASE METADATA ADAPTER
    // ============================================================

    private class FakeDatabaseMetadataAdapter(
        private val schemas:
        List<DiscoveredSchema> =
            emptyList(),

        private val tables:
        List<DiscoveredTable> =
            emptyList(),

        private val views:
        List<DiscoveredView> =
            emptyList(),

        private val materializedViews:
        List<DiscoveredView> =
            emptyList(),

        private val sequences:
        List<DiscoveredSequence> =
            emptyList(),

        private val functions:
        List<DiscoveredRoutine> =
            emptyList(),

        private val storedProcedures:
        List<DiscoveredRoutine> =
            emptyList(),

        private val columns:
        List<DiscoveredColumn> =
            emptyList(),

        private val primaryKeys:
        List<DiscoveredKey> =
            emptyList(),

        private val uniqueKeys:
        List<DiscoveredKey> =
            emptyList(),

        private val foreignKeys:
        List<DiscoveredForeignKey> =
            emptyList(),

        private val indexes:
        List<DiscoveredIndex> =
            emptyList()
    ) : DatabaseMetadataAdapter {

        var discoverSchemasCalled =
            false

        var discoverTablesCalled =
            false

        var discoverViewsCalled =
            false

        var discoverMaterializedViewsCalled =
            false

        var discoverSequencesCalled =
            false

        var discoverFunctionsCalled =
            false

        var discoverStoredProceduresCalled =
            false

        var discoverPrimaryKeysCalled =
            false

        var discoverUniqueKeysCalled =
            false

        var discoverForeignKeysCalled =
            false

        var discoverIndexesCalled =
            false

        var lastSchemaName:
            String? =
            null

        var lastTableName:
            String? =
            null


        override fun getDatabaseName(): String {

            return "companydb"
        }


        override fun discoverSchemas():
            List<DiscoveredSchema> {

            discoverSchemasCalled =
                true

            return schemas
        }


        override fun discoverTables():
            List<DiscoveredTable> {

            discoverTablesCalled =
                true

            return tables
        }


        override fun discoverViews():
            List<DiscoveredView> {

            discoverViewsCalled =
                true

            return views
        }


        override fun discoverMaterializedViews():
            List<DiscoveredView> {

            discoverMaterializedViewsCalled =
                true

            return materializedViews
        }


        override fun discoverSequences():
            List<DiscoveredSequence> {

            discoverSequencesCalled =
                true

            return sequences
        }


        override fun discoverFunctions():
            List<DiscoveredRoutine> {

            discoverFunctionsCalled =
                true

            return functions
        }


        override fun discoverStoredProcedures():
            List<DiscoveredRoutine> {

            discoverStoredProceduresCalled =
                true

            return storedProcedures
        }


        override fun discoverColumns(
            schemaName: String,
            tableName: String
        ): List<DiscoveredColumn> {

            lastSchemaName =
                schemaName

            lastTableName =
                tableName

            return columns
        }


        override fun discoverPrimaryKeys(
            schemaName: String,
            tableName: String
        ): List<DiscoveredKey> {

            discoverPrimaryKeysCalled =
                true

            lastSchemaName =
                schemaName

            lastTableName =
                tableName

            return primaryKeys
        }


        override fun discoverUniqueKeys(
            schemaName: String,
            tableName: String
        ): List<DiscoveredKey> {

            discoverUniqueKeysCalled =
                true

            lastSchemaName =
                schemaName

            lastTableName =
                tableName

            return uniqueKeys
        }


        override fun discoverForeignKeys(
            schemaName: String,
            tableName: String
        ): List<DiscoveredForeignKey> {

            discoverForeignKeysCalled =
                true

            lastSchemaName =
                schemaName

            lastTableName =
                tableName

            return foreignKeys
        }


        override fun discoverIndexes(
            schemaName: String,
            tableName: String
        ): List<DiscoveredIndex> {

            discoverIndexesCalled =
                true

            lastSchemaName =
                schemaName

            lastTableName =
                tableName

            return indexes
        }
    }
}
