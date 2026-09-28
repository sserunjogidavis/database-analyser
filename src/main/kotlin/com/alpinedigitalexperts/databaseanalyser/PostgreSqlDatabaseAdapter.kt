package com.alpinedigitalexperts.databaseanalyser

import java.sql.Connection

class PostgreSqlDatabaseAdapter(
    private val connection: Connection
) : DatabaseMetadataAdapter {

    override fun getDatabaseName(): String {

        connection.prepareStatement(
            "SELECT current_database()"
        ).use { statement ->

            statement.executeQuery().use { resultSet ->

                if (resultSet.next()) {
                    return resultSet.getString(1)
                }
            }
        }

        error(
            "Unable to determine the current PostgreSQL database."
        )
    }


    // ============================================================
    // SCHEMAS
    // ============================================================

    override fun discoverSchemas(): List<DiscoveredSchema> {

        val databaseName =
            getDatabaseName()

        val schemas =
            mutableListOf<DiscoveredSchema>()

        val sql = """
            SELECT schema_name
            FROM information_schema.schemata
            WHERE schema_name NOT IN (
                'pg_catalog',
                'information_schema'
            )
            AND schema_name NOT LIKE 'pg_toast%'
            AND schema_name NOT LIKE 'pg_temp_%'
            ORDER BY schema_name
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                while (resultSet.next()) {

                    schemas.add(
                        DiscoveredSchema(
                            databaseName = databaseName,
                            schemaName =
                                resultSet.getString(
                                    "schema_name"
                                )
                        )
                    )
                }
            }
        }

        return schemas
    }


    // ============================================================
    // TABLES
    // ============================================================

    override fun discoverTables(): List<DiscoveredTable> {

        val databaseName =
            getDatabaseName()

        val tables =
            mutableListOf<DiscoveredTable>()

        val sql = """
            SELECT
                table_schema,
                table_name
            FROM information_schema.tables
            WHERE table_type = 'BASE TABLE'
            AND table_schema NOT IN (
                'pg_catalog',
                'information_schema'
            )
            AND table_schema NOT LIKE 'pg_toast%'
            AND table_schema NOT LIKE 'pg_temp_%'
            ORDER BY
                table_schema,
                table_name
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                while (resultSet.next()) {

                    tables.add(
                        DiscoveredTable(
                            databaseName = databaseName,
                            schemaName =
                                resultSet.getString(
                                    "table_schema"
                                ),
                            tableName =
                                resultSet.getString(
                                    "table_name"
                                )
                        )
                    )
                }
            }
        }

        return tables
    }


    // ============================================================
    // VIEWS
    // ============================================================

    override fun discoverViews(): List<DiscoveredView> {

        val databaseName =
            getDatabaseName()

        val views =
            mutableListOf<DiscoveredView>()

        val sql = """
            SELECT
                schemaname,
                viewname,
                definition
            FROM pg_views
            WHERE schemaname NOT IN (
                'pg_catalog',
                'information_schema'
            )
            AND schemaname NOT LIKE 'pg_toast%'
            AND schemaname NOT LIKE 'pg_temp_%'
            ORDER BY
                schemaname,
                viewname
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                while (resultSet.next()) {

                    views.add(
                        DiscoveredView(
                            databaseName = databaseName,
                            schemaName =
                                resultSet.getString(
                                    "schemaname"
                                ),
                            viewName =
                                resultSet.getString(
                                    "viewname"
                                ),
                            kind =
                                DiscoveredViewKind.VIEW,
                            definition =
                                resultSet.getString(
                                    "definition"
                                )
                        )
                    )
                }
            }
        }

        return views
    }


    // ============================================================
    // MATERIALIZED VIEWS
    // ============================================================

    override fun discoverMaterializedViews(): List<DiscoveredView> {

        val databaseName =
            getDatabaseName()

        val views =
            mutableListOf<DiscoveredView>()

        val sql = """
            SELECT
                schemaname,
                matviewname,
                definition
            FROM pg_matviews
            WHERE schemaname NOT IN (
                'pg_catalog',
                'information_schema'
            )
            AND schemaname NOT LIKE 'pg_toast%'
            AND schemaname NOT LIKE 'pg_temp_%'
            ORDER BY
                schemaname,
                matviewname
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                while (resultSet.next()) {

                    views.add(
                        DiscoveredView(
                            databaseName = databaseName,
                            schemaName =
                                resultSet.getString(
                                    "schemaname"
                                ),
                            viewName =
                                resultSet.getString(
                                    "matviewname"
                                ),
                            kind =
                                DiscoveredViewKind.MATERIALIZED_VIEW,
                            definition =
                                resultSet.getString(
                                    "definition"
                                )
                        )
                    )
                }
            }
        }

        return views
    }


    // ============================================================
    // SEQUENCES
    // ============================================================

    override fun discoverSequences(): List<DiscoveredSequence> {

        val databaseName =
            getDatabaseName()

        val sequences =
            mutableListOf<DiscoveredSequence>()

        val sql = """
            SELECT
                namespace_entry.nspname
                    AS schema_name,

                sequence_entry.relname
                    AS sequence_name,

                format_type(
                    sequence_info.seqtypid,
                    NULL
                ) AS data_type,

                sequence_info.seqstart
                    AS start_value,

                sequence_info.seqincrement
                    AS increment_by,

                sequence_info.seqmin
                    AS minimum_value,

                sequence_info.seqmax
                    AS maximum_value,

                sequence_info.seqcache
                    AS cache_size,

                sequence_info.seqcycle
                    AS cycles

            FROM pg_class sequence_entry

            JOIN pg_namespace namespace_entry
                ON namespace_entry.oid =
                   sequence_entry.relnamespace

            JOIN pg_sequence sequence_info
                ON sequence_info.seqrelid =
                   sequence_entry.oid

            WHERE sequence_entry.relkind = 'S'

            AND namespace_entry.nspname NOT IN (
                'pg_catalog',
                'information_schema'
            )

            AND namespace_entry.nspname
                NOT LIKE 'pg_toast%'

            AND namespace_entry.nspname
                NOT LIKE 'pg_temp_%'

            ORDER BY
                namespace_entry.nspname,
                sequence_entry.relname
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { resultSet ->

                while (resultSet.next()) {

                    sequences.add(
                        DiscoveredSequence(
                            databaseName =
                                databaseName,
                            schemaName =
                                resultSet.getString(
                                    "schema_name"
                                ),
                            sequenceName =
                                resultSet.getString(
                                    "sequence_name"
                                ),
                            dataType =
                                resultSet.getString(
                                    "data_type"
                                ),
                            startValue =
                                resultSet.getLong(
                                    "start_value"
                                ),
                            incrementBy =
                                resultSet.getLong(
                                    "increment_by"
                                ),
                            minimumValue =
                                resultSet.getLong(
                                    "minimum_value"
                                ),
                            maximumValue =
                                resultSet.getLong(
                                    "maximum_value"
                                ),
                            cacheSize =
                                resultSet.getLong(
                                    "cache_size"
                                ),
                            cycles =
                                resultSet.getBoolean(
                                    "cycles"
                                )
                        )
                    )
                }
            }
        }

        return sequences
    }


    // ============================================================
    // FUNCTIONS
    // ============================================================

    override fun discoverFunctions(): List<DiscoveredRoutine> {

        return discoverRoutines(
            postgresRoutineKind = "f",
            discoveredKind =
                DiscoveredRoutineKind.FUNCTION
        )
    }


    // ============================================================
    // STORED PROCEDURES
    // ============================================================

    override fun discoverStoredProcedures(): List<DiscoveredRoutine> {

        return discoverRoutines(
            postgresRoutineKind = "p",
            discoveredKind =
                DiscoveredRoutineKind.STORED_PROCEDURE
        )
    }


    // ============================================================
    // SHARED ROUTINE DISCOVERY
    // ============================================================

    private fun discoverRoutines(
        postgresRoutineKind: String,
        discoveredKind: DiscoveredRoutineKind
    ): List<DiscoveredRoutine> {

        val databaseName =
            getDatabaseName()

        val routines =
            mutableListOf<DiscoveredRoutine>()

        val sql = """
            SELECT
                namespace_entry.nspname
                    AS schema_name,

                routine_entry.proname
                    AS routine_name,

                language_entry.lanname
                    AS routine_language,

                pg_get_function_identity_arguments(
                    routine_entry.oid
                ) AS identity_arguments,

                pg_get_function_result(
                    routine_entry.oid
                ) AS result_type

            FROM pg_proc routine_entry

            JOIN pg_namespace namespace_entry
                ON namespace_entry.oid =
                   routine_entry.pronamespace

            JOIN pg_language language_entry
                ON language_entry.oid =
                   routine_entry.prolang

            WHERE routine_entry.prokind = ?

            AND namespace_entry.nspname NOT IN (
                'pg_catalog',
                'information_schema'
            )

            AND namespace_entry.nspname
                NOT LIKE 'pg_toast%'

            AND namespace_entry.nspname
                NOT LIKE 'pg_temp_%'

            ORDER BY
                namespace_entry.nspname,
                routine_entry.proname,
                pg_get_function_identity_arguments(
                    routine_entry.oid
                )
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.setString(
                1,
                postgresRoutineKind
            )

            statement.executeQuery().use { resultSet ->

                while (resultSet.next()) {

                    routines.add(
                        DiscoveredRoutine(
                            databaseName =
                                databaseName,
                            schemaName =
                                resultSet.getString(
                                    "schema_name"
                                ),
                            routineName =
                                resultSet.getString(
                                    "routine_name"
                                ),
                            kind =
                                discoveredKind,
                            language =
                                resultSet.getString(
                                    "routine_language"
                                ),
                            identityArguments =
                                resultSet.getString(
                                    "identity_arguments"
                                ),
                            resultType =
                                resultSet.getString(
                                    "result_type"
                                )
                        )
                    )
                }
            }
        }

        return routines
    }


    // ============================================================
    // COLUMNS
    // ============================================================

    override fun discoverColumns(
        schemaName: String,
        tableName: String
    ): List<DiscoveredColumn> {

        val databaseName =
            getDatabaseName()

        val columns =
            mutableListOf<DiscoveredColumn>()

        val sql = """
            SELECT
                column_name,
                data_type,
                udt_name,
                is_nullable,
                column_default,
                ordinal_position
            FROM information_schema.columns
            WHERE table_schema = ?
            AND table_name = ?
            ORDER BY ordinal_position
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.setString(
                1,
                schemaName
            )

            statement.setString(
                2,
                tableName
            )

            statement.executeQuery().use { resultSet ->

                while (resultSet.next()) {

                    columns.add(
                        DiscoveredColumn(
                            databaseName =
                                databaseName,
                            schemaName =
                                schemaName,
                            tableName =
                                tableName,
                            columnName =
                                resultSet.getString(
                                    "column_name"
                                ),
                            dataType =
                                resultSet.getString(
                                    "data_type"
                                ),
                            databaseVendorType =
                                resultSet.getString(
                                    "udt_name"
                                ),
                            nullable =
                                resultSet.getString(
                                    "is_nullable"
                                ) == "YES",
                            defaultValue =
                                resultSet.getString(
                                    "column_default"
                                ),
                            ordinalPosition =
                                resultSet.getInt(
                                    "ordinal_position"
                                )
                        )
                    )
                }
            }
        }

        return columns
    }


    // ============================================================
    // PRIMARY KEYS
    // ============================================================

    override fun discoverPrimaryKeys(
        schemaName: String,
        tableName: String
    ): List<DiscoveredKey> {

        return discoverKeys(
            schemaName =
                schemaName,
            tableName =
                tableName,
            postgresConstraintType =
                "p",
            keyKind =
                DiscoveredKeyKind.PRIMARY_KEY
        )
    }


    // ============================================================
    // UNIQUE KEYS
    // ============================================================

    override fun discoverUniqueKeys(
        schemaName: String,
        tableName: String
    ): List<DiscoveredKey> {

        return discoverKeys(
            schemaName =
                schemaName,
            tableName =
                tableName,
            postgresConstraintType =
                "u",
            keyKind =
                DiscoveredKeyKind.UNIQUE_KEY
        )
    }


    // ============================================================
    // SHARED KEY DISCOVERY
    // ============================================================

    private fun discoverKeys(
        schemaName: String,
        tableName: String,
        postgresConstraintType: String,
        keyKind: DiscoveredKeyKind
    ): List<DiscoveredKey> {

        val databaseName =
            getDatabaseName()

        val keyMembers =
            linkedMapOf<
                String,
                MutableList<DiscoveredKeyMember>
                >()

        val sql = """
            SELECT
                constraint_info.constraint_name,
                attribute.attname AS column_name,
                member.ordinality AS member_order
            FROM (
                SELECT
                    constraint_entry.conname
                        AS constraint_name,

                    constraint_entry.conrelid,

                    constraint_entry.conkey

                FROM pg_constraint constraint_entry

                JOIN pg_class table_entry
                    ON table_entry.oid =
                       constraint_entry.conrelid

                JOIN pg_namespace schema_entry
                    ON schema_entry.oid =
                       table_entry.relnamespace

                WHERE constraint_entry.contype = ?
                AND schema_entry.nspname = ?
                AND table_entry.relname = ?
            ) AS constraint_info

            CROSS JOIN LATERAL
                unnest(
                    constraint_info.conkey
                )
                WITH ORDINALITY
                AS member(
                    attribute_number,
                    ordinality
                )

            JOIN pg_attribute attribute
                ON attribute.attrelid =
                   constraint_info.conrelid
                AND attribute.attnum =
                    member.attribute_number

            ORDER BY
                constraint_info.constraint_name,
                member.ordinality
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.setString(
                1,
                postgresConstraintType
            )

            statement.setString(
                2,
                schemaName
            )

            statement.setString(
                3,
                tableName
            )

            statement.executeQuery().use { resultSet ->

                while (resultSet.next()) {

                    val keyName =
                        resultSet.getString(
                            "constraint_name"
                        )

                    keyMembers
                        .getOrPut(
                            keyName
                        ) {
                            mutableListOf()
                        }
                        .add(
                            DiscoveredKeyMember(
                                columnName =
                                    resultSet.getString(
                                        "column_name"
                                    ),
                                memberOrder =
                                    resultSet.getInt(
                                        "member_order"
                                    ) - 1
                            )
                        )
                }
            }
        }

        return keyMembers.map {
                (
                    keyName,
                    members
                ) ->

            DiscoveredKey(
                databaseName =
                    databaseName,
                schemaName =
                    schemaName,
                tableName =
                    tableName,
                keyName =
                    keyName,
                kind =
                    keyKind,
                members =
                    members.toList()
            )
        }
    }


    // ============================================================
    // FOREIGN KEYS
    // ============================================================

    override fun discoverForeignKeys(
        schemaName: String,
        tableName: String
    ): List<DiscoveredForeignKey> {

        val databaseName =
            getDatabaseName()

        data class ForeignKeyAccumulator(
            val referencedSchemaName: String,
            val referencedTableName: String,
            val referencedKeyName: String?,
            val members:
            MutableList<DiscoveredForeignKeyMember>
        )

        val foreignKeys =
            linkedMapOf<
                String,
                ForeignKeyAccumulator
                >()

        val sql = """
            SELECT
                foreign_key.conname
                    AS foreign_key_name,

                target_schema.nspname
                    AS referenced_schema_name,

                target_table.relname
                    AS referenced_table_name,

                referenced_key.conname
                    AS referenced_key_name,

                source_attribute.attname
                    AS source_column_name,

                target_attribute.attname
                    AS target_column_name,

                member.ordinality
                    AS member_order

            FROM pg_constraint foreign_key

            JOIN pg_class source_table
                ON source_table.oid =
                   foreign_key.conrelid

            JOIN pg_namespace source_schema
                ON source_schema.oid =
                   source_table.relnamespace

            JOIN pg_class target_table
                ON target_table.oid =
                   foreign_key.confrelid

            JOIN pg_namespace target_schema
                ON target_schema.oid =
                   target_table.relnamespace

            CROSS JOIN LATERAL
                unnest(
                    foreign_key.conkey,
                    foreign_key.confkey
                )
                WITH ORDINALITY
                AS member(
                    source_attribute_number,
                    target_attribute_number,
                    ordinality
                )

            JOIN pg_attribute source_attribute
                ON source_attribute.attrelid =
                   foreign_key.conrelid
                AND source_attribute.attnum =
                    member.source_attribute_number

            JOIN pg_attribute target_attribute
                ON target_attribute.attrelid =
                   foreign_key.confrelid
                AND target_attribute.attnum =
                    member.target_attribute_number

            LEFT JOIN pg_constraint referenced_key
                ON referenced_key.conrelid =
                   foreign_key.confrelid
                AND referenced_key.contype
                    IN ('p', 'u')
                AND referenced_key.conkey =
                    foreign_key.confkey

            WHERE foreign_key.contype = 'f'
            AND source_schema.nspname = ?
            AND source_table.relname = ?

            ORDER BY
                foreign_key.conname,
                member.ordinality
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.setString(
                1,
                schemaName
            )

            statement.setString(
                2,
                tableName
            )

            statement.executeQuery().use { resultSet ->

                while (resultSet.next()) {

                    val foreignKeyName =
                        resultSet.getString(
                            "foreign_key_name"
                        )

                    val accumulator =
                        foreignKeys.getOrPut(
                            foreignKeyName
                        ) {

                            ForeignKeyAccumulator(
                                referencedSchemaName =
                                    resultSet.getString(
                                        "referenced_schema_name"
                                    ),
                                referencedTableName =
                                    resultSet.getString(
                                        "referenced_table_name"
                                    ),
                                referencedKeyName =
                                    resultSet.getString(
                                        "referenced_key_name"
                                    ),
                                members =
                                    mutableListOf()
                            )
                        }

                    accumulator.members.add(
                        DiscoveredForeignKeyMember(
                            sourceColumnName =
                                resultSet.getString(
                                    "source_column_name"
                                ),
                            targetColumnName =
                                resultSet.getString(
                                    "target_column_name"
                                ),
                            memberOrder =
                                resultSet.getInt(
                                    "member_order"
                                ) - 1
                        )
                    )
                }
            }
        }

        return foreignKeys.map {
                (
                    foreignKeyName,
                    accumulator
                ) ->

            DiscoveredForeignKey(
                databaseName =
                    databaseName,
                schemaName =
                    schemaName,
                tableName =
                    tableName,
                foreignKeyName =
                    foreignKeyName,
                referencedSchemaName =
                    accumulator.referencedSchemaName,
                referencedTableName =
                    accumulator.referencedTableName,
                referencedKeyName =
                    accumulator.referencedKeyName,
                members =
                    accumulator.members.toList()
            )
        }
    }


    // ============================================================
    // INDEXES
    // ============================================================

    override fun discoverIndexes(
        schemaName: String,
        tableName: String
    ): List<DiscoveredIndex> {

        val databaseName =
            getDatabaseName()

        data class IndexAccumulator(
            val isUnique: Boolean,
            val isClustered: Boolean,
            val isPrimaryKeyBackingIndex: Boolean,
            val accessMethod: String,
            val predicate: String?,
            val members:
            MutableList<DiscoveredIndexMember>
        )

        val indexes =
            linkedMapOf<
                String,
                IndexAccumulator
                >()

        val sql = """
            SELECT
                index_table.relname
                    AS index_name,

                index_info.indisunique
                    AS is_unique,

                index_info.indisclustered
                    AS is_clustered,

                index_info.indisprimary
                    AS is_primary,

                access_method.amname
                    AS access_method,

                pg_get_expr(
                    index_info.indpred,
                    index_info.indrelid
                ) AS predicate,

                index_info.indnkeyatts
                    AS key_attribute_count,

                member.ordinality
                    AS member_order,

                CASE
                    WHEN member.attribute_number = 0
                    THEN NULL
                    ELSE table_attribute.attname
                END AS column_name,

                pg_get_indexdef(
                    index_info.indexrelid,
                    member.ordinality::integer,
                    TRUE
                ) AS member_definition

            FROM pg_index index_info

            JOIN pg_class source_table
                ON source_table.oid =
                   index_info.indrelid

            JOIN pg_namespace source_schema
                ON source_schema.oid =
                   source_table.relnamespace

            JOIN pg_class index_table
                ON index_table.oid =
                   index_info.indexrelid

            JOIN pg_am access_method
                ON access_method.oid =
                   index_table.relam

            CROSS JOIN LATERAL
                unnest(
                    index_info.indkey::smallint[]
                )
                WITH ORDINALITY
                AS member(
                    attribute_number,
                    ordinality
                )

            LEFT JOIN pg_attribute table_attribute
                ON table_attribute.attrelid =
                   source_table.oid
                AND table_attribute.attnum =
                    member.attribute_number
                AND member.attribute_number > 0

            WHERE source_schema.nspname = ?
            AND source_table.relname = ?

            ORDER BY
                index_table.relname,
                member.ordinality
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.setString(
                1,
                schemaName
            )

            statement.setString(
                2,
                tableName
            )

            statement.executeQuery().use { resultSet ->

                while (resultSet.next()) {

                    val indexName =
                        resultSet.getString(
                            "index_name"
                        )

                    val keyAttributeCount =
                        resultSet.getInt(
                            "key_attribute_count"
                        )

                    val memberOrderOneBased =
                        resultSet.getInt(
                            "member_order"
                        )

                    val accumulator =
                        indexes.getOrPut(
                            indexName
                        ) {

                            IndexAccumulator(
                                isUnique =
                                    resultSet.getBoolean(
                                        "is_unique"
                                    ),
                                isClustered =
                                    resultSet.getBoolean(
                                        "is_clustered"
                                    ),
                                isPrimaryKeyBackingIndex =
                                    resultSet.getBoolean(
                                        "is_primary"
                                    ),
                                accessMethod =
                                    resultSet.getString(
                                        "access_method"
                                    ),
                                predicate =
                                    resultSet.getString(
                                        "predicate"
                                    ),
                                members =
                                    mutableListOf()
                            )
                        }

                    accumulator.members.add(
                        DiscoveredIndexMember(
                            columnName =
                                resultSet.getString(
                                    "column_name"
                                ),
                            memberDefinition =
                                resultSet.getString(
                                    "member_definition"
                                ),
                            memberOrder =
                                memberOrderOneBased - 1,
                            isKeyMember =
                                memberOrderOneBased <=
                                    keyAttributeCount
                        )
                    )
                }
            }
        }

        return indexes.map {
                (
                    indexName,
                    accumulator
                ) ->

            DiscoveredIndex(
                databaseName =
                    databaseName,
                schemaName =
                    schemaName,
                tableName =
                    tableName,
                indexName =
                    indexName,
                isUnique =
                    accumulator.isUnique,
                isClustered =
                    accumulator.isClustered,
                isPrimaryKeyBackingIndex =
                    accumulator
                        .isPrimaryKeyBackingIndex,
                accessMethod =
                    accumulator.accessMethod,
                predicate =
                    accumulator.predicate,
                members =
                    accumulator.members.toList()
            )
        }
    }
}
