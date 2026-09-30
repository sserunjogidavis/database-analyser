package com.alpinedigitalexperts.databaseanalyser

import com.networknt.schema.InputFormat
import com.networknt.schema.SchemaRegistry
import com.networknt.schema.SpecificationVersion
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertTrue


class ApplicationCatalogSchemaValidationTest {

    @Test
    fun `generated application catalog conforms to pinned workbench schema`() {

        // ========================================================
        // BUILD APPLICATION CATALOG
        // ========================================================

        val catalogJson =
            DatabaseConnector.connect().use { connection ->

                val analyzer =
                    DatabaseAnalyzer(connection)

                val catalog =
                    DatabaseCatalogBuilder(
                        analyzer
                    )
                        .buildCatalog()

                ApplicationCatalogJsonExporter()
                    .toJson(catalog)
            }


        // ========================================================
        // LOAD PINNED WORKBENCH SCHEMA
        // ========================================================

        val schemaPath =
            Path.of(
                "contracts",
                "workbench-catalog",
                "snapshot",
                "contracts",
                "catalog",
                "v1",
                "catalog.schema.json"
            )

        assertTrue(
            Files.exists(schemaPath),
            "Pinned Workbench catalog schema was not found at: " +
                schemaPath.toAbsolutePath()
        )

        val schemaJson =
            Files.readString(
                schemaPath
            )


        // ========================================================
        // CREATE DRAFT 2020-12 SCHEMA REGISTRY
        // ========================================================

        val schemaRegistry =
            SchemaRegistry.withDefaultDialect(
                SpecificationVersion.DRAFT_2020_12
            )


        // ========================================================
        // LOAD SCHEMA
        // ========================================================

        val schema =
            schemaRegistry.getSchema(
                schemaJson
            )


        // ========================================================
        // VALIDATE GENERATED CATALOG
        // ========================================================

        val validationErrors =
            schema.validate(
                catalogJson,
                InputFormat.JSON
            )


        // ========================================================
        // FAIL TEST WITH USEFUL ERROR MESSAGE
        // ========================================================

        assertTrue(
            validationErrors.isEmpty(),
            buildString {

                appendLine(
                    "Generated application catalog does not conform " +
                        "to the pinned Workbench schema."
                )

                appendLine()

                appendLine(
                    "Validation errors:"
                )

                validationErrors.forEach { error ->

                    appendLine(
                        " - $error"
                    )
                }
            }
        )
    }
}
