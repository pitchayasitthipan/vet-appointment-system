// PawCare - Veterinarians & Schedule Management
// ข้อมูลสัตวแพทย์มาจากฐานข้อมูลจริง: หน้าเว็บ (Thymeleaf) สร้างการ์ดไว้แล้ว
// ไฟล์นี้อ่านข้อมูลจาก data-* ของการ์ด แล้วทำ: กรอง, สถานะวันนี้, ตารางเวรรายวัน/รายสัปดาห์
// เจ้าหน้าที่: เพิ่ม/แก้ไข/ลบ ผ่าน REST API /api/v1/doctors แล้วโหลดหน้าใหม่

const API_DOCTORS = "/api/v1/doctors";
const IS_STAFF = document.body.dataset.staff === "true";

const DAYS_ORDER = ["จันทร์", "อังคาร", "พุธ", "พฤหัสบดี", "ศุกร์", "เสาร์", "อาทิตย์"];

const THAI_MONTHS = [
  "มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน",
  "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"
];

let doctorsList = [];
let currentWeekOffset = 0; // 0 = current week, -1 = last week, +1 = next week
let activeDayIndex = 0;

// ป้องกันข้อความจากฐานข้อมูลไปเป็น HTML
function escapeHtml(text) {
  return String(text ?? "").replace(/[&<>"']/g, ch => (
    { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[ch]
  ));
}

// เบอร์แบบมีขีด: 0812345678 -> 081-234-5678, 021234567 -> 02-123-4567
function formatPhone(phone) {
  const p = String(phone || "").replace(/[\s-]/g, "");
  if (p.length === 10) return `${p.slice(0, 3)}-${p.slice(3, 6)}-${p.slice(6)}`;
  if (p.length === 9) return `${p.slice(0, 2)}-${p.slice(2, 5)}-${p.slice(5)}`;
  return p || "-";
}

// อ่านข้อมูลสัตวแพทย์จากการ์ดที่หน้าเว็บสร้างไว้
function readDoctorsFromCards() {
  return Array.from(document.querySelectorAll(".doctor-card")).map(card => ({
    doctorId: Number(card.dataset.id),
    firstName: card.dataset.firstName || "",
    lastName: card.dataset.lastName || "",
    specialization: card.dataset.spec || "",
    phone: card.dataset.phone || "",
    email: card.dataset.email || "",
    workSchedule: card.dataset.schedule || "",
    card
  }));
}

function fullName(doc) {
  return `${doc.firstName} ${doc.lastName}`.trim();
}

function initials(doc) {
  return doc.firstName.length > 1 ? doc.firstName.substring(0, 2) : doc.firstName;
}

// ห้องตรวจที่เขียนไว้ในวงเล็บของตารางเวร เช่น "... (ห้องตรวจ 1)"
function parseRoom(scheduleText) {
  const match = (scheduleText || "").match(/\(([^)]+)\)/);
  return match ? match[1].trim() : "";
}

// เวลาออกตรวจในตารางเวร เช่น "09:00 - 17:00"
function parseTime(scheduleText) {
  const match = (scheduleText || "").match(/\d{1,2}[:.]\d{2}\s*-\s*\d{1,2}[:.]\d{2}(\s*น\.)?/);
  return match ? match[0] : "";
}

function getWeekDates(offsetWeeks = 0) {
  const now = new Date();
  const base = new Date(now.getFullYear(), now.getMonth(), now.getDate() + (offsetWeeks * 7));

  // Calculate Monday (Sunday is 0, Monday is 1, ..., Saturday is 6)
  const day = base.getDay();
  const diffToMonday = (day + 6) % 7;
  const monday = new Date(base.getFullYear(), base.getMonth(), base.getDate() - diffToMonday);

  const days = [];
  const shortNames = ["จ.", "อ.", "พ.", "พฤ.", "ศ.", "ส.", "อา."];

  for (let i = 0; i < 7; i++) {
    const d = new Date(monday.getFullYear(), monday.getMonth(), monday.getDate() + i);
    days.push({
      name: shortNames[i],
      num: d.getDate(),
      thDay: DAYS_ORDER[i],
      dateObj: d,
      isToday: d.toDateString() === now.toDateString()
    });
  }

  return { monday, sunday: days[6].dateObj, days };
}

