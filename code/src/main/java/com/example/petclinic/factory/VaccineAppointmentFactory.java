package com.example.petclinic.factory;

import com.example.petclinic.domain.entity.Appointment;
import com.example.petclinic.domain.enums.ServiceType;
import org.springframework.stereotype.Component;

@Component
public class VaccineAppointmentFactory extends AppointmentFactory {
    @Override public ServiceType getServiceType() { return ServiceType.VACCINE; }

    @Override protected Appointment createAppointment() {
        Appointment appointment = new Appointment();
        appointment.setPreparationInstructions("นำสมุดวัคซีนมาด้วย และแจ้งสัตวแพทย์หากสัตว์เลี้ยงมีอาการป่วย");
        return appointment;
    }
}
