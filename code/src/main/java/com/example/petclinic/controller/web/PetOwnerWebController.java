package com.example.petclinic.controller.web;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.petclinic.dto.request.PetOwnerRequestDTO;
import com.example.petclinic.dto.response.PetOwnerResponseDTO;
import com.example.petclinic.exception.DuplicateResourceException;
import com.example.petclinic.exception.ResourceNotFoundException;
import com.example.petclinic.service.PetOwnerService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/owners")
public class PetOwnerWebController {

    private final PetOwnerService petOwnerService;

    public PetOwnerWebController(PetOwnerService petOwnerService) {
        this.petOwnerService = petOwnerService;
    }

    // 1. หน้ารายชื่อทั้งหมด (พร้อม Pagination) + ค้นหาด้วยเบอร์โทร
    // ซ้าย = รายชื่อ, ขวา = ข้อมูลย่อของคนที่เลือก (selected = ownerId)
    @GetMapping
    public String listPetOwners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Long selected,
            Model model) {

        List<PetOwnerResponseDTO> owners;

        if (phone != null && !phone.isBlank()) {
            // ค้นหาด้วยเบอร์: เบอร์ห้ามซ้ำ จึงได้ 0 หรือ 1 คน
            try {
                owners = List.of(petOwnerService.getPetOwnerByPhone(phone.trim()));
            } catch (ResourceNotFoundException e) {
                owners = List.of();
            }
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalItems", owners.size());
        } else {
            Sort sort = sortDir.equalsIgnoreCase("asc")
                    ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<PetOwnerResponseDTO> ownerPage = petOwnerService.getAllPetOwners(pageable);
            owners = ownerPage.getContent();
            model.addAttribute("totalPages", ownerPage.getTotalPages());
            model.addAttribute("totalItems", ownerPage.getTotalElements());
        }

        // คนที่แสดงฝั่งขวา: ถ้ากดเลือกมา ใช้คนนั้น ถ้าไม่ได้เลือก ใช้คนแรกในหน้า
        PetOwnerResponseDTO selectedOwner = null;
        for (PetOwnerResponseDTO owner : owners) {
            if (selectedOwner == null || owner.getOwnerId().equals(selected)) {
                selectedOwner = owner;
            }
        }

        model.addAttribute("owners", owners);
        model.addAttribute("selectedOwner", selectedOwner);
        model.addAttribute("phone", phone);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "petowner/list";
    }

    // 2. หน้าแสดงรายละเอียด (Detail)
    @GetMapping("/{id}")
    public String showOwnerDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            PetOwnerResponseDTO owner = petOwnerService.getPetOwnerById(id);
            model.addAttribute("owner", owner);
            return "petowner/detail";
        } catch (ResourceNotFoundException e) {
            // ไม่พบข้อมูล -> กลับไปหน้ารายชื่อ พร้อมแจ้งเตือน
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/owners";
        }
    }

    // 3. หน้าฟอร์มเพิ่มข้อมูลใหม่ (Create Form)
    // ถ้าค้นหาเบอร์แล้วไม่เจอ จะส่ง ?phone= มาด้วย -> กรอกเบอร์ให้อัตโนมัติ
    @GetMapping("/new")
    public String showCreateForm(@RequestParam(required = false) String phone, Model model) {
        PetOwnerRequestDTO requestDTO = new PetOwnerRequestDTO();
        requestDTO.setPhone(phone);
        model.addAttribute("ownerRequest", requestDTO);
        model.addAttribute("isEdit", false);
        return "petowner/form";
    }

    // 4. หน้าฟอร์มแก้ไขข้อมูล (Edit Form)
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        PetOwnerResponseDTO existingOwner;
        try {
            existingOwner = petOwnerService.getPetOwnerById(id);
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/owners";
        }

        // แปลง ResponseDTO กลับเป็น RequestDTO เพื่อ bind เข้ากับ form
        PetOwnerRequestDTO requestDTO = new PetOwnerRequestDTO();
        requestDTO.setFirstName(existingOwner.getFirstName());
        requestDTO.setLastName(existingOwner.getLastName());
        requestDTO.setEmail(existingOwner.getEmail());
        requestDTO.setPhone(existingOwner.getPhone());
        requestDTO.setAddress(existingOwner.getAddress());
        requestDTO.setEmergencyContactName(existingOwner.getEmergencyContactName());
        requestDTO.setEmergencyContactPhone(existingOwner.getEmergencyContactPhone());

        model.addAttribute("ownerId", id);
        model.addAttribute("owner", existingOwner); // ใช้แสดงเลขแฟ้ม + วันลงทะเบียน
        model.addAttribute("ownerRequest", requestDTO);
        model.addAttribute("isEdit", true);
        return "petowner/form";
    }

    // 5. บันทึกข้อมูลใหม่
    // next = "pet" เมื่อกดปุ่ม "บันทึกและเพิ่มสัตว์เลี้ยง"
    @PostMapping
    public String createOwner(
            @Valid @ModelAttribute("ownerRequest") PetOwnerRequestDTO requestDTO,
            BindingResult bindingResult,
            @RequestParam(required = false) String next,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "petowner/form";
        }

        try {
            PetOwnerResponseDTO savedOwner = petOwnerService.createPetOwner(requestDTO);
            if ("pet".equals(next)) {
                // ส่ง ownerId ต่อให้หน้าเพิ่มสัตว์เลี้ยง (Pet)
                return "redirect:/pets/new?ownerId=" + savedOwner.getOwnerId();
            }
            redirectAttributes.addFlashAttribute("successMessage", "บันทึกข้อมูลเรียบร้อยแล้ว");
            return "redirect:/owners/" + savedOwner.getOwnerId();
        } catch (DuplicateResourceException e) { // อีเมล หรือ เบอร์โทรซ้ำ
            model.addAttribute("errorMessage", e.getMessage());
            addDuplicatePhoneOwner(requestDTO.getPhone(), null, model);
            model.addAttribute("isEdit", false);
            return "petowner/form";
        }
    }

    // 6. บันทึกการแก้ไขข้อมูล
    @PostMapping("/{id}")
    public String updateOwner(
            @PathVariable Long id,
            @Valid @ModelAttribute("ownerRequest") PetOwnerRequestDTO requestDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("ownerId", id);
            model.addAttribute("owner", petOwnerService.getPetOwnerById(id));
            model.addAttribute("isEdit", true);
            return "petowner/form";
        }

        try {
            petOwnerService.updatePetOwner(id, requestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "แก้ไขข้อมูลเรียบร้อยแล้ว");
            return "redirect:/owners/" + id;
        } catch (DuplicateResourceException | ResourceNotFoundException e) { // อีเมล/เบอร์ซ้ำ หรือไม่พบ Id
            model.addAttribute("errorMessage", e.getMessage());
            addDuplicatePhoneOwner(requestDTO.getPhone(), id, model);
            model.addAttribute("ownerId", id);
            model.addAttribute("isEdit", true);
            return "petowner/form";
        }
    }

    // 7. ลบข้อมูล
    @PostMapping("/{id}/delete")
    public String deleteOwner(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            petOwnerService.deletePetOwner(id);
            redirectAttributes.addFlashAttribute("successMessage", "ลบข้อมูลเรียบร้อยแล้ว");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owners";
    }

    // ถ้าเบอร์นี้มีเจ้าของคนอื่นใช้แล้ว ส่งข้อมูลคนนั้นไปแสดงกล่อง "เปิดแฟ้มเดิม"
    // ในฟอร์ม
    private void addDuplicatePhoneOwner(String phone, Long currentOwnerId, Model model) {
        try {
            PetOwnerResponseDTO other = petOwnerService.getPetOwnerByPhone(phone);
            if (!other.getOwnerId().equals(currentOwnerId)) {
                model.addAttribute("duplicateOwner", other);
            }
        } catch (ResourceNotFoundException e) {
            // ไม่มีใครใช้เบอร์นี้ -> error มาจากอีเมลซ้ำ แสดงแค่ errorMessage
        }
    }
}