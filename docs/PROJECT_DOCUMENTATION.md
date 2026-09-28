# Database Analyser – Project Documentation

## 1. Introduction

The Database Analyser is a Kotlin application developed to inspect PostgreSQL databases, discover database metadata, analyse data quality, perform aggregate analysis, and generate structured quality reports.

The project is also designed to work within the Application Catalog contract defined by the Legacy Modernization Workbench.

The development approach has been incremental. Each major capability was implemented, tested, and verified before moving to the next stage.

---

## 2. Project Objectives

The main objectives of the Database Analyser are to:

- connect securely to a PostgreSQL database;
- discover database schemas and objects;
- identify tables and their columns;
- identify primary keys;
- identify unique constraints;
- identify foreign-key relationships;
- validate foreign-key references;
- analyse NULL and non-NULL values;
- identify distinct and repeated values;
- identify potential duplicate records;
- detect text-quality problems;
- validate email formats;
- analyse numeric data;
- perform aggregate analysis;
- detect numeric anomalies and potential outliers;
- analyse date values;
- generate an overall database-quality assessment;
- export analysis results in multiple formats; and
- provide automated tests for the implemented functionality.

---

## 3. Technology Stack

The project uses the following technologies:

| Technology | Purpose |
|---|---|
| Kotlin | Main programming language |
| Gradle Kotlin DSL | Build and dependency management |
| JDK 21 | Java runtime baseline |
| PostgreSQL | Current supported database |
| JDBC | Database connectivity |
| Docker | Local PostgreSQL development environment |
| Kotlin Serialization | Structured report serialization |
| Kotlin Test | Automated testing |
| IntelliJ IDEA | Development environment |
| Git | Version control |
| GitHub | Repository hosting and review |

---

## 4. Initial Project Setup

The project was configured as a Kotlin JVM application using Gradle.

The main source code is located under:

```text
src/main/kotlin/com/alpinedigitalexperts/databaseanalyser/
```

Tests are located under:

```text
src/test/kotlin/com/alpinedigitalexperts/databaseanalyser/
```

The application entry point is:

```text
com.alpinedigitalexperts.databaseanalyser.MainKt
```

The PostgreSQL JDBC driver is included as a project dependency so that the Kotlin application can communicate with PostgreSQL.

---

## 5. PostgreSQL Development Environment

PostgreSQL was selected as the first database engine for development.

A PostgreSQL 16 instance was run using Docker.

The development container is named:

```text
database-analyser-postgres
```

PostgreSQL is exposed on the standard port:

```text
5432
```

The development database used for analysis is:

```text
companydb
```

Docker made it possible to maintain a repeatable local database environment while developing and testing the analyser.

The running container can be checked using:

```bash
docker ps
```

The development container can be started using:

```bash
docker start database-analyser-postgres
```

---

## 6. Database Configuration

Database configuration was separated from the main analysis logic.

This responsibility is handled by:

```text
DatabaseConfig.kt
```

