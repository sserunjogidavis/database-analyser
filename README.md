Database Analyser
The Database Analyser is a Kotlin/JDK 21 application for analysing PostgreSQL databases. It discovers database structure and dependencies, performs data-quality and aggregate analysis, produces quality reports, and exports database metadata as an Application Catalog graph for the Legacy Modernization Workbench.
The Workbench is the semantic authority for the Application Catalog. This repository pins an immutable snapshot of that contract and verifies changes against it.
Current implementation status
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
- schema validation and automated integration testing;
- configurable quality-analysis rules through environment variables and command-line overrides;
- command-line runtime configuration for database connection settings and quality-analysis thresholds;
- historical database-quality report comparison with category-level change tracking and JSON export;
- multi-snapshot historical quality trend analysis with per-snapshot category history, first-to-latest category change tracking, console output, and JSON export; and
- automatic verification of the pinned Workbench contract during the Gradle check lifecycle.
  Detailed chronological documentation is available in [`docs/PROJECT_DOCUMENTATION.md`](docs/PROJECT_DOCUMENTATION.md).
  Technology stack
  Technology    Purpose
  Kotlin    Main implementation language
  JDK 21    Java runtime baseline
  Gradle Kotlin DSL Build and dependency management
  PostgreSQL    Current supported database engine
  JDBC  Database connectivity
  Docker    Local PostgreSQL development environment
  Kotlin Serialization  JSON serialization
  Kotlin Test / JUnit   Automated testing
  NetworkNT JSON Schema Validator   Application Catalog schema validation
  Git / GitHub  Version control and repository hosting


Pinned Application Catalog contract
The Application Catalog contract is pinned to Workbench commit:
732de4ddf73969b31d1791bf454b0d924459b9f2
Important contract files include:
- [`contracts/workbench-catalog/README.md`](contracts/workbench-catalog/README.md)
- [`contracts/workbench-catalog/contract-lock.json`](contracts/workbench-catalog/contract-lock.json)
- [`contracts/workbench-catalog/snapshot/contracts/catalog/v1/application-catalog-model.md`](contracts/workbench-catalog/snapshot/contracts/catalog/v1/application-catalog-model.md)
- [`contracts/workbench-catalog/snapshot/contracts/catalog/v1/application-model-projection.md`](contracts/workbench-catalog/snapshot/contracts/catalog/v1/application-model-projection.md)
- [`AGENTS.md`](AGENTS.md)
  The analyser must not independently introduce catalog semantics that conflict with the pinned Workbench contract.
  The contract snapshot can be verified directly on Windows:
  .\scripts\verify-workbench-contract.ps1
  It is also verified automatically by the repository build gate:
  .\gradlew.bat check
  A successful verification reports the pinned Workbench commit and verifies the checked-in contract files.
  PostgreSQL connectivity
  Database configuration is supplied through environment variables rather than hardcoded credentials:
  DB_HOST
  DB_PORT
  DB_NAME
  DB_USER
  DB_PASSWORD
  The database password is read from DB_PASSWORD and must not be committed to the repository.
  During development, PostgreSQL 16 is run in Docker and the analyser is tested against the companydb database.
  Reproducible Docker development database
  The repository includes a compose.yml file so another developer can create the same PostgreSQL development environment without manually recreating the container configuration.
  The Compose configuration uses:
  PostgreSQL: 16
  Database: companydb
  User: analyst
  Port: 5432
  Password: supplied through DB_PASSWORD
  The password is intentionally not written into compose.yml. Before starting the container, set DB_PASSWORD in the local environment.
  Example in PowerShell:
  $env:DB_PASSWORD = "your-local-password"
  Then start PostgreSQL with:
  docker compose up -d
  Check the container with:
  docker compose ps
  Stop the environment with:
  docker compose down
  The Compose configuration uses a named Docker volume so PostgreSQL data can persist between normal container restarts.
  The repository also contains a base PostgreSQL initialization script at:
  docker/init/01-base-schema.sql
  The docker/init directory is mounted read-only at /docker-entrypoint-initdb.d. PostgreSQL runs the initialization script when a new empty Compose data volume is initialized. The script creates and seeds the normal development objects used by the analyser and integration tests, including:
  public.departments
  public.employees
  reporting.monthly_sales
  This fresh-database path has been verified end-to-end: a new Compose-managed PostgreSQL 16 database was initialized from the repository, the expected development tables and seed rows were present, ./gradlew.bat clean test passed, and ./gradlew.bat check passed while verifying the pinned Workbench Application Catalog contract.
  If a separately created container named database-analyser-postgres already exists, stop and rename or remove that old container before starting the Compose-managed environment to avoid a container-name or port conflict. Do not remove an old Docker data volume unless its data is no longer needed.
  Configurable quality rules
  Quality-analysis thresholds can be configured through environment variables. If these variables are not set, the analyser uses defaults that preserve the standard analysis behaviour.
  Environment variable  Default    Purpose
  QUALITY_NULL_WARNING_THRESHOLD    20.0   NULL percentage at which a column is reported as a high-NULL warning
  QUALITY_NULL_CRITICAL_THRESHOLD   50.0   NULL percentage above which a column is reported as critical
  QUALITY_OUTLIER_ZSCORE_THRESHOLD  3.5    Modified z-score threshold used for potential numeric-outlier detection
  QUALITY_OUTLIER_MIN_SAMPLE_SIZE   3  Minimum number of non-NULL numeric values required before outlier analysis is performed


