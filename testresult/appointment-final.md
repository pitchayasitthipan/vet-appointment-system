# Appointment — ผลตรวจหลัง review รอบสอง / commit 16

วันที่ 9 ตุลาคม 2026. Branch Nathapat_6733805834_04 จาก develop
เฉพาะ commits งานอ้น ไม่มี merge หรือประวัติ commits ของเพื่อน

## Backend
Maven clean test: 69 tests, failures 0, errors 0, skipped 0
JDK 26.0.2 / target Java 17 / Spring Boot 4.1.1
ครอบคลุม Factory/Service/Schedule/API/JPA/version/Guest และการปฏิเสธการปลอม ownerId ก่อนถึง service
ตรวจ lookup เก็บ myOwnerId, lookup ไม่พบล้างแฟ้มเดิม, guest ไม่มี session ได้ 403
ตรวจ API list/get/create/update/cancel และ pet selection ของคนอื่นได้ 403
ObjectProvider ใช้ configured Clock หรือ Bangkok fallback; MVC slice ของโมดูลอื่นเปิดได้โดยไม่มี Clock

## Frontend
Node --test: 9 tests ผ่านบน actual scripts ใน jsdom
API paths ทั้งหมดของ Appointment/Guest เป็น /api/v1; request ส่ง same-origin session cookies
การจอง/409/stale lookup/แก้ไข/ยกเลิก/filtering/pagination และจุดเชื่อมที่ยังไม่พร้อมผ่าน

## Compatibility กับ Owner
สำเนาทดสอบแยกจาก PR #4 (20baa33) พร้อมโค้ด Appointment รุ่นนี้: 118 tests ผ่าน
รวม Owner API/Web MVC tests ที่ไม่มี Clock bean
ไม่ merge code หรือ commits ของเพื่อนเข้า branch; ไฟล์ compatibility อยู่ tmp ที่ไม่ commit

## HTTP และ PostgreSQL จริง
ฐานข้อมูล appointment_review2 แยก, port แอปทดสอบ 8083
HTTP script: CRUD/validation/version/cancel/idempotence/คืนคิว ผ่าน
คำขอพร้อมกันคิวหมอซ้ำ และสัตว์ซ้ำ: 201 หนึ่งคำขอ, 409 อีกคำขอทั้งสองสถานการณ์
Session owner 2 เข้าถึง/ยกเลิกนัด/เลือกสัตว์ owner 1 ได้ 403
ละ ownerId แล้วเรียกรหัสนัดคนอื่นได้ 404 โดย scope จาก session
Query isStaff=true ไม่ให้สิทธิ์; Swagger /v3/api-docs มี paths /api/v1 และ summaries ทุก Appointment endpoint
Maven package ผ่าน

## งานรอทีม
Navbar กลาง, shared Owner lookup, shared Pet entity และ Staff clinic list/status รอ modules เข้า develop
ตามเงื่อนไข reviewer; รายละเอียด doc/appointment-review2.md
ยังไม่ได้ตรวจ browser ใหม่รอบนี้; DOM tests และ HTTP จริงผ่าน
ไม่ใช่ load test จำนวนมาก; Docker ไม่ได้รัน

## Commit 17 — ผลตรวจ Owner/Staff integration (9 ต.ค. 2026)

- Rebase เฉพาะ 17 commit ของอ้นบน develop 104ec59; ไม่มี merge commit ในชุดงานเรา
- Maven tests รวมกับ Owner/Doctor/MedicalRecord: 155 tests, 0 failures/errors/skipped
- DOM tests: 12 ผ่าน ครอบคลุมการรับแฟ้มจาก session, cache หมดสิทธิ์ และ Staff ส่ง status/version
- HTTP/PostgreSQL appointment_review3: 27 จุดตรวจผ่าน; Owner web search → /me, query owner อื่น 403, Guest เปลี่ยนสถานะ 403, Staff clinic pagination/filter, ยืนยัน version 0→1, รุ่นเก่า 409, ปิดก่อนเวลานัด 409, ปิดหลังถึงเวลา version 1→2, logout แล้ว list/status 403
- ฟอร์ม/รายการ Thymeleaf แสดง navbar กลางจริง; legacy .html routes ตอบ 200 และรักษา query
- Browser จริง: กดนัดหมายใหม่จากหน้า Owner แล้วแสดงแฟ้ม/สัตว์ทันที; ตรวจหน้าจอมือถือไม่มี horizontal overflow
- Package ในสำเนาแยกและเปิดแอปทดสอบได้ เนื่องจาก jar แอปเดิมใน code/target ถูกใช้งานอยู่
- ไม่เปลี่ยน Entity/schema ตาม PDF โดยอัตโนมัติ; Data Dictionary ระบุความต่างจากแบบ 6 Table และฐานข้อมูลจริง
- ยังรอ Pet entity และการเชื่อม FK MedicalRecord; ไม่แก้โค้ดของโมดูลเพื่อน