Configuration values are obtained from environment variables, including:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USER
DB_PASSWORD
```

An important security decision was not to store the database password directly in the source code.

The password is obtained from:

```text
DB_PASSWORD
```

This allows the source code to be committed without publishing the database password.

---

## 7. Database Connection

Database connection handling is implemented in:

```text
DatabaseConnector.kt
```

The connector uses JDBC together with the configuration values to establish a PostgreSQL connection.

The connection is created before database analysis begins.

During successful execution, the application confirms that the database connection has been established.

---

## 8. Database Metadata Architecture

As development progressed, database metadata access was separated from the higher-level analysis logic.

The important components are:

```text
DatabaseMetadataAdapter.kt
PostgreSqlDatabaseAdapter.kt
DatabaseAnalyzer.kt
```

### DatabaseMetadataAdapter

`DatabaseMetadataAdapter` defines the metadata operations required by the analyser.

### PostgreSqlDatabaseAdapter

`PostgreSqlDatabaseAdapter` implements PostgreSQL-specific metadata behaviour.

### DatabaseAnalyzer

`DatabaseAnalyzer` performs higher-level database analysis using the metadata adapter and JDBC connection.

This separation is important because the core analyser does not need to contain all PostgreSQL-specific metadata logic.

It also provides a foundation for supporting additional database engines through other adapters in the future.

---

## 9. Schema Discovery

The first versions of the analyser focused primarily on tables in the normal PostgreSQL `public` schema.

The project was later extended to support schema-aware discovery.

The development database currently demonstrates this using:

```text
public
reporting
```

Tables successfully discovered include:

```text
public.departments
public.employees
reporting.monthly_sales
```

This verifies that the analyser is not restricted to the `public` schema.

A dedicated integration test was added for non-public-schema behaviour.

---

## 10. Table Discovery

The analyser retrieves the available tables from PostgreSQL.

For each table, schema information is retained so that tables can be referenced using qualified names such as:

```text
public.employees
reporting.monthly_sales
```

Schema qualification prevents ambiguity when different schemas contain objects with the same name.

---

## 11. Column Discovery

After finding tables, the analyser retrieves their columns.

Column metadata includes information such as:

- column name;
- PostgreSQL data type;
- nullable status; and
- default value.

For example, the `employees` table contains columns such as:

```text
employee_id
first_name
last_name
email
salary
hire_date
department_id
```

This metadata becomes the basis for later data-quality analysis.

---

## 12. Primary-Key Detection

Primary-key discovery was added so that the analyser could understand the identity structure of each table.

For example:

```text
public.employees
Primary Key: employee_id
```

and:

```text
public.departments
Primary Key: department_id
```

Primary-key information is important for:

- understanding table identity;
- distinguishing identifier columns;
- duplicate analysis; and
- detecting tables without primary keys.

Primary-key issues are included in the final quality summary.

---

## 13. Unique-Constraint Detection

The analyser was extended to discover unique columns.

For example:

```text
public.employees.email
```

is protected by a UNIQUE constraint in the development database.

Knowing whether a column is unique allows the analyser to interpret repeated values correctly.

A duplicate in a UNIQUE column is different from normal repetition in an ordinary column.

---

## 14. Foreign-Key Detection

Foreign-key discovery was added to identify relationships between tables.

For example:

```text
public.employees.department_id
    ->
