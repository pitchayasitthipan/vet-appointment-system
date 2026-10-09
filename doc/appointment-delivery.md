# ส่งงาน Appointment — อ้น / Nathapat 6733805834

PR มีเฉพาะ 15 commits ของอ้นจาก develop ไม่มี merge commit หรือประวัติ commit ของเพื่อน
ประกอบด้วย backend/API/Guest lookup, หน้าจองและรายการ, Factory Method, tests และ sequence diagrams 3 สถานการณ์
ส่วน Pet/Owner ที่ Appointment อ่านเป็น adapter ของเรา; ไม่เพิ่ม CRUD หรือหน้าเว็บของโมดูลทีม

## เปิดใช้งาน
Windows ที่มี JDK 17+, Maven และ PostgreSQL:
```powershell
cd C:\pp\Ppgf
.\scripts\start-local.ps1
```
ใช้ฐานข้อมูล development แยกใน tmp/appointment-pg, loopback พอร์ต 55432
ระบุ -JavaHome/-PostgresBin เมื่อ path ต่างกัน; Ctrl+C หยุดแอป
ฐานข้อมูลใหม่ยังไม่มีแฟ้มเจ้าของ สัตว์ และหมอ ต้องเตรียม test data หรือรอโมดูลทีมเข้า develop
ไม่รวมหน้าลงทะเบียน Owner/Pet หรือ Springdoc จาก branch เพื่อน

- `/appointment-create.html` จองนัดสำหรับแฟ้มที่มีข้อมูลแล้ว
- `/appointments.html` รายการ/กรอง/เลื่อน/ยกเลิก
- `/appointments` และ `/appointments/new` redirect ไปหน้าของเรา

เมื่อไม่พบเจ้าของหรือยังไม่มีสัตว์ จะแจ้งว่ารอเชื่อมโมดูลทีมและไม่พาไปหน้า 404
หลังทีม merge Owner/Pet และรองรับ returnTo=appointment แล้วจึงตั้ง:
```
appointments.owner-registration-enabled=true
appointments.pet-registration-enabled=true
appointments.owner-registration-path=/owners/new
```
งานลงทะเบียนและเพิ่มสัตว์ไม่ได้อยู่ใน PR นี้; หน้าหมอและหน้าสัตว์ของเพื่อนไม่ถูกแก้

## ข้อตกลงข้อมูล
AppointmentPet เป็น @Immutable projection ตาราง pet อ่าน pet_id/name/owner_id โดยไม่ชนชื่อ Pet ของทีม
ใช้ PetOwner และ Doctor จาก develop; Appointment เก็บความสัมพันธ์/FK ไปตารางทั้งสอง
30 นาทีคือช่องนัดพบ/เตรียมการ รวม SURGERY ไม่ใช่ระยะผ่าตัดทั้งหมด
คิวอนาคตใน Asia/Bangkok อยู่ในเวรหมอและเวลาคลินิก; ไม่รองรับเวรข้ามคืน
PENDING/CONFIRMED จองคิว, CANCELLED คืนคิว; PUT ใช้ version ล่าสุด
Guest phone lookup ใช้เลือกแฟ้มตามโจทย์ ไม่มี login/authentication
MedicalRecord/สถานะ CONFIRMED และ COMPLETED รอทีมเชื่อมภายหลัง

## เอกสารและทดสอบ
- doc/appointment-contract.md: API และจุดเชื่อมทีม
- doc/appointment-factory.md: Factory Method
- doc/appointment-sequences.md: สร้าง/เลื่อน/ยกเลิก
- doc/sql/appointment-schema.sql: schema/FK/index อ้างอิง
- testresult/appointment-final.md: ผลตรวจเฉพาะ branch นี้

`mvn -f code/pom.xml clean test`: 61 tests ผ่าน
`pnpm --dir code/frontend-tests test`: 9 DOM tests ผ่าน
HTTP script ใน test/appointment_http_test.py ใช้เฉพาะฐานข้อมูลทดสอบที่เตรียม owner/สัตว์สองตัว/หมอสองคนไว้แล้ว
สร้างเฉพาะ appointments; ไม่มีการสร้างหรือแก้ข้อมูลโมดูลอื่น
