# Appointment module — อ้น / Nathapat 6733805834

งานของอ้นเสร็จ: Appointment Entity/Repository/DTO/Service/Controller, API สร้าง/ค้นหา/แก้ไข/ยกเลิก,
Guest Flow, หน้าจองและหน้ารายการ, Factory Method, tests และ Sequence Diagrams 3 scenarios.
รวมโมดูล Pet ของโอ่งจาก cbb47b3 และ PetOwner ของเชอจาก 20baa33 บน branch อ้น
เพื่อให้ registration → pet → appointment ทำงานครบ; ไม่แก้ remote branch ของเพื่อน.
เพิ่ม 2 integration commits จากแผน 15 เดิม จึงมี 17 commits ในเส้นงานหลักของอ้น.

## เปิดใช้งาน
**ในเครื่อง Windows ที่ติดตั้ง Java/Maven และ PostgreSQL:**
```powershell
cd C:\pp\Ppgf
.\scripts\start-local.ps1
```
สคริปต์ใช้ฐานข้อมูล development แยกใน `tmp/appointment-pg`, รับเฉพาะ loopback พอร์ต 55432.
ไม่แตะ PostgreSQL service/ฐานข้อมูลเดิม. ถ้า path ต่างกันให้ระบุ `-JavaHome` / `-PostgresBin`.
กด Ctrl+C หยุดแอป; PostgreSQL dev หยุดได้ด้วย `pg_ctl -D tmp/appointment-pg stop`.

**Docker (ตามการตั้งค่าทีม):** `docker compose up --build` ใช้ฐานข้อมูลที่กำหนดใน compose.
ยังไม่ได้รัน Docker ในรอบนี้เพราะ Docker daemon ไม่เปิด แต่ตรวจ app กับ PostgreSQL 18 จริงแล้ว.

เส้นทาง:
- `/appointment-create.html` จองนัด
- `/appointments.html` ดู/กรอง/เลื่อน/ยกเลิกนัด
- `/owners/new` ลงทะเบียนเจ้าของเมื่อไม่พบเบอร์
- `/pets/new?ownerId=...&returnTo=appointment` เพิ่มสัตว์แล้วกลับมาจอง
- `/swagger-ui.html` เอกสาร API ที่ Springdoc สร้างให้

ใช้เบอร์ไทยที่ลงทะเบียนไว้เพื่อเริ่ม flow. ถ้าไม่พบ ระบบส่งเบอร์และ `returnTo=appointment`
ไปหน้าลงทะเบียน; หลังบันทึกจะพาไปเพิ่มสัตว์ แล้วกลับหน้าจอง. ถ้ามีเจ้าของแต่ยังไม่มีสัตว์
จะแสดงลิงก์เพิ่มสัตว์และปิดการจองจนกว่าจะเพิ่มแล้ว.
หน้า My Pets ใช้ ownerId จากแฟ้มที่เลือก ไม่ใช้ค่า hardcode 1.

## กฎนัดหมายและข้อตกลงทีม
- ทุกบริการจองหนึ่งช่อง 30 นาที (รวม SURGERY ซึ่งเป็นเวลานัดพบ/เตรียมการผ่าตัด ไม่ใช่ระยะผ่าตัดทั้งหมด).
- เวลาใน Asia/Bangkok ต้องเป็นอนาคต ตรงนาที 00/30 และทั้งช่องอยู่ในเวลาเปิดคลินิก/เวรหมอ.
- เวรเป็นข้อความวันภาษาไทยและช่วงเวลา; หลายกะใช้ newline หรือ semicolon. เวรที่อ่านไม่ได้ไม่เปิดให้จอง.
- ตรวจคิวซ้ำทั้งหมอและสัตว์ โดยล็อก Doctor แล้ว Pet ภายใน transaction ก่อนตรวจและบันทึก.
- PENDING/CONFIRMED จองคิวไว้; CANCELLED คืนคิว; COMPLETED แก้ไขไม่ได้.
- PUT รับ version ล่าสุด; UI โหลดข้อมูลล่าสุดก่อนเปิดแก้ไข. คิวที่ดูว่างอาจถูกจองแทรกได้ จึงตรวจซ้ำเมื่อส่งจริง.
- COMPLETED/CONFIRMED สงวนเป็นสถานะสำหรับโมดูลหมอ/ประวัติที่ทีมจะเชื่อม ไม่เปิดให้ Guest เปลี่ยนสถานะเอง.
- ระบบค้นเบอร์เป็น Guest ตามโจทย์ ไม่ใช่การยืนยันตัวตนแบบ Account; ownerId เป็นข้อมูลเลือกแฟ้ม ไม่ใช่ token สิทธิ์.
- MedicalRecord สามารถอ้าง Appointment.appointmentId และความสัมพันธ์ Pet/Doctor ได้ตาม domain.

## เอกสารประกอบ
- `doc/appointment-factory.md`: Factory Method และ class diagram
- `doc/appointment-sequences.md`: สร้าง / เลื่อน / ยกเลิก
- `doc/appointment-contract.md`: fields, endpoints, integration contract
- `doc/sql/appointment-schema.sql`: FK/index/schema อ้างอิง PostgreSQL
- `testresult/appointment-final.md`: ผลทดสอบรอบสุดท้าย

## ทดสอบ
`mvn -f code/pom.xml test` — 115 tests ผ่าน (H2 ใช้เฉพาะทดสอบ).
`pnpm --dir code/frontend-tests install --frozen-lockfile` แล้ว `pnpm --dir code/frontend-tests test` — 8 DOM tests ผ่าน.
`python test/appointment_http_test.py http://localhost:8080` — ใช้กับฐานข้อมูล disposable เท่านั้น เพราะสร้าง fixtures.
ตรวจ browser จริง: ค้นหา → จอง #4 → ดูรายการ → เลื่อนไป 13:00 → ยกเลิก ผ่าน.
ตรวจ layout ที่ความกว้างมือถือและ 1280px; desktop ไม่ล้นแนวนอน.