public.departments.department_id
```

The analyser records the relationship between the source and referenced columns.

This allows database relationships to be identified automatically rather than being manually configured.

---

## 15. Foreign-Key Quality Validation

Detecting a foreign-key definition alone is not enough for data-quality analysis.

The analyser therefore checks whether child values have matching values in the referenced parent table.

For the development database, the `employees.department_id` relationship has been verified successfully.

When all references are valid, the analyser reports that no invalid foreign-key values were found.

Foreign-key problems are included in the overall database quality report.

---

## 16. Row Count Analysis

Row counting was added to establish how much data exists in each table.

Conceptually, the operation is:

```sql
COUNT(*)
```

The row count is used throughout later analysis.

For example, it provides the denominator needed to calculate NULL percentages.

---

## 17. NULL Value Analysis

The analyser was extended to inspect missing values in every column.

For each column, it calculates:

- row count;
- non-NULL count;
- NULL count; and
- NULL percentage.

The main SQL aggregate concepts are:

```sql
COUNT(*)
COUNT(column)
```

Because `COUNT(column)` ignores NULL values, it provides the non-NULL count.

The NULL percentage can then be calculated from the total row count and NULL count.

This makes it possible to identify columns with a high proportion of missing data.

---

## 18. Distinct Value Analysis

The analyser calculates distinct-value counts using the equivalent of:

```sql
COUNT(DISTINCT column)
```

This helps determine whether:

- every value is unique;
- values are repeated; or
- the column contains only one distinct value.

A column containing one value across all analysed rows can be reported as a constant-value condition.

---

## 19. Duplicate Value Analysis

Duplicate-value analysis was added after distinct analysis.

The analyser does not treat every repeated value as an error.

Interpretation depends on the role of the column.

### Primary-key columns

Primary-key values should be unique.

### Unique columns

Columns protected by UNIQUE constraints should not contain duplicate non-NULL values.

### Foreign-key columns

Repeated values are normally allowed because several child rows may reference the same parent row.

### Ordinary columns

Repeated values may be informational rather than errors.

This context-aware approach avoids incorrectly reporting valid repeated data as a database problem.

---

## 20. Potential Duplicate Record Analysis

Column-level duplicate detection does not necessarily identify duplicate business records.

The analyser was therefore extended to check for potential duplicate records.

This analysis compares relevant non-key business data to identify records that may represent the same logical entity.

Potential duplicate records are reported separately from ordinary repeated column values.

---

## 21. Empty-String Analysis

Text columns were analysed for empty strings.

An empty string is different from a SQL NULL value.

For example:

```text
''
```

contains no characters but still represents a stored value.

This analysis helps identify missing information that would not be found by NULL analysis alone.

---

## 22. Whitespace Analysis

Text-quality analysis was expanded to identify:

- whitespace-only values; and
- leading or trailing whitespace.

For example, a value containing only spaces may visually appear empty even though it is not NULL and is not technically an empty string.

Leading and trailing spaces can also cause comparison, searching, and reporting problems.

---

## 23. Email Format Analysis

The analyser includes email-format validation for columns identified as email-related.

This detects values that do not match the expected email format.

Email-format problems are included in the overall quality summary.

This is a format-quality check rather than proof that an email address actually exists.

---

## 24. Numeric Data Analysis

Numeric columns were initially analysed using statistics such as:

```text
Minimum
Maximum
Average
Total
```

These correspond to SQL aggregate operations:

```sql
MIN(column)
MAX(column)
AVG(column)
SUM(column)
```

This provides useful information about the range and distribution of numeric business data.

---

## 25. Aggregate Analysis

Aggregate analysis was expanded as part of the later development work.

For appropriate numeric business columns, the analyser now provides:

```sql
COUNT(column)
COUNT(DISTINCT column)
MIN(column)
MAX(column)
AVG(column)
SUM(column)
```

The report presents these as:

- Non-NULL Count;
- Distinct Count;
- Minimum;
- Maximum;
- Average; and
- Total.

For example, the latest verified analysis of `employees.salary` produced:

```text
Non-NULL Count: 3
Distinct Count: 3
Minimum: 2500000.00
Maximum: 3200000.00
Average: 2833333.333333333333
Total: 8500000.00
```

Aggregate analysis is applied to appropriate numeric business data rather than treating identifier columns as normal measurements.

This prevents values such as employee IDs from being interpreted as meaningful business totals or averages.

---

## 26. Numeric Anomaly Detection

Numeric-quality analysis was expanded to identify suspicious values.

The analyser currently checks for negative numeric values where numeric anomaly analysis is applicable.

This was tested during development by temporarily introducing negative data and confirming that the analyser reported the anomaly.

The test data was later restored to valid values.

---

## 27. Potential Numeric Outlier Detection

The analyser was extended beyond simple negative-value checks to identify potential statistical outliers.

The implementation uses a median-based approach.

Median-based analysis is useful because it is less affected by extreme values than an average-based method.

Potential outliers are reported separately so that they can be reviewed rather than automatically treated as invalid data.

---

## 28. Date Analysis

Date columns are analysed to determine:

- earliest date; and
- latest date.

For example, analysis of `employees.hire_date` can show the range of employee hiring dates.

This provides a quick overview of the time period represented by the data.

---

## 29. Date Anomaly Analysis

The analyser also checks for future-date values.

During development, a future date was deliberately introduced to verify that the detection worked.

The test data was later restored.

Future dates are reported for review because whether a future date is invalid depends on the meaning of the field.

For example, a future hire date may be suspicious, while a future appointment date may be completely valid.

---

## 30. Database Quality Report Model

As the number of analyses increased, a structured report model was introduced.

The main report model is defined in:

```text
DatabaseQualityReport.kt
```

It stores the results of the analysis in structured Kotlin objects.

This was an important architectural step because it separated:

```text
Database analysis
        ↓
Structured report
        ↓
