package com.example.petclinic.dto.response;

public record AppointmentGuestSessionDTO(Long ownerId, String firstName, String lastName, boolean isStaff) { }
