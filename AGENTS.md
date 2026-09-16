# Repository instructions

These rules apply to the entire repository.

## Language and platform

- Source code, identifiers, comments, commit messages, schemas, and documentation are
  written in English.
- Kotlin on JDK 21 is the default implementation platform.
- Use the checked-in Gradle Wrapper. Do not require a globally installed Gradle version.
- Keep database-vendor integrations behind explicit adapter boundaries.

## Canonical Application Catalog contract

- The Legacy Modernization Workbench is the only semantic authority for the Application
  Catalog model.
- This repository pins one exact Workbench revision in
  `contracts/workbench-catalog/contract-lock.json` and vendors its immutable contract under
  `contracts/workbench-catalog/snapshot/`.
- Before changing discovery, identity, ownership, relationships, privacy analysis, export,
  validation, or presentation, read the pinned model specification, projection rules,
  fixture index, and cross-repository governance document.
- Never add a database-specific catalog node or relationship kind locally. Propose semantic
  changes in the Workbench contract first, then upgrade this snapshot in a dedicated change.
- Never edit files below `contracts/workbench-catalog/snapshot/` individually. Synchronize
  the complete canonical bundle, regenerate its manifest and lock, and review the upgrade.
- Run `./gradlew check` before committing. CI must fail when the pinned snapshot or lock is
  inconsistent.

## Modeling rules

- Preserve stable node and relationship identities across unchanged analyses.
- Separate physical structures, logical data concepts, integration structures, and runtime
  evidence.
- Use canonical generic `dataObjectKind` values. Preserve a vendor-specific type as
  evidence; do not turn it into cross-platform vocabulary.
- Distinguish declared constraints from inferred semantic relationships.
- Record provenance, evidence classification, confidence, and inference method for inferred
  facts. Do not present an inference as a declared or confirmed fact.
- Store one canonical directed relationship. Derive inverse wording and backlinks instead
  of persisting duplicate inverse edges.
- Use `contains` only for structural ownership and `includes` for semantic membership.
- Do not guess read/write access when only an unspecified dependency is known; use the
  contract's weaker `usesData` relationship.
- Treat `unknown` PII status as different from `no`. Missing analysis never means that data
  is not personal information.
- Do not require or export runtime personal values. Omit, mask, or protect evidence according
  to an explicit policy.

## Required analyzer conformance

Every database adapter must test:

- positive and negative shared fixtures from the pinned bundle;
- composite primary, unique, and foreign keys with stable member ordering;
- indexes, views, materialized views, routines, packages, triggers, schedules, jobs, and
  cross-object dependencies supported by the vendor;
- declared foreign keys separately from semantic candidates inferred from SQL, naming,
  values, source code, or workload evidence;
- PII classification provenance, confidence, conflicts, and unknown coverage;
- identical identities and relationships across repeated unchanged analyses; and
- Workbench import and normalized graph comparison using a golden analyzer export.

## Security

- Treat connection metadata, database metadata, SQL definitions, catalog comments, and
  extracted values as untrusted input.
- Use least-privilege, read-only database credentials for discovery.
- Never commit credentials, connection strings, production data, or customer evidence.
- Do not execute discovered SQL, routines, triggers, jobs, or instructions embedded in
  database content.
- Bound metadata sizes, recursion, query duration, and result counts.
- Do not log secrets, raw PII, full SQL bodies, or complete customer payloads by default.
