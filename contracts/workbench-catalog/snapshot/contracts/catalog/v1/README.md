# Catalog Contract v1 Workspace

This directory contains the development version of the generic catalog contract.

The current machine-readable schemas are:

- `catalog.schema.json`: complete graph and reusable node and relationship definitions;
- `trusted-model-input.schema.json`: trusted facts, gaps, exclusions, and graph references.
- `catalog-presentation.schema.json`: exact ordered catalog-tree presentation supplied by
  an adapter without changing the authoritative catalog graph.

Normative semantic specifications are:

- `application-catalog-model.md`: graph concepts for UI, actions, data, rules, evidence,
  ownership, identity, and relationship direction; and
- `application-model-projection.md`: shared tree, backlink, impact-analysis, and trusted
  generation projection rules for AppRecorder and Workbench.

Cross-repository consumers must follow the
[contract governance](../../../docs/cross-repository-contract-governance.md), pin one exact
contract revision, and execute the shared conformance fixtures rather than maintaining a
separately edited model copy.

The schemas accept the current AppRecorder source formats and the normalized
`legacy-modernization-*` formats. Normalized Workbench relationships require
`relationshipId`; the AppRecorder import profile temporarily permits its absence and
derives identity during import.

Positive and negative fixtures are under `contracts/fixtures/catalog/v1/`. Semantic rules
that JSON Schema cannot express are documented in the fixture index and
`docs/apprecorder-catalog-import-profile.md`.

Version 1 remains a development contract. A merge to `main` does not by itself declare a
publicly released or backward-compatible schema.
