package com.alpinedigitalexperts.databaseanalyser

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class ApplicationCatalogIntegrationTest {

    // ============================================================
    // CATALOG ROOT STRUCTURE
    // ============================================================

    @Test
    fun `catalog has required root structure`() {

        DatabaseConnector.connect().use { connection ->

            val analyzer =
                DatabaseAnalyzer(connection)

            val catalog =
                DatabaseCatalogBuilder(analyzer)
                    .buildCatalog()

            val jsonText =
                ApplicationCatalogJsonExporter()
                    .toJson(catalog)

            val root =
                Json
                    .parseToJsonElement(jsonText)
                    .jsonObject

            assertEquals(
                1,
                root["schemaVersion"]
                    ?.jsonPrimitive
                    ?.content
                    ?.toInt()
            )

            assertEquals(
                "legacy-modernization-catalog",
                root["format"]
                    ?.jsonPrimitive
                    ?.content
            )

            assertTrue(
                root.containsKey("nodes")
            )

            assertTrue(
                root.containsKey("relationships")
            )

            assertTrue(
                root["nodes"]
                    ?.jsonArray
                    ?.isNotEmpty()
                    == true
            )
        }
    }


    // ============================================================
    // NODE IDS MUST BE UNIQUE
    // ============================================================

    @Test
    fun `catalog node ids are unique`() {

        DatabaseConnector.connect().use { connection ->

            val catalog =
                DatabaseCatalogBuilder(
                    DatabaseAnalyzer(connection)
                )
                    .buildCatalog()

            val nodeIds =
                catalog.nodes.map {
                    it.nodeId
                }

            assertEquals(
                nodeIds.size,
                nodeIds.toSet().size
            )
        }
    }


    // ============================================================
    // RELATIONSHIP IDS MUST BE UNIQUE
    // ============================================================

    @Test
    fun `catalog relationship ids are unique`() {

        DatabaseConnector.connect().use { connection ->

            val catalog =
                DatabaseCatalogBuilder(
                    DatabaseAnalyzer(connection)
                )
                    .buildCatalog()

            val relationshipIds =
                catalog.relationships.map {
                    it.relationshipId
                }

            assertEquals(
                relationshipIds.size,
                relationshipIds.toSet().size
            )
        }
    }


    // ============================================================
    // ALL RELATIONSHIPS MUST POINT TO REAL NODES
    // ============================================================

    @Test
    fun `all catalog relationships reference existing nodes`() {

        DatabaseConnector.connect().use { connection ->

            val catalog =
                DatabaseCatalogBuilder(
                    DatabaseAnalyzer(connection)
                )
                    .buildCatalog()

            val nodeIds =
                catalog.nodes
                    .map {
                        it.nodeId
                    }
                    .toSet()

            for (
            relationship
            in catalog.relationships
            ) {

                assertTrue(
                    relationship.sourceNodeId in nodeIds,
                    "Missing source node for relationship " +
                        relationship.relationshipId
                )

                assertTrue(
                    relationship.targetNodeId in nodeIds,
                    "Missing target node for relationship " +
                        relationship.relationshipId
                )
            }
        }
    }


    // ============================================================
    // IDENTITIES MUST REMAIN STABLE
    // ============================================================

    @Test
    fun `catalog identities are stable across repeated builds`() {

        DatabaseConnector.connect().use { connection ->

            val builder =
                DatabaseCatalogBuilder(
                    DatabaseAnalyzer(connection)
                )

            val firstCatalog =
                builder.buildCatalog()

            val secondCatalog =
                builder.buildCatalog()

            val firstNodeIds =
                firstCatalog.nodes
                    .map {
                        it.nodeId
                    }
                    .sorted()

            val secondNodeIds =
                secondCatalog.nodes
                    .map {
                        it.nodeId
                    }
                    .sorted()

            assertEquals(
                firstNodeIds,
                secondNodeIds
            )

            val firstRelationshipIds =
                firstCatalog.relationships
                    .map {
                        it.relationshipId
                    }
                    .sorted()

            val secondRelationshipIds =
                secondCatalog.relationships
                    .map {
                        it.relationshipId
                    }
                    .sorted()

            assertEquals(
                firstRelationshipIds,
                secondRelationshipIds
            )
        }
    }


    // ============================================================
    // DATABASE ANALYSIS ORIGIN
    // ============================================================

    @Test
    fun `database catalog nodes use database analysis origin`() {

        DatabaseConnector.connect().use { connection ->

            val catalog =
                DatabaseCatalogBuilder(
                    DatabaseAnalyzer(connection)
                )
                    .buildCatalog()

            assertTrue(
                catalog.nodes.all {
                    it.origin ==
                        ApplicationCatalogOrigin.DATABASE_ANALYSIS
                }
            )

            assertTrue(
                catalog.relationships.all {
                    it.origin ==
                        ApplicationCatalogOrigin.DATABASE_ANALYSIS
                }
            )
        }
    }


    // ============================================================
    // EXPECTED DATABASE HIERARCHY
    // ============================================================

    @Test
    fun `catalog contains database schemas tables and columns`() {

        DatabaseConnector.connect().use { connection ->

            val catalog =
                DatabaseCatalogBuilder(
                    DatabaseAnalyzer(connection)
                )
                    .buildCatalog()

            assertTrue(
                catalog.nodes.any {
                    it.dataObjectKind ==
                        ApplicationCatalogDataObjectKind.DATABASE &&
                        it.qualifiedName ==
                        "companydb"
                }
            )

            assertTrue(
                catalog.nodes.any {
                    it.dataObjectKind ==
                        ApplicationCatalogDataObjectKind.SCHEMA &&
                        it.qualifiedName ==
                        "companydb.public"
                }
            )

            assertTrue(
                catalog.nodes.any {
                    it.dataObjectKind ==
                        ApplicationCatalogDataObjectKind.SCHEMA &&
                        it.qualifiedName ==
                        "companydb.reporting"
                }
            )

            assertTrue(
                catalog.nodes.any {
                    it.dataObjectKind ==
                        ApplicationCatalogDataObjectKind.TABLE &&
                        it.qualifiedName ==
                        "companydb.public.employees"
                }
            )

            assertTrue(
                catalog.nodes.any {
                    it.dataObjectKind ==
                        ApplicationCatalogDataObjectKind.COLUMN &&
                        it.qualifiedName ==
                        "companydb.public.employees.email"
                }
            )
        }
    }


    // ============================================================
    // PRIMARY KEY MEMBERSHIP
    // ============================================================

    @Test
    fun `catalog exports primary key membership`() {

        DatabaseConnector.connect().use { connection ->

            val catalog =
                DatabaseCatalogBuilder(
                    DatabaseAnalyzer(connection)
                )
                    .buildCatalog()

            val primaryKeyNode =
                catalog.nodes
                    .firstOrNull {
                        it.dataObjectKind ==
                            ApplicationCatalogDataObjectKind.PRIMARY_KEY
                    }

            assertTrue(
                primaryKeyNode != null,
                "Expected at least one primary key node"
            )

            val membershipRelationships =
                catalog.relationships
                    .filter {
                        it.sourceNodeId ==
                            primaryKeyNode.nodeId &&
                            it.kind ==
                            ApplicationCatalogRelationshipKind.INCLUDES
                    }

            assertTrue(
                membershipRelationships.isNotEmpty(),
                "Expected primary key to include at least one column"
            )
        }
    }


    // ============================================================
    // FOREIGN KEY GRAPH
    // ============================================================

    @Test
    fun `catalog exports declared foreign key correctly`() {

        DatabaseConnector.connect().use { connection ->

            val catalog =
                DatabaseCatalogBuilder(
                    DatabaseAnalyzer(connection)
                )
                    .buildCatalog()

            val foreignKeyNode =
                catalog.nodes
                    .firstOrNull {
                        it.dataObjectKind ==
                            ApplicationCatalogDataObjectKind.FOREIGN_KEY
                    }

            assertTrue(
                foreignKeyNode != null,
                "Expected at least one foreign key node"
            )

            assertTrue(
                foreignKeyNode.isDeclared == true,
                "Expected database foreign key to be declared"
            )

            val foreignKeyRelationships =
                catalog.relationships
                    .filter {
                        it.sourceNodeId ==
                            foreignKeyNode.nodeId
                    }

            // A declared physical foreign key includes
            // its ordered source column members.
            val includedColumns =
                foreignKeyRelationships
                    .filter {
                        it.kind ==
                            ApplicationCatalogRelationshipKind.INCLUDES
                    }

            assertTrue(
                includedColumns.isNotEmpty(),
                "Expected foreign key to include at least one source column"
            )

            // A declared foreign key references its
            // target primary or unique key.
            val referencesKeyRelationship =
                foreignKeyRelationships
                    .firstOrNull {
                        it.kind ==
                            ApplicationCatalogRelationshipKind.REFERENCES_KEY
                    }

            assertTrue(
                referencesKeyRelationship != null,
                "Expected foreign key to reference a target key"
            )

            val referencedNode =
                catalog.nodes
                    .firstOrNull {
                        it.nodeId ==
                            referencesKeyRelationship.targetNodeId
                    }

            assertTrue(
                referencedNode != null,
                "Referenced target key node was not found"
            )

            assertTrue(
                referencedNode.dataObjectKind ==
                    ApplicationCatalogDataObjectKind.PRIMARY_KEY ||
                    referencedNode.dataObjectKind ==
                    ApplicationCatalogDataObjectKind.UNIQUE_KEY,
                "Foreign key must reference a primary or unique key"
            )

            // hasSourceMember / hasTargetMember belong to
            // inferred dataRelationship nodes, not declared FKs.
            assertFalse(
                foreignKeyRelationships.any {
                    it.kind ==
                        ApplicationCatalogRelationshipKind.HAS_SOURCE_MEMBER ||
                        it.kind ==
                        ApplicationCatalogRelationshipKind.HAS_TARGET_MEMBER
                },
                "Declared foreign key must not use inferred relationship membership edges"
            )
        }
    }



    // ============================================================
    // DATABASE DEPENDENCY GRAPH
    // ============================================================

    @Test
    fun `catalog does not duplicate declared foreign key as depends on`() {

        DatabaseConnector.connect().use { connection ->

            val catalog =
                DatabaseCatalogBuilder(
                    DatabaseAnalyzer(connection)
                )
                    .buildCatalog()

            val sourceNode =
                catalog.nodes
                    .first {
                        it.dataObjectKind ==
                            ApplicationCatalogDataObjectKind.COLUMN &&
                            it.qualifiedName ==
                            "companydb.public.employees.department_id"
                    }

            val targetNode =
                catalog.nodes
                    .first {
                        it.dataObjectKind ==
                            ApplicationCatalogDataObjectKind.COLUMN &&
                            it.qualifiedName ==
                            "companydb.public.departments.department_id"
                    }

            assertTrue(
                catalog.relationships.none {
                    it.sourceNodeId == sourceNode.nodeId &&
                        it.kind ==
                        ApplicationCatalogRelationshipKind.DEPENDS_ON &&
                        it.targetNodeId == targetNode.nodeId
                },
                "Declared foreign key must not also be exported as a column-to-column DEPENDS_ON relationship"
            )
        }
    }


    // ============================================================
    // INDEX MEMBERSHIP
    // ============================================================

    @Test
    fun `catalog exports index membership when indexes exist`() {

        DatabaseConnector.connect().use { connection ->

            val catalog =
                DatabaseCatalogBuilder(
                    DatabaseAnalyzer(connection)
                )
                    .buildCatalog()

            val indexNodes =
                catalog.nodes
                    .filter {
                        it.dataObjectKind ==
                            ApplicationCatalogDataObjectKind.INDEX
                    }

            for (indexNode in indexNodes) {

                val relationships =
                    catalog.relationships
                        .filter {
                            it.sourceNodeId ==
                                indexNode.nodeId
                        }

                assertTrue(
                    relationships.any {
                        it.kind ==
                            ApplicationCatalogRelationshipKind.INDEXES
                    },
                    "Expected index to reference the table it indexes"
                )

                assertTrue(
                    relationships.any {
                        it.kind ==
                            ApplicationCatalogRelationshipKind.INCLUDES
                    },
                    "Expected index to include at least one column"
                )
            }
        }
    }



    // ============================================================
    // DEPENDENCY JSON EXPORT
    // ============================================================

    @Test
    fun `catalog json does not duplicate declared foreign key as depends on`() {

        DatabaseConnector.connect().use { connection ->

            val catalog =
                DatabaseCatalogBuilder(
                    DatabaseAnalyzer(connection)
                )
                    .buildCatalog()

            val sourceNode =
                catalog.nodes
                    .first {
                        it.dataObjectKind ==
                            ApplicationCatalogDataObjectKind.COLUMN &&
                            it.qualifiedName ==
                            "companydb.public.employees.department_id"
                    }

            val targetNode =
                catalog.nodes
                    .first {
                        it.dataObjectKind ==
                            ApplicationCatalogDataObjectKind.COLUMN &&
                            it.qualifiedName ==
                            "companydb.public.departments.department_id"
                    }

            val jsonText =
                ApplicationCatalogJsonExporter()
                    .toJson(catalog)

            val relationships =
                Json
                    .parseToJsonElement(jsonText)
                    .jsonObject["relationships"]
                    ?.jsonArray

            assertTrue(
                relationships != null,
                "Expected relationships array in catalog JSON"
            )

            assertTrue(
                relationships.none { element ->
                    val relationship = element.jsonObject

                    relationship["sourceNodeId"]
                        ?.jsonPrimitive
                        ?.content == sourceNode.nodeId &&
                        relationship["kind"]
                            ?.jsonPrimitive
                            ?.content == "dependsOn" &&
                        relationship["targetNodeId"]
                            ?.jsonPrimitive
                            ?.content == targetNode.nodeId
                },
                "Declared foreign key must not be serialized as a duplicate column-to-column dependsOn relationship"
            )
        }
    }


    // ============================================================
    // OPTIONAL NULL PROPERTIES MUST NOT BE WRITTEN
    // ============================================================

    @Test
    fun `catalog json does not contain json null values`() {

        DatabaseConnector.connect().use { connection ->

            val catalog =
                DatabaseCatalogBuilder(
                    DatabaseAnalyzer(connection)
                )
                    .buildCatalog()

            val jsonText =
                ApplicationCatalogJsonExporter()
                    .toJson(catalog)

            val jsonElement =
                Json.parseToJsonElement(
                    jsonText
                )

            assertFalse(
                containsJsonNull(
                    jsonElement
                ),
                "Application catalog contains JSON null values"
            )
        }
    }


    // ============================================================
    // HELPER
    // ============================================================

    private fun containsJsonNull(
        element: JsonElement
    ): Boolean {

        return when (element) {

            JsonNull ->
                true

            is JsonObject ->
                element.values.any {
                    containsJsonNull(it)
                }

            is JsonArray ->
                element.any {
                    containsJsonNull(it)
                }

            else ->
                false
        }
    }
}
