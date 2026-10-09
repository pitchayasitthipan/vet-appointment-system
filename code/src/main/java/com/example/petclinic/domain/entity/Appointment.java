package com.example.petclinic.domain.entity;

import java.time.LocalDateTime;
import com.example.petclinic.domain.enums.*;
import jakarta.persistence.*;

@Entity
@Table(name = "appointment", indexes = {
    @Index(name = "idx_appointment_doctor_time", columnList = "doctor_id,appointment_date_time"),
    @Index(name = "idx_appointment_pet_time", columnList = "pet_id,appointment_date_time")
})
public class Appointment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long appointmentId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private AppointmentPet pet;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;
    @Column(name = "appointment_date_time", nullable = false)
    private LocalDateTime appointmentDateTime;
    @Enumerated(EnumType.STRING) @Column(name = "service_type", nullable = false, length = 20)
    private ServiceType serviceType;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.PENDING;
    @Column(length = 1000)
    private String symptoms;
    @Column(name = "preparation_instructions", length = 500)
    private String preparationInstructions;
    @Version private Long version;

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long id) { this.appointmentId = id; }
    public AppointmentPet getPet() { return pet; }
    public void setPet(AppointmentPet pet) { this.pet = pet; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(LocalDateTime time) { this.appointmentDateTime = time; }
    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType type) { this.serviceType = type; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public String getPreparationInstructions() { return preparationInstructions; }
    public void setPreparationInstructions(String instructions) { this.preparationInstructions = instructions; }
    public Long getVersion() { return version; }
}
