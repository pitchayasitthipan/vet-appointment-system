# Appointment module — อ้น (Nathapat 6733805834)

Base: develop 8b52f96 (รวม Owner/Staff, navbar ล่าสุด และ Pet จาก PR #9 แล้ว). Branch: Nathapat_6733805834_04. Java 17+, Spring Boot 4.1.1.

## ขอบเขตของ PR
เฉพาะ Appointment Entity/Repository/DTO/Factory/Service/Controller, Guest lookup, หน้าจองและรายการ,
tests และเอกสาร ไม่รวม merge commit, Pet CRUD, PetOwner CRUD หรือหน้าเว็บของเพื่อน
ไม่แก้ Doctor, PetOwner, GlobalExceptionHandler หรือไฟล์ JavaScript ของโมดูลอื่น

## ข้อมูลและกฎ
- appointment: appointment_id, pet_id, doctor_id, appointment_date_time, service_type, status, symptoms, preparation_instructions, version
- หน้า Appointment แสดงและเลือกปี พ.ศ. ทั้งหน้าจอง/เลื่อนนัด/รายการ/ข้อความยืนยัน; ช่องวันและเดือนเป็นภาษาไทยและปีระบุ พ.ศ. ชัดเจน
- ข้อมูลส่ง API และเก็บฐานข้อมูลยังเป็น ISO ค.ศ. โดยแปลง พ.ศ. ลบ 543; การคำนวณคิว/วันอธิกสุรทิน/min date ใช้ปฏิทิน Gregorian และเวลา Bangkok
- บริการ CONSULTATION/VACCINE/SURGERY; สถานะ PENDING/CONFIRMED/COMPLETED/CANCELLED
- วันเวลา ISO ใน Asia/Bangkok; ช่อง 30 นาทีตรงนาที 00/30 อยู่ในเวลาคลินิกและเวรหมอ
- PENDING/CONFIRMED จองคิว; CANCELLED คืนคิว; COMPLETED แก้ไขไม่ได้
- ตรวจเจ้าของสัตว์ คิวหมอและสัตว์ซ้ำ; ล็อก Doctor แล้ว Pet ก่อนตรวจและบันทึก
- PUT รับ version ล่าสุดและป้องกันการแก้ไขข้อมูลเก่า

## จุดเชื่อมทีม
อ่าน PetOwner/Doctor ที่มีอยู่ใน develop โดยไม่แก้ entity หรือ repository ของเพื่อน
ใช้ `Pet` entity ของทีมโดยตรง; ลบ AppointmentPet projection ที่ map ตารางซ้ำแล้ว
`AppointmentPetRepository` เป็น Repository<Pet, Long> เฉพาะอ่าน/ล็อก ไม่มี save/delete และไม่แก้ PetRepository ของทีม
Pet ที่เพิ่มผ่านโมดูล Pet ปรากฏในรายการเลือกสัตว์ของ Appointment ตามเจ้าของ; tests ใช้ species ที่บังคับตาม schema
Staff เปลี่ยน PENDING → CONFIRMED ก่อนถึงเวลานัด และ CONFIRMED → COMPLETED เมื่อถึงเวลานัดแล้ว
ฝั่ง MedicalRecord เรียก requireCompletedForMedicalRecord ภายใน write transaction เพื่อตรวจนัดจริงและ COMPLETED; ดู [contract](appointment-medical-record-contract.md) งานเรียกใช้และ FK ยังเป็นของเจ้าของ MedicalRecord
Guest lookup เป็นการเลือกแฟ้มตามโจทย์ ไม่มี User/Account/login หรือการยืนยันตัวตน

## API
- POST/GET `/api/v1/appointments`
- GET `/api/v1/appointments/staff`: รายการทั้งคลินิก เฉพาะ Staff
- PATCH `/api/v1/appointments/{id}/status`: `{status, version}` เฉพาะ Staff
- GET `/api/v1/appointment-guests/me?ownerId=...`: แฟ้มและสิทธิ์จาก session; ownerId เป็นตัวเลือกและตรวจสิทธิ์ก่อนอ่าน
- GET/PUT `/api/v1/appointments/{id}?ownerId=...`
- PATCH `/api/v1/appointments/{id}/cancel?ownerId=...`
- GET `/api/v1/appointments/availability?doctorId=...&date=...`
- POST `/api/v1/appointment-guests/lookup` รับ `{phone}` คืน ownerId/ชื่อ; normalize ช่องว่าง/ขีด แล้วเรียก PetOwnerService กลาง; ไม่พบ 404
- GET `/api/v1/appointment-guests/{ownerId}/pets` คืน petId/petName
- GET `/api/v1/appointment-guests/config` คืนเส้นทางลงทะเบียนและ flags พร้อมใช้งาน

GET รายการ: ownerId, status, page >= 0, size 1–100, sort=appointmentId|appointmentDateTime|status|serviceType, direction=asc|desc
POST คืน 201 พร้อม Location/version=0; ยกเลิกผ่าน PATCH ไม่ลบแถว
Availability คืน array ISO ของคิวหมอ; create/update ตรวจคิวสัตว์ซ้ำเพิ่มเติม

## ลงทะเบียนที่รอเชื่อมทีม
ค่าเริ่มต้น `appointments.owner-registration-enabled=true` และ `appointments.pet-registration-enabled=false`
เมื่อไม่มีแฟ้ม หน้าเว็บพาไปลงทะเบียน Owner กลาง; หากไม่มีสัตว์ยังแจ้งว่ารอโมดูล Pet และไม่ส่งไปหน้าที่ไม่มีอยู่
Pet เข้า develop แล้ว แต่ยังไม่เปิด pet-registration-enabled จนหน้าเพิ่มสัตว์รองรับ session เจ้าของและ /pets/new; PR #9 ปัจจุบันยังเลือก ownerId=1 และไม่มีเส้นทางนี้
`appointments.owner-registration-path` ค่าเริ่มต้น `/owners/new`; ส่ง phone ที่ normalize และ returnTo=appointment
ลิงก์เพิ่มสัตว์ใช้ `/pets/new?ownerId=...&returnTo=appointment` เฉพาะเมื่อเปิด flag
PR นี้ไม่มี implementation ของหน้าลงทะเบียนทั้งสอง

## 18 commits ของอ้น
1 scope/API 2 domain 3 queries/locks 4 DTO 5 Factory Method 6 schedule rules
7 create/read 8 update/cancel 9 REST API 10 guest lookup 11 booking layout
12 booking API connection 13 list/edit/cancel UI 14 tests 15 isolated adapters/diagrams/delivery
16 session/API/team review fixes; 17 Owner/Staff integration, shared templates, service interface และ [Appointment Data Dictionary](appointment-data-dictionary.md)
18 ใช้ Pet entity กลางจาก PR #9, เตรียม MedicalRecord contract สำหรับ PR #12, Staff passcode environment และ tests จุดเชื่อม

## Session และ review รอบสอง (commit 16)
POST lookup สำเร็จเก็บ Long ownerId ใน session key myOwnerId; lookup ไม่พบล้างค่าที่เลือกก่อนหน้า
GET/list/PUT/PATCH และ POST นัด ใช้ session เป็นสิทธิ์ ไม่เชื่อ ownerId ใน query/body
Guest ที่ยังไม่ค้นเบอร์หรือส่ง ownerId ต่างจาก session ได้ 403 ก่อนถึง service
GET/list/PUT/PATCH ไม่ระบุ ownerId ได้ โดยใช้แฟ้มจาก session; Staff ต้องระบุ ownerId ที่ต้องการ
session isStaff=true อนุญาตเลือกแฟ้มอื่นตาม contract ของทีม; query/body isStaff ไม่ให้สิทธิ์
AppointmentAccess เป็น helper ตรวจแฟ้มของเรา และใช้ StaffAccess.isStaff(session) กลางแล้ว
AppointmentExceptionHandler ใช้ ObjectProvider<Clock> พร้อม Bangkok fallback และคง constructor Clock สำหรับ tests
REST controllers อยู่ controller/api; view controller อยู่ controller/web
Swagger /swagger-ui.html ใช้ @Tag/@Operation ทุก Appointment/Guest endpoint
เส้นทางเดิม /api/appointments และ /api/appointment-guests ถูกย้าย ต้องปรับ callers เป็น /api/v1/...
