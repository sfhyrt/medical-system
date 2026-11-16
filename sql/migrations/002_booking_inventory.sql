-- Apply through scripts/migrate-database.ps1 for restart-safe metadata checks.
USE medical_system;
CREATE TABLE IF NOT EXISTS doctor_schedule (
 id BIGINT NOT NULL AUTO_INCREMENT, doctor_id BIGINT NOT NULL, visit_date DATE NOT NULL,
 session VARCHAR(2) NOT NULL, capacity INT NOT NULL, remaining INT NOT NULL, enabled INT NOT NULL DEFAULT 1,
 PRIMARY KEY(id), UNIQUE KEY uk_schedule_doctor_session(doctor_id,visit_date,session), KEY idx_schedule_date(visit_date,enabled),
 CONSTRAINT fk_schedule_doctor FOREIGN KEY(doctor_id) REFERENCES doctor(id) ON DELETE RESTRICT,
 CONSTRAINT ck_schedule_session CHECK(session IN ('AM','PM')),
 CONSTRAINT ck_schedule_quota CHECK(capacity>0 AND remaining>=0 AND remaining<=capacity),
 CONSTRAINT ck_schedule_enabled CHECK(enabled IN (0,1))
) ENGINE=InnoDB;
ALTER TABLE registration ADD COLUMN schedule_id BIGINT NULL;
ALTER TABLE registration ADD COLUMN active_patient_id BIGINT GENERATED ALWAYS AS (CASE WHEN status <> 'CANCELLED' THEN patient_id ELSE NULL END) STORED;
ALTER TABLE registration ADD CONSTRAINT fk_registration_schedule FOREIGN KEY(schedule_id) REFERENCES doctor_schedule(id) ON DELETE RESTRICT;
ALTER TABLE registration ADD CONSTRAINT uk_registration_active UNIQUE(active_patient_id,schedule_id);
ALTER TABLE medicine ADD COLUMN stock DECIMAL(12,2) NOT NULL DEFAULT 0;
ALTER TABLE medicine ADD CONSTRAINT ck_medicine_stock CHECK(stock>=0);
CREATE TABLE IF NOT EXISTS schema_change (version VARCHAR(50) PRIMARY KEY, applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB;
INSERT IGNORE INTO schema_change(version) VALUES('002_booking_inventory');
