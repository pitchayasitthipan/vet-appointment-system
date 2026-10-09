package com.example.petclinic.controller.api;

import java.net.URI;
import java.time.*;
import java.util.List;
import com.example.petclinic.domain.enums.AppointmentStatus;
import com.example.petclinic.dto.request.*;
import com.example.petclinic.dto.response.*;
import com.example.petclinic.service.AppointmentService;
import jakarta.validation.Valid;
import com.example.petclinic.controller.StaffAccess;
import jakarta.servlet.http.HttpSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appointments")
@Tag(name = "Appointments", description = "สร้าง ดู เลื่อน และยกเลิกนัดของแฟ้มที่เลือกใน session")
public class AppointmentController {
    private final AppointmentService service;
    public AppointmentController(AppointmentService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "สร้างนัดสำหรับเจ้าของที่ค้นหาแล้ว หรือแฟ้มที่ Staff เลือก")
    public ResponseEntity<AppointmentResponseDTO> create(@Valid @RequestBody AppointmentRequestDTO request,
            HttpSession session) {
        AppointmentAccess.owner(session, request.ownerId());
        AppointmentResponseDTO result = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/appointments/" + result.appointmentId()
            + "?ownerId=" + result.ownerId())).body(result);
    }

    @GetMapping
    @Operation(summary = "ดูรายการนัดของแฟ้มที่ได้รับอนุญาต พร้อมกรองและแบ่งหน้า")
    public AppointmentPageDTO list(@RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentDateTime") String sort,
            @RequestParam(defaultValue = "asc") String direction, HttpSession session) {
        return service.list(AppointmentAccess.owner(session, ownerId), status, page, size, sort, direction);
    }

    @GetMapping("/availability")
    @Operation(summary = "ตรวจคิวว่างของหมอตามวัน ไม่แสดงข้อมูลเจ้าของหรือนัด")
    public List<LocalDateTime> availability(@RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.availability(doctorId, date);
    }

    @GetMapping("/{id}")
    @Operation(summary = "ดูรายละเอียดนัดในแฟ้มที่ได้รับอนุญาต")
    public AppointmentResponseDTO get(@PathVariable Long id, @RequestParam(required = false) Long ownerId,
            HttpSession session) {
        return service.get(id, AppointmentAccess.owner(session, ownerId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "เลื่อนหรือแก้ไขนัดในแฟ้มที่ได้รับอนุญาต โดยตรวจ version")
    public AppointmentResponseDTO update(@PathVariable Long id, @RequestParam(required = false) Long ownerId,
            @Valid @RequestBody AppointmentUpdateDTO request, HttpSession session) {
        return service.update(id, AppointmentAccess.owner(session, ownerId), request);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "ยกเลิกนัดในแฟ้มที่ได้รับอนุญาตและคืนคิว")
    public AppointmentResponseDTO cancel(@PathVariable Long id, @RequestParam(required = false) Long ownerId,
            HttpSession session) {
        return service.cancel(id, AppointmentAccess.owner(session, ownerId));
    }
    @GetMapping("/staff")
    @Operation(summary = "ดูนัดทั้งคลินิก พร้อมกรองและแบ่งหน้า [เฉพาะเจ้าหน้าที่]")
    public AppointmentPageDTO clinic(@RequestParam(required = false) AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentDateTime") String sort,
            @RequestParam(defaultValue = "asc") String direction, HttpSession session) {
        StaffAccess.requireStaff(session);
        return service.listClinic(status, page, size, sort, direction);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "ยืนยันหรือปิดนัด โดยตรวจ version [เฉพาะเจ้าหน้าที่]")
    public AppointmentResponseDTO changeStatus(@PathVariable Long id,
            @Valid @RequestBody AppointmentStatusUpdateDTO request, HttpSession session) {
        StaffAccess.requireStaff(session);
        return service.changeStatus(id, request);
    }
}