Different output formats
```

Without this separation, every exporter would need to perform or reconstruct the analysis independently.

---

## 31. Database Quality Report Builder

Report construction is handled by:

```text
DatabaseQualityReportBuilder.kt
```

The builder collects analysis results and constructs the final structured report.

The report contains database-level, table-level, and column-level information.

It also calculates summary issue counts.

---

## 32. Quality Findings

Quality findings are represented using:

```text
QualityFinding.kt
```

This provides a structured way of representing conditions discovered during analysis rather than relying only on terminal text.

---

## 33. Overall Quality Summary

The analyser produces an overall database-quality summary.

The summary includes categories such as:

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

The overall status provides a high-level indication of whether the results require review.

Current status values include:

```text
GOOD
REVIEW
ATTENTION REQUIRED
```

The detailed findings remain available so that the status can be understood rather than being treated as a standalone conclusion.

---

## 34. JSON Report Export

A JSON exporter was implemented in:

```text
DatabaseQualityReportJsonExporter.kt
```

The generated file is:

```text
output/database-quality-report.json
```

JSON provides a machine-readable representation of the analysis and can be consumed by other applications.

---

## 35. HTML Report Export

An HTML exporter was implemented in:

```text
DatabaseQualityReportHtmlExporter.kt
```

The generated file is:

```text
output/database-quality-report.html
```

The HTML format allows the report to be opened and reviewed directly in a web browser.

---

## 36. CSV Report Export

A CSV exporter was implemented in:

```text
DatabaseQualityReportCsvExporter.kt
```

The generated file is:

```text
output/database-quality-report.csv
```

The CSV report includes column-level information such as:

- row count;
- non-NULL count;
- NULL count;
- NULL percentage;
- distinct count;
- duplicate information;
- numeric aggregates;
- date information; and
- key information.

CSV output makes it possible to inspect the results using spreadsheet software and other data-processing tools.

---

## 37. Generated Output Handling

Generated reports are stored under:

```text
output/
```

The output directory is ignored by Git.

This keeps generated analysis results separate from the application's source code and prevents locally generated reports from being unnecessarily committed.

---

## 38. Automated Testing

Automated tests were added throughout development.

The current test suite includes:

```text
DatabaseAnalyzerIntegrationTest.kt
DatabaseAnalyzerMetadataAdapterTest.kt
DatabaseQualityReportBuilderIntegrationTest.kt
DatabaseQualityReportCsvExporterTest.kt
DatabaseQualityReportHtmlExporterTest.kt
DatabaseQualityReportJsonExporterTest.kt
NonPublicSchemaIntegrationTest.kt
PostgreSqlDatabaseAdapterIntegrationTest.kt
```

Testing covers areas including:

- database analysis;
- metadata discovery;
- metadata abstraction;
- PostgreSQL-specific behaviour;
- non-public schemas;
- report construction;
- JSON export;
- HTML export; and
- CSV export.

The complete test suite can be run using:

```powershell
.\gradlew.bat test
```

Successful runs produce:

```text
BUILD SUCCESSFUL
```

---

## 39. Development Test Database

The current development database contains three analysed tables:

```text
public.departments
public.employees
reporting.monthly_sales
```

### public.departments

The table contains three rows.

Important columns include:

```text
department_id
department_name
location
```

`department_id` is the primary key.

### public.employees

The table contains three rows.

Important columns include:

```text
employee_id
first_name
last_name
email
salary
hire_date
department_id
```

`employee_id` is the primary key.

`email` has a unique constraint.

`department_id` is a foreign key referencing:

```text
public.departments.department_id
```

### reporting.monthly_sales

This table demonstrates support for a schema other than `public`.

Important columns include:

```text
sale_id
region
total_amount
report_date
```

`total_amount` is used to demonstrate numeric aggregate analysis.

---

## 40. Latest Verified Analysis Result

The latest verified development run analysed:

```text
Tables analysed: 3
Columns analysed: 14
Rows analysed: 9
```

The quality summary reported:

```text
Primary Key Issues: 0
Foreign Key Issues: 0
Duplicate Value Issues: 0
Potential Duplicate Record Issues: 0
Empty String Issues: 0
Whitespace-Only Issues: 0
Leading/Trailing Whitespace Issues: 0
Email Format Issues: 0
Numeric Anomaly Issues: 0
Potential Numeric Outlier Issues: 0
Date Anomaly Issues: 0
Constant Value Issues: 1
High NULL Percentage Issues: 0
Total Issues: 1
Overall Status: REVIEW
```

The remaining issue was a constant-value condition in the development test data.

The run successfully generated:

```text
database-quality-report.json
database-quality-report.html
database-quality-report.csv
```

and completed successfully.

---

## 41. Workbench Application Catalog Contract

The repository contains a pinned snapshot of the Application Catalog contract from the Legacy Modernization Workbench.

The Workbench remains the semantic authority for the Application Catalog.

The pinned contract documentation is located under:

```text
contracts/workbench-catalog/
```

The repository's root `README.md`, `AGENTS.md`, and contract documentation contain the rules governing changes that affect the shared Application Catalog model.

The pinned Workbench commit is:

```text
732de4ddf73969b31d1791bf454b0d924459b9f2
```

The contract can be verified on Windows using:

```powershell
.\scripts\verify-workbench-contract.ps1
```

or as part of the Gradle verification process:

```powershell
.\gradlew.bat check
```

The analyser must not independently introduce Application Catalog semantics that are not supported by the pinned Workbench contract.

---

## 42. Problems Encountered During Development

Several useful problems were encountered during implementation.

### Duplicate UNIQUE value

While testing database inserts, PostgreSQL rejected an attempt to insert a duplicate email because the `email` column has a UNIQUE constraint.

This confirmed that the development database constraint was functioning correctly and reinforced the distinction between database constraints and analyser-level duplicate reporting.

### Deliberately invalid test data

During development, invalid data was intentionally introduced to verify detection logic.

Examples included:

- an empty first name;
- an invalid email format;
- a negative salary; and
- a future hire date.

The analyser successfully detected these conditions.

The data was later restored to valid values.

### Non-public schema support

The analyser initially focused on the `public` schema.

A `reporting` schema and `monthly_sales` table were later used to verify that schema-aware analysis worked correctly.

### Aggregate analysis

The original numeric analysis already calculated minimum, maximum, average, and total.

The implementation was later expanded to expose additional aggregate information, particularly:

```text
Non-NULL Count
Distinct Count
```

This completed the aggregate analysis currently required by the project.

### Report consistency

As new analysis fields were added, the structured report and exporters also had to be updated.

For example, adding non-NULL counts required changes to:

```text
DatabaseQualityReport.kt
DatabaseQualityReportBuilder.kt
DatabaseQualityReportCsvExporter.kt
DatabaseQualityReportHtmlExporter.kt
```

The JSON exporter automatically includes fields from the serialized report model.

Automated tests were used to verify that the changes remained consistent.

---

## 43. Security Considerations

Database credentials must not be committed to Git.

The application reads the database password from the environment:

```text
DB_PASSWORD
```

The `.gitignore` file excludes local environment and generated files.

Before the implementation was published to the development GitHub repository, the staged Git content was checked for common secret patterns.

No hardcoded database password, API key, or authentication token was intentionally included in the implementation commit.

---

## 44. Git and GitHub Workflow

Git is used for version control.

The local project maintains separate Git remotes for the developer repository and the original company repository.

The normal development workflow is:

```bash
git status
git add .
git commit -m "Describe the change"
git push
```

Changes should be tested before they are committed.

Generated reports and sensitive environment information should not be committed.

---

## 45. Current Project Status

The current implementation successfully provides:

- PostgreSQL database connectivity;
- environment-based configuration;
- database metadata discovery;
- schema-aware table discovery;
- column discovery;
- primary-key detection;
- unique-key detection;
- foreign-key detection;
- foreign-key validation;
- row counts;
- NULL analysis;
- non-NULL counts;
- NULL percentages;
- distinct-value analysis;
- duplicate-value analysis;
- potential duplicate-record analysis;
- empty-string analysis;
- whitespace analysis;
- email-format analysis;
- numeric statistics;
- aggregate analysis;
- numeric anomaly detection;
- potential numeric-outlier detection;
- date analysis;
- future-date detection;
- constant-value detection;
- structured database-quality reporting;
- JSON export;
- HTML export;
- CSV export; and
- automated tests.

The PostgreSQL analysis and data-quality foundation is therefore functional and tested.

---

## 46. Remaining and Future Work

The current implementation provides a strong foundation, but the repository's wider Workbench contract describes capabilities beyond the completed data-quality analyser.

Future work may include:

- completing Application Catalog graph export;
- validating exported catalog documents against the pinned schema;
- shared positive and negative fixture conformance;
- stable catalog identities across repeated analyses;
- richer dependency discovery;
- read/write/create/delete/call dependency representation;
- trigger and scheduling relationships;
- semantic relationship candidates;
- PII classification with provenance and confidence;
- support for additional database engines;
- configurable data-quality rules;
- command-line configuration;
- additional aggregate/statistical analysis;
- historical report comparison;
- graphical reporting; and
- CI/CD integration.

Any model-affecting Application Catalog work must continue to follow the pinned Workbench contract rather than introducing an independent semantic model.

---

## 47. Conclusion

The Database Analyser has progressed from basic PostgreSQL connectivity to a structured database-analysis application capable of discovering metadata, evaluating multiple dimensions of data quality, performing aggregate analysis, and exporting results in several formats.

The project was developed incrementally, with automated testing used to verify new functionality as it was introduced.

The current implementation provides the working PostgreSQL analysis and quality-reporting foundation. Future development can build on this foundation while remaining aligned with the pinned Legacy Modernization Workbench Application Catalog contract.
