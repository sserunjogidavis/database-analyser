package com.alpinedigitalexperts.databaseanalyser

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// ============================================================
// APPLICATION CATALOG ROOT
// ============================================================

@Serializable
data class ApplicationCatalog(

    val schemaVersion: Int = 1,

    val format: ApplicationCatalogFormat =
        ApplicationCatalogFormat.LEGACY_MODERNIZATION_CATALOG,

    val nodes: List<ApplicationCatalogNode>,

    val relationships: List<ApplicationCatalogRelationship>
)


// ============================================================
// CATALOG FORMAT
// ============================================================

@Serializable
enum class ApplicationCatalogFormat {

    @SerialName("legacy-modernization-catalog")
    LEGACY_MODERNIZATION_CATALOG,

    @SerialName("apprecorder-catalog")
    APPRECORDER_CATALOG
}


// ============================================================
// CATALOG NODE
// ============================================================

@Serializable
data class ApplicationCatalogNode(

    val nodeId: String,

    val nodeType: ApplicationCatalogNodeType,

    val displayName: String,

    val origin: ApplicationCatalogOrigin,

    val dataObjectKind:
    ApplicationCatalogDataObjectKind? = null,

    val qualifiedName: String? = null,

    val dataType: String? = null,

    val isNullable: Boolean? = null,

    val isKey: Boolean? = null,

    val databaseVendorType: String? = null,

    val isUnique: Boolean? = null,

    val isClustered: Boolean? = null,

    val isDeclared: Boolean? = null,

    val constraintEnforcement:
    ApplicationCatalogConstraintEnforcement? = null,

    val routineLanguage: String? = null,

    val description: String? = null,

    val sourceReference: String? = null,

    val adapterId: String? = null,

    val evidenceClassification:
    ApplicationCatalogEvidenceClassification? = null
)


// ============================================================
// NODE TYPE
// ============================================================

@Serializable
enum class ApplicationCatalogNodeType {

    @SerialName("dataObject")
    DATA_OBJECT,

    @SerialName("dataRelationship")
    DATA_RELATIONSHIP,

    @SerialName("privacyClassification")
    PRIVACY_CLASSIFICATION,

    @SerialName("businessRule")
    BUSINESS_RULE,

    @SerialName("specification")
    SPECIFICATION,

    @SerialName("observation")
    OBSERVATION
}


// ============================================================
// NODE ORIGIN
// ============================================================

@Serializable
enum class ApplicationCatalogOrigin {

    @SerialName("databaseAnalysis")
    DATABASE_ANALYSIS,

    @SerialName("sourceCodeAnalysis")
    SOURCE_CODE_ANALYSIS,

    @SerialName("businessAnalysis")
    BUSINESS_ANALYSIS,

    @SerialName("manualAddition")
    MANUAL_ADDITION,

    @SerialName("manualImport")
    MANUAL_IMPORT,

    @SerialName("projectKnowledge")
    PROJECT_KNOWLEDGE,

    @SerialName("importedCatalog")
    IMPORTED_CATALOG
}


// ============================================================
// DATABASE / DATA OBJECT KIND
// ============================================================

@Serializable
enum class ApplicationCatalogDataObjectKind {

    @SerialName("dataSource")
    DATA_SOURCE,

    @SerialName("namespace")
    NAMESPACE,

    @SerialName("database")
    DATABASE,

    @SerialName("schema")
    SCHEMA,

    @SerialName("table")
    TABLE,

    @SerialName("view")
    VIEW,

    @SerialName("column")
    COLUMN,

    @SerialName("materializedView")
    MATERIALIZED_VIEW,

    @SerialName("index")
    INDEX,

    @SerialName("key")
    KEY,

    @SerialName("primaryKey")
    PRIMARY_KEY,

    @SerialName("uniqueKey")
    UNIQUE_KEY,

    @SerialName("foreignKey")
    FOREIGN_KEY,

    @SerialName("checkConstraint")
    CHECK_CONSTRAINT,

