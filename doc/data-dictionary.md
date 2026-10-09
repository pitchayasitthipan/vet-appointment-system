# Data Dictionary: โมดูลเจ้าของสัตว์เลี้ยง (PetOwner)

---

## ภาพรวมตาราง

| ตาราง | Entity | คำอธิบาย | ความสัมพันธ์ |
| --- | --- | --- | --- |
| `pet_owner` | `PetOwner` | ข้อมูลหลักของเจ้าของสัตว์เลี้ยง (ชื่อ, ติดต่อ) | 1-1 กับ `pet_owner_detail`, 1-N กับ `pet` (ตารางของโมดูล Pet) |
| `pet_owner_detail` | `PetOwnerDetail` | ข้อมูลเพิ่มเติม (ที่อยู่, ผู้ติดต่อฉุกเฉิน) | 1-1 กับ `pet_owner` |

---

### 1. Entity Fields: `PetOwner` (ตาราง `pet_owner`)

| Field | Column | Data Type (Java / DB) | Key | Null | Unique | Constraints / Format | คำอธิบาย | ตัวอย่าง |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `ownerId` | `owner_id` | `Long` / `BIGINT` | PK | ไม่ได้ | Unique | Auto Increment (`IDENTITY`) | รหัสประจำตัวเจ้าของสัตว์เลี้ยง | `1` |
| `firstName` | `first_name` | `String` / `VARCHAR(50)` | | ไม่ได้ | | Max 50, ห้ามว่าง | ชื่อจริง | `John` |
| `lastName` | `last_name` | `String` / `VARCHAR(50)` | | ไม่ได้ | | Max 50, ห้ามว่าง | นามสกุล | `Doe` |
| `email` | `email` | `String` / `VARCHAR(100)` | | ไม่ได้ | Unique | Max 100, Email Format, Case-sensitive | อีเมล (ใช้ระบุตัวตน ห้ามซ้ำ) | `test@example.com` |
| `phone` | `phone` | `String` / `VARCHAR(20)` | | ไม่ได้ | | Thai Phone Regex `^0[0-9]{8,9}$` | เบอร์โทรศัพท์ | `0876543210` |
| `createdAt` | `created_at` | `LocalDateTime` / `TIMESTAMP` | | ได้ | | Auto Generate (`@PrePersist`), แก้ไขไม่ได้ (`updatable = false`) | วันเวลาที่สร้างข้อมูล | `2026-10-07 16:14:41` |
| `petOwnerDetail` | ไม่มีคอลัมน์ | `PetOwnerDetail` | | | | `@OneToOne(mappedBy = "petOwner", cascade = ALL, fetch = LAZY)` | ฝั่งที่ไม่ได้ถือ FK ของความสัมพันธ์ 1-1 | |

---

### 2. Entity Fields: `PetOwnerDetail` (ตาราง `pet_owner_detail`)

| Field | Column | Data Type (Java / DB) | Key | Null | Unique | Constraints / Format | คำอธิบาย | ตัวอย่าง |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `ownerDetailId` | `owner_detail_id` | `Long` / `BIGINT` | PK | ไม่ได้ | Unique | Auto Increment (`IDENTITY`) | รหัสรายละเอียดเจ้าของ | `1` |
| `address` | `address` | `String` / `TEXT` | | ได้ | | ไม่จำกัดความยาว | ที่อยู่ | `Khon Kaen` |
| `emergencyContactName` | `emergency_contact_name` | `String` / `VARCHAR(50)` | | ได้ | | Max 50 | ชื่อผู้ติดต่อฉุกเฉิน | `familyMember` |
| `emergencyContactPhone` | `emergency_contact_phone` | `String` / `VARCHAR(20)` | | ได้ (DB) / บังคับกรอก (API) | | Thai Phone Regex `^0[0-9]{8,9}$` | เบอร์โทรศัพท์ฉุกเฉิน | `0987654321` |
| `petOwner` | `owner_id` | `PetOwner` / `BIGINT` | FK → `pet_owner.owner_id` | ไม่ได้ | Unique | `@OneToOne(fetch = LAZY)`, `@JoinColumn(nullable = false, unique = true)` | เชื่อมกลับไปยัง `PetOwner` | `1` |

