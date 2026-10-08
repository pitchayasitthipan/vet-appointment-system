package com.example.petclinic.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.petclinic.dto.request.PetOwnerRequestDTO;
import com.example.petclinic.dto.response.PetOwnerResponseDTO;

public interface PetOwnerService {

    // Create: เพิ่มข้อมูลเจ้าของสัตว์เลี้ยงใหม่
    PetOwnerResponseDTO createPetOwner(PetOwnerRequestDTO requestDTO);

    // Read all: ดึงข้อมูลเจ้าของสัตว์เลี้ยงทั้งหมด
    Page<PetOwnerResponseDTO> getAllPetOwners(Pageable pageable);

    // Read by Id: ดึงข้อมูลเจ้าของสัตว์เลี้ยงตามรหัส
    PetOwnerResponseDTO getPetOwnerById(Long id);

    // Update: แก้ไขข้อมูลเจ้าของสัตว์เลี้ยง
    PetOwnerResponseDTO updatePetOwner(Long id, PetOwnerRequestDTO requestDTO);

    // Delete: ลบข้อมูลเจ้าของสัตว์เลี้ยง
    void deletePetOwner(Long id);
}
