// PawCare - Pet Management

let petsList = [];
let currentPetId = null;

// สัตว์ที่กำลังแก้ไข (null = ฟอร์มอยู่ในโหมดเพิ่มสัตว์ใหม่)
let editingPetId = null;

// ปุ่มกรองที่เลือกอยู่: all | Dog | Cat | Other
let activeFilter = "all";

// หน้าปัจจุบัน (ใช้เฉพาะเจ้าหน้าที่ดูสัตว์ทั้งหมด)
let currentPage = 0;

// เจ้าของที่กำลังดู: Controller ใส่ไว้ใน <body data-owner-id> (ลูกค้ามาจาก session)
// ว่าง = เจ้าหน้าที่ดูสัตว์ทั้งหมดในคลินิก
const CURRENT_OWNER_ID = Number(document.body.dataset.ownerId) || null;
const IS_STAFF = document.body.dataset.staff === "true";
const ALL_PETS_MODE = IS_STAFF && CURRENT_OWNER_ID === null;

// REST API ของโมดูล Pet
const PETS_API = "/api/v1/pets";

// เจ้าหน้าที่ดูสัตว์ทั้งหมด: หน้าละ 8 ตัว (การ์ด 4 ใบ x 2 แถว)
const PAGE_SIZE = 8;

// ในฐานข้อมูลเก็บเป็นภาษาอังกฤษ แสดงผลเป็นภาษาไทย
const SPECIES_LABELS = {
    Dog: "สุนัข",
    Cat: "แมว",
    Other: "อื่น ๆ"
};

const GENDER_LABELS = {
    Male: "เพศผู้",
    Female: "เพศเมีย"
};

const THAI_MONTHS = [
    "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.",
    "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค."
];


// ==================== LOAD PETS ====================

async function loadPets() {

    const container = document.getElementById("petCardsContainer");

    if (!container) return;

    try {

        if (ALL_PETS_MODE) {
            await loadAllPetsPage();
        } else {
            await loadOwnerPets();
        }

        applyFilters();

    } catch (error) {

        console.error("Error loading pets:", error);

        container.innerHTML = `
            <div class="pet-empty-state">
                <span class="icon i-warning icon-lg"></span>
                <h3>ไม่สามารถโหลดข้อมูลได้</h3>
                <p>กรุณาตรวจสอบการเชื่อมต่อกับระบบ</p>
            </div>
        `;
    }
}


// สัตว์ของเจ้าของคนเดียว (ลูกค้า หรือเจ้าหน้าที่ที่เลือกแฟ้มแล้ว)
async function loadOwnerPets() {

    const response =
        await fetch(`${PETS_API}/owner/${CURRENT_OWNER_ID}`);

    if (!response.ok) {
        throw new Error("Failed to load pets");
    }

    petsList = await response.json();

    renderOwnerStats(petsList);
}


// เจ้าหน้าที่ดูสัตว์ทั้งหมด: ดึงทีละหน้าจาก GET /api/v1/pets?page=&size=
async function loadAllPetsPage() {

    const response =
        await fetch(`${PETS_API}?page=${currentPage}&size=${PAGE_SIZE}&sort=petId,asc`);

    if (!response.ok) {
        throw new Error("Failed to load pets");
    }

    const page = await response.json();

    // ลบตัวสุดท้ายของหน้าสุดท้ายแล้วหน้าว่าง -> ถอยกลับหนึ่งหน้า
    if (page.content.length === 0 && currentPage > 0) {
        currentPage--;
        return loadAllPetsPage();
    }

    petsList = page.content;

    await renderClinicStats(page.totalElements);

    renderPager(page.totalElements, page.totalPages);
}


// ==================== STATS ====================

function statItem(icon, tone, number, label) {

    return `
        <div class="pet-stat">
            <span class="pet-stat-icon ${tone}"><span class="icon ${icon} icon-sm"></span></span>
            <span><b>${number}</b> ${label}</span>
        </div>
    `;
}


function countBySpecies(pets) {

    return {
        Dog: pets.filter(p => p.species === "Dog").length,
        Cat: pets.filter(p => p.species === "Cat").length,
        Other: pets.filter(p => p.species !== "Dog" && p.species !== "Cat").length
    };
}


function renderOwnerStats(pets) {

    const counts = countBySpecies(pets);

    document.getElementById("petStats").innerHTML =
        statItem("i-paw-print", "tone-all", pets.length, "ตัวทั้งหมด") +
        statItem("i-dog", "tone-dog", counts.Dog, "สุนัข") +
        statItem("i-cat", "tone-cat", counts.Cat, "แมว") +
        statItem("i-paw-print", "tone-other", counts.Other, "อื่น ๆ");

    // ใส่จำนวนในปุ่มกรอง "ทั้งหมด"
    document.querySelector('.pet-filter[data-filter="all"]').innerText =
        `ทั้งหมด ${pets.length}`;
}


