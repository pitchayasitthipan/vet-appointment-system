package com.example.petclinic.controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petclinic.controller.StaffAccess;
import com.example.petclinic.dto.request.PetRequestDTO;
import com.example.petclinic.dto.response.PetResponseDTO;
import com.example.petclinic.service.PetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

// สิทธิ์: เจ้าหน้าที่จัดการได้ทุกตัว, ลูกค้าจัดการได้เฉพาะสัตว์ของตัวเอง (myOwnerId ใน session)
@Tag(name = "Pets", description = "จัดการข้อมูลสัตว์เลี้ยง")
@RestController
@RequestMapping("/api/v1/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    // 1. ดูสัตว์เลี้ยงทั้งหมดในคลินิก (แบ่งหน้า) เช่น GET /api/v1/pets?page=0&size=10
    @Operation(summary = "ดึงข้อมูลสัตว์เลี้ยงทั้งหมด (แบ่งหน้า + เรียงลำดับ) [เฉพาะเจ้าหน้าที่]")
    @GetMapping
    public ResponseEntity<Page<PetResponseDTO>> getAllPets(
            @PageableDefault(size = 10, sort = "petId", direction = Sort.Direction.ASC) Pageable pageable,
            HttpSession session) {
        StaffAccess.requireStaff(session);
        return ResponseEntity.ok(petService.getAllPets(pageable));
    }

    // 2. ดูข้อมูลสัตว์เลี้ยงตาม ID
    @Operation(summary = "ดึงข้อมูลสัตว์เลี้ยงตาม Id [เจ้าของหรือเจ้าหน้าที่]")
    @GetMapping("/{id}")
    public ResponseEntity<PetResponseDTO> getPetById(@PathVariable Long id, HttpSession session) {
        PetResponseDTO pet = petService.getPetById(id);
        StaffAccess.requireOwnerOrStaff(session, pet.getOwnerId());
        return ResponseEntity.ok(pet);
    }

    // 3. ดูสัตว์เลี้ยงทั้งหมดของเจ้าของ
    @Operation(summary = "ดึงสัตว์เลี้ยงทั้งหมดของเจ้าของ [เจ้าของหรือเจ้าหน้าที่]")
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<PetResponseDTO>> getPetsByOwnerId(
            @PathVariable Long ownerId, HttpSession session) {
        StaffAccess.requireOwnerOrStaff(session, ownerId);
        return ResponseEntity.ok(petService.getPetsByOwnerId(ownerId));
    }

    // 4. เพิ่มข้อมูลสัตว์เลี้ยง -> 201 Created
    @Operation(summary = "เพิ่มข้อมูลสัตว์เลี้ยง [เจ้าของหรือเจ้าหน้าที่]")
    @PostMapping
    public ResponseEntity<PetResponseDTO> createPet(
            @Valid @RequestBody PetRequestDTO requestDTO, HttpSession session) {
        StaffAccess.requireOwnerOrStaff(session, requestDTO.getOwnerId());
        PetResponseDTO createdPet = petService.createPet(requestDTO);
        return new ResponseEntity<>(createdPet, HttpStatus.CREATED);
    }

    // 5. แก้ไขข้อมูลสัตว์เลี้ยง (เจ้าของเดิมเสมอ ไม่ย้ายแฟ้ม)
    @Operation(summary = "แก้ไขข้อมูลสัตว์เลี้ยง [เจ้าของหรือเจ้าหน้าที่]")
    @PutMapping("/{id}")
    public ResponseEntity<PetResponseDTO> updatePet(
            @PathVariable Long id,
            @Valid @RequestBody PetRequestDTO requestDTO,
            HttpSession session) {
        StaffAccess.requireOwnerOrStaff(session, petService.getPetById(id).getOwnerId());
        PetResponseDTO updatedPet = petService.updatePet(id, requestDTO);
        return ResponseEntity.ok(updatedPet);
    }

    // 6. ลบข้อมูลสัตว์เลี้ยง
    @Operation(summary = "ลบข้อมูลสัตว์เลี้ยง [เจ้าของหรือเจ้าหน้าที่]")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePet(
            @PathVariable Long id, HttpSession session) {

        StaffAccess.requireOwnerOrStaff(session, petService.getPetById(id).getOwnerId());
        petService.deletePet(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "ลบข้อมูลสัตว์เลี้ยงรหัส " + id + " เรียบร้อยแล้ว");

        return ResponseEntity.ok(response);
    }
}