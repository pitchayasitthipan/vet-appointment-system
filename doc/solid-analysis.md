# SOLID Analysis: ระบบ PawCare Pet Clinic & Vaccination

วิเคราะห์หลักการ SOLID ทั้ง 5 ข้อ แต่ละข้อยกตัวอย่างจากโค้ดจริงใน Branch `develop` พร้อมบอกว่าถ้าไม่ทำแบบนี้จะเกิดปัญหาอะไร

| หลักการ | ตัวอย่างในระบบ |
| --- | --- |
| [S: Single Responsibility](#s-single-responsibility-principle) | `AppointmentSchedulePolicy`, `PetOwnerMapper`, `GlobalExceptionHandler`, `StaffAccess` |
| [O: Open/Closed](#o-openclosed-principle) | `AppointmentFactory` + `AppointmentFactoryRegistry` |
| [L: Liskov Substitution](#l-liskov-substitution-principle) | Factory 3 ตัวแทน `AppointmentFactory` / `*ServiceImpl` แทน Service Interface |
| [I: Interface Segregation](#i-interface-segregation-principle) | Service Interface แยกตามโมดูล, `AppointmentPetRepository` |
| [D: Dependency Inversion](#d-dependency-inversion-principle) | Controller พึ่ง Interface, ฉีด `Clock` จาก `AppointmentTimeConfig` |

---

## S: Single Responsibility Principle
> คลาสหนึ่งควรมีเหตุผลให้เปลี่ยนเพียงเรื่องเดียว

| คลาส | รับผิดชอบเรื่องเดียว | ถ้าไม่แยก |
| --- | --- | --- |
| `AppointmentSchedulePolicy` | กฎเวลานัด: อนาคต ช่อง 30 นาที อยู่ในเวลาคลินิกและเวรแพทย์ คำนวณช่องว่าง | กฎเวลาจะปนอยู่ใน `AppointmentServiceImpl` แก้เวลาเปิดคลินิกทีต้องไล่แก้ Service |
| `PetOwnerMapper` | แปลง DTO ↔ Entity ของเจ้าของ | Service ต้องรู้ทั้งกฎธุรกิจและวิธีแปลงข้อมูล |
| `GlobalExceptionHandler` | แปลง Exception เป็น JSON รูปแบบเดียวกัน (`timestamp`, `status`, `error`, `message`, `path`) | ทุก Controller ต้องเขียน try-catch เอง รูปแบบ error ไม่เหมือนกัน |
| `StaffAccess` | ตรวจสิทธิ์เจ้าหน้าที่ / เจ้าของแฟ้มจาก Session (`requireStaff`, `requireOwnerOrStaff`) | เงื่อนไขสิทธิ์กระจายทุก Controller แก้ทีต้องแก้หลายที่ |
| `PetSpecifications` | เงื่อนไขค้นหาสัตว์เลี้ยง (ชื่อ / สายพันธุ์ / ประเภท / ชื่อเจ้าของ) | query ค้นหาปนใน Service |

**แยกชั้นตาม Layered Architecture ด้วย:** Controller รับคำขอและตรวจสิทธิ์ → Service ทำกฎธุรกิจ → Repository เข้าถึงฐานข้อมูล

---

## O: Open/Closed Principle
> เปิดให้ขยาย ปิดไม่ให้ต้องแก้โค้ดเดิม

**ตัวอย่าง:** Factory Method ของนัดหมาย
- `AppointmentFactoryRegistry` รับ Factory ทุกตัวจาก Spring อัตโนมัติ (`List<AppointmentFactory>`) แล้วเลือกตาม `ServiceType`
- `AppointmentServiceImpl` เรียกแค่ `factories.create(request, pet, doctor)`

**ถ้าคลินิกเพิ่มบริการ "อาบน้ำตัดขน":**
1. เพิ่ม `GROOMING` ใน `ServiceType`
2. สร้าง `GroomingAppointmentFactory extends AppointmentFactory`
3. **ไม่ต้องแก้** `AppointmentServiceImpl`, `AppointmentFactoryRegistry` หรือ Controller

ถ้าลืมสร้าง Factory ระบบจะไม่ยอมเริ่มทำงาน (Registry ตรวจว่าทุก `ServiceType` มี Factory ครบ) จึงไม่มีทางลืมแล้วไปพังตอนผู้ใช้จอง

---

## L: Liskov Substitution Principle
> ใช้คลาสลูกแทนคลาสแม่ได้โดยระบบยังทำงานถูกต้อง

**ตัวอย่าง 1: Factory**
- `ConsultationAppointmentFactory`, `VaccineAppointmentFactory`, `SurgeryAppointmentFactory` ใช้แทน `AppointmentFactory` ได้ทุกตัว
- เมธอด `create(...)` ประกาศเป็น `final` คลาสลูกเปลี่ยนขั้นตอนประกอบนัดไม่ได้ เปลี่ยนได้แค่ส่วนของตัวเอง (`createAppointment()`) ทุกตัวจึงคืนนัดที่สถานะเริ่ม `PENDING` และมีข้อมูลครบเหมือนกัน
- `AppointmentFactoryTest` ทดสอบทั้ง 3 ประเภทด้วย test เดียวกัน (`@ParameterizedTest`) ผลต้องเหมือนกัน

**ตัวอย่าง 2: Service Interface**
- `PetOwnerServiceImpl` ใช้แทน `PetOwnerService` ได้ และใน Controller Test ใช้ Mock (`@MockitoBean PetOwnerService`) แทนตัวจริงได้โดย Controller ไม่ต้องรู้

---

## I: Interface Segregation Principle
> ไม่บังคับให้ผู้ใช้พึ่งเมธอดที่ไม่ได้ใช้

**ตัวอย่าง 1: Service Interface แยกตามโมดูล**
`PetOwnerService`, `PetService`, `DoctorService`, `AppointmentService`, `MedicalRecordService` แต่ละตัวมีแค่เมธอดของโมดูลตัวเอง Controller ของสัตว์เลี้ยงพึ่งแค่ `PetService` ไม่ต้องรู้จักเมธอดของนัดหมายหรือประวัติการรักษา

**ตัวอย่าง 2: `AppointmentPetRepository`**
โมดูลนัดหมายต้องอ่านข้อมูลสัตว์เลี้ยง แต่ไม่ควรแก้ไขหรือลบ จึงสร้าง Repository แคบ ๆ ที่มีแค่ 3 เมธอดที่ใช้จริง แทนการใช้ `PetRepository` ที่มีเมธอด save / delete ครบ

```java
/** Reads and locks shared Pet rows; all Pet writes belong to the Pet module. */
public interface AppointmentPetRepository extends Repository<Pet, Long> {
    Optional<Pet> findById(Long id);
    List<Pet> findByPetOwnerOwnerIdOrderByNameAsc(Long ownerId);
    Optional<Pet> findLockedById(Long id);   // ล็อกแถวกันจองชน
}
```
ผล: โมดูลนัดหมายเผลอแก้ข้อมูลสัตว์ไม่ได้ เพราะไม่มีเมธอดให้เรียก

**ตัวอย่าง 3:** `AppointmentGuestService` (ค้นแฟ้มด้วยเบอร์ก่อนจองนัด) แยกจาก `AppointmentService` (จัดการนัด) แต่ละฝั่งพึ่งเฉพาะที่ตัวเองใช้

---

## D: Dependency Inversion Principle
> โมดูลระดับสูงพึ่ง Abstraction ไม่พึ่งคลาสที่ลงมือทำจริง

**ตัวอย่าง 1: Controller พึ่ง Interface ผ่าน Constructor Injection**
```java
public class PetOwnerController {
    private final PetOwnerService petOwnerService;          // Interface
    public PetOwnerController(PetOwnerService petOwnerService) {
        this.petOwnerService = petOwnerService;              // Spring ฉีด PetOwnerServiceImpl ให้
    }
}
```
Controller ไม่ได้ `new PetOwnerServiceImpl()` เอง จึงเปลี่ยนตัวทำงานจริง หรือใช้ Mock ใน test ได้โดยไม่แก้ Controller

**ตัวอย่าง 2: เวลาปัจจุบัน (`Clock`)**
- `AppointmentTimeConfig` สร้าง Bean `Clock` เวลา `Asia/Bangkok`
- `AppointmentServiceImpl` และ `AppointmentSchedulePolicy` รับ `Clock` ผ่าน Constructor แทนการเรียก `LocalDateTime.now()` ตรง ๆ
- ผล: test กำหนดเวลาคงที่ได้ (`Clock.fixed(...)` ใน `AppointmentServiceTest`, `AppointmentSchedulePolicyTest`) ทดสอบกฎ "ห้ามจองย้อนหลัง" หรือ "ปิดนัดก่อนเวลาไม่ได้" ได้แน่นอนทุกครั้ง ไม่ขึ้นกับวันที่รัน test

---

## สรุป
| หลักการ | ประโยชน์ที่ได้ในโปรเจกต์ |
| --- | --- |
| S | แก้กฎเวลา / สิทธิ์ / รูปแบบ error ได้ที่เดียว |
| O | เพิ่มประเภทบริการใหม่โดยไม่แก้ Service เดิม |
| L | ทดสอบ Factory ทุกตัวด้วย test ชุดเดียว และ Mock Service ใน Controller Test ได้ |
| I | โมดูลนัดหมายอ่านข้อมูลสัตว์ได้ แต่แก้ไม่ได้ ลดโอกาสข้อมูลเสียข้ามโมดูล |
| D | Controller และ Service เปลี่ยนตัวทำงานจริงได้ ทดสอบด้วยเวลาคงที่ได้ |
