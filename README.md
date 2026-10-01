# Database Analyser

The **Database Analyser** is a Kotlin/JDK 21 application for analysing PostgreSQL databases. It discovers database structure and dependencies, performs data-quality and aggregate analysis, produces quality reports, and exports database metadata as an **Application Catalog graph** for the Legacy Modernization Workbench.

The Workbench is the semantic authority for the Application Catalog. This repository pins an immutable snapshot of that contract and verifies changes against it.

## Current implementation status

The PostgreSQL implementation is functional and tested. It currently provides:

- schema-aware discovery of databases, schemas, tables, columns, views, materialized views, keys, indexes, sequences, functions, and stored procedures;
- primary-key, unique-key, and foreign-key discovery and validation;
- database dependency discovery;
- NULL/non-NULL, distinct-value, duplicate-record, text, email, numeric, date, constant-value, and high-NULL analysis;
- grouped and column-level numeric aggregate analysis;
- potential numeric-outlier detection;
- structured database-quality reporting;
- JSON, HTML, and CSV quality-report export;
- Workbench-compatible Application Catalog export;
- contract-aligned dependency semantics;
- schema validation and automated integration testing; and
- automatic verification of the pinned Workbench contract during the Gradle `check` lifecycle.

Detailed chronological documentation is available in [`docs/PROJECT_DOCUMENTATION.md`](docs/PROJECT_DOCUMENTATION.md).

## Technology stack

| Technology | Purpose |
|---|---|
| Kotlin | Main implementation language |
| JDK 21 | Java runtime baseline |
| Gradle Kotlin DSL | Build and dependency management |
| PostgreSQL | Current supported database engine |
| JDBC | Database connectivity |
| Docker | Local PostgreSQL development environment |
| Kotlin Serialization | JSON serialization |
| Kotlin Test / JUnit | Automated testing |
| NetworkNT JSON Schema Validator | Application Catalog schema validation |
| Git / GitHub | Version control and repository hosting |

## Pinned Application Catalog contract

The Application Catalog contract is pinned to Workbench commit:

```text
732de4ddf73969b31d1791bf454b0d924459b9f2
```

Important contract files include:

- [`contracts/workbench-catalog/README.md`](contracts/workbench-catalog/README.md)
- [`contracts/workbench-catalog/contract-lock.json`](contracts/workbench-catalog/contract-lock.json)
- [`contracts/workbench-catalog/snapshot/contracts/catalog/v1/application-catalog-model.md`](contracts/workbench-catalog/snapshot/contracts/catalog/v1/application-catalog-model.md)
- [`contracts/workbench-catalog/snapshot/contracts/catalog/v1/application-model-projection.md`](contracts/workbench-catalog/snapshot/contracts/catalog/v1/application-model-projection.md)
- [`AGENTS.md`](AGENTS.md)

The analyser must not independently introduce catalog semantics that conflict with the pinned Workbench contract.

The contract snapshot can be verified directly on Windows:

```powershell
.\scripts\verify-workbench-contract.ps1
```

It is also verified automatically by the repository build gate:

```powershell
.\gradlew.bat check
```

A successful verification reports the pinned Workbench commit and verifies the checked-in contract files.

## PostgreSQL connectivity

