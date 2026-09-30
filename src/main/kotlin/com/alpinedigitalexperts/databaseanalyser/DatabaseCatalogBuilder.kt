package com.alpinedigitalexperts.databaseanalyser

import java.security.MessageDigest


class DatabaseCatalogBuilder(
    private val analyzer: DatabaseAnalyzer
) {

    fun buildCatalog(): ApplicationCatalog {

        val nodes =
            mutableListOf<ApplicationCatalogNode>()

        val relationships =
            mutableListOf<ApplicationCatalogRelationship>()


        // ========================================================
        // DISCOVER DATABASE METADATA
        // ========================================================

        val schemas =
            analyzer
                .discoverSchemas()
                .sortedBy {
                    it.schemaName
                }

        val tables =
            analyzer
                .discoverTables()
                .sortedWith(
                    compareBy(
                        DiscoveredTable::schemaName,
                        DiscoveredTable::tableName
                    )
                )

        val views =
            analyzer
                .discoverViews()
                .sortedWith(
                    compareBy(
                        DiscoveredView::schemaName,
                        DiscoveredView::viewName
                    )
                )

        val materializedViews =
            analyzer
                .discoverMaterializedViews()
                .sortedWith(
                    compareBy(
                        DiscoveredView::schemaName,
                        DiscoveredView::viewName
                    )
                )

        val sequences =
            analyzer
                .discoverSequences()
                .sortedWith(
                    compareBy(
                        DiscoveredSequence::schemaName,
                        DiscoveredSequence::sequenceName
                    )
                )

        val functions =
            analyzer
                .discoverFunctions()
                .sortedWith(
                    compareBy(
                        DiscoveredRoutine::schemaName,
                        DiscoveredRoutine::routineName,
                        DiscoveredRoutine::identityArguments
                    )
                )

        val storedProcedures =
            analyzer
                .discoverStoredProcedures()
                .sortedWith(
                    compareBy(
                        DiscoveredRoutine::schemaName,
                        DiscoveredRoutine::routineName,
                        DiscoveredRoutine::identityArguments
                    )
                )


        val dependencies =
            analyzer
                .discoverDependencies()
                .sortedWith(
                    compareBy(
                        DiscoveredDependency::sourceSchemaName,
                        DiscoveredDependency::sourceObjectName,
                        DiscoveredDependency::sourceSubObjectName,
                        DiscoveredDependency::targetSchemaName,
                        DiscoveredDependency::targetObjectName,
                        DiscoveredDependency::targetSubObjectName,
                        DiscoveredDependency::dependencyKind
                    )
                )


        // ========================================================
        // DATABASE NAME
        // ========================================================

        val databaseName =
            schemas
                .firstOrNull()
                ?.databaseName
                ?: tables
                    .firstOrNull()
                    ?.databaseName
                ?: views
                    .firstOrNull()
                    ?.databaseName
                ?: materializedViews
                    .firstOrNull()
                    ?.databaseName
                ?: sequences
                    .firstOrNull()
                    ?.databaseName
                ?: functions
                    .firstOrNull()
                    ?.databaseName
                ?: storedProcedures
                    .firstOrNull()
                    ?.databaseName
                ?: "unknown"


        // ========================================================
        // PRELOAD TABLE METADATA
        // ========================================================

        val columnsByTable =
            linkedMapOf<String, List<DiscoveredColumn>>()

        val primaryKeysByTable =
            linkedMapOf<String, List<DiscoveredKey>>()

        val uniqueKeysByTable =
            linkedMapOf<String, List<DiscoveredKey>>()

        val foreignKeysByTable =
            linkedMapOf<String, List<DiscoveredForeignKey>>()

        val indexesByTable =
            linkedMapOf<String, List<DiscoveredIndex>>()


        for (table in tables) {

            val key =
                tableKey(
                    table.schemaName,
                    table.tableName
                )

            columnsByTable[key] =
                analyzer
                    .discoverColumns(
                        schemaName = table.schemaName,
                        tableName = table.tableName
                    )
                    .sortedBy {
                        it.ordinalPosition
                    }

            primaryKeysByTable[key] =
                analyzer
                    .discoverPrimaryKeys(
                        schemaName = table.schemaName,
                        tableName = table.tableName
                    )
                    .sortedBy {
                        it.keyName
                    }

            uniqueKeysByTable[key] =
                analyzer
                    .discoverUniqueKeys(
                        schemaName = table.schemaName,
                        tableName = table.tableName
                    )
                    .sortedBy {
                        it.keyName
                    }

            foreignKeysByTable[key] =
                analyzer
                    .discoverForeignKeys(
                        schemaName = table.schemaName,
                        tableName = table.tableName
                    )
                    .sortedBy {
                        it.foreignKeyName
                    }

            indexesByTable[key] =
                analyzer
                    .discoverIndexes(
                        schemaName = table.schemaName,
                        tableName = table.tableName
                    )
                    .sortedBy {
                        it.indexName
                    }
        }


        // ========================================================
        // LOOKUP FOR TARGET KEYS
        // ========================================================

        val discoveredKeys =
            mutableMapOf<String, DiscoveredKey>()

        for (table in tables) {

            val key =
                tableKey(
                    table.schemaName,
                    table.tableName
                )

            val keys =
                primaryKeysByTable[key].orEmpty() +
                    uniqueKeysByTable[key].orEmpty()

            for (discoveredKey in keys) {

                discoveredKeys[
                    constraintKey(
                        discoveredKey.schemaName,
                        discoveredKey.tableName,
                        discoveredKey.keyName
                    )
                ] =
                    discoveredKey
            }
        }


        // ========================================================
        // DATABASE NODE
        // ========================================================

        val databaseNodeId =
            stableNodeId(
                type = "database",
                qualifiedName = databaseName
            )

        nodes +=
            ApplicationCatalogNode(
                nodeId = databaseNodeId,
                nodeType =
                    ApplicationCatalogNodeType.DATA_OBJECT,
                displayName =
                    databaseName,
                origin =
                    ApplicationCatalogOrigin.DATABASE_ANALYSIS,
                dataObjectKind =
                    ApplicationCatalogDataObjectKind.DATABASE,
                qualifiedName =
                    databaseName,
                evidenceClassification =
                    ApplicationCatalogEvidenceClassification.OBSERVED
            )


        // ========================================================
        // SCHEMAS
        // ========================================================

        for (
        (schemaIndex, schema)
        in schemas.withIndex()
        ) {

            val schemaNodeId =
                stableNodeId(
                    type = "schema",
                    qualifiedName =
                        schema.qualifiedName
                )

            nodes +=
                ApplicationCatalogNode(
                    nodeId =
                        schemaNodeId,
                    nodeType =
                        ApplicationCatalogNodeType.DATA_OBJECT,
                    displayName =
                        schema.schemaName,
                    origin =
                        ApplicationCatalogOrigin.DATABASE_ANALYSIS,
                    dataObjectKind =
                        ApplicationCatalogDataObjectKind.SCHEMA,
                    qualifiedName =
                        schema.qualifiedName,
                    evidenceClassification =
                        ApplicationCatalogEvidenceClassification.OBSERVED
                )

            relationships +=
                containsRelationship(
                    parentNodeId =
                        databaseNodeId,
                    childNodeId =
                        schemaNodeId,
                    order =
                        schemaIndex
                )
        }


        // ========================================================
        // TABLES BY SCHEMA
        // ========================================================

        for (schema in schemas) {

            val schemaNodeId =
                stableNodeId(
                    type = "schema",
                    qualifiedName =
                        schema.qualifiedName
                )

            var schemaChildOrder = 0


            // ====================================================
            // TABLES
            // ====================================================

            val schemaTables =
                tables.filter {
                    it.schemaName ==
                        schema.schemaName
                }

            for (table in schemaTables) {

                val tableNodeId =
                    stableNodeId(
                        type = "table",
                        qualifiedName =
                            table.qualifiedName
                    )

                nodes +=
                    ApplicationCatalogNode(
                        nodeId =
                            tableNodeId,
                        nodeType =
                            ApplicationCatalogNodeType.DATA_OBJECT,
                        displayName =
                            table.tableName,
                        origin =
                            ApplicationCatalogOrigin.DATABASE_ANALYSIS,
                        dataObjectKind =
                            ApplicationCatalogDataObjectKind.TABLE,
                        qualifiedName =
                            table.qualifiedName,
                        evidenceClassification =
                            ApplicationCatalogEvidenceClassification.OBSERVED
                    )

                relationships +=
                    containsRelationship(
                        parentNodeId =
                            schemaNodeId,
                        childNodeId =
                            tableNodeId,
                        order =
                            schemaChildOrder++
                    )


                val metadataKey =
                    tableKey(
                        table.schemaName,
                        table.tableName
                    )

                val columns =
                    columnsByTable[
                        metadataKey
                    ]
                        .orEmpty()

                val primaryKeys =
                    primaryKeysByTable[
                        metadataKey
                    ]
                        .orEmpty()

                val uniqueKeys =
                    uniqueKeysByTable[
                        metadataKey
                    ]
                        .orEmpty()

                val foreignKeys =
                    foreignKeysByTable[
                        metadataKey
                    ]
                        .orEmpty()

                val indexes =
                    indexesByTable[
                        metadataKey
                    ]
                        .orEmpty()


                val primaryKeyColumns =
                    primaryKeys
                        .flatMap {
                            it.members
                        }
                        .map {
                            it.columnName
                        }
                        .toSet()

                val singleColumnUniqueColumns =
                    uniqueKeys
                        .filter {
                            it.members.size == 1
                        }
                        .map {
                            it.members.first().columnName
                        }
                        .toSet()


                // =================================================
                // COLUMNS
                // =================================================

                for (column in columns) {

                    val columnNodeId =
                        stableNodeId(
                            type = "column",
                            qualifiedName =
                                column.qualifiedName
                        )

                    nodes +=
                        ApplicationCatalogNode(
                            nodeId =
                                columnNodeId,
                            nodeType =
                                ApplicationCatalogNodeType.DATA_OBJECT,
                            displayName =
                                column.columnName,
                            origin =
                                ApplicationCatalogOrigin.DATABASE_ANALYSIS,
                            dataObjectKind =
                                ApplicationCatalogDataObjectKind.COLUMN,
                            qualifiedName =
                                column.qualifiedName,
                            dataType =
                                column.dataType,
                            isNullable =
                                column.nullable,
                            isKey =
                                column.columnName in
                                    primaryKeyColumns,
                            databaseVendorType =
                                column.databaseVendorType,
                            isUnique =
                                column.columnName in
                                    singleColumnUniqueColumns,
                            evidenceClassification =
                                ApplicationCatalogEvidenceClassification.OBSERVED
                        )

                    relationships +=
                        containsRelationship(
                            parentNodeId =
                                tableNodeId,
                            childNodeId =
                                columnNodeId,
                            order =
                                column.ordinalPosition - 1
                        )
                }


                var tableChildOrder =
                    columns.size


                // =================================================
                // PRIMARY KEYS
                // =================================================

                for (primaryKey in primaryKeys) {

                    val primaryKeyNodeId =
                        stableNodeId(
                            type = "primaryKey",
                            qualifiedName =
                                primaryKey.qualifiedName
                        )

                    nodes +=
                        ApplicationCatalogNode(
                            nodeId =
                                primaryKeyNodeId,
                            nodeType =
                                ApplicationCatalogNodeType.DATA_OBJECT,
                            displayName =
                                primaryKey.keyName,
                            origin =
                                ApplicationCatalogOrigin.DATABASE_ANALYSIS,
                            dataObjectKind =
                                ApplicationCatalogDataObjectKind.PRIMARY_KEY,
                            qualifiedName =
                                primaryKey.qualifiedName,
                            isDeclared =
                                true,
                            constraintEnforcement =
                                ApplicationCatalogConstraintEnforcement.ENFORCED,
                            evidenceClassification =
                                ApplicationCatalogEvidenceClassification.OBSERVED
                        )

                    relationships +=
                        containsRelationship(
                            parentNodeId =
                                tableNodeId,
                            childNodeId =
                                primaryKeyNodeId,
                            order =
                                tableChildOrder++
                        )

                    for (
                    member
                    in primaryKey.members
                        .sortedBy {
                            it.memberOrder
                        }
                    ) {

                        val columnQualifiedName =
                            "${primaryKey.databaseName}." +
                                "${primaryKey.schemaName}." +
                                "${primaryKey.tableName}." +
                                member.columnName

                        val columnNodeId =
                            stableNodeId(
                                type = "column",
                                qualifiedName =
                                    columnQualifiedName
                            )

                        relationships +=
                            includesRelationship(
                                sourceNodeId =
                                    primaryKeyNodeId,
                                targetNodeId =
                                    columnNodeId,
                                order =
                                    member.memberOrder
                            )
                    }
                }


                // =================================================
                // UNIQUE KEYS
                // =================================================

                for (uniqueKey in uniqueKeys) {

                    val uniqueKeyNodeId =
                        stableNodeId(
                            type = "uniqueKey",
                            qualifiedName =
                                uniqueKey.qualifiedName
                        )

                    nodes +=
                        ApplicationCatalogNode(
                            nodeId =
                                uniqueKeyNodeId,
                            nodeType =
                                ApplicationCatalogNodeType.DATA_OBJECT,
                            displayName =
                                uniqueKey.keyName,
                            origin =
                                ApplicationCatalogOrigin.DATABASE_ANALYSIS,
                            dataObjectKind =
                                ApplicationCatalogDataObjectKind.UNIQUE_KEY,
                            qualifiedName =
                                uniqueKey.qualifiedName,
                            isDeclared =
                                true,
                            constraintEnforcement =
                                ApplicationCatalogConstraintEnforcement.ENFORCED,
                            evidenceClassification =
                                ApplicationCatalogEvidenceClassification.OBSERVED
                        )

                    relationships +=
                        containsRelationship(
                            parentNodeId =
                                tableNodeId,
                            childNodeId =
                                uniqueKeyNodeId,
                            order =
                                tableChildOrder++
                        )

                    for (
                    member
                    in uniqueKey.members
                        .sortedBy {
                            it.memberOrder
                        }
                    ) {

                        val columnQualifiedName =
                            "${uniqueKey.databaseName}." +
                                "${uniqueKey.schemaName}." +
                                "${uniqueKey.tableName}." +
                                member.columnName

                        val columnNodeId =
                            stableNodeId(
                                type = "column",
                                qualifiedName =
                                    columnQualifiedName
                            )

                        relationships +=
                            includesRelationship(
                                sourceNodeId =
                                    uniqueKeyNodeId,
                                targetNodeId =
                                    columnNodeId,
                                order =
                                    member.memberOrder
                            )
                    }
                }


                // =================================================
                // FOREIGN KEYS
                // =================================================

                for (foreignKey in foreignKeys) {

                    val foreignKeyNodeId =
                        stableNodeId(
                            type = "foreignKey",
                            qualifiedName =
                                foreignKey.qualifiedName
                        )

                    nodes +=
                        ApplicationCatalogNode(
                            nodeId =
                                foreignKeyNodeId,
                            nodeType =
                                ApplicationCatalogNodeType.DATA_OBJECT,
                            displayName =
                                foreignKey.foreignKeyName,
                            origin =
                                ApplicationCatalogOrigin.DATABASE_ANALYSIS,
                            dataObjectKind =
                                ApplicationCatalogDataObjectKind.FOREIGN_KEY,
                            qualifiedName =
                                foreignKey.qualifiedName,
                            isDeclared =
                                true,
                            constraintEnforcement =
                                ApplicationCatalogConstraintEnforcement.ENFORCED,
                            evidenceClassification =
                                ApplicationCatalogEvidenceClassification.OBSERVED
                        )

                    relationships +=
                        containsRelationship(
                            parentNodeId =
                                tableNodeId,
                            childNodeId =
                                foreignKeyNodeId,
                            order =
                                tableChildOrder++
                        )


                    for (
                    member
                    in foreignKey.members
                        .sortedBy {
                            it.memberOrder
                        }
                    ) {

                        val sourceColumnQualifiedName =
                            "${foreignKey.databaseName}." +
                                "${foreignKey.schemaName}." +
                                "${foreignKey.tableName}." +
                                member.sourceColumnName

                        val sourceColumnNodeId =
                            stableNodeId(
                                type = "column",
                                qualifiedName =
                                    sourceColumnQualifiedName
                            )

                        relationships +=
                            includesRelationship(
                                sourceNodeId =
                                    foreignKeyNodeId,
                                targetNodeId =
                                    sourceColumnNodeId,
                                order =
                                    member.memberOrder
                            )
                    }


                    val referencedKeyName =
                        foreignKey.referencedKeyName

                    if (referencedKeyName != null) {

                        val referencedKey =
                            discoveredKeys[
                                constraintKey(
                                    foreignKey.referencedSchemaName,
                                    foreignKey.referencedTableName,
                                    referencedKeyName
                                )
                            ]

                        if (referencedKey != null) {

                            val referencedKeyType =
                                when (
                                    referencedKey.kind
                                ) {

                                    DiscoveredKeyKind.PRIMARY_KEY ->
                                        "primaryKey"

                                    DiscoveredKeyKind.UNIQUE_KEY ->
                                        "uniqueKey"
                                }

                            val referencedKeyNodeId =
                                stableNodeId(
                                    type =
                                        referencedKeyType,
                                    qualifiedName =
                                        referencedKey.qualifiedName
                                )

                            relationships +=
                                applicationRelationship(
                                    sourceNodeId =
                                        foreignKeyNodeId,
                                    kind =
                                        ApplicationCatalogRelationshipKind.REFERENCES_KEY,
                                    targetNodeId =
                                        referencedKeyNodeId
                                )
                        }
                    }
                }


                // =================================================
                // INDEXES
                // =================================================

                for (index in indexes) {

                    val indexNodeId =
                        stableNodeId(
                            type = "index",
                            qualifiedName =
                                index.qualifiedName
                        )

                    nodes +=
                        ApplicationCatalogNode(
                            nodeId =
                                indexNodeId,
                            nodeType =
                                ApplicationCatalogNodeType.DATA_OBJECT,
                            displayName =
                                index.indexName,
                            origin =
                                ApplicationCatalogOrigin.DATABASE_ANALYSIS,
                            dataObjectKind =
                                ApplicationCatalogDataObjectKind.INDEX,
                            qualifiedName =
                                index.qualifiedName,
                            databaseVendorType =
                                index.accessMethod,
                            isUnique =
                                index.isUnique,
                            isClustered =
                                index.isClustered,
                            evidenceClassification =
                                ApplicationCatalogEvidenceClassification.OBSERVED
                        )

                    relationships +=
                        containsRelationship(
                            parentNodeId =
                                tableNodeId,
                            childNodeId =
                                indexNodeId,
                            order =
                                tableChildOrder++
                        )

                    relationships +=
                        applicationRelationship(
                            sourceNodeId =
                                indexNodeId,
                            kind =
                                ApplicationCatalogRelationshipKind.INDEXES,
                            targetNodeId =
                                tableNodeId
                        )

                    for (
                    member
                    in index.members
                        .sortedBy {
                            it.memberOrder
                        }
                    ) {

                        val columnName =
                            member.columnName

                        if (columnName != null) {

                            val columnQualifiedName =
                                "${index.databaseName}." +
                                    "${index.schemaName}." +
                                    "${index.tableName}." +
                                    columnName

                            val columnNodeId =
                                stableNodeId(
                                    type = "column",
                                    qualifiedName =
                                        columnQualifiedName
                                )

                            relationships +=
                                includesRelationship(
                                    sourceNodeId =
                                        indexNodeId,
                                    targetNodeId =
                                        columnNodeId,
                                    order =
                                        member.memberOrder
                                )
                        }
                    }
                }
            }


            // ====================================================
            // VIEWS
            // ====================================================

            val schemaViews =
                views.filter {
                    it.schemaName ==
                        schema.schemaName
                }

            for (view in schemaViews) {

                val viewNodeId =
                    stableNodeId(
                        type = "view",
                        qualifiedName =
                            view.qualifiedName
                    )

                nodes +=
                    ApplicationCatalogNode(
                        nodeId =
                            viewNodeId,
                        nodeType =
                            ApplicationCatalogNodeType.DATA_OBJECT,
                        displayName =
                            view.viewName,
                        origin =
                            ApplicationCatalogOrigin.DATABASE_ANALYSIS,
                        dataObjectKind =
                            ApplicationCatalogDataObjectKind.VIEW,
                        qualifiedName =
                            view.qualifiedName,
                        evidenceClassification =
                            ApplicationCatalogEvidenceClassification.OBSERVED
                    )

                relationships +=
                    containsRelationship(
                        parentNodeId =
                            schemaNodeId,
                        childNodeId =
                            viewNodeId,
                        order =
                            schemaChildOrder++
                    )
            }


            // ====================================================
            // MATERIALIZED VIEWS
            // ====================================================

            val schemaMaterializedViews =
                materializedViews.filter {
                    it.schemaName ==
                        schema.schemaName
                }

            for (view in schemaMaterializedViews) {

                val viewNodeId =
                    stableNodeId(
                        type = "materializedView",
                        qualifiedName =
                            view.qualifiedName
                    )

                nodes +=
                    ApplicationCatalogNode(
                        nodeId =
                            viewNodeId,
                        nodeType =
                            ApplicationCatalogNodeType.DATA_OBJECT,
                        displayName =
                            view.viewName,
                        origin =
                            ApplicationCatalogOrigin.DATABASE_ANALYSIS,
                        dataObjectKind =
                            ApplicationCatalogDataObjectKind.MATERIALIZED_VIEW,
                        qualifiedName =
                            view.qualifiedName,
                        evidenceClassification =
                            ApplicationCatalogEvidenceClassification.OBSERVED
                    )

                relationships +=
                    containsRelationship(
                        parentNodeId =
                            schemaNodeId,
                        childNodeId =
                            viewNodeId,
                        order =
                            schemaChildOrder++
                    )
            }


            // ====================================================
            // SEQUENCES
            // ====================================================

            val schemaSequences =
                sequences.filter {
                    it.schemaName ==
                        schema.schemaName
                }

            for (sequence in schemaSequences) {

                val sequenceNodeId =
                    stableNodeId(
                        type = "sequence",
                        qualifiedName =
                            sequence.qualifiedName
                    )

                nodes +=
                    ApplicationCatalogNode(
                        nodeId =
                            sequenceNodeId,
                        nodeType =
                            ApplicationCatalogNodeType.DATA_OBJECT,
                        displayName =
                            sequence.sequenceName,
                        origin =
                            ApplicationCatalogOrigin.DATABASE_ANALYSIS,
                        dataObjectKind =
                            ApplicationCatalogDataObjectKind.SEQUENCE,
                        qualifiedName =
                            sequence.qualifiedName,
                        dataType =
                            sequence.dataType,
                        evidenceClassification =
                            ApplicationCatalogEvidenceClassification.OBSERVED
                    )

                relationships +=
                    containsRelationship(
                        parentNodeId =
                            schemaNodeId,
                        childNodeId =
                            sequenceNodeId,
                        order =
                            schemaChildOrder++
                    )
            }


            // ====================================================
            // FUNCTIONS
            // ====================================================

            val schemaFunctions =
                functions.filter {
                    it.schemaName ==
                        schema.schemaName
                }

            for (routine in schemaFunctions) {

                val routineNodeId =
                    stableNodeId(
                        type = "function",
                        qualifiedName =
                            routine.qualifiedName
                    )

                nodes +=
                    ApplicationCatalogNode(
                        nodeId =
                            routineNodeId,
                        nodeType =
                            ApplicationCatalogNodeType.DATA_OBJECT,
                        displayName =
                            routineDisplayName(
                                routine
                            ),
                        origin =
                            ApplicationCatalogOrigin.DATABASE_ANALYSIS,
                        dataObjectKind =
                            ApplicationCatalogDataObjectKind.FUNCTION,
                        qualifiedName =
                            routine.qualifiedName,
                        dataType =
                            routine.resultType,
                        routineLanguage =
                            routine.language,
                        evidenceClassification =
                            ApplicationCatalogEvidenceClassification.OBSERVED
                    )

                relationships +=
                    containsRelationship(
                        parentNodeId =
                            schemaNodeId,
                        childNodeId =
                            routineNodeId,
                        order =
                            schemaChildOrder++
                    )
            }


            // ====================================================
            // STORED PROCEDURES
            // ====================================================

            val schemaProcedures =
                storedProcedures.filter {
                    it.schemaName ==
                        schema.schemaName
                }

            for (routine in schemaProcedures) {

                val routineNodeId =
                    stableNodeId(
                        type = "storedProcedure",
                        qualifiedName =
                            routine.qualifiedName
                    )

                nodes +=
                    ApplicationCatalogNode(
                        nodeId =
                            routineNodeId,
                        nodeType =
                            ApplicationCatalogNodeType.DATA_OBJECT,
                        displayName =
                            routineDisplayName(
                                routine
                            ),
                        origin =
                            ApplicationCatalogOrigin.DATABASE_ANALYSIS,
                        dataObjectKind =
                            ApplicationCatalogDataObjectKind.STORED_PROCEDURE,
                        qualifiedName =
                            routine.qualifiedName,
                        routineLanguage =
                            routine.language,
                        evidenceClassification =
                            ApplicationCatalogEvidenceClassification.OBSERVED
                    )

                relationships +=
                    containsRelationship(
                        parentNodeId =
                            schemaNodeId,
                        childNodeId =
                            routineNodeId,
                        order =
                            schemaChildOrder++
                    )
            }
        }


        // ========================================================
        // DATABASE DEPENDENCY RELATIONSHIPS
        // ========================================================

        val nodeIdsByQualifiedName =
            nodes
                .mapNotNull { node ->

                    val qualifiedName =
                        node.qualifiedName

                    if (qualifiedName == null) {
                        null
                    } else {
                        qualifiedName to node.nodeId
                    }
                }
                .toMap()

        fun nodeIdForDependencyObject(
            dependency: DiscoveredDependency,
            source: Boolean
        ): String? {

            val schemaName =
                if (source) {
                    dependency.sourceSchemaName
                } else {
                    dependency.targetSchemaName
                }

            val objectName =
                if (source) {
                    dependency.sourceObjectName
                } else {
                    dependency.targetObjectName
                }

            val objectKind =
                if (source) {
                    dependency.sourceObjectKind
                } else {
                    dependency.targetObjectKind
                }

            val subObjectName =
                if (source) {
                    dependency.sourceSubObjectName
                } else {
                    dependency.targetSubObjectName
                }

            val identityArguments =
                if (source) {
                    dependency.sourceIdentityArguments
                } else {
                    dependency.targetIdentityArguments
                }

            val baseQualifiedName =
                "${dependency.databaseName}." +
                    "$schemaName." +
                    objectName

            if (
                objectKind ==
                DiscoveredDependencyObjectKind.TABLE &&
                !subObjectName.isNullOrBlank()
            ) {

                return nodeIdsByQualifiedName[
                    "$baseQualifiedName.$subObjectName"
                ]
            }

            if (
                objectKind ==
                DiscoveredDependencyObjectKind.FUNCTION
            ) {

                if (identityArguments != null) {

                    val routineQualifiedName =
                        "${dependency.databaseName}." +
                            "$schemaName." +
                            "$objectName($identityArguments)"

                    return nodeIdsByQualifiedName[
                        routineQualifiedName
                    ]
                }

                val matchingFunctions =
                    functions.filter {
                        it.schemaName == schemaName &&
                            it.routineName == objectName
                    }

                if (matchingFunctions.size != 1) {
                    return null
                }

                return nodeIdsByQualifiedName[
                    matchingFunctions.single().qualifiedName
                ]
            }

            if (
                objectKind ==
                DiscoveredDependencyObjectKind.STORED_PROCEDURE
            ) {

                if (identityArguments != null) {

                    val routineQualifiedName =
                        "${dependency.databaseName}." +
                            "$schemaName." +
                            "$objectName($identityArguments)"

                    return nodeIdsByQualifiedName[
                        routineQualifiedName
                    ]
                }

                val matchingProcedures =
                    storedProcedures.filter {
                        it.schemaName == schemaName &&
                            it.routineName == objectName
                    }

                if (matchingProcedures.size != 1) {
                    return null
                }

                return nodeIdsByQualifiedName[
                    matchingProcedures.single().qualifiedName
                ]
            }

            return nodeIdsByQualifiedName[
                baseQualifiedName
            ]
        }

        for (dependency in dependencies) {

            // Declared foreign keys are already represented by the physical
            // foreign-key node, its ordered INCLUDES edges, and REFERENCES_KEY.
            // Do not duplicate that fact as a column-to-column DEPENDS_ON edge.
            if (dependency.dependencyKind == DiscoveredDependencyKind.FOREIGN_KEY) {
                continue
            }

            val sourceNodeId =
                nodeIdForDependencyObject(
                    dependency = dependency,
                    source = true
                )

            val targetNodeId =
                nodeIdForDependencyObject(
                    dependency = dependency,
                    source = false
                )

            if (
                sourceNodeId != null &&
                targetNodeId != null &&
                sourceNodeId != targetNodeId
            ) {

                val relationshipKind =
                    when (dependency.dependencyKind) {

                        DiscoveredDependencyKind.VIEW_REFERENCE,
                        DiscoveredDependencyKind.MATERIALIZED_VIEW_REFERENCE ->
                            ApplicationCatalogRelationshipKind.DERIVED_FROM

                        DiscoveredDependencyKind.ROUTINE_REFERENCE ->
                            ApplicationCatalogRelationshipKind.USES_DATA

                        DiscoveredDependencyKind.SEQUENCE_REFERENCE,
                        DiscoveredDependencyKind.OTHER ->
                            ApplicationCatalogRelationshipKind.DEPENDS_ON

                        DiscoveredDependencyKind.FOREIGN_KEY ->
                            error("Foreign-key dependencies are handled by physical FK semantics")
                    }

                relationships +=
                    applicationRelationship(
                        sourceNodeId =
                            sourceNodeId,
                        kind =
                            relationshipKind,
                        targetNodeId =
                            targetNodeId
                    )
            }
        }


        // ========================================================
        // RETURN CATALOG
        // ========================================================

        return ApplicationCatalog(
            nodes =
                nodes.distinctBy {
                    it.nodeId
                },
            relationships =
                relationships.distinctBy {
                    it.relationshipId
                }
        )
    }


    // ============================================================
    // ROUTINE DISPLAY NAME
    // ============================================================

    private fun routineDisplayName(
        routine: DiscoveredRoutine
    ): String {

        return if (
            routine.identityArguments.isBlank()
        ) {

            routine.routineName

        } else {

            "${routine.routineName}(" +
                "${routine.identityArguments})"
        }
    }


    // ============================================================
    // TABLE LOOKUP KEY
    // ============================================================

    private fun tableKey(
        schemaName: String,
        tableName: String
    ): String {

        return "$schemaName.$tableName"
    }


    // ============================================================
    // CONSTRAINT LOOKUP KEY
    // ============================================================

    private fun constraintKey(
        schemaName: String,
        tableName: String,
        constraintName: String
    ): String {

        return "$schemaName.$tableName.$constraintName"
    }


    // ============================================================
    // CONTAINS
    // ============================================================

    private fun containsRelationship(
        parentNodeId: String,
        childNodeId: String,
        order: Int
    ): ApplicationCatalogRelationship {

        return ApplicationCatalogRelationship(
            relationshipId =
                stableRelationshipId(
                    parentNodeId,
                    ApplicationCatalogRelationshipKind.CONTAINS,
                    childNodeId
                ),
            sourceNodeId =
                parentNodeId,
            kind =
                ApplicationCatalogRelationshipKind.CONTAINS,
            targetNodeId =
                childNodeId,
            origin =
                ApplicationCatalogOrigin.DATABASE_ANALYSIS,
            order =
                order,
            evidenceClassification =
                ApplicationCatalogEvidenceClassification.OBSERVED
        )
    }


    // ============================================================
    // INCLUDES
    // ============================================================

    private fun includesRelationship(
        sourceNodeId: String,
        targetNodeId: String,
        order: Int
    ): ApplicationCatalogRelationship {

        return ApplicationCatalogRelationship(
            relationshipId =
                stableRelationshipId(
                    sourceNodeId,
                    ApplicationCatalogRelationshipKind.INCLUDES,
                    targetNodeId
                ),
            sourceNodeId =
                sourceNodeId,
            kind =
                ApplicationCatalogRelationshipKind.INCLUDES,
            targetNodeId =
                targetNodeId,
            origin =
                ApplicationCatalogOrigin.DATABASE_ANALYSIS,
            order =
                order,
            evidenceClassification =
                ApplicationCatalogEvidenceClassification.OBSERVED
        )
    }


    // ============================================================
    // GENERAL RELATIONSHIP
    // ============================================================

    private fun applicationRelationship(
        sourceNodeId: String,
        kind: ApplicationCatalogRelationshipKind,
        targetNodeId: String
    ): ApplicationCatalogRelationship {

        return ApplicationCatalogRelationship(
            relationshipId =
                stableRelationshipId(
                    sourceNodeId,
                    kind,
                    targetNodeId
                ),
            sourceNodeId =
                sourceNodeId,
            kind =
                kind,
            targetNodeId =
                targetNodeId,
            origin =
                ApplicationCatalogOrigin.DATABASE_ANALYSIS,
            evidenceClassification =
                ApplicationCatalogEvidenceClassification.OBSERVED
        )
    }


    // ============================================================
    // STABLE NODE ID
    // ============================================================

    private fun stableNodeId(
        type: String,
        qualifiedName: String
    ): String {

        return "db:$type:${
            sha256(
                "$type|$qualifiedName"
            )
                .take(24)
        }"
    }


    // ============================================================
    // STABLE RELATIONSHIP ID
    // ============================================================

    private fun stableRelationshipId(
        sourceNodeId: String,
        kind: ApplicationCatalogRelationshipKind,
        targetNodeId: String
    ): String {

        return "rel:${
            sha256(
                "$sourceNodeId|" +
                    "${kind.name}|" +
                    targetNodeId
            )
                .take(24)
        }"
    }


    // ============================================================
    // SHA-256
    // ============================================================

    private fun sha256(
        value: String
    ): String {

        val digest =
            MessageDigest
                .getInstance(
                    "SHA-256"
                )
                .digest(
                    value.toByteArray(
                        Charsets.UTF_8
                    )
                )

        return digest.joinToString(
            ""
        ) {
            "%02x".format(it)
        }
    }
}
