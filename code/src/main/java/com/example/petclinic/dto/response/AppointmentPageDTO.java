package com.example.petclinic.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record AppointmentPageDTO(List<AppointmentResponseDTO> content, int number, int size,
    long totalElements, int totalPages) {
    public static AppointmentPageDTO from(Page<AppointmentResponseDTO> page) {
        return new AppointmentPageDTO(page.getContent(), page.getNumber(), page.getSize(),
            page.getTotalElements(), page.getTotalPages());
    }
}
