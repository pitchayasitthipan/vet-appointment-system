# Appointment module — อ้น (Nathapat 6733805834)

Base: develop. Branch: Nathapat_6733805834_04. Build: Maven, Java 17+, Spring Boot 4.1.1.

## ขอบเขต
Entity → Repository → DTO → Factory Method → Service → REST Controller → หน้าจองและรายการนัดหมาย → Tests → เอกสารและ Sequence Diagrams

Guest Flow: เบอร์โทร → ownerId → สัตว์ของเจ้าของ → หมอ/วันเวลา/บริการ → จอง
ไม่มี User, Account หรือ Login. การค้นหาด้วยเบอร์เป็นเพียงการเลือกแฟ้มตามข้อกำหนดกลุ่ม ไม่ใช่การยืนยันตัวตน.

## ข้อตกลงข้อมูล
- ตาราง appointment: appointment_id, pet_id (FK), doctor_id (FK), appointment_date_time, service_type, status, symptoms, preparation_instructions, version.
- ServiceType: CONSULTATION, VACCINE, SURGERY. AppointmentStatus: PENDING, CONFIRMED, COMPLETED, CANCELLED.
- วันเวลาใช้เวลาท้องถิ่น Asia/Bangkok รูปแบบ ISO `2026-10-12T09:00:00`.
- ช่องนัด 30 นาที จองตรงชั่วโมงหรือครึ่งชั่วโมง ตรวจตารางเวรข้อความภาษาไทยของ Doctor และเวลาเปิดคลินิก.
- PENDING/CONFIRMED จองและแก้ไขได้ก่อนเวลานัด; CANCELLED คืนคิว; COMPLETED แก้ไขไม่ได้.
- ownerId ต้องตรงกับ Pet.petOwner.ownerId. รายการและรายละเอียดต้องระบุ ownerId.

## จุดเชื่อมทีม
- เชอ: รวมโมดูล PetOwner/PetOwnerDetail, CRUD และหน้าลงทะเบียนจาก 20baa33 แล้ว. Appointment guest adapter ตัดช่องว่างและขีดทั้งค่ารับเข้าและค่าที่เก็บ; เบอร์ไทย 9-10 หลักขึ้นต้น 0; ไม่พบคืน 404, หลายแฟ้มคืน 409.
- โอ่ง: รวม Pet/CRUD/UI จาก cbb47b3 แล้ว ใช้ `Pet.name` (column name) ตามโมดูลทีม; Appointment DTO ส่งชื่อเป็น petName. Repository คงทั้ง query ของ Pet และ lock ของ Appointment.
- บูบู้: ใช้ Doctor และ Singleton เดิม; ปุ่มจองบนหน้าหมอส่งไปหน้าจองจริงแทน modal ที่จำลองสำเร็จ.
- เนย: อ้างอิง Appointment.appointmentId จาก MedicalRecord; สถานะ COMPLETED สงวนไว้สำหรับ flow บันทึกการรักษาที่ทีมจะเชื่อมภายหลัง.

## API ที่ใช้งานจริง
`/api/appointments`: POST, GET (ownerId, status, page, size, sort).
`/api/appointments/{id}`: GET/PUT (ownerId), PATCH `/{id}/cancel` (ownerId).
`/api/appointments/availability`: GET (doctorId, date).
`/api/appointment-guests/lookup`: POST `{phone}` → ownerId, firstName, lastName.
`/api/appointment-guests/{ownerId}/pets`: GET → petId, petName.

GET `/api/appointment-guests/config` คืน ownerRegistrationPath=`/owners/new` และ bookingReturnPath=`/appointment-create.html`.
UI ส่ง `phone` และ `returnTo=appointment` ให้หน้าลงทะเบียนของเชอ. บันทึกแล้วส่ง ownerId ไป `/pets/new`,
เพิ่มสัตว์แล้วกลับหน้าจอง. รับเฉพาะ returnTo ที่กำหนดไว้ ไม่ใช้ URL ปลายทางที่รับจากผู้ใช้ตรง ๆ.

## กฎ API
- GET รายการรับ `direction=asc|desc`, `sort=appointmentId|appointmentDateTime|status|serviceType`; size 1-100, page เริ่ม 0. ค่าเริ่มต้น size=10, sort=appointmentDateTime, direction=asc.
- POST สร้างคืน 201 พร้อม Location และ version=0. PUT รับ version ล่าสุดและคืน version ที่เพิ่มแล้ว; ยกเลิกผ่าน PATCH ไม่ลบแฟ้ม.
- error ใช้รูปแบบร่วม timestamp/status/error/message หรือ errors สำหรับ field validation.
- Availability คืน array วันเวลา ISO ของช่องว่างฝั่งหมอ; create/update ตรวจคิวซ้ำฝั่งสัตว์อีกครั้ง. CANCELLED ไม่นับเป็นคิวที่ถูกจอง.
- เวลาเวรหมอรองรับวันภาษาไทย เช่น `จันทร์ - ศุกร์: 09:00 - 17:00`, `ทุกวัน: 09:00 - 17:00` หรือหลายกะคั่นด้วย newline/semicolon. ไม่รองรับกะข้ามคืน. เวรที่อ่านไม่ได้ไม่เปิดช่องจอง.
- เจ้าของสัตว์ทุกคนใช้เบอร์ไม่ซ้ำตามโมดูลเชอที่รวมแล้ว. Adapter ยังตรวจกรณีข้อมูลเก่าที่หลายเบอร์ normalize แล้วตรงกัน.
- Guest phone lookup เป็นการเลือกแฟ้มตามโจทย์ ไม่ใช่การยืนยันตัวตน. ownerId จำกัดผลตามแฟ้ม แต่ไม่ได้เป็นสิทธิ์เข้าถึงแบบระบบ Login.
- กำหนด `appointments.owner-registration-path` เพื่อเปลี่ยนเส้นทางในอนาคต. ปัจจุบันเชื่อม UI กับหน้าลงทะเบียนจริงแล้ว.
- ไม่เพิ่ม User/Account. การรวมโมดูลเพื่อนเก็บประวัติ Git ต้นฉบับและไม่แก้ remote branch ของเพื่อน.

## ลำดับ commit
1. scope/API contract 2. domain 3. repository 4. DTO validation 5. factory
6. schedule rules 7. create/read service 8. update/cancel service 9. REST API
10. guest adapter 11. booking layout 12. booking integration 13. list/edit/cancel UI
14. tests 15. diagrams, test report, delivery documentation.