async function renderClinicStats(totalPets) {

    // จำนวนแฟ้มเจ้าของ: ใช้ totalElements จาก API เจ้าของ (เจ้าหน้าที่เรียกได้)
    let ownerCount = "-";

    try {
        const response = await fetch("/api/v1/owners?size=1");

        if (response.ok) {
            ownerCount = (await response.json()).totalElements;
        }
    } catch (error) {
        console.error(error);
    }

    document.getElementById("petStats").innerHTML =
        statItem("i-paw-print", "tone-all", totalPets, "ตัวในระบบ") +
        statItem("i-users", "tone-cat", ownerCount, "แฟ้มเจ้าของ");
}


// ==================== FILTER & SEARCH ====================

// รวมปุ่มกรองกับช่องค้นหา แล้วแสดงผล
function applyFilters() {

    const input =
        document.getElementById("petSearchInput");

    const keyword =
        input ? input.value.trim().toLowerCase() : "";

    const filtered =
        petsList.filter(pet => {

            const matchesFilter =
                activeFilter === "all" ||
                (activeFilter === "Other"
                    ? pet.species !== "Dog" && pet.species !== "Cat"
                    : pet.species === activeFilter);

            // ค้นได้ทั้งชื่อ สายพันธุ์ ประเภท (ไทย/อังกฤษ) และชื่อเจ้าของ
            const text = [
                pet.name,
                pet.breed,
                pet.species,
                speciesLabel(pet.species),
                pet.ownerName
            ].join(" ").toLowerCase();

            return matchesFilter && text.includes(keyword);
        });

    renderPets(filtered);
}


function setFilter(filter) {

    activeFilter = filter;

    document.querySelectorAll(".pet-filter").forEach(button => {
        button.classList.toggle("active", button.dataset.filter === filter);
    });

    applyFilters();
}


// ==================== RENDER PETS ====================

function renderPets(pets) {

    const container =
        document.getElementById("petCardsContainer");

    const count =
        document.getElementById("petCountText");

    if (!container) return;

    const isFiltering =
        activeFilter !== "all" ||
        document.getElementById("petSearchInput").value.trim() !== "";

    if (count) {
        count.innerText = ALL_PETS_MODE
            ? `แสดง ${pets.length} ตัวในหน้านี้`
            : `พบสัตว์เลี้ยง ${pets.length} ตัว`;
    }

    // ยังไม่มีสัตว์เลยสักตัว (ไม่ได้กรอง)
    if (petsList.length === 0) {

        container.innerHTML = `
            <div class="pet-empty-state">
                <span class="icon i-paw-print icon-lg"></span>
                <h3>ยังไม่มีข้อมูลสัตว์เลี้ยง</h3>
                <p>${ALL_PETS_MODE
                    ? "เพิ่มสัตว์เลี้ยงได้จากแฟ้มเจ้าของแต่ละคน"
                    : 'กด "เพิ่มสัตว์เลี้ยง" เพื่อเพิ่มตัวแรก'}</p>
            </div>
        `;

        return;
    }

    const cards =
        pets.map(renderPetCard).join("");

    const empty = pets.length === 0
        ? `<div class="pet-empty-state">
               <span class="icon i-magnifying-glass icon-lg"></span>
               <h3>ไม่พบสัตว์เลี้ยงที่ตรงกับการค้นหา</h3>
               <p>ลองเปลี่ยนคำค้นหรือเลือก "ทั้งหมด"</p>
           </div>`
        : "";

    // ช่องเพิ่มสัตว์ท้ายรายการ: เฉพาะตอนดูแฟ้มเดียวและไม่ได้กรอง
    const addTile = !ALL_PETS_MODE && !isFiltering
        ? `<button type="button" class="pet-add-tile" onclick="openAddPetModal()">
               <span class="pet-add-icon"><span class="icon i-plus icon-lg"></span></span>
               เพิ่มสัตว์เลี้ยงตัวใหม่
           </button>`
        : "";

    container.innerHTML = cards + empty + addTile;
}


