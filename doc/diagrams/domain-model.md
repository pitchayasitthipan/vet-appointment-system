# Domain Model — PawCare

แสดงแนวคิดหลักของธุรกิจ (ไม่ใช่คลาสของ Controller/Service) โดยอิง Entity ใน `code/src/main/java/com/example/petclinic/domain/entity/`.

```mermaid
classDiagram
    class PetOwner
    class PetOwnerDetail
    class Pet
    class Doctor
    class Appointment
    class MedicalRecord
    class ServiceType {
      <<enumeration>>
      CONSULTATION
      VACCINE
      SURGERY
    }
    class AppointmentStatus {
      <<enumeration>>
      PENDING
      CONFIRMED
      COMPLETED
      CANCELLED
    }
    PetOwner "1" -- "1" PetOwnerDetail : contact details
    PetOwner "1" -- "0..*" Pet : owns
    Pet "1" -- "0..*" Appointment : receives care
    Doctor "1" -- "0..*" Appointment : assigned
    Appointment "1" -- "0..*" MedicalRecord : documents
    Appointment --> ServiceType : type
    Appointment --> AppointmentStatus : status
```

การตีความ: เจ้าของสัตว์เลี้ยงลงทะเบียนข้อมูลและสัตว์เลี้ยงของตน จากนั้นจองนัดกับสัตวแพทย์ได้ โดยนัดหมายมีประเภทบริการและสถานะ ส่วนประวัติการรักษาผูกกับนัดหมาย ตรวจสอบข้อกำหนดของความสัมพันธ์กับ ER Diagram และ Entity อีกครั้งก่อนนำเสนอ.
