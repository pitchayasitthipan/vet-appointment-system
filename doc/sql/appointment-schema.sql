-- PostgreSQL reference schema. Requires Pet and Doctor tables from the integrated modules.
-- For a fresh schema or a separately reviewed migration; application development uses Hibernate update.
CREATE TABLE IF NOT EXISTS appointment (
    appointment_id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL REFERENCES pet(pet_id),
    doctor_id BIGINT NOT NULL REFERENCES doctor(doctor_id),
    appointment_date_time TIMESTAMP NOT NULL,
    service_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    symptoms VARCHAR(1000),
    preparation_instructions VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_appointment_doctor_time ON appointment(doctor_id, appointment_date_time);
CREATE INDEX IF NOT EXISTS idx_appointment_pet_time ON appointment(pet_id, appointment_date_time);
CREATE INDEX IF NOT EXISTS idx_pet_owner ON pet(owner_id);
