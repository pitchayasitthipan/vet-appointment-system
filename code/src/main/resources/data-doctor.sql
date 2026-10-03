-- ตัวอย่างข้อมูลสัตวแพทย์และตารางเวร (Sample Doctor & Work Schedule Data)
-- สามารถนำไปใส่ใน PostgreSQL หรือ MySQL ได้ตามโครงสร้างตาราง doctor

-- แบบที่ 1: เก็บ work_schedule เป็นข้อความอ่านง่าย (Plain Text)
INSERT INTO doctor (first_name, last_name, specialization, phone, email, work_schedule)
VALUES 
('นันทิดา', 'รักษ์สัตว์', 'อายุรกรรมทั่วไป (General Medicine)', '081-111-2233', 'nantida.r@vetclinic.com', 'จันทร์ - ศุกร์: 09:00 - 17:00'),
('กิตติศักดิ์', 'เจริญกิจ', 'ศัลยกรรมและกระดูก (Surgery & Orthopedics)', '082-222-3344', 'kittisak.k@vetclinic.com', 'อังคาร - เสาร์: 10:00 - 19:00'),
('วรรณภา', 'ใจดี', 'วัคซีนและผิวหนังสัตว์ (Vaccination & Dermatology)', '083-333-4455', 'wannapa.j@vetclinic.com', 'พุธ - อาทิตย์: 09:00 - 18:00');

-- แบบที่ 2: หรือหากกลุ่มต้องการเก็บเป็น JSON String ในคอลัมน์ TEXT เพื่อให้นำไป parse ต่อใน Frontend หรือระบบนัดหมายได้สะดวก
-- INSERT INTO doctor (first_name, last_name, specialization, phone, email, work_schedule)
-- VALUES 
-- ('นันทิดา', 'รักษ์สัตว์', 'อายุรกรรมทั่วไป', '081-111-2233', 'nantida.r@vetclinic.com', 
--  '[{"day":"MONDAY","startTime":"09:00","endTime":"17:00"},{"day":"TUESDAY","startTime":"09:00","endTime":"17:00"},{"day":"WEDNESDAY","startTime":"09:00","endTime":"17:00"},{"day":"THURSDAY","startTime":"09:00","endTime":"17:00"},{"day":"FRIDAY","startTime":"09:00","endTime":"17:00"}]');
