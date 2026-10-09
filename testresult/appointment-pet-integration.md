# Appointment integration with Pet and latest team reviews

ตรวจ 2026-10-10 บนฐาน develop 8b52f96 (PR #9 + PR #11)

## เปลี่ยนเฉพาะ Appointment

- ใช้ Pet entity กลางใน Appointment relation, factory, response DTO, guest lookup และ booking lock; ลบ AppointmentPet entity ซ้ำ
- AppointmentPetRepository ใช้ Repository<Pet, Long> สำหรับอ่าน/ล็อก ไม่มี save/delete หรือ index ซ้ำกับ Pet
- Persistence fixtures มี species ตาม schema จริง และทดสอบการอ่านชื่อ Pet ที่แก้จากโมดูลอื่น
- เพิ่ม requireCompletedForMedicalRecord ตรวจนัดจริง/COMPLETED และล็อกนัดใน write transaction เดียวกับผู้เรียก (MANDATORY)
- ให้ ForbiddenException ของ StaffAccess กลางตอบผ่าน GlobalExceptionHandler; advice ของเรายังจำกัดเฉพาะ Appointment controllers
- หน้า Appointment ทั้งสองใช้ pawcare.css, icons.css และ fragments/layout จาก develop ล่าสุด เทียบแล้วไม่มี diff กับไฟล์กลาง
- navbar ใหม่มี Medical Records เฉพาะ Staff; tests ตรวจทั้ง Staff และ Guest

## ผลตรวจ

- Java clean test: 165 tests, 0 failures/errors/skips รวม Pet, Owner, MedicalRecord, Doctor และ Appointment
- Appointment frontend: 12 tests ผ่าน
- HTTP/PostgreSQL: 33 จุดตรวจผ่าน ที่พอร์ต 8086 ฐานข้อมูล appointment_pet_integration แยก
- สร้าง Pet ผ่าน API ของ PR #9 → ค้นแฟ้ม Owner → รายการสัตว์ Appointment → จองนัด 201
- Pet ของเจ้าของอื่นถูกปฏิเสธ; แก้ชื่อ Pet แล้ว Appointment อ่านชื่อใหม่และเจ้าของเดิม
- Guest/Staff/session, version conflict, ยืนยันและปิดนัด, logout ผ่าน
- ทดสอบ Java contract MedicalRecord: รหัสผิด, ไม่พบ, สถานะไม่ผ่าน, COMPLETED และการเรียกใน transaction จริง
- diff check ผ่าน; ไม่มีการแก้ Pet/Owner/Doctor/MedicalRecord implementation หรือ GlobalExceptionHandler ของเพื่อน

## ยังต้องเชื่อมโดยเจ้าของโมดูล

- MedicalRecord ต้องเรียก guard ของ Appointment จาก create/update ภายใน transaction, ตรวจ Staff และเพิ่ม FK พร้อม migration; API ปัจจุบันยังไม่บังคับ contract นี้อัตโนมัติ
- หน้าเพิ่ม Pet ยังไม่มี /pets/new และยังเลือก ownerId=1 จึงคงปิด pet-registration-enabled
- ไม่ push, merge หรือโพสต์รีวิวระหว่างการแก้รอบนี้; rebase เฉพาะ branch Nathapat บน develop ล่าสุด (17 commits, ไม่มี merge commit เพิ่ม)

## Commit 18: เตรียมรวม PR #12

เทียบ PR #12 head 8f80d28 โดยคัดเฉพาะโค้ด PR เข้าสำเนาทดสอบ tmp/appointment-pr12 ไม่ใส่โค้ดของเนยลง branch เรา

- ตัวเปิดแอปรองรับ STAFF_PASSCODE 8 หลักจาก environment หรือถามแบบซ่อนรหัส; ตั้งชื่อ environment ของ clinic.staff-passcode ให้ใช้ได้กับ develop ปัจจุบันและ PR #12 โดยไม่ใส่รหัสใน Java command line
- application.properties ของ tests มีเฉพาะรหัสจำลอง ไม่เพิ่ม production default
- AppointmentMedicalRecordIntegrationTest ใช้ service/repository จริง ตรวจ COMPLETED → create/update record, ปฏิเสธ CONFIRMED ก่อนบันทึก, และปลดล็อก Staff ผ่าน Owner แล้วเรียก Appointment/MedicalRecord ด้วย session เดียวกัน รวมถึง logout
- ชุดรวม PR #12: 185 tests ผ่าน ไม่มี failures/errors/skips
- ชุด integration ใหม่ 3 tests ผ่านกับ develop ปัจจุบันด้วย
- PowerShell launcher syntax ผ่าน และ diff check ผ่าน

Contract validation พร้อมให้เนยนำไปเรียกแล้ว แต่ service ของ PR #12 ยังไม่ได้เรียก guard และยังไม่มี FK; ไม่กล่าวอ้างว่า API MedicalRecord ปัจจุบันบังคับ COMPLETED แล้ว
Commit 18 รวมเฉพาะงาน Appointment/tests/launcher/เอกสารของเรา ไม่มีการ push หรืออัปเดต PR ระหว่าง commit
