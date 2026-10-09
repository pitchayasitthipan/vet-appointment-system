package com.example.petclinic.factory;

import com.example.petclinic.domain.entity.Appointment;
import com.example.petclinic.domain.enums.ServiceType;
import org.springframework.stereotype.Component;

@Component
public class ConsultationAppointmentFactory extends AppointmentFactory {
    @Override public ServiceType getServiceType() { return ServiceType.CONSULTATION; }

    @Override protected Appointment createAppointment() {
        Appointment appointment = new Appointment();
        appointment.setPreparationInstructions("นำข้อมูลอาการและประวัติการรักษามาด้วย");
        return appointment;
    }
}
