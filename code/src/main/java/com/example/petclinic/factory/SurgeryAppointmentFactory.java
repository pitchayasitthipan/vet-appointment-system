package com.example.petclinic.factory;

import com.example.petclinic.domain.entity.Appointment;
import com.example.petclinic.domain.enums.ServiceType;
import org.springframework.stereotype.Component;

@Component
public class SurgeryAppointmentFactory extends AppointmentFactory {
    @Override public ServiceType getServiceType() { return ServiceType.SURGERY; }

    @Override protected Appointment createAppointment() {
        Appointment appointment = new Appointment();
        appointment.setPreparationInstructions("ติดต่อคลินิกเพื่อรับคำแนะนำการเตรียมตัวเฉพาะจากสัตวแพทย์ก่อนวันนัด");
        return appointment;
    }
}
