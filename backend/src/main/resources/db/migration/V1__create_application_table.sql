CREATE TABLE application (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    company VARCHAR(150) NOT NULL,
    job_title VARCHAR(150) NOT NULL,
    job_url TEXT NOT NULL,
    source VARCHAR(30),
    work_model VARCHAR(30),
    seniority VARCHAR(30),
    location VARCHAR(150),
    salary NUMERIC(10,2),
    tech_stack TEXT,
    job_description TEXT,
    status VARCHAR(30) NOT NULL,
    application_deadline DATE,
    applied_on DATE,
    total_stages INTEGER,
    notes TEXT
);