function formatDateRange(monday, sunday) {
  const mDay = monday.getDate();
  const mMonth = THAI_MONTHS[monday.getMonth()];
  const mYear = monday.getFullYear() + 543;

  const sDay = sunday.getDate();
  const sMonth = THAI_MONTHS[sunday.getMonth()];
  const sYear = sunday.getFullYear() + 543;

  if (monday.getMonth() === sunday.getMonth() && mYear === sYear) {
    return `${mDay} - ${sDay} ${mMonth} ${mYear}`;
  } else if (mYear === sYear) {
    return `${mDay} ${mMonth} - ${sDay} ${sMonth} ${mYear}`;
  } else {
    return `${mDay} ${mMonth} ${mYear} - ${sDay} ${sMonth} ${sYear}`;
  }
}

let weekInfo = getWeekDates(currentWeekOffset);
let daysData = weekInfo.days;

// หมอออกตรวจวันนั้นไหม (อ่านจากข้อความตารางเวร เช่น "จันทร์ - ศุกร์", "อังคาร, พฤหัสบดี")
// ไม่ได้ระบุตารางเวร = ไม่แสดงว่าออกตรวจ (ไม่เดา)
function doctorWorksOnDay(scheduleText, targetDay) {
  if (!targetDay) return true;
  if (!scheduleText) return false;

  const targetIdx = DAYS_ORDER.indexOf(targetDay);
  if (targetIdx === -1) return true;

  // ช่วงวัน เช่น "จันทร์ - ศุกร์", "ศุกร์ - อังคาร" (ข้ามสุดสัปดาห์)
  const dayPattern = "(จันทร์|อังคาร|พุธ|พฤหัสบดี|ศุกร์|เสาร์|อาทิตย์)";
  const rangeRegex = new RegExp(`${dayPattern}\\s*[-–—]\\s*${dayPattern}`, "g");
  let range;
  while ((range = rangeRegex.exec(scheduleText)) !== null) {
    const startIdx = DAYS_ORDER.indexOf(range[1]);
    const endIdx = DAYS_ORDER.indexOf(range[2]);
    const inRange = startIdx <= endIdx
      ? targetIdx >= startIdx && targetIdx <= endIdx
      : targetIdx >= startIdx || targetIdx <= endIdx;
    if (inRange) return true;
  }

  // ระบุวันตรง ๆ เช่น "อังคาร, พฤหัสบดี" (แยกเป็นคำแล้วเทียบทั้งคำ)
  return scheduleText.split(/[\s,\/:()\-–—]+/).includes(targetDay);
}

// Initialize
document.addEventListener("DOMContentLoaded", () => {
  doctorsList = readDoctorsFromCards();

  // ถ้าวันนี้อยู่ในสัปดาห์นี้ เลือกวันนี้เป็นค่าเริ่มต้น
  const todayIdx = daysData.findIndex(d => d.isToday);
  activeDayIndex = todayIdx !== -1 ? todayIdx : 0;

  decorateCards();
  fillSpecFilter();
  updateCalendarView();
  renderWeeklyMatrixTable();
  setupEventListeners();
  setupDoctorForm();
});

// เติมป้ายความเชี่ยวชาญ ห้อง และสถานะวันนี้ ให้การ์ดแต่ละใบ
function decorateCards() {
  const today = DAYS_ORDER[(new Date().getDay() + 6) % 7];

  doctorsList.forEach(doc => {
    const card = doc.card;

    const tagsEl = card.querySelector(".doc-tags");
    const tags = (doc.specialization || "สัตวแพทย์ทั่วไป").split(/[,/]+/).map(t => t.trim()).filter(Boolean);
    tagsEl.innerHTML = tags.map(tag => `<span class="tag-pill">${escapeHtml(tag)}</span>`).join("");

    const room = parseRoom(doc.workSchedule);
    const roomEl = card.querySelector(".doc-room");
    if (room && roomEl) {
      roomEl.lastElementChild.textContent = room;
      roomEl.hidden = false;
    }

    const isOn = doctorWorksOnDay(doc.workSchedule, today);
    const badge = card.querySelector(".status-badge");
    badge.classList.toggle("available", isOn);
    badge.classList.toggle("off", !isOn);
    badge.querySelector(".status-text").textContent = isOn ? "ออกตรวจวันนี้" : "หยุดวันนี้";
    card.querySelector(".doc-time-text").textContent = isOn ? (parseTime(doc.workSchedule) || "ตามตารางเวร") : "-";
  });
}

