# Factory Method — Appointment (อ้น)

`AppointmentFactory` เป็น Creator และ `createAppointment()` เป็น Factory Method.
`ConsultationAppointmentFactory`, `VaccineAppointmentFactory`, `SurgeryAppointmentFactory`
เป็น Concrete Creators ที่สร้าง Appointment พร้อมคำแนะนำเตรียมตัวต่างกัน.
`create(...)` ประกอบข้อมูลร่วมและกำหนดสถานะ PENDING; `AppointmentFactoryRegistry`
เลือก creator จาก ServiceType ผ่าน Spring beans และตรวจว่าครบทุกประเภทโดยไม่ซ้ำ.

เก็บทุกประเภทในตาราง appointment เดียว โดยใช้ service_type แยกประเภท เพื่อให้ MedicalRecord
อ้างอิง appointment_id ได้โดยไม่ต้องรู้ชนิดบริการและไม่ต้องเพิ่มตารางย่อย.
Factory ไม่บันทึกฐานข้อมูล และไม่แทนการตรวจเจ้าของ ตารางเวร หรือคิวซ้ำใน Service.

```mermaid
classDiagram
  class AppointmentFactory {
    <<abstract>>
    +create(pet, doctor, time, symptoms) Appointment
    #createAppointment() Appointment
    +getServiceType() ServiceType
  }
  AppointmentFactory <|-- ConsultationAppointmentFactory
  AppointmentFactory <|-- VaccineAppointmentFactory
  AppointmentFactory <|-- SurgeryAppointmentFactory
  AppointmentFactoryRegistry --> AppointmentFactory : selects creator
  AppointmentFactory --> Appointment : creates
```

## สถานะส่งงานรอบ 11 commits
เสร็จ: เอกสารข้อตกลง, domain, repositories, DTO validation, Factory Method, กฎตารางเวร,
Service สร้าง/ค้นหา/แก้ไข/ยกเลิก, REST API, Guest lookup, tests และโครงหน้าจองที่รองรับมือถือ.
หน้า `/appointment-create.html` เป็น layout ใน commit 11 ปิดปุ่มและฟอร์มไว้ก่อน;
การเชื่อมหน้าเว็บกับ API เป็น commit 12 ตามแผนเดิม ไม่แสดงการจองสำเร็จจำลอง.
ยังเหลือในรอบถัดไป: การเชื่อมหน้าจอง, หน้ารายการ/แก้ไข/ยกเลิก, ทดสอบเพิ่มเติม และ Sequence Diagrams.

ทดสอบทั้งหมดโดยใช้ H2 เฉพาะใน test scope ไม่ต้องเชื่อม PostgreSQL:
```powershell
mvn -f code/pom.xml test
```
`src/test/resources/application.properties` แยกฐานข้อมูลทดสอบออกจาก production.

ผลตรวจวันที่ 8 ตุลาคม 2026: คอมไพล์ Java release 17 บน JDK 26.0.2 และ Maven 3.9.16 ผ่าน.
ทดสอบรวม 26 กรณี ผ่านทั้งหมด (Factory 13, DoctorService 10, ClinicConfigService 3).
Factory tests ครอบคลุมทุกประเภทบริการ คำแนะนำเฉพาะประเภท การสร้าง object แยกกัน
ข้อมูลไม่ครบ อาการว่าง/ยาวเกิน และ factory registration ไม่ครบ/ซ้ำ.
รอบแรกยังไม่ได้ตรวจ JPA queries/locking; รอบ 11 commits เพิ่ม integration tests ตรวจ mappings,
queries, การเปลี่ยน version, การคืนคิว และ guest phone lookup กับ H2 แล้ว.
ยังไม่ได้ทดสอบ PostgreSQL จริงหรือการแข่งขันระหว่างหลาย transaction พร้อมกัน.
