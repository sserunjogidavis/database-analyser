# Cross-repository Application Catalog Governance

## 1. Purpose

The Workbench repository is the canonical source for the Application Catalog schemas,
semantic interpretation rules, projection rules, and conformance fixtures. AppRecorder,
database analyzers, source analyzers, and future adapters produce or consume this contract;
they do not maintain independent reinterpretations.

This process makes model decisions durable across repositories, developers, AI-assisted
changes, and future conversations.

## 2. Canonical contract set

An adapter design must use the complete contract set from one Workbench revision:

- `contracts/catalog/v1/catalog.schema.json`;
- `contracts/catalog/v1/trusted-model-input.schema.json` where applicable;
- `contracts/catalog/v1/application-catalog-model.md`;
- `contracts/catalog/v1/application-model-projection.md`;
- `contracts/fixtures/README.md`; and
- all fixtures required by the adapter profile.

The schema defines representation. The model specification defines meaning. The projection
specification defines interpretation and presentation. None can be used alone.

## 3. Adapter contract lock

Every producer repository must contain a machine-readable contract lock, for example
`contracts/workbench-catalog.lock.json`, containing at least:

- Workbench repository identity;
- exact Workbench commit or released contract tag;
- catalog contract version;
- adapter profile identifier and version; and
- SHA-256 of the synchronized contract bundle.

Contract files and fixtures are synchronized by a script that verifies the lock and bundle
hash. They are not copied and edited manually. A contract upgrade is a dedicated reviewed
change in the adapter repository.

While catalog v1 remains a development contract, adapters pin a Workbench commit. After a
contract release, they pin the immutable release tag and bundle hash.

## 4. Repository instructions and design gate

Every adapter repository must add a root `AGENTS.md` rule requiring developers and coding
agents to read the pinned model, projection, adapter profile, and fixture index before they
change export, import, discovery, identity, relationship, privacy, or explorer behavior.

A design or pull request affecting the catalog answers these questions:

1. Which canonical node and relationship types are used?
2. Is the fact observed, inferred, curated, or confirmed?
3. Are identity and ownership stable across unchanged runs?
4. Are inverse references derived rather than duplicated?
5. Are unknown and negative findings kept distinct?
6. Are PII values excluded or protected and classification provenance retained?
7. Which positive, negative, and cross-product fixtures prove conformance?
8. Is the change additive, or does it require a new contract version and migration?

Review cannot approve a model-affecting change with unanswered items.

## 5. Build and conformance gate

Each adapter build must:

- verify the contract-lock hash before compilation or tests;
- validate its produced documents with the pinned JSON Schemas;
- execute the shared positive and negative fixtures;
- add adapter-specific fixtures without weakening shared expectations;
- prove stable identities and relationships across unchanged repeated analysis;
- prove that unknown PII remains unknown rather than becoming no;
- distinguish declared constraints from inferred semantic relationships; and
- compare its projected graph with Workbench normalization using golden exports.

AppRecorder tests additionally cover UI ownership, transaction traces, screenshots, and
manual versus automatic execution. Database analyzer tests cover composite keys, routines,
packages, triggers, schedules, read/write dependencies, semantic relationship confidence,
and privacy-classification provenance.

CI must fail when the lock is stale, a fixture differs from the pinned bundle, or the
adapter emits unsupported semantics.

## 6. Export declaration and compatibility

Every export declares its schema version, format, adapter profile, and producer version.
The Workbench accepts only a supported combination and validates the complete package
atomically. It never guesses the intended semantics from producer names or field presence.

The Workbench maintains an explicit compatibility matrix for supported adapter-profile
versions. A producer contract upgrade is accepted only after the corresponding golden
export passes Workbench import, normalization, comparison, and projection tests.

## 7. Change workflow

1. Propose the semantic change in the Workbench contract.
2. Update schema, normative documentation, positive fixtures, and negative fixtures.
3. Run Workbench validation and compatibility tests.
4. Publish or identify the immutable contract revision and bundle hash.
5. Upgrade one adapter's contract lock in a dedicated branch.
6. Implement export or analysis changes and pass shared plus adapter-specific tests.
7. Import its golden export into the Workbench and compare the normalized graph.
8. Only then merge the adapter and update the compatibility matrix.

This ordering prevents AppRecorder or a data analyzer from silently becoming the semantic
source of truth.

## 8. Current follow-up

The next AppRecorder branch must introduce its contract lock and repository instruction
before redesigning the Application Model Explorer or export. The future database-analyzer
repository begins with the same lock, synchronization, and conformance structure before its
first database adapter is implemented.
