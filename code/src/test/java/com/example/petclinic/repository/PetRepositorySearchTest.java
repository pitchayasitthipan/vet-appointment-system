package com.example.petclinic.repository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.example.petclinic.domain.entity.Pet;
import com.example.petclinic.domain.entity.PetOwner;
import com.example.petclinic.domain.entity.PetOwnerDetail;

// ทดสอบการค้นหาสัตว์ทั้งคลินิก (PetSpecifications) กับฐานข้อมูลจริง
// ข้อมูลทดสอบใช้นามสกุลเจ้าของ "nametest" ที่ไม่ซ้ำข้อมูลจริง แต่ละ test rollback ให้เอง
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PetRepositorySearchTest {

    private static final String OWNER_LAST_NAME = "nametest";

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private PetOwnerRepository petOwnerRepository;

    @BeforeEach
    void setUp() {
        PetOwner owner = new PetOwner();
        owner.setFirstName("ชื่อจริง");
        owner.setLastName(OWNER_LAST_NAME);
        owner.setEmail("nametest@example.com");
        owner.setPhone("0999999911");

        PetOwnerDetail detail = new PetOwnerDetail();
        detail.setEmergencyContactPhone("0987654321");
        owner.setPetOwnerDetail(detail);
        petOwnerRepository.save(owner);

        // 12 ตัว: เกิน 1 หน้า (หน้าละ 8) ตัวที่ต้องค้นหาอยู่ท้ายสุด
        for (int i = 1; i <= 10; i++) {
            petRepository.save(pet("Puppy" + i, "Dog", "Thai", owner));
        }
        petRepository.save(pet("Kitty", "Cat", "Persian", owner));
        petRepository.save(pet("Bunny", "Other", "Rabbit", owner));
    }

    private Pet pet(String name, String species, String breed, PetOwner owner) {
        Pet pet = new Pet();
        pet.setName(name);
        pet.setSpecies(species);
        pet.setBreed(breed);
        pet.setPetOwner(owner);
        return pet;
    }

    private List<String> names(Page<Pet> page) {
        return page.getContent().stream().map(Pet::getName).toList();
    }

    @Test
    @DisplayName("ค้นชื่อสัตว์: เจอตัวที่อยู่หน้าหลังๆ ได้ (ค้นทั้งคลินิก ไม่ใช่เฉพาะหน้าแรก)")
    void search_byName_findsPetBeyondFirstPage() {
        Page<Pet> page = petRepository.findAll(
                PetSpecifications.search("Bunny", null), PageRequest.of(0, 8));

        assertThat(names(page)).containsExactly("Bunny");
    }

    @Test
    @DisplayName("ค้นชื่อเจ้าของ: เจอสัตว์ทุกตัวของเจ้าของ และแบ่งหน้าตามจำนวนที่ค้นเจอ")
    void search_byOwnerName_pagesOverMatches() {
        Page<Pet> page = petRepository.findAll(
                PetSpecifications.search(OWNER_LAST_NAME, null), PageRequest.of(0, 8));

        assertThat(page.getTotalElements()).isEqualTo(12);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("กรองประเภท: Cat/Other")
    void search_bySpecies() {
        assertThat(names(petRepository.findAll(
                PetSpecifications.search(OWNER_LAST_NAME, "Cat"), PageRequest.of(0, 8))))
                .containsExactly("Kitty");
        assertThat(names(petRepository.findAll(
                PetSpecifications.search(OWNER_LAST_NAME, "Other"), PageRequest.of(0, 8))))
                .containsExactly("Bunny");
    }

    @Test
    @DisplayName("ค้นด้วยคำภาษาไทย 'แมว' และสายพันธุ์ ได้ผลถูกต้อง")
    void search_byThaiSpeciesAndBreed() {
        assertThat(names(petRepository.findAll(
                PetSpecifications.search("แมว", "Cat"), PageRequest.of(0, 50))))
                .contains("Kitty");
        assertThat(names(petRepository.findAll(
                PetSpecifications.search("persian", null), PageRequest.of(0, 50))))
                .contains("Kitty");
    }
}