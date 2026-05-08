CREATE TABLE patients (
    id UUID PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    date_of_birth DATE,
    phone VARCHAR(50)
);

CREATE TABLE visits (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    reason VARCHAR(255) NOT NULL,
    notes TEXT,
    CONSTRAINT fk_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);
