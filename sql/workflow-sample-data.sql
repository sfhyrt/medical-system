-- Synthetic records for local feature previews. Existing inventory and quotas are never reset.
USE medical_system;
INSERT IGNORE INTO medicine(code,name,unit,stock) VALUES
('MED-DEV-A','模拟药品 A','盒',40.00),('MED-DEV-B','模拟药品 B','瓶',1.00),('MED-DEV-C','模拟药品 C','盒',0.00);
INSERT IGNORE INTO doctor_schedule(doctor_id,visit_date,session,capacity,remaining,enabled)
SELECT id,DATE_ADD(CURRENT_DATE,INTERVAL 1 DAY),'AM',8,8,1 FROM doctor WHERE code='DOC001' AND status=1;
INSERT IGNORE INTO doctor_schedule(doctor_id,visit_date,session,capacity,remaining,enabled)
SELECT id,DATE_ADD(CURRENT_DATE,INTERVAL 1 DAY),'PM',5,5,1 FROM doctor WHERE code='DOC002' AND status=1;