Database configuration is supplied through environment variables rather than hardcoded credentials:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USER
DB_PASSWORD
```

The database password is read from `DB_PASSWORD` and must not be committed to the repository.

During development, PostgreSQL 16 is run in Docker and the analyser is tested against the `companydb` database.


## Reproducible Docker development database

The repository includes a `compose.yml` file so another developer can create the same PostgreSQL development environment without manually recreating the container configuration.

The Compose configuration uses:

```text
PostgreSQL: 16
Database: companydb
User: analyst
Port: 5432
Password: supplied through DB_PASSWORD
```

The password is intentionally not written into `compose.yml`. Before starting the container, set `DB_PASSWORD` in the local environment.

Example in PowerShell:

```powershell
$env:DB_PASSWORD = "your-local-password"
```

Then start PostgreSQL with:

```powershell
docker compose up -d
```

Check the container with:

```powershell
docker compose ps
```

Stop the environment with:

```powershell
docker compose down
```

The Compose configuration uses a named Docker volume so PostgreSQL data can persist between normal container restarts.

If a separately created container named `database-analyser-postgres` already exists, stop or remove that old container before starting the Compose-managed environment to avoid a container-name or port conflict.

## Metadata discovery

The PostgreSQL metadata adapter discovers supported database objects including:

```text
Database
Schema
Table
Column
Primary Key
Unique Key
Foreign Key
Index
View
Materialized View
Sequence
Function
Stored Procedure
```

The implementation is schema-aware. The development database demonstrates this with:

```text
public.departments
public.employees
reporting.monthly_sales
```

The metadata architecture is separated into:

```text
DatabaseMetadataAdapter.kt
PostgreSqlDatabaseAdapter.kt
DatabaseAnalyzer.kt
```

`DatabaseMetadataAdapter` defines the metadata abstraction, `PostgreSqlDatabaseAdapter` contains PostgreSQL-specific discovery, and `DatabaseAnalyzer` provides the higher-level analysis operations.

## Database dependency discovery

The analyser discovers dependencies between supported PostgreSQL database objects.

Application Catalog relationships use contract-aligned semantics. In particular:

- ordinary views use `derivedFrom`;
- materialized views use `derivedFrom`;
- routines use `usesData` when a database dependency is proven but the precise read/write access mode cannot be established;
- sequence dependencies use `dependsOn`;
- declared foreign keys are represented as physical foreign-key objects rather than as redundant generic dependency edges.

Routine dependency discovery retains PostgreSQL identity arguments so overloaded functions and procedures can be resolved to the correct catalog node.

## Foreign-key representation

Declared foreign keys are exported as physical Application Catalog objects.

The foreign-key node:

1. is structurally contained by its owning table;
2. uses ordered `includes` relationships for its source columns; and
3. uses `referencesKey` to point to the referenced primary or unique key.

For example, the development database contains:

```text
public.employees.department_id
    ->
