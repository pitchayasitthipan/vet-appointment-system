package com.example.petclinic.controller.web;

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
import com.example.petclinic.service.PetOwnerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/owners")
@RequiredArgsConstructor
public class PetOwnerWebController {

    private final PetOwnerService petOwnerService;

    // 1. หน้าตารางแสดง รายชื่อทั้งหมด (พร้อม Pagination)
    @GetMapping
    public String listPetOwners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ownerId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page ownerPage = petOwnerService.getAllPetOwners(pageable);

        model.addAttribute("owners", ownerPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ownerPage.getTotalPages());
        model.addAttribute("totalItems", ownerPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "pet-owners/list";
    }

    // 2. หน้าแสดงรายละเอียด (Detail)
    @GetMapping("/{id}")
    public String showOwnerDetail(@PathVariable Long id, Model model) {
        PetOwnerResponseDTO owner = petOwnerService.getPetOwnerById(id)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบข้อมูลเจ้าของสัตว์เลี้ยง รหัส: " + id));
        model.addAttribute("owner", owner);
        return "pet-owners/detail";
    }

    // 3. หน้าฟอร์มเพิ่มข้อมูลใหม่ (Create Form)
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("ownerRequest", new PetOwnerRequestDTO());
        model.addAttribute("isEdit", false);
        return "pet-owners/form";
    }

    // 4. หน้าฟอร์มแก้ไขข้อมูล (Edit Form)
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        PetOwnerResponseDTO existingOwner = petOwnerService.getPetOwnerById(id)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบข้อมูลเจ้าของสัตว์เลี้ยง รหัส: " + id));

        // แปลง ResponseDTO กลับเป็น RequestDTO เพื่อ Bind เข้ากับ Form
        PetOwnerRequestDTO requestDTO = new PetOwnerRequestDTO();
        requestDTO.setFirstName(existingOwner.getFirstName());
        requestDTO.setLastName(existingOwner.getLastName());
        requestDTO.setEmail(existingOwner.getEmail());
        requestDTO.setPhone(existingOwner.getPhone());
        requestDTO.setAddress(existingOwner.getAddress());
        requestDTO.setEmergencyContactName(existingOwner.getEmergencyContactName());
        requestDTO.setEmergencyContactPhone(existingOwner.getEmergencyContactPhone());

        model.addAttribute("ownerId", id);
        model.addAttribute("ownerRequest", requestDTO);
        model.addAttribute("isEdit", true);
        return "pet-owners/form";
    }

    // 5. บันทึกข้อมูลใหม่
    @PostMapping
    public String createOwner(
            @Valid @ModelAttribute("ownerRequest") PetOwnerRequestDTO requestDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "pet-owners/form";
        }

        try {
            petOwnerService.createPetOwner(requestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "เพิ่มข้อมูลเจ้าของสัตว์เลี้ยงเรียบร้อยแล้ว");
            return "redirect:/owners";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isEdit", false);
            return "pet-owners/form";
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
            model.addAttribute("isEdit", true);
            return "pet-owners/form";
        }

        try {
            petOwnerService.updatePetOwner(id, requestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "แก้ไขข้อมูลเรียบร้อยแล้ว");
            return "redirect:/owners/" + id;
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("ownerId", id);
            model.addAttribute("isEdit", true);
            return "pet-owners/form";
        }
    }

    // 7. ลบข้อมูล
    @PostMapping("/{id}/delete")
    public String deleteOwner(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            petOwnerService.deletePetOwner(id);
            redirectAttributes.addFlashAttribute("successMessage", "ลบข้อมูลเรียบร้อยแล้ว");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owners";
    }
}