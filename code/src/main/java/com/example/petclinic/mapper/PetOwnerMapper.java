package com.example.petclinic.mapper;

import org.springframework.stereotype.Component;

import com.example.petclinic.domain.entity.PetOwner;
import com.example.petclinic.domain.entity.PetOwnerDetail;
import com.example.petclinic.dto.request.PetOwnerRequestDTO;
import com.example.petclinic.dto.response.PetOwnerResponseDTO;

// แปลงข้อมูลระหว่าง DTO <-> Entity ของเจ้าของสัตว์เลี้ยง
// จาก Service เพื่อให้ Service ทำเฉพาะ Business Logic (Single Responsibility)
@Component
public class PetOwnerMapper {

    // สร้าง Entity PetOwner ใหม่จากข้อมูลใน RequestDTO (ใช้ตอน Create)
    public PetOwner toEntity(PetOwnerRequestDTO requestDTO) {
        PetOwner petOwner = new PetOwner();
        updateEntity(petOwner, requestDTO);
        return petOwner;
    }

    // คัดลอกข้อมูลจาก RequestDTO ลง Entity (ใช้ทั้ง Create แล้วก็ Update)
    public void updateEntity(PetOwner petOwner, PetOwnerRequestDTO requestDTO) {
        // อัปเดตข้อมูลหลัก (PetOwner)
        petOwner.setFirstName(requestDTO.getFirstName());
        petOwner.setLastName(requestDTO.getLastName());
        petOwner.setEmail(requestDTO.getEmail());
        petOwner.setPhone(requestDTO.getPhone());

        // อัปเดตข้อมูล PetOwnerDetail
        PetOwnerDetail detail = petOwner.getPetOwnerDetail();
        if (detail == null) {
            // ถ้าเดิมยังไม่มี detail -> สร้างใหม่
            detail = new PetOwnerDetail();
            // เชื่อมความสัมพันธ์ One to One ของ PetOwner กับ PetOwnerDetail
            petOwner.setPetOwnerDetail(detail);
        }
        detail.setAddress(requestDTO.getAddress());
        detail.setEmergencyContactName(requestDTO.getEmergencyContactName());
        detail.setEmergencyContactPhone(requestDTO.getEmergencyContactPhone());
    }

    // Mapper: แปลง Entity -> ResponseDTO
    public PetOwnerResponseDTO toResponse(PetOwner petOwner) {
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
