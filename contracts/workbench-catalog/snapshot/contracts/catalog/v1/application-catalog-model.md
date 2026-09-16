# Application Catalog Model v1

## 1. Scope and terminology

This document specifies the semantic model of the **Application Catalog**. It is normative
for adapters, the Workbench, the AppRecorder, and later model-input generation. The JSON
representation is defined by `catalog.schema.json`; this document defines meaning and legal
interpretation where JSON Schema alone is insufficient.

The catalog is a directed, typed graph. It is not a serialized editor tree. A tree shown by
an **Application Model Explorer** is a deterministic projection of the graph as specified in
`application-model-projection.md`.

Every catalog node has one stable identity. A node may be visible in several contextual
places, but those appearances are references to the same node and never independent copies.
Display IDs, titles, coordinates, array positions, localized strings, runtime values, and
file names are not identities.

## 2. Knowledge layers

The graph can contain application structure, behavior, data, rules, evidence, specifications,
and planned target components. Each fact retains its origin and evidence classification.
Observed evidence, human interpretation, and derived suggestions MUST NOT overwrite one
another implicitly. Runtime state and values MUST NOT cause structural identity churn.

The following distinctions are mandatory:

- a reusable definition is different from one runtime instance;
- a UI element is different from the action it invokes;
- a data definition is different from a runtime value;
- structural ownership is different from a non-hierarchical semantic relationship; and
- an observed fact is different from a confirmed business interpretation.

## 3. UI structure

### 3.1 Application and windows

An `application` contains one or more `applicationWindow` nodes. Application windows contain
their structural regions. A typical window projects menu bar, toolbar, workspace, status
area, and top-level window commands as siblings. A top-level Close command belongs to the
window. It terminates the application only when application behavior establishes that the
closed window is the last relevant window.

A `workspace` contains the working regions of the window. It may contain primary content,
docked regions, and one or more `viewGroup` nodes. Commands such as creating a tab, closing
the active tab, or opening a tab list belong to the workspace or view group, not to the
top-level window.

### 3.2 Views, instances, and tabs

A `logicalView` describes a reusable semantic view. A `viewInstance` is one concrete open
instance, such as a document tab for one file or a view for one business record. The
relationship `instanceOf` links the instance to its definition.

A tab is an `element` with `catalogKind: tab`. It is structurally owned by its `viewGroup`,
represents a `viewInstance`, and invokes an action whose scope is normally `viewInstance`.
Switching tabs changes the active instance; it does not create a new logical view.

Consequently, multiple captured windows with their own content tabs MUST NOT automatically
be interpreted as multiple logical screens. For the current Notepad++ profile, identifiers
such as `SCR-0001` through `SCR-0007` represent open view instances unless additional
evidence establishes distinct logical view definitions.

### 3.3 Menus, toolbars, commands, and actions

Application menu and toolbar structures are peers of the workspace. They MUST NOT be owned
by the currently active view instance merely because their action affects it. Arbitrary menu
depth is represented through `contains`; the terminal command invokes a separate canonical
`action`.

Menus, toolbar buttons, ordinary buttons, tabs, keyboard shortcuts, and other command
surfaces may invoke the same action. The action is modeled once. `actionScope` states what
it operates on: application, application window, workspace, view group, view instance,
selection, dialog, external system, background context, or another explicitly described
scope. `appliesTo` may identify a concrete target when scope alone is insufficient.

Dialogs, menus, context menus, popups, and notifications are transient surfaces. They are
not logical views solely because they have a screenshot. Controls inside a dialog are owned
by that dialog; their invoked actions remain independent graph nodes.

### 3.4 Generic UI elements

`uiElementFamily` gives a small stable classification and `uiElementRole` supplies an
extensible semantic role.

| Family | Typical roles |
| --- | --- |
| `input` | text input, filter input, date input, numeric input |
| `selection` | checkbox, radio button, select list, list item, tree item, tab |
| `command` | button, menu command, toolbar command, link, window command |
| `display` | label, dynamic text, image, progress value, formatted result |
| `collection` | table, list, tree, grid, option collection |
| `structure` | panel, field group, tab strip, separator, region |
| `feedback` | status text, validation message, notification, progress indicator |
| `other` | evidence-backed role not covered above |

The role is language-neutral persisted data, not a translated display label. Dynamic status
text, list rows, transient choices, and current field values are runtime state or evidence.
They MUST NOT create new structural nodes on each observation. Static business choices may
be catalog definitions when their stable semantics matter.

