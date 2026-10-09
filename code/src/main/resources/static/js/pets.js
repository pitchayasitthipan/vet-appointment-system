// PawCare - Pet Management

let petsList = [];
let currentPetId = null;

// สัตว์ที่กำลังแก้ไข (null = ฟอร์มอยู่ในโหมดเพิ่มสัตว์ใหม่)
let editingPetId = null;

// เจ้าของที่กำลังดู: Controller ใส่ไว้ใน <body data-owner-id> (ลูกค้ามาจาก session)
const CURRENT_OWNER_ID = Number(document.body.dataset.ownerId);

// REST API ของโมดูล Pet
const PETS_API = "/api/v1/pets";

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

        const response =
            await fetch(`${PETS_API}/owner/${CURRENT_OWNER_ID}`);

        if (!response.ok) {
            throw new Error("Failed to load pets");
        }

        petsList = await response.json();

        renderPets(petsList);

    } catch (error) {

        console.error("Error loading pets:", error);

        container.innerHTML = `
            <div class="pet-empty-state">
                <span class="icon i-warning icon-lg icon-pink"></span>
                <h3>ไม่สามารถโหลดข้อมูลได้</h3>
                <p>กรุณาตรวจสอบการเชื่อมต่อกับระบบ</p>
            </div>
        `;
    }
}


// ==================== RENDER PETS ====================

function renderPets(pets) {

    const container =
        document.getElementById("petCardsContainer");

    const count =
        document.getElementById("petCountText");

    if (!container) return;

    if (count) {
        count.innerText =
            `พบสัตว์เลี้ยงทั้งหมด ${pets.length} ตัว`;
    }

    if (!pets || pets.length === 0) {

        container.innerHTML = `
            <div class="pet-empty-state">
                <span class="icon i-paw-print icon-lg icon-pink"></span>
                <h3>ยังไม่มีข้อมูลสัตว์เลี้ยง</h3>
                <p>กด "เพิ่มสัตว์เลี้ยง" เพื่อเพิ่มข้อมูล</p>
            </div>
        `;

        return;
    }


    // ชื่อ class ตรงกับที่มีใน pets.css (pet-card-header, pet-card-details, ...)
    container.innerHTML = pets.map(pet => `

        <article class="pet-card">

            <div class="pet-card-header">

                <div class="pet-card-icon">
                    ${getPetIcon(pet.species)}
                </div>

                <div class="pet-card-heading">
                    <h3>${escapeHtml(pet.name || "-")}</h3>

                    <span class="pet-species">
                        ${escapeHtml(speciesLabel(pet.species))}
                    </span>
                </div>

                <span class="pet-card-id">
                    #${pet.petId}
                </span>

            </div>


            <div class="pet-card-details">

                <div>
                    <span>สายพันธุ์</span>
                    <strong>${escapeHtml(pet.breed || "-")}</strong>
                </div>

                <div>
                    <span>เพศ</span>
                    <strong>${escapeHtml(genderLabel(pet.gender))}</strong>
                </div>

                <div>
                    <span>น้ำหนัก</span>
                    <strong>${pet.weight != null ? pet.weight + " กก." : "-"}</strong>
                </div>

                <div>
                    <span>อายุ</span>
                    <strong>${escapeHtml(petAge(pet.birthDate))}</strong>
                </div>

            </div>


            <div class="pet-card-actions">

                <button type="button" onclick="viewPet(${pet.petId})">
                    ดูรายละเอียด
                </button>

                <button type="button" onclick="editPet(${pet.petId})">
                    แก้ไข
                </button>

                <button type="button" class="danger-button"
                        onclick="deletePet(${pet.petId})">
                    ลบ
                </button>

            </div>

        </article>

    `).join("");
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

        document.getElementById("detailPetIcon").innerHTML =
            getPetIcon(pet.species);

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

    editingPetId = null;

    document.getElementById("addPetForm").reset();

    setPetFormMode(false);

    openModal("addPetModal");
}


async function handleAddPet(event) {

    event.preventDefault();

    const form = event.target;

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

        ownerId: CURRENT_OWNER_ID
    };

    const isEdit = editingPetId !== null;


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


// ==================== SEARCH ====================

function searchPets() {

    const input =
        document.getElementById("petSearchInput");

    if (!input) return;

    const keyword =
        input.value.trim().toLowerCase();


    // ค้นได้ทั้งชื่อ สายพันธุ์ และประเภท (พิมพ์ "แมว" หรือ "cat" ก็เจอ)
    const filtered =
        petsList.filter(pet => {

            const name =
                (pet.name || "").toLowerCase();

            const species =
                (pet.species || "").toLowerCase();

            const speciesThai =
                speciesLabel(pet.species);

            const breed =
                (pet.breed || "").toLowerCase();

            return (
                name.includes(keyword) ||
                species.includes(keyword) ||
                speciesThai.includes(keyword) ||
                breed.includes(keyword)
            );
        });


    renderPets(filtered);
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

    return `<span class="icon ${iconName} icon-lg icon-pink"></span>`;
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

    if (!isoDate) return "-";

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
                searchPets
            );
        }


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