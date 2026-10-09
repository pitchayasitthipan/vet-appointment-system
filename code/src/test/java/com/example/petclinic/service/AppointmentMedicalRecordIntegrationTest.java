package com.example.petclinic.service;

import java.time.LocalDateTime;
import com.example.petclinic.domain.entity.*;
import com.example.petclinic.domain.enums.*;
import com.example.petclinic.dto.request.MedicalRecordRequestDTO;
import com.example.petclinic.dto.response.MedicalRecordResponseDTO;
import com.example.petclinic.exception.DuplicateResourceException;
import com.example.petclinic.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;

/** Exercises the caller contract against the real MedicalRecord service, without changing it. */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:appointmentmedical;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.show-sql=false"
})
@Transactional
@AutoConfigureMockMvc
class AppointmentMedicalRecordIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired AppointmentService appointments;
    @Autowired MedicalRecordService records;
    @Autowired AppointmentRepository appointmentRepository;
    @Autowired PetOwnerRepository owners;
    @Autowired PetRepository pets;
    @Autowired AppointmentDoctorRepository doctors;
    @Autowired MedicalRecordRepository recordRepository;

    @Test void sharedStaffUnlockWorksForBothModulesAndLogoutRevokesAppointmentAccess() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/api/v1/appointments/staff").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/owners/staff/unlock").session(session).param("passcode", "87654321"))
            .andExpect(status().is3xxRedirection());
        assertThat(session.getAttribute("isStaff")).isEqualTo(true);
        mvc.perform(get("/api/v1/appointments/staff").session(session)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/medical-records").session(session)).andExpect(status().isOk());
        mvc.perform(post("/owners/staff/logout").session(session)).andExpect(status().is3xxRedirection());
        mvc.perform(get("/api/v1/appointments/staff").session(session)).andExpect(status().isForbidden());
    }

    private Appointment appointment(AppointmentStatus status) {
        PetOwner owner = new PetOwner(); owner.setFirstName("อ้น"); owner.setLastName("ทดสอบ");
        owner.setEmail("medical-" + status + "@example.test"); owner.setPhone("0812345678");
        owners.saveAndFlush(owner);
        Pet pet = new Pet(); pet.setName("มะลิ"); pet.setSpecies("Cat"); pet.setPetOwner(owner);
        pets.saveAndFlush(pet);
        Doctor doctor = new Doctor("หมอ", "ทดสอบ", "ทั่วไป", "0823456789",
            "medical-" + status + "@doctor.test", "ทุกวัน: 09:00 - 17:00");
        doctors.saveAndFlush(doctor);
        Appointment appointment = new Appointment(); appointment.setPet(pet); appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(LocalDateTime.now().minusDays(1));
        appointment.setServiceType(ServiceType.CONSULTATION); appointment.setSymptoms("ตรวจสุขภาพ");
        appointment.setStatus(status);
        return appointmentRepository.saveAndFlush(appointment);
    }

    // The MedicalRecord owner will place this sequence in its transactional create/update methods.
    private MedicalRecordResponseDTO create(MedicalRecordRequestDTO request) {
        appointments.requireCompletedForMedicalRecord(request.getAppointmentId());
        return records.createMedicalRecord(request);
    }

    @Test void completedAppointmentSupportsMedicalRecordCreateAndUpdate() {
        Appointment appointment = appointment(AppointmentStatus.COMPLETED);
        MedicalRecordRequestDTO request = new MedicalRecordRequestDTO();
        request.setAppointmentId(appointment.getAppointmentId()); request.setDiagnosis("ตรวจสุขภาพ");
        MedicalRecordResponseDTO created = create(request);
        assertThat(created.getAppointmentId()).isEqualTo(appointment.getAppointmentId());
        request.setTreatment("ดูแลตามคำแนะนำ");
        appointments.requireCompletedForMedicalRecord(request.getAppointmentId());
        var updated = records.updateMedicalRecord(created.getMedicalRecordId(), request);
        assertThat(updated.getTreatment()).isEqualTo("ดูแลตามคำแนะนำ");
        assertThat(records.getMedicalRecordsByAppointmentId(appointment.getAppointmentId())).hasSize(1);
        assertThat(appointmentRepository.findById(appointment.getAppointmentId()).orElseThrow().getStatus())
            .isEqualTo(AppointmentStatus.COMPLETED);
    }

    @Test void unfinishedAppointmentDoesNotCreateMedicalRecordWhenCallerUsesGuard() {
        Appointment appointment = appointment(AppointmentStatus.CONFIRMED);
        MedicalRecordRequestDTO request = new MedicalRecordRequestDTO(); request.setAppointmentId(appointment.getAppointmentId());
        assertThatThrownBy(() -> create(request)).isInstanceOf(DuplicateResourceException.class);
        assertThat(recordRepository.count()).isZero();
    }
}
