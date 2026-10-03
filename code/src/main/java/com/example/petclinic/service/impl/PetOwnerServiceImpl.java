package com.example.petclinic.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petclinic.domain.entity.PetOwner;
import com.example.petclinic.domain.entity.PetOwnerDetail;
import com.example.petclinic.dto.request.PetOwnerRequestDTO;
import com.example.petclinic.dto.response.PetOwnerResponseDTO;
import com.example.petclinic.repository.PetOwnerRepository;
import com.example.petclinic.service.PetOwnerService;

@Service
public class PetOwnerServiceImpl implements PetOwnerService {

    private final PetOwnerRepository petOwnerRepository;

    public PetOwnerServiceImpl(PetOwnerRepository petOwnerRepository) {
        this.petOwnerRepository = petOwnerRepository;
    }

    // Create: เพิ่มข้อมูลเจ้าของสัตว์เลี้ยงใหม่
    @Override
    @Transactional // ถ้าเกิด error ระหว่างทำงาน จะ rollback ไม่ให้ข้อมูลเสียหาย
    public PetOwnerResponseDTO createPetOwner(PetOwnerRequestDTO requestDTO) {
        // ตรวจสอบว่า email ซ้ำหรือไม่
        if (petOwnerRepository.existsByEmail(requestDTO.getEmail())) {
            throw new IllegalArgumentException("อีเมลนี้ถูกใช้งานในระบบแล้ว: " + requestDTO.getEmail());
        }

        // สร้าง Entity PetOwner จากข้อมูลใน RequestDTO
        PetOwner petOwner = new PetOwner();
        petOwner.setFirstName(requestDTO.getFirstName());
        petOwner.setLastName(requestDTO.getLastName());
        petOwner.setEmail(requestDTO.getEmail());
        petOwner.setPhone(requestDTO.getPhone());

        // สร้าง Entity PetOwnerDetail สำหรับข้อมูลเพิ่มเติม
        PetOwnerDetail detail = new PetOwnerDetail();
        detail.setAddress(requestDTO.getAddress());
        detail.setEmergencyContactName(requestDTO.getEmergencyContactName());
        detail.setEmergencyContactPhone(requestDTO.getEmergencyContactPhone());

        // เชื่อมความสัมพันธ์ One to One ระหว่าง PetOwner กับ PetOwnerDetail
        detail.setPetOwner(petOwner);
        petOwner.setPetOwnerDetail(detail);

        // บันทึกลงฐานข้อมูล
        PetOwner savedPetOwner = petOwnerRepository.save(petOwner);

        // แปลง Entity -> ResponseDTO ก่อนส่งกลับ
        return mapToResponseDTO(savedPetOwner);
    }

    // Read all: ดึงข้อมูลเจ้าของสัตว์เลี้ยงทั้งหมด
    @Override
    @Transactional(readOnly = true) // readOnly = true -> query only, ไม่แก้ DB
    public List<PetOwnerResponseDTO> getAllPetOwners() {
        // ดึงข้อมูลทั้งหมด แล้วแปลงเป็น DTO ทีละตัว
        return petOwnerRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Read by Id: ค้นหาข้อมูลเจ้าของสัตว์เลี้ยงด้วย Id
    @Override
    @Transactional(readOnly = true) // readOnly = true -> query only, ไม่แก้ DB
    public Optional<PetOwnerResponseDTO> getPetOwnerById(Long id) {
        // ถ้าไม่เจอ Id -> return Optional.empty()
        return petOwnerRepository.findById(id)
                .map(this::mapToResponseDTO);
    }

    // Update: แก้ไขข้อมูลเจ้าของสัตว์เลี้ยง
    @Override
    @Transactional // ถ้าเกิด error ระหว่างทำงาน จะ rollback ไม่ให้ข้อมูลเสียหาย
    public PetOwnerResponseDTO updatePetOwner(Long id, PetOwnerRequestDTO requestDTO) {
        // ตรวจสอบว่ามีเจ้าของสัตว์เลี้ยงตาม Id หรือไม่
        PetOwner petOwner = petOwnerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบข้อมูลเจ้าของสัตว์เลี้ยงรหัส: " + id));

        // ถ้าเปลี่ยนอีเมลใหม่ ตรวจสอบว่าไม่ซ้ำกับที่มีอยู่แล้ว
        if (!petOwner.getEmail().equalsIgnoreCase(requestDTO.getEmail()) &&
                petOwnerRepository.existsByEmail(requestDTO.getEmail())) {
            throw new IllegalArgumentException("อีเมลนี้ถูกใช้งานในระบบแล้ว: " + requestDTO.getEmail());
        }

        // อัปเดตข้อมูลหลัก (PetOwner)
        petOwner.setFirstName(requestDTO.getFirstName());
        petOwner.setLastName(requestDTO.getLastName());
        petOwner.setEmail(requestDTO.getEmail());
        petOwner.setPhone(requestDTO.getPhone());

        // อัปเดตข้อมูลเพิ่มเติม (PetOwnerDetail)
        PetOwnerDetail detail = petOwner.getPetOwnerDetail();
        if (detail == null) {
            // ถ้าเดิมยังไม่มี detail -> สร้างใหม่
            detail = new PetOwnerDetail();
            detail.setPetOwner(petOwner);
            petOwner.setPetOwnerDetail(detail);
        }
        detail.setAddress(requestDTO.getAddress());
        detail.setEmergencyContactName(requestDTO.getEmergencyContactName());
        detail.setEmergencyContactPhone(requestDTO.getEmergencyContactPhone());

        // บันทึกการเปลี่ยนแปลง
        PetOwner updatedPetOwner = petOwnerRepository.save(petOwner);
        return mapToResponseDTO(updatedPetOwner);
    }

    // Delete: ลบข้อมูลเจ้าของสัตว์เลี้ยง
    @Override
    @Transactional
    public void deletePetOwner(Long id) {
        // ตรวจสอบว่ามีข้อมูลก่อนลบ
        if (!petOwnerRepository.existsById(id)) {
            throw new IllegalArgumentException("ไม่พบข้อมูลเจ้าของสัตว์เลี้ยงรหัส: " + id);
        }
        // ใช้ CascadeType.ALL ใน petOwner ไป -> ลบ PetOwnerDetail ตามไปด้วย
        petOwnerRepository.deleteById(id);
    }

    // Mapper: แปลง Entity -> ResponseDTO
    private PetOwnerResponseDTO mapToResponseDTO(PetOwner petOwner) {
        PetOwnerResponseDTO dto = new PetOwnerResponseDTO();
        dto.setOwnerId(petOwner.getOwnerId());
        dto.setFirstName(petOwner.getFirstName());
        dto.setLastName(petOwner.getLastName());
        dto.setEmail(petOwner.getEmail());
        dto.setPhone(petOwner.getPhone());
        dto.setCreatedAt(petOwner.getCreatedAt());

        // ถ้ามีข้อมูล detail -> ใส่ลง DTO ด้วย
        if (petOwner.getPetOwnerDetail() != null) {
            PetOwnerDetail detail = petOwner.getPetOwnerDetail();
            dto.setOwnerDetailId(detail.getOwnerDetailId());
            dto.setAddress(detail.getAddress());
            dto.setEmergencyContactName(detail.getEmergencyContactName());
            dto.setEmergencyContactPhone(detail.getEmergencyContactPhone());
        }
        return dto;
    }
}
