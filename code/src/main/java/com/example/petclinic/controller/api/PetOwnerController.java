package com.example.petclinic.controller.api;

import java.util.List;

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

import com.example.petclinic.dto.request.PetOwnerRequestDTO;
import com.example.petclinic.dto.response.PetOwnerResponseDTO;
import com.example.petclinic.service.PetOwnerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/petowners")
public class PetOwnerController {

    private final PetOwnerService petOwnerService;

    public PetOwnerController(PetOwnerService petOwnerService) {
        this.petOwnerService = petOwnerService;
    }

    // Post เพิ่มข้อมูลเจ้าของสัตว์เลี้ยง
    @PostMapping
    public ResponseEntity<PetOwnerResponseDTO> createPetOwner(
            @Valid @RequestBody PetOwnerRequestDTO requestDTO) {
        PetOwnerResponseDTO responseDTO = petOwnerService.createPetOwner(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    // Get ดึงข้อมูลเจ้าของสัตว์เลี้ยงทั้งหมด
    @GetMapping
    public ResponseEntity<List<PetOwnerResponseDTO>> getAllPetOwners() {
        List<PetOwnerResponseDTO> owners = petOwnerService.getAllPetOwners();
        return ResponseEntity.ok(owners);
    }

    // Get by Id ดึงข้อมูลเจ้าของสัตว์เลี้ยงตาม Id
    @GetMapping("/{id}")
    public ResponseEntity<PetOwnerResponseDTO> getPetOwnerById(@PathVariable Long id) {
        return petOwnerService.getPetOwnerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Put แก้ไขข้อมูลเจ้าของสัตว์เลี้ยง
    @PutMapping("/{id}")
    public ResponseEntity<PetOwnerResponseDTO> updatePetOwner(
            @PathVariable Long id,
            @Valid @RequestBody PetOwnerRequestDTO requestDTO) {
        PetOwnerResponseDTO updatedOwner = petOwnerService.updatePetOwner(id, requestDTO);
        return ResponseEntity.ok(updatedOwner);
    }

    // Delete ลบข้อมูลเจ้าของสัตว์เลี้ยง
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePetOwner(@PathVariable Long id) {
        petOwnerService.deletePetOwner(id);
        return ResponseEntity.noContent().build();
    }
}
