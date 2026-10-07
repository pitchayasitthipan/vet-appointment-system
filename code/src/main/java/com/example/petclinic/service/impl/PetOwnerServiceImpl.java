package com.example.petclinic.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petclinic.domain.entity.PetOwner;
import com.example.petclinic.dto.request.PetOwnerRequestDTO;
import com.example.petclinic.dto.response.PetOwnerResponseDTO;
import com.example.petclinic.exception.DuplicateResourceException;
import com.example.petclinic.exception.ResourceNotFoundException;
import com.example.petclinic.mapper.PetOwnerMapper;
import com.example.petclinic.repository.PetOwnerRepository;
import com.example.petclinic.service.PetOwnerService;

@Service
public class PetOwnerServiceImpl implements PetOwnerService {

    private final PetOwnerRepository petOwnerRepository;
    private final PetOwnerMapper petOwnerMapper;

    public PetOwnerServiceImpl(PetOwnerRepository petOwnerRepository, PetOwnerMapper petOwnerMapper) {
        this.petOwnerRepository = petOwnerRepository;
        this.petOwnerMapper = petOwnerMapper;
    }

    // Create: เพิ่มข้อมูลเจ้าของสัตว์เลี้ยงใหม่
    @Override
    @Transactional // ถ้าเกิด error ระหว่างทำงาน จะ rollback ไม่ให้ข้อมูลเสียหาย
    public PetOwnerResponseDTO createPetOwner(PetOwnerRequestDTO requestDTO) {
        // ตรวจสอบว่า email ซ้ำหรือไม่
        if (petOwnerRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("อีเมลนี้ถูกใช้งานในระบบแล้ว: " + requestDTO.getEmail());
        }

        // สร้าง Entity PetOwner + PetOwnerDetail จากข้อมูลใน RequestDTO
        // และเชื่อมความสัมพันธ์ One to One (ทำใน PetOwnerMapper)
        PetOwner petOwner = petOwnerMapper.toEntity(requestDTO);

        // บันทึกลงฐานข้อมูล
        PetOwner savedPetOwner = petOwnerRepository.save(petOwner);

        // แปลง Entity -> ResponseDTO ก่อนส่งกลับ
        return petOwnerMapper.toResponse(savedPetOwner);
    }

    // Read all: ดึงข้อมูลเจ้าของสัตว์เลี้ยงทั้งหมด
    @Override
    @Transactional(readOnly = true) // readOnly = true -> query only, ไม่แก้ DB
    public Page<PetOwnerResponseDTO> getAllPetOwners(Pageable pageable) {
        // ดึงข้อมูลทั้งหมดแบบแบ่งหน้า แล้วแปลงเป็น DTO ทีละตัว
        return petOwnerRepository.findAll(pageable)
                .map(petOwnerMapper::toResponse);
    }

    // Read by Id: ค้นหาข้อมูลเจ้าของสัตว์เลี้ยงด้วย Id
    @Override
    @Transactional(readOnly = true) // readOnly = true -> query only, ไม่แก้ DB
    public PetOwnerResponseDTO getPetOwnerById(Long id) {
        // ถ้าไม่เจอ Id -> โยน ResourceNotFoundException (404)
        return petOwnerMapper.toResponse(findOwnerOrThrow(id));
    }

    // Update: แก้ไขข้อมูลเจ้าของสัตว์เลี้ยง
    @Override
    @Transactional // ถ้าเกิด error ระหว่างทำงาน จะ rollback ไม่ให้ข้อมูลเสียหาย
    public PetOwnerResponseDTO updatePetOwner(Long id, PetOwnerRequestDTO requestDTO) {
        // ตรวจสอบว่ามีเจ้าของสัตว์เลี้ยงตาม Id หรือไม่
        PetOwner petOwner = findOwnerOrThrow(id);

        // ถ้าเปลี่ยนอีเมลใหม่ ตรวจสอบว่าไม่ซ้ำกับที่มีอยู่แล้ว
        if (!petOwner.getEmail().equals(requestDTO.getEmail()) &&
                petOwnerRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("อีเมลนี้ถูกใช้งานในระบบแล้ว: " + requestDTO.getEmail());
        }

        // อัปเดตข้อมูลหลัก (PetOwner) และข้อมูลเพิ่มเติม (PetOwnerDetail)
        // ถ้าเดิมยังไม่มี detail -> Mapper สร้างใหม่ให้
        petOwnerMapper.updateEntity(petOwner, requestDTO);

        // บันทึกการเปลี่ยนแปลง
        PetOwner updatedPetOwner = petOwnerRepository.save(petOwner);
        return petOwnerMapper.toResponse(updatedPetOwner);
    }

    // Delete: ลบข้อมูลเจ้าของสัตว์เลี้ยง
    @Override
    @Transactional
    public void deletePetOwner(Long id) {
        // ตรวจสอบว่ามีข้อมูลก่อนลบ
        if (!petOwnerRepository.existsById(id)) {
            throw new ResourceNotFoundException("ไม่พบข้อมูลเจ้าของสัตว์เลี้ยงรหัส: " + id);
        }
        // ใช้ CascadeType.ALL ใน petOwner ไป -> ลบ PetOwnerDetail ตามไปด้วย
        petOwnerRepository.deleteById(id);
    }

    // ค้นหาเจ้าของตาม Id ถ้าไม่เจอ -> 404 (ใช้ร่วมกันใน getById และ update)
    private PetOwner findOwnerOrThrow(Long id) {
        return petOwnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลเจ้าของสัตว์เลี้ยงรหัส: " + id));
    }
}
