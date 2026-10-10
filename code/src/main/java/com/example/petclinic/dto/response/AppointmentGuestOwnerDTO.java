package com.example.petclinic.dto.response;

/** Return only the selected owner's identity; do not expose owner lists or contact details. */
public record AppointmentGuestOwnerDTO(Long ownerId, String firstName, String lastName) { }
