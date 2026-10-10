package com.example.petclinic.dto.request;

import com.example.petclinic.domain.enums.AppointmentStatus;
import jakarta.validation.constraints.*;

public record AppointmentStatusUpdateDTO(@NotNull AppointmentStatus status, @NotNull @PositiveOrZero Long version) { }
