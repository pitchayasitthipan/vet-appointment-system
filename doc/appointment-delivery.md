# ส่งงาน Appointment — อ้น / Nathapat 6733805834

ชุดส่งงานมีเฉพาะ 18 commits ของอ้นเทียบ develop ไม่มี merge commit เพิ่มในชุดงานของเรา
ประกอบด้วย backend/API/Guest lookup, หน้าจองและรายการ, Factory Method, tests และ sequence diagrams 3 สถานการณ์
ใช้ PetOwnerService, StaffAccess และ Pet entity กลางจาก develop 8b52f96; ไม่เพิ่ม CRUD หรือแก้หน้าเว็บโมดูลทีม

## เปิดใช้งาน
สถานะ integration ล่าสุด: ใช้ Pet entity กลางแล้ว, CSS/icons/navbar ตรง develop 8b52f96, และเตรียม [MedicalRecord contract](appointment-medical-record-contract.md)
หน้าเพิ่ม Pet ยังไม่เปิดลิงก์จนรองรับ session ของเจ้าของและ /pets/new; ดู [ผลตรวจล่าสุด](../testresult/appointment-pet-integration.md)

Windows ที่มี JDK 17+, Maven และ PostgreSQL:
ตั้ง STAFF_PASSCODE เป็นตัวเลข 8 หลักก่อนรันเพื่อรองรับ PR #12; หากยังไม่ตั้ง script จะถามแบบซ่อนรหัส ไม่พิมพ์รหัสลง log หรือใส่ใน command line ของ Java
```powershell
cd C:\pp\Ppgf
.\scripts\start-local.ps1
```
ใช้ฐานข้อมูล development แยกใน tmp/appointment-pg, loopback พอร์ต 55432
ระบุ -JavaHome/-PostgresBin เมื่อ path ต่างกัน; Ctrl+C หยุดแอป
ฐานข้อมูลใหม่ยังไม่มีแฟ้มเจ้าของ สัตว์ และหมอ ต้องเตรียม test data หรือรอโมดูลทีมเข้า develop
ใช้หน้าลงทะเบียน Owner และ dependency กลางที่เข้า develop แล้ว; หน้าเพิ่ม Pet ยังรอโมดูลทีม

- `/appointment-create.html` จองนัดสำหรับแฟ้มที่มีข้อมูลแล้ว
- `/appointments.html` รายการ/กรอง/เลื่อน/ยกเลิก
- `/appointments` และ `/appointments/new` แสดง templates/appointment ผ่าน navbar กลาง
- URL .html เดิมแสดง template เดียวกันและคง query/session โดยไม่ redirect
- เมื่อ Staff เปิด `/appointments` โดยไม่เลือกเจ้าของ จะเห็นนัดทั้งคลินิกและปุ่มยืนยัน/ปิดนัด

เมื่อไม่พบเจ้าของหรือยังไม่มีสัตว์ จะแจ้งว่ารอเชื่อมโมดูลทีมและไม่พาไปหน้า 404
การลงทะเบียน Owner เปิดใช้ได้แล้ว ส่วนหน้าเพิ่ม Pet ยังปิดไว้จนโมดูลทีมพร้อม:
```
appointments.owner-registration-enabled=true
appointments.pet-registration-enabled=false
appointments.owner-registration-path=/owners/new
```
งานลงทะเบียนและเพิ่มสัตว์ไม่ได้อยู่ใน PR นี้; หน้าหมอและหน้าสัตว์ของเพื่อนไม่ถูกแก้