function renderPetCard(pet) {

    const tone = speciesTone(pet.species);

    // เจ้าหน้าที่ดูสัตว์ทั้งหมด: บอกว่าเป็นของแฟ้มไหน กดแล้วเปิดแฟ้ม
    const ownerLine = ALL_PETS_MODE
        ? `<a class="pet-owner-line" href="/owners/${pet.ownerId}">
               <span class="icon i-user icon-sm"></span>
               ${escapeHtml(pet.ownerName || "-")} · แฟ้ม #${String(pet.ownerId).padStart(4, "0")}
           </a>`
        : "";

    return `

        <article class="pet-profile ${tone}">

            <span class="pet-profile-id">#${pet.petId}</span>

            <div class="pet-avatar">
                ${getPetIcon(pet.species)}
            </div>

            <h3>${escapeHtml(pet.name || "-")}</h3>

            ${ownerLine}

            <p class="pet-meta">
                ${escapeHtml(speciesLabel(pet.species))} · ${escapeHtml(pet.breed || "ไม่ระบุสายพันธุ์")}
            </p>

            <div class="pet-tags">
                <span>${escapeHtml(pet.gender ? genderLabel(pet.gender) : "ไม่ระบุเพศ")}</span>
                <span>${escapeHtml(petAge(pet.birthDate))}</span>
                <span>${pet.weight != null ? pet.weight + " กก." : "ไม่ระบุน้ำหนัก"}</span>
            </div>

            <div class="pet-profile-actions">

                <a class="pet-book-btn" href="/appointments/new?ownerId=${pet.ownerId}">
                    <span class="icon i-calendar-plus icon-sm"></span> จองนัด
                </a>

                <button type="button" class="pet-icon-btn" title="ดูรายละเอียด"
                        aria-label="ดูรายละเอียด ${escapeHtml(pet.name)}"
                        onclick="viewPet(${pet.petId})">
                    <span class="icon i-eye icon-sm"></span>
                </button>

                <button type="button" class="pet-icon-btn" title="แก้ไข"
                        aria-label="แก้ไข ${escapeHtml(pet.name)}"
                        onclick="editPet(${pet.petId})">
                    <span class="icon i-pencil-simple icon-sm"></span>
                </button>

                <button type="button" class="pet-icon-btn danger" title="ลบ"
                        aria-label="ลบ ${escapeHtml(pet.name)}"
                        onclick="deletePet(${pet.petId})">
                    <span class="icon i-trash icon-sm"></span>
                </button>

            </div>

        </article>
    `;
}


// ==================== PAGER ====================

function renderPager(totalElements, totalPages) {

    const pager = document.getElementById("petPager");

    if (!pager) return;

    pager.hidden = totalPages <= 1;

    if (totalPages <= 1) return;

    const from = currentPage * PAGE_SIZE + 1;
    const to = Math.min(from + PAGE_SIZE - 1, totalElements);

    let pages = "";

    for (let i = 0; i < totalPages; i++) {
        pages += i === currentPage
            ? `<span class="active">${i + 1}</span>`
            : `<a href="#" onclick="goToPage(${i}); return false;">${i + 1}</a>`;
    }

    pager.innerHTML = `
        <span>แสดง ${from}–${to} จาก ${totalElements} ตัว</span>
        <div class="pager">
            ${currentPage > 0
                ? `<a href="#" aria-label="หน้าก่อน" onclick="goToPage(${currentPage - 1}); return false;"><span class="icon i-caret-left icon-sm"></span></a>`
                : ""}
            ${pages}
            ${currentPage < totalPages - 1
                ? `<a href="#" aria-label="หน้าถัดไป" onclick="goToPage(${currentPage + 1}); return false;"><span class="icon i-caret-right icon-sm"></span></a>`
                : ""}
        </div>
    `;
}


async function goToPage(page) {

    currentPage = page;

    await loadPets();

    window.scrollTo({ top: 0, behavior: "smooth" });
}


// ==================== VIEW PET ====================

async function viewPet(id) {

    try {

        const response =
            await fetch(`${PETS_API}/${id}`);

        if (!response.ok) {
            throw new Error("Pet not found");
        }

        const pet = await response.json();

        currentPetId = pet.petId;

        const icon = document.getElementById("detailPetIcon");
        icon.className = `modal-icon ${speciesTone(pet.species)}`;
        icon.innerHTML = getPetIcon(pet.species);

        document.getElementById("detailPetName").innerText =
            pet.name || "-";

        document.getElementById("detailPetSpecies").innerText =
            speciesLabel(pet.species);

        document.getElementById("detailPetBreed").innerText =
            pet.breed || "-";

        document.getElementById("detailPetGender").innerText =
            genderLabel(pet.gender);

        document.getElementById("detailPetBirthDate").innerText =
            pet.birthDate
                ? `${formatThaiDate(pet.birthDate)} (${petAge(pet.birthDate)})`
                : "-";

        document.getElementById("detailPetWeight").innerText =
            pet.weight != null
                ? `${pet.weight} กก.`
                : "-";

        document.getElementById("detailPetMicrochip").innerText =
            pet.microchipNumber || "-";

        openModal("petDetailModal");

    } catch (error) {

        console.error(error);

        alert("ไม่สามารถโหลดข้อมูลสัตว์เลี้ยงได้");
    }
}


