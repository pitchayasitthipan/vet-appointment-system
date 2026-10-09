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
python test/appointment_http_test.py http://localhost:8080
```
Run ONLY against a disposable database. This script creates synthetic owners, pets, doctors and appointments.
It checks registration redirects, CRUD, status/error codes, owner scoping, slot release and two concurrent races.
Do not run it against the real clinic database. It intentionally leaves the fixtures for manual UI verification.

Browser manual check: phone lookup → select pet/service/doctor/date/free slot → book → view list → edit → cancel.
Also check unknown phone redirects to registration, and check mobile/desktop layouts.
