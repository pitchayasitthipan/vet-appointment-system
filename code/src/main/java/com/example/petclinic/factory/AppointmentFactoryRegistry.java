package com.example.petclinic.factory;

import java.util.*;
import com.example.petclinic.domain.entity.*;
import com.example.petclinic.domain.enums.ServiceType;
import com.example.petclinic.dto.request.AppointmentRequestDTO;
import com.example.petclinic.exception.InvalidAppointmentException;
import org.springframework.stereotype.Component;

/** Selects a concrete creator; never persists an appointment itself. */
@Component
public class AppointmentFactoryRegistry {
    private final Map<ServiceType, AppointmentFactory> factories;

    public AppointmentFactoryRegistry(List<AppointmentFactory> creators) {
        EnumMap<ServiceType, AppointmentFactory> registered = new EnumMap<>(ServiceType.class);
        for (AppointmentFactory creator : creators) {
            if (registered.putIfAbsent(creator.getServiceType(), creator) != null) {
                throw new IllegalStateException("Duplicate appointment factory: " + creator.getServiceType());
            }
        }
        if (registered.size() != ServiceType.values().length) {
            throw new IllegalStateException("A factory is required for every appointment service type");
        }
        factories = Map.copyOf(registered);
    }

    public Appointment create(AppointmentRequestDTO request, Pet pet, Doctor doctor) {
        if (request == null || request.serviceType() == null) {
            throw new InvalidAppointmentException("กรุณาเลือกประเภทบริการ");
        }
        return factories.get(request.serviceType()).create(pet, doctor,
            request.appointmentDateTime(), request.symptoms());
    }
}
