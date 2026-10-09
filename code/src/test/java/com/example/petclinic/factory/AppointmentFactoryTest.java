package com.example.petclinic.factory;

import java.time.LocalDateTime;
import java.util.List;
import com.example.petclinic.domain.entity.*;
import com.example.petclinic.domain.enums.*;
import com.example.petclinic.dto.request.AppointmentRequestDTO;
import com.example.petclinic.exception.InvalidAppointmentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import static org.assertj.core.api.Assertions.*;

class AppointmentFactoryTest {
    private final List<AppointmentFactory> factories = List.of(new ConsultationAppointmentFactory(),
        new VaccineAppointmentFactory(), new SurgeryAppointmentFactory());
    private final AppointmentFactoryRegistry registry = new AppointmentFactoryRegistry(factories);
    private final AppointmentPet pet = new AppointmentPet();
    private final Doctor doctor = new Doctor();
    private final LocalDateTime time = LocalDateTime.of(2027, 1, 4, 9, 0);

    private AppointmentRequestDTO request(ServiceType type, String symptoms) {
        return new AppointmentRequestDTO(1L, 2L, 3L, time, type, symptoms);
    }

    @ParameterizedTest @EnumSource(ServiceType.class)
    void selectsTypeAndAssemblesPendingAppointment(ServiceType type) {
        Appointment a = registry.create(request(type, "  นัดตรวจ  "), pet, doctor);
        assertThat(a.getServiceType()).isEqualTo(type);
        assertThat(a.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        assertThat(a.getPet()).isSameAs(pet);
        assertThat(a.getDoctor()).isSameAs(doctor);
        assertThat(a.getAppointmentDateTime()).isEqualTo(time);
        assertThat(a.getSymptoms()).isEqualTo("นัดตรวจ");
        assertThat(a.getAppointmentId()).isNull();
        assertThat(a.getPreparationInstructions()).isNotBlank();
    }

    @Test void suppliesServiceSpecificPreparation() {
        assertThat(registry.create(request(ServiceType.VACCINE, "วัคซีน"), pet, doctor)
            .getPreparationInstructions()).contains("สมุดวัคซีน");
        assertThat(registry.create(request(ServiceType.SURGERY, "ผ่าตัด"), pet, doctor)
            .getPreparationInstructions()).contains("เฉพาะจากสัตวแพทย์");
    }

    @Test void createsIndependentObjects() {
        Appointment first = registry.create(request(ServiceType.VACCINE, "วัคซีน"), pet, doctor);
        Appointment second = registry.create(request(ServiceType.VACCINE, "วัคซีน"), pet, doctor);
        first.setStatus(AppointmentStatus.CANCELLED);
        assertThat(first).isNotSameAs(second);
        assertThat(second.getStatus()).isEqualTo(AppointmentStatus.PENDING);
    }

    @ParameterizedTest @NullAndEmptySource @ValueSource(strings = {" ", "\t"})
    void rejectsEmptySymptoms(String symptoms) {
        assertThatThrownBy(() -> registry.create(request(ServiceType.CONSULTATION, symptoms), pet, doctor))
            .isInstanceOf(InvalidAppointmentException.class);
    }

    @Test void rejectsOversizedSymptoms() {
        assertThatThrownBy(() -> registry.create(request(ServiceType.CONSULTATION, "a".repeat(1001)), pet, doctor))
            .isInstanceOf(InvalidAppointmentException.class);
    }

    @Test void rejectsMissingRelationsAndTime() {
        AppointmentFactory factory = factories.get(0);
        assertThatThrownBy(() -> factory.create(null, doctor, time, "ตรวจ")).isInstanceOf(InvalidAppointmentException.class);
        assertThatThrownBy(() -> factory.create(pet, null, time, "ตรวจ")).isInstanceOf(InvalidAppointmentException.class);
        assertThatThrownBy(() -> factory.create(pet, doctor, null, "ตรวจ")).isInstanceOf(InvalidAppointmentException.class);
    }

    @Test void rejectsMissingServiceType() {
        assertThatThrownBy(() -> registry.create(request(null, "ตรวจ"), pet, doctor))
            .isInstanceOf(InvalidAppointmentException.class);
        assertThatThrownBy(() -> registry.create(null, pet, doctor)).isInstanceOf(InvalidAppointmentException.class);
    }

    @Test void failsFastOnIncompleteOrDuplicateRegistration() {
        assertThatThrownBy(() -> new AppointmentFactoryRegistry(List.of(factories.get(0))))
            .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new AppointmentFactoryRegistry(List.of(factories.get(0), factories.get(0))))
            .isInstanceOf(IllegalStateException.class);
    }
}
