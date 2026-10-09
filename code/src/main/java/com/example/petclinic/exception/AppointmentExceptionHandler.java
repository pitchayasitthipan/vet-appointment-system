package com.example.petclinic.exception;

import java.time.*;
import java.util.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.petclinic.controller.api.AppointmentController;
import com.example.petclinic.controller.api.AppointmentGuestController;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Uses the shared error shape without changing other modules' exception behavior. */
@RestControllerAdvice(assignableTypes = {AppointmentController.class, AppointmentGuestController.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AppointmentExceptionHandler {
    private final Clock clock;
    @Autowired
    public AppointmentExceptionHandler(ObjectProvider<Clock> clocks) {
        this(clocks.getIfAvailable(() -> Clock.system(ZoneId.of("Asia/Bangkok"))));
    }
    public AppointmentExceptionHandler(Clock appointmentClock) { this.clock = appointmentClock; }

    // Shared StaffAccess ForbiddenException is handled by GlobalExceptionHandler.
    @ExceptionHandler(AppointmentAccessException.class)
    public ResponseEntity<Map<String, Object>> forbidden(RuntimeException ex) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(InvalidAppointmentException.class)
    public ResponseEntity<Map<String, Object>> invalid(InvalidAppointmentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class})
    public ResponseEntity<Map<String, Object>> malformed(Exception ex) {
        return error(HttpStatus.BAD_REQUEST, "ข้อมูลไม่ถูกต้อง กรุณาตรวจรหัส ประเภทบริการ สถานะ และรูปแบบวันเวลา");
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<Map<String, Object>> concurrent(ConcurrencyFailureException ex) {
        return error(HttpStatus.CONFLICT, "มีการจองหรือแก้ไขข้อมูลพร้อมกัน กรุณาโหลดข้อมูลล่าสุดแล้วลองใหม่");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("timestamp", LocalDateTime.now(clock),
            "status", status.value(), "error", status.getReasonPhrase(), "message", message));
    }
}