// ตัวเลือกความเชี่ยวชาญสร้างจากข้อมูลจริง (ไม่เขียนตายตัว)
function fillSpecFilter() {
  const select = document.getElementById("specFilter");
  if (!select) return;
  const specs = new Set();
  doctorsList.forEach(doc => (doc.specialization || "").split(/[,/]+/)
    .map(t => t.trim()).filter(Boolean).forEach(t => specs.add(t)));
  Array.from(specs).sort((a, b) => a.localeCompare(b, "th")).forEach(spec => {
    const option = document.createElement("option");
    option.value = spec;
    option.textContent = spec;
    select.appendChild(option);
  });
}

// Calendar Navigation & Update
function updateCalendarView() {
  weekInfo = getWeekDates(currentWeekOffset);
  daysData = weekInfo.days;

  const rangeEl = document.getElementById("currentDateRange");
  if (rangeEl) {
    rangeEl.innerText = formatDateRange(weekInfo.monday, weekInfo.sunday);
  }

  renderDaysStrip();
  renderScheduleSlots(activeDayIndex);
}

function changeWeek(direction) {
  currentWeekOffset += direction;
  updateCalendarView();
}

function goToCurrentWeekAndToday() {
  currentWeekOffset = 0;
  weekInfo = getWeekDates(0);
  daysData = weekInfo.days;

  const todayIdx = daysData.findIndex(d => d.isToday);
  activeDayIndex = todayIdx !== -1 ? todayIdx : 0;

  updateCalendarView();
}

// Render Days Strip
function renderDaysStrip() {
  const container = document.getElementById("daysStrip");
  if (!container) return;

  container.innerHTML = daysData.map((d, idx) => `
    <button type="button" class="day-pill ${idx === activeDayIndex ? 'active' : ''} ${d.isToday ? 'is-today' : ''}"
         onclick="selectDay(${idx})"
         title="${d.thDay}ที่ ${d.num} ${d.isToday ? '(วันนี้)' : ''}">
      <span class="day-name">${d.name}</span>
      <span class="day-number">${d.num}</span>
    </button>
  `).join("");
}

// Day Selection
function selectDay(index) {
  activeDayIndex = index;
  renderDaysStrip();
  renderScheduleSlots(index);
}

// ตารางเวรรายวันฝั่งขวา (สร้างจากตารางเวรของหมอแต่ละคน)
function renderScheduleSlots(dayIdx) {
  const container = document.getElementById("timeSlotsContainer");
  if (!container) return;

  const currentDay = daysData[dayIdx] || daysData[0];

  const slots = doctorsList.map(doc => ({
    doctorId: doc.doctorId,
    name: fullName(doc),
    initials: initials(doc),
    spec: doc.specialization || "สัตวแพทย์ทั่วไป",
    room: parseRoom(doc.workSchedule),
    time: parseTime(doc.workSchedule) || "-",
    status: doctorWorksOnDay(doc.workSchedule, currentDay.thDay) ? "on" : "off"
  }));

  // Sort: on duty doctors first
  slots.sort((a, b) => (b.status === "on" ? 1 : 0) - (a.status === "on" ? 1 : 0));

  if (slots.length === 0) {
    container.innerHTML = `<div class="slot-empty">ยังไม่มีข้อมูลตารางเวร</div>`;
    return;
  }

  container.innerHTML = slots.map(slot => `
    <div class="slot-item" onclick="viewDoctorSchedule(${slot.doctorId})" title="คลิกเพื่อดูรายละเอียดตารางเวร">
      <div class="slot-time">${escapeHtml(slot.status === "on" ? slot.time : "-")}</div>
      <span class="slot-avatar doc-initial">${escapeHtml(slot.initials)}</span>
      <div class="slot-info">
        <div class="slot-doc-name">${escapeHtml(slot.name)}</div>
        <div class="slot-spec">${escapeHtml(slot.spec)}</div>
      </div>
      ${slot.room ? `<div class="slot-room"><span class="icon i-hospital icon-accent icon-sm"></span> ${escapeHtml(slot.room)}</div>` : ""}
      <div class="slot-status-pill ${slot.status}">
        ${slot.status === "on" ? "ออกตรวจ" : "ไม่ออกตรวจ"}
      </div>
    </div>
  `).join("");
}

