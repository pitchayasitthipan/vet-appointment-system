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
    rating: 4.9,
    reviews: 128,
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
    rating: 4.8,
    reviews: 96,
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
    rating: 4.7,
    reviews: 74,
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
    rating: 4.6,
    reviews: 58,
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
    rating: 4.8,
    reviews: 82,
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
let activeDayIndex = 2; // Default to Wednesday (พ. 25)
const daysData = [
  { name: "จ.", num: 23, fullDay: "MONDAY", thDay: "จันทร์" },
  { name: "อ.", num: 24, fullDay: "TUESDAY", thDay: "อังคาร" },
  { name: "พ.", num: 25, fullDay: "WEDNESDAY", thDay: "พุธ" },
  { name: "พฤ.", num: 26, fullDay: "THURSDAY", thDay: "พฤหัสบดี" },
  { name: "ศ.", num: 27, fullDay: "FRIDAY", thDay: "ศุกร์" },
  { name: "ส.", num: 28, fullDay: "SATURDAY", thDay: "เสาร์" },
  { name: "อา.", num: 29, fullDay: "SUNDAY", thDay: "อาทิตย์" }
];

// Schedule slots data for days
const dailyScheduleSlots = {
  2: [ // Wednesday (พ. 25)
    { time: "09:00 - 12:00", name: "สพ.ญ. ณิชาภา วงศ์วิริยะ", spec: "อายุรศาสตร์ / โรคผิวหนัง", room: "ห้องตรวจ 1", status: "on", avatar: "images/dr-nichapa.jpg" },
    { time: "09:00 - 16:00", name: "สพ.ญ. ปรียาภรณ์ ตั้งพงษ์ศิริ", spec: "สัตว์เลี้ยงพิเศษ", room: "ห้องตรวจ 2", status: "on", avatar: "images/dr-preeyaporn.jpg" },
    { time: "10:00 - 13:00", name: "น.สพ. กิตติภัทร สินธวรกุล", spec: "ศัลยกรรม / กระดูกและข้อ", room: "ห้องผ่าตัด", status: "on", avatar: "images/dr-kittiphat.jpg" },
    { time: "13:00 - 17:00", name: "สพ.ญ. อรอนงค์ จันทร์ไพศาล", spec: "ทันตกรรม / วัคซีน", room: "ห้องตรวจ 1", status: "on", avatar: "images/dr-ohanong.jpg" },
    { time: "13:00 - 16:00", name: "น.สพ. ธนวัฒน์ อภิญญากุล", spec: "อายุรศาสตร์ / หัวใจ", room: "ห้องตรวจ 3", status: "off", avatar: "images/dr-thanawat.jpg" }
  ]
};

// Initialize
document.addEventListener("DOMContentLoaded", () => {
  renderDaysStrip();
  loadDoctors();
  setupEventListeners();
  renderScheduleSlots(activeDayIndex);
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
            <div class="doc-rating">
              <span>⭐</span>
              <span>${doc.rating || '4.8'}</span>
              <span class="doc-reviews">(${doc.reviews || '80'} รีวิว)</span>
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

// Render Days Strip
function renderDaysStrip() {
  const container = document.getElementById("daysStrip");
  if (!container) return;

  container.innerHTML = daysData.map((d, idx) => `
    <div class="day-pill ${idx === activeDayIndex ? 'active' : ''}" onclick="selectDay(${idx})">
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

// Render Slots for Right Column
function renderScheduleSlots(dayIdx) {
  const container = document.getElementById("timeSlotsContainer");
  if (!container) return;

  const slots = dailyScheduleSlots[dayIdx] || [
    { time: "09:00 - 12:00", name: "สพ.ญ. ณิชาภา วงศ์วิริยะ", spec: "อายุรศาสตร์ / โรคผิวหนัง", room: "ห้องตรวจ 1", status: "on", avatar: "images/dr-nichapa.jpg" },
    { time: "10:00 - 13:00", name: "น.สพ. กิตติภัทร สินธวรกุล", spec: "ศัลยกรรม / กระดูกและข้อ", room: "ห้องผ่าตัด", status: "on", avatar: "images/dr-kittiphat.jpg" },
    { time: "13:00 - 17:00", name: "สพ.ญ. อรอนงค์ จันทร์ไพศาล", spec: "ทันตกรรม / วัคซีน", room: "ห้องตรวจ 1", status: "on", avatar: "images/dr-ohanong.jpg" }
  ];

  container.innerHTML = slots.map(slot => `
    <div class="slot-item">
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

  const filterHandler = () => {
    const query = (searchInput ? searchInput.value : "").trim().toLowerCase();
    const spec = specFilter ? specFilter.value : "";

    const filtered = doctorsList.filter(doc => {
      const matchQuery = !query || 
        doc.firstName.toLowerCase().includes(query) ||
        doc.lastName.toLowerCase().includes(query) ||
        (doc.specialization && doc.specialization.toLowerCase().includes(query)) ||
        (doc.nameEn && doc.nameEn.toLowerCase().includes(query));

      const matchSpec = !spec || (doc.specialization && doc.specialization.includes(spec));

      return matchQuery && matchSpec;
    });

    renderDoctors(filtered);
    updateDoctorCount(filtered.length);
  };

  if (searchInput) searchInput.addEventListener("input", filterHandler);
  if (specFilter) specFilter.addEventListener("change", filterHandler);
  if (dayFilter) dayFilter.addEventListener("change", filterHandler);
}

function updateDoctorCount(count) {
  const el = document.getElementById("doctorCount");
  if (el) el.innerText = `พบสัตวแพทย์ทั้งหมด ${count} ท่าน`;
}

// View Doctor Schedule Modal
function viewDoctorSchedule(id) {
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
      rating: 5.0,
      reviews: 1,
      room: "ห้องตรวจ 1",
      experience: "1 ปี",
      education: "คณะสัตวแพทยศาสตร์",
      statusToday: "available",
      statusText: "ออกตรวจวันนี้",
      timeToday: "09:00 - 17:00 น.",
      avatar: "/images/dr-nichapa.jpg"
    };
    doctorsList.unshift(localNewDoc);
    renderDoctors(doctorsList);
    updateDoctorCount(doctorsList.length);
    closeModal("addDoctorModal");
    alert("เพิ่มข้อมูลสัตวแพทย์ในรายการเรียบร้อยแล้ว!");
  }
}
