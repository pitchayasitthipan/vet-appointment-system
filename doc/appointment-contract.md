# Appointment module — อ้น (Nathapat 6733805834)

Base: develop. Branch: Nathapat_6733805834_04. Java 17+, Spring Boot 4.1.1.

## ขอบเขตของ PR
เฉพาะ Appointment Entity/Repository/DTO/Factory/Service/Controller, Guest lookup, หน้าจองและรายการ,
tests และเอกสาร ไม่รวม merge commit, Pet CRUD, PetOwner CRUD หรือหน้าเว็บของเพื่อน
ไม่แก้ Doctor, PetOwner, GlobalExceptionHandler หรือไฟล์ JavaScript ของโมดูลอื่น

## ข้อมูลและกฎ
- appointment: appointment_id, pet_id, doctor_id, appointment_date_time, service_type, status, symptoms, preparation_instructions, version
- บริการ CONSULTATION/VACCINE/SURGERY; สถานะ PENDING/CONFIRMED/COMPLETED/CANCELLED
- วันเวลา ISO ใน Asia/Bangkok; ช่อง 30 นาทีตรงนาที 00/30 อยู่ในเวลาคลินิกและเวรหมอ
- PENDING/CONFIRMED จองคิว; CANCELLED คืนคิว; COMPLETED แก้ไขไม่ได้
- ตรวจเจ้าของสัตว์ คิวหมอและสัตว์ซ้ำ; ล็อก Doctor แล้ว AppointmentPet ก่อนตรวจและบันทึก
- PUT รับ version ล่าสุดและป้องกันการแก้ไขข้อมูลเก่า

## จุดเชื่อมทีม
อ่าน PetOwner/Doctor ที่มีอยู่ใน develop โดยไม่แก้ entity หรือ repository ของเพื่อน
`AppointmentPet` เป็น projection @Immutable เฉพาะ Appointment ของตาราง pet (pet_id, name, owner_id)
ชื่อ Java ไม่ชนกับ Pet ของทีม และไม่มี Pet CRUD; `AppointmentPetRepository` ใช้ค้นสัตว์/ล็อกแถวเท่านั้นใน production
การเพิ่มเจ้าของหรือสัตว์ต้องรอ PR ของผู้รับผิดชอบเข้า develop แยกกันก่อน
CONFIRMED/COMPLETED สงวนให้ทีมเชื่อม flow หมอ/MedicalRecord ภายหลัง
Guest lookup เป็นการเลือกแฟ้มตามโจทย์ ไม่มี User/Account/login หรือการยืนยันตัวตน

## API
- POST/GET `/api/appointments`
- GET/PUT `/api/appointments/{id}?ownerId=...`
- PATCH `/api/appointments/{id}/cancel?ownerId=...`
- GET `/api/appointments/availability?doctorId=...&date=...`
- POST `/api/appointment-guests/lookup` รับ `{phone}` คืน ownerId/ชื่อ; normalize ช่องว่าง/ขีด, ไม่พบ 404, หลายแฟ้ม 409
- GET `/api/appointment-guests/{ownerId}/pets` คืน petId/petName
- GET `/api/appointment-guests/config` คืนเส้นทางลงทะเบียนและ flags พร้อมใช้งาน

GET รายการ: ownerId, status, page >= 0, size 1–100, sort=appointmentId|appointmentDateTime|status|serviceType, direction=asc|desc
POST คืน 201 พร้อม Location/version=0; ยกเลิกผ่าน PATCH ไม่ลบแถว
Availability คืน array ISO ของคิวหมอ; create/update ตรวจคิวสัตว์ซ้ำเพิ่มเติม

## ลงทะเบียนที่รอเชื่อมทีม
ค่าเริ่มต้น `appointments.owner-registration-enabled=false` และ `appointments.pet-registration-enabled=false`
เมื่อไม่มีแฟ้มหรือสัตว์ หน้าเว็บแจ้งว่ารอเชื่อมโมดูลทีมและไม่ส่งไปหน้าที่ไม่มีอยู่
หลังทีม merge Owner/Pet เข้า develop และรองรับ returnTo แล้วจึงเปิด flags เป็น true
`appointments.owner-registration-path` ค่าเริ่มต้น `/owners/new`; ส่ง phone ที่ normalize และ returnTo=appointment
ลิงก์เพิ่มสัตว์ใช้ `/pets/new?ownerId=...&returnTo=appointment` เฉพาะเมื่อเปิด flag
PR นี้ไม่มี implementation ของหน้าลงทะเบียนทั้งสอง

## 15 commits ของอ้น
1 scope/API 2 domain 3 queries/locks 4 DTO 5 Factory Method 6 schedule rules
7 create/read 8 update/cancel 9 REST API 10 guest lookup 11 booking layout
12 booking API connection 13 list/edit/cancel UI 14 tests 15 isolated adapters/diagrams/delivery