// ค้นหาและกรอง (ซ่อน/แสดงการ์ดที่มีอยู่)
function setupEventListeners() {
  const searchInput = document.getElementById("searchInput");
  const specFilter = document.getElementById("specFilter");
  const dayFilter = document.getElementById("dayFilter");

  const filterHandler = () => {
    const query = (searchInput ? searchInput.value : "").trim().toLowerCase();
    const spec = specFilter ? specFilter.value : "";
    const day = dayFilter ? dayFilter.value : "";

    let shown = 0;
    doctorsList.forEach(doc => {
      const matchQuery = !query ||
        fullName(doc).toLowerCase().includes(query) ||
        (doc.specialization && doc.specialization.toLowerCase().includes(query));
      const matchSpec = !spec || (doc.specialization && doc.specialization.includes(spec));
      const matchDay = !day || doctorWorksOnDay(doc.workSchedule, day);

      const visible = matchQuery && matchSpec && matchDay;
      doc.card.hidden = !visible;
      if (visible) shown++;
    });

    updateDoctorCount(shown);
    const noMatch = document.getElementById("noMatch");
    if (noMatch) noMatch.hidden = shown > 0 || doctorsList.length === 0;
  };

  if (searchInput) searchInput.addEventListener("input", filterHandler);
  if (specFilter) specFilter.addEventListener("change", filterHandler);
  if (dayFilter) dayFilter.addEventListener("change", filterHandler);
}

function updateDoctorCount(count) {
  const el = document.getElementById("doctorCount");
  if (el) el.innerText = `พบสัตวแพทย์ทั้งหมด ${count} ท่าน`;
}

// ดูตารางเวร (ป๊อปอัป) + ปุ่มจองนัดกับหมอคนนี้
function viewDoctorSchedule(id) {
  const doc = doctorsList.find(d => d.doctorId === id);
  const modal = document.getElementById("scheduleModal");
  if (!doc || !modal) return;

  document.getElementById("modalDocName").innerText = fullName(doc);
  document.getElementById("modalDocSpec").innerText = doc.specialization || "สัตวแพทย์ทั่วไป";
  document.getElementById("modalDocSchedule").innerText = doc.workSchedule || "ยังไม่ระบุตารางเวร";
  document.getElementById("modalDocPhone").innerText = formatPhone(doc.phone);
  document.getElementById("modalDocEmail").innerText = doc.email || "-";
  document.getElementById("modalBookLink").href = `/appointments/new?doctorId=${doc.doctorId}`;

  modal.classList.add("show");
}

// Switch between Daily Timeline View and Weekly Matrix Table View
function switchScheduleView(viewType) {
  const weekly = viewType === "weekly";
  document.getElementById("tabSchedule").classList.toggle("active", !weekly);
  document.getElementById("tabList").classList.toggle("active", weekly);
  document.getElementById("dailyScheduleView").hidden = weekly;
  document.getElementById("weeklyScheduleView").hidden = !weekly;
  if (weekly) renderWeeklyMatrixTable();
}

// ตารางภาพรวมทั้งสัปดาห์
function renderWeeklyMatrixTable() {
  const tbody = document.getElementById("weeklyTableBody");
  if (!tbody) return;

  if (doctorsList.length === 0) {
    tbody.innerHTML = `<tr><td colspan="8" class="slot-empty">ยังไม่มีข้อมูลตารางเวร</td></tr>`;
    return;
  }

  tbody.innerHTML = doctorsList.map(doc => {
    const dayCells = DAYS_ORDER.map(day => doctorWorksOnDay(doc.workSchedule, day)
      ? `<td><span class="duty-pill on" title="${day}: ออกตรวจ">ตรวจ</span></td>`
      : `<td><span class="duty-pill off" title="${day}: ไม่ออกตรวจ">-</span></td>`
    ).join("");
    const room = parseRoom(doc.workSchedule);

    return `
      <tr>
        <td>
          <div class="weekly-doc-cell" onclick="viewDoctorSchedule(${doc.doctorId})" title="คลิกเพื่อดูรายละเอียดตารางเวร">
            <span class="weekly-doc-avatar doc-initial">${escapeHtml(initials(doc))}</span>
            <div>
              <div class="weekly-doc-name">${escapeHtml(fullName(doc))}</div>
              ${room ? `<div class="weekly-doc-room">${escapeHtml(room)}</div>` : ""}
            </div>
          </div>
        </td>
        ${dayCells}
      </tr>
    `;
  }).join("");
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.remove("show");
}