## 4. Structural ownership and inference

Each structurally displayed UI element has at most one canonical incoming `contains`
relationship. Reusable definitions and their occurrences are linked with `instanceOf` or
`represents`; they are not assigned multiple ownership parents.

Adapters SHOULD infer ownership from the strongest available sources in this order:

1. explicit application semantics or durable adapter identifiers;
2. operating-system or automation parent/container hierarchy;
3. window, region, and view-group association;
4. stable structural paths; and
5. geometry only as supporting evidence.

Geometry alone MUST NOT establish ownership. An inferred relationship records origin,
evidence classification, and, when available, `confidence` and `inferenceMethod`. If the
parent cannot be established safely, the adapter preserves an unresolved reference or gap;
it does not guess. Human correction creates a curated relationship while retaining the
original observation for audit.

## 5. Actions and behavior

An action represents application behavior independently of its trigger. It may read, create,
change, delete, filter, sort, validate, or transform data; change UI state; launch an external
UI; start background work; or produce another result. Execution status and observed outcome
are evidence about the action, not its identity.

Business requirements and descriptions target the action when they specify behavior. A
description of a button's appearance targets the button. This distinction permits the same
action to be discovered through several command surfaces without duplicating requirements.

## 6. Business transactions and execution traces

### 6.1 Definition model

A `businessTransaction` describes a bounded business goal such as *Create customer*,
*Contact customer*, *Approve order*, or *Reconcile payment*. It is the business context that
connects UI, actions, data, rules, roles, events, evidence, and specifications. It is not a
synonym for a single button click or technical database transaction.

A transaction has one or more `businessScenario` variants. Scenarios distinguish the main
path, alternatives, and exceptions. A scenario has reusable `processStep` definitions.
Steps form a directed process graph: `precedes` expresses ordinary flow and `branchesTo`
expresses conditional alternatives. Relationship `condition` records a branch condition.
Loops point back to an existing step; steps MUST NOT be cloned to force the flow into a tree.

A step may be performed by a `businessRole`, use applications and UI elements, invoke
canonical actions, read or change data, be governed by rules, and produce a `businessEvent`.
Preconditions, postconditions, requirements, and acceptance criteria attach at transaction,
scenario, or step level according to their actual scope.

### 6.2 Observed executions

An `executionTrace` is one immutable observed performance of a transaction or scenario. It
records whether execution was automatic, assisted, manual, or mixed, along with status,
time range, environment provenance, and actor classification. An `executionStep` records one
ordered observed interaction or system event. It may link to the process step it
`observedAs`, the action it invoked, UI it used, data it affected, and evidence captured at
that moment.

Definitions and traces MUST remain separate. Repeating the same transaction creates another
trace, not another business transaction. A trace may reveal an unmapped step, alternative,
failure, or gap; analysis can then curate the definition without rewriting the trace.

AppRecorder static exploration supplies UI structure. A controlled automatic run on a test
system or a user-led recording supplies an execution trace. Screenshots, UI observations,
input/output observations, and technical events attach to the applicable execution step.
Sensitive runtime values are omitted, masked, or retained only under explicit data policy.

### 6.3 Migration and generation relevance

Migration coverage is measured per transaction and scenario, not only per UI element. For
a 1:1 migration, observed main, alternative, and exception paths remain traceable to their
UI, actions, data effects, and rules. Functional improvements are separate future
specifications and MUST NOT silently alter captured legacy behavior.

Generation consumes trusted transaction definitions plus linked actions, data, rules,
acceptance criteria, and representative evidence. It does not infer an entire business
process from a static UI tree or treat one trace as proof that no other variant exists.

## 7. Data model

### 7.1 Layers

Data is represented at four layers:

| Layer | Examples |
| --- | --- |
| `physical` | database, schema, table, view, column, key, index, routine, trigger, job |
| `logical` | business entity, logical field, data group, record type |
| `integration` | API, operation, file, message |
| `runtime` | observed record, field value, request, response, current filter value |

Runtime values are immutable evidence when retained. They are not catalog definitions and
MUST be handled according to data classification and privacy policy.

### 7.2 Logical and physical structures

Physical containment follows the source technology: for example, a database contains a
schema, a schema contains tables, and a table contains columns and keys. A physical column
has one owning table in a given physical model.

