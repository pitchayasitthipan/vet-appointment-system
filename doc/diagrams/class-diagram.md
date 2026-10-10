# Class Diagram

## 1. Domain (Entity และ Enum)

```mermaid
classDiagram
    direction LR
    class PetOwner {
        -Long ownerId
        -String firstName
        -String lastName
        -String email
        -String phone
        -LocalDateTime createdAt
    }
    class PetOwnerDetail {
        -Long ownerDetailId
        -String address
        -String emergencyContactName
        -String emergencyContactPhone
    }
    class Pet {
        -Long petId
        -String name
        -String species
        -String breed
        -String gender
        -LocalDate birthDate
        -Double weight
        -String microchipNumber
    }
    class Doctor {
        -Long doctorId
        -String firstName
        -String lastName
        -String specialization
        -String phone
        -String email
        -String workSchedule
    }
    class Appointment {
        -Long appointmentId
        -LocalDateTime appointmentDateTime
        -ServiceType serviceType
        -AppointmentStatus status
        -String symptoms
        -String preparationInstructions
        -Long version
    }
    class MedicalRecord {
        -Long medicalRecordId
        -Long appointmentId
        -String diagnosis
        -String treatment
        -String vaccineName
        -LocalDate vaccineDate
        -LocalDate nextVaccineDate
        -String notes
        -LocalDateTime createdAt
        +builder()$ Builder
    }
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

    PetOwner "1" -- "1" PetOwnerDetail
    PetOwner "1" -- "0..*" Pet
    Pet "1" -- "0..*" Appointment
    Doctor "1" -- "0..*" Appointment
    Appointment "1" -- "0..*" MedicalRecord
    Appointment ..> ServiceType
    Appointment ..> AppointmentStatus
```

## 2. คลาสที่ใช้ Design Pattern

### 2.1 Factory Method (สร้างนัดหมายตามประเภทบริการ)

```mermaid
classDiagram
    direction TB
    class AppointmentFactory {
        <<abstract>>
        +getServiceType()* ServiceType
        #createAppointment()* Appointment
        +create(pet, doctor, time, symptoms) Appointment
    }
    class ConsultationAppointmentFactory
    class VaccineAppointmentFactory
    class SurgeryAppointmentFactory
    class AppointmentFactoryRegistry {
        -Map~ServiceType, AppointmentFactory~ factories
        +create(request, pet, doctor) Appointment
    }
    AppointmentFactory <|-- ConsultationAppointmentFactory
    AppointmentFactory <|-- VaccineAppointmentFactory
    AppointmentFactory <|-- SurgeryAppointmentFactory
    AppointmentFactoryRegistry o-- AppointmentFactory : เลือกตาม ServiceType
    AppointmentServiceImpl --> AppointmentFactoryRegistry
```

### 2.2 Builder (ประกอบประวัติการรักษา)

```mermaid
classDiagram
    direction LR
    class MedicalRecordBuilder["MedicalRecord.Builder"] {
        +appointmentId(Long) Builder
        +diagnosis(String) Builder
        +treatment(String) Builder
        +vaccineName(String) Builder
        +vaccineDate(LocalDate) Builder
        +nextVaccineDate(LocalDate) Builder
        +notes(String) Builder
        +build() MedicalRecord
    }
    MedicalRecordServiceImpl --> MedicalRecordBuilder : MedicalRecord.builder()
    MedicalRecordBuilder ..> MedicalRecord : build()
```

### 2.3 Singleton (ค่ากำหนดคลินิก) และ 2.4 DTO + Mapper (เจ้าของสัตว์เลี้ยง)

```mermaid
classDiagram
    direction LR
    class ClinicConfigService {
        <<Singleton Spring Bean>>
        +getConfigDTO() ClinicConfigResponseDTO
        +getInstanceHashCode() int
        +updatePricePolicy(consultation, vaccine, surgery)
    }
    ClinicConfigController --> ClinicConfigService
    class PetOwnerMapper {
        +toEntity(PetOwnerRequestDTO) PetOwner
        +updateEntity(PetOwner, PetOwnerRequestDTO)
        +toResponse(PetOwner) PetOwnerResponseDTO
    }
    PetOwnerServiceImpl --> PetOwnerMapper
    PetOwnerMapper ..> PetOwnerRequestDTO
    PetOwnerMapper ..> PetOwnerResponseDTO
```

| Pattern | คลาสหลัก | หน้าที่ |
| --- | --- | --- |
| Factory Method | `AppointmentFactory` + Factory 3 ประเภท, `AppointmentFactoryRegistry` | สร้างนัดหมายพร้อมคำแนะนำการเตรียมตัวตามประเภทบริการ |
| Builder | `MedicalRecord.Builder` | ประกอบประวัติการรักษาที่มีหลายฟิลด์ไม่บังคับ |
| Singleton | `ClinicConfigService` | ข้อมูลคลินิกและอัตราค่าบริการมี Instance เดียว (`GET /api/v1/config/singleton-check`) |
| DTO + Mapper | `PetOwnerRequestDTO`, `PetOwnerResponseDTO`, `PetOwnerMapper` | แยก Entity ออกจากข้อมูลที่รับ-ส่งผ่าน API |
