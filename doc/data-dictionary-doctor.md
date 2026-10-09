# Data Dictionary: โมดูล Doctor (ข้อมูลสัตวแพทย์และตารางเวร)
**ผู้รับผิดชอบ:** สิทธิโชค (บูบู้) — รหัสนิสิต 6733804286  
**Branch:** `sitthichok_6733804286_04`  
**อ้างอิงจาก Entity:** `com.example.petclinic.domain.entity.Doctor` และไฟล์ `data-doctor.sql`

---

## ตารางที่ 4: `doctor`

### 1. วัตถุประสงค์ของตาราง (Purpose)
เก็บข้อมูลหลักของสัตวแพทย์ประจำคลินิก PawCare ข้อมูลความเชี่ยวชาญ ช่องทางการติดต่อ และตารางเวรปฏิบัติงาน เพื่อใช้สำหรับแสดงรายชื่อสัตวแพทย์บนหน้าเว็บ การค้นหา และให้เจ้าของสัตว์เลี้ยงใช้เลือกสัตวแพทย์ในขั้นตอนการจองนัดหมาย

---

### 2. รายละเอียดคอลัมน์ (Column Specifications)

| ลำดับ | ชื่อคอลัมน์ (Column Name) | ชนิดข้อมูล (PostgreSQL) | ชนิดข้อมูล (Java) | Key / Constraint | Nullable | ค่าเริ่มต้น (Default) | คำอธิบาย (Description) |
|:---:|:---|:---|:---|:---:|:---:|:---:|:---|
| 1 | `doctor_id` | `BIGSERIAL` | `Long` | **PK** | **No** | Auto Increment | รหัสประจำตัวสัตวแพทย์ (Primary Key รันอัตโนมัติ) |
| 2 | `first_name` | `VARCHAR(100)` | `String` | - | **No** | - | ชื่อจริงของสัตวแพทย์ (ห้ามว่าง) |
| 3 | `last_name` | `VARCHAR(100)` | `String` | - | **No** | - | นามสกุลของสัตวแพทย์ (ห้ามว่าง) |
| 4 | `specialization` | `VARCHAR(100)` | `String` | - | **Yes** | `NULL` | ความเชี่ยวชาญเฉพาะทาง (เช่น อายุรกรรมทั่วไป, ศัลยกรรมและกระดูก, ผิวหนัง) |
| 5 | `phone` | `VARCHAR(20)` | `String` | - | **No** | - | เบอร์โทรศัพท์สำหรับติดต่อ (ความยาวไม่เกิน 20 ตัวอักษร) |
| 6 | `email` | `VARCHAR(255)` | `String` | **UNIQUE** | **No** | - | อีเมลสำหรับติดต่อสัตวแพทย์ (ห้ามซ้ำกันในระบบ) |
| 7 | `work_schedule` | `TEXT` | `String` | - | **Yes** | `NULL` | ข้อมูลวันและเวลาเข้าเวร (เช่น "จันทร์ - ศุกร์: 09:00 - 17:00") |

---

### 3. ความสัมพันธ์ระหว่างตาราง (Entity Relationships)

| ตารางต้นทาง (Source) | ความสัมพันธ์ (Relationship) | ตารางปลายทาง (Target) | คำอธิบาย |
|:---|:---:|:---|:---|
| `doctor` (`doctor_id`) | **1 : N** (One-to-Many) | `appointment` (`doctor_id`) | สัตวแพทย์ 1 ท่าน สามารถมีนัดหมายตรวจ/รักษาได้หลายรายการ (อ้างอิงผ่าน FK `doctor_id` ในตาราง `appointment`) |

---

### 4. ดัชนีและข้อกำหนดความถูกต้อง (Indexes & Constraints)
- **Primary Key:** `PRIMARY KEY (doctor_id)`
- **Unique Constraint:** `CONSTRAINT uk_doctor_email UNIQUE (email)` — ป้องกันการลงทะเบียนอีเมลสัตวแพทย์ซ้ำ
- **Validation Rules (ใน DTO / Entity):**
  - `first_name`: ต้องไม่เป็นค่าว่าง (`@NotBlank`), ความยาวสูงสุด 100 ตัวอักษร
  - `last_name`: ต้องไม่เป็นค่าว่าง (`@NotBlank`), ความยาวสูงสุด 100 ตัวอักษร
  - `phone`: ต้องไม่เป็นค่าว่าง (`@NotBlank`), ความยาวสูงสุด 20 ตัวอักษร (เช่น รูปแบบ `081-111-2233` หรือ `0811112233`)
  - `email`: ต้องไม่เป็นค่าว่าง (`@NotBlank`), รูปแบบอีเมลถูกต้อง (`@Email`), ไม่ซ้ำกับคนอื่น
  - `specialization`: ความยาวสูงสุด 100 ตัวอักษร
  - `work_schedule`: ข้อความระบุวันและช่วงเวลาปฏิบัติงาน

---

### 5. ตัวอย่างข้อมูล (Sample Data - PostgreSQL DDL & DML)

```sql
-- DDL การสร้างตาราง doctor
CREATE TABLE IF NOT EXISTS doctor (
    doctor_id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    work_schedule TEXT
);

-- ตัวอย่างข้อมูลจำลอง (จาก data-doctor.sql)
INSERT INTO doctor (first_name, last_name, specialization, phone, email, work_schedule)
VALUES 
('นันทิดา', 'รักษ์สัตว์', 'อายุรกรรมทั่วไป (General Medicine)', '081-111-2233', 'nantida.r@vetclinic.com', 'จันทร์ - ศุกร์: 09:00 - 17:00'),
('กิตติศักดิ์', 'เจริญกิจ', 'ศัลยกรรมและกระดูก (Surgery & Orthopedics)', '082-222-3344', 'kittisak.k@vetclinic.com', 'อังคาร - เสาร์: 10:00 - 19:00'),
('วรรณภา', 'ใจดี', 'วัคซีนและผิวหนังสัตว์ (Vaccination & Dermatology)', '083-333-4455', 'wannapa.j@vetclinic.com', 'พุธ - อาทิตย์: 09:00 - 18:00')
ON CONFLICT (email) DO NOTHING;
```
