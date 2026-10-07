package com.example.petclinic.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.petclinic.dto.response.ClinicConfigResponseDTO;
import com.example.petclinic.service.ClinicConfigService;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*")
public class ClinicConfigController {

    private final ClinicConfigService clinicConfigService;

    public ClinicConfigController(ClinicConfigService clinicConfigService) {
        this.clinicConfigService = clinicConfigService;
    }

    // 1. ดึงการตั้งค่าของคลินิกทั้งหมด
    @GetMapping
    public ResponseEntity<ClinicConfigResponseDTO> getClinicConfig() {
        return ResponseEntity.ok(clinicConfigService.getConfigDTO());
    }

    // 2. ตรวจสอบและพิสูจน์ Singleton Pattern
    @GetMapping("/singleton-check")
    public ResponseEntity<Map<String, Object>> checkSingleton() {
        Map<String, Object> result = new HashMap<>();
        result.put("pattern", "Singleton Pattern (Spring Bean Managed)");
        result.put("beanClass", clinicConfigService.getClass().getName());
        result.put("instanceIdentityHashCode", clinicConfigService.getInstanceHashCode());
        result.put("message", "ทุก Request ที่เรียกเข้ามาจะได้รับ Service Instance เดิมในหน่วยความจำเสมอ (Single Shared Instance)");
        return ResponseEntity.ok(result);
    }

    // 3. แก้ไขอัตราค่าบริการ
    @PutMapping("/fees")
    public ResponseEntity<ClinicConfigResponseDTO> updateFees(
            @RequestParam(required = false) BigDecimal consultation,
            @RequestParam(required = false) BigDecimal vaccine,
            @RequestParam(required = false) BigDecimal surgery) {
        clinicConfigService.updatePricePolicy(consultation, vaccine, surgery);
        return ResponseEntity.ok(clinicConfigService.getConfigDTO());
    }
}
