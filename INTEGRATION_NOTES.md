# PawCare — รวมงานจากเชอและเปิดทางเพิ่มสัตว์เลี้ยง

## ไฟล์ที่จัดให้
- `img/` ภาพหน้าจอจากเชอ 84 ภาพ (desktop 42 / mobile 42) และ `img/README.md`
- `code/src/main/java/com/example/petclinic/controller/api/AppointmentGuestController.java` ปรับค่าเริ่มต้น `appointments.pet-registration-enabled` เป็น `true` เพื่อแสดงลิงก์เพิ่มสัตว์เลี้ยงจากหน้าจองนัดเมื่อแฟ้มไม่มีสัตว์เลี้ยง

## ข้อควรทราบ
- ไฟล์ภาพเป็นหลักฐาน UI ไม่ใช่ไฟล์ HTML/CSS/JS ใหม่จากเชอ
- ถ้ามีการตั้ง `appointments.pet-registration-enabled=false` ใน application.properties ค่าใน config จะยัง override ค่าเริ่มต้นนี้ ต้องปรับค่าในเครื่องด้วย
- ยังไม่ได้รัน Maven/Browser กับชุดแก้ไขนี้ในเครื่องผู้ใช้
- ห้ามทับไฟล์ที่เพื่อนแก้หลังจาก ZIP ล่าสุดโดยไม่ตรวจ diff

## ขั้นตอนบน Mac จาก repo root
1. แตก ZIP นี้ที่ root ของ repository โดยรักษาโครงสร้างโฟลเดอร์ `code/` และ `img/`
2. ตรวจ `git diff --check` และ `git status --short`
3. ตรวจ `grep -n 'appointments.pet-registration-enabled' code/src/main/java/com/example/petclinic/controller/api/AppointmentGuestController.java code/src/main/resources/application.properties` (หากมี)
4. `cd code && ./mvnw test` หรือ `mvn test` แล้วกลับ root
5. ทดสอบ Browser: ค้นหาเบอร์เจ้าของที่ยังไม่มีสัตว์ -> ลิงก์เพิ่มสัตว์ -> บันทึก -> กลับหน้าจอง
6. ค่อย stage เฉพาะ `img/` และไฟล์ Java ที่แก้ รวมกับงานอื่นเมื่อพร้อม

## งานที่ยังต้องยืนยันก่อนส่งตามใบงาน
- doc/solid-analysis.md ระบุไฟล์และเลขบรรทัด
- doc/design-patterns.md อธิบาย GoF 3 แบบในกลุ่มเดียวกันพร้อม Class Diagram
- doc/diagrams ครบทุกชนิด และ Data Dictionary + schema/migration
- test report, Dockerfile, docker-compose.yml, Deploy URL สาธารณะ, README สมาชิกครบ
- PR มี reviewer, การ commit ของแต่ละคนเป็นของตัวเอง
