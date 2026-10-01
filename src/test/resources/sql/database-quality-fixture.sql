DROP SCHEMA IF EXISTS quality_test CASCADE;

CREATE SCHEMA quality_test;


-- ============================================================
-- 1. PARENT TABLE
-- ============================================================

CREATE TABLE quality_test.departments (
                                          department_id INTEGER PRIMARY KEY,
                                          department_name VARCHAR(100) NOT NULL
);

INSERT INTO quality_test.departments (
    department_id,
    department_name
)
VALUES
    (1, 'Engineering'),
    (2, 'Finance'),
    (3, 'Human Resources'),
    (4, 'Operations');


-- ============================================================
-- 2. MAIN QUALITY TEST TABLE
-- ============================================================

CREATE TABLE quality_test.employees (
                                        employee_id INTEGER PRIMARY KEY,
                                        first_name VARCHAR(100) NOT NULL,
                                        last_name VARCHAR(100) NOT NULL,
                                        email VARCHAR(255),
                                        salary NUMERIC(12, 2),
                                        hire_date DATE,
                                        department_id INTEGER
);


-- ============================================================
-- 3. CONTROLLED TEST DATA
--
-- This dataset intentionally contains planted defects:
--
-- - 20 total rows
-- - 6 NULL salaries = 30% NULL
-- - 2 malformed emails
-- - 1 numeric outlier
-- - 1 future hire date
-- - 1 orphaned foreign-key value
-- - duplicate business records
-- - multiple rows per department for grouped analysis
-- ============================================================

INSERT INTO quality_test.employees (
    employee_id,
    first_name,
    last_name,
    email,
    salary,
    hire_date,
    department_id
)
VALUES
    (1,  'Alice',   'Auma',      'alice@example.com',      1000.00, '2024-01-10', 1),
    (2,  'Brian',   'Bukenya',   'brian@example.com',      1020.00, '2024-01-15', 1),
    (3,  'Carol',   'Nabirye',   'carol@example.com',      1040.00, '2024-02-01', 1),
    (4,  'Daniel',  'Okello',    'daniel@example.com',     1060.00, '2024-02-10', 1),
    (5,  'Evelyn',  'Nansubuga', 'evelyn@example.com',     1080.00, '2024-03-01', 1),

    (6,  'Frank',   'Kato',      'frank@example.com',      1100.00, '2024-03-10', 2),
    (7,  'Grace',   'Nakato',    'grace@example.com',      1120.00, '2024-04-01', 2),
    (8,  'Henry',   'Mugisha',   'henry.example.com',      1140.00, '2024-04-10', 2),
    (9,  'Irene',   'Namukasa',  'irene@example.com',      1160.00, '2024-05-01', 2),
    (10, 'John',    'Ssekandi',  'john@example',           1180.00, '2024-05-10', 2),

    (11, 'Kevin',   'Muwanga',   'kevin@example.com',          NULL, '2024-06-01', 3),
    (12, 'Linda',   'Nabukenya', 'linda@example.com',          NULL, '2024-06-10', 3),
    (13, 'Martin',  'Kizza',     'martin@example.com',         NULL, '2024-07-01', 3),
    (14, 'Nancy',   'Atim',      'nancy@example.com',          NULL, '2024-07-10', 3),
    (15, 'Oscar',   'Tumusiime', 'oscar@example.com',          NULL, '2024-08-01', 3),

    (16, 'Peter',   'Ouma',      'peter@example.com',          NULL, '2024-08-10', 4),
    (17, 'Queen',   'Achen',     'queen@example.com',       1200.00, '2024-09-01', 4),
    (18, 'Robert',  'Mwesigwa',  'robert@example.com',      1220.00, '2024-09-10', 4),

    -- Deliberate duplicate business record of row 18.
    -- Only the primary key differs.
    (19, 'Robert',  'Mwesigwa',  'robert@example.com',      1220.00, '2024-09-10', 4),

    -- Deliberate extreme outlier + future date + orphan FK.
    (20, 'Sarah',   'Nankya',    'sarah@example.com',      50000.00, '2035-01-01', 999);


-- ============================================================
-- 4. DECLARED FOREIGN KEY WITH EXISTING ORPHAN
--
-- NOT VALID allows PostgreSQL to create the constraint while
-- retaining the deliberately planted department_id = 999 row.
-- New rows must still satisfy the constraint.
-- ============================================================

ALTER TABLE quality_test.employees
    ADD CONSTRAINT fk_quality_employee_department
        FOREIGN KEY (department_id)
            REFERENCES quality_test.departments(department_id)
    NOT VALID;


-- ============================================================
-- 5. GROUPED AGGREGATE TEST TABLE
-- ============================================================

CREATE TABLE quality_test.aggregate_analysis (
                                                 test_id INTEGER PRIMARY KEY,
                                                 category VARCHAR(100) NOT NULL,
                                                 amount NUMERIC(12, 2) NOT NULL
);

INSERT INTO quality_test.aggregate_analysis (
    test_id,
    category,
    amount
)
VALUES
    (1,  'Food',      100.00),
    (2,  'Food',      120.00),
    (3,  'Food',      140.00),
    (4,  'Food',      160.00),
    (5,  'Food',      180.00),

    (6,  'Books',      50.00),
    (7,  'Books',      60.00),
    (8,  'Books',      70.00),
    (9,  'Books',      80.00),
    (10, 'Books',      90.00),

    (11, 'Transport',  30.00),
    (12, 'Transport',  40.00),
    (13, 'Transport',  50.00),
    (14, 'Transport',  60.00),
    (15, 'Transport',  70.00);
