package com.example.petclinic.controller.api;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.petclinic.controller.StaffAccess;
import com.example.petclinic.dto.request.PetOwnerRequestDTO;
import com.example.petclinic.dto.response.PetOwnerResponseDTO;
import com.example.petclinic.service.PetOwnerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Tag(name = "Pet Owners", description = "จัดการข้อมูลเจ้าของสัตว์เลี้ยง") // ชื่อกลุ่ม API ใน Swagger UI
@RestController
@RequestMapping("/api/v1/owners") // Resource-based: /api/v1/{resource}
public class PetOwnerController {

    private final PetOwnerService petOwnerService;

    public PetOwnerController(PetOwnerService petOwnerService) {
        this.petOwnerService = petOwnerService;
    }

    // Post เพิ่มข้อมูลเจ้าของสัตว์เลี้ยง -> 201 Created (ลูกค้าลงทะเบียนเองได้
    // ไม่ต้องใส่รหัส)
    @Operation(summary = "เพิ่มข้อมูลเจ้าของสัตว์เลี้ยง")
    @PostMapping
    public ResponseEntity<PetOwnerResponseDTO> createPetOwner(
            @Valid @RequestBody PetOwnerRequestDTO requestDTO) {
        PetOwnerResponseDTO responseDTO = petOwnerService.createPetOwner(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    // Get ดึงข้อมูลเจ้าของสัตว์เลี้ยงทั้งหมด (Pagination & Sorting)
    // ตัวอย่าง: GET /api/v1/owners?page=0&size=10&sort=lastName,asc
    // ถ้าไม่ส่งค่ามา ใช้ค่าเริ่มต้น เป็น หน้าละ 10 รายการ เรียงตาม Id
    // เจ้าของจากน้อยไปมาก
    // เฉพาะเจ้าหน้าที่ (ใส่รหัสแล้ว) ไม่งั้นตอบ 403
    @Operation(summary = "ดึงข้อมูลเจ้าของสัตว์เลี้ยงทั้งหมด (แบ่งหน้า + เรียงลำดับ) [เฉพาะเจ้าหน้าที่]")
    @GetMapping
    public ResponseEntity<Page<PetOwnerResponseDTO>> getAllPetOwners(
            @PageableDefault(size = 10, sort = "ownerId", direction = Sort.Direction.ASC) Pageable pageable,
            HttpSession session) {
        StaffAccess.requireStaff(session);
        Page<PetOwnerResponseDTO> owners = petOwnerService.getAllPetOwners(pageable);
        return ResponseEntity.ok(owners);
    }

    // Get by phone ค้นหาเจ้าของสัตว์เลี้ยงด้วยเบอร์โทร (ระบบไม่มี Login
    // จึงใช้เบอร์แทน)
    // ตัวอย่าง: GET /api/v1/owners/search?phone=0812345678
    // เจอ -> 200 พร้อม ownerId ให้โมดูลอื่นใช้ต่อ, ไม่เจอ -> 404 (ลูกค้าเรียกได้
    // ไม่ต้องใส่รหัส)
    @Operation(summary = "ค้นหาเจ้าของสัตว์เลี้ยงด้วยเบอร์โทรศัพท์")
    @GetMapping("/search")
    public ResponseEntity<PetOwnerResponseDTO> getPetOwnerByPhone(@RequestParam String phone, HttpSession session) {
        PetOwnerResponseDTO owner = petOwnerService.getPetOwnerByPhone(phone);
        if (StaffAccess.isStaff(session)) {
            return ResponseEntity.ok(owner); // เจ้าหน้าที่เห็นข้อมูลครบ
        }
        // ลูกค้าทั่วไป: ส่งแค่ ownerId กับชื่อ พอให้โมดูลอื่นใช้ต่อ
        // ไม่ส่งอีเมล ที่อยู่ ผู้ติดต่อฉุกเฉิน กันคนอื่นเอาเบอร์มาเปิดดูข้อมูลส่วนตัว
        PetOwnerResponseDTO basic = new PetOwnerResponseDTO();
        basic.setOwnerId(owner.getOwnerId());
        basic.setFirstName(owner.getFirstName());
        basic.setLastName(owner.getLastName());
        return ResponseEntity.ok(basic);
    }

    // Get by Id ดึงข้อมูลเจ้าของสัตว์เลี้ยงตาม Id
    // ถ้าไม่เจอ Service จะโยน ResourceNotFoundException -> GlobalExceptionHandler
    // ตอบ 404
    // เฉพาะเจ้าหน้าที่ กันไม่ให้ไล่เลข Id ดูข้อมูลคนอื่น
    @Operation(summary = "ดึงข้อมูลเจ้าของสัตว์เลี้ยงตาม Id [เฉพาะเจ้าหน้าที่]")
    @GetMapping("/{id}")
    public ResponseEntity<PetOwnerResponseDTO> getPetOwnerById(@PathVariable Long id, HttpSession session) {
        StaffAccess.requireStaff(session);
        PetOwnerResponseDTO owner = petOwnerService.getPetOwnerById(id);
        return ResponseEntity.ok(owner);
    }

    // Put แก้ไขข้อมูลเจ้าของสัตว์เลี้ยง -> 200 OK
    @Operation(summary = "แก้ไขข้อมูลเจ้าของสัตว์เลี้ยง [เฉพาะเจ้าหน้าที่]")
    @PutMapping("/{id}")
    public ResponseEntity<PetOwnerResponseDTO> updatePetOwner(
            @PathVariable Long id,
            @Valid @RequestBody PetOwnerRequestDTO requestDTO,
            HttpSession session) {
        StaffAccess.requireStaff(session);
        PetOwnerResponseDTO updatedOwner = petOwnerService.updatePetOwner(id, requestDTO);
        return ResponseEntity.ok(updatedOwner);
    }

    // Delete ลบข้อมูลเจ้าของสัตว์เลี้ยง -> 204 No Content
    @Operation(summary = "ลบข้อมูลเจ้าของสัตว์เลี้ยง [เฉพาะเจ้าหน้าที่]")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePetOwner(@PathVariable Long id, HttpSession session) {
        StaffAccess.requireStaff(session);
        petOwnerService.deletePetOwner(id);
        return ResponseEntity.noContent().build();
    }
}