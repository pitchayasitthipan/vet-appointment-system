package com.example.petclinic.service.impl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.petclinic.domain.entity.PetOwner;
import com.example.petclinic.domain.entity.PetOwnerDetail;
import com.example.petclinic.dto.request.PetOwnerRequestDTO;
import com.example.petclinic.dto.response.PetOwnerResponseDTO;
import com.example.petclinic.exception.DuplicateResourceException;
import com.example.petclinic.exception.ResourceNotFoundException;
import com.example.petclinic.mapper.PetOwnerMapper;
import com.example.petclinic.repository.PetOwnerRepository;

class PetOwnerServiceImplTest {

    @Mock
    private PetOwnerRepository petOwnerRepository;

    // Mapper ใช้ของจริง (@Spy) เพราะแค่แปลงข้อมูล ไม่ต้องต่อ db
    @Spy
    private PetOwnerMapper petOwnerMapper = new PetOwnerMapper();

    @InjectMocks
    private PetOwnerServiceImpl petOwnerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("สร้างข้อมูลเจ้าของสัตว์เลี้ยง: สำเร็จ")
    void testCreatePetOwner_Success() {
        PetOwnerRequestDTO request = new PetOwnerRequestDTO();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("test@example.com");
        request.setPhone("0876543210");
        request.setAddress("Khon Kaen");
        request.setEmergencyContactName("familyMember");
        request.setEmergencyContactPhone("0987654321");

        when(petOwnerRepository.existsByEmail("test@example.com")).thenReturn(false);

        PetOwner saved = new PetOwner();
        saved.setOwnerId(1L);
        saved.setFirstName("John");
        saved.setLastName("Doe");
        saved.setEmail("test@example.com");
        saved.setPhone("0876543210");

        PetOwnerDetail detail = new PetOwnerDetail();
        detail.setAddress("Khon Kaen");
        detail.setEmergencyContactName("familyMember");
        detail.setEmergencyContactPhone("0987654321");
        saved.setPetOwnerDetail(detail);

        when(petOwnerRepository.save(any(PetOwner.class))).thenReturn(saved);

        PetOwnerResponseDTO result = petOwnerService.createPetOwner(request);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Khon Kaen", result.getAddress());
        verify(petOwnerRepository, times(1)).save(any(PetOwner.class));
    }

    @Test
    @DisplayName("สร้างข้อมูลเจ้าของสัตว์เลี้ยง: อีเมลซ้ำ")
    void testCreatePetOwner_DuplicateEmail() {
        PetOwnerRequestDTO request = new PetOwnerRequestDTO();
        request.setEmail("test@example.com");

        when(petOwnerRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> petOwnerService.createPetOwner(request));
        verify(petOwnerRepository, never()).save(any(PetOwner.class));
    }

