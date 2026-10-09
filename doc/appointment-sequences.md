# Sequence Diagrams — Appointment (อ้น)

## 1. ค้นหาเจ้าของและสร้างนัดหมาย
```mermaid
sequenceDiagram
    actor Guest
    participant UI as appointment-create.html
    participant GC as AppointmentGuestController
    participant GS as AppointmentGuestService
    participant AC as AppointmentController
    participant AS as AppointmentService
    participant FM as AppointmentFactoryRegistry
    participant DB as PostgreSQL
    Guest->>UI: กรอกเบอร์โทร
    UI->>GC: POST /api/appointment-guests/lookup
    GC->>GS: lookup(phone)
    GS->>DB: ค้นเบอร์ที่ normalize แล้ว
    alt ไม่พบแฟ้ม
        GS-->>UI: 404
        UI->>GC: GET /config
        UI-->>Guest: /owners/new?phone=...&returnTo=appointment
    else พบแฟ้มเดียว
        GS-->>UI: ownerId + ชื่อ
        UI->>GC: GET /{ownerId}/pets
        GS->>DB: สัตว์ของเจ้าของ
        GC-->>UI: petId + petName
        Guest->>UI: เลือกสัตว์ บริการ หมอ วันเวลา
        UI->>AC: GET /availability
        AC->>AS: availability(doctorId, date)
        AS->>DB: อ่านนัด PENDING/CONFIRMED ของหมอ
        AS-->>UI: ช่องเวลาในเวรที่ยังว่าง
        UI->>AC: POST /api/appointments
        AC->>AS: create(valid DTO)
        AS->>DB: begin transaction; lock Doctor แล้ว Pet
        AS->>AS: ตรวจเจ้าของ เวลาเปิดคลินิก เวร และอนาคต
        AS->>DB: countConflicts (หมอ OR สัตว์)
        alt คิวซ้ำ
            AS-->>UI: 409; ไม่บันทึก
        else คิวว่าง
            AS->>FM: create(serviceType)
            FM-->>AS: Appointment + คำแนะนำ + PENDING
            AS->>DB: saveAndFlush; commit
            AC-->>UI: 201 + Location + version
            UI-->>Guest: แสดงหมายเลขนัดที่บันทึกจริง
        end
    end
```

## 2. แก้ไขและเลื่อนนัด
```mermaid
sequenceDiagram
    actor Guest
    participant UI as appointments.html
    participant AC as AppointmentController
    participant AS as AppointmentService
    participant FM as AppointmentFactoryRegistry
    participant DB as PostgreSQL
    Guest->>UI: เปิดแก้ไขนัด
    UI->>AC: GET /{id}?ownerId=...
    AC->>AS: get(id, ownerId)
    AS->>DB: นัดของเจ้าของเท่านั้น
    DB-->>UI: ข้อมูลล่าสุด + version
    Guest->>UI: เปลี่ยนหมอ/เวลา/บริการ/รายละเอียด
    UI->>AC: PUT /{id}?ownerId=... (version)
    AC->>AS: update(...)
    AS->>DB: lock Appointment ของเจ้าของ
    AS->>AS: ตรวจสถานะ active และเวลานัดเดิมยังไม่ถึง
    alt version เก่า
        AS-->>UI: 409; ให้โหลดข้อมูลล่าสุด
    else version ตรง
        AS->>DB: lock Doctor แล้ว Pet
        AS->>AS: ตรวจเวรและเวลาใหม่
        AS->>DB: countConflicts ยกเว้นนัดเดิม
        AS->>FM: คำแนะนำตามบริการใหม่
        AS->>DB: update; version เพิ่ม; commit
        AC-->>UI: 200 + ข้อมูลล่าสุด
        UI-->>Guest: โหลดรายการใหม่และแจ้งบันทึกแล้ว
    end
```

## 3. ยกเลิกนัดและคืนคิว
```mermaid
sequenceDiagram
    actor Guest
    participant UI as appointments.html
    participant AC as AppointmentController
    participant AS as AppointmentService
    participant DB as PostgreSQL
    Guest->>UI: กดยกเลิก
    UI-->>Guest: แสดงนัดและวันเวลาให้ยืนยัน
    Guest->>UI: ยืนยันยกเลิกนัดนี้
    UI->>AC: PATCH /{id}/cancel?ownerId=...
    AC->>AS: cancel(id, ownerId)
    AS->>DB: lock Appointment ของเจ้าของ
    alt ยกเลิกแล้ว
        AS-->>UI: 200 + CANCELLED เดิม (idempotent)
    else PENDING/CONFIRMED และยังไม่ถึงเวลานัด
        AS->>DB: status = CANCELLED; flush; commit
        AC-->>UI: 200 + version ใหม่
        UI-->>Guest: สถานะยกเลิกแล้ว
        UI->>AC: GET /availability
        AS->>DB: อ่านเฉพาะ PENDING/CONFIRMED
        AS-->>UI: ช่องเวลาที่คืนแล้ว
    else เสร็จสิ้นหรือถึงเวลานัดแล้ว
        AS-->>UI: 409 หรือ 400; ไม่เปลี่ยนข้อมูล
    end
```

## ขอบเขตการเชื่อมทีม
เส้นทางลงทะเบียนใน diagram เป็น integration contract เมื่อเปิด flag หลัง Owner/Pet เข้า develop เท่านั้น
ค่าเริ่มต้น flags ปิด; UI แจ้งรอเชื่อมโมดูลทีม ไม่มี controller หรือหน้าลงทะเบียนของเพื่อนใน PR นี้
