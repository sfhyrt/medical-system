-- Run once against MySQL 8.0+. Existing databases and rows are preserved.
CREATE DATABASE IF NOT EXISTS medical_system CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE medical_system;
CREATE TABLE IF NOT EXISTS department (
 id BIGINT NOT NULL AUTO_INCREMENT, code VARCHAR(32) NOT NULL, name VARCHAR(80) NOT NULL,
 location VARCHAR(120) NOT NULL, phone VARCHAR(11) NOT NULL, description VARCHAR(500) NOT NULL DEFAULT '',
 PRIMARY KEY(id), UNIQUE KEY uk_department_code(code), UNIQUE KEY uk_department_name(name),
 CONSTRAINT ck_department_phone CHECK(phone REGEXP '^1[3-9][0-9]{9}$')
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS doctor (
 id BIGINT NOT NULL AUTO_INCREMENT, code VARCHAR(32) NOT NULL, name VARCHAR(80) NOT NULL,
 department_id BIGINT NOT NULL, title VARCHAR(50) NOT NULL, phone VARCHAR(11) NOT NULL,
 status INT NOT NULL DEFAULT 1, PRIMARY KEY(id), UNIQUE KEY uk_doctor_code(code),
 KEY idx_doctor_department_status(department_id,status), KEY idx_doctor_name(name),
 CONSTRAINT fk_doctor_department FOREIGN KEY(department_id) REFERENCES department(id) ON DELETE RESTRICT,
 CONSTRAINT ck_doctor_status CHECK(status IN (0,1)), CONSTRAINT ck_doctor_phone CHECK(phone REGEXP '^1[3-9][0-9]{9}$')
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS patient (
 id BIGINT NOT NULL AUTO_INCREMENT, name VARCHAR(80) NOT NULL, id_card CHAR(18) NOT NULL,
 phone VARCHAR(11) NOT NULL, address VARCHAR(200) NOT NULL DEFAULT '', PRIMARY KEY(id),
 UNIQUE KEY uk_patient_identity(id_card), KEY idx_patient_name(name), KEY idx_patient_phone(phone),
 CONSTRAINT ck_patient_phone CHECK(phone REGEXP '^1[3-9][0-9]{9}$'),
 CONSTRAINT ck_patient_identity CHECK(id_card REGEXP '^[1-9][0-9]{16}[0-9X]$')
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS registration (
 id BIGINT NOT NULL AUTO_INCREMENT, registration_no VARCHAR(40) NOT NULL, patient_id BIGINT NOT NULL,
 doctor_id BIGINT NOT NULL, visit_time DATETIME NOT NULL, status VARCHAR(16) NOT NULL DEFAULT 'BOOKED',
 PRIMARY KEY(id), UNIQUE KEY uk_registration_no(registration_no),
 KEY idx_registration_patient_time(patient_id,visit_time), KEY idx_registration_doctor_time(doctor_id,visit_time),
 CONSTRAINT fk_registration_patient FOREIGN KEY(patient_id) REFERENCES patient(id) ON DELETE RESTRICT,
 CONSTRAINT fk_registration_doctor FOREIGN KEY(doctor_id) REFERENCES doctor(id) ON DELETE RESTRICT,
 CONSTRAINT ck_registration_status CHECK(status IN ('BOOKED','COMPLETED','CANCELLED'))
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS prescription (
 id BIGINT NOT NULL AUTO_INCREMENT, prescription_no VARCHAR(40) NOT NULL, registration_id BIGINT NOT NULL,
 diagnosis VARCHAR(500) NOT NULL, issued_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(id), UNIQUE KEY uk_prescription_no(prescription_no), UNIQUE KEY uk_prescription_registration(registration_id),
 CONSTRAINT fk_prescription_registration FOREIGN KEY(registration_id) REFERENCES registration(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS medicine (
 id BIGINT NOT NULL AUTO_INCREMENT, code VARCHAR(32) NOT NULL, name VARCHAR(100) NOT NULL, unit VARCHAR(20) NOT NULL,
 PRIMARY KEY(id), UNIQUE KEY uk_medicine_code(code)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS prescription_item (
 id BIGINT NOT NULL AUTO_INCREMENT, prescription_id BIGINT NOT NULL, line_no INT NOT NULL,
 medicine_id BIGINT NOT NULL, dosage VARCHAR(100) NOT NULL, frequency VARCHAR(100) NOT NULL,
 days INT NOT NULL, quantity DECIMAL(10,2) NOT NULL, PRIMARY KEY(id),
 UNIQUE KEY uk_prescription_line(prescription_id,line_no), KEY idx_item_medicine(medicine_id),
 CONSTRAINT fk_item_prescription FOREIGN KEY(prescription_id) REFERENCES prescription(id) ON DELETE RESTRICT,
 CONSTRAINT fk_item_medicine FOREIGN KEY(medicine_id) REFERENCES medicine(id) ON DELETE RESTRICT,
 CONSTRAINT ck_item_values CHECK(line_no>0 AND days>0 AND quantity>0)
) ENGINE=InnoDB;
