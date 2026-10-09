package com.example.petclinic.controller;

import java.util.*;
import com.example.petclinic.dto.request.PhoneLookupRequestDTO;
import com.example.petclinic.dto.response.*;
import com.example.petclinic.service.AppointmentGuestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointment-guests")
public class AppointmentGuestController {
    private final AppointmentGuestService service;
    private final String registrationPath;
    @Value("${appointments.owner-registration-enabled:false}")
    private boolean registrationEnabled;
    @Value("${appointments.pet-registration-enabled:false}")
    private boolean petRegistrationEnabled;
    public AppointmentGuestController(AppointmentGuestService service,
            @Value("${appointments.owner-registration-path:/owners/new}") String registrationPath) {
        this.service = service; this.registrationPath = registrationPath;
    }
    @PostMapping("/lookup")
    public AppointmentGuestOwnerDTO lookup(@Valid @RequestBody PhoneLookupRequestDTO request) {
        return service.lookup(request.phone());
    }
    @GetMapping("/{ownerId}/pets")
    public List<AppointmentGuestPetDTO> pets(@PathVariable Long ownerId) { return service.pets(ownerId); }

    @GetMapping("/config")
    public Map<String, String> config() {
        return Map.of("ownerRegistrationPath", registrationPath, "bookingReturnPath", "/appointment-create.html",
            "ownerRegistrationEnabled", Boolean.toString(registrationEnabled),
            "petRegistrationEnabled", Boolean.toString(petRegistrationEnabled));
    }
}