// ==================== ADD / EDIT FORM ====================

// ฟอร์มเดียวใช้ทั้งเพิ่มและแก้ไข: เปลี่ยนแค่หัวข้อกับปุ่มบันทึก
function setPetFormMode(isEdit) {

    document.getElementById("addPetEyebrow").innerText =
        isEdit ? "EDIT COMPANION" : "NEW COMPANION";

    document.getElementById("addPetTitle").innerText =
        isEdit ? "แก้ไขข้อมูลสัตว์เลี้ยง" : "เพิ่มสัตว์เลี้ยง";

    document.getElementById("addPetSubtitle").innerText =
        isEdit
            ? "แก้ไขข้อมูลแล้วกดบันทึก"
            : "กรอกข้อมูลพื้นฐานของสัตว์เลี้ยงของคุณ";

    document.getElementById("addPetSubmit").innerText =
        isEdit ? "บันทึกการแก้ไข" : "บันทึกข้อมูล";
}


function openAddPetModal() {

    // เจ้าหน้าที่ดูสัตว์ทั้งหมด ต้องเลือกแฟ้มเจ้าของก่อน
    if (ALL_PETS_MODE) {
        window.location.href = "/owners/staff";
        return;
    }

    editingPetId = null;

    document.getElementById("addPetForm").reset();

    setPetFormMode(false);

    openModal("addPetModal");
}


async function handleAddPet(event) {

    event.preventDefault();

    const form = event.target;

    const isEdit = editingPetId !== null;

    // แก้ไข: ใช้เจ้าของเดิมของสัตว์ตัวนั้น / เพิ่ม: เจ้าของที่กำลังดูอยู่
    const editingPet = petsList.find(p => p.petId === editingPetId);

    const petData = {

        name: form.name.value.trim(),

        species: form.species.value.trim(),

        breed: form.breed.value.trim(),

        gender: form.gender.value,

        birthDate: form.birthDate.value || null,

        weight:
            form.weight.value
                ? Number(form.weight.value)
                : null,

        microchipNumber:
            form.microchipNumber.value.trim() || null,

        ownerId: isEdit && editingPet ? editingPet.ownerId : CURRENT_OWNER_ID
    };


    try {

        const response =
            await fetch(isEdit ? `${PETS_API}/${editingPetId}` : PETS_API, {

                method: isEdit ? "PUT" : "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify(petData)
            });


        if (!response.ok) {

            const errorData =
                await response.json().catch(() => ({}));

            throw new Error(
                errorData.message ||
                (isEdit
                    ? "ไม่สามารถแก้ไขข้อมูลได้"
                    : "ไม่สามารถเพิ่มสัตว์เลี้ยงได้")
            );
        }


        alert(isEdit
            ? "แก้ไขข้อมูลสัตว์เลี้ยงสำเร็จ!"
            : "เพิ่มข้อมูลสัตว์เลี้ยงสำเร็จ!");

        // มาจากหน้าจองนัด -> กลับไปจองนัดต่อ
        if (!isEdit && document.body.dataset.returnToAppointment === "true") {
            window.location.href = `/appointments/new?ownerId=${CURRENT_OWNER_ID}`;
            return;
        }

        closeModal("addPetModal");

        form.reset();

        editingPetId = null;

        await loadPets();

    } catch (error) {

        console.error(error);

        alert(error.message);
    }
}


// ==================== EDIT PET ====================

// เปิดฟอร์มเดียวกับตอนเพิ่ม พร้อมค่าเดิม แก้ได้ทุกช่อง
async function editPet(id) {

    try {

        const response =
            await fetch(`${PETS_API}/${id}`);

        if (!response.ok) {
            throw new Error("Pet not found");
        }

        const pet = await response.json();

        const form = document.getElementById("addPetForm");

        form.name.value = pet.name || "";
        form.species.value = pet.species || "";
        form.breed.value = pet.breed || "";
        form.gender.value = pet.gender || "";
        form.birthDate.value = pet.birthDate || "";
        form.weight.value = pet.weight != null ? pet.weight : "";
        form.microchipNumber.value = pet.microchipNumber || "";

        editingPetId = pet.petId;

        setPetFormMode(true);

        closeModal("petDetailModal");

        openModal("addPetModal");

    } catch (error) {

        console.error(error);

        alert("ไม่สามารถโหลดข้อมูลสัตว์เลี้ยงได้");
    }
}


