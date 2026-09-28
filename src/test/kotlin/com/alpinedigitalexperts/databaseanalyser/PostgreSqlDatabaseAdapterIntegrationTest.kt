package com.alpinedigitalexperts.databaseanalyser

import java.sql.Connection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PostgreSqlDatabaseAdapterIntegrationTest {

    private fun withRollback(
        testBlock: (
            Connection,
            PostgreSqlDatabaseAdapter
        ) -> Unit
    ) {

        DatabaseConnector
            .connect()
            .use { connection ->

                connection.autoCommit =
                    false

                try {

                    val adapter =
                        PostgreSqlDatabaseAdapter(
                            connection
                        )

                    testBlock(
                        connection,
                        adapter
                    )

                } finally {

                    connection.rollback()
                }
            }
    }


    // ============================================================
    // DATABASE
    // ============================================================

    @Test
    fun discoversCurrentDatabase() {

        DatabaseConnector.connect().use { connection ->

            val adapter =
                PostgreSqlDatabaseAdapter(
                    connection
                )

            assertEquals(
                "companydb",
                adapter.getDatabaseName()
            )
        }
    }


    // ============================================================
    // SCHEMA
    // ============================================================

    @Test
    fun discoversPublicSchema() {

        DatabaseConnector.connect().use { connection ->

            val adapter =
                PostgreSqlDatabaseAdapter(
                    connection
                )

            assertTrue(
                adapter
                    .discoverSchemas()
                    .any {
                        it.schemaName ==
                            "public"
                    }
            )
        }
    }


    // ============================================================
    // TABLES
    // ============================================================

    @Test
    fun discoversExpectedTables() {

        DatabaseConnector.connect().use { connection ->

            val adapter =
                PostgreSqlDatabaseAdapter(
                    connection
                )

            val tables =
                adapter.discoverTables()

            assertTrue(
                tables.any {
                    it.schemaName ==
                        "public" &&
                        it.tableName ==
                        "employees"
                }
            )

            assertTrue(
                tables.any {
                    it.schemaName ==
                        "public" &&
                        it.tableName ==
                        "departments"
                }
            )
        }
    }


    @Test
    fun doesNotReturnSystemTables() {

        DatabaseConnector.connect().use { connection ->

            val adapter =
                PostgreSqlDatabaseAdapter(
                    connection
                )

            val tables =
                adapter.discoverTables()

            assertFalse(
                tables.any {
                    it.schemaName ==
                        "pg_catalog"
                }
            )

            assertFalse(
                tables.any {
                    it.schemaName ==
                        "information_schema"
                }
            )
        }
    }


    // ============================================================
    // VIEWS
    // ============================================================

    @Test
    fun discoversOrdinaryView() {

        withRollback {
                connection,
                adapter ->

            connection
                .createStatement()
                .use { statement ->

                    statement.execute(
                        """
                        CREATE VIEW employee_salary_view AS
                        SELECT
                            employee_id,
                            first_name,
                            salary
                        FROM employees
                        """.trimIndent()
                    )
                }

            val view =
                adapter
                    .discoverViews()
                    .single {
                        it.schemaName ==
                            "public" &&
                            it.viewName ==
                            "employee_salary_view"
                    }

            assertEquals(
                DiscoveredViewKind.VIEW,
                view.kind
            )

            assertEquals(
                "companydb.public.employee_salary_view",
                view.qualifiedName
            )

            assertNotNull(
                view.definition
            )
        }
    }


    @Test
    fun discoversMaterializedView() {

        withRollback {
                connection,
                adapter ->

            connection
                .createStatement()
                .use { statement ->

                    statement.execute(
                        """
                        CREATE MATERIALIZED VIEW department_employee_count_mv AS
                        SELECT
                            department_id,
                            COUNT(*) AS employee_count
                        FROM employees
                        GROUP BY department_id
                        """.trimIndent()
                    )
                }

            val view =
                adapter
                    .discoverMaterializedViews()
                    .single {
                        it.schemaName ==
                            "public" &&
                            it.viewName ==
                            "department_employee_count_mv"
                    }

            assertEquals(
                DiscoveredViewKind.MATERIALIZED_VIEW,
                view.kind
            )

            assertEquals(
                "companydb.public.department_employee_count_mv",
                view.qualifiedName
            )
        }
    }


    @Test
    fun systemViewsAreExcluded() {

        DatabaseConnector.connect().use { connection ->

            val adapter =
                PostgreSqlDatabaseAdapter(
                    connection
                )

            assertFalse(
                adapter
                    .discoverViews()
                    .any {
                        it.schemaName ==
                            "pg_catalog" ||
                            it.schemaName ==
                            "information_schema"
                    }
            )
        }
    }


    // ============================================================
    // SEQUENCES
    // ============================================================

    @Test
    fun discoversSequenceMetadata() {

        withRollback {
                connection,
                adapter ->

            connection
                .createStatement()
                .use { statement ->

                    statement.execute(
                        """
                        CREATE SEQUENCE adapter_sequence_test
                        AS BIGINT
                        START WITH 100
                        INCREMENT BY 5
                        MINVALUE 10
                        MAXVALUE 100000
                        CACHE 7
                        NO CYCLE
                        """.trimIndent()
                    )
                }

            val sequence =
                adapter
                    .discoverSequences()
                    .single {
                        it.schemaName ==
                            "public" &&
                            it.sequenceName ==
                            "adapter_sequence_test"
                    }

            assertEquals(
                "bigint",
                sequence.dataType
            )

            assertEquals(
                100L,
                sequence.startValue
            )

            assertEquals(
                5L,
                sequence.incrementBy
            )

            assertEquals(
                10L,
                sequence.minimumValue
            )

            assertEquals(
                100000L,
                sequence.maximumValue
            )

            assertEquals(
                7L,
                sequence.cacheSize
            )

            assertFalse(
                sequence.cycles
            )

            assertEquals(
                "companydb.public.adapter_sequence_test",
                sequence.qualifiedName
            )
        }
    }


    @Test
    fun systemSequencesAreExcluded() {

        DatabaseConnector.connect().use { connection ->

            val adapter =
                PostgreSqlDatabaseAdapter(
                    connection
                )

            val sequences =
                adapter.discoverSequences()

            assertFalse(
                sequences.any {
                    it.schemaName ==
                        "pg_catalog"
                }
            )

            assertFalse(
                sequences.any {
                    it.schemaName ==
                        "information_schema"
                }
            )
        }
    }


    // ============================================================
    // FUNCTIONS
    // ============================================================

    @Test
    fun discoversFunctionMetadata() {

        withRollback {
                connection,
                adapter ->

            connection
                .createStatement()
                .use { statement ->

                    statement.execute(
                        """
                        CREATE FUNCTION adapter_add_test(
                            left_value INTEGER,
                            right_value INTEGER
                        )
                        RETURNS INTEGER
                        LANGUAGE SQL
                        IMMUTABLE
                        AS ${'$'}function${'$'}
                            SELECT left_value + right_value
                        ${'$'}function${'$'}
                        """.trimIndent()
                    )
                }

            val function =
                adapter
                    .discoverFunctions()
                    .single {
                        it.schemaName ==
                            "public" &&
                            it.routineName ==
                            "adapter_add_test"
                    }

            assertEquals(
                DiscoveredRoutineKind.FUNCTION,
                function.kind
            )

            assertEquals(
                "sql",
                function.language
            )

            assertTrue(
                function.identityArguments
                    .contains(
                        "integer",
                        ignoreCase = true
                    )
            )

            assertEquals(
                "integer",
                function.resultType
            )

            assertTrue(
                function.qualifiedName
                    .startsWith(
                        "companydb.public.adapter_add_test("
                    )
            )
        }
    }


    // ============================================================
    // OVERLOADED FUNCTION IDENTITY
    // ============================================================

    @Test
    fun distinguishesOverloadedFunctionsByIdentityArguments() {

        withRollback {
                connection,
                adapter ->

            connection
                .createStatement()
                .use { statement ->

                    statement.execute(
                        """
                        CREATE FUNCTION adapter_overload_test(
                            input_value INTEGER
                        )
                        RETURNS INTEGER
                        LANGUAGE SQL
                        AS ${'$'}function${'$'}
                            SELECT input_value
                        ${'$'}function${'$'}
                        """.trimIndent()
                    )

                    statement.execute(
                        """
                        CREATE FUNCTION adapter_overload_test(
                            input_value TEXT
                        )
                        RETURNS TEXT
                        LANGUAGE SQL
                        AS ${'$'}function${'$'}
                            SELECT input_value
                        ${'$'}function${'$'}
                        """.trimIndent()
                    )
                }

            val functions =
                adapter
                    .discoverFunctions()
                    .filter {
                        it.schemaName ==
                            "public" &&
                            it.routineName ==
                            "adapter_overload_test"
                    }

            assertEquals(
                2,
                functions.size
            )

            assertEquals(
                2,
                functions
                    .map {
                        it.qualifiedName
                    }
                    .toSet()
                    .size
            )

            assertTrue(
                functions.any {
                    it.identityArguments
                        .contains(
                            "integer",
                            ignoreCase = true
                        )
                }
            )

            assertTrue(
                functions.any {
                    it.identityArguments
                        .contains(
                            "text",
                            ignoreCase = true
                        )
                }
            )
        }
    }


    // ============================================================
    // STORED PROCEDURES
    // ============================================================

    @Test
    fun discoversStoredProcedureMetadata() {

        withRollback {
                connection,
                adapter ->

            connection
                .createStatement()
                .use { statement ->

                    statement.execute(
                        """
                        CREATE PROCEDURE adapter_procedure_test(
                            input_value INTEGER
                        )
                        LANGUAGE SQL
                        AS ${'$'}procedure${'$'}
                            SELECT input_value
                        ${'$'}procedure${'$'}
                        """.trimIndent()
                    )
                }

            val procedure =
                adapter
                    .discoverStoredProcedures()
                    .single {
                        it.schemaName ==
                            "public" &&
                            it.routineName ==
                            "adapter_procedure_test"
                    }

            assertEquals(
                DiscoveredRoutineKind.STORED_PROCEDURE,
                procedure.kind
            )

            assertEquals(
                "sql",
                procedure.language
            )

            assertTrue(
                procedure.identityArguments
                    .contains(
                        "integer",
                        ignoreCase = true
                    )
            )

            assertTrue(
                procedure.qualifiedName
                    .startsWith(
                        "companydb.public.adapter_procedure_test("
                    )
            )
        }
    }


    // ============================================================
    // ROUTINE SYSTEM FILTERING
    // ============================================================

    @Test
    fun systemRoutinesAreExcluded() {

        DatabaseConnector.connect().use { connection ->

            val adapter =
                PostgreSqlDatabaseAdapter(
                    connection
                )

            val routines =
                adapter.discoverFunctions() +
                    adapter.discoverStoredProcedures()

            assertFalse(
                routines.any {
                    it.schemaName ==
                        "pg_catalog"
                }
            )

            assertFalse(
                routines.any {
                    it.schemaName ==
                        "information_schema"
                }
            )
        }
    }


    // ============================================================
    // COLUMNS
    // ============================================================

    @Test
    fun discoversEmployeeColumnsInStableOrder() {

        DatabaseConnector.connect().use { connection ->

            val adapter =
                PostgreSqlDatabaseAdapter(
                    connection
                )

            val names =
                adapter
                    .discoverColumns(
                        "public",
                        "employees"
                    )
                    .map {
                        it.columnName
                    }

            assertEquals(
                listOf(
                    "employee_id",
                    "first_name",
                    "last_name",
                    "email",
                    "salary",
                    "hire_date",
                    "department_id"
                ),
                names
            )
        }
    }


    @Test
    fun discoversSalaryMetadata() {

        DatabaseConnector.connect().use { connection ->

            val adapter =
                PostgreSqlDatabaseAdapter(
                    connection
                )

            val salary =
                adapter
                    .discoverColumns(
                        "public",
                        "employees"
                    )
                    .single {
                        it.columnName ==
                            "salary"
                    }

            assertEquals(
                "numeric",
                salary.dataType
            )

            assertEquals(
                "numeric",
                salary.databaseVendorType
            )

            assertTrue(
                salary.nullable
            )
        }
    }


    // ============================================================
    // PRIMARY KEY
    // ============================================================

    @Test
    fun discoversEmployeePrimaryKey() {

        DatabaseConnector.connect().use { connection ->

            val adapter =
                PostgreSqlDatabaseAdapter(
                    connection
                )

            val key =
                adapter
                    .discoverPrimaryKeys(
                        "public",
                        "employees"
                    )
                    .single()

            assertEquals(
                DiscoveredKeyKind.PRIMARY_KEY,
                key.kind
            )

            assertEquals(
                listOf(
                    DiscoveredKeyMember(
                        "employee_id",
                        0
                    )
                ),
                key.members
            )
        }
    }


    // ============================================================
    // UNIQUE KEY
    // ============================================================

    @Test
    fun discoversEmployeeEmailUniqueKey() {

        DatabaseConnector.connect().use { connection ->

            val adapter =
                PostgreSqlDatabaseAdapter(
                    connection
                )

            val key =
                adapter
                    .discoverUniqueKeys(
                        "public",
                        "employees"
                    )
                    .singleOrNull {
                        it.members.map { member ->
                            member.columnName
                        } == listOf("email")
                    }

            assertNotNull(
                key
            )
        }
    }


    // ============================================================
    // FOREIGN KEY
    // ============================================================

    @Test
    fun discoversEmployeeDepartmentForeignKey() {

        DatabaseConnector.connect().use { connection ->

            val adapter =
                PostgreSqlDatabaseAdapter(
                    connection
                )

            val key =
                adapter
                    .discoverForeignKeys(
                        "public",
                        "employees"
                    )
                    .single()

            assertEquals(
                "departments",
                key.referencedTableName
            )

            assertEquals(
                listOf(
                    DiscoveredForeignKeyMember(
                        "department_id",
                        "department_id",
                        0
                    )
                ),
                key.members
            )

            assertNotNull(
                key.referencedKeyName
            )
        }
    }


    // ============================================================
    // COMPOSITE PRIMARY KEY
    // ============================================================

    @Test
    fun discoversCompositePrimaryKeyInStableMemberOrder() {

        withRollback {
                connection,
                adapter ->

            connection
                .createStatement()
                .use { statement ->

                    statement.execute(
                        """
                        CREATE TABLE composite_primary_key_test (
                            tenant_id INTEGER NOT NULL,
                            record_id INTEGER NOT NULL,
                            CONSTRAINT composite_primary_key_test_pkey
                                PRIMARY KEY (
                                    tenant_id,
                                    record_id
                                )
                        )
                        """.trimIndent()
                    )
                }

            val key =
                adapter
                    .discoverPrimaryKeys(
                        "public",
                        "composite_primary_key_test"
                    )
                    .single()

            assertEquals(
                listOf(
                    DiscoveredKeyMember(
                        "tenant_id",
                        0
                    ),
                    DiscoveredKeyMember(
                        "record_id",
                        1
                    )
                ),
                key.members
            )
        }
    }


    // ============================================================
    // COMPOSITE UNIQUE KEY
    // ============================================================

    @Test
    fun discoversCompositeUniqueKeyInStableMemberOrder() {

        withRollback {
                connection,
                adapter ->

            connection
                .createStatement()
                .use { statement ->

                    statement.execute(
                        """
                        CREATE TABLE composite_unique_key_test (
                            id SERIAL PRIMARY KEY,
                            country_code TEXT NOT NULL,
                            external_number TEXT NOT NULL,
                            CONSTRAINT composite_unique_key_test_country_number_key
                                UNIQUE (
                                    country_code,
                                    external_number
                                )
                        )
                        """.trimIndent()
                    )
                }

            val key =
                adapter
                    .discoverUniqueKeys(
                        "public",
                        "composite_unique_key_test"
                    )
                    .single {
                        it.keyName ==
                            "composite_unique_key_test_country_number_key"
                    }

            assertEquals(
                listOf(
                    DiscoveredKeyMember(
                        "country_code",
                        0
                    ),
                    DiscoveredKeyMember(
                        "external_number",
                        1
                    )
                ),
                key.members
            )
        }
    }


    // ============================================================
    // COMPOSITE FOREIGN KEY
    // ============================================================

    @Test
    fun discoversCompositeForeignKeyWithStableSourceAndTargetOrder() {

        withRollback {
                connection,
                adapter ->

            connection
                .createStatement()
                .use { statement ->

                    statement.execute(
                        """
                        CREATE TABLE composite_parent_test (
                            tenant_id INTEGER NOT NULL,
                            parent_id INTEGER NOT NULL,
                            CONSTRAINT composite_parent_test_pkey
                                PRIMARY KEY (
                                    tenant_id,
                                    parent_id
                                )
                        )
                        """.trimIndent()
                    )

                    statement.execute(
                        """
                        CREATE TABLE composite_child_test (
                            child_id SERIAL PRIMARY KEY,
                            tenant_id INTEGER NOT NULL,
                            parent_id INTEGER NOT NULL,
                            CONSTRAINT composite_child_test_parent_fkey
                                FOREIGN KEY (
                                    tenant_id,
                                    parent_id
                                )
                                REFERENCES composite_parent_test (
                                    tenant_id,
                                    parent_id
                                )
                        )
                        """.trimIndent()
                    )
                }

            val key =
                adapter
                    .discoverForeignKeys(
                        "public",
                        "composite_child_test"
                    )
                    .single()

            assertEquals(
                "composite_parent_test_pkey",
                key.referencedKeyName
            )

            assertEquals(
                listOf(
                    DiscoveredForeignKeyMember(
                        "tenant_id",
                        "tenant_id",
                        0
                    ),
                    DiscoveredForeignKeyMember(
                        "parent_id",
                        "parent_id",
                        1
                    )
                ),
                key.members
            )
        }
    }


    // ============================================================
    // ORDINARY INDEX
    // ============================================================

    @Test
    fun discoversOrdinaryIndex() {

        withRollback {
                connection,
                adapter ->

            connection
                .createStatement()
                .use { statement ->

                    statement.execute(
                        """
                        CREATE TABLE ordinary_index_test (
                            id INTEGER PRIMARY KEY,
                            description TEXT
                        )
                        """.trimIndent()
                    )

                    statement.execute(
                        """
                        CREATE INDEX ordinary_index_test_description_idx
                        ON ordinary_index_test (
                            description
                        )
                        """.trimIndent()
                    )
                }

            val index =
                adapter
                    .discoverIndexes(
                        "public",
                        "ordinary_index_test"
                    )
                    .single {
                        it.indexName ==
                            "ordinary_index_test_description_idx"
                    }

            assertFalse(
                index.isUnique
            )

            assertEquals(
                "btree",
                index.accessMethod
            )

            assertNull(
                index.predicate
            )
        }
    }


    // ============================================================
    // COMPOSITE INDEX
    // ============================================================

    @Test
    fun discoversCompositeIndexInStableMemberOrder() {

        withRollback {
                connection,
                adapter ->

            connection
                .createStatement()
                .use { statement ->

                    statement.execute(
                        """
                        CREATE TABLE composite_index_test (
                            id INTEGER PRIMARY KEY,
                            tenant_id INTEGER,
                            record_id INTEGER
                        )
                        """.trimIndent()
                    )

                    statement.execute(
                        """
                        CREATE INDEX composite_index_test_tenant_record_idx
                        ON composite_index_test (
                            tenant_id,
                            record_id
                        )
                        """.trimIndent()
                    )
                }

            val index =
                adapter
                    .discoverIndexes(
                        "public",
                        "composite_index_test"
                    )
                    .single {
                        it.indexName ==
                            "composite_index_test_tenant_record_idx"
                    }

            assertEquals(
                listOf(
                    "tenant_id",
                    "record_id"
                ),
                index.members.map {
                    it.columnName
                }
            )

            assertEquals(
                listOf(
                    0,
                    1
                ),
                index.members.map {
                    it.memberOrder
                }
            )
        }
    }


    // ============================================================
    // INCLUDE INDEX
    // ============================================================

    @Test
    fun distinguishesIndexKeyMembersFromIncludedColumns() {

        withRollback {
                connection,
                adapter ->

            connection
                .createStatement()
                .use { statement ->

                    statement.execute(
                        """
                        CREATE TABLE included_column_index_test (
                            id INTEGER PRIMARY KEY,
                            lookup_code TEXT,
                            description TEXT
                        )
                        """.trimIndent()
                    )

                    statement.execute(
                        """
                        CREATE INDEX included_column_index_test_lookup_idx
                        ON included_column_index_test (
                            lookup_code
                        )
                        INCLUDE (
                            description
                        )
                        """.trimIndent()
                    )
                }

            val index =
                adapter
                    .discoverIndexes(
                        "public",
                        "included_column_index_test"
                    )
                    .single {
                        it.indexName ==
                            "included_column_index_test_lookup_idx"
                    }

            assertTrue(
                index.members[0].isKeyMember
            )

            assertFalse(
                index.members[1].isKeyMember
            )
        }
    }


    // ============================================================
    // EXPRESSION INDEX
    // ============================================================

    @Test
    fun preservesExpressionIndexDefinition() {

        withRollback {
                connection,
                adapter ->

            connection
                .createStatement()
                .use { statement ->

                    statement.execute(
                        """
                        CREATE TABLE expression_index_test (
                            id INTEGER PRIMARY KEY,
                            email TEXT
                        )
                        """.trimIndent()
                    )

                    statement.execute(
                        """
                        CREATE INDEX expression_index_test_lower_email_idx
                        ON expression_index_test (
                            lower(email)
                        )
                        """.trimIndent()
                    )
                }

            val member =
                adapter
                    .discoverIndexes(
                        "public",
                        "expression_index_test"
                    )
                    .single {
                        it.indexName ==
                            "expression_index_test_lower_email_idx"
                    }
                    .members
                    .single()

            assertNull(
                member.columnName
            )

            assertTrue(
                member.memberDefinition
                    .contains(
                        "lower",
                        ignoreCase = true
                    )
            )
        }
    }


    // ============================================================
    // PARTIAL INDEX
    // ============================================================

    @Test
    fun preservesPartialIndexPredicate() {

        withRollback {
                connection,
                adapter ->

            connection
                .createStatement()
                .use { statement ->

                    statement.execute(
                        """
                        CREATE TABLE partial_index_test (
                            id INTEGER PRIMARY KEY,
                            active BOOLEAN NOT NULL,
                            external_code TEXT
                        )
                        """.trimIndent()
                    )

                    statement.execute(
                        """
                        CREATE INDEX partial_index_test_active_code_idx
                        ON partial_index_test (
                            external_code
                        )
                        WHERE active = TRUE
                        """.trimIndent()
                    )
                }

            val index =
                adapter
                    .discoverIndexes(
                        "public",
                        "partial_index_test"
                    )
                    .single {
                        it.indexName ==
                            "partial_index_test_active_code_idx"
                    }

            assertNotNull(
                index.predicate
            )
        }
    }
}
