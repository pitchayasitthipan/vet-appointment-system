package com.example.petclinic.exception;

public class AppointmentAccessException extends RuntimeException {
    public AppointmentAccessException(String message) { super(message); }
}
