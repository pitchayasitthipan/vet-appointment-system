package com.example.petclinic.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

// ดักจับ Exception จากทุก Controller แล้วตอบกลับเป็น JSON รูปแบบเดียวกันทั้งระบบ
// timestamp, status, error, message, path [+ errors เมื่อ Validation ไม่ผ่าน]
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400: Validation ไม่ผ่าน (@Valid) -> ส่งกลับทุก field ที่ผิด
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> errorResponse = buildError(HttpStatus.BAD_REQUEST, "ข้อมูลไม่ถูกต้อง", request);
        errorResponse.put("errors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // 400: คำขอผิดรูปแบบ
    // HttpMessageNotReadableException: JSON ผิดรูปแบบ หรือว่า ไม่ครบ
    // MethodArgumentTypeMismatchException: ชนิดข้อมูลผิด เช่น id ไม่ใช่ตัวเลข
    // PropertyReferenceException: sort ด้วย field ที่ไม่มี
    @ExceptionHandler({ HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            PropertyReferenceException.class })
    public ResponseEntity<Map<String, Object>> handleBadRequestFormat(
            Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(HttpStatus.BAD_REQUEST, "รูปแบบคำขอไม่ถูกต้อง", request));
    }

    // 400: ค่าที่ส่งมาผิดเงื่อนไขทาง business
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request));
    }

    // 404: ไม่พบข้อมูลตาม Id
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request));
    }

    // 404: เรียก URL ที่ไม่มีอยู่ในระบบ
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, "ไม่พบ URL ที่ร้องขอ", request));
    }

    // 405: ใช้ HTTP Method ที่ Endpoint นั้นไม่รองรับ
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(buildError(HttpStatus.METHOD_NOT_ALLOWED,
                        "ไม่รองรับ Method " + ex.getMethod() + " สำหรับ URL นี้", request));
    }

    // 409: ข้อมูลซ้ำ เช่น อีเมลซ้ำ
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResourceException(
            DuplicateResourceException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, ex.getMessage(), request));
    }

    // 409: ผิดเงื่อนไขของฐานข้อมูล เช่น Unique ซ้ำ
    // หรือลบข้อมูลที่เป็ฯ Foreign Key
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, "ข้อมูลขัดแย้งกับข้อมูลที่มีอยู่ในระบบ", request));
    }

    // 500: Error อื่นๆ ที่ไม่ได้คาดไว้
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(
            Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR, "เกิดข้อผิดพลาดภายในระบบ", request));
    }

    // สร้าง Error Response รูปแบบเดียวกันทุกกรณี
    private Map<String, Object> buildError(HttpStatus status, String message, HttpServletRequest request) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("path", request.getRequestURI());
        return errorResponse;
    }
}
