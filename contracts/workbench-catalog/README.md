# Pinned Workbench Application Catalog contract

This directory is an immutable, verified snapshot of the Application Catalog contract from
the Legacy Modernization Workbench. The Workbench remains the canonical source. Files in the
snapshot are present so local and CI builds are reproducible and do not depend on a mutable
remote branch.

## Pinned source

- Repository: `https://github.com/alpine-digital-experts/LegacyModernizationWorkbench`
- Commit: `732de4ddf73969b31d1791bf454b0d924459b9f2`
- Contract: catalog v1 development contract
- Consumer profile: database analyser v1 development

The complete machine-readable provenance is in `contract-lock.json`. Every synchronized
file is listed in `workbench-catalog.sha256`. Hashes are calculated from UTF-8 text after
normalizing CRLF and CR line endings to LF, so the same contract is verifiable on Windows,
macOS, and Linux without accepting any semantic content change.

## Mandatory reading before analyzer design

1. `snapshot/contracts/catalog/v1/application-catalog-model.md`
2. `snapshot/contracts/catalog/v1/application-model-projection.md`
3. `snapshot/contracts/catalog/v1/catalog.schema.json`
4. `snapshot/contracts/fixtures/README.md`
5. `snapshot/docs/cross-repository-contract-governance.md`

The schema defines representation, the model specification defines meaning, and the
projection specification defines shared interpretation. None is sufficient alone.

## Verification

Run:

```powershell
.\scripts\verify-workbench-contract.ps1
```

or:

```powershell
.\gradlew.bat check
```

Verification rejects:

- a changed manifest;
- changed, missing, unlisted, duplicate, or path-escaping snapshot files;
- an unexpected file count; and
- a lock whose digest no longer describes the manifest.

## Contract upgrade procedure

Do not edit a snapshot file to make an analyzer test pass.

1. Propose and merge semantic changes in the Workbench repository.
2. Run Workbench schema, semantic, projection, and fixture tests.
3. Copy the complete contract set from one immutable Workbench commit or release.
4. Regenerate `workbench-catalog.sha256` using normalized relative paths.
5. Update `contract-lock.json`, including commit, file count, and manifest SHA-256.
6. Run this repository's shared and adapter-specific conformance tests.
7. Import a golden analyzer export into the Workbench and compare the normalized graph.
8. Review and merge the contract upgrade separately from unrelated analyzer behavior.

The analyzer must not emit new semantics until the pinned Workbench contract supports them.
