package com.example.petclinic.service;

import java.time.*;
import java.util.List;
import com.example.petclinic.domain.enums.AppointmentStatus;
import com.example.petclinic.dto.request.*;
import com.example.petclinic.dto.response.*;

public interface AppointmentService {
    AppointmentResponseDTO create(AppointmentRequestDTO request);
    AppointmentResponseDTO update(Long id, Long ownerId, AppointmentUpdateDTO request);
    AppointmentResponseDTO cancel(Long id, Long ownerId);
    AppointmentResponseDTO get(Long id, Long ownerId);
    AppointmentPageDTO list(Long ownerId, AppointmentStatus status, int page, int size, String sort, String direction);
    List<LocalDateTime> availability(Long doctorId, LocalDate date);
    AppointmentPageDTO listClinic(AppointmentStatus status, int page, int size, String sort, String direction);
    AppointmentResponseDTO changeStatus(Long id, AppointmentStatusUpdateDTO request);
}
