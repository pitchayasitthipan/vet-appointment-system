
package com.example.petclinic.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.petclinic.service.MedicalRecordService;

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

    // แสดงรายละเอียดประวัติการรักษาตาม ID
    @GetMapping("/{id}")
    public String viewMedicalRecord(
            @PathVariable Long id,
            Model model) {

        // ดึงข้อมูลรายการที่เลือกจาก Service
        model.addAttribute(
            "record",
            medicalRecordService.getMedicalRecordById(id)
    );

    return "medicalrecord/detail";
}

// เปิดฟอร์มแก้ไข โดยดึงข้อมูลเดิมมาแสดง
@GetMapping("/{id}/edit")
public String showEditForm(@PathVariable Long id, Model model) {

    var record = medicalRecordService.getMedicalRecordById(id);

    // นำข้อมูลเดิมใส่ DTO เพื่อให้ฟอร์มแสดงค่าเดิม
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
        RedirectAttributes redirectAttributes) {

    if (bindingResult.hasErrors()) {
        // ส่ง ID กลับไป เพื่อให้ฟอร์มยังรู้ว่ากำลังแก้ไขรายการไหน
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
        RedirectAttributes redirectAttributes) {

    // เรียก Service เพื่อลบข้อมูลที่เลือก
    medicalRecordService.deleteMedicalRecord(id);

    // แจ้งผลหลังลบ แล้วกลับไปหน้ารายการ
    redirectAttributes.addFlashAttribute(
            "successMessage", "ลบประวัติการรักษาสำเร็จ");

    return "redirect:/medical-records";
}

}
