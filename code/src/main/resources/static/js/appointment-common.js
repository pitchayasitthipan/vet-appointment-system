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
    const parts = new Intl.DateTimeFormat("en-CA", { timeZone: "Asia/Bangkok", year: "numeric", month: "2-digit", day: "2-digit" }).formatToParts(new Date());
    const part = name => parts.find(p => p.type === name).value;
    return `${part("year")}-${part("month")}-${part("day")}`;
  }
  function formatDate(value) {
    return new Intl.DateTimeFormat("th-TH", { timeZone: "Asia/Bangkok", dateStyle: "medium", timeStyle: "short" }).format(new Date(`${value}+07:00`));
  }
  async function lookup(phone) {
    return request("/api/v1/appointment-guests/lookup", { method: "POST", body: JSON.stringify({phone}) });
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
  return { request, query, owner, setOwner, clearOwner, message, options, bangkokToday, formatDate, services, statuses, lookup, registration };
})();
