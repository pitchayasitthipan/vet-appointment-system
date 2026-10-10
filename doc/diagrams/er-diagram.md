# ER Diagram

ฐานข้อมูล PostgreSQL 6 ตาราง (Hibernate สร้างจาก JPA Entity ด้วย `ddl-auto=update`)

```mermaid
erDiagram
    PET_OWNER ||--|| PET_OWNER_DETAIL : "มีรายละเอียด"
    PET_OWNER ||--o{ PET : "เป็นเจ้าของ"
    PET ||--o{ APPOINTMENT : "ถูกนัด"
    DOCTOR ||--o{ APPOINTMENT : "รับนัด"
    APPOINTMENT ||--o{ MEDICAL_RECORD : "มีประวัติการรักษา"

    PET_OWNER {
        BIGINT owner_id PK
        VARCHAR first_name "50"
        VARCHAR last_name "50"
        VARCHAR email UK "100"
        VARCHAR phone UK "20"
        TIMESTAMP created_at
    }
    PET_OWNER_DETAIL {
        BIGINT owner_detail_id PK
        TEXT address
        VARCHAR emergency_contact_name "50"
        VARCHAR emergency_contact_phone "20"
        BIGINT owner_id FK,UK
    }
    PET {
        BIGINT pet_id PK
        VARCHAR name "100"
        VARCHAR species "50 Dog Cat Other"
        VARCHAR breed "100"
        VARCHAR gender "20"
        DATE birth_date
        DOUBLE weight
        VARCHAR microchip_number UK "100"
        BIGINT owner_id FK
    }
    DOCTOR {
        BIGINT doctor_id PK
        VARCHAR first_name "100"
        VARCHAR last_name "100"
        VARCHAR specialization "100"
        VARCHAR phone "20"
        VARCHAR email UK "255"
        TEXT work_schedule
    }
    APPOINTMENT {
        BIGINT appointment_id PK
        BIGINT pet_id FK
        BIGINT doctor_id FK
        TIMESTAMP appointment_date_time
        VARCHAR service_type "CONSULTATION VACCINE SURGERY"
        VARCHAR status "PENDING CONFIRMED COMPLETED CANCELLED"
        VARCHAR symptoms "1000"
        VARCHAR preparation_instructions "500"
        BIGINT version "Optimistic Lock"
    }
    MEDICAL_RECORD {
        BIGINT medical_record_id PK
        BIGINT appointment_id FK
        TEXT diagnosis
        TEXT treatment
        VARCHAR vaccine_name "150"
        DATE vaccine_date
        DATE next_vaccine_date
        TEXT notes
        TIMESTAMP created_at
    }
```

| ความสัมพันธ์ | ประเภท | Foreign Key | กฎ |
| --- | --- | --- | --- |
| `pet_owner` — `pet_owner_detail` | 1 : 1 | `pet_owner_detail.owner_id` (Unique) | บันทึกและลบพร้อมกัน |
| `pet_owner` — `pet` | 1 : N | `pet.owner_id` | ลบเจ้าของที่ยังมีสัตว์ไม่ได้ |
| `pet` — `appointment` | 1 : N | `appointment.pet_id` | ยกเลิกนัดด้วยการเปลี่ยนสถานะ ไม่ลบแถว |
| `doctor` — `appointment` | 1 : N | `appointment.doctor_id` | ลบสัตวแพทย์ที่ยังมีนัดไม่ได้ |
| `appointment` — `medical_record` | 1 : N | `medical_record.appointment_id` (`fk_medical_record_appointment`) | บันทึกได้เฉพาะนัดที่ `COMPLETED` |
