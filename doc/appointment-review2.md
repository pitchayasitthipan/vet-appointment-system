# Review รอบสอง — Appointment commit 16

บันทึกย้อนหลัง ณ commit 16: PR #4 ยัง open และ develop ยัง b8f6db8

สถานะล่าสุดรอบปรับ PR #9: develop 8b52f96 รวม Pet และ navbar ใหม่แล้ว; ใช้ Pet entity กลางและลบ AppointmentPet projection, เพิ่ม MedicalRecord contract และตรวจ handler กลาง ดู testresult/appointment-pet-integration.md ส่วนตารางด้านล่างเป็นบันทึก review รอบก่อน
ไม่รวมประวัติหรือ implementation ของเพื่อนเข้า branch อ้น

| ข้อ | ผลรอบนี้ |
|---|---|
| 1 Clock | แก้ ObjectProvider + Bangkok fallback, คง constructor Clock; tests ตรวจ slice โมดูลอื่นและ configured clock |
| 2 Session | แก้ lookup เก็บ myOwnerId; ทุก API ของแฟ้มตรวจ session; เปลี่ยน ownerId ได้ 403; รองรับ isStaff=true เฉพาะ server session ตาม contract |
| 3 API version | ย้าย Appointment/Guest เป็น /api/v1 พร้อม JS, tests, HTTP script และเอกสาร |
| 4 Packages | ย้าย REST ไป controller/api และ view ไป controller/web |
| 5 Navbar | รอ PR #4 และ fragment กลางเข้า develop ตามข้อกำหนด reviewer; หน้า Appointment ยังเป็น static |
| 6 CSS/icon | รอปรับกับธีม/Phosphor กลางเมื่อรวมงานตามคำแนะนำ |
| 7 Swagger | เพิ่ม @Tag และ @Operation ทุก endpoint พร้อม Springdoc |
| 8 Owner lookup ร่วม | รอ PetOwnerService เข้า develop; คง normalized-phone adapter ของเรา ไม่คัดลอก service เพื่อน |
| 9 Pet entity ร่วม | รอ Pet module เข้า develop; คง AppointmentPet projection ของเรา ไม่คัดลอก entity เพื่อน |
| 10 Staff clinic list/status | รอ StaffAccess/Staff flow เข้า develop แล้วเพิ่มตามข้อกำหนด; รอบนี้รองรับ session Staff เลือกแฟ้ม แต่ยังไม่มี endpoint ดูทั้งคลินิก/CONFIRMED/COMPLETED |

StaffAccess ยังไม่มีใน develop จึงใช้ AppointmentAccess ที่อ่าน session keys เดียวกันเป็นจุดเชื่อมชั่วคราว
ไม่ได้เพิ่มทางลัดให้ client ตั้ง isStaff หรือ myOwnerId ผ่าน query/body
เมื่อ PR ทีมเข้า develop แล้วจึงดึง develop และเปลี่ยนจุดเชื่อมเหล่านี้ เฉพาะไฟล์งาน Appointment

## ผลทดสอบ
- Branch อ้น: 69 Java tests, 9 DOM tests ผ่าน
- Compatibility สำเนาแยกกับ PR #4 (20baa33): 118 tests ผ่าน รวม Owner MVC tests ที่ไม่มี Clock bean
- HTTP + PostgreSQL ฐานข้อมูลแยก appointment_review2: CRUD/version/cancel/คิวซ้ำ ผ่าน
- Session ของ owner 2 เรียกนัด/สัตว์ owner 1 ได้ 403; ถ้าละ ownerId แล้วใช้รหัสนัดคนอื่นได้ 404
- Query isStaff=true ไม่ให้สิทธิ์; Swagger paths/summaries ตรวจผ่าน

Compatibility สำเนาทดสอบไม่ใช่ merge commit และไม่อยู่ใน PR/branch ของอ้น
