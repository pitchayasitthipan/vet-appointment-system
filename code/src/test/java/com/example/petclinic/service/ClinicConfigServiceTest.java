package com.example.petclinic.service;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.petclinic.dto.response.ClinicConfigResponseDTO;

/**
 * ทดสอบ Singleton Pattern ของ ClinicConfigService ผ่าน Spring IoC Container
 * โดยใช้ @ContextConfiguration เพื่อโหลดเฉพาะ Service Bean โดยไม่ต้องต่อฐานข้อมูลจริง
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ClinicConfigService.class)
class ClinicConfigServiceTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ClinicConfigService clinicConfigService1;

    @Autowired
    private ClinicConfigService clinicConfigService2;

    @Test
    @DisplayName("Singleton Test: Injection สองจุดต้องได้ Instance เดียวกันในหน่วยความจำเสมอ")
    void testSingletonScope_ShouldReturnSameInstance() {
        // Assert: ทั้งสองตัวแปรต้องชี้ไปยัง Object เดียวกันใน Memory (same identity)
        assertThat(clinicConfigService1).isSameAs(clinicConfigService2);
        assertThat(clinicConfigService1.getInstanceHashCode()).isEqualTo(clinicConfigService2.getInstanceHashCode());

        // ตรวจสอบผ่าน ApplicationContext ดึงซ้ำ 2 ครั้ง
        ClinicConfigService fromContext1 = applicationContext.getBean(ClinicConfigService.class);
        ClinicConfigService fromContext2 = applicationContext.getBean(ClinicConfigService.class);
        assertThat(fromContext1).isSameAs(fromContext2);
    }

    @Test
    @DisplayName("Config Values Test: ค่าเริ่มต้นของคลินิกต้องถูกต้องตามที่กำหนด")
    void testDefaultConfigValues() {
        ClinicConfigResponseDTO config = clinicConfigService1.getConfigDTO();

        assertThat(config.getClinicName()).isEqualTo("PawCare Pet Clinic & Vaccination");
        assertThat(config.getContactPhone()).isEqualTo("02-123-4567");
        assertThat(config.getBaseConsultationFee()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(config.getVaccineServiceFee()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(config.getSurgeryServiceFee()).isEqualByComparingTo(new BigDecimal("1500.00"));
    }

    @Test
    @DisplayName("Update Policy Test: อัปเดตราคาจาก Service หนึ่ง อีก Service หนึ่งต้องเห็นค่าใหม่ทันที (Shared State)")
    void testUpdateFeePolicy_SharedStateAcrossReferences() {
        BigDecimal newConsultationFee = new BigDecimal("350.00");
        BigDecimal newVaccineFee = new BigDecimal("200.00");
        BigDecimal newSurgeryFee = new BigDecimal("1800.00");

        // อัปเดตผ่าน reference ตัวที่ 1
        clinicConfigService1.updatePricePolicy(newConsultationFee, newVaccineFee, newSurgeryFee);

        // ตรวจสอบผ่าน reference ตัวที่ 2 ว่าเห็นการเปลี่ยนแปลงทันทีเพราะเป็น Instance เดียวกัน
        assertThat(clinicConfigService2.getBaseConsultationFee()).isEqualByComparingTo(newConsultationFee);
        assertThat(clinicConfigService2.getVaccineServiceFee()).isEqualByComparingTo(newVaccineFee);
        assertThat(clinicConfigService2.getSurgeryServiceFee()).isEqualByComparingTo(newSurgeryFee);
    }

    @Test
    @DisplayName("Update Policy Test: ค่าบริการติดลบต้องไม่ถูกบันทึก (IllegalArgumentException -> 400)")
    void testUpdateFeePolicy_NegativeFee_ShouldThrow() {
        BigDecimal before = clinicConfigService1.getVaccineServiceFee();

        assertThatThrownBy(() -> clinicConfigService1.updatePricePolicy(null, new BigDecimal("-1"), null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(clinicConfigService1.getVaccineServiceFee()).isEqualByComparingTo(before);
    }
}
