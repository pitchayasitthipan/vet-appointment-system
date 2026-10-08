// PawCare - Pet Management

let petsList = [];
let currentPetId = null;

// Change this if the logged-in owner's ID is available later.
const CURRENT_OWNER_ID = 1;


// ==================== LOAD PETS ====================

async function loadPets() {

    const container = document.getElementById("petCardsContainer");

    if (!container) return;

    try {

        const response =
            await fetch(`/api/pets/owner/${CURRENT_OWNER_ID}`);

        if (!response.ok) {
            throw new Error("Failed to load pets");
        }

        petsList = await response.json();

        renderPets(petsList);

    } catch (error) {

        console.error("Error loading pets:", error);

        container.innerHTML = `
            <div class="pet-empty">
                <div class="pet-empty-icon">🐾</div>
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
        document.getElementById("petCount");

    if (!container) return;

    if (count) {
        count.innerText =
            `พบสัตว์เลี้ยงทั้งหมด ${pets.length} ตัว`;
    }

    if (!pets || pets.length === 0) {

        container.innerHTML = `
            <div class="pet-empty">
                <div class="pet-empty-icon">🐾</div>
                <h3>ยังไม่มีข้อมูลสัตว์เลี้ยง</h3>
                <p>กด "เพิ่มสัตว์เลี้ยง" เพื่อเพิ่มข้อมูล</p>
            </div>
        `;

        return;
    }


    container.innerHTML = pets.map(pet => `

        <article class="pet-card">

            <div class="pet-card-icon">
                ${getPetIcon(pet.species)}
            </div>

            <div class="pet-card-content">

                <div class="pet-card-header">

                    <div>
                        <h3>${escapeHtml(pet.name || "-")}</h3>

                        <span class="pet-species">
                            ${escapeHtml(pet.species || "-")}
                        </span>
                    </div>

                    <span class="pet-id">
                        #${pet.petId}
                    </span>

                </div>


                <div class="pet-info-grid">

                    <div>
                        <span>สายพันธุ์</span>
                        <strong>
                            ${escapeHtml(pet.breed || "-")}
                        </strong>
                    </div>

                    <div>
                        <span>เพศ</span>
                        <strong>
                            ${escapeHtml(pet.gender || "-")}
                        </strong>
                    </div>

                    <div>
                        <span>น้ำหนัก</span>
                        <strong>
                            ${pet.weight != null
                                ? pet.weight + " kg"
                                : "-"}
                        </strong>
                    </div>

                </div>


                <div class="pet-card-actions">

                    <button
                        class="btn-view-pet"
                        onclick="viewPet(${pet.petId})">
                        ดูรายละเอียด
                    </button>

                    <button
                        class="btn-card-edit"
                        onclick="editPet(${pet.petId})">
                        แก้ไข
                    </button>

                    <button
                        class="btn-card-delete"
                        onclick="deletePet(${pet.petId})">
                        ลบ
                    </button>

                </div>

            </div>

        </article>

    `).join("");
}


// ==================== VIEW PET ====================

async function viewPet(id) {

    try {

        const response =
            await fetch(`/api/pets/${id}`);

        if (!response.ok) {
            throw new Error("Pet not found");
        }

        const pet = await response.json();

        currentPetId = pet.petId;

        document.getElementById("detailName").innerText =
            pet.name || "-";

        document.getElementById("detailSpecies").innerText =
            pet.species || "-";

        document.getElementById("detailBreed").innerText =
            pet.breed || "-";

        document.getElementById("detailGender").innerText =
            pet.gender || "-";

        document.getElementById("detailBirthDate").innerText =
            pet.birthDate || "-";

        document.getElementById("detailWeight").innerText =
            pet.weight != null
                ? `${pet.weight} kg`
                : "-";

        document.getElementById("detailMicrochip").innerText =
            pet.microchipNumber || "-";

        document.getElementById("detailAvatar").innerText =
            getPetIcon(pet.species);

        document.getElementById("detailEditButton").onclick =
            () => editPet(pet.petId);

        document.getElementById("detailDeleteButton").onclick =
            () => deletePet(pet.petId);

        openModal("petDetailModal");

    } catch (error) {

        console.error(error);

        alert("ไม่สามารถโหลดข้อมูลสัตว์เลี้ยงได้");
    }
}


// ==================== ADD PET ====================

function openAddPetModal() {

    const modal =
        document.getElementById("addPetModal");

    if (modal) {
        modal.classList.add("show");
    }
}


async function handleAddPet(event) {

    event.preventDefault();

    const form = event.target;

    const newPet = {

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

        ownerId:
            Number(form.ownerId.value)
    };


    try {

        const response =
            await fetch("/api/pets", {

                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify(newPet)
            });


        if (!response.ok) {

            const errorData =
                await response.json().catch(() => ({}));

            throw new Error(
                errorData.message ||
                "ไม่สามารถเพิ่มสัตว์เลี้ยงได้"
            );
        }


        alert("เพิ่มข้อมูลสัตว์เลี้ยงสำเร็จ!");

        closeModal("addPetModal");

        form.reset();

        await loadPets();

    } catch (error) {

        console.error(error);

        alert(error.message);
    }
}


// ==================== EDIT PET ====================

async function editPet(id) {

    try {

        const response =
            await fetch(`/api/pets/${id}`);

        if (!response.ok) {
            throw new Error("Pet not found");
        }

        const pet = await response.json();

        const name =
            prompt("ชื่อสัตว์เลี้ยง:", pet.name || "");

        if (name === null) return;


        const breed =
            prompt("สายพันธุ์:", pet.breed || "");

        if (breed === null) return;


        const weightInput =
            prompt(
                "น้ำหนัก (kg):",
                pet.weight != null ? pet.weight : ""
            );

        if (weightInput === null) return;


        const updatedPet = {

            name: name.trim(),

            species: pet.species,

            breed: breed.trim(),

            gender: pet.gender,

            birthDate: pet.birthDate,

            weight:
                weightInput.trim()
                    ? Number(weightInput)
                    : null,

            microchipNumber:
                pet.microchipNumber,

            ownerId: pet.ownerId
        };


        const updateResponse =
            await fetch(`/api/pets/${id}`, {

                method: "PUT",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify(updatedPet)
            });


        if (!updateResponse.ok) {

            const errorData =
                await updateResponse.json().catch(() => ({}));

            throw new Error(
                errorData.message ||
                "ไม่สามารถแก้ไขข้อมูลได้"
            );
        }


        alert("แก้ไขข้อมูลสัตว์เลี้ยงสำเร็จ!");

        closeModal("petDetailModal");

        await loadPets();

    } catch (error) {

        console.error(error);

        alert(error.message);
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
            await fetch(`/api/pets/${id}`, {

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


    const filtered =
        petsList.filter(pet => {

            const name =
                (pet.name || "").toLowerCase();

            const species =
                (pet.species || "").toLowerCase();

            const breed =
                (pet.breed || "").toLowerCase();

            return (
                name.includes(keyword) ||
                species.includes(keyword) ||
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

    const value =
        (species || "").toLowerCase();

    if (value.includes("cat") ||
        value.includes("แมว")) {
        return "🐱";
    }

    if (value.includes("dog") ||
        value.includes("หมา") ||
        value.includes("สุนัข")) {
        return "🐶";
    }

    if (value.includes("bird") ||
        value.includes("นก")) {
        return "🐦";
    }

    if (value.includes("rabbit") ||
        value.includes("กระต่าย")) {
        return "🐰";
    }

    return "🐾";
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


        document
            .querySelectorAll(".modal-overlay")
            .forEach(modal => {

                modal.addEventListener(
                    "click",
                    event => {

                        if (event.target === modal) {
                            modal.classList.remove("show");
                        }

                    }
                );

            });


        loadPets();
    }
);