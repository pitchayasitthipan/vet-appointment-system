package com.example.petclinic.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class MedicalRecordRequestDTO {

    @NotNull(message = "กรุณาระบุรหัสการนัดหมาย")
    @Positive(message = "รหัสการนัดหมายต้องมากกว่า 0")
    private Long appointmentId;

    @Size(max = 2000, message = "การวินิจฉัยต้องมีความยาวไม่เกิน 2000 ตัวอักษร")
    private String diagnosis;

    @Size(max = 2000, message = "การรักษาต้องมีความยาวไม่เกิน 2000 ตัวอักษร")
    private String treatment;

    @Size(max = 150, message = "ชื่อวัคซีนต้องมีความยาวไม่เกิน 150 ตัวอักษร")
    private String vaccineName;

    private java.time.LocalDate vaccineDate;

    private java.time.LocalDate nextVaccineDate;

    @Size(max = 2000, message = "หมายเหตุต้องมีความยาวไม่เกิน 2000 ตัวอักษร")
    private String notes;

    public MedicalRecordRequestDTO() {
    }

    public MedicalRecordRequestDTO(
            Long appointmentId,
            String diagnosis,
            String treatment,
            String vaccineName,
            java.time.LocalDate vaccineDate,
            java.time.LocalDate nextVaccineDate,
            String notes) {
        this.appointmentId = appointmentId;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.vaccineName = vaccineName;
        this.vaccineDate = vaccineDate;
        this.nextVaccineDate = nextVaccineDate;
        this.notes = notes;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }

    public java.time.LocalDate getVaccineDate() {
        return vaccineDate;
    }

    public void setVaccineDate(java.time.LocalDate vaccineDate) {
        this.vaccineDate = vaccineDate;
    }

    public java.time.LocalDate getNextVaccineDate() {
        return nextVaccineDate;
    }

    public void setNextVaccineDate(java.time.LocalDate nextVaccineDate) {
        this.nextVaccineDate = nextVaccineDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}