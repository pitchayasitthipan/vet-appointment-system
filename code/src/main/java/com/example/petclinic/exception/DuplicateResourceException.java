package com.example.petclinic.exception;

// ใช้เมื่อข้อมูลซ้ำกับที่มีอยู่แล้ว เช่น อีเมลซ้ำ -> GlobalExceptionHandler ตอบ 409 Conflict
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
