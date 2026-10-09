-- ตัวอย่างข้อมูลสัตวแพทย์และตารางเวร (Sample Doctor & Work Schedule Data)
-- สำหรับฐานข้อมูล PostgreSQL ประจำตาราง doctor
-- เบอร์โทรเก็บเป็นตัวเลขล้วน (ตรงกับ DoctorRequestDTO) หน้าเว็บจัดรูปแบบมีขีดให้เอง
-- ตารางเวรเขียนแบบ "วันเริ่ม - วันสุดท้าย: เวลา (ห้อง)" เพื่อให้หน้า Veterinarians คำนวณวันออกตรวจได้

CREATE TABLE IF NOT EXISTS doctor (
    doctor_id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    work_schedule TEXT
);

INSERT INTO doctor (first_name, last_name, specialization, phone, email, work_schedule)
VALUES
('นันทิดา', 'รักษ์สัตว์', 'อายุรกรรมทั่วไป, โรคผิวหนัง', '0811112233', 'nantida.r@vetclinic.com', 'จันทร์ - ศุกร์: 09:00 - 17:00 น. (ห้องตรวจ 1)'),
('กิตติศักดิ์', 'เจริญกิจ', 'ศัลยกรรม, กระดูกและข้อ', '0822223344', 'kittisak.k@vetclinic.com', 'อังคาร - เสาร์: 10:00 - 19:00 น. (ห้องผ่าตัด)'),
('วรรณภา', 'ใจดี', 'วัคซีน, เวชศาสตร์ป้องกัน', '0833334455', 'wannapa.j@vetclinic.com', 'พุธ - อาทิตย์: 09:00 - 18:00 น. (ห้องตรวจ 2)'),
('ธนวัฒน์', 'อภิญญากุล', 'อายุรกรรม, หัวใจและทางเดินหายใจ', '0844445566', 'thanawat.a@vetclinic.com', 'ศุกร์ - อังคาร: 09:00 - 18:00 น. (ห้องตรวจ 3)'),
('ปรียาภรณ์', 'ตั้งพงษ์ศิริ', 'สัตว์เลี้ยงพิเศษ, ทันตกรรม', '0855556677', 'preeyaporn.t@vetclinic.com', 'จันทร์, พุธ, ศุกร์: 13:00 - 20:00 น. (ห้องตรวจ 2)')
ON CONFLICT (email) DO NOTHING;
