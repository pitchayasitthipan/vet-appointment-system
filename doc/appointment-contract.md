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
- เชอ: PetOwner/PetOwnerDetail มีแล้วใน develop แต่ยังไม่มี API; ใช้ appointment guest adapter ที่ค้นเบอร์โดยตัดช่องว่างและขีดทั้งค่ารับเข้าและค่าที่เก็บ. เบอร์ไทย 9-10 หลักขึ้นต้น 0; ไม่พบคืน 404, หลายแฟ้มคืน 409 เพื่อให้คลินิกตรวจสอบ ไม่เลือกแฟ้มแทน.
- โอ่ง: develop ยังไม่มี Pet. เพิ่ม entity/repository ขั้นต่ำ `Pet(petId, petName, petOwner)` เพื่อให้ FK และ flow ทำงานจริง; ต้องตกลงชื่อ field เมื่อรวมโมดูล Pet (ไม่ทำ CRUD Pet ซ้ำ).
- บูบู้: ใช้ DoctorRepository และ Doctor.workSchedule เดิม; ไม่เปลี่ยนหน้าสัตวแพทย์หรือ Singleton.
- เนย: อ้างอิง Appointment.appointmentId จาก MedicalRecord; สถานะ COMPLETED สงวนไว้สำหรับ flow บันทึกการรักษาที่ทีมจะเชื่อมภายหลัง.

## API ที่จะใช้
`/api/appointments`: POST, GET (ownerId, status, page, size, sort).
`/api/appointments/{id}`: GET/PUT (ownerId), PATCH `/{id}/cancel` (ownerId).
`/api/appointments/availability`: GET (doctorId, date).
`/api/appointment-guests/lookup`: POST `{phone}` → ownerId, firstName, lastName.
`/api/appointment-guests/{ownerId}/pets`: GET → petId, petName.

หน้าลงทะเบียนเป็นงานเชอ: ตั้ง URL ผ่าน `/api/appointment-guests/config`; ค่าเริ่มต้น `/owners.html` พร้อม query `phone` และ `returnTo=/appointment-create.html`. หลังรวมงานต้องตรวจเส้นทางนี้กับหน้าจริงของเชอ.

## API ที่ทำแล้ว ณ commit 11
- GET รายการรับ `direction=asc|desc`, `sort=appointmentId|appointmentDateTime|status|serviceType`; size 1-100, page เริ่ม 0. ค่าเริ่มต้น size=10, sort=appointmentDateTime, direction=asc.
- POST สร้างคืน 201 พร้อม Location และ version=0. PUT รับ version ล่าสุดและคืน version ที่เพิ่มแล้ว; ยกเลิกผ่าน PATCH ไม่ลบแฟ้ม.
- error ใช้รูปแบบร่วม timestamp/status/error/message หรือ errors สำหรับ field validation.
- Availability คืน array วันเวลา ISO ของช่องว่างฝั่งหมอ; create/update ตรวจคิวซ้ำฝั่งสัตว์อีกครั้ง. CANCELLED ไม่นับเป็นคิวที่ถูกจอง.
- เวลาเวรหมอรองรับวันภาษาไทย เช่น `จันทร์ - ศุกร์: 09:00 - 17:00`, `ทุกวัน: 09:00 - 17:00` หรือหลายกะคั่นด้วย newline/semicolon. ไม่รองรับกะข้ามคืน. เวรที่อ่านไม่ได้ไม่เปิดช่องจอง.
- เจ้าของสัตว์ทุกคนต้องใช้เบอร์ไม่ซ้ำเมื่อรวมกับโมดูลเชอ. ปัจจุบัน adapter ตรวจกรณีซ้ำได้ แต่ไม่ได้เปลี่ยน schema ของเพื่อน.
- Guest phone lookup เป็นการเลือกแฟ้มตามโจทย์ ไม่ใช่การยืนยันตัวตน. ownerId จำกัดผลตามแฟ้ม แต่ไม่ได้เป็นสิทธิ์เข้าถึงแบบระบบ Login.
- กำหนด `appointments.owner-registration-path` เพื่อเปลี่ยนหน้าลงทะเบียนเมื่อทีมรวมหน้าจริง. ในรอบ 11 commits ยังไม่ได้ redirect จาก UI.
- ตาราง Pet ขั้นต่ำยังต้องตรวจ field mapping เมื่อรวมกับโมดูลโอ่ง. API เจ้าของ/Pet CRUD และหน้าลงทะเบียนไม่ใช่งานใน branch นี้.

## ลำดับ commit
1. scope/API contract 2. domain 3. repository 4. DTO validation 5. factory
6. schedule rules 7. create/read service 8. update/cancel service 9. REST API
10. guest adapter 11. booking layout 12. booking integration 13. list/edit/cancel UI
14. tests 15. diagrams, test report, delivery documentation.
