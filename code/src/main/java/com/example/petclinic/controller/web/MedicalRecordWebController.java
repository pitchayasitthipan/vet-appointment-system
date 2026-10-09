
package com.example.petclinic.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.example.petclinic.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.petclinic.service.MedicalRecordService;

import com.example.petclinic.dto.request.MedicalRecordRequestDTO;

import jakarta.validation.Valid;

import com.example.petclinic.controller.StaffAccess;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/medical-records")
public class MedicalRecordWebController {

    private final MedicalRecordService medicalRecordService;

    // รับ Service เพื่อดึงข้อมูลประวัติการรักษา
    public MedicalRecordWebController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

        // ตรวจสิทธิ์เจ้าหน้าที่ก่อนเข้าใช้งานหน้าประวัติการรักษา
    private boolean isNotStaff(HttpSession session) {
        return !StaffAccess.isStaff(session);
    }

    // หากยังไม่ปลดล็อก ให้กลับไปหน้ากรอกรหัสเจ้าหน้าที่
    private String redirectToStaffLogin(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(
                "errorMessage",
                "กรุณากรอกรหัสเจ้าหน้าที่ก่อนเข้าถึงประวัติการรักษา");
        return "redirect:/owners/staff";
    }

        // เปิดหน้ารายการประวัติการรักษา
    @GetMapping
    public String listMedicalRecords(
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (isNotStaff(session)) {
            return redirectToStaffLogin(redirectAttributes);
        }

        model.addAttribute(
                "medicalRecords",
                medicalRecordService.getAllMedicalRecords()
        );

        return "medicalrecord/list";
    }

        // เปิดฟอร์มสำหรับเพิ่มประวัติการรักษา
    @GetMapping("/new")
    public String showCreateForm(
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (isNotStaff(session)) {
            return redirectToStaffLogin(redirectAttributes);
        }

        model.addAttribute("medicalRecord", new MedicalRecordRequestDTO());
        return "medicalrecord/form";
    }

        // รับข้อมูลจากฟอร์มแล้วบันทึกผ่าน Service
    @PostMapping
    public String createMedicalRecord(
            @Valid @ModelAttribute("medicalRecord") MedicalRecordRequestDTO requestDTO,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (isNotStaff(session)) {
            return redirectToStaffLogin(redirectAttributes);
        }

        if (bindingResult.hasErrors()) {
            return "medicalrecord/form";
        }

        medicalRecordService.createMedicalRecord(requestDTO);

        redirectAttributes.addFlashAttribute(
                "successMessage", "บันทึกประวัติการรักษาสำเร็จ");

        return "redirect:/medical-records";
    }

        // แสดงรายละเอียดประวัติการรักษาตาม ID
    @GetMapping("/{id}")
    public String viewMedicalRecord(
            @PathVariable Long id,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (isNotStaff(session)) {
            return redirectToStaffLogin(redirectAttributes);
        }

        model.addAttribute(
                "record",
                medicalRecordService.getMedicalRecordById(id)
        );

        return "medicalrecord/detail";
    }

        // เปิดฟอร์มแก้ไข โดยดึงข้อมูลเดิมมาแสดง
    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (isNotStaff(session)) {
            return redirectToStaffLogin(redirectAttributes);
        }

        var record = medicalRecordService.getMedicalRecordById(id);

        MedicalRecordRequestDTO form = new MedicalRecordRequestDTO(
                record.getAppointmentId(),
                record.getDiagnosis(),
                record.getTreatment(),
                record.getVaccineName(),
                record.getVaccineDate(),
                record.getNextVaccineDate(),
                record.getNotes()
        );

        model.addAttribute("medicalRecord", form);
        model.addAttribute("recordId", id);

        return "medicalrecord/form";
    }

        // รับข้อมูลที่แก้ไข แล้วส่งให้ Service บันทึก
    @PostMapping("/{id}/edit")
    public String updateMedicalRecord(
            @PathVariable Long id,
            @Valid @ModelAttribute("medicalRecord") MedicalRecordRequestDTO requestDTO,
            BindingResult bindingResult,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (isNotStaff(session)) {
            return redirectToStaffLogin(redirectAttributes);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("recordId", id);
            return "medicalrecord/form";
        }

        medicalRecordService.updateMedicalRecord(id, requestDTO);

        redirectAttributes.addFlashAttribute(
                "successMessage", "แก้ไขประวัติการรักษาสำเร็จ");

        return "redirect:/medical-records/" + id;
    }

        // ลบประวัติการรักษาตาม ID
    @PostMapping("/{id}/delete")
    public String deleteMedicalRecord(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (isNotStaff(session)) {
            return redirectToStaffLogin(redirectAttributes);
        }

        medicalRecordService.deleteMedicalRecord(id);

        redirectAttributes.addFlashAttribute(
                "successMessage", "ลบประวัติการรักษาสำเร็จ");

        return "redirect:/medical-records";
    }


// ถ้าไม่พบประวัติการรักษา ให้กลับหน้ารายการพร้อมแจ้งเตือน
@ExceptionHandler(ResourceNotFoundException.class)
public String handleRecordNotFound(
        ResourceNotFoundException ex,
        RedirectAttributes redirectAttributes) {

    redirectAttributes.addFlashAttribute(
            "errorMessage",
            "ไม่พบประวัติการรักษาที่ต้องการ");

    return "redirect:/medical-records";
}

}
