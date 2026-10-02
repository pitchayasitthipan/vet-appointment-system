
---

### 1. Entity Fields: `PetOwner` (ตาราง `pet_owner`)

| Field | Data Type (Java / DB) | Constraints / Format | คำอธิบาย |
| --- | --- | --- | --- |
| `ownerId` | `Long` / `BIGINT` | Primary Key (`IDENTITY`) | รหัสประจำตัวเจ้าของสัตว์เลี้ยง |
| `firstName` | `String` / `VARCHAR(50)` | Max 50 | ชื่อจริง |
| `lastName` | `String` / `VARCHAR(50)` | Max 50 | นามสกุล |
| `email` | `String` / `VARCHAR(100)` | Unique, Max 100, Email Format | อีเมล |
| `phone` | `String` / `VARCHAR(20)` | Max 20, Thai Phone Regex | เบอร์โทรศัพท์ |
| `createdAt` | `LocalDateTime` / `TIMESTAMP` | Auto Generate | วันเวลาที่สร้างข้อมูล ||

---

### 2. Entity Fields: `PetOwnerDetail` (ตาราง `pet_owner_detail`)

| Field | Data Type (Java / DB) | Constraints / Format | คำอธิบาย |
| --- | --- | --- | --- |
| `ownerDetailId` | `Long` / `BIGINT` | Primary Key (`IDENTITY`) | รหัสรายละเอียดเจ้าของ |
| `address` | `String` / `TEXT` | `TEXT` | ที่อยู่ |
| `emergencyContactName` | `String` / `VARCHAR(50)` | Max 50 | ชื่อผู้ติดต่อฉุกเฉิน |
| `emergencyContactPhone` | `String` / `VARCHAR(20)` | Max 20, Thai Phone Regex | เบอร์โทรศัพท์ฉุกเฉิน |
| `petOwner` | `PetOwner` / `BIGINT` | Foreign Key (`owner_id`), Unique | เชื่อมกลับไปยัง `PetOwner` |

---