// ==================== DELETE PET ====================

async function deletePet(id) {

    const pet =
        petsList.find(p => p.petId === id);

    const petName =
        pet ? pet.name : "สัตว์เลี้ยง";


    const confirmed =
        confirm(
            `ต้องการลบข้อมูล "${petName}" หรือไม่?`
        );

    if (!confirmed) return;


    try {

        const response =
            await fetch(`${PETS_API}/${id}`, {

                method: "DELETE"
            });


        if (!response.ok) {

            const errorData =
                await response.json().catch(() => ({}));

            throw new Error(
                errorData.message ||
                "ไม่สามารถลบข้อมูลได้"
            );
        }


        alert("ลบข้อมูลสัตว์เลี้ยงสำเร็จ!");

        closeModal("petDetailModal");

        await loadPets();

    } catch (error) {

        console.error(error);

        alert(error.message);
    }
}


// ==================== MODAL ====================

function openModal(id) {

    const modal =
        document.getElementById(id);

    if (modal) {
        modal.classList.add("show");
    }
}


function closeModal(id) {

    const modal =
        document.getElementById(id);

    if (modal) {
        modal.classList.remove("show");
    }
}


// ==================== HELPERS ====================

// สีตามประเภทสัตว์ (กำหนดใน pets.css): สุนัขส้มน้ำตาล แมวฟ้า อื่น ๆ เขียว
function speciesTone(species) {

    if (species === "Dog") return "tone-dog";
    if (species === "Cat") return "tone-cat";

    return "tone-other";
}


function getPetIcon(species) {

    // ไอคอน Phosphor (icons.css) ชุดเดียวกับหน้าอื่นของเว็บ
    // สัตว์มี 3 ประเภท: สุนัข (Dog) แมว (Cat) อื่น ๆ (Other ใช้รอยเท้า)
    let iconName = "i-paw-print";

    if (species === "Dog") {
        iconName = "i-dog";
    }

    if (species === "Cat") {
        iconName = "i-cat";
    }

    return `<span class="icon ${iconName} icon-lg"></span>`;
}


function speciesLabel(species) {

    return SPECIES_LABELS[species] || species || "-";
}


function genderLabel(gender) {

    return GENDER_LABELS[gender] || gender || "-";
}


// 2023-05-01 -> 1 พ.ค. 2566 (ปี พ.ศ. = ค.ศ. + 543) ให้เหมือนหน้าอื่นของเว็บ
function formatThaiDate(isoDate) {

    if (!isoDate) return "-";

    const [year, month, day] =
        isoDate.split("-").map(Number);

    return `${day} ${THAI_MONTHS[month - 1]} ${year + 543}`;
}


// อายุจากวันเกิด เช่น "2 ปี 5 เดือน" หรือ "3 เดือน"
function petAge(isoDate) {

    if (!isoDate) return "ไม่ระบุอายุ";

    const [year, month, day] =
        isoDate.split("-").map(Number);

    const today = new Date();

    let months =
        (today.getFullYear() - year) * 12 +
        (today.getMonth() + 1 - month);

    if (today.getDate() < day) {
        months--;
    }

    if (months < 1) return "น้อยกว่า 1 เดือน";

    const years = Math.floor(months / 12);
    const restMonths = months % 12;

    if (years === 0) return `${restMonths} เดือน`;
    if (restMonths === 0) return `${years} ปี`;

    return `${years} ปี ${restMonths} เดือน`;
}


function escapeHtml(value) {

    const div =
        document.createElement("div");

    div.textContent =
        value == null ? "" : String(value);

    return div.innerHTML;
}


// ==================== EVENTS ====================

document.addEventListener(
    "DOMContentLoaded",
    () => {

        const searchInput =
            document.getElementById("petSearchInput");

        if (searchInput) {

            searchInput.addEventListener(
                "input",
                applyFilters
            );
        }


        document
            .querySelectorAll(".pet-filter")
            .forEach(button => {

                button.addEventListener(
                    "click",
                    () => setFilter(button.dataset.filter)
                );
            });


        const addPetForm =
            document.getElementById("addPetForm");

        if (addPetForm) {

            addPetForm.addEventListener(
                "submit",
                handleAddPet
            );
        }


        loadPets();

        // มาจาก /pets/new -> เปิดฟอร์มเพิ่มสัตว์ทันที
        if (document.body.dataset.openAdd === "true") {
            openAddPetModal();
        }
    }
);