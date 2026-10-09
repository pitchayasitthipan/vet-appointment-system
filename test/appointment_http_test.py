"""Run only against a disposable clinic database: creates synthetic fixture data.
Usage: python test/appointment_http_test.py http://localhost:8080
"""
import concurrent.futures
import datetime as dt
import json
import http.cookiejar
import sys
import threading
import urllib.error
import urllib.parse
import urllib.request
import uuid

base = sys.argv[1] if len(sys.argv) > 1 else 'http://localhost:8080'

class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None

client = urllib.request.build_opener(NoRedirect(), urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))

def request(method, path, payload=None, form=False):
    data = None
    headers = {}
    if payload is not None:
        data = (urllib.parse.urlencode(payload) if form else json.dumps(payload)).encode('utf-8')
        headers['Content-Type'] = 'application/x-www-form-urlencoded' if form else 'application/json'
    req = urllib.request.Request(base + path, data=data, headers=headers, method=method)
    try:
        result = client.open(req, timeout=20)
    except urllib.error.HTTPError as result_error:
        result = result_error
    raw = result.read().decode('utf-8')
    try:
        body = json.loads(raw)
    except ValueError:
        body = raw
    return result.code, body, result.headers

def expect(method, path, status, payload=None, form=False):
    actual, data, headers = request(method, path, payload, form)
    assert actual == status, (method, path, actual, data)
    return data, headers

def local_path(url):
    parsed=urllib.parse.urlsplit(url)
    return parsed.path + ('?' + parsed.query if parsed.query else '')

run_id = uuid.uuid4().hex[:10]
phone = '09' + str(uuid.uuid4().int % 100000000).zfill(8)
web_owner = dict(firstName='Appointment', lastName='HTTP test', email=f'{run_id}@example.test',
    phone=phone, address='Test fixture', emergencyContactName='Test', emergencyContactPhone='0899999999', returnTo='appointment')

for path in ('/', '/appointment-create.html', '/appointments.html', '/js/appointments.js', '/css/appointments.css'):
    expect('GET', path, 200)
expect('POST', '/api/appointment-guests/lookup', 404, {'phone':phone})
config, _ = expect('GET', '/api/appointment-guests/config', 200)
assert config['ownerRegistrationPath'] == '/owners/new'
html, _ = expect('GET', '/owners/new?phone=' + phone + '&returnTo=appointment', 200)
assert 'name="returnTo"' in html and 'value="appointment"' in html
_, headers = expect('POST', '/owners', 302, web_owner, form=True)
assert local_path(headers['Location']).startswith('/pets/new?ownerId=') and 'returnTo=appointment' in headers['Location'], headers['Location']
_, pet_headers = expect('GET', local_path(headers['Location']), 302)
assert local_path(pet_headers['Location']).startswith('/pets.html?')
found, _ = expect('POST', '/api/appointment-guests/lookup', 200, {'phone':phone[:3]+'-'+phone[3:]})
owner_id = found['ownerId']
assert 'email' not in found and 'phone' not in found
pet1, _ = expect('POST', '/api/pets', 201, {'name':'HTTP Cat', 'species':'cat', 'ownerId':owner_id})
pet2, _ = expect('POST', '/api/pets', 201, {'name':'HTTP Dog', 'species':'dog', 'ownerId':owner_id})
pets, _ = expect('GET', f'/api/appointment-guests/{owner_id}/pets', 200)
assert {p['petId'] for p in pets} == {pet1['petId'],pet2['petId']}
doctor_data = dict(firstName='Appointment',lastName='Test',specialization='Test',phone='0823456789',
    email=f'doctor-{run_id}@example.test',workSchedule='ทุกวัน: 09:00 - 17:00')
doctor1, _ = expect('POST', '/api/doctors', 201, doctor_data)
doctor2, _ = expect('POST', '/api/doctors', 201, {**doctor_data,'email':f'doctor2-{run_id}@example.test'})
today = dt.datetime.now(dt.timezone(dt.timedelta(hours=7))).date()
day = today + dt.timedelta(days=(7-today.weekday()) % 7 or 7)
time = day.isoformat()+'T09:00:00'
payload = dict(ownerId=owner_id,petId=pet1['petId'],doctorId=doctor1['doctorId'],appointmentDateTime=time,
    serviceType='VACCINE',symptoms='HTTP test')
a, headers = expect('POST','/api/appointments',201,payload)
assert a['version']==0 and 'Location' in headers
expect('POST','/api/appointments',409,payload)
expect('POST','/api/appointments',404,{**payload,'ownerId':owner_id+100000})
expect('POST','/api/appointments',400,{**payload,'appointmentDateTime':day.isoformat()+'T09:15:00'})
expect('POST','/api/appointments',400,{**payload,'appointmentDateTime':'2000-01-01T09:00:00'})
expect('GET',f"/api/appointments/{a['appointmentId']}?ownerId={owner_id+100000}",404)
page, _ = expect('GET',f'/api/appointments?ownerId={owner_id}&status=PENDING&size=1',200)
assert page['totalElements']==1 and len(page['content'])==1
url = f"/api/appointments/{a['appointmentId']}?ownerId={owner_id}"
update = dict(doctorId=doctor1['doctorId'],appointmentDateTime=day.isoformat()+'T09:30:00',
    serviceType='CONSULTATION',symptoms='Updated',version=a['version'])
updated, _ = expect('PUT',url,200,update)
assert updated['version']==1
expect('PUT',url,409,update)
expect('PATCH',f"/api/appointments/{a['appointmentId']}/cancel?ownerId={owner_id}",200)
expect('PATCH',f"/api/appointments/{a['appointmentId']}/cancel?ownerId={owner_id}",200)
slots,_=expect('GET',f"/api/appointments/availability?doctorId={doctor1['doctorId']}&date={day}",200)
assert update['appointmentDateTime'] in slots

def race(left, right):
    barrier=threading.Barrier(2)
    def book(data):
        barrier.wait(timeout=5)
        return request('POST','/api/appointments',data)[0]
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
        results=list(pool.map(book,[left,right]))
    assert sorted(results)==[201,409], results
    return results

same_doctor = race(payload,{**payload,'petId':pet2['petId']})
same_pet = race({**payload,'appointmentDateTime':day.isoformat()+'T10:00:00'},
    {**payload,'appointmentDateTime':day.isoformat()+'T10:00:00','doctorId':doctor2['doctorId']})
print(json.dumps({'result':'PASS','database':'PostgreSQL','registration_to_pet':'PASS','CRUD_validation_pagination':'PASS',
    'same_doctor_race':same_doctor,'same_pet_race':same_pet,'fixture_phone':phone,'ownerId':owner_id},ensure_ascii=False))