Indexes, primary keys, unique keys, foreign keys, check constraints, and default constraints
are physical objects owned by their table or schema. They are not children of a logical
`dataGroup`. A key or index uses ordered `includes` relationships to its columns. A foreign
key uses `referencesKey` for its target primary or unique key, which supports composite keys
without inventing pairwise constraints.

A logical field expresses business meaning independently of storage. Several physical
columns may `mapTo` one logical field. A `dataGroup` is a business-oriented grouping and may
`include` logical fields sourced from several entities, record types, tables, APIs, or files.
It therefore MUST NOT be forced into a physical containment tree.

`includes` is a semantic membership edge; `contains` is canonical structural ownership.
This distinction permits, for example, the logical field *Customer number* to be included in
several record types without cloning it.

### 7.3 Executable and operational database objects

The physical model also includes materialized views, sequences, stored procedures,
functions, packages or modules, triggers, jobs, schedules, tasks, synonyms, and queues.
Database-specific objects use the closest generic `dataObjectKind`. Unsupported constructs
use `other` and retain their original `databaseVendorType`; a vendor name MUST NOT become a
new cross-platform node type.

Packages contain routines. Routines may `invokes` other routines and use `readsData`,
`writesData`, `createsData`, `deletesData`, `usesData`, or `returnsData` according to proven
behavior. A trigger `firesOn` a table or data event and may invoke routines or affect data.
A schedule `schedules` a job or task, and a job `executes` a task, routine, or integration.
Views and materialized views use `derivedFrom`, field mappings, and read dependencies.

When an analyzer knows only that an executable object depends on data but cannot establish
access mode, it uses `usesData`. It MUST NOT guess a read or write relationship.

### 7.4 Declared and semantic data relationships

A declared database foreign key is a physical `dataObject` with kind `foreignKey`,
`isDeclared: true`, and observed provenance. A relationship inferred from SQL joins, names,
values, source code, or workload behavior is a separate `dataRelationship` node. It uses
ordered `hasSourceMember` and `hasTargetMember` edges so composite candidates remain one
relationship.

An inferred relationship records evidence classification, method, confidence, and source
evidence. Human confirmation changes its knowledge status in a later revision; it does not
turn the candidate into a declared database constraint. Rejection also remains auditable.

### 7.5 Privacy and PII classification

General `dataClassification` expresses confidentiality or sensitivity and is distinct from
PII status. A `privacyClassification` node records one independently attributable assessment
and points to the classified logical or physical data through `classifies`. It contains:

- `piiStatus`: yes, no, unknown, or not applicable;
- PII categories such as direct identifier, contact, financial, health, biometric,
  credential, or indirect identifier;
- `specialCategoryPiiStatus`;
- assessment state, method, rationale, provenance, and confidence; and
- whether masking is required, not required, conditional, or unknown.

`unknown` is the safe default. Missing analysis MUST NOT be interpreted as `no`. Field and
column classifications are primary. A table, view, data group, API, file, message, process,
or business transaction derives `yes` when any reachable used field is classified `yes`.
It derives `no` only when every relevant member has a confirmed `no`; otherwise it remains
`unknown`. Derived roll-ups display their complete paths and are not stored as independent
confirmed assessments.

Mappings between logical and physical fields propagate visibility of classification, not
authority. Conflicting assessments remain visible for resolution. Runtime PII values are
never required in the catalog and must be omitted, masked, or separately protected.

### 7.6 UI and behavior bindings

UI and actions relate to data through explicit edges:

- `displaysData` for output;
- `editsData` for user-editable content;
- `readsData`, `writesData`, `createsData`, and `deletesData` for behavior;
- `filtersData` and `sortsData` for collection operations;
- `mapsTo` for logical-to-physical or integration mappings; and
- `transformsData` for transformations.

Field-level bindings SHOULD be recorded when the evidence supports them. A view-level
relationship is allowed when finer granularity is unknown, but MUST NOT be presented as a
confirmed field mapping.

These bindings make privacy and data lineage bidirectional in the explorer. A field shows
the UI, routines, jobs, integrations, process steps, and business transactions that use it;
each of those consumers shows the affected field and its effective privacy classification.

## 8. Rules, validation, security, and transformations

Business rules are first-class nodes. `governedBy` states that an element, action, or data
object is subject to a rule. `validates` identifies what a validation action or rule checks;
`transformsData` identifies transformation inputs or results. Rules can describe validation,
calculation, authorization, workflow, mapping, and constraints.

