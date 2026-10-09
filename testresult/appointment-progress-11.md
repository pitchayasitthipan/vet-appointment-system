# Appointment — ผลตรวจรอบ 11 commits

ผู้รับผิดชอบ: อ้น / Nathapat_6733805834_04. วันที่: 9 ตุลาคม 2026.

## คำสั่งและผล
`mvn -B -f code/pom.xml test` บน Maven 3.9.16, JDK 26.0.2 (compile target Java 17).
ผ่านทั้งหมด 61 tests, failures=0, errors=0, skipped=0.

| ชุดทดสอบ | จำนวน | สิ่งที่ตรวจ |
| --- | ---: | --- |
| AppointmentFactoryTest | 13 | ประเภทบริการ การเตรียมตัว ข้อมูลขาด/ผิด และ factory registration |
| AppointmentSchedulePolicyTest | 5 | เวลาอนาคต ช่อง 30 นาที เวรภาษาไทย วันหยุด เวลาเปิดคลินิก |
| AppointmentServiceTest | 13 | ตรวจเจ้าของ คิวซ้ำ paging availability เลื่อน/ยกเลิก และ stale version |
| AppointmentControllerTest | 5 | HTTP 201/400/404/409, Location, validation และ ownerId |
| AppointmentGuestServiceTest | 6 | normalize phone ไม่พบ/ซ้ำ และสัตว์เฉพาะเจ้าของ |
| AppointmentGuestControllerTest | 3 | lookup/pets/config และข้อมูลตอบกลับไม่รวมเบอร์หรืออีเมล |
| AppointmentPersistenceTest | 2 | JPA จริงบน H2: FK, queries, version, คืนคิว และค้นหาเบอร์ที่เก็บแบบมีขีด |
| PetclinicApplicationTests | 1 | Spring context และ wiring ของทุกโมดูล |
| DoctorServiceTest + ClinicConfigServiceTest | 13 | regression tests เดิม |

H2 อยู่ใน test scope และใช้ฐานข้อมูลในหน่วยความจำ; ไม่แก้ฐานข้อมูล PostgreSQL ของคลินิก.
การทดสอบ H2 ไม่ใช่หลักฐานการทดสอบ PostgreSQL หรือ concurrent booking หลาย transaction พร้อมกัน.

## หน้าเว็บ
`/appointment-create.html` เป็น layout ขั้น 11: header, ขั้นตอน, phone lookup, pet/service,
doctor/date/time, preparation sidebar, labels และ CSS สำหรับหน้าจอกว้าง/มือถือ.
ฟอร์มยัง disabled จนกว่าจะเชื่อม API ใน commit 12; ไม่มี mock booking success.
ตรวจโครง HTML: ID ไม่ซ้ำ, label ชี้ field จริง, stylesheet มีอยู่, ฟอร์มปิดอยู่ และมี mobile rules.
ยังไม่ได้ยืนยันภาพ render: เบราว์เซอร์ในแอปเชื่อม localhost ไม่สำเร็จและไม่มีเบราว์เซอร์ในเครื่องเชื่อมกับเครื่องมือ.

## งานที่ยังเหลือ
Commit 12: เชื่อมหน้าจองกับ API และ redirect ไปลงทะเบียนเมื่อไม่พบเบอร์.
Commit 13: หน้ารายการและจัดการนัดหมาย.
Commit 14-15: ทดสอบเพิ่มเติม, PostgreSQL/concurrency, Sequence Diagrams และเอกสารส่งงานฉบับสมบูรณ์.
ต้องตรวจชื่อ field ของ Pet และเส้นทางหน้าลงทะเบียนเมื่อรวมงานเพื่อน.