    @SerialName("defaultConstraint")
    DEFAULT_CONSTRAINT,

    @SerialName("partition")
    PARTITION,

    @SerialName("sequence")
    SEQUENCE,

    @SerialName("storedProcedure")
    STORED_PROCEDURE,

    @SerialName("function")
    FUNCTION,

    @SerialName("package")
    PACKAGE,

    @SerialName("trigger")
    TRIGGER,

    @SerialName("job")
    JOB,

    @SerialName("schedule")
    SCHEDULE,

    @SerialName("task")
    TASK,

    @SerialName("synonym")
    SYNONYM,

    @SerialName("entity")
    ENTITY,

    @SerialName("field")
    FIELD,

    @SerialName("dataGroup")
    DATA_GROUP,

    @SerialName("recordType")
    RECORD_TYPE,

    @SerialName("api")
    API,

    @SerialName("file")
    FILE,

    @SerialName("message")
    MESSAGE,

    @SerialName("queue")
    QUEUE,

    @SerialName("operation")
    OPERATION,

    @SerialName("other")
    OTHER
}


// ============================================================
// CATALOG RELATIONSHIP
// ============================================================

@Serializable
data class ApplicationCatalogRelationship(

    val relationshipId: String,

    val sourceNodeId: String,

    val kind: ApplicationCatalogRelationshipKind,

    val targetNodeId: String,

    val origin: ApplicationCatalogOrigin,

    val order: Int? = null,

    val evidenceClassification:
    ApplicationCatalogEvidenceClassification? = null,

    val confidence: Double? = null,

    val inferenceMethod: String? = null,

    val role: String? = null,

    val cardinality: String? = null,

    val condition: String? = null
)


// ============================================================
// RELATIONSHIP KIND
// ============================================================

@Serializable
enum class ApplicationCatalogRelationshipKind {

    @SerialName("contains")
    CONTAINS,

    @SerialName("includes")
    INCLUDES,

    @SerialName("references")
    REFERENCES,

    @SerialName("dependsOn")
    DEPENDS_ON,

    @SerialName("readsData")
    READS_DATA,

    @SerialName("writesData")
    WRITES_DATA,

    @SerialName("createsData")
    CREATES_DATA,

    @SerialName("deletesData")
    DELETES_DATA,

    @SerialName("usesData")
    USES_DATA,

    @SerialName("indexes")
    INDEXES,

    @SerialName("referencesKey")
    REFERENCES_KEY,

    @SerialName("hasSourceMember")
    HAS_SOURCE_MEMBER,

    @SerialName("hasTargetMember")
    HAS_TARGET_MEMBER,

    @SerialName("classifies")
    CLASSIFIES,

    @SerialName("firesOn")
    FIRES_ON,

    @SerialName("schedules")
    SCHEDULES,

    @SerialName("executes")
    EXECUTES,

    @SerialName("returnsData")
    RETURNS_DATA,

    @SerialName("derivedFrom")
    DERIVED_FROM
}


// ============================================================
// EVIDENCE CLASSIFICATION
// ============================================================

@Serializable
enum class ApplicationCatalogEvidenceClassification {

    @SerialName("observed")
    OBSERVED,

    @SerialName("imported")
    IMPORTED,

    @SerialName("customerConfirmed")
    CUSTOMER_CONFIRMED,

    @SerialName("consultantConfirmed")
    CONSULTANT_CONFIRMED,

    @SerialName("assumption")
    ASSUMPTION,

    @SerialName("inferred")
    INFERRED,

    @SerialName("proposed")
    PROPOSED,

    @SerialName("machineSuggestion")
    MACHINE_SUGGESTION
}


// ============================================================
// CONSTRAINT ENFORCEMENT
// ============================================================

@Serializable
enum class ApplicationCatalogConstraintEnforcement {

    @SerialName("enforced")
    ENFORCED,

    @SerialName("notEnforced")
    NOT_ENFORCED,

    @SerialName("unknown")
    UNKNOWN
}
