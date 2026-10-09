package com.example.petclinic.controller.api;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.petclinic.controller.StaffAccess;
import com.example.petclinic.dto.response.ClinicConfigResponseDTO;
import com.example.petclinic.service.ClinicConfigService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;

@RestController
@Tag(name = "Clinic Config", description = "ค่ากำหนดของคลินิก (Singleton Pattern)")
@RequestMapping("/api/v1/config")
public class ClinicConfigController {

    private final ClinicConfigService clinicConfigService;

    public ClinicConfigController(ClinicConfigService clinicConfigService) {
        this.clinicConfigService = clinicConfigService;
    }

    // 1. ดึงการตั้งค่าของคลินิกทั้งหมด
    @Operation(summary = "ดึงข้อมูลคลินิก เวลาเปิดทำการ และอัตราค่าบริการ")
    @GetMapping
    public ResponseEntity<ClinicConfigResponseDTO> getClinicConfig() {
        return ResponseEntity.ok(clinicConfigService.getConfigDTO());
    }

    // 2. ตรวจสอบและพิสูจน์ Singleton Pattern
    @Operation(summary = "ตรวจสอบว่าเป็น Instance เดียวกันทุกครั้ง (Singleton Pattern)")
    @GetMapping("/singleton-check")
    public ResponseEntity<Map<String, Object>> checkSingleton() {
        Map<String, Object> result = new HashMap<>();
        result.put("pattern", "Singleton Pattern (Spring Bean Managed)");
        result.put("beanClass", clinicConfigService.getClass().getName());
        result.put("instanceIdentityHashCode", clinicConfigService.getInstanceHashCode());
        result.put("message", "ทุก Request ที่เรียกเข้ามาจะได้รับ Service Instance เดิมในหน่วยความจำเสมอ (Single Shared Instance)");
        return ResponseEntity.ok(result);
    }

    // 3. แก้ไขอัตราค่าบริการ (เฉพาะเจ้าหน้าที่ ค่าติดลบได้ 400)
    @Operation(summary = "แก้ไขอัตราค่าบริการ [เฉพาะเจ้าหน้าที่]")
    @PutMapping("/fees")
    public ResponseEntity<ClinicConfigResponseDTO> updateFees(
            @RequestParam(required = false) BigDecimal consultation,
            @RequestParam(required = false) BigDecimal vaccine,
            @RequestParam(required = false) BigDecimal surgery,
            HttpSession session) {
        StaffAccess.requireStaff(session);
        clinicConfigService.updatePricePolicy(consultation, vaccine, surgery);
        return ResponseEntity.ok(clinicConfigService.getConfigDTO());
    }
}
