package com.example.petclinic.dto.response;

import java.math.BigDecimal;

public class ClinicConfigResponseDTO {

    private String clinicName;
    private String clinicAddress;
    private String contactPhone;
    private String emergencyPhone;
    private String openingHoursWeekdays;
    private String openingHoursSaturday;
    private String openingHoursSunday;
    private BigDecimal baseConsultationFee;
    private BigDecimal vaccineServiceFee;
    private BigDecimal surgeryServiceFee;
    private String systemVersion;
    private int beanHashCode;

    public ClinicConfigResponseDTO() {
    }

    public ClinicConfigResponseDTO(String clinicName, String clinicAddress, String contactPhone, 
                                   String emergencyPhone, String openingHoursWeekdays, 
                                   String openingHoursSaturday, String openingHoursSunday, 
                                   BigDecimal baseConsultationFee, BigDecimal vaccineServiceFee, 
                                   BigDecimal surgeryServiceFee, String systemVersion, int beanHashCode) {
        this.clinicName = clinicName;
        this.clinicAddress = clinicAddress;
        this.contactPhone = contactPhone;
        this.emergencyPhone = emergencyPhone;
        this.openingHoursWeekdays = openingHoursWeekdays;
        this.openingHoursSaturday = openingHoursSaturday;
        this.openingHoursSunday = openingHoursSunday;
        this.baseConsultationFee = baseConsultationFee;
        this.vaccineServiceFee = vaccineServiceFee;
        this.surgeryServiceFee = surgeryServiceFee;
        this.systemVersion = systemVersion;
        this.beanHashCode = beanHashCode;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getClinicAddress() {
        return clinicAddress;
    }

    public void setClinicAddress(String clinicAddress) {
        this.clinicAddress = clinicAddress;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }

    public String getOpeningHoursWeekdays() {
        return openingHoursWeekdays;
    }

    public void setOpeningHoursWeekdays(String openingHoursWeekdays) {
        this.openingHoursWeekdays = openingHoursWeekdays;
    }

    public String getOpeningHoursSaturday() {
        return openingHoursSaturday;
    }

    public void setOpeningHoursSaturday(String openingHoursSaturday) {
        this.openingHoursSaturday = openingHoursSaturday;
    }

    public String getOpeningHoursSunday() {
        return openingHoursSunday;
    }

    public void setOpeningHoursSunday(String openingHoursSunday) {
        this.openingHoursSunday = openingHoursSunday;
    }

    public BigDecimal getBaseConsultationFee() {
        return baseConsultationFee;
    }

    public void setBaseConsultationFee(BigDecimal baseConsultationFee) {
        this.baseConsultationFee = baseConsultationFee;
    }

    public BigDecimal getVaccineServiceFee() {
        return vaccineServiceFee;
    }

    public void setVaccineServiceFee(BigDecimal vaccineServiceFee) {
        this.vaccineServiceFee = vaccineServiceFee;
    }

    public BigDecimal getSurgeryServiceFee() {
        return surgeryServiceFee;
    }

    public void setSurgeryServiceFee(BigDecimal surgeryServiceFee) {
        this.surgeryServiceFee = surgeryServiceFee;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    public int getBeanHashCode() {
        return beanHashCode;
    }

    public void setBeanHashCode(int beanHashCode) {
        this.beanHashCode = beanHashCode;
    }
}