## ข้อตกลงข้อมูล
Appointment อ้าง Pet entity ของทีมโดยตรง; AppointmentPetRepository อ่าน/ล็อกแถว Pet เท่านั้น ไม่มี entity ตาราง pet ซ้ำ
ใช้ PetOwner และ Doctor จาก develop; Appointment เก็บความสัมพันธ์/FK ไปตารางทั้งสอง
30 นาทีคือช่องนัดพบ/เตรียมการ รวม SURGERY ไม่ใช่ระยะผ่าตัดทั้งหมด
คิวอนาคตใน Asia/Bangkok อยู่ในเวรหมอและเวลาคลินิก; ไม่รองรับเวรข้ามคืน
PENDING/CONFIRMED จองคิว, CANCELLED คืนคิว; PUT ใช้ version ล่าสุด
Guest phone lookup ใช้เลือกแฟ้มตามโจทย์ ไม่มี login/authentication
Staff ยืนยันและปิดนัดได้แล้ว; FK และกฎฝั่ง MedicalRecord รอเจ้าของโมดูลเชื่อม

## เอกสารและทดสอบ
- doc/appointment-contract.md: API และจุดเชื่อมทีม
- doc/appointment-factory.md: Factory Method
- doc/appointment-sequences.md: สร้าง/เลื่อน/ยกเลิก
- doc/sql/appointment-schema.sql: schema/FK/index อ้างอิง
- doc/appointment-data-dictionary.md: ความหมายคอลัมน์ ชนิดข้อมูล PK/FK ค่าเริ่มต้น และกฎข้อมูล
- testresult/appointment-final.md: ผลตรวจเฉพาะ branch นี้

`mvn -f code/pom.xml test`: tests รวมกับ develop 155 tests ผ่าน
`pnpm --dir code/frontend-tests test`: 12 DOM tests ผ่าน
HTTP script ใน test/appointment_http_test.py ใช้เฉพาะฐานข้อมูลทดสอบที่เตรียม owner/สัตว์สองตัว/หมอสองคนไว้แล้ว
สร้างเฉพาะ appointments; ไม่มีการสร้างหรือแก้ข้อมูลโมดูลอื่น

## Commit 16 — ปรับตาม review รอบสอง
แก้ Clock fallback, session owner scoping, API /api/v1, package api/web, Swagger และ tests
69 Java tests และ 9 DOM tests ผ่าน; ตรวจร่วม Owner ในสำเนาแยก 118 tests ผ่านโดยไม่ merge เข้า branch
HTTP/PostgreSQL บนฐานข้อมูล appointment_review2 แยก: CRUD/validation/version/cancel/concurrency ผ่าน
session เจ้าของคนที่สองอ่าน/ยกเลิก/เลือกสัตว์ของคนแรกไม่ได้; isStaff=true ใน URL ให้สิทธิ์ไม่ได้
รายการรอ PR ทีมเข้า develop อยู่ใน doc/appointment-review2.md

## Commit 17 — เชื่อม Owner/Staff และ Data Dictionary
ใช้ StaffAccess และ PetOwnerService กลาง; เพิ่ม /api/v1/appointment-guests/me และ Staff clinic/status endpoints
แยก AppointmentService เป็น interface + impl; ย้ายหน้าเข้า templates/appointment และใช้ navbar/ฟอนต์/ไอคอนกลาง
รองรับปุ่มจาก Owner ผ่าน session; ข้อมูล cache ไม่ให้สิทธิ์; query ownerId ต้องตรง session ยกเว้น Staff
รวม dependency Swagger/Thymeleaf ที่ซ้ำให้เหลือหนึ่งรายการ ใช้ Swagger 3.1.1 จาก develop
Data Dictionary ครบ 9 คอลัมน์ อ้างอิง Entity/ฐานข้อมูลจริง และระบุความต่างจาก 6 Table.pdf
Java 155 tests, DOM 12 tests, HTTP/PostgreSQL 27 จุดตรวจ และ browser Owner → Appointment ผ่าน
ฐานข้อมูลทดสอบ appointment_review3 แยกจากข้อมูลใช้งาน; ไม่แก้โค้ดโมดูล Owner/Pet/Doctor/MedicalRecord
ชุด commit เทียบ develop มี 17 commit ของอ้น ไม่มี merge commit; Pet entity ยังรอ PR ทีม
