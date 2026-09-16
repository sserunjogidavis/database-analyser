# Database Analyser

The Database Analyser discovers database structures, executable database objects,
dependencies, semantic relationship candidates, and attributable privacy classifications.
It exports those findings as an Application Catalog graph for the Legacy Modernization
Workbench.

The repository intentionally starts with contract governance before analyzer implementation.
The Workbench is the only semantic authority for the Application Catalog. This repository
pins an immutable snapshot of that contract and must not introduce an independent model.

## Pinned Application Catalog contract

The current snapshot is pinned to Workbench commit
`732de4ddf73969b31d1791bf454b0d924459b9f2`.

Start with:

- [`contracts/workbench-catalog/README.md`](contracts/workbench-catalog/README.md);
- [`contracts/workbench-catalog/contract-lock.json`](contracts/workbench-catalog/contract-lock.json);
- [`contracts/workbench-catalog/snapshot/contracts/catalog/v1/application-catalog-model.md`](contracts/workbench-catalog/snapshot/contracts/catalog/v1/application-catalog-model.md);
- [`contracts/workbench-catalog/snapshot/contracts/catalog/v1/application-model-projection.md`](contracts/workbench-catalog/snapshot/contracts/catalog/v1/application-model-projection.md); and
- [`AGENTS.md`](AGENTS.md) before making model-affecting changes.

Verify the checked-in snapshot on Windows:

```powershell
.\scripts\verify-workbench-contract.ps1
```

Or run the repository build gate:

```powershell
.\gradlew.bat check
```

## Planned implementation

Kotlin on JDK 21 is the implementation baseline. Initial analyzer work should proceed in
small adapters, beginning with a containerized test database and deterministic metadata
discovery. Every exported catalog document must be validated against the pinned schema and
the shared positive and negative fixtures before a database adapter is considered complete.

The first implementation must cover at least:

- databases, schemas, tables, views, materialized views, columns, keys, indexes, and
  constraints;
- stored procedures, functions, packages or modules, triggers, jobs, schedules, tasks,
  sequences, synonyms, and queues where supported by the database;
- proven read, write, create, delete, call, trigger, scheduling, and derivation dependencies;
- declared foreign keys separately from inferred semantic relationship candidates;
- stable identities across unchanged repeated analyses; and
- PII status with provenance, confidence, and `unknown` as the safe default.
