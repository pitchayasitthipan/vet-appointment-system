package com.example.petclinic.dto.request;

import java.time.LocalDateTime;
import com.example.petclinic.domain.enums.ServiceType;
import jakarta.validation.constraints.*;

/** AppointmentPet and owner stay fixed when rescheduling; create a new booking for another pet. */
public record AppointmentUpdateDTO(
    @NotNull @Positive Long doctorId,
    @NotNull LocalDateTime appointmentDateTime,
    @NotNull ServiceType serviceType,
    @NotBlank @Size(max = 1000) String symptoms,
    @NotNull @PositiveOrZero Long version
) { }
