CREATE SCHEMA IF NOT EXISTS reporting;


-- ============================================================
-- PUBLIC.DEPARTMENTS
-- ============================================================

CREATE TABLE IF NOT EXISTS public.departments (
                                                  department_id SERIAL PRIMARY KEY,
                                                  department_name VARCHAR(100) NOT NULL,
    location VARCHAR(100)
    );


-- ============================================================
-- PUBLIC.EMPLOYEES
-- ============================================================

CREATE TABLE IF NOT EXISTS public.employees (
                                                employee_id SERIAL PRIMARY KEY,
                                                first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    salary NUMERIC(12, 2),
    hire_date DATE,
    department_id INTEGER,
    CONSTRAINT employees_email_key UNIQUE (email),
    CONSTRAINT fk_employee_department
    FOREIGN KEY (department_id)
    REFERENCES public.departments(department_id)
    );


-- ============================================================
-- REPORTING.MONTHLY_SALES
-- ============================================================

CREATE TABLE IF NOT EXISTS reporting.monthly_sales (
                                                       sale_id SERIAL PRIMARY KEY,
                                                       region VARCHAR(100) NOT NULL,
    total_amount NUMERIC(12, 2),
    report_date DATE
    );


-- ============================================================
-- SEED DEPARTMENTS
-- ============================================================

INSERT INTO public.departments (
    department_name,
    location
)
VALUES
    ('Engineering', 'Kampala'),
    ('Finance', 'Kampala'),
    ('Human Resources', 'Entebbe')
    ON CONFLICT DO NOTHING;


-- ============================================================
-- SEED EMPLOYEES
-- ============================================================

INSERT INTO public.employees (
    first_name,
    last_name,
    email,
    salary,
    hire_date,
    department_id
)
VALUES
    (
        'John',
        'Mugisha',
        'john@example.com',
        2500000.00,
        '2024-01-15',
        1
    ),
    (
        'Sarah',
        'Nankya',
        'sarah@example.com',
        3200000.00,
        '2023-06-10',
        2
    ),
    (
        'David',
        'Okello',
        'david@example.com',
        2800000.00,
        '2024-03-20',
        1
    )
    ON CONFLICT DO NOTHING;


-- ============================================================
-- SEED MONTHLY SALES
-- ============================================================

INSERT INTO reporting.monthly_sales (
    region,
    total_amount,
    report_date
)
VALUES
    ('Central', 1500000.00, '2026-08-01'),
    ('Eastern', 2100000.00, '2026-08-01'),
    ('Western', 1800000.00, '2026-08-01')
    ON CONFLICT DO NOTHING;
