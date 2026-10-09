package com.example.petclinic.factory;

import java.time.LocalDateTime;
import com.example.petclinic.domain.entity.*;
import com.example.petclinic.domain.enums.*;
import com.example.petclinic.exception.InvalidAppointmentException;

/** Creator: subclasses implement the factory method for their service type. */
public abstract class AppointmentFactory {
    public abstract ServiceType getServiceType();

    protected abstract Appointment createAppointment();

    /** Common assembly; ownership, schedule and conflict checks belong to the service layer. */
    public final Appointment create(Pet pet, Doctor doctor, LocalDateTime time, String symptoms) {
        if (pet == null || doctor == null || time == null) {
            throw new InvalidAppointmentException("กรุณาระบุสัตว์เลี้ยง สัตวแพทย์ และวันเวลานัดหมาย");
        }
        if (symptoms == null || symptoms.isBlank() || symptoms.length() > 1000) {
            throw new InvalidAppointmentException("กรุณาระบุรายละเอียดอาการไม่เกิน 1000 ตัวอักษร");
        }
        Appointment appointment = createAppointment();
        appointment.setServiceType(getServiceType());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setPet(pet);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(time);
        appointment.setSymptoms(symptoms.trim());
        return appointment;
    }
}
