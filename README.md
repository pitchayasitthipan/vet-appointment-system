## Pet Clinic Appointment & Vaccination System
**ระบบบริหารจัดการนัดหมายและประวัติการฉีดวัคซีนสำหรับคลินิกสัตว์เลี้ยง**

เว็บแอปพลิเคชันบริหารจัดการคลินิกสัตว์เลี้ยงพัฒนาด้วย Java 17 และ Spring Boot ตามสถาปัตยกรรมแบบ Layered Architecture ระบบได้ออกแบบเพื่อรองรับการทำงานของเจ้าของสัตว์เลี้ยงในการบันทึกข้อมูลสัตว์เลี้ยงและการจองคิวนัดหมายตรวจรักษาหรือฉีดวัคซีน ตลอดจนสนับสนุนการปฏิบัติงานของสัตวแพทย์และเจ้าหน้าที่ในการบริหารจัดการตารางเวลา การบันทึกประวัติการรักษา และการประมวลผลรายงานสรุปผลการบริการ

---

## สมาชิกกลุ่ม (Group Members) - กลุ่มที่ 11 (Section 04)

| ลำดับ | ชื่อ-นามสกุล | รหัสนักศึกษา | Section | Branch | หน้าที่รับผิดชอบ |
| --- | --- | --- | --- | --- | --- |
| 1 | นายสิทธิโชค มุขนาค | 673380428-6 | 04 | `sitthichok_6733804286_04` |  |
| 2 | นายณัฐภัทร ฉ่ำตะคุ | 673380583-4 | 04 | `nathapat_6733805834_04` |  |
| 3 | นางสาวพิชยา สิทธิพันธุ์ | 673380596-5 | 04 | `pitchaya_6733805965_04` |  |
| 4 | นายสรวิศ สุคงเจริญ | 673380606-8 | 04 | `soravit_6733806068_04` |  |
| 5 | นางสาวอมลวรรณ พิมพิชัย | 673380608-4 | 04 | `amonwan_6733806084_04` |  |

---

## Tech Stack

* **Backend Framework:** Java 17+, Spring Boot 3.x (Spring Data JPA, Spring Validation)
* **Build Tool:** Gradle
* **Database:** PostgreSQL / MySQL (Relational Database)
* **ORM:** Spring Data JPA (Hibernate)
* **Frontend Framework:** Thymeleaf
* **API Documentation:** OpenAPI 3.0 / Swagger UI
* **Testing Framework:** JUnit 5, Mockito, Spring Boot Test
* **DevOps & Deployment:** Docker, Docker Compose, Render / Cloud Service

---

## System Architecture

```text
Presentation Layer (RestController / Thymeleaf View)
       ↓
Service Layer (Business Logic & Transaction Management)
       ↓
Repository Layer (Data Access - Spring Data JPA)
       ↓
Domain / Entity Layer (Entities, Value Objects, Enums) + DTO Layer (Request/Response DTO + Mapper)

```

### กระบวนการทำงานของระบบแบ่งตามสิทธิ์ผู้ใช้งาน (Role-based Workflows):

1. **กระบวนการสำหรับสัตวแพทย์และเจ้าหน้าที่ (Doctor Flow):** `Dashboard/Calendar` -> `Recent Requests` -> `Pet Profile Search` -> `Add Medical Record / Vaccine Update`
2. **กระบวนการสำหรับเจ้าของสัตว์เลี้ยง (Pet Owner Flow):** `My Pets Management` -> `Search Doctor` -> `Send Appointment Request` -> `View Medical & Vaccination History`

---

## Database Design (ER Diagram)

โครงสร้างฐานข้อมูลเชิงสัมพันธ์ (Relational Database) ประกอบด้วย 6 ตารางหลัก รองรับความสัมพันธ์ประเภท **One-to-One** และ **One-to-Many** ดังนี้:

1. **`PetOwner`:** จัดเก็บข้อมูลหลักของบัญชีผู้ใช้งานฝั่งเจ้าของสัตว์เลี้ยง
2. **`PetOwnerDetail`:** *(One-to-One กับ PetOwner)* จัดเก็บข้อมูลเชิงลึก ได้แก่ ที่อยู่ และเบอร์โทรศัพท์ติดต่อฉุกเฉิน
3. **`Pet`:** *(One-to-Many จาก PetOwner)* จัดเก็บข้อมูลประวัติสัตว์เลี้ยง (สายพันธุ์, น้ำหนัก, วันเกิด, หมายเลขไมโครชิป)
4. **`Doctor`:** จัดเก็บข้อมูลสัตวแพทย์ รายละเอียดความเชี่ยวชาญ และตารางเวลาการปฏิบัติงาน (ตารางเวร)
5. **`Appointment`:** *(One-to-Many จาก Pet และ Doctor)* จัดเก็บข้อมูลการนัดหมาย วันเวลา รายละเอียดอาการเบื้องต้น และประเภทบริการ
6. **`MedicalRecord`:** *(One-to-Many จาก Appointment)* จัดเก็บประวัติผลการตรวจรักษา รายการยา และการฉีดวัคซีนจริง

