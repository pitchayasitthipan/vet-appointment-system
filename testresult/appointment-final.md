# Appointment — ผลทดสอบฉบับสมบูรณ์

วันที่ 9 ตุลาคม 2026 (Asia/Bangkok). Branch: Nathapat_6733805834_04.
Environment: Maven 3.9.16, JDK 26.0.2, compile target Java 17, Spring Boot 4.1.1, PostgreSQL 18.

## Backend
คำสั่ง `mvn -q -f code/pom.xml clean test` สำเร็จ.
รายงาน Surefire XML รวม **115 tests, failures 0, errors 0, skipped 0**.
ครอบคลุม Appointment Factory/Service/Schedule/REST API/JPA persistence, Guest adapter,
PetOwner API/Web/Repository/Service, PetService, DoctorService และ ClinicConfigService.
H2 ใช้เฉพาะ test scope; ตรวจ startup ของ Spring รวมทุกโมดูลผ่าน.
คำสั่ง `mvn -f code/pom.xml -DskipTests package` สร้าง runnable JAR สำเร็จ.

## Frontend
Node `--test` ใน `code/frontend-tests`: **8 tests ผ่าน**.
- ส่ง ownerId, petId, doctorId, serviceType และเวลาที่เลือกจริง
- แสดงสำเร็จหลัง API บันทึก; กรณี 409 ตรวจคิวใหม่และไม่แสดงสำเร็จ
- เปลี่ยนเบอร์ระหว่างรอ lookup ไม่ใช้คำตอบเก่ามาเลือกแฟ้ม
- URL ลงทะเบียนส่งเบอร์ที่ normalize และ returnTo=appointment
- แสดงชื่อจาก API เป็น text ไม่รัน HTML ที่แทรกในชื่อ
- PUT ใช้ version ล่าสุดและ ownerId
- PATCH ยกเลิกหลังยืนยันเท่านั้น
- ไม่มีสัตว์ในแฟ้มไม่เปิดให้จอง; กรองสถานะรีเซ็ต page

## HTTP + PostgreSQL จริง
ใช้ฐานข้อมูล disposable `appointment_dev` ที่ 127.0.0.1:55432 ภายใน `tmp/appointment-pg`.
ไม่ได้เชื่อม/แก้ฐานข้อมูล PostgreSQL service เดิมของเครื่อง.
`python test/appointment_http_test.py http://localhost:8080`: PASS.
- หน้า Home/จอง/รายการ และไฟล์ JS/CSS ตอบ 200
- Unknown phone 404 → config `/owners/new` → แบบฟอร์มจริง → บันทึกเจ้าของ → redirect `/pets/new`
- เพิ่มสัตว์ของเจ้าของและค้นหาเบอร์ที่มีขีดได้
- POST 201; คิวซ้ำ 409; สัตว์ผิดเจ้าของ 404; เวลาไม่ตรงช่อง/อดีต 400
- GET รายการมี pagination/filter และรายละเอียดไม่คืนแฟ้มเจ้าของอื่น
- PUT เพิ่ม version; version เก่า 409
- PATCH ยกเลิกซ้ำ idempotent และคิวกลับมาว่าง
- สอง requests จอง **หมอเดียวกัน/เวลาเดียวกัน**: 201 หนึ่ง, 409 หนึ่ง
- สอง requests จอง **สัตว์ตัวเดียว/เวลาเดียวกันแต่คนละหมอ**: 201 หนึ่ง, 409 หนึ่ง
- ตรวจ schema/index script บน PostgreSQL จริงผ่าน

## Browser จริง
เปิด `http://localhost:8080/appointment-create.html` ใน in-app browser.
ใช้ข้อมูล synthetic จาก HTTP tests; ค้นแฟ้ม → เลือก HTTP Cat/ฉีดวัคซีน/12 ต.ค. 2026 เวลา 11:00 → บันทึกนัด #4.
หน้าแสดงหมายเลขจริงและสถานะ PENDING; หน้ารายการแสดงนัดนั้น.
แก้เวลาเป็น 13:00 → ข้อความบันทึกสำเร็จ → ยืนยันยกเลิก → สถานะ CANCELLED ผ่านทั้งหมด.
ตรวจมือถือและเดสก์ท็อป 1280px: ช่องฟอร์ม/การ์ดไม่ซ้อนกัน; desktop content width = viewport width ไม่ล้นแนวนอน.
คืน viewport เดิมหลังตรวจและเปิดหน้าจองไว้ให้ผู้ใช้.

## ขอบเขตผลตรวจ
Concurrency ตรวจสองคำขอในสองสถานการณ์ข้างต้น ไม่ใช่ load test จำนวนมาก.
ไม่ได้ทดสอบ Docker เพราะ daemon ไม่เปิด; ใช้ PostgreSQL/Java จริงในเครื่องแทน.
ผลทั้งหมดเป็นงาน Appointment และจุดเชื่อมกับโมดูลทีม; ไม่ครอบคลุม MedicalRecord ที่อยู่นอกหน้าที่อ้น.
