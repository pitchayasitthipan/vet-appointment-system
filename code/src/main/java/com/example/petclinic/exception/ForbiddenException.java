package com.example.petclinic.exception;

// ใช้เมื่อไม่มีสิทธิ์ทำรายการนั้น เช่น ยังไม่ได้ใส่รหัสเจ้าหน้าที่ -> GlobalExceptionHandler ตอบ 403 Forbidden
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}