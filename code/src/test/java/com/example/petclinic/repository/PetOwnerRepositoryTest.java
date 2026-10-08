package com.example.petclinic.repository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import com.example.petclinic.domain.entity.PetOwner;
import com.example.petclinic.domain.entity.PetOwnerDetail;

// ทดสอบ Repository กับฐานข้อมูลจริง (ไม่ใช้ Mock)
// แต่ละ test จะ rollback ข้อมูลให้เอง ข้อมูลไม่ปนกัน
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PetOwnerRepositoryTest {

    @Autowired
    private PetOwnerRepository petOwnerRepository;

    // ใช้สั่ง flush/clear บังคับให้อ่านข้อมูลจากฐานข้อมูลจริง ไม่ใช่จาก cache
    @Autowired
    private TestEntityManager entityManager;

    private PetOwner owner;

    @BeforeEach
    void setUp() {
        owner = new PetOwner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setEmail("test@example.com");
        // ใช้เบอร์ที่ไม่น่าจะมีในข้อมูลจริง เพราะเบอร์ห้ามซ้ำ (unique)
        owner.setPhone("0999999901");

        PetOwnerDetail detail = new PetOwnerDetail();
        detail.setAddress("Khon Kaen");
        detail.setEmergencyContactName("familyMember");
        detail.setEmergencyContactPhone("0987654321");
        owner.setPetOwnerDetail(detail);
    }

    @Test
    @DisplayName("บันทึกเจ้าของสัตว์เลี้ยง: ได้ Id และ createdAt อัตโนมัติ")
    void testSave_GeneratesIdAndCreatedAt() {
        PetOwner saved = petOwnerRepository.save(owner);

        assertNotNull(saved.getOwnerId());
        assertNotNull(saved.getCreatedAt()); //
    }

    @Test
    @DisplayName("existsByEmail: มีอีเมลนี้ในระบบ / ไม่มี")
    void testExistsByEmail() {
        petOwnerRepository.save(owner);

        assertTrue(petOwnerRepository.existsByEmail("test@example.com"));
        assertFalse(petOwnerRepository.existsByEmail("other@example.com"));
    }

    @Test
    @DisplayName("existsByEmail: ตรวจแบบ Case-sensitive")
    void testExistsByEmail_CaseSensitive() {
        petOwnerRepository.save(owner);

        // อีเมลในระบบเป็นตัวเล็ก ค้นด้วยตัวใหญ่ต้องไม่เจอ
        assertFalse(petOwnerRepository.existsByEmail("TEST@example.com"));
    }

    @Test
    @DisplayName("findByEmail และ findByPhone: พบข้อมูล")
    void testFindByEmailAndPhone() {
        petOwnerRepository.save(owner);

        Optional<PetOwner> byEmail = petOwnerRepository.findByEmail("test@example.com");
        Optional<PetOwner> byPhone = petOwnerRepository.findByPhone("0999999901");

        assertTrue(byEmail.isPresent());
        assertEquals("John", byEmail.get().getFirstName());
        assertTrue(byPhone.isPresent());
        assertEquals("John", byPhone.get().getFirstName());
    }

    @Test
    @DisplayName("existsByPhone: มีเบอร์นี้ในระบบ / ไม่มี")
    void testExistsByPhone() {
        petOwnerRepository.save(owner);

        assertTrue(petOwnerRepository.existsByPhone("0999999901"));
        assertFalse(petOwnerRepository.existsByPhone("0800000000"));
    }

    @Test
    @DisplayName("One-to-One: ลบ PetOwner แล้ว PetOwnerDetail ถูกลบตาม (Cascade)")
    void testDelete_CascadesPetOwnerDetail() {
        PetOwner saved = petOwnerRepository.save(owner);
        entityManager.flush();
        Long detailId = saved.getPetOwnerDetail().getOwnerDetailId();

        petOwnerRepository.deleteById(saved.getOwnerId());
        entityManager.flush();
        entityManager.clear();

        assertFalse(petOwnerRepository.existsById(saved.getOwnerId()));
        assertNull(entityManager.find(PetOwnerDetail.class, detailId));
    }
}