The settings are represented by QualityRules.kt, validated when configuration is created, loaded from the environment by DatabaseConfig.kt, and supplied to DatabaseAnalyzer.
Example PowerShell overrides:
$env:QUALITY_NULL_WARNING_THRESHOLD = "30"
$env:QUALITY_NULL_CRITICAL_THRESHOLD = "60"
$env:QUALITY_OUTLIER_ZSCORE_THRESHOLD = "4.0"
$env:QUALITY_OUTLIER_MIN_SAMPLE_SIZE = "5"

.\gradlew.bat run
Invalid configuration values are rejected. If none of these variables is supplied, the defaults above are used.
Metadata discovery
The PostgreSQL metadata adapter discovers supported database objects including:
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
The implementation is schema-aware. The development database demonstrates this with:
public.departments
public.employees
reporting.monthly_sales
The metadata architecture is separated into:
DatabaseMetadataAdapter.kt
PostgreSqlDatabaseAdapter.kt
DatabaseAnalyzer.kt
DatabaseMetadataAdapter defines the metadata abstraction, PostgreSqlDatabaseAdapter contains PostgreSQL-specific discovery, and DatabaseAnalyzer provides the higher-level analysis operations.
Database dependency discovery
The analyser discovers dependencies between supported PostgreSQL database objects.
Application Catalog relationships use contract-aligned semantics. In particular:
- ordinary views use derivedFrom;
- materialized views use derivedFrom;
- routines use usesData when a database dependency is proven but the precise read/write access mode cannot be established;
- sequence dependencies use dependsOn;
- declared foreign keys are represented as physical foreign-key objects rather than as redundant generic dependency edges.
  Routine dependency discovery retains PostgreSQL identity arguments so overloaded functions and procedures can be resolved to the correct catalog node.
  Foreign-key representation
  Declared foreign keys are exported as physical Application Catalog objects.
  The foreign-key node:
1. is structurally contained by its owning table;
2. uses ordered includes relationships for its source columns; and
3. uses referencesKey to point to the referenced primary or unique key.
   For example, the development database contains:
   public.employees.department_id
   ->
   public.departments.department_id
   The analyser also validates foreign-key data and reports invalid references as data-quality findings.
   Data-quality analysis
   The analyser performs multiple quality checks.
   NULL analysis
   For each column it calculates:
   Row Count
   Non-NULL Count
   NULL Count
   NULL Percentage
   Distinct and duplicate analysis
   The analyser calculates distinct-value counts and distinguishes between repetition in:
- primary-key columns;
- unique columns;
- foreign-key columns; and
- ordinary columns.
  It also checks for potential duplicate business records.
  Text analysis
  Text columns can be checked for:
