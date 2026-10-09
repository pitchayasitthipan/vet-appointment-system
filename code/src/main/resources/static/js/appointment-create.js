"use strict";

(() => {
  const P = window.PawAppointments;
  const byId = id => document.getElementById(id);
  const form = byId("appointment-form");
  const phone = byId("owner-phone");
  const doctor = byId("doctor-id");
  const date = byId("appointment-date");
  const time = byId("appointment-time");
  const pet = byId("pet-id");
  const submit = byId("submit-appointment");
  const feedback = byId("booking-message");
  let owner = null, saving = false, complete = false, lookupVersion = 0, slotVersion = 0, doctorsReady = false;
  const selectedDoctor = new URLSearchParams(location.search).get("doctorId");
  function ready() { submit.disabled = !owner || !pet.value || !time.value || !doctorsReady || saving || complete; }
  function resetOwner() {
    lookupVersion++; slotVersion++; owner = null; complete = false; P.clearOwner();
    byId("pet-fields").disabled = true; byId("time-fields").disabled = true;
    P.options(pet, [], "ค้นหาเจ้าของก่อนเลือกสัตว์เลี้ยง");
    P.options(time, [], "เลือกหมอและวันที่ก่อน");
    byId("owner-result").hidden = true;
    byId("registration-link-container").hidden = true;
    byId("booking-result-links").hidden = true;
    P.message(feedback, ""); ready();
  }
  async function loadSlots() {
    const version = ++slotVersion;
    P.options(time, [], "กำลังตรวจคิวว่าง…"); ready();
    if (!owner || !doctor.value || !date.value) { P.options(time, [], "เลือกหมอและวันที่ก่อน"); return; }
    try {
      const slots = await P.request(`/api/appointments/availability?${P.query({doctorId:doctor.value, date:date.value})}`);
      if (version !== slotVersion) return;
      P.options(time, slots.map(slot => [slot, slot.slice(11, 16) + " น."]), slots.length ? "เลือกเวลา" : "ไม่มีคิวว่างในวันนี้");
      P.message(feedback, slots.length ? "" : "ไม่มีคิวที่รองรับในวันนี้ กรุณาเลือกวันที่หรือสัตวแพทย์อื่น");
    } catch (e) { if (version === slotVersion) { P.options(time, [], "ตรวจคิวไม่สำเร็จ"); P.message(feedback, e.message, "error"); } }
    ready();
  }
  byId("owner-lookup-form").addEventListener("submit", async event => {
    event.preventDefault(); resetOwner();
    const version = lookupVersion;
    const lookupButton = event.currentTarget.querySelector("button");
    lookupButton.disabled = true;
    P.message(byId("owner-result"), "กำลังค้นหาข้อมูล…");
    try {
      const found = await P.lookup(phone.value.trim());
      const pets = await P.request(`/api/appointment-guests/${found.ownerId}/pets`);
      if (version !== lookupVersion) return;
      owner = found; P.setOwner(found);
      P.message(byId("owner-result"), `พบแฟ้ม ${found.firstName} ${found.lastName}`, "success");
      P.options(pet, pets.map(p => [p.petId, p.petName]), pets.length ? "เลือกสัตว์เลี้ยง" : "ยังไม่มีสัตว์เลี้ยงในแฟ้ม");
      byId("pet-fields").disabled = !pets.length;
      byId("time-fields").disabled = !pets.length || !doctorsReady;
      if (!pets.length) {
        P.message(feedback, "กรุณาเพิ่มสัตว์เลี้ยงก่อนจองนัดหมาย");
        const config = await P.request("/api/appointment-guests/config");
        if (version !== lookupVersion) return;
        if (config.petRegistrationEnabled === "true") {
          const link = document.createElement("a"); link.href = `/pets/new?${P.query({ownerId:found.ownerId, returnTo:"appointment"})}`;
          link.textContent = "เพิ่มสัตว์เลี้ยงแล้วกลับมาจอง";
          byId("registration-link-container").replaceChildren(link); byId("registration-link-container").hidden = false;
        } else P.message(feedback, "ยังไม่มีสัตว์เลี้ยงในแฟ้ม ขณะนี้ยังรอเชื่อมหน้าเพิ่มสัตว์เลี้ยง กรุณาติดต่อคลินิก");
      } else await loadSlots();
    } catch (e) {
      if (version !== lookupVersion) return;
      if (e.status === 404) {
        try { const path = await P.registration(phone.value.trim()); if (version === lookupVersion) location.assign(path); }
        catch (configError) { P.message(byId("owner-result"), configError.message, "error"); }
      } else P.message(byId("owner-result"), e.message, "error");
    } finally { lookupButton.disabled = false; ready(); }
  });
  phone.addEventListener("input", resetOwner);
  doctor.addEventListener("change", loadSlots); date.addEventListener("change", loadSlots);
  pet.addEventListener("change", ready); time.addEventListener("change", ready);
  form.addEventListener("submit", async event => {
    event.preventDefault();
    if (saving || complete || !owner || !form.reportValidity() || !time.value) return;
    const bookedOwner = owner;
    saving = true; ready(); byId("owner-lookup-form").querySelector("fieldset").disabled = true;
    try {
      const serviceType = form.querySelector('input[name="serviceType"]:checked').value;
      const result = await P.request("/api/appointments", { method: "POST", body: JSON.stringify({ownerId:bookedOwner.ownerId,
        petId:Number(pet.value), doctorId:Number(doctor.value), appointmentDateTime:time.value,
        serviceType, symptoms:byId("symptoms").value.trim()}) });
      complete = true; byId("pet-fields").disabled = true; byId("time-fields").disabled = true;
      P.message(feedback, `บันทึกนัด #${result.appointmentId} แล้ว (${P.statuses[result.status]}) · ${P.formatDate(result.appointmentDateTime)} · ${result.preparationInstructions}`, "success");
      byId("booking-result-links").hidden = false; feedback.focus();
    } catch (e) {
      if (e.status === 409) await loadSlots();
      P.message(feedback, e.message, "error");
    } finally { saving = false; byId("owner-lookup-form").querySelector("fieldset").disabled = false; ready(); }
  });
  date.min = P.bangkokToday();
  const cached = P.owner();
  if (cached) { P.message(byId("booking-notice"), "กรอกเบอร์โทรของคุณเพื่อเลือกแฟ้มและเริ่มจองนัดหมาย"); }
  P.request("/api/doctors").then(doctors => {
    P.options(doctor, doctors.map(d => [d.doctorId, `${d.firstName} ${d.lastName} · ${d.specialization || "สัตวแพทย์"}`]), doctors.length ? "เลือกสัตวแพทย์" : "ยังไม่มีสัตวแพทย์ในระบบ");
    doctorsReady = doctors.length > 0;
    if (selectedDoctor && Array.from(doctor.options).some(o => o.value === selectedDoctor)) doctor.value = selectedDoctor;
    if (owner) { byId("time-fields").disabled = !doctorsReady || pet.options.length <= 1; loadSlots(); }
    if (!doctorsReady) P.message(feedback, "ยังไม่มีสัตวแพทย์ที่รับนัด กรุณาติดต่อคลินิก", "error");
  }).catch(e => P.message(feedback, e.message + " · โหลดหน้านี้ใหม่เพื่อลองอีกครั้ง", "error"));
})();
