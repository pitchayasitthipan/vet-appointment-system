# Data Dictionary — Appointment

ผู้รับผิดชอบ: อ้น / Nathapat 6733805834  
Branch: `Nathapat_6733805834_04`  
วันที่จัดทำ: 9 ตุลาคม 2026 (commit 17)

เอกสารนี้อธิบายตาราง `appointment` ของโมดูลนัดหมาย ใช้ 6 Table.pdf เป็นแบบออกแบบเริ่มต้นของกลุ่ม และตรวจสถานะปัจจุบันโดยเทียบกับ [Appointment.java](../code/src/main/java/com/example/petclinic/domain/entity/Appointment.java), DTO, service และฐานข้อมูล PostgreSQL `appointment_dev` ที่ตรวจวันที่ 9 ตุลาคม 2026 โดยมี [SQL อ้างอิง](sql/appointment-schema.sql) แยกต่างหาก ไม่ใช่ Data Dictionary ของโมดูล Pet, Owner หรือ Doctor ทั้งหมด

## ตาราง appointment

วัตถุประสงค์: เก็บนัดหมายสัตว์เลี้ยงกับสัตวแพทย์ ประเภทบริการ อาการ คำแนะนำเตรียมตัว สถานะ และเลขรุ่นข้อมูลสำหรับป้องกันการแก้ไขทับข้อมูลล่าสุด

คอลัมน์ชนิดข้อมูลและ “NULL” ด้านล่างอ้างอิงฐานข้อมูล PostgreSQL ที่ตรวจจริง ส่วนกฎ API และค่าที่แอปสร้างให้อธิบายแยกไว้ เพื่อไม่สับสนกับ default/constraint ที่ฐานข้อมูลบังคับจริง

| คอลัมน์ | ชนิดข้อมูล / ความยาว | NULL | Key | ค่าเริ่มต้นในฐานข้อมูลจริง | ความหมายและกฎของแอป |
| --- | --- | --- | --- | --- | --- |
| `appointment_id` | `BIGINT` (Identity) | ไม่ได้ | PK | สร้างอัตโนมัติด้วย Identity | รหัสนัดหมายไม่ซ้ำ; JPA ใช้ `GenerationType.IDENTITY`; ผู้เรียก API ไม่กำหนดเอง |
| `pet_id` | `BIGINT` | ไม่ได้ | FK → `pet.pet_id` | ไม่มี | สัตว์ที่รับบริการ; ตอนสร้างต้องเป็นรหัสบวก มีอยู่จริง และอยู่ในแฟ้มเจ้าของที่ได้รับสิทธิ์; ไม่เปลี่ยนสัตว์ตอนเลื่อนนัด |
| `doctor_id` | `BIGINT` | ไม่ได้ | FK → `doctor.doctor_id` | ไม่มี | สัตวแพทย์ผู้รับนัด; ต้องเป็นรหัสบวก มีอยู่จริง และมีเวรครอบคลุมช่องนัด; เปลี่ยนได้ตอนเลื่อนนัด |
| `appointment_date_time` | `TIMESTAMP WITHOUT TIME ZONE` | ไม่ได้ | — | ไม่มี | วันเวลาเริ่มนัด; Java ใช้ `LocalDateTime` และตีความเป็นเวลา `Asia/Bangkok`; ต้องอยู่ในอนาคต นาที 00/30 วินาทีและเศษวินาทีเป็น 0; ทั้งช่อง 30 นาทีต้องอยู่ในเวลาคลินิกและเวรหมอ |
| `service_type` | `VARCHAR(20)` | ไม่ได้ | — | ไม่มี | ประเภทบริการ: `CONSULTATION`, `VACCINE`, `SURGERY`; เก็บชื่อ enum เป็นข้อความ; Factory Method เลือกคำแนะนำตามบริการ |
| `status` | `VARCHAR(20)` | ไม่ได้ | — | ไม่มี SQL DEFAULT | สถานะ: `PENDING`, `CONFIRMED`, `COMPLETED`, `CANCELLED`; แอปสร้างนัดใหม่เป็น `PENDING`; รายละเอียดสถานะอยู่ด้านล่าง |
| `symptoms` | `VARCHAR(1000)` | ได้ | — | ไม่มี | รายละเอียดอาการ/เหตุผลที่มารับบริการ; API สร้างและแก้ไขบังคับไม่ว่าง สูงสุด 1,000 ตัวอักษร และ Factory ตัดช่องว่างหัวท้ายก่อนบันทึก |
| `preparation_instructions` | `VARCHAR(500)` | ได้ | — | ไม่มี | คำแนะนำเตรียมตัวที่ Factory ของแต่ละบริการสร้างให้; API ไม่รับให้ผู้ใช้กำหนดเอง; คำนวณใหม่เมื่อแก้ไขบริการ |
| `version` | `BIGINT` | ไม่ได้ | — | ไม่มี SQL DEFAULT | เลขรุ่นข้อมูลที่ Hibernate ดูแลผ่าน `@Version`; เริ่มที่ 0 สำหรับนัดใหม่ และเพิ่มเมื่อมีการอัปเดตแถว; PUT ต้องส่งเลขรุ่นล่าสุดเป็นจำนวนเต็มไม่ติดลบ รุ่นไม่ตรงตอบ 409 |