Security and privacy semantics attach to logical or physical data using
`dataClassification` and governed rules. Access behavior is represented by actions and
authorization rules, not by hiding graph relationships.

## 9. Relationship direction and inverse wording

Only one canonical directed relationship is stored. The Application Model Explorer derives
the inverse human wording. It MUST NOT persist a second inverse edge merely for display.

| Stored kind | Forward wording | Derived inverse wording |
| --- | --- | --- |
| `contains` | contains | is part of |
| `instanceOf` | is instance of | has instance |
| `represents` | represents | is represented by |
| `invokes` | invokes | is invoked by |
| `appliesTo` | applies to | is target of |
| `includes` | includes | is included in |
| `mapsTo` | maps to | is implemented/mapped by |
| `displaysData` | displays | is displayed by |
| `editsData` | edits | is edited by |
| `readsData` | reads | is read by |
| `writesData` | writes | is written by |
| `filtersData` | filters | is filtered by |
| `references` | references | is referenced by |
| `governedBy` | is governed by | governs |
| `validates` | validates | is validated by |
| `hasEvidence` | has evidence | is evidence for |

The remaining v1 kinds use the same rule:

| Stored kind | Forward wording | Derived inverse wording |
| --- | --- | --- |
| `representsState` | represents state | has state representation |
| `hasAttachment` | has attachment | is attached to |
| `hasAnnotation` | has annotation | annotates |
| `specifies` | specifies | is specified by |
| `requiresCreationOf` | requires creation of | is required by |
| `constrains` | constrains | is constrained by |
| `dependsOn` | depends on | is dependency of |
| `producesResult` | produces result | is produced by |
| `triggeredBy` | is triggered by | triggers |
| `controlsState` | controls state of | has state controlled by |
| `createsData` | creates | is created by |
| `deletesData` | deletes | is deleted by |
| `sortsData` | sorts | is sorted by |
| `transformsData` | transforms | is transformed by |
| `derivedFrom` | is derived from | is source of derivation |
| `providedBy` | is provided by | provides |
| `hasScenario` | has scenario | is scenario of |
| `hasStep` | has step | is step of |
| `precedes` | precedes | follows |
| `branchesTo` | branches to | is branch from |
| `performedBy` | is performed by | performs |
| `usesApplication` | uses application | is used by |
| `usesUi` | uses UI | is used by process step |
| `demonstrates` | demonstrates | is demonstrated by |
| `observedAs` | is observed as | is definition of observation |
| `indexes` | indexes | is indexed by |
| `referencesKey` | references key | is referenced by foreign key |
| `firesOn` | fires on | has trigger |
| `schedules` | schedules | is scheduled by |
| `executes` | executes | is executed by |
| `returnsData` | returns data | is returned by |
| `usesData` | uses data | is used by |
| `hasSourceMember` | has source member | is source member of |
| `hasTargetMember` | has target member | is target member of |
| `classifies` | classifies | is classified by |

Direct relationships and paths derived by graph traversal are different facts. A derived
path records or displays every hop and MUST NOT masquerade as a direct confirmed edge.

For example, *Create customer* directly exposes its scenarios, roles, applications, and
resulting event. Its steps expose invoked actions and affected data. Conversely, the
logical field *Customer number* exposes incoming write relationships and derived paths back
to the scenario and transaction. Both directions are navigable even though only the
canonical forward edges are persisted.

## 10. Stability and compatibility

Adapters preserve stable identities across unchanged exports. Navigation, selection,
timestamps, array ordering, current values, or presentation changes MUST NOT generate new
catalog identities or relationships. A semantic edit changes only the affected nodes and
edges plus deterministic derived projections.

Additive v1 fields and enum members may be introduced while v1 is a development contract.
Once v1 is declared released, incompatible identity or semantic changes require a new
contract version and explicit migration rules.

## 11. Conformance

Conforming implementations MUST validate the JSON Schema and semantic graph rules. The
positive fixture `application-model-graph.json` demonstrates UI ownership, action reuse,
logical and physical data, a business transaction, a recorded execution trace, inferred
mappings, and rules. Negative fixtures establish stable diagnostics for invalid vocabulary
or topology.

The same conformance fixtures MUST be used by AppRecorder export tests, Workbench import
tests, explorer projection tests, and any later trusted generation projection.
