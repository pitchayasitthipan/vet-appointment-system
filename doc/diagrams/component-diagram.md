# Component Diagram — PawCare

แสดงส่วนประกอบระดับซอฟต์แวร์ที่อ้างอิงโครงสร้าง Spring Boot ใน `code/src/main/java/com/example/petclinic`.

```mermaid
flowchart LR
    U[Browser: Owner / Staff] --> W[Web UI: HTML CSS JavaScript]
    W --> WC[Web Controllers]
    W --> API[REST API Controllers]
    WC --> S[Service Interfaces / Implementations]
    API --> S
    S --> P[Appointment Schedule Policy / Factory]
    S --> R[Spring Data JPA Repositories]
    R --> DB[(PostgreSQL)]
    API --> D[DTO / Validation]
    WC --> D
    S --> E[Domain Entities / Enums]
    R --> E
    API --> X[Exception Handlers]
```

ข้อควรอธิบาย: Controllers จัดการ HTTP, Services ดูแลกฎธุรกิจ, Repositories ติดต่อฐานข้อมูล, Entities แทนข้อมูลหลัก และ DTO ใช้รับส่งข้อมูล API.
