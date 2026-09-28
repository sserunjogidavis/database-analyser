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



- ## Current implementation status

The PostgreSQL analyzer implementation is now functional and includes metadata discovery, data-quality analysis, aggregate analysis, reporting, and automated tests.

### PostgreSQL connectivity

The analyzer connects to PostgreSQL through JDBC.

Database configuration is supplied through environment variables rather than hardcoded credentials. The current configuration includes values such as:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USER
DB_PASSWORD
```

The database password is read from the `DB_PASSWORD` environment variable.

During development, PostgreSQL is run in Docker and the analyzer is tested against the `companydb` database.

### Metadata discovery

The analyzer currently discovers PostgreSQL metadata including:

- schemas;
- tables;
- columns;
- primary keys;
- unique keys;
- foreign keys;
- indexes;
- views;
- materialized views;
- sequences;
- functions; and
- stored procedures.

The implementation supports non-public schemas.

The current development database includes tables such as:

```text
public.departments
public.employees
reporting.monthly_sales
```

### Table and column analysis

For each table, the analyzer records:

- database name;
- schema name;
- table name;
- qualified table name;
- column name;
- data type;
- nullable status;
- default value; and
- row count.

### Primary-key analysis

The analyzer discovers primary keys and verifies whether each table has a primary key.

Tables without a primary key are included in the database quality summary.

### Unique-key analysis

Single-column unique constraints are detected and used when evaluating duplicate values.

### Foreign-key analysis

Foreign-key relationships are discovered automatically.

For each relationship, the analyzer records:

- source column;
- referenced schema;
- referenced table;
- referenced column; and
- invalid reference count.

Example:

```text
department_id -> public.departments.department_id
```

Foreign-key values are validated by comparing child values with the referenced parent values.

### NULL and non-NULL analysis

For every column, the analyzer calculates:

- row count;
- non-NULL count;
- NULL count; and
- NULL percentage.

Conceptually, this includes:

```sql
COUNT(*)
COUNT(column)
```

Example:

```text
salary:
    Row Count: 3
    Non-NULL Count: 3
    NULL Count: 0
    NULL Percentage: 0.00%
```

### Distinct-value analysis

The analyzer calculates the number of distinct values in each column.

Conceptually:

```sql
COUNT(DISTINCT column)
```

This is used to identify:

- fully unique columns;
- repeated values; and
- constant-value columns.

### Duplicate analysis

Duplicate analysis distinguishes between:

- primary-key columns;
- unique columns;
- foreign-key columns; and
- normal columns.

Repeated foreign-key values are allowed, while duplicates in primary-key or unique columns are treated as quality problems.

### Potential duplicate-record detection

The analyzer can detect potential duplicate records by comparing non-key business columns.

This helps identify records that may represent the same logical business entity.

### Text-quality analysis

Text columns are checked for:

- empty strings;
- whitespace-only values; and
- leading or trailing whitespace.

### Email-format analysis

Columns whose names indicate that they contain email addresses are checked for invalid email formats.

### Numeric aggregate analysis

Numeric business columns are analysed using PostgreSQL aggregate functions.

The analyzer currently calculates:

```text
COUNT(column)
COUNT(DISTINCT column)
MIN(column)
MAX(column)
AVG(column)
SUM(column)
```

These are reported as:

- Non-NULL Count;
- Distinct Count;
- Minimum;
- Maximum;
- Average; and
- Total.

Identifier columns are excluded from business numeric analysis.

### Numeric anomaly analysis

Numeric business columns are checked for:

- negative values; and
- potential statistical outliers.

Potential outliers are detected using a median-based method.

### Date analysis

Date and timestamp columns are analysed for:

- earliest value;
- latest value; and
- future-date values.

### Database quality report

All analysis results are combined into a structured database quality report.

The summary includes:

- tables analysed;
- columns analysed;
- rows analysed;
- primary-key issues;
- foreign-key issues;
- duplicate-value issues;
- potential duplicate-record issues;
- empty-string issues;
- whitespace-only issues;
- leading/trailing whitespace issues;
- email-format issues;
- numeric-anomaly issues;
- potential numeric-outlier issues;
- date-anomaly issues;
- constant-value issues;
- high-NULL-percentage issues;
- total issues; and
- overall status.

The current overall status values are:

```text
GOOD
REVIEW
ATTENTION REQUIRED
```

### Report export

The analyzer exports reports in three formats:

```text
output/database-quality-report.json
output/database-quality-report.html
output/database-quality-report.csv
```

The JSON report provides structured machine-readable output.

The HTML report provides a browser-friendly view of the analysis.

The CSV report provides column-level analysis that can be opened in spreadsheet software.

Generated reports are stored under `output/` and should not be committed.

### Automated testing

The project includes unit and integration tests covering areas such as:

- analyzer behaviour;
- metadata-adapter behaviour;
- PostgreSQL integration;
- non-public-schema discovery;
- database-quality report generation;
- JSON export;
- HTML export; and
- CSV export.

The full test suite can be run on Windows with:

```powershell
.\gradlew.bat test
```

A successful run ends with:

```text
BUILD SUCCESSFUL
```

### Latest verified development run

The latest verified analyzer run successfully analysed:

```text
Tables analysed: 3
Columns analysed: 14
Rows analysed: 9
```

The analyzed tables were:

```text
public.departments
public.employees
reporting.monthly_sales
```

The run produced:

```text
Total Issues: 1
Overall Status: REVIEW
```

The single issue was a constant-value condition in the development test data.

The analyzer successfully generated JSON, HTML, and CSV reports and completed with:

```text
BUILD SUCCESSFUL
```

## Running the analyzer

Before running the application:

1. Start PostgreSQL.
2. Configure the required database environment variables.
3. Ensure `DB_PASSWORD` is set.
4. Run the analyzer through IntelliJ or Gradle.

The current application entry point is:

```text
com.alpinedigitalexperts.databaseanalyser.MainKt
```

The analyzer connects to the database, discovers database objects, performs the configured analyses, prints the results, and generates the output reports.

## Current analyzer source structure

The main implementation is located under:

```text
src/main/kotlin/com/alpinedigitalexperts/databaseanalyser/
```

Important source files currently include:

```text
DatabaseAnalyzer.kt
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
main.kt
```

The test implementation is located under:

```text
src/test/kotlin/com/alpinedigitalexperts/databaseanalyser/
```
