package com.example.petclinic.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "medical_record")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medical_record_id")
    private Long medicalRecordId;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "treatment", columnDefinition = "TEXT")
    private String treatment;

    @Column(name = "vaccine_name", length = 255)
    private String vaccineName;

    @Column(name = "vaccine_date")
    private LocalDate vaccineDate;

    @Column(name = "next_vaccine_date")
    private LocalDate nextVaccineDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public MedicalRecord() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
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

    // Builder Pattern: ช่วยสร้าง MedicalRecord ทีละฟิลด์ โดยไม่ต้องใช้ Constructor ที่มีพารามิเตอร์จำนวนมาก
    public static Builder builder() {
        return new Builder();
    }

    // เก็บค่าที่ต้องการก่อนสร้าง MedicalRecord จริง
    public static class Builder {
        private Long appointmentId;
        private String diagnosis;
        private String treatment;
        private String vaccineName;
        private LocalDate vaccineDate;
        private LocalDate nextVaccineDate;
        private String notes;

        public Builder appointmentId(Long appointmentId) {
            this.appointmentId = appointmentId;
            return this;
        }

        public Builder diagnosis(String diagnosis) {
            this.diagnosis = diagnosis;
            return this;
        }

        public Builder treatment(String treatment) {
            this.treatment = treatment;
            return this;
        }

        public Builder vaccineName(String vaccineName) {
            this.vaccineName = vaccineName;
            return this;
        }

        public Builder vaccineDate(LocalDate vaccineDate) {
            this.vaccineDate = vaccineDate;
            return this;
        }

        public Builder nextVaccineDate(LocalDate nextVaccineDate) {
            this.nextVaccineDate = nextVaccineDate;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        // สร้าง MedicalRecord จากค่าที่กำหนดไว้ใน Builder
        public MedicalRecord build() {
            MedicalRecord record = new MedicalRecord();
            record.setAppointmentId(appointmentId);
            record.setDiagnosis(diagnosis);
            record.setTreatment(treatment);
            record.setVaccineName(vaccineName);
            record.setVaccineDate(vaccineDate);
            record.setNextVaccineDate(nextVaccineDate);
            record.setNotes(notes);
            return record;
        }
    }
}