    @Test
    @DisplayName("สร้างข้อมูลเจ้าของสัตว์เลี้ยง: อีเมลตัวพิมพ์ใหญ่ที่มีอยู่แล้วในระบบ (409)")
    void testCreatePetOwner_DuplicateUppercaseEmail() {
        PetOwnerRequestDTO request = new PetOwnerRequestDTO();
        request.setEmail("ABC@gmail.com");

        // จำลองว่ามีคนใช้อีเมลนี้แล้ว, mock existsByEmail()
        when(petOwnerRepository.existsByEmail("ABC@gmail.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> petOwnerService.createPetOwner(request));

        verify(petOwnerRepository, times(1)).existsByEmail("ABC@gmail.com");
        verify(petOwnerRepository, never()).save(any(PetOwner.class));
    }

    @Test
    @DisplayName("ดึงข้อมูลเจ้าของสัตว์เลี้ยงทั้งหมด: สำเร็จ (Pagination)")
    void testGetAllPetOwners_Pagination() {
        PetOwner owner = new PetOwner();
        owner.setOwnerId(1L);
        owner.setFirstName("John");
        owner.setLastName("Test");
        owner.setEmail("test@example.com");
        owner.setPhone("0876543210");

        Pageable pageable = PageRequest.of(0, 5);
        Page<PetOwner> pageResult = new PageImpl<>(List.of(owner), pageable, 1);

        when(petOwnerRepository.findAll(pageable)).thenReturn(pageResult);

        Page<PetOwnerResponseDTO> result = petOwnerService.getAllPetOwners(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals("John", result.getContent().get(0).getFirstName());
        verify(petOwnerRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("ค้นหาเจ้าของสัตว์เลี้ยงด้วย Id: พบข้อมูล")
    void testGetPetOwnerById_Found() {
        PetOwner owner = new PetOwner();
        owner.setOwnerId(1L);
        owner.setFirstName("John");

        when(petOwnerRepository.findById(1L)).thenReturn(Optional.of(owner));

        PetOwnerResponseDTO result = petOwnerService.getPetOwnerById(1L);

        assertEquals("John", result.getFirstName());
        verify(petOwnerRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("ค้นหาเจ้าของสัตว์เลี้ยงด้วย Id: ไม่พบข้อมูล")
    void testGetPetOwnerById_NotFound() {
        when(petOwnerRepository.findById(99L)).thenReturn(Optional.empty());

        // ไม่พบข้อมูล -> Service โยน ResourceNotFoundException (404)
        assertThrows(ResourceNotFoundException.class, () -> petOwnerService.getPetOwnerById(99L));
        verify(petOwnerRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("แก้ไขข้อมูลเจ้าของสัตว์เลี้ยง: สำเร็จ")
    void testUpdatePetOwner_Success() {
        PetOwner existing = new PetOwner();
        existing.setOwnerId(1L);
        existing.setFirstName("OldName");
        existing.setLastName("OldLast");
        existing.setEmail("old@example.com");

        PetOwnerDetail oldDetail = new PetOwnerDetail();
        existing.setPetOwnerDetail(oldDetail);

        PetOwnerRequestDTO request = new PetOwnerRequestDTO();
        request.setFirstName("John");
        request.setLastName("Updated");
        request.setEmail("test@example.com");
        request.setPhone("0876543210");
        request.setAddress("Khon Kaen");
        request.setEmergencyContactName("familyMember");
        request.setEmergencyContactPhone("0987654321");

        when(petOwnerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(petOwnerRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(petOwnerRepository.save(existing)).thenReturn(existing);

        PetOwnerResponseDTO result = petOwnerService.updatePetOwner(1L, request);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Updated", result.getLastName());
        assertEquals("Khon Kaen", result.getAddress());
        verify(petOwnerRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("แก้ไขข้อมูลเจ้าของสัตว์เลี้ยง: ไม่พบ Id")
    void testUpdatePetOwner_NotFound() {
        PetOwnerRequestDTO request = new PetOwnerRequestDTO();
        request.setEmail("new@example.com");

        when(petOwnerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> petOwnerService.updatePetOwner(99L, request));
        verify(petOwnerRepository, times(1)).findById(99L);
        verify(petOwnerRepository, never()).save(any(PetOwner.class));
    }

    // ต้อง mock ทั้ง findById() และ existsByEmail() เชคกับข้อมูลเดิมที่มีในระบบ
    @Test
    @DisplayName("แก้ไขข้อมูลเจ้าของสัตว์เลี้ยง: เปลี่ยนเป็นอีเมลที่มีคนใช้แล้ว (409)")
    void testUpdatePetOwner_DuplicateUppercaseEmail() {
        PetOwner existing = new PetOwner();
        existing.setOwnerId(1L);
        existing.setEmail("abc@gmail.com");

        PetOwnerRequestDTO request = new PetOwnerRequestDTO();
        request.setEmail("ABC@gmail.com");

        when(petOwnerRepository.findById(1L))
                .thenReturn(Optional.of(existing));
        when(petOwnerRepository.existsByEmail("ABC@gmail.com"))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> petOwnerService.updatePetOwner(1L, request));

        verify(petOwnerRepository, times(1))
                .existsByEmail("ABC@gmail.com");
        verify(petOwnerRepository, never())
                .save(any(PetOwner.class));
    }

    @Test
    @DisplayName("แก้ไขข้อมูลเจ้าของสัตว์เลี้ยง: อีเมลต่างกันเฉพาะตัวพิมพ์เล็ก-ใหญ่ สามารถใช้ได้")
    void testUpdatePetOwner_CaseSensitiveEmailAllowed() {
        PetOwner existing = new PetOwner();
        existing.setOwnerId(1L);
        existing.setEmail("abc@gmail.com");

        PetOwnerRequestDTO request = new PetOwnerRequestDTO();
        request.setEmail("ABC@gmail.com");

        when(petOwnerRepository.findById(1L))
                .thenReturn(Optional.of(existing));
        // ไม่พบ ABC@gmail.com ในระบบ แปลว่าสามารถใช้ได้
        when(petOwnerRepository.existsByEmail("ABC@gmail.com"))
                .thenReturn(false);
        when(petOwnerRepository.save(existing))
                .thenReturn(existing);

        PetOwnerResponseDTO result = petOwnerService.updatePetOwner(1L, request);

        assertNotNull(result);
        assertEquals("ABC@gmail.com", result.getEmail());
        verify(petOwnerRepository, times(1))
                .existsByEmail("ABC@gmail.com");
        verify(petOwnerRepository, times(1))
                .save(existing);
    }

    @Test
    @DisplayName("ลบข้อมูลเจ้าของสัตว์เลี้ยง: สำเร็จ")
    void testDeletePetOwner_Success() {
        when(petOwnerRepository.existsById(1L)).thenReturn(true);

        petOwnerService.deletePetOwner(1L);

        verify(petOwnerRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("ลบข้อมูลเจ้าของสัตว์เลี้ยง: ไม่พบ Id")
    void testDeletePetOwner_NotFound() {
        when(petOwnerRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> petOwnerService.deletePetOwner(99L));
        verify(petOwnerRepository, times(1)).existsById(99L);
        verify(petOwnerRepository, never()).deleteById(any());
    }
}
