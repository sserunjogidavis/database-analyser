# Application Model Explorer Projection v1

## 1. Purpose

This document defines the deterministic presentation rules for an **Application Model
Explorer**. It is normative for AppRecorder and Workbench tree views and for bounded model
projections supplied to later generation stages. Visual styling may differ; semantic
placement, references, and paths may not.

The graph in `catalog.schema.json` is authoritative. A producer-supplied
`catalog-presentation.json` is a cached projection and may add synthetic groups, but it MUST
conform to these rules and MUST NOT redefine graph ownership.

The explorer provides three primary semantic lenses over the same graph: **UI**,
**Business transactions**, and **Data**. These are not separate models. Cross-links and
backlinks always navigate to the same stable nodes.

## 2. Canonical structural tree

The primary tree follows canonical `contains` relationships. Siblings use explicit `order`
when present, followed by stable node identity as the deterministic fallback. Labels,
display IDs, source-array positions, pixel coordinates, and translated text MUST NOT decide
ownership or order.

The normal UI projection is:

1. application;
2. application windows;
3. window-level siblings such as window commands, menu, toolbar, workspace, and status;
4. workspace regions and view groups;
5. view-group commands, tabs, and view instances;
6. regions, forms, groups, controls, collections, and feedback owned by those instances;
7. transient surfaces below the structural owner that opened or contains them; and
8. evidence and annotations in dedicated contextual sections, not as false UI children.

An Application Model Explorer MUST present the persisted semantic type of the selected item.
A `viewInstance` MUST NOT be labelled as a logical screen. A menu MUST NOT be nested under
the active view instance when it is application-window structure. A window command and a
workspace command MUST remain distinguishable.

## 3. Definitions, instances, and contextual appearances

Reusable definitions appear at their canonical ownership location or in an explicit
definition group. Instances appear at their structural runtime location and link to their
definition through `instanceOf`. Tabs appear in their view group and link to the instance
they `represent`.

Non-containment relationships do not create a second owning parent. An explorer may show a
contextual reference, but it MUST be visually identifiable as a link and navigate to the
same stable node. Expanding a reference MUST NOT manufacture cloned descendants.

Synthetic headings such as *Window commands*, *Menu*, *Workspace*, *Data*, or *Evidence*
are presentation nodes only. They have no catalog identity unless the graph explicitly
contains the corresponding semantic node.

## 4. Paths and selection

The canonical breadcrumb follows `contains` from the application root to the selected node.
It includes the semantic type and prefers a confirmed business title over an internal or
generated display ID. The ID remains visible as secondary identification.

Selecting an item drives one coherent detail context. The center and right panels MUST show
the same stable node, its canonical path, type, acquisition/execution status, descriptions,
actions, data bindings, evidence, and relationships. Ancestor context is labelled as context
and never presented as though it were owned by the selected child.

## 5. Relationship and backlink view

Every selected node has one relationship view containing all direct incoming and outgoing
edges. The explorer computes backlinks from the graph; nodes do not carry redundant
backlink arrays. For a logical field such as *Customer number*, references are grouped as:

- UI usages;
- logical entities, record types, and data groups;
- physical storage;
- database constraints, routines, triggers, and scheduled execution;
- actions and processes;
- business transactions, scenarios, execution traces, and roles;
- rules and validations;
- APIs and integrations; and
- evidence and provenance.

Each group shows a count. Each entry shows localized forward or inverse wording, target
type, preferred business title, secondary stable ID, confirmation state, and provenance.
Entries navigate to the referenced node.

The view supports filters for incoming/outgoing, direct/derived, read/write, and
confirmed/inferred relationships. A derived reference displays its complete path. The UI
MUST NOT imply that a derived path is a direct relationship.

For a business transaction, the relationship view groups roles, applications, scenarios,
UI, actions, logical and physical data, rules, events, specifications, and recorded
executions. Every referenced UI or data node shows the inverse transaction or step
reference in its own relationship view.