**ความต่างระหว่าง SQL อ้างอิงกับฐานข้อมูลจริง:** ฐานข้อมูลที่ตรวจมี `version NOT NULL` และ CHECK ของ enum แต่ไม่มี SQL DEFAULT ให้ status/version; SQL อ้างอิงกำหนด DEFAULT PENDING/0 และใช้ BIGSERIAL จึงไม่ใช่ snapshot ของฐานข้อมูลปัจจุบัน **JPA:** `status` มีค่าเริ่มต้น `PENDING` ใน Java และ Factory แต่ annotation ไม่ประกาศ default ที่ฐานข้อมูล ส่วน `version` ใช้ `@Version` โดยไม่ประกาศ `nullable=false` หรือ SQL default ใน annotation จึงไม่ควรถือว่า `ddl-auto=update` สร้าง `NOT NULL DEFAULT 0` เหมือน SQL อ้างอิงเสมอ ต้องตรวจ schema ของฐานข้อมูลปลายทางก่อนใช้เป็น migration; เอกสารนี้ไม่ได้เปลี่ยน schema

## ค่าที่อนุญาต

| service_type | ความหมาย |
| --- | --- |
| `CONSULTATION` | ตรวจรักษาทั่วไป |
| `VACCINE` | ฉีดวัคซีน |
| `SURGERY` | นัดพบ/เตรียมการสำหรับการผ่าตัด; ช่องนัด 30 นาทีไม่ได้หมายถึงระยะผ่าตัดทั้งหมด |

| status | ความหมาย | ผลต่อคิวและการแก้ไข |
| --- | --- | --- |
| `PENDING` | รอยืนยันนัด | จองคิวหมอและสัตว์; แก้ไข/ยกเลิกได้เมื่อยังไม่ถึงเวลา |
| `CONFIRMED` | ยืนยันนัดแล้ว | จองคิวหมอและสัตว์; แก้ไข/ยกเลิกได้เมื่อยังไม่ถึงเวลา |
| `COMPLETED` | รับบริการเสร็จสิ้น | ไม่อยู่ในกลุ่มคิวที่ตรวจชน; แก้ไข/ยกเลิกไม่ได้ |
| `CANCELLED` | ยกเลิกนัด | คืนคิว; แก้ไขไม่ได้; เรียกยกเลิกซ้ำคืนข้อมูลเดิมโดยไม่ลบแถว |

ปัจจุบัน API สร้าง `PENDING` และยกเลิกเป็น `CANCELLED` ได้ Staff ใช้ PATCH /api/v1/appointments/{id}/status พร้อม version ล่าสุดเพื่อยืนยัน PENDING → CONFIRMED ก่อนถึงเวลานัด และปิด CONFIRMED → COMPLETED เมื่อถึงเวลานัดแล้ว; สถานะปลายทางอื่นตอบ 400 และ transition/version ไม่ถูกต้องตอบ 409 ฐานข้อมูลจริงมี CHECK constraint สำหรับ service_type/status แต่ SQL อ้างอิงไม่ได้ประกาศ CHECK

## ความสัมพันธ์และ index

| รายการ | รายละเอียด |
| --- | --- |
| Primary key | `appointment_id` ระบุนัดหมายหนึ่งรายการ |
| ความสัมพันธ์สัตว์ | สัตว์หนึ่งตัวมีหลาย appointment; appointment หนึ่งรายการมีสัตว์หนึ่งตัว ผ่าน `pet_id` |
| ความสัมพันธ์หมอ | หมอหนึ่งคนมีหลาย appointment; appointment หนึ่งรายการมีหมอหนึ่งคน ผ่าน `doctor_id` |
| เจ้าของสัตว์ | ไม่มีคอลัมน์ `owner_id` ใน appointment; อ่านผ่าน `appointment.pet_id → pet.owner_id` และตรวจสิทธิ์ด้วย session |
| `idx_appointment_doctor_time` | Index บน `(doctor_id, appointment_date_time)` ช่วยค้นนัดหมอและตรวจคิว |
| `idx_appointment_pet_time` | Index บน `(pet_id, appointment_date_time)` ช่วยค้นนัดสัตว์และตรวจคิว |
| `idx_pet_owner` | Index บน `pet(owner_id)` สำหรับค้นสัตว์ในแฟ้มเจ้าของ; เป็น index ของตารางที่โมดูลนี้อ่าน ไม่ใช่คอลัมน์เพิ่มใน appointment |