public.departments.department_id
```

The analyser also validates foreign-key data and reports invalid references as data-quality findings.

## Data-quality analysis

The analyser performs multiple quality checks.

### NULL analysis

For each column it calculates:

```text
Row Count
Non-NULL Count
NULL Count
NULL Percentage
```

### Distinct and duplicate analysis

The analyser calculates distinct-value counts and distinguishes between repetition in:

- primary-key columns;
- unique columns;
- foreign-key columns; and
- ordinary columns.

It also checks for potential duplicate business records.

### Text analysis

Text columns can be checked for:

- empty strings;
- whitespace-only values;
- leading/trailing whitespace; and
- inconsistent text conditions supported by the analyser.

Email-related columns are also checked for invalid email formats.

### Numeric analysis

Appropriate numeric business columns are analysed using:

```text
COUNT(column)
COUNT(DISTINCT column)
MIN(column)
MAX(column)
AVG(column)
SUM(column)
```

Identifier columns are excluded from business numeric statistics where appropriate.

Numeric quality analysis also includes negative-value detection and median-based potential-outlier detection.

### Date analysis

Date and timestamp columns can be analysed for:

```text
Earliest Value
Latest Value
Future-Date Values
```

### Constant values and high NULL percentages

The analyser identifies columns that contain only one distinct non-NULL value and tracks high NULL-percentage conditions in the quality summary.

## Aggregate analysis

The analyser supports generic grouped numeric aggregate analysis.

Instead of hardcoding a particular table such as `reporting.monthly_sales`, the report builder identifies suitable grouping columns and numeric business columns.

This allows aggregate analysis to work with different database structures while avoiding identifier columns as business measurements.

Aggregate information is included in the structured report and supported by the JSON, HTML, and CSV reporting pipeline.

## Database quality report

Analysis results are assembled by `DatabaseQualityReportBuilder.kt` into a structured report.

The summary includes metrics such as:

```text
Tables Analysed
Columns Analysed
Rows Analysed
Primary Key Issues
Foreign Key Issues
Duplicate Value Issues
Potential Duplicate Record Issues
Empty String Issues
Whitespace-Only Issues
Leading/Trailing Whitespace Issues
Email Format Issues
Numeric Anomaly Issues
Potential Numeric Outlier Issues
Date Anomaly Issues
Constant Value Issues
High NULL Percentage Issues
Total Issues
Overall Status
```

Current overall status values include:

```text
GOOD
REVIEW
ATTENTION REQUIRED
```

## Report export

The application generates database-quality reports in three formats:

```text
output/database-quality-report.json
output/database-quality-report.html
output/database-quality-report.csv
```

The JSON output is machine-readable, the HTML report is browser-friendly, and the CSV report supports spreadsheet/data-processing workflows.

Generated output is kept under `output/` and should not be committed.

## Application Catalog export

The application also generates:

```text
output/application-catalog.json
```

The catalog represents discovered database metadata as Workbench Application Catalog nodes and relationships.

The current implementation exports physical database objects with `databaseAnalysis` origin and observed evidence where the information was directly discovered from PostgreSQL.

Examples of exported relationships include:

```text
contains
includes
indexes
referencesKey
dependsOn
derivedFrom
usesData
```

Catalog construction is handled primarily by:

```text
ApplicationCatalog.kt
DatabaseCatalogBuilder.kt
ApplicationCatalogJsonExporter.kt
```

## Application Catalog validation

The exported catalog is tested against the pinned Workbench JSON Schema.

The project also contains integration tests for catalog construction and PostgreSQL dependency discovery, including views, materialized views, sequences, functions, stored procedures, declared foreign keys, and overloaded routine resolution.

Model-affecting changes must continue to follow the pinned contract rather than creating analyser-specific catalog semantics.

## Automated testing

The project contains unit and integration tests covering:

- analyzer behaviour;
- metadata-adapter behaviour;
- PostgreSQL metadata discovery;
- non-public schemas;
- quality-report construction;
- aggregate analysis;
- JSON, HTML, and CSV exporters;
- Application Catalog construction;
- Application Catalog schema validation;
- database dependency discovery;
- overloaded routine dependency resolution;
- reproducible SQL fixture loading;
- planted-defect assertions for NULL percentages, malformed emails, numeric outliers, duplicate records, future dates, and invalid foreign-key references;
- multi-row grouped aggregate verification; and
- Workbench contract verification.

Run the tests on Windows with:

```powershell
.\gradlew.bat test
```

Run the complete verification gate with:

```powershell
.\gradlew.bat check
```

The latest completed verification passed successfully and verified the pinned Workbench Application Catalog contract.


## Reproducible database-quality integration fixture

The repository contains a dedicated integration-test fixture at:

```text
src/test/resources/sql/database-quality-fixture.sql
```

The fixture creates an isolated PostgreSQL schema:

```text
quality_test
```

This prevents the stronger integration tests from depending on whatever data happens to exist in the normal development tables.

The fixture intentionally contains known data-quality conditions so tests can assert exact expected results rather than only checking that a value is non-null or happens to be zero.

The main fixture includes:

```text
20 employee rows
6 NULL salaries = 30% NULL
2 malformed email addresses
1 deliberately extreme numeric salary outlier
1 future hire date
1 exact duplicate business record
1 orphaned foreign-key value
```

The orphaned foreign-key case uses a PostgreSQL `NOT VALID` foreign-key constraint. This allows the fixture to retain one deliberately invalid existing reference while still exposing a declared foreign-key definition to the analyser.

The grouped aggregate fixture contains:

```text
15 rows
3 categories
5 rows per category
```

This provides enough observations per group to exercise grouped minimum, maximum, average, total, and row-count calculations more meaningfully than the previous three-row dataset.

The fixture is loaded by:

```text
DatabaseTestFixture.kt
```

and verified by:

```text
DatabaseTestFixtureIntegrationTest.kt
DatabaseQualityReportBuilderIntegrationTest.kt
```

Tests load the fixture inside a database transaction and roll the transaction back afterward. The fixture therefore does not permanently replace or pollute the normal `public` and `reporting` development data.

A focused fixture test can be run with:

```powershell
.\gradlew.bat test --tests "com.alpinedigitalexperts.databaseanalyser.DatabaseTestFixtureIntegrationTest"
```

The stronger report-builder integration tests can be run with:

```powershell
.\gradlew.bat test --tests "com.alpinedigitalexperts.databaseanalyser.DatabaseQualityReportBuilderIntegrationTest"
```

The full clean test suite can be run with:

```powershell
.\gradlew.bat clean test
```

## Latest development database result

The development database currently contains three analysed tables:

```text
public.departments
public.employees
reporting.monthly_sales
```

A verified quality-analysis run analysed:

```text
Tables analysed: 3
Columns analysed: 14
Rows analysed: 9
```

The remaining quality finding in that development dataset was a constant-value condition, producing:

```text
Total Issues: 1
Overall Status: REVIEW
```

This is a finding in the test/development data rather than a build failure.

## Running the analyser

Before running the application:

1. Start PostgreSQL.
2. Configure the database environment variables.
3. Ensure `DB_PASSWORD` is set.
4. Run the application through IntelliJ IDEA or Gradle.

From PowerShell:

```powershell
.\gradlew.bat run
```

The application entry point is:

```text
com.alpinedigitalexperts.databaseanalyser.MainKt
```

A normal run connects to PostgreSQL, discovers supported database objects and dependencies, performs the configured analysis, builds the quality report and Application Catalog, and writes the generated output files.

## Source structure

Main implementation:

```text
src/main/kotlin/com/alpinedigitalexperts/databaseanalyser/
```

Important files include:

```text
ApplicationCatalog.kt
ApplicationCatalogJsonExporter.kt
DatabaseAnalyzer.kt
DatabaseCatalogBuilder.kt
DatabaseConfig.kt
DatabaseConnector.kt
DatabaseMetadataAdapter.kt
DatabaseQualityReport.kt
DatabaseQualityReportBuilder.kt
DatabaseQualityReportCsvExporter.kt
DatabaseQualityReportHtmlExporter.kt
DatabaseQualityReportJsonExporter.kt
PostgreSqlDatabaseAdapter.kt
QualityFinding.kt
Main.kt
```

Tests:

```text
src/test/kotlin/com/alpinedigitalexperts/databaseanalyser/
```

Reproducible SQL test fixtures:

```text
src/test/resources/sql/
```

Docker Compose environment:

```text
compose.yml
```

Contract snapshot:

```text
contracts/workbench-catalog/
```

Detailed project documentation:

```text
docs/PROJECT_DOCUMENTATION.md
```

## Security

Database credentials must not be committed.

In particular:

```text
DB_PASSWORD
```

is obtained from the environment.

Generated output, IDE files, build files, and local secret/environment files should remain excluded according to the repository's `.gitignore`.

## Future work

The current PostgreSQL implementation provides the core metadata, dependency, quality-analysis, aggregate-analysis, reporting, and Application Catalog foundation.

Possible future extensions include:

- proving more precise routine access modes such as `readsData`, `writesData`, `createsData`, and `deletesData` where PostgreSQL evidence permits;
- triggers, jobs, schedules, tasks, synonyms, queues, and other supported database objects;
- inferred semantic relationship candidates;
- privacy/PII classification with provenance and confidence;
- additional database-engine adapters;
- configurable quality rules;
- command-line configuration;
- historical report comparison;
- additional statistical analysis;
- graphical reporting; and
- CI/CD integration.

Any future Application Catalog work must remain aligned with the pinned Workbench contract.

## Documentation

For the full chronological implementation history, design decisions, tests, encountered problems, and current project status, see:

[`docs/PROJECT_DOCUMENTATION.md`](docs/PROJECT_DOCUMENTATION.md)