- empty strings;
- whitespace-only values;
- leading/trailing whitespace; and
- inconsistent text conditions supported by the analyser.
  Email-related columns are also checked for invalid email formats.
  Numeric analysis
  Appropriate numeric business columns are analysed using:
  COUNT(column)
  COUNT(DISTINCT column)
  MIN(column)
  MAX(column)
  AVG(column)
  SUM(column)
  Identifier columns are excluded from business numeric statistics where appropriate.
  Numeric quality analysis also includes negative-value detection and median-based potential-outlier detection.
  Date analysis
  Date and timestamp columns can be analysed for:
  Earliest Value
  Latest Value
  Future-Date Values
  Constant values and high NULL percentages
  The analyser identifies columns that contain only one distinct non-NULL value and tracks high NULL-percentage conditions in the quality summary.
  Aggregate analysis
  The analyser supports generic grouped numeric aggregate analysis.
  Instead of hardcoding a particular table such as reporting.monthly_sales, the report builder identifies suitable grouping columns and numeric business columns.
  This allows aggregate analysis to work with different database structures while avoiding identifier columns as business measurements.
  Aggregate information is included in the structured report and supported by the JSON, HTML, and CSV reporting pipeline.
  Database quality report
  Analysis results are assembled by DatabaseQualityReportBuilder.kt into a structured report.
  The summary includes metrics such as:
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
  Current overall status values include:
  GOOD
  REVIEW
  ATTENTION REQUIRED
  Report export
  The application generates database-quality reports in three formats:
  output/database-quality-report.json
  output/database-quality-report.html
  output/database-quality-report.csv
  The JSON output is machine-readable, the HTML report is browser-friendly, and the CSV report supports spreadsheet/data-processing workflows.
  When a previous JSON quality report is supplied with --compare-with, the application also generates:
  output/database-quality-comparison.json
  The comparison records the previous and current total issue counts, the issue-count change, previous and current overall status, an IMPROVED, REGRESSED, or UNCHANGED outcome, and category-by-category issue changes.
  Generated output is kept under output/ and should not be committed.
  Application Catalog export
  The application also generates:
  output/application-catalog.json
  The catalog represents discovered database metadata as Workbench Application Catalog nodes and relationships.
  The current implementation exports physical database objects with databaseAnalysis origin and observed evidence where the information was directly discovered from PostgreSQL.
  Examples of exported relationships include:
  contains
  includes
  indexes
  referencesKey
  dependsOn
  derivedFrom
  usesData
  Catalog construction is handled primarily by:
  ApplicationCatalog.kt
  DatabaseCatalogBuilder.kt
  ApplicationCatalogJsonExporter.kt
  Application Catalog validation
  The exported catalog is tested against the pinned Workbench JSON Schema.
  The project also contains integration tests for catalog construction and PostgreSQL dependency discovery, including views, materialized views, sequences, functions, stored procedures, declared foreign keys, and overloaded routine resolution.
  Model-affecting changes must continue to follow the pinned contract rather than creating analyser-specific catalog semantics.
  Automated testing
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
- multi-row grouped aggregate verification;
- configurable quality-rule validation and integration behaviour;
- command-line argument parsing;
- runtime configuration resolution and command-line overrides;
- historical quality-report comparison;
- historical quality-report JSON reading and comparison JSON export;
- multi-snapshot historical quality-trend analysis, per-snapshot category tracking, first-to-latest category changes, and trend JSON export; and
- Workbench contract verification.
  Run the tests on Windows with:
  .\gradlew.bat test
  Run the complete verification gate with:
  .\gradlew.bat check
  The latest completed verification passed successfully and verified the pinned Workbench Application Catalog contract.
  Reproducible database-quality integration fixture
  The repository contains a dedicated integration-test fixture at:
  src/test/resources/sql/database-quality-fixture.sql
  The fixture creates an isolated PostgreSQL schema:
  quality_test
  This prevents the stronger integration tests from depending on whatever data happens to exist in the normal development tables.
  The fixture intentionally contains known data-quality conditions so tests can assert exact expected results rather than only checking that a value is non-null or happens to be zero.
  The main fixture includes:
  20 employee rows
  6 NULL salaries = 30% NULL
  2 malformed email addresses
  1 deliberately extreme numeric salary outlier
  1 future hire date
  1 exact duplicate business record
  1 orphaned foreign-key value
  The orphaned foreign-key case uses a PostgreSQL NOT VALID foreign-key constraint. This allows the fixture to retain one deliberately invalid existing reference while still exposing a declared foreign-key definition to the analyser.
  The grouped aggregate fixture contains:
  15 rows
  3 categories
  5 rows per category
  This provides enough observations per group to exercise grouped minimum, maximum, average, total, and row-count calculations more meaningfully than the previous three-row dataset.
  The fixture is loaded by:
  DatabaseTestFixture.kt
  and verified by:
  DatabaseTestFixtureIntegrationTest.kt
  DatabaseQualityReportBuilderIntegrationTest.kt
  Tests load the fixture inside a database transaction and roll the transaction back afterward. The fixture therefore does not permanently replace or pollute the normal public and reporting development data.
  A focused fixture test can be run with:
  .\gradlew.bat test --tests "com.alpinedigitalexperts.databaseanalyser.DatabaseTestFixtureIntegrationTest"
  The stronger report-builder integration tests can be run with:
  .\gradlew.bat test --tests "com.alpinedigitalexperts.databaseanalyser.DatabaseQualityReportBuilderIntegrationTest"
  The full clean test suite can be run with:
  .\gradlew.bat clean test
  Latest development database result
  The development database currently contains three analysed tables:
  public.departments
  public.employees
  reporting.monthly_sales
  A verified quality-analysis run analysed:
  Tables analysed: 3
  Columns analysed: 14
  Rows analysed: 9
  The remaining quality finding in that development dataset was a constant-value condition, producing:
  Total Issues: 1
  Overall Status: REVIEW
  This is a finding in the test/development data rather than a build failure.
  Running the analyser
  Before running the application:
