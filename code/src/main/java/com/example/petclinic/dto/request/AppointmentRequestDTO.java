package com.example.petclinic.dto.request;

import java.time.LocalDateTime;
import com.example.petclinic.domain.enums.ServiceType;
import jakarta.validation.constraints.*;

public record AppointmentRequestDTO(
    @NotNull @Positive Long ownerId,
    @NotNull @Positive Long petId,
    @NotNull @Positive Long doctorId,
    @NotNull LocalDateTime appointmentDateTime,
    @NotNull ServiceType serviceType,
    @NotBlank @Size(max = 1000) String symptoms
) { }
