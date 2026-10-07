-- ตัวอย่างข้อมูลสัตวแพทย์และตารางเวร (Sample Doctor & Work Schedule Data)
-- สำหรับฐานข้อมูล PostgreSQL ประจำตาราง doctor

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
('นันทิดา', 'รักษ์สัตว์', 'อายุรกรรมทั่วไป (General Medicine)', '081-111-2233', 'nantida.r@vetclinic.com', 'จันทร์ - ศุกร์: 09:00 - 17:00'),
('กิตติศักดิ์', 'เจริญกิจ', 'ศัลยกรรมและกระดูก (Surgery & Orthopedics)', '082-222-3344', 'kittisak.k@vetclinic.com', 'อังคาร - เสาร์: 10:00 - 19:00'),
('วรรณภา', 'ใจดี', 'วัคซีนและผิวหนังสัตว์ (Vaccination & Dermatology)', '083-333-4455', 'wannapa.j@vetclinic.com', 'พุธ - อาทิตย์: 09:00 - 18:00')
ON CONFLICT (email) DO NOTHING;
