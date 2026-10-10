# Contract: Appointment → MedicalRecord

อ้างอิง develop 8b52f96 ซึ่งรวม Owner/Staff, navbar และ Pet จาก PR #9 แล้ว

ตรวจร่วมกับ PR #12 head 8f80d28: REST และ web ของ MedicalRecord ตรวจ StaffAccess แล้ว แต่ service ยังไม่ได้เรียก guard ของ Appointment
Appointment ใช้ session isStaff กลางชุดเดียวกัน; ไม่เพิ่ม login หรือรหัส Staff แยกสำหรับ Appointment
ตัวเปิดแอป scripts/start-local.ps1 รับ STAFF_PASSCODE 8 หลักจาก environment หรือถามแบบซ่อนรหัสก่อนเปิดแอป ไม่มีรหัส production เริ่มต้นใน script

## รหัสและสถานะ

- `MedicalRecord.appointmentId` ใช้ `appointment.appointment_id` (BIGINT / Long) ซึ่งเป็น PK ไม่ใช่ pet_id หรือวันเวลานัด
- วันเวลานัดใช้ `appointment_date_time` ไม่เกี่ยวกับคอลัมน์ FK ของ MedicalRecord
- อนุญาตบันทึกประวัติสำหรับนัด `COMPLETED` เท่านั้น; `PENDING`, `CONFIRMED`, `CANCELLED` ตอบ 409
- Staff ยืนยัน PENDING → CONFIRMED แล้วปิด CONFIRMED → COMPLETED เมื่อถึงเวลานัด จึงบันทึกประวัติ

## เมธอดสำหรับโมดูล MedicalRecord

เรียก `AppointmentService.requireCompletedForMedicalRecord(appointmentId)` ก่อน save ทั้งตอนสร้างและแก้ไข โดยใช้รหัสนัดจาก request ที่จะบันทึก:

1. Controller ของ MedicalRecord ตรวจ `StaffAccess.requireStaff(session)` สำหรับ API และ web
2. Service ของ MedicalRecord เปิด write transaction ด้วย `@Transactional`
3. เรียกเมธอดนี้ผ่าน injected AppointmentService bean ใน transaction เดียวกัน
4. บันทึกประวัติหลังเมธอดคืน AppointmentResponseDTO สำเร็จ

เมธอดตรวจรหัสบวกและการมีอยู่จริง แล้วล็อกแถวนัดแบบ PESSIMISTIC_WRITE จน transaction ของผู้เรียกสิ้นสุด ไม่เรียก save หรือเปลี่ยนสถานะนัด
ใช้ Propagation.MANDATORY เพื่อป้องกันการตรวจแล้วปล่อย lock ก่อนการบันทึกประวัติ หากเรียกโดยไม่มี transaction ถือเป็น wiring error

| กรณี | ผล |
| --- | --- |
| null / รหัสไม่บวก | IllegalArgumentException → 400 ผ่าน GlobalExceptionHandler |
| รหัสไม่มีจริง | ResourceNotFoundException → 404 |
| สถานะยังไม่ COMPLETED | DuplicateResourceException → 409 |
| COMPLETED | คืน appointmentId, ownerId, petId, doctorId, วันเวลา และสถานะ |

นี่เป็น internal service contract; ไม่เปิด endpoint สำหรับข้ามการตรวจ Staff และไม่ใช่การตรวจสิทธิ์แทน MedicalRecord controller

## งานที่เจ้าของ MedicalRecord ต้องเชื่อม

Appointment เตรียมเมธอดและ tests แล้ว แต่ยังไม่ได้แก้ MedicalRecordServiceImpl หรือ controller ของเพื่อนให้เรียกเมธอดนี้
ดังนั้น API ของ MedicalRecord ปัจจุบันยังไม่ได้บังคับกฎนี้โดยอัตโนมัติ
เจ้าของ MedicalRecord ต้องเชื่อมทั้ง create/update, เพิ่ม transaction และสิทธิ์ Staff, พร้อม migration FK `medical_record.appointment_id → appointment.appointment_id` หลังตรวจข้อมูลเดิม
ใน PR #12 สิทธิ์ Staff ทำแล้ว จึงเหลือ service transaction + guard และ FK; AppointmentMedicalRecordIntegrationTest ทดสอบเรียก guard ก่อน create/update กับ MedicalRecord service จริงแล้ว
FK ตรวจการมีอยู่จริง ส่วนกฎ COMPLETED ตรวจใน service; ไม่กำหนด cascade ลบประวัติเมื่อเปลี่ยนนัด