The data lens separates logical data, physical structures, executable database objects,
operational automation, semantic relationship candidates, and privacy classifications.
Selecting a field or column shows all direct and derived usages together, including keys,
indexes, views, routines, triggers, jobs, UI bindings, process steps, business transactions,
integrations, rules, and classifications.

Declared foreign keys and inferred semantic relationships have different labels and
filters. Composite relationships appear once with their ordered source and target members.
An inferred candidate always shows method, confidence, evidence, and review state.

PII is displayed as **Yes**, **No**, **Unknown**, or **Not applicable** using localized UI
labels. Unknown or incomplete coverage must never look like No. Derived table, flow, or
transaction classifications show the contributing fields and paths. Conflicts remain
visible instead of being collapsed into one unexplained badge.

## 6. Business transaction and trace presentation

The transaction lens lists stable business transactions by preferred business title. A
selected transaction shows goal, preconditions, postconditions, roles, applications,
scenario coverage, and resulting business events. Each scenario is rendered as a compact
flow graph or ordered step list that supports branches, loops, and exception paths without
duplicating steps.

A step detail shows its semantic kind, responsible role, used UI, invoked actions, affected
data, rules, evidence, and specifications. Unknown mappings remain visible as coverage gaps.

Recorded executions appear separately from the definition. A trace timeline shows actual
sequence, execution mode, result, timestamps, mapped definition step, screenshots, and other
evidence. Comparing traces with the definition highlights uncovered steps and observed
variants; it does not rewrite either side automatically.

## 7. Impact analysis

Before an element is marked invalid or a manual element is removed, the explorer computes
the affected incoming and outgoing references. It groups direct breakage separately from
derived paths that would disappear. The user receives the affected UI elements, actions,
business transactions, traces, data, rules, integrations, evidence, and specifications
before confirming the change.

Data impact analysis additionally includes dependent views, keys, indexes, routines,
triggers, schedules, jobs, semantic relationship candidates, privacy assessments, masking
requirements, UI bindings, and business transactions.

Observed facts are normally marked invalid rather than physically deleted. Historical
evidence and revision traceability remain intact.

## 8. Search and filtering

Search returns every matching node plus the minimum canonical ancestor chain. Filters may
hide branches temporarily but MUST restore the same structural tree and identities. Search
also covers business titles, display IDs, types, descriptions, qualified data names, and
relationship targets without translating captured application content.

Runtime values are excluded from the structural search index unless a user explicitly
searches evidence. Large collections are virtualized or paged; the projection does not turn
rows or current options into structural nodes.

## 9. Adapter presentation validation

When an adapter supplies `catalog-presentation.json`, import verifies:

- every linked `catalogNodeId` exists;
- a graph node is not duplicated as several owning presentation items;
- linked parentage agrees with canonical `contains` ownership;
- definition/instance and tab/represented-instance links remain visible;
- sibling ordering is deterministic;
- the presentation is connected and acyclic; and
- contextual appearances are marked and behave as references.

An invalid cached projection rejects the complete package atomically. A consumer MUST NOT
silently prefer the malformed presentation or invent a replacement from labels or geometry.

## 10. Generation projection

A later generation prompt consumes a bounded trusted graph projection, not the visual tree
and not a raw recorder archive. The projection includes stable identities, semantic types,
business transactions and covered scenarios, direct relationships, explicitly labelled
derived paths, trusted specifications, provenance, and required evidence references.
Captured text is quoted as data and never gains instruction authority through placement in
the tree.

Raw runtime PII is excluded from generation input by default. The trusted projection may
include classification metadata, logical field meaning, masking requirements, and approved
synthetic examples, but not observed personal values unless an explicit authorized policy
permits them.

## 11. Shared acceptance rules

AppRecorder and Workbench MUST execute the same golden projection scenarios. Given an
identical catalog graph, both show the same canonical parent, semantic type, sibling order,
definition/instance distinction, and relationship targets. Localization may change Workbench
labels but not source content or model vocabulary.

The current AppRecorder tree is a migration source, not the semantic authority. Existing
items incorrectly classified as logical screens, screen-owned application menus, or generic
batch actions require adapter migration to the graph concepts in this specification.
