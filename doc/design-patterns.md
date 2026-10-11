# Design Patterns: ระบบ PawCare Pet Clinic & Vaccination

เอกสารนี้อธิบาย Design Pattern ที่ใช้ในระบบ ทำไมถึงเลือก ใช้ที่คลาสไหน และมี test อะไรยืนยัน

| Pattern | กลุ่ม | ใช้กับ | คลาสหลัก |
| --- | --- | --- | --- |
| [Factory Method](#1-factory-method) | Creational | สร้างนัดหมายตามประเภทบริการ | `AppointmentFactory`, `AppointmentFactoryRegistry` |
| [Builder](#2-builder) | Creational | สร้างประวัติการรักษา | `MedicalRecord.Builder` |
| [Singleton](#3-singleton) | Creational | ค่ากำหนดคลินิก | `ClinicConfigService` |
| [DTO + Mapper](#4-dto--mapper) | Enterprise | แยกข้อมูลที่รับ-ส่งผ่าน API ออกจาก Entity | `PetOwnerRequestDTO`, `PetOwnerResponseDTO`, `PetOwnerMapper` |

Class Diagram ของทุก pattern อยู่ใน [`diagrams/class-diagram.md`](diagrams/class-diagram.md)

---

## 1. Factory Method

### ปัญหา
นัดหมายมี 3 ประเภทบริการ (`CONSULTATION`, `VACCINE`, `SURGERY`) แต่ละประเภทต้องมีคำแนะนำการเตรียมตัวต่างกัน ถ้าเขียน `if / switch` ตามประเภทไว้ใน Service ทุกครั้งที่เพิ่มบริการใหม่ต้องกลับไปแก้ Service และโค้ดสร้างนัดจะปนกับกฎธุรกิจ (ตรวจเวลา, ตรวจคิวซ้ำ)

### วิธีแก้
- **`AppointmentFactory`** (abstract class) มี Factory Method `createAppointment()` ให้คลาสลูกกำหนดส่วนที่ต่างกัน และมีเมธอด `create(pet, doctor, time, symptoms)` ประกอบส่วนที่เหมือนกันทุกประเภท (สถานะเริ่ม `PENDING`, สัตว์, แพทย์, เวลา, อาการ)
- **คลาสลูก 3 ตัว** แต่ละตัวบอกประเภทบริการของตัวเอง และใส่คำแนะนำการเตรียมตัว

| Factory | `ServiceType` | คำแนะนำการเตรียมตัว |
| --- | --- | --- |
| `ConsultationAppointmentFactory` | `CONSULTATION` | นำข้อมูลอาการและประวัติการรักษามาด้วย |
| `VaccineAppointmentFactory` | `VACCINE` | นำสมุดวัคซีนมาด้วย และแจ้งสัตวแพทย์หากสัตว์เลี้ยงมีอาการป่วย |
| `SurgeryAppointmentFactory` | `SURGERY` | ติดต่อคลินิกเพื่อรับคำแนะนำการเตรียมตัวเฉพาะจากสัตวแพทย์ก่อนวันนัด |

- **`AppointmentFactoryRegistry`** รับ Factory ทุกตัวจาก Spring (`List<AppointmentFactory>`) แล้วเก็บเป็น `Map<ServiceType, AppointmentFactory>` ตอนเริ่มระบบจะตรวจว่าทุก `ServiceType` มี Factory ครบและไม่ซ้ำ
- **`AppointmentServiceImpl`** เรียกแค่ `factories.create(request, pet, doctor)` ไม่ต้องรู้ว่าเป็นบริการประเภทไหน

```java
public abstract class AppointmentFactory {
    public abstract ServiceType getServiceType();
    protected abstract Appointment createAppointment();   // Factory Method

    public final Appointment create(Pet pet, Doctor doctor, LocalDateTime time, String symptoms) {
        Appointment appointment = createAppointment();
        appointment.setServiceType(getServiceType());
        appointment.setStatus(AppointmentStatus.PENDING);
        ...
    }
}
```

### ผลที่ได้
- **เพิ่มบริการใหม่** แค่เพิ่มค่าใน `ServiceType` และสร้าง Factory ใหม่ 1 คลาส ไม่ต้องแก้ `AppointmentServiceImpl`
- **เลื่อนนัดแล้วเปลี่ยนประเภทบริการ** ระบบเรียก Factory ใหม่ คำแนะนำการเตรียมตัวเปลี่ยนตามอัตโนมัติ
- **ข้อจำกัด:** ทุก Factory สร้างคลาส `Appointment` ตัวเดียวกัน ต่างกันที่ข้อมูล ถ้าอนาคตแต่ละบริการต้องมีพฤติกรรมต่างกันมาก อาจต้องแยก subclass ของ `Appointment`

**Test:** `AppointmentFactoryTest` (13 tests) ตรวจว่าแต่ละ Factory สร้างนัดถูกประเภท สถานะเริ่ม `PENDING` และ Registry ปฏิเสธ Factory ซ้ำหรือไม่ครบ

---

## 2. Builder

### ปัญหา
ประวัติการรักษา (`MedicalRecord`) มีหลายฟิลด์และส่วนใหญ่ไม่บังคับ (การวินิจฉัย การรักษา ชื่อวัคซีน วันที่ฉีด วันนัดครั้งถัดไป หมายเหตุ) ถ้าใช้ Constructor ต้องส่งพารามิเตอร์ยาว 7 ตัว เรียงผิดง่าย และต้องส่ง `null` หลายตัว

### วิธีแก้
`MedicalRecord` มีคลาสซ้อน `MedicalRecord.Builder` ตั้งค่าทีละฟิลด์ด้วยชื่อที่อ่านออก แล้วเรียก `build()` ครั้งเดียว

```java
// MedicalRecordServiceImpl.createMedicalRecord(...)
MedicalRecord medicalRecord = MedicalRecord.builder()
        .appointmentId(requestDTO.getAppointmentId())
        .diagnosis(requestDTO.getDiagnosis())
        .treatment(requestDTO.getTreatment())
        .vaccineName(requestDTO.getVaccineName())
        .vaccineDate(requestDTO.getVaccineDate())
        .nextVaccineDate(requestDTO.getNextVaccineDate())
        .notes(requestDTO.getNotes())
        .build();
```

### ผลที่ได้
- อ่านง่าย รู้ทันทีว่าค่าไหนคือฟิลด์อะไร
- ประวัติที่ไม่มีวัคซีนก็ไม่ต้องเรียก `.vaccineName(...)` ไม่ต้องส่ง `null`
- **ข้อจำกัด:** ต้องเขียนคลาส Builder เพิ่มเอง (ไม่ได้ใช้ Lombok) ถ้าเพิ่มฟิลด์ใหม่ต้องเพิ่มเมธอดใน Builder ด้วย

**Test:** `MedicalRecordServiceTest` ตรวจการสร้างและแก้ไขประวัติผ่าน Service

---

## 3. Singleton

### ปัญหา
ข้อมูลคลินิก (ชื่อ ที่อยู่ เบอร์ติดต่อ เวลาเปิดทำการ อัตราค่าบริการ) ต้องเป็นชุดเดียวกันทั้งระบบ ถ้าแต่ละส่วนสร้าง Object ของตัวเอง เมื่อแก้ค่าบริการที่หนึ่ง อีกที่จะยังเห็นค่าเดิม และระบบนัดหมายใช้เวลาเปิดทำการชุดนี้ตรวจว่าจองได้ไหม

### วิธีแก้
ใช้ Singleton ผ่าน Spring Bean ให้ Spring IoC Container สร้าง Instance เดียวและส่งตัวเดียวกันให้ทุกคลาสที่ต้องใช้

```java
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ClinicConfigService {
    public synchronized ClinicConfigResponseDTO getConfigDTO() { ... }
    public synchronized void updatePricePolicy(BigDecimal consultation, BigDecimal vaccine, BigDecimal surgery) { ... }
    public int getInstanceHashCode() { ... }
}
```

- เมธอดที่อ่าน / แก้ค่าเป็น `synchronized` กันหลายคำขอแก้ค่าพร้อมกัน
- ใช้โดย `ClinicConfigController` (`GET /api/v1/config`, `PUT /api/v1/config/fees`) และ `AppointmentSchedulePolicy` (ตรวจเวลาเปิดทำการตอนจองนัด)
- ตรวจสอบได้ที่ `GET /api/v1/config/singleton-check` ทุกครั้งที่เรียกจะได้ hash code เดียวกัน

### ผลที่ได้
- ทุกส่วนเห็นค่าชุดเดียวกัน แก้ที่เดียวมีผลทั้งระบบ
- ใช้ Spring จัดการ จึงไม่ต้องเขียน `private constructor` + `getInstance()` เอง และยัง inject / mock ใน test ได้
- **ข้อจำกัด:** ค่าที่แก้เก็บในหน่วยความจำ รีสตาร์ทระบบแล้วกลับเป็นค่าเริ่มต้น

**Test:** `ClinicConfigServiceTest.testSingletonScope_ShouldReturnSameInstance` ตรวจว่า inject สองจุดได้ Object เดียวกัน (`isSameAs`) รายละเอียดเพิ่มเติมใน [`singleton-pattern.md`](singleton-pattern.md)

---

## 4. DTO + Mapper

### ปัญหา
ถ้า Controller รับและส่ง Entity (`PetOwner`) ตรง ๆ
- ผู้เรียก API ส่งค่าที่ไม่ควรแก้ได้ เช่น `ownerId`, `createdAt`
- ข้อมูลที่ส่งออกจะผูกกับโครงสร้างตาราง เปลี่ยนตารางเมื่อไร API ก็เปลี่ยนตาม
- `PetOwner` กับ `PetOwnerDetail` แยกกัน 2 ตาราง แต่หน้าเว็บอยากได้ข้อมูลรวมในก้อนเดียว

### วิธีแก้
- **`PetOwnerRequestDTO`** รับข้อมูลเข้า พร้อมกฎ Validation (`@NotBlank`, `@Email`, `@Pattern` เบอร์โทร) ไม่ผ่าน → ตอบ 400
- **`PetOwnerResponseDTO`** ส่งข้อมูลออก รวมข้อมูลจาก `PetOwner` + `PetOwnerDetail` เป็นก้อนเดียว
- **`PetOwnerMapper`** แปลงไป-กลับ แยกออกจาก Service

| เมธอด | หน้าที่ |
| --- | --- |
| `toEntity(PetOwnerRequestDTO)` | สร้าง `PetOwner` + `PetOwnerDetail` จากข้อมูลที่รับมา |
| `updateEntity(PetOwner, PetOwnerRequestDTO)` | แก้ไข Entity เดิมตามข้อมูลใหม่ |
| `toResponse(PetOwner)` | แปลงเป็นข้อมูลที่ส่งออก |

### ผลที่ได้
- Service ทำแค่ Business Logic (เช่น ห้ามอีเมลหรือเบอร์ซ้ำ) ไม่ต้องรู้วิธีแปลงข้อมูล
- เปลี่ยนโครงสร้างตารางได้โดยไม่กระทบ API
- โมดูลอื่นใช้ DTO เหมือนกัน แต่แปลงข้อมูลด้วยเมธอด `fromEntity(...)` ใน Response DTO แทนคลาส Mapper แยก (เช่น `AppointmentResponseDTO.fromEntity`, `PetResponseDTO`, `DoctorResponseDTO`)

**Test:** `PetOwnerServiceImplTest`, `PetOwnerControllerTest` ตรวจการเพิ่ม แก้ไข และ Validation ผ่าน DTO
