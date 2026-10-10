package com.example.petclinic.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.petclinic.service.ClinicConfigService;
import com.example.petclinic.service.DoctorService;

// หน้า Veterinarians: รายชื่อสัตวแพทย์และตารางเวร ข้อมูลจริงจากฐานข้อมูล
// ทุกคนดูได้, ปุ่มเพิ่ม/แก้ไข/ลบ แสดงเฉพาะเจ้าหน้าที่ (isStaff มาจาก WebModelAdvice)
@Controller
public class DoctorWebController {

    private final DoctorService doctorService;

    // ข้อมูลคลินิก เวลาเปิด และค่าบริการ (Singleton Pattern)
    private final ClinicConfigService clinicConfigService;

    public DoctorWebController(DoctorService doctorService, ClinicConfigService clinicConfigService) {
        this.doctorService = doctorService;
        this.clinicConfigService = clinicConfigService;
    }

    @GetMapping({"/doctors", "/veterinarians"})
    public String doctorPage(Model model) {
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("clinic", clinicConfigService.getConfigDTO());
        return "doctor/list";
    }

    // ลิงก์เก่าที่ยังชี้ไปไฟล์ static เดิม -> ไปหน้าใหม่
    @GetMapping("/doctors.html")
    public String oldDoctorPage() {
        return "redirect:/doctors";
    }
}
