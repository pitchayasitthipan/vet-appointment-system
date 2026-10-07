package com.example.petclinic.exception;

// ใช้เมื่อค้นหาข้อมูลตาม Id แล้วไม่พบ -> GlobalExceptionHandler ตอบ 404 Not Found
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
