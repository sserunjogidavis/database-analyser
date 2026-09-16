# Contract Conformance Fixtures

These fixtures define portable test cases shared by Kotlin, .NET, and future adapter
implementations. They contain synthetic data only.

Validation has separate stages:

1. `schema` validates JSON shape, required fields, primitive constraints, and enums.
2. `semantic` validates graph identity, references, status consistency, trust, authority,
   project identity, edit permissions, and operation preconditions.
3. `package` validates ZIP structure, normalized paths, declared entries, byte limits, and
   hashes.

An implementation must report the expected error code for every invalid fixture. It may
also report more specific diagnostic details.

| Fixture | Expected | Stage | Error code |
| --- | --- | --- | --- |
| `catalog/v1/valid/comprehensive-catalog.json` | valid | schema + semantic | - |
| `catalog/v1/valid/application-model-graph.json` | valid | schema + semantic | - |
| `catalog/v1/valid/trusted-model-input.json` | valid | schema + semantic | - |
| `catalog/v1/valid/catalog-presentation.json` | valid | schema + semantic | - |
| `catalog/v1/invalid/catalog-presentation-disconnected.json` | invalid | semantic | `catalog.presentation.notConnected` |
| `catalog/v1/invalid/duplicate-node-id.json` | invalid | semantic | `catalog.node.duplicateId` |
| `catalog/v1/invalid/dangling-relationship.json` | invalid | semantic | `catalog.relationship.endpointMissing` |
| `catalog/v1/invalid/usage-status-mismatch.json` | invalid | semantic | `catalog.usage.confirmedEffectiveMismatch` |
| `catalog/v1/invalid/untrusted-code-generation-specification.json` | invalid | semantic | `catalog.specification.untrustedInstructionAuthority` |
| `catalog/v1/invalid/unknown-node-type.json` | invalid | schema | `catalog.schema.unsupportedNodeType` |
| `catalog/v1/invalid/workbench-relationship-without-id.json` | invalid | schema | `catalog.relationship.idRequired` |
| `catalog/v1/invalid/application-model-unknown-ui-family.json` | invalid | schema | `catalog.schema.invalid` |
| `catalog/v1/invalid/business-transaction-invalid-execution-mode.json` | invalid | schema | `catalog.schema.invalid` |
| `catalog/v1/invalid/privacy-classification-invalid-pii-status.json` | invalid | schema | `catalog.schema.invalid` |
| `catalog/v1/invalid/trusted-graph-not-closed.json` | invalid | semantic | `trusted.relationship.endpointMissing` |
| `catalog/v1/invalid/trusted-summary-mismatch.json` | invalid | semantic | `trusted.summary.countMismatch` |
| `exchange/v1/valid/revision.json` | valid | schema + semantic | - |
| `exchange/v1/valid/snapshot.json` | valid | schema + semantic | - |
| `exchange/v1/valid/snapshot-manifest.json` | valid | schema | - |
| `exchange/v1/valid/changeset.json` | valid | schema + semantic | - |
| `exchange/v1/invalid/unsafe-archive-path.json` | invalid | schema | `exchange.path.unsafe` |
| `exchange/v1/invalid/revision-source-package-hash.json` | invalid | schema | `exchange.schema.invalid` |
| `exchange/v1/invalid/immutable-evidence-update.json` | invalid | semantic | `changeset.evidence.immutableField` |
| `import/apprecorder/v1/valid/analysis.json` with its catalog, trusted projection, and catalog presentation | valid | import | - |
| `import/apprecorder/v1/invalid/relationship-identity-collision.json` | invalid | import | `import.relationship.identityCollision` |

Descriptor hashes in the standalone JSON fixtures are syntactically valid illustrative
values. They do not describe a ZIP fixture and therefore are not package-hash test vectors.
Byte-exact package and canonicalization fixtures will be added with the canonical hashing
algorithm.
