
package com.example.petclinic.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.petclinic.service.MedicalRecordService;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.petclinic.dto.request.MedicalRecordRequestDTO;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/medical-records")
public class MedicalRecordWebController {

    private final MedicalRecordService medicalRecordService;

    // รับ Service เพื่อดึงข้อมูลประวัติการรักษา
    public MedicalRecordWebController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    // เปิดหน้ารายการประวัติการรักษา
    @GetMapping
    public String listMedicalRecords(Model model) {

        // ส่งข้อมูลจาก Service ไปให้หน้า HTML
        model.addAttribute(
                "medicalRecords",
                medicalRecordService.getAllMedicalRecords()
        );

        // เปิดไฟล์ templates/medicalrecord/list.html
        return "medicalrecord/list";
    }


    // เปิดฟอร์มสำหรับเพิ่มประวัติการรักษา
    @GetMapping("/new")
    public String showCreateForm(Model model) {

        // ส่ง DTO เปล่าให้ Thymeleaf ใช้รับข้อมูลจากฟอร์ม
        model.addAttribute("medicalRecord", new MedicalRecordRequestDTO());

        return "medicalrecord/form";
    }

    // รับข้อมูลจากฟอร์มแล้วบันทึกผ่าน Service
    @PostMapping
    public String createMedicalRecord(
            @Valid @ModelAttribute("medicalRecord") MedicalRecordRequestDTO requestDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        // ถ้าข้อมูลไม่ถูกต้อง ให้กลับไปแสดงข้อผิดพลาดในฟอร์ม
        if (bindingResult.hasErrors()) {
            return "medicalrecord/form";
        }

        medicalRecordService.createMedicalRecord(requestDTO);

        // บันทึกสำเร็จแล้วกลับไปหน้ารายการ
        redirectAttributes.addFlashAttribute(
                "successMessage", "บันทึกประวัติการรักษาสำเร็จ");

        return "redirect:/medical-records";
    }

}
