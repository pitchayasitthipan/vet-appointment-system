"use strict";

window.PawAppointments = (() => {
  const key = "pawcare.owner";
  const services = { CONSULTATION: "ตรวจทั่วไป", VACCINE: "ฉีดวัคซีน", SURGERY: "ผ่าตัด" };
  const statuses = { PENDING: "รอยืนยัน", CONFIRMED: "ยืนยันแล้ว", COMPLETED: "เสร็จสิ้น", CANCELLED: "ยกเลิกแล้ว" };
  class ApiError extends Error {
    constructor(message, status) { super(message); this.status = status; }
  }
  async function request(url, options = {}) {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), 15000);
    try {
      const res = await fetch(url, { ...options, credentials: "same-origin", signal: controller.signal,
        headers: { ...options.headers, ...(options.body ? { "Content-Type": "application/json" } : {}) } });
      const data = await res.json().catch(() => null);
      if (!res.ok) throw new ApiError(data?.errors ? Object.values(data.errors).join(" · ")
        : data?.message || "ไม่สามารถทำรายการได้ กรุณาลองใหม่", res.status);
      return data;
    } catch (e) {
      if (e instanceof ApiError) throw e;
      throw new ApiError("เชื่อมต่อคลินิกไม่สำเร็จ กรุณาตรวจการเชื่อมต่อแล้วลองใหม่", 0);
    } finally { clearTimeout(timer); }
  }
  const query = values => new URLSearchParams(Object.entries(values).filter(([,v]) => v !== "" && v != null)).toString();
  function setOwner(owner) { try { sessionStorage.setItem(key, JSON.stringify(owner)); } catch {} }
  function owner() {
    try { const value = JSON.parse(sessionStorage.getItem(key)); return Number.isSafeInteger(value?.ownerId) && value.ownerId > 0 ? value : null; }
    catch { return null; }
  }
  function clearOwner() { try { sessionStorage.removeItem(key); } catch {} }
  function message(element, text, kind = "info") {
    element.textContent = text; element.hidden = !text; element.className = `feedback ${kind}`;
    element.setAttribute("role", kind === "error" ? "alert" : "status");
  }
  function options(select, rows, placeholder) {
    select.replaceChildren(new Option(placeholder, ""));
    rows.forEach(([value, label]) => select.add(new Option(label, String(value))));
  }
  function bangkokToday() {
    const parts = new Intl.DateTimeFormat("en-CA-u-ca-gregory", { timeZone: "Asia/Bangkok", year: "numeric", month: "2-digit", day: "2-digit" }).formatToParts(new Date());
    const part = name => parts.find(p => p.type === name).value;
    return `${part("year")}-${part("month")}-${part("day")}`;
  }
  function formatDate(value) {
    return new Intl.DateTimeFormat("th-TH-u-ca-buddhist", { timeZone: "Asia/Bangkok", day: "numeric", month: "short",
      year: "numeric", era: "short", hour: "2-digit", minute: "2-digit" }).format(new Date(`${value}+07:00`));
  }
  function thaiDatePicker(input) {
    const months = ["มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน",
      "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"];
    const original = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, "value");
    const fields = document.createElement("div"); fields.className = "thai-date-picker";
    function field(tag, suffix, caption) {
      const label = document.createElement("label"); label.textContent = caption;
      const control = document.createElement(tag); control.id = `${input.id}-${suffix}`;
      control.required = input.required; label.htmlFor = control.id; label.append(control); fields.append(label);
      return control;
    }
    const day = field("select", "day", "วัน"), month = field("select", "month", "เดือน");
    const year = field("input", "year", "ปี พ.ศ.");
    year.type = "number"; year.min = "544"; year.max = "10542"; year.step = "1"; year.inputMode = "numeric";
    options(month, months.map((name, i) => [i + 1, name]), "เลือกเดือน");
    function sync() {
      const value = original.get.call(input);
      const [y, m, d] = value ? value.split("-").map(Number) : [Number(bangkokToday().slice(0, 4)), 0, 0];
      year.value = String(y + 543); month.value = m ? String(m) : "";
      days(d); validate();
    }
    function days(selected) {
      const y = Number(year.value) - 543, m = Number(month.value);
      const leap = y % 4 === 0 && (y % 100 !== 0 || y % 400 === 0);
      const count = m ? [31, leap ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][m - 1] : 31;
      options(day, Array.from({length: count}, (_, i) => [i + 1, i + 1]), "เลือกวัน");
      day.value = selected ? String(Math.min(selected, count)) : "";
    }
    function validate() {
      day.setCustomValidity(input.value && input.min && input.value < input.min ? "กรุณาเลือกวันที่ตั้งแต่วันนี้เป็นต้นไป" : "");
    }
    function update(rebuild) {
      if (rebuild) days(Number(day.value));
      const y = Number(year.value) - 543;
      const value = day.value && month.value && year.validity.valid && Number.isInteger(y)
        ? `${String(y).padStart(4, "0")}-${month.value.padStart(2, "0")}-${day.value.padStart(2, "0")}` : "";
      original.set.call(input, value); validate(); input.dispatchEvent(new Event("change", {bubbles: true}));
    }
    // Keep the existing ISO value/API contract while all visible controls use Buddhist years.
    input.type = "hidden"; input.after(fields);
    Object.defineProperty(input, "value", {get() { return original.get.call(input); },
      set(value) { original.set.call(input, value); sync(); }});
    const dateLabel = document.querySelector(`label[for="${input.id}"]`);
    if (dateLabel) { dateLabel.htmlFor = day.id; }
    day.addEventListener("change", () => update(false));
    month.addEventListener("change", () => update(true)); year.addEventListener("input", () => update(true));
    sync();
  }
  async function lookup(phone) {
    return request("/api/v1/appointment-guests/lookup", { method: "POST", body: JSON.stringify({phone}) });
  }
  async function session() {
    const requested = new URLSearchParams(location.search).get("ownerId");
    if (requested !== null && !/^[1-9][0-9]*$/.test(requested)) {
      throw new ApiError("รหัสเจ้าของไม่ถูกต้อง", 400);
    }
    const selected = await request(`/api/v1/appointment-guests/me?${query({ownerId:requested})}`);
    if (selected.ownerId) setOwner(selected); else clearOwner();
    return selected;
  }
  async function registration(phone) {
    const config = await request("/api/v1/appointment-guests/config");
    if (config.ownerRegistrationEnabled === "false") {
      throw new ApiError("ไม่พบเบอร์นี้ ขณะนี้ยังรอเชื่อมหน้าลงทะเบียนเจ้าของ กรุณาติดต่อคลินิก", 404);
    }
    const url = new URL(config.ownerRegistrationPath, location.origin);
    if (url.origin !== location.origin) throw new ApiError("เส้นทางลงทะเบียนไม่ถูกต้อง กรุณาติดต่อคลินิก", 0);
    url.searchParams.set("phone", phone.replace(/[ -]/g, ""));
    url.searchParams.set("returnTo", "appointment");
    return url.pathname + url.search;
  }
  return { request, query, owner, setOwner, clearOwner, message, options, bangkokToday, formatDate, thaiDatePicker, services, statuses, lookup, session, registration };
})();