FK ใน SQL อ้างอิงไม่ได้กำหนด `ON DELETE CASCADE`; โมดูลนี้ยกเลิกนัดด้วยการเปลี่ยนสถานะ ไม่ลบ appointment ส่วนตาราง `pet` ใช้ Pet entity ของทีมจาก PR #9 โดยตรงแล้ว ไม่มี projection entity ซ้ำ

## กฎ API ที่เกี่ยวข้องกับข้อมูล

- สร้างนัดผ่าน `POST /api/v1/appointments` รับ `ownerId`, `petId`, `doctorId`, `appointmentDateTime`, `serviceType`, `symptoms` โดยรหัสทั้งสามต้องเป็นจำนวนเต็มบวก
- `ownerId` ใช้เลือก/ตรวจแฟ้ม ไม่บันทึกเป็นคอลัมน์ใน appointment; Guest ต้องตรงกับ session `myOwnerId` ส่วน Staff ตรวจจาก session `isStaff` ฝั่งเซิร์ฟเวอร์
- เลื่อนนัดผ่าน `PUT /api/v1/appointments/{id}` รับ `doctorId`, `appointmentDateTime`, `serviceType`, `symptoms`, `version`; คงรหัสนัด สัตว์ และสถานะเดิม
- `appointmentDateTime` ส่งเป็น ISO local date-time เช่น `2026-12-01T09:30:00` โดยไม่มี offset; ตัวอย่างเป็นรูปแบบข้อมูล การจองจริงยังต้องผ่านตารางเวรและเวลาคลินิก
- คิวชนเมื่อหมอเดียวกัน **หรือ** สัตว์เดียวกันมีนัดเวลาเริ่มเดียวกันในสถานะ `PENDING` / `CONFIRMED`; ใช้ transaction และการล็อกแถว Doctor แล้ว Pet ก่อนตรวจและบันทึก ไม่ได้มี unique constraint ของคิวในฐานข้อมูล
- Session เก็บแฟ้มที่ค้นหาด้วยเบอร์โทร; ไม่เพิ่มตาราง User/Login หรือคอลัมน์ session ใน appointment
- MedicalRecord สามารถอ้างอิง `appointment_id` เมื่อรวมโมดูล แต่ branch นี้ยังไม่ได้เพิ่ม FK หรือเปลี่ยนตาราง MedicalRecord ของทีม

## แหล่งตรวจสอบ

- [Appointment entity](../code/src/main/java/com/example/petclinic/domain/entity/Appointment.java)
- [DTO สร้างนัด](../code/src/main/java/com/example/petclinic/dto/request/AppointmentRequestDTO.java) และ [DTO เลื่อนนัด](../code/src/main/java/com/example/petclinic/dto/request/AppointmentUpdateDTO.java)
- [AppointmentService](../code/src/main/java/com/example/petclinic/service/AppointmentService.java) และ [implementation](../code/src/main/java/com/example/petclinic/service/impl/AppointmentServiceImpl.java), [AppointmentSchedulePolicy](../code/src/main/java/com/example/petclinic/service/AppointmentSchedulePolicy.java) และ [AppointmentFactory](../code/src/main/java/com/example/petclinic/factory/AppointmentFactory.java)
- [ข้อตกลงโมดูล](appointment-contract.md) และ [SQL อ้างอิง](sql/appointment-schema.sql)

## ความต่างจาก 6 Table.pdf ที่ต้องแจ้งผู้รวบรวม

| แบบออกแบบกลุ่ม | Entity / ฐานข้อมูลปัจจุบัน |
| --- | --- |
| appointment_type VARCHAR(30), GENERAL | service_type VARCHAR(20), CONSULTATION (VACCINE/SURGERY ยังเหมือนเดิม) |
| appointment_date DATE + appointment_time TIME | appointment_date_time TIMESTAMP WITHOUT TIME ZONE |
| initial_symptoms TEXT | symptoms VARCHAR(1000) |
| status VARCHAR(30) | status VARCHAR(20) |
| created_at TIMESTAMP | ยังไม่มีในโมดูลปัจจุบัน ต้องตกลงกับทีมก่อนเพิ่ม |
| PK BIGSERIAL | BIGINT Identity |
| ไม่มี preparation_instructions / version | เพิ่มคำแนะนำตาม Factory และเลขรุ่นข้อมูล |

PK/FK appointment_id, pet_id, doctor_id ยังเชื่อมตามแบบเดิม เอกสารนี้บันทึกสิ่งที่พัฒนาแล้ว ไม่ได้เปลี่ยนชื่อคอลัมน์หรือเพิ่ม created_at เอง โดยเจ้าของโมดูลส่งรายละเอียดให้เชอรวบรวมใน doc/data-dictionary.md ตามข้อตกลงทีม
