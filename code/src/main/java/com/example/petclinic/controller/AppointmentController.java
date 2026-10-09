package com.example.petclinic.controller;

import java.net.URI;
import java.time.*;
import java.util.List;
import com.example.petclinic.domain.enums.AppointmentStatus;
import com.example.petclinic.dto.request.*;
import com.example.petclinic.dto.response.*;
import com.example.petclinic.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService service;
    public AppointmentController(AppointmentService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> create(@Valid @RequestBody AppointmentRequestDTO request) {
        AppointmentResponseDTO result = service.create(request);
        return ResponseEntity.created(URI.create("/api/appointments/" + result.appointmentId()
            + "?ownerId=" + result.ownerId())).body(result);
    }

    @GetMapping
    public AppointmentPageDTO list(@RequestParam Long ownerId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentDateTime") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        return service.list(ownerId, status, page, size, sort, direction);
    }

    @GetMapping("/availability")
    public List<LocalDateTime> availability(@RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.availability(doctorId, date);
    }

    @GetMapping("/{id}")
    public AppointmentResponseDTO get(@PathVariable Long id, @RequestParam Long ownerId) {
        return service.get(id, ownerId);
    }

    @PutMapping("/{id}")
    public AppointmentResponseDTO update(@PathVariable Long id, @RequestParam Long ownerId,
            @Valid @RequestBody AppointmentUpdateDTO request) {
        return service.update(id, ownerId, request);
    }

    @PatchMapping("/{id}/cancel")
    public AppointmentResponseDTO cancel(@PathVariable Long id, @RequestParam Long ownerId) {
        return service.cancel(id, ownerId);
    }
}