1. Start PostgreSQL.
2. Configure the database environment variables.
3. Ensure DB_PASSWORD is set.
4. Run the application through IntelliJ IDEA or Gradle.
   From PowerShell:
   .\gradlew.bat run
   Command-line configuration
   The analyser supports command-line overrides for database connection settings and quality-analysis thresholds.
   Supported options are:
   Command-line option  Purpose
   --host   PostgreSQL host
   --port   PostgreSQL port
   --database   PostgreSQL database name
   --user   PostgreSQL user
   --null-warning-threshold NULL-percentage warning threshold
   --null-critical-threshold    NULL-percentage critical threshold
   --outlier-zscore-threshold   Modified z-score threshold for potential numeric-outlier detection
   --outlier-min-sample-size    Minimum number of values required before outlier analysis is performed
   --compare-with   Path to a previous database-quality JSON report to compare with the current run
   --trend-with   Path to a historical database-quality JSON report for trend analysis; repeat the option to supply multiple snapshots in chronological order


For example, database connection settings can be supplied through command-line arguments:
.\gradlew.bat run --args="--host localhost --port 5432 --database companydb --user analyst"
Quality-analysis rules can also be overridden:
.\gradlew.bat run --args="--null-warning-threshold 10 --null-critical-threshold 40 --outlier-zscore-threshold 4.5 --outlier-min-sample-size 3"
A current run can be compared with a previously exported JSON quality report:
.\gradlew.bat run --args="--compare-with output\previous-database-quality-report.json"
When comparison is enabled, the application prints a historical quality comparison to the console and writes:
output/database-quality-comparison.json
The comparison is classified as IMPROVED when the current total issue count is lower, REGRESSED when it is higher, and UNCHANGED when the total issue count is the same. Category-level changes are included so individual issue categories can also be inspected.

Historical trend analysis can use multiple previously exported reports. Supply --trend-with once for each historical report, in chronological order:
.\gradlew.bat run --args="--trend-with output\trend-report-1.json --trend-with output\trend-report-2.json"
The current run is automatically appended as the latest snapshot. The application prints a HISTORICAL QUALITY TREND section and writes:
output/database-quality-trend.json
The trend is classified as IMPROVING when the latest total issue count is lower than the first snapshot, WORSENING when it is higher, and STABLE when the first and latest totals are equal. Intermediate snapshots are retained in order so the history can be inspected even when the first and latest totals are equal. Each snapshot retains all 13 quality-category issue counts. The trend also calculates first-to-latest changes for each category, prints those category changes in the console, and includes them in output/database-quality-trend.json. The overall IMPROVING, WORSENING, or STABLE classification continues to depend only on the first and latest total issue counts; individual category changes do not override that classification. --compare-with and --trend-with can be used together in the same run.