// ปิดป๊อปอัปเมื่อกดพื้นหลังหรือปุ่ม Esc
document.addEventListener("click", e => {
  if (e.target.classList && e.target.classList.contains("modal-overlay")) e.target.classList.remove("show");
});
document.addEventListener("keydown", e => {
  if (e.key === "Escape") document.querySelectorAll(".modal-overlay.show").forEach(m => m.classList.remove("show"));
});

// ==================== เจ้าหน้าที่: เพิ่ม / แก้ไข / ลบ ====================

function clearFormErrors(form) {
  form.querySelectorAll(".field-error").forEach(el => { el.textContent = ""; });
  form.querySelectorAll(".form-input").forEach(el => el.classList.remove("is-invalid"));
  document.getElementById("doctorFormMessage").textContent = "";
}

// เปิดฟอร์ม: ไม่ส่ง id = เพิ่มใหม่, ส่ง id = แก้ไขหมอคนนั้น
function openDoctorForm(id) {
  if (!IS_STAFF) return;
  const modal = document.getElementById("doctorFormModal");
  const form = document.getElementById("doctorForm");
  if (!modal || !form) return;

  form.reset();
  clearFormErrors(form);

  const doc = id ? doctorsList.find(d => d.doctorId === id) : null;
  const title = document.querySelector("#doctorFormTitle span:last-child");
  title.textContent = doc ? `แก้ไขข้อมูล ${fullName(doc)}` : "เพิ่มข้อมูลสัตวแพทย์ใหม่";

  if (doc) {
    form.doctorId.value = doc.doctorId;
    form.firstName.value = doc.firstName;
    form.lastName.value = doc.lastName;
    form.specialization.value = doc.specialization;
    form.phone.value = doc.phone;
    form.email.value = doc.email;
    form.workSchedule.value = doc.workSchedule;
  }

  modal.classList.add("show");
  form.firstName.focus();
}

function setupDoctorForm() {
  const form = document.getElementById("doctorForm");
  if (!form) return;

  form.addEventListener("submit", async event => {
    event.preventDefault();
    clearFormErrors(form);

    const id = form.doctorId.value;
    const body = {
      firstName: form.firstName.value.trim(),
      lastName: form.lastName.value.trim(),
      specialization: form.specialization.value.trim(),
      phone: form.phone.value.trim(),
      email: form.email.value.trim(),
      workSchedule: form.workSchedule.value.trim()
    };

    const submit = document.getElementById("doctorFormSubmit");
    submit.disabled = true;
    try {
      const res = await fetch(id ? `${API_DOCTORS}/${id}` : API_DOCTORS, {
        method: id ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
      });

      if (res.ok) {
        location.reload();
        return;
      }

      const err = await res.json().catch(() => ({}));
      // 400: แสดงข้อความใต้ช่องที่ผิด
      Object.entries(err.errors || {}).forEach(([field, message]) => {
        const holder = form.querySelector(`.field-error[data-for="${field}"]`);
        if (holder) holder.textContent = message;
        if (form[field]) form[field].classList.add("is-invalid");
      });
      document.getElementById("doctorFormMessage").textContent =
        res.status === 403 ? "หมดเวลาเจ้าหน้าที่ กรุณาใส่รหัสที่หน้า Staff Only ใหม่" : (err.message || "ไม่สามารถบันทึกได้");
    } catch (e) {
      document.getElementById("doctorFormMessage").textContent = "เชื่อมต่อเซิร์ฟเวอร์ไม่ได้ กรุณาลองใหม่";
    } finally {
      submit.disabled = false;
    }
  });
}

async function deleteDoctor(id) {
  if (!IS_STAFF) return;
  const doc = doctorsList.find(d => d.doctorId === id);
  if (!doc || !confirm(`ต้องการลบข้อมูล ${fullName(doc)} ใช่หรือไม่?`)) return;

  try {
    const res = await fetch(`${API_DOCTORS}/${id}`, { method: "DELETE" });
    if (res.ok) {
      location.reload();
      return;
    }
    const err = await res.json().catch(() => ({}));
    // 409: ยังมีนัดหมายผูกกับหมอคนนี้
    alert(err.message || "ไม่สามารถลบได้");
  } catch (e) {
    alert("เชื่อมต่อเซิร์ฟเวอร์ไม่ได้ กรุณาลองใหม่");
  }
}
