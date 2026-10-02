## Pet Clinic Appointment & Vaccination System
### ระบบบริหารจัดการนัดหมายและประวัติการฉีดวัคซีนสำหรับคลินิกสัตว์เลี้ยง ### 
**รายละเอียด:** ระบบจัดการข้อมูลและการนัดหมายสำหรับคลินิกสัตว์เลี้ยงที่พัฒนาด้วย Java 17 และ Spring Boot ตามสถาปัตยกรรมแบบ Layered Architecture เพื่อช่วยให้เจ้าของสัตว์เลี้ยงสามารถจัดการข้อมูลสัตว์เลี้ยง นัดหมายกับสัตวแพทย์ และตรวจสอบประวัติการรักษาและการฉีดวัคซีนได้อย่างสะดวก ตลอดจนช่วยสัตวแพทย์ให้จัดการตารางนัดและบันทึกประวัติได้อย่างเป็นระบบ เพื่อลดความซ้ำซ้อนของข้อมูลและเพิ่มประสิทธิภาพในการบริการ

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

## วัตถุประสงค์ (Objectives)
1. เพื่อพัฒนาระบบจัดการข้อมูลเจ้าของสัตว์เลี้ยงและสัตว์เลี้ยง
2. เพื่ออำนวยความสะดวกในการนัดหมายระหว่างเจ้าของสัตว์เลี้ยงกับสัตวแพทย์
3. เพื่อให้สัตวแพทย์สามารถจัดการข้อมูลการนัดหมายและบันทึกประวัติการรักษาได้
4. เพื่อจัดเก็บประวัติการรักษาและการฉีดวัคซีนของสัตว์เลี้ยงอย่างเป็นระบบ
5. เพื่อประยุกต์ใช้ Design Patterns ในการออกแบบและพัฒนาซอฟต์แวร์

---

## ฟังก์ชันและโครงสร้างหน้าเว็บ (Core Screens & Features)
ระบบกำหนดโครงสร้างหน้าจอหลักไว้ 4 หน้า ได้แก่:

* **หน้าจอหลักของระบบ (4 Core Screens)**
หน้าหลัก / แดชบอร์ด (Home / Dashboard): หน้าต้อนรับ ค้นหาสัตวแพทย์ และแสดงข่าวสาร/บริการของคลินิก
* **หน้าจัดการข้อมูลสัตว์เลี้ยง (Pet Management Page):** หน้าสำหรับเพิ่ม แก้ไข และดูรายชื่อสัตว์เลี้ยงของเจ้าของ
* **หน้าระบบนัดหมาย (Appointment Page):** หน้าจองคิว เลือกสัตวแพทย์ เลือกวันเวลา และเลือกประเภทบริการ (ตรวจรักษา/ฉีดวัคซีน)
* **หน้าประวัติการรักษาและวัคซีน (Medical & Vaccination Record Page):** หน้าแสดงประวัติการรักษา บันทึกสัตวแพทย์ และตารางการรับวัคซีน

---

## ขอบเขตฟังก์ชันแบ่งตามสิทธิ์ผู้ใช้
**สำหรับเจ้าของสัตว์เลี้ยง (Pet Owner):**
* จัดการข้อมูลส่วนตัวและข้อมูลติดต่อ
* เพิ่มและจัดการข้อมูลสัตว์เลี้ยง
* ค้นหาและดูข้อมูลสัตวแพทย์
* ส่งคำขอนัดหมายและตรวจสอบสถานะการนัดหมาย
* ดูประวัติการรักษาและประวัติการฉีดวัคซีนของสัตว์เลี้ยง

**สำหรับสัตวแพทย์ (Doctor):**
* ดูรายการนัดหมายและจัดการตารางนัดหมาย
* ตรวจสอบ/อนุมัติคำขอนัดหมาย
* ค้นหาข้อมูลและประวัติสัตว์เลี้ยง
* บันทึกประวัติการรักษาและข้อมูลการฉีดวัคซีน

*หมายเหตุ: ฟังก์ชันและหน้าจอข้างต้นเป็นขอบเขตที่วางแผนไว้ และอยู่ระหว่างการพัฒนาอย่างเป็นขั้นตอน*

---

## Tech Stack

* **Programming Language:** Java 17
* **Backend Framework:** Spring Boot 3.x (Spring Data JPA, Spring Validation)
* **Build Tool:** Gradle
* **Database:** PostgreSQL / MySQL (ประมวลผลผ่าน Spring Data JPA)
* **ORM:** Spring Data JPA (Hibernate)
* **Frontend Framework:** Thymeleaf + HTML5 / CSS3 (Bootstrap 5)
* **API Documentation:** OpenAPI 3.0 / Swagger UI
* **Testing Framework:** JUnit 5, Mockito, Spring Boot Test
* **Containerization:** Docker และ Docker Compose
* **Development Environment:** Visual Studio Code (VS Code)

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

1. **กระบวนการสำหรับสัตวแพทย์และเจ้าหน้าที่ (Doctor Flow):** 
2. **กระบวนการสำหรับเจ้าของสัตว์เลี้ยง (Pet Owner Flow):** 