*(หมายเหตุ: เอกสารแผนผัง ER Diagram และ Data Dictionary ฉบับสมบูรณ์จัดเก็บอยู่ในโฟลเดอร์ `doc/diagrams/` และ `doc/data-dictionary.md`)*

---

## Design Patterns Applied

ประยุกต์ใช้ **Creational Design Patterns** เพื่อแก้ปัญหาในการออกแบบเชิงวัตถุให้สอดคล้องกับข้อกำหนดทางเทคนิค:

| Pattern | Group | วัตถุประสงค์และการประยุกต์ใช้งานในระบบ |
| --- | --- | --- |
| **Factory Method** | Creational | แยกวัตถุการนัดหมายตามประเภทบริการ เช่น `VaccineAppointment` (สำหรับการตรวจนัดฉีดวัคซีนตามระยะ) และ `SurgeryAppointment` (สำหรับการนัดหมายผ่าตัดที่ต้องมีเงื่อนไขเตรียมตัวพิเศษ) |
| **Builder Pattern** | Creational | ใช้ในการประกอบวัตถุ DTO ที่มีความซับซ้อน ได้แก่ `MedicalSummaryReportDTO` ซึ่งรวบรวมข้อมูลจากหลาย Entity เพื่อส่งออกข้อมูลผ่าน REST API |
| **Singleton Pattern** | Creational | บริหารจัดการ Instance ของการตั้งค่าระบบ (`SystemConfigRegistry`) และนโยบายอัตราค่าบริการ (`ClinicPricePolicy`) ให้มีเพียง Instance เดียวตลอดวงจรชีวิตของแอปพลิเคชันผ่าน Spring Bean |

---

## Installation & Setup

### เงื่อนไขเบื้องต้น (Prerequisites)

* Java 17 JDK หรือเวอร์ชันที่สูงกว่า
* Docker และ Docker Compose
* Git Version Control

### ขั้นตอนที่ 1: การ Clone Repository
```bash
git clone [https://github.com/pitchayasitthipan/vet-appointment.git](https://github.com/pitchayasitthipan/vet-appointment.git)
cd vet-appointment

```

### ขั้นตอนที่ 2: การกำหนดค่าฐานข้อมูล (Database Configuration)

ปรับแต่งค่าการเชื่อมต่อฐานข้อมูลในไฟล์ `code/src/main/resources/application.yml` หรือผ่าน Environment Variables:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vetcare_db
    username: postgres
    password: postgrespassword

```

---

## How to Run

### วิธีที่ 1: การรันแอปพลิเคชันผ่าน Gradle Wrapper

```bash
# เริ่มต้นการทำงานของฐานข้อมูลผ่าน Docker Compose
docker-compose up -d

# คอมไพล์และสั่งรันระบบด้วย Gradle
./gradlew bootRun

```

### วิธีที่ 2: การรันแอปพลิเคชันผ่าน Docker Container

```bash
docker-compose up --build

```

---

## API Documentation

เมื่อระบบเริ่มต้นการทำงานเรียบร้อยแล้ว สามารถเข้าถึงเอกสารและทดสอบการทำงานของ RESTful API ผ่าน Swagger UI ได้ที่:

* **Swagger UI URL:** `http://localhost:8080/swagger-ui.html`

---

## How to Run Tests

Unit Testin และ Integration Testing ดำเนินการผ่าน JUnit 5 และ Mockito ด้วยคำสั่ง:

```bash
./gradlew test

```

*รายงานผลการทดสอบ (Test Report) จะถูกสร้างขึ้น ณ ตำแหน่ง `build/reports/tests/test/index.html*`

---

## Deployment URL

* **Production App URL:** https://vetcare-clinic.onrender.com
* **Database Server:** Cloud PostgreSQL (Supabase / Aiven / Railway)

---

## Project Structure

```text
.
├── code/                   # Source code และไฟล์การกำหนดค่าระบบทั้งหมด
│   ├── src/main/java/com/example/vetcare/
│   │   ├── config/            # System Configuration & Security Beans
│   │   ├── controller/        # RestControllers (@RestControllerAdvice) & Web Controllers
│   │   ├── service/           # Business Logic Layer (Interfaces & Implementations)
│   │   ├── repository/        # Spring Data JPA Repositories
│   │   ├── domain/            # Entities, Enums, Value Objects
│   │   ├── dto/               # Request/Response DTOs & Mappers
│   │   └── exception/         # Global Exception Handlers & Custom Exceptions
│   └── src/main/resources/    # Configuration files & Database Migration Scripts
├── test/                   # การทดสอบทั้งหมด (JUnit 5, Mockito, Spring Boot Test)
├── doc/                    # เอกสารวิเคราะห์สถาปัตยกรรมระบบและสไลด์นำเสนอ
│   ├── diagrams/              # Use Case, ERD, Class, Sequence, State Diagrams
│   ├── solid-analysis.md      # เอกสารวิเคราะห์ SOLID Principles
│   ├── design-patterns.md    # เอกสารวิเคราะห์ Design Patterns
│   └── slide/                 # สไลด์นำเสนอโปรเจกต์
└── img/                    # ไฟล์สื่อและภาพประกอบระบบ

```