---

### 3. ความสัมพันธ์ (Relationships)

<!-- รอเขียนเพิ่มเติมภายหลัง -->

---

### 4. Index และ Constraint

| ชื่อ | ตาราง | คอลัมน์ | ประเภท | เหตุผล |
| --- | --- | --- | --- | --- |
| Primary Key | `pet_owner` | `owner_id` | PK (มี index อัตโนมัติ) | ค้นหาตาม Id (`findById`) |
| Unique `email` | `pet_owner` | `email` | Unique (มี index อัตโนมัติ) | ห้ามอีเมลซ้ำ และ `existsByEmail` / `findByEmail` ค้นได้เร็ว |
| Primary Key | `pet_owner_detail` | `owner_detail_id` | PK (มี index อัตโนมัติ) | ระบุแถวรายละเอียดแต่ละแถว |
| Unique `owner_id` | `pet_owner_detail` | `owner_id` | Unique + FK (มี index อัตโนมัติ) | บังคับ 1-1 และใช้ join กับ `pet_owner` ได้เร็ว |

---

### 5. กฎการตรวจสอบข้อมูล (Validation ใน `PetOwnerRequestDTO`)

| Field | Annotation | ข้อความเมื่อไม่ผ่าน |
| --- | --- | --- |
| `firstName` | `@NotBlank`, `@Size(max = 50)` | กรุณากรอกชื่อจริงของคุณ / ชื่อจริงต้องมีความยาวไม่เกิน 50 ตัวอักษร |
| `lastName` | `@NotBlank`, `@Size(max = 50)` | กรุณากรอกนามสกุล / นามสกุลต้องมีความยาวไม่เกิน 50 ตัวอักษร |
| `email` | `@NotBlank`, `@Email`, `@Size(max = 100)` | กรุณากรอกอีเมล / รูปแบบอีเมลไม่ถูกต้อง / อีเมลต้องมีความยาวไม่เกิน 100 ตัวอักษร |
| `phone` | `@NotBlank`, `@Pattern(^0[0-9]{8,9}$)` | กรุณากรอกเบอร์โทรศัพท์ / เบอร์โทรศัพท์ที่กรอกต้องไม่เกิน 10 หลัก (เช่น 0812345678) |
| `address` | ไม่บังคับกรอก | |
| `emergencyContactName` | `@Size(max = 50)` (ไม่บังคับกรอก) | ชื่อผู้ติดต่อฉุกเฉินต้องมีความยาวไม่เกิน 50 ตัวอักษร |
| `emergencyContactPhone` | `@NotBlank`, `@Pattern(^0[0-9]{8,9}$)` | กรุณากรอกเบอร์ติดต่อฉุกเฉิน / เบอร์โทรศัพท์ที่กรอกต้องไม่เกิน 10 หลัก (เช่น 0812345678) |

**Business Logic ใน Service (`PetOwnerServiceImpl`)**

| กฎ | เมื่อไม่ผ่าน | HTTP Status |
| --- | --- | --- |
| อีเมลห้ามซ้ำ (ตรวจแบบ Case-sensitive) ทั้งตอนเพิ่มและตอนแก้ไขเป็นอีเมลใหม่ | `DuplicateResourceException` | 409 Conflict |
| ค้นหา / แก้ไข / ลบ ต้องมี Id อยู่จริง | `ResourceNotFoundException` | 404 Not Found |
| ข้อมูลไม่ผ่าน Validation ข้างบน | `MethodArgumentNotValidException` | 400 Bad Request |

---

### 6. ตัวอย่างข้อมูล

**`pet_owner`**

| owner_id | first_name | last_name | email | phone | created_at |
| --- | --- | --- | --- | --- | --- |
| 1 | John | Doe | test@example.com | 0876543210 | 2026-10-07 16:14:41 |

**`pet_owner_detail`**

| owner_detail_id | address | emergency_contact_name | emergency_contact_phone | owner_id |
| --- | --- | --- | --- | --- |
| 1 | Khon Kaen | familyMember | 0987654321 | 1 |