*(หมายเหตุ: มีการเพิ่มข้อมูลอีกครั้งในภายหลัง)*

---

## โครงสร้างฐานข้อมูล (Database Design)


โครงสร้างฐานข้อมูลเชิงสัมพันธ์ (Relational Database) ประกอบด้วย 6 ตารางหลัก รองรับความสัมพันธ์ประเภท **One-to-One** และ **One-to-Many** ดังนี้:

1. **`PetOwner`:** จัดเก็บข้อมูลหลักของบัญชีผู้ใช้งานฝั่งเจ้าของสัตว์เลี้ยง
2. **`PetOwnerDetail`:** *(One-to-One กับ PetOwner)* จัดเก็บข้อมูลเชิงลึก ได้แก่ ที่อยู่ และเบอร์โทรศัพท์ติดต่อฉุกเฉิน
3. **`Pet`:** *(One-to-Many จาก PetOwner)* จัดเก็บข้อมูลประวัติสัตว์เลี้ยง (สายพันธุ์, น้ำหนัก, วันเกิด, หมายเลขไมโครชิป)
4. **`Doctor`:** จัดเก็บข้อมูลสัตวแพทย์ รายละเอียดความเชี่ยวชาญ และตารางเวลาการปฏิบัติงาน (ตารางเวร)
5. **`Appointment`:** *(One-to-Many จาก Pet และ Doctor)* จัดเก็บข้อมูลการนัดหมาย วันเวลา รายละเอียดอาการเบื้องต้น และประเภทบริการ
6. **`MedicalRecord`:** *(One-to-Many จาก Appointment)* จัดเก็บประวัติผลการตรวจรักษา รายการยา และการฉีดวัคซีนจริง

*(หมายเหตุ: เอกสารแผนผัง ER Diagram และ Data Dictionary ฉบับสมบูรณ์จัดเก็บอยู่ในโฟลเดอร์ `doc/diagrams/` และ `doc/data-dictionary.md`)*

---

## การนำ Design Patterns มาใช้งาน (Design Patterns Applied)

เพื่อแก้ปัญหาในการออกแบบเชิงวัตถุให้สอดคล้องกับข้อกำหนดทางเทคนิค:

| Pattern | Group | วัตถุประสงค์และการประยุกต์ใช้งานในระบบ |
| --- | --- | --- |
| **Factory Method** | Creational | แยกวัตถุการนัดหมายตามประเภทบริการ เช่น `VaccineAppointment` (สำหรับการตรวจนัดฉีดวัคซีนตามระยะ) และ `SurgeryAppointment` (สำหรับการนัดหมายผ่าตัดที่ต้องมีเงื่อนไขเตรียมตัวพิเศษ) |
| **Builder Pattern** | Creational | ใช้ในการประกอบวัตถุ DTO ที่มีความซับซ้อน ได้แก่ `MedicalSummaryReportDTO` ซึ่งรวบรวมข้อมูลจากหลาย Entity เพื่อส่งออกข้อมูลผ่าน REST API |
| **Singleton Pattern** | Creational | บริหารจัดการ Instance ของการตั้งค่าระบบ (`SystemConfigRegistry`) และนโยบายอัตราค่าบริการ (`ClinicPricePolicy`) ให้มีเพียง Instance เดียวตลอดวงจรชีวิตของแอปพลิเคชันผ่าน Spring Bean |

---

## การติดตั้งและเริ่มต้นใช้งาน (Installation & Setup)

**เงื่อนไขเบื้องต้น (Prerequisites)**

**สิ่งที่ต้องติดตั้ง**
* Java Development Kit (JDK) 17 หรือเวอร์ชันที่โครงการรองรับ
* Visual Studio Code (VS Code)
* Gradle หรือใช้ Gradle Wrapper ที่อยู่ในโครงการ
* PostgreSQL หรือ MySQL ตามฐานข้อมูลที่โครงการเลือกใช้
* Git


### ขั้นตอนที่ 1: การ Clone Repository
```bash
1. Clone Repository จาก GitHub
git clone [https://github.com/pitchayasitthipan/vet-appointment-system.git](https://github.com/pitchayasitthipan/vet-appointment-system.git)

2. เข้าสู่โฟลเดอร์โปรเจกต์
cd vet-appointment-system

```

### ขั้นตอนที่ 2: การกำหนดค่าฐานข้อมูล (Database Configuration)

* สร้างฐานข้อมูลตามชื่อและการตั้งค่าที่กำหนดไว้ในไฟล์ application.properties หรือ application.yml 
* กำหนดข้อมูลการเชื่อมต่อฐานข้อมูลให้ตรงกับสภาพแวดล้อมที่ใช้งาน โดยไม่ควรเผยแพร่รหัสผ่านหรือข้อมูลสำคัญลงใน Repository

```
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

*รายงานผลการทดสอบ (Test Report) จะถูกสร้างขึ้น ณ ตำแหน่ง `build/reports/tests/test/index.html*`*

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
*(หมายเหตุ: อาจะมีการแก้ไขในภายหลัง)*