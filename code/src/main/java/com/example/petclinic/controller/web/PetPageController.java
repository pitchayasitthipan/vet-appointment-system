package com.example.petclinic.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.petclinic.controller.StaffAccess;
import com.example.petclinic.dto.response.PetOwnerResponseDTO;
import com.example.petclinic.exception.ResourceNotFoundException;
import com.example.petclinic.service.PetOwnerService;

import jakarta.servlet.http.HttpSession;

// หน้าเว็บสัตว์เลี้ยง: ข้อมูลในหน้าโหลดด้วย pets.js ผ่าน /api/v1/pets
// ลูกค้าเห็นเฉพาะสัตว์ของตัวเอง (myOwnerId ใน session)
// เจ้าหน้าที่: ไม่ระบุ ownerId = ดูสัตว์ทั้งหมดในคลินิก (แบ่งหน้า), ระบุ ownerId= = ดูของเจ้าของคนนั้น
@Controller
public class PetPageController {

    private final PetOwnerService petOwnerService;

    public PetPageController(PetOwnerService petOwnerService) {
        this.petOwnerService = petOwnerService;
    }

    // เมนู My Pets: GET /pets
    @GetMapping("/pets")
    public String petsPage(@RequestParam(required = false) Long ownerId, HttpSession session,
            Model model, RedirectAttributes redirectAttributes) {
        return showPets(ownerId, false, session, model, redirectAttributes);
    }

    // ปุ่ม "เพิ่มสัตว์เลี้ยง" จากแฟ้มเจ้าของ: GET /pets/new?ownerId=.. -> เปิดหน้าพร้อมฟอร์มเพิ่มสัตว์
    // มาจากหน้าจองนัด (?returnTo=appointment) -> เพิ่มเสร็จแล้วกลับไปจองนัดต่อ
    @GetMapping("/pets/new")
    public String newPetPage(@RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String returnTo, HttpSession session,
            Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("returnToAppointment", "appointment".equals(returnTo));
        return showPets(ownerId, true, session, model, redirectAttributes);
    }

    private String showPets(Long ownerId, boolean openAddForm, HttpSession session,
            Model model, RedirectAttributes redirectAttributes) {
        Long targetOwnerId;
        if (StaffAccess.isStaff(session)) {
            // เจ้าหน้าที่ไม่ระบุเจ้าของ -> หน้าสัตว์เลี้ยงทั้งหมดในคลินิก
            if (ownerId == null) {
                model.addAttribute("allPets", true);
                model.addAttribute("openAddForm", false);
                return "pet/list";
            }
            targetOwnerId = ownerId;
        } else {
            Object myOwnerId = session.getAttribute(StaffAccess.MY_OWNER_ID);
            if (!(myOwnerId instanceof Long)) {
                redirectAttributes.addFlashAttribute("errorMessage", "กรุณาค้นหาด้วยเบอร์โทรศัพท์ของคุณก่อน");
                return "redirect:/owners";
            }
            targetOwnerId = (Long) myOwnerId;
        }

        try {
            PetOwnerResponseDTO owner = petOwnerService.getPetOwnerById(targetOwnerId);
            model.addAttribute("owner", owner);
            model.addAttribute("allPets", false);
            model.addAttribute("openAddForm", openAddForm);
            return "pet/list";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/owners";
        }
    }
}