// PawCare - Veterinarians & Schedule Management

// Initial Rich Data from Mockup (used if API is offline or returns empty)
const DEFAULT_DOCTORS = [
  {
    doctorId: 1,
    firstName: "ณิชาภา",
    lastName: "วงศ์วิริยะ",
    nameEn: "Dr. Nichapa Wongviriya",
    specialization: "อายุรศาสตร์, โรคผิวหนัง, อัลตราซาวด์",
    phone: "081-998-1122",
    email: "nichapa.w@pawcare.com",
    titlePrefix: "สพ.ญ.",
    isChief: true,
    room: "ห้องตรวจ 1",
    experience: "8 ปี",
    education: "คณะสัตวแพทยศาสตร์ จุฬาฯ",
    statusToday: "available",
    statusText: "ออกตรวจวันนี้",
    timeToday: "09:00 - 17:00 น.",
    avatar: "images/dr-nichapa.jpg",
    workSchedule: "จันทร์ - ศุกร์: 09:00 - 17:00 น. (ห้องตรวจ 1)"
  },
  {
    doctorId: 2,
    firstName: "กิตติภัทร",
    lastName: "สินธวรกุล",
    nameEn: "Dr. Kittiphat Sinthavorakul",
    specialization: "ศัลยกรรม, กระดูกและข้อ, ออร์โธปิดิกส์",
    phone: "082-334-5566",
    email: "kittiphat.s@pawcare.com",
    titlePrefix: "น.สพ.",
    isChief: false,
    room: "ห้องผ่าตัด",
    experience: "7 ปี",
    education: "คณะสัตวแพทยศาสตร์ ม.เกษตรฯ",
    statusToday: "available",
    statusText: "ออกตรวจวันนี้",
    timeToday: "10:00 - 18:00 น.",
    avatar: "images/dr-kittiphat.jpg",
    workSchedule: "อังคาร - เสาร์: 10:00 - 18:00 น. (ห้องผ่าตัด)"
  },
  {
    doctorId: 3,
    firstName: "ปรียาภรณ์",
    lastName: "ตั้งพงษ์ศิริ",
    nameEn: "Dr. Preeyaporn Tangpongsiri",
    specialization: "สัตว์เลี้ยงพิเศษ, สัตว์ฟันแทะ, สัตว์เลี้ยงขนาดเล็ก",
    phone: "083-445-6677",
    email: "preeyaporn.t@pawcare.com",
    titlePrefix: "สพ.ญ.",
    isChief: false,
    room: "ห้องตรวจ 2",
    experience: "6 ปี",
    education: "คณะสัตวแพทยศาสตร์ ม.มหิดล",
    statusToday: "available",
    statusText: "ออกตรวจวันนี้",
    timeToday: "09:00 - 16:00 น.",
    avatar: "images/dr-preeyaporn.jpg",
    workSchedule: "พุธ - อาทิตย์: 09:00 - 16:00 น. (ห้องตรวจ 2)"
  },
  {
    doctorId: 4,
    firstName: "ธนวัฒน์",
    lastName: "อภิญญากุล",
    nameEn: "Dr. Thanawat Apinyakul",
    specialization: "อายุรศาสตร์, หัวใจ, ระบบทางเดินหายใจ",
    phone: "084-556-7788",
    email: "thanawat.a@pawcare.com",
    titlePrefix: "น.สพ.",
    isChief: false,
    room: "ห้องตรวจ 3",
    experience: "5 ปี",
    education: "คณะสัตวแพทยศาสตร์ ม.ขอนแก่น",
    statusToday: "off",
    statusText: "หยุดวันนี้",
    timeToday: "-",
    avatar: "images/dr-thanawat.jpg",
    workSchedule: "ศุกร์ - อังคาร: 09:00 - 18:00 น. (ห้องตรวจ 3)"
  },
  {
    doctorId: 5,
    firstName: "อรอนงค์",
    lastName: "จันทร์ไพศาล",
    nameEn: "Dr. Ohanong Chanpaisan",
    specialization: "ทันตกรรม, เวชศาสตร์ป้องกัน, วัคซีน",
    phone: "085-667-8899",
    email: "ohanong.c@pawcare.com",
    titlePrefix: "สพ.ญ.",
    isChief: false,
    room: "ห้องตรวจ 1",
    experience: "6 ปี",
    education: "คณะสัตวแพทยศาสตร์ ม.เชียงใหม่",
    statusToday: "available",
    statusText: "ออกตรวจวันนี้",
    timeToday: "13:00 - 20:00 น.",
    avatar: "images/dr-ohanong.jpg",
    workSchedule: "พุธ - อาทิตย์: 13:00 - 20:00 น. (ห้องตรวจ 1)"
  }
];

