# Appointment verification

Backend + H2 integration/regression: `mvn -f code/pom.xml test`.

Frontend DOM tests (Node.js + pnpm):
```powershell
pnpm --dir code/frontend-tests install --frozen-lockfile
pnpm --dir code/frontend-tests test
```
These execute the actual page scripts in jsdom with controlled API responses. They verify form state,
submitted owner/pet/doctor/time/version, errors, stale requests, filtering and cancellation confirmation.
They do not substitute for the live PostgreSQL and browser checks below.

HTTP + PostgreSQL checks:
```powershell
python test/appointment_http_test.py http://localhost:8080 TEST_OWNER_PHONE
```
Run ONLY against a disposable database. Seed a test owner with two pets and two doctors first.
Both doctors must work every day 09:00-17:00; use an owner with no active test bookings.
The script writes appointments only. It checks CRUD, status/error codes, owner scoping,
slot release and two concurrent races. Owner/Pet registration belongs to the team and is not tested here.

Browser manual check: phone lookup → select pet/service/doctor/date/free slot → book → view list → edit → cancel.
Registration destinations require the team's Owner/Pet modules to be merged into develop separately.

API ของ Appointment/Guest ใช้ /api/v1. Guest ต้อง POST lookup เพื่อให้ server เลือก myOwnerId ใน session ก่อน
HTTP script เก็บ session cookies และตรวจการแก้ ownerId ต้องได้ 403
เมื่อ session หมดอายุหรือเปลี่ยนแฟ้มในแท็บอื่นต้องค้นเบอร์ใหม่; sessionStorage เป็นเพียง cache สำหรับ UI
