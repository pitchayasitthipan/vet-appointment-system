package com.example.petclinic.dto.response;

import java.time.LocalDateTime;
import com.example.petclinic.domain.entity.Appointment;
import com.example.petclinic.domain.enums.*;

public record AppointmentResponseDTO(Long appointmentId, Long ownerId, Long petId, String petName,
    Long doctorId, String doctorName, LocalDateTime appointmentDateTime, ServiceType serviceType,
    AppointmentStatus status, String symptoms, String preparationInstructions, Long version) {
    public static AppointmentResponseDTO fromEntity(Appointment a) {
        return new AppointmentResponseDTO(a.getAppointmentId(), a.getPet().getPetOwner().getOwnerId(),
            a.getPet().getPetId(), a.getPet().getPetName(), a.getDoctor().getDoctorId(),
            a.getDoctor().getFirstName() + " " + a.getDoctor().getLastName(),
            a.getAppointmentDateTime(), a.getServiceType(), a.getStatus(), a.getSymptoms(),
            a.getPreparationInstructions(), a.getVersion());
    }
}