let doctorsList = [];
let currentWeekOffset = 0; // 0 = current week, -1 = last week, +1 = next week
let activeDayIndex = 2; // Default to Wednesday or today's day

const THAI_MONTHS = [
  "มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน",
  "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"
];

function getWeekDates(offsetWeeks = 0) {
  const now = new Date();
  const base = new Date(now.getFullYear(), now.getMonth(), now.getDate() + (offsetWeeks * 7));
  
  // Calculate Monday (Sunday is 0, Monday is 1, ..., Saturday is 6)
  const day = base.getDay();
  const diffToMonday = (day + 6) % 7;
  const monday = new Date(base.getFullYear(), base.getMonth(), base.getDate() - diffToMonday);

  const days = [];
  const shortNames = ["จ.", "อ.", "พ.", "พฤ.", "ศ.", "ส.", "อา."];
  const fullNames = ["จันทร์", "อังคาร", "พุธ", "พฤหัสบดี", "ศุกร์", "เสาร์", "อาทิตย์"];
  const fullEnDays = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];

  for (let i = 0; i < 7; i++) {
    const d = new Date(monday.getFullYear(), monday.getMonth(), monday.getDate() + i);
    days.push({
      name: shortNames[i],
      num: d.getDate(),
      fullDay: fullEnDays[i],
      thDay: fullNames[i],
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

// Helper: Check if a doctor works on a given day (e.g. "จันทร์", "อังคาร")
function doctorWorksOnDay(scheduleText, targetDay) {
  if (!scheduleText || !targetDay) return true;
  
  const daysOrder = ["จันทร์", "อังคาร", "พุธ", "พฤหัสบดี", "ศุกร์", "เสาร์", "อาทิตย์"];
  const targetIdx = daysOrder.indexOf(targetDay);
  if (targetIdx === -1) return true;

  // Direct match
  if (scheduleText.includes(targetDay)) return true;

  // Match range (e.g. "จันทร์ - ศุกร์", "อังคาร - เสาร์", "ศุกร์ - อังคาร")
  const rangeMatch = scheduleText.match(/(จันทร์|อังคาร|พุธ|พฤหัสบดี|ศุกร์|เสาร์|อาทิตย์)\s*[-–—ถึง]+\s*(จันทร์|อังคาร|พุธ|พฤหัสบดี|ศุกร์|เสาร์|อาทิตย์)/);
  if (rangeMatch) {
    const startIdx = daysOrder.indexOf(rangeMatch[1]);
    const endIdx = daysOrder.indexOf(rangeMatch[2]);
    if (startIdx !== -1 && endIdx !== -1) {
      if (startIdx <= endIdx) {
        return targetIdx >= startIdx && targetIdx <= endIdx;
      } else {
        // Wraps over weekend (e.g. ศุกร์ - อังคาร)
        return targetIdx >= startIdx || targetIdx <= endIdx;
      }
    }
  }
  return false;
}

// Initialize
document.addEventListener("DOMContentLoaded", () => {
  // If today is in the current week, select today by default
  const todayIdx = daysData.findIndex(d => d.isToday);
  if (todayIdx !== -1) {
    activeDayIndex = todayIdx;
  }

  updateCalendarView();
  loadDoctors();
  setupEventListeners();
});

// Load Doctors from REST API /api/doctors (or fallback)
async function loadDoctors() {
  try {
    const res = await fetch("/api/doctors");
    if (res.ok) {
      const data = await res.json();
      if (Array.isArray(data) && data.length > 0) {
        // Merge with avatar & extra presentation properties
        doctorsList = data.map((doc, idx) => {
          const matchDefault = DEFAULT_DOCTORS.find(d => d.doctorId === doc.doctorId || d.email === doc.email) || DEFAULT_DOCTORS[idx % DEFAULT_DOCTORS.length];
          return {
            ...matchDefault,
            ...doc,
            avatar: matchDefault ? matchDefault.avatar : "images/dr-nichapa.jpg"
          };
        });
      } else {
        doctorsList = [...DEFAULT_DOCTORS];
      }
    } else {
      doctorsList = [...DEFAULT_DOCTORS];
    }
  } catch (err) {
    console.log("Using default mock doctor list:", err);
    doctorsList = [...DEFAULT_DOCTORS];
  }

  renderDoctors(doctorsList);
  updateDoctorCount(doctorsList.length);
  renderScheduleSlots(activeDayIndex);
  renderWeeklyMatrixTable();
}

// Render Doctor Cards
function renderDoctors(list) {
  const container = document.getElementById("doctorCardsContainer");
  if (!container) return;

  if (list.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 3rem; background: #fff; border-radius: 12px; border: 1px dashed #e2e8f0;">
        <span style="font-size: 2.5rem;">🐾</span>
        <h4 style="margin-top: 0.5rem; color: #475569;">ไม่พบข้อมูลสัตวแพทย์ที่ค้นหา</h4>
        <p style="color: #94a3b8; font-size: 0.85rem;">กรุณาลองเปลี่ยนคำค้นหาหรือเลือกตัวกรองใหม่</p>
      </div>
    `;
    return;
  }

  container.innerHTML = list.map(doc => {
    const prefix = doc.titlePrefix || (doc.firstName.startsWith("ณิชาภา") || doc.firstName.startsWith("ปรียาภรณ์") || doc.firstName.startsWith("อรอนงค์") || doc.firstName.startsWith("นันทิดา") || doc.firstName.startsWith("วรรณภา") ? "สพ.ญ." : "น.สพ.");
    const fullName = `${prefix} ${doc.firstName} ${doc.lastName}`;
    const nameEn = doc.nameEn || `Dr. ${doc.firstName} ${doc.lastName}`;
    const tags = (doc.specialization || "").split(/[,/]+/).map(t => t.trim()).filter(Boolean);
    const isAvail = doc.statusToday === "available";
    const statusClass = isAvail ? "available" : "off";
    const statusText = doc.statusText || (isAvail ? "ออกตรวจวันนี้" : "หยุดวันนี้");
    const timeText = isAvail ? (doc.timeToday || "09:00 - 17:00 น.") : "-";
    const avatarSrc = doc.avatar || "images/dr-nichapa.jpg";

    return `
      <div class="doctor-card" data-id="${doc.doctorId}">
        <div class="doc-avatar-wrap">
          <img src="${avatarSrc}" alt="${fullName}" class="doc-avatar" onerror="this.src='images/dr-nichapa.jpg'">
        </div>
        
        <div class="doc-details">
          <div class="doc-header-row">
            <div class="doc-title-group">
              ${doc.isChief ? '<span class="badge-chief">หัวหน้าแพทย์</span>' : ''}
              <h3 class="doc-name">${fullName}</h3>
            </div>
          </div>
          
          <div class="doc-name-en">${nameEn}</div>
          
          <div class="doc-tags">
            ${tags.map(tag => `<span class="tag-pill">${tag}</span>`).join('')}
          </div>
          
          <div class="doc-meta-row">
            <span class="meta-item"><span class="icon i-hospital icon-accent icon-sm"></span> ${doc.room || 'ห้องตรวจ 1'}</span>
            <span class="meta-item"><span class="icon i-stethoscope icon-accent icon-sm"></span> ประสบการณ์ ${doc.experience || '6 ปี'}</span>
            <span class="meta-item"><span class="icon i-identification-card icon-accent icon-sm"></span> ${doc.education || 'คณะสัตวแพทยศาสตร์'}</span>
            ${doc.phone ? `<span class="meta-item"><span class="icon i-phone icon-accent icon-sm"></span> ${doc.phone}</span>` : ''}
          </div>
        </div>
        
        <div class="doc-card-right">
          <div>
            <div class="status-badge ${statusClass}">
              <span class="status-dot"></span>
              <span>${statusText}</span>
            </div>
            <div class="doc-time-text">${timeText}</div>
          </div>
          
          <button class="btn-schedule" onclick="viewDoctorSchedule(${doc.doctorId})">
            <span class="icon i-calendar-blank icon-sm"></span> ดูตารางเวร
          </button>
        </div>
      </div>
    `;
  }).join("");
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
  activeDayIndex = todayIdx !== -1 ? todayIdx : 2;
  
  updateCalendarView();
}

// Render Days Strip
function renderDaysStrip() {
  const container = document.getElementById("daysStrip");
  if (!container) return;

  container.innerHTML = daysData.map((d, idx) => `
    <div class="day-pill ${idx === activeDayIndex ? 'active' : ''} ${d.isToday ? 'is-today' : ''}" 
         onclick="selectDay(${idx})" 
         title="${d.thDay}ที่ ${d.num} ${d.isToday ? '(วันนี้)' : ''}">
      <span class="day-name">${d.name}</span>
      <span class="day-number">${d.num}</span>
    </div>
  `).join("");
}

// Day Selection
function selectDay(index) {
  activeDayIndex = index;
  renderDaysStrip();
  renderScheduleSlots(index);
}

// Render Slots for Right Column (Derived dynamically from doctorsList)
function renderScheduleSlots(dayIdx) {
  const container = document.getElementById("timeSlotsContainer");
  if (!container) return;

  const currentDay = daysData[dayIdx] || daysData[0];
  const targetDayName = currentDay.thDay;

  const listToUse = (doctorsList && doctorsList.length > 0) ? doctorsList : DEFAULT_DOCTORS;

  const slots = listToUse.map(doc => {
    const isOn = doctorWorksOnDay(doc.workSchedule, targetDayName);
    
    let time = "09:00 - 17:00 น.";
    if (doc.workSchedule) {
      const match = doc.workSchedule.match(/\d{2}:\d{2}\s*-\s*\d{2}:\d{2}\s*(?:น\.)?/);
      if (match) time = match[0];
    } else if (doc.timeToday) {
      time = doc.timeToday;
    }

    return {
      doctorId: doc.doctorId,
      name: `${doc.titlePrefix || 'สพ.'} ${doc.firstName} ${doc.lastName}`,
      spec: doc.specialization || "สัตวแพทย์ทั่วไป",
      room: doc.room || "ห้องตรวจ 1",
      avatar: doc.avatar || "images/dr-nichapa.jpg",
      time: time,
      status: isOn ? "on" : "off"
    };
  });

  // Sort: on duty doctors first
  slots.sort((a, b) => (b.status === "on" ? 1 : 0) - (a.status === "on" ? 1 : 0));

  if (slots.length === 0) {
    container.innerHTML = `<div style="text-align: center; padding: 2rem; color: #94a3b8;">ไม่มีข้อมูลตารางเวรในวันนี้</div>`;
    return;
  }

  container.innerHTML = slots.map(slot => `
    <div class="slot-item" onclick="viewDoctorSchedule(${slot.doctorId})" title="คลิกเพื่อดูรายละเอียดตารางเวร ${slot.name}" style="cursor: pointer;">
      <div class="slot-time">${slot.time}</div>
      <img src="${slot.avatar}" alt="${slot.name}" class="slot-avatar" onerror="this.src='images/dr-nichapa.jpg'">
      <div class="slot-info">
        <div class="slot-doc-name">${slot.name}</div>
        <div class="slot-spec">${slot.spec}</div>
      </div>
      <div class="slot-room"><span class="icon i-hospital icon-accent icon-sm"></span> ${slot.room}</div>
      <div class="slot-status-pill ${slot.status}">
        ${slot.status === 'on' ? 'ออกตรวจ' : 'ไม่ออกตรวจ'}
      </div>
    </div>
  `).join("");
}

// Setup Event Listeners (Search & Filter)
function setupEventListeners() {
  const searchInput = document.getElementById("searchInput");
  const specFilter = document.getElementById("specFilter");
  const dayFilter = document.getElementById("dayFilter");
  const btnSearch = document.querySelector(".btn-search");

  const filterHandler = () => {
    const query = (searchInput ? searchInput.value : "").trim().toLowerCase();
    const spec = specFilter ? specFilter.value : "";
    const day = dayFilter ? dayFilter.value : "";

    const filtered = doctorsList.filter(doc => {
      const matchQuery = !query || 
        doc.firstName.toLowerCase().includes(query) ||
        doc.lastName.toLowerCase().includes(query) ||
        (doc.specialization && doc.specialization.toLowerCase().includes(query)) ||
        (doc.nameEn && doc.nameEn.toLowerCase().includes(query));

      const matchSpec = !spec || (doc.specialization && doc.specialization.includes(spec));
      const matchDay = !day || doctorWorksOnDay(doc.workSchedule, day);

      return matchQuery && matchSpec && matchDay;
    });

    renderDoctors(filtered);
    updateDoctorCount(filtered.length);
  };

  if (searchInput) searchInput.addEventListener("input", filterHandler);
  if (specFilter) specFilter.addEventListener("change", filterHandler);
  if (dayFilter) dayFilter.addEventListener("change", filterHandler);
  if (btnSearch) btnSearch.addEventListener("click", filterHandler);
}

function updateDoctorCount(count) {
  const el = document.getElementById("doctorCount");
  if (el) el.innerText = `พบสัตวแพทย์ทั้งหมด ${count} ท่าน`;
}

let currentSelectedDoctorId = 1;

// View Doctor Schedule Modal
function viewDoctorSchedule(id) {
  currentSelectedDoctorId = id;
  const doc = doctorsList.find(d => d.doctorId === id) || DEFAULT_DOCTORS[0];
  const modal = document.getElementById("scheduleModal");
  if (!modal) return;

  document.getElementById("modalDocName").innerText = `${doc.titlePrefix || 'สพ.ญ.'} ${doc.firstName} ${doc.lastName}`;
  document.getElementById("modalDocSpec").innerText = doc.specialization || "สัตวแพทย์ทั่วไป";
  document.getElementById("modalDocRoom").innerText = doc.room || "ห้องตรวจ 1";
  document.getElementById("modalDocSchedule").innerText = doc.workSchedule || "จันทร์ - ศุกร์: 09:00 - 17:00 น.";
  document.getElementById("modalDocPhone").innerText = doc.phone || "081-111-2233";
  document.getElementById("modalDocEmail").innerText = doc.email || "-";

  modal.classList.add("show");
}

function proceedToBookingFromModal() {
  closeModal('scheduleModal');
  openBookingModal(currentSelectedDoctorId);
}

// Switch between Daily Timeline View and Weekly Matrix Table View
function switchScheduleView(viewType) {
  const tabSchedule = document.getElementById("tabSchedule");
  const tabList = document.getElementById("tabList");
  const dailyView = document.getElementById("dailyScheduleView");
  const weeklyView = document.getElementById("weeklyScheduleView");

  if (viewType === 'weekly') {
    if (tabSchedule) tabSchedule.classList.remove("active");
    if (tabList) tabList.classList.add("active");
    if (dailyView) dailyView.style.display = "none";
    if (weeklyView) weeklyView.style.display = "block";
    renderWeeklyMatrixTable();
  } else {
    if (tabList) tabList.classList.remove("active");
    if (tabSchedule) tabSchedule.classList.add("active");
    if (weeklyView) weeklyView.style.display = "none";
    if (dailyView) dailyView.style.display = "block";
  }
}

// Render Weekly Overview Matrix Table
function renderWeeklyMatrixTable() {
  const tbody = document.getElementById("weeklyTableBody");
  if (!tbody) return;

  const list = (doctorsList && doctorsList.length > 0) ? doctorsList : DEFAULT_DOCTORS;
  const days = ["จันทร์", "อังคาร", "พุธ", "พฤหัสบดี", "ศุกร์", "เสาร์", "อาทิตย์"];

  tbody.innerHTML = list.map(doc => {
    const fullName = `${doc.titlePrefix || 'สพ.'} ${doc.firstName} ${doc.lastName}`;
    const avatarSrc = doc.avatar || "images/dr-nichapa.jpg";

    const dayCells = days.map(day => {
      const works = doctorWorksOnDay(doc.workSchedule, day);
      if (works) {
        return `<td><span class="duty-pill on" title="${day}: ออกตรวจ (${doc.workSchedule || ''})">ตรวจ</span></td>`;
      } else {
        return `<td><span class="duty-pill off" title="${day}: ไม่ออกตรวจ">-</span></td>`;
      }
    }).join("");

    return `
      <tr>
        <td>
          <div class="weekly-doc-cell" onclick="viewDoctorSchedule(${doc.doctorId})" title="คลิกเพื่อดูรายละเอียดตารางเวร ${fullName}">
            <img src="${avatarSrc}" alt="${fullName}" class="weekly-doc-avatar" onerror="this.src='images/dr-nichapa.jpg'">
            <div>
              <div class="weekly-doc-name">${fullName}</div>
              <div class="weekly-doc-room">${doc.room || 'ห้องตรวจ'}</div>
            </div>
          </div>
        </td>
        ${dayCells}
      </tr>
    `;
  }).join("");
}

// Open Appointment Booking Modal
function openBookingModal(doctorId) {
  const modal = document.getElementById("bookingModal");
  if (!modal) return;

  const list = (doctorsList && doctorsList.length > 0) ? doctorsList : DEFAULT_DOCTORS;
  const targetId = doctorId || currentSelectedDoctorId || list[0].doctorId;

  const selectEl = document.getElementById("bookingDoctorSelect");
  if (selectEl) {
    selectEl.innerHTML = list.map(d => `
      <option value="${d.doctorId}" ${d.doctorId == targetId ? 'selected' : ''}>
        ${d.titlePrefix || 'สพ.'} ${d.firstName} ${d.lastName} (${d.specialization})
      </option>
    `).join("");
  }

  onBookingDoctorChange(targetId);

  const dateInput = document.getElementById("bookingDate");
  if (dateInput && !dateInput.value) {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    dateInput.value = tomorrow.toISOString().split("T")[0];
    dateInput.min = new Date().toISOString().split("T")[0];
  }

  const successBox = document.getElementById("bookingSuccessBox");
  if (successBox) successBox.style.display = "none";

  const submitBtn = document.getElementById("btnSubmitBooking");
  if (submitBtn) {
    submitBtn.disabled = false;
    submitBtn.innerText = "ยืนยันการนัดหมาย";
  }

  modal.classList.add("show");
}

function onBookingDoctorChange(docId) {
  const list = (doctorsList && doctorsList.length > 0) ? doctorsList : DEFAULT_DOCTORS;
  const doc = list.find(d => d.doctorId == docId) || list[0];
  if (!doc) return;

  const nameEl = document.getElementById("bookingDocName");
  const specEl = document.getElementById("bookingDocSpec");
  const avatarEl = document.getElementById("bookingDocAvatar");

  if (nameEl) nameEl.innerText = `${doc.titlePrefix || 'สพ.'} ${doc.firstName} ${doc.lastName}`;
  if (specEl) specEl.innerText = `${doc.specialization} • ${doc.room || 'ห้องตรวจ 1'}`;
  if (avatarEl) avatarEl.src = doc.avatar || "images/dr-nichapa.jpg";
}

function handleBookingSubmit(event) {
  event.preventDefault();
  const successBox = document.getElementById("bookingSuccessBox");
  const submitBtn = document.getElementById("btnSubmitBooking");

  if (successBox) successBox.style.display = "block";
  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.innerText = "บันทึกเรียบร้อย ✓";
  }

  setTimeout(() => {
    closeModal("bookingModal");
    if (event.target) event.target.reset();
    if (successBox) successBox.style.display = "none";
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.innerText = "ยืนยันการนัดหมาย";
    }
  }, 1600);
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.remove("show");
}

// Modal for Adding / Creating Doctor (via REST API POST /api/doctors)
function openAddDoctorModal() {
  const modal = document.getElementById("addDoctorModal");
  if (modal) modal.classList.add("show");
}

async function handleAddDoctor(event) {
  event.preventDefault();
  const form = event.target;
  const newDoctor = {
    firstName: form.firstName.value.trim(),
    lastName: form.lastName.value.trim(),
    specialization: form.specialization.value.trim(),
    phone: form.phone.value.trim(),
    email: form.email.value.trim(),
    workSchedule: form.workSchedule.value.trim()
  };

  try {
    const res = await fetch("/api/doctors", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newDoctor)
    });

    if (res.ok) {
      alert("เพิ่มข้อมูลสัตวแพทย์สำเร็จ!");
      closeModal("addDoctorModal");
      form.reset();
      loadDoctors(); // Refresh list
    } else {
      const err = await res.json();
      alert("เกิดข้อผิดพลาด: " + (err.message || "ไม่สามารถบันทึกได้"));
    }
  } catch (e) {
    // If backend isn't running, append locally to mock data
    const localNewDoc = {
      ...newDoctor,
      doctorId: Date.now(),
      room: "ห้องตรวจ 1",
      experience: "1 ปี",
      education: "คณะสัตวแพทยศาสตร์",
      statusToday: "available",
      statusText: "ออกตรวจวันนี้",
      timeToday: "09:00 - 17:00 น.",
      avatar: "images/dr-nichapa.jpg"
    };
    doctorsList.unshift(localNewDoc);
    renderDoctors(doctorsList);
    updateDoctorCount(doctorsList.length);
    closeModal("addDoctorModal");
    alert("เพิ่มข้อมูลสัตวแพทย์ในรายการเรียบร้อยแล้ว!");
  }
}
