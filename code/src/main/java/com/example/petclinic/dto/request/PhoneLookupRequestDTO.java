package com.example.petclinic.dto.request;

import jakarta.validation.constraints.*;

public record PhoneLookupRequestDTO(@NotBlank @Size(max = 20) String phone) { }