Configuration precedence is:
Command-line option
↓
Environment variable
↓
Built-in default
This allows temporary command-line overrides without changing the normal environment configuration.
DB_PASSWORD is intentionally not available as a command-line option. The database password must continue to be supplied through the DB_PASSWORD environment variable so that credentials are not unnecessarily exposed in command history or process arguments.
Command-line input is parsed by CommandLineConfig.kt. RuntimeConfig.kt combines command-line overrides with the existing environment/default configuration before the database connection and quality analyser are created.
The command-line configuration behaviour is covered by CommandLineConfigTest.kt and RuntimeConfigTest.kt.
The application entry point is:
com.alpinedigitalexperts.databaseanalyser.MainKt
A normal run connects to PostgreSQL, discovers supported database objects and dependencies, performs the configured analysis, builds the quality report and Application Catalog, and writes the generated output files.
Source structure
Main implementation:
src/main/kotlin/com/alpinedigitalexperts/databaseanalyser/
Important files include:
ApplicationCatalog.kt
ApplicationCatalogJsonExporter.kt
CommandLineConfig.kt
DatabaseAnalyzer.kt
DatabaseCatalogBuilder.kt
DatabaseConfig.kt
DatabaseConnector.kt
DatabaseMetadataAdapter.kt
DatabaseQualityComparison.kt
DatabaseQualityComparisonJsonExporter.kt
DatabaseQualityTrend.kt
DatabaseQualityTrendJsonExporter.kt
DatabaseQualityReport.kt
DatabaseQualityReportBuilder.kt
DatabaseQualityReportCsvExporter.kt
DatabaseQualityReportHtmlExporter.kt
DatabaseQualityReportJsonExporter.kt
DatabaseQualityReportJsonReader.kt
PostgreSqlDatabaseAdapter.kt
QualityFinding.kt
QualityRules.kt
RuntimeConfig.kt
main.kt
Tests:
src/test/kotlin/com/alpinedigitalexperts/databaseanalyser/
Important command-line configuration, historical-comparison, and historical-trend tests include:
CommandLineConfigTest.kt
RuntimeConfigTest.kt
DatabaseQualityComparatorTest.kt
DatabaseQualityComparisonJsonExporterTest.kt
DatabaseQualityReportJsonReaderTest.kt
DatabaseQualityTrendAnalyzerTest.kt
DatabaseQualityTrendJsonExporterTest.kt
Reproducible SQL test fixtures:
src/test/resources/sql/
Docker Compose environment and base database initialization:
compose.yml
docker/init/01-base-schema.sql
Contract snapshot:
contracts/workbench-catalog/
Detailed project documentation:
docs/PROJECT_DOCUMENTATION.md
Security
Database credentials must not be committed.
In particular:
DB_PASSWORD
is obtained from the environment.
The command-line interface deliberately does not provide a password argument. Database passwords therefore remain environment-based rather than being passed as ordinary command-line arguments.
Generated output, IDE files, build files, and local secret/environment files should remain excluded according to the repository's .gitignore.
Future work
The current PostgreSQL implementation provides the core metadata, dependency, quality-analysis, aggregate-analysis, reporting, Application Catalog, configurable quality-rule, command-line configuration, historical quality-report comparison, and multi-snapshot historical trend-analysis foundation.
Possible future extensions include:
- proving more precise routine access modes such as readsData, writesData, createsData, and deletesData where PostgreSQL evidence permits;
- triggers, jobs, schedules, tasks, synonyms, queues, and other supported database objects;
- inferred semantic relationship candidates;
- privacy/PII classification with provenance and confidence;
- additional database-engine adapters;
- richer historical trend analysis, including snapshot timestamps, source-report provenance, and visualization;
- additional statistical analysis;
- graphical reporting; and
- additional CI/CD automation.
  Any future Application Catalog work must remain aligned with the pinned Workbench contract.
  Documentation
  For the full chronological implementation history, design decisions, tests, encountered problems, and current project status, see:
  [`docs/PROJECT_DOCUMENTATION.md`](docs/PROJECT_DOCUMENTATION.md)
