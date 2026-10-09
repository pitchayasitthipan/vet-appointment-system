package com.example.petclinic.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petclinic.dto.request.PetRequestDTO;
import com.example.petclinic.dto.response.PetResponseDTO;
import com.example.petclinic.service.PetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pets")
@CrossOrigin(origins = "*")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    // 1. ดูข้อมูลสัตว์เลี้ยงตาม ID
    @GetMapping("/{id}")
    public ResponseEntity<PetResponseDTO> getPetById(@PathVariable Long id) {
        PetResponseDTO pet = petService.getPetById(id);
        return ResponseEntity.ok(pet);
    }

    // 2. ดูสัตว์เลี้ยงทั้งหมดของเจ้าของ
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<PetResponseDTO>> getPetsByOwnerId(
            @PathVariable Long ownerId) {
        List<PetResponseDTO> pets = petService.getPetsByOwnerId(ownerId);
        return ResponseEntity.ok(pets);
    }

    // 3. เพิ่มข้อมูลสัตว์เลี้ยง
    @PostMapping
    public ResponseEntity<PetResponseDTO> createPet(
            @Valid @RequestBody PetRequestDTO requestDTO) {
        PetResponseDTO createdPet = petService.createPet(requestDTO);
        return new ResponseEntity<>(createdPet, HttpStatus.CREATED);
    }

    // 4. แก้ไขข้อมูลสัตว์เลี้ยง
    @PutMapping("/{id}")
    public ResponseEntity<PetResponseDTO> updatePet(
            @PathVariable Long id,
            @Valid @RequestBody PetRequestDTO requestDTO) {
        PetResponseDTO updatedPet = petService.updatePet(id, requestDTO);
        return ResponseEntity.ok(updatedPet);
    }

    // 5. ลบข้อมูลสัตว์เลี้ยง
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePet(
            @PathVariable Long id) {

        petService.deletePet(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "ลบข้อมูลสัตว์เลี้ยงรหัส " + id + " เรียบร้อยแล้ว");

        return ResponseEntity.ok(response);
    }
}