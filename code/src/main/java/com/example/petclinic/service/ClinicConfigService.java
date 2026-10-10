package com.example.petclinic.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.example.petclinic.dto.response.ClinicConfigResponseDTO;

/**
 * ClinicConfigService - บริหารจัดการค่ากำหนดของคลินิกด้วย Singleton Pattern ผ่าน Spring Bean
 * 
 * Spring IoC Container จะสร้าง Instance ของ Bean นี้เพียง Instance เดียวตลอดวงจรชีวิตของแอปพลิเคชัน
 * (Application Lifecycle) และนำไปใช้ร่วมกัน (Shared Instance) ในทุกจุดที่มีการเรียกใช้งาน
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ClinicConfigService {

    // ข้อมูลทั่วไปของคลินิก
    private String clinicName = "PawCare Pet Clinic & Vaccination";
    private String clinicAddress = "123 ถนนพหลโยธิน แขวงลาดยาว เขตจตุจักร กรุงเทพฯ 10900";
    private String contactPhone = "02-123-4567";
    private String emergencyPhone = "089-999-8888";

    // เวลาเปิดให้บริการ
    private String openingHoursWeekdays = "09:00 - 20:00 น.";
    private String openingHoursSaturday = "09:00 - 18:00 น.";
    private String openingHoursSunday = "09:00 - 17:00 น.";

    // อัตราค่าบริการเริ่มต้น (Clinic Price Policy)
    private BigDecimal baseConsultationFee = new BigDecimal("300.00");
    private BigDecimal vaccineServiceFee = new BigDecimal("150.00");
    private BigDecimal surgeryServiceFee = new BigDecimal("1500.00");

    private String systemVersion = "1.0.0";

    public ClinicConfigService() {
    }

    /**
     * ดึงข้อมูลการตั้งค่าทั้งหมดของคลินิกในรูปแบบ DTO
     */
    public synchronized ClinicConfigResponseDTO getConfigDTO() {
        return new ClinicConfigResponseDTO(
                clinicName,
                clinicAddress,
                contactPhone,
                emergencyPhone,
                openingHoursWeekdays,
                openingHoursSaturday,
                openingHoursSunday,
                baseConsultationFee,
                vaccineServiceFee,
                surgeryServiceFee,
                systemVersion,
                getInstanceHashCode()
        );
    }

    /**
     * คืนค่า Identity HashCode ของ Instance ในหน่วยความจำ
     * เพื่อใช้พิสูจน์ว่าเป็น Singleton Instance เดียวกันเสมอ
     */
    public int getInstanceHashCode() {
        return System.identityHashCode(this);
    }

    // Thread-safe update methods
    public synchronized void updateContactInfo(String contactPhone, String emergencyPhone) {
        this.contactPhone = contactPhone;
        this.emergencyPhone = emergencyPhone;
    }

    public synchronized void updatePricePolicy(BigDecimal consultation, BigDecimal vaccine, BigDecimal surgery) {
        // ค่าบริการติดลบไม่ได้ -> IllegalArgumentException (400 ผ่าน GlobalExceptionHandler)
        for (BigDecimal fee : new BigDecimal[] { consultation, vaccine, surgery }) {
            if (fee != null && fee.signum() < 0) {
                throw new IllegalArgumentException("ค่าบริการต้องไม่ติดลบ");
            }
        }
        if (consultation != null) this.baseConsultationFee = consultation;
        if (vaccine != null) this.vaccineServiceFee = vaccine;
        if (surgery != null) this.surgeryServiceFee = surgery;
    }

    // Getters & Setters
    public String getClinicName() {
        return clinicName;
    }

    public String getClinicAddress() {
        return clinicAddress;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public String getOpeningHoursWeekdays() {
        return openingHoursWeekdays;
    }

    public String getOpeningHoursSaturday() {
        return openingHoursSaturday;
    }

    public String getOpeningHoursSunday() {
        return openingHoursSunday;
    }

    public BigDecimal getBaseConsultationFee() {
        return baseConsultationFee;
    }

    public BigDecimal getVaccineServiceFee() {
        return vaccineServiceFee;
    }

    public BigDecimal getSurgeryServiceFee() {
        return surgeryServiceFee;
    }

    public String getSystemVersion() {
        return systemVersion;
    }
}
