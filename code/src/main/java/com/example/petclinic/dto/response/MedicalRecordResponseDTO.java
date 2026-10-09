package com.example.petclinic.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.petclinic.domain.entity.MedicalRecord;

public class MedicalRecordResponseDTO {

    private Long medicalRecordId;
    private Long appointmentId;
    private String diagnosis;
    private String treatment;
    private String vaccineName;
    private LocalDate vaccineDate;
    private LocalDate nextVaccineDate;
    private String notes;
    private LocalDateTime createdAt;

    public MedicalRecordResponseDTO() {
    }

    public MedicalRecordResponseDTO(
            Long medicalRecordId,
            Long appointmentId,
            String diagnosis,
            String treatment,
            String vaccineName,
            LocalDate vaccineDate,
            LocalDate nextVaccineDate,
            String notes,
            LocalDateTime createdAt) {
        this.medicalRecordId = medicalRecordId;
        this.appointmentId = appointmentId;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.vaccineName = vaccineName;
        this.vaccineDate = vaccineDate;
        this.nextVaccineDate = nextVaccineDate;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public static MedicalRecordResponseDTO fromEntity(MedicalRecord medicalRecord) {
        if (medicalRecord == null) {
            return null;
        }

        return new MedicalRecordResponseDTO(
                medicalRecord.getMedicalRecordId(),
                medicalRecord.getAppointmentId(),
                medicalRecord.getDiagnosis(),
                medicalRecord.getTreatment(),
                medicalRecord.getVaccineName(),
                medicalRecord.getVaccineDate(),
                medicalRecord.getNextVaccineDate(),
                medicalRecord.getNotes(),
                medicalRecord.getCreatedAt()
        );
    }

    public Long getMedicalRecordId() {
        return medicalRecordId;
    }

    public void setMedicalRecordId(Long medicalRecordId) {
        this.medicalRecordId = medicalRecordId;
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

    public LocalDate getVaccineDate() {
        return vaccineDate;
    }

    public void setVaccineDate(LocalDate vaccineDate) {
        this.vaccineDate = vaccineDate;
    }

    public LocalDate getNextVaccineDate() {
        return nextVaccineDate;
    }

    public void setNextVaccineDate(LocalDate nextVaccineDate) {
        this.nextVaccineDate = nextVaccineDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}