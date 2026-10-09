package com.example.petclinic.service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import com.example.petclinic.domain.entity.*;
import com.example.petclinic.domain.enums.*;
import com.example.petclinic.dto.request.*;
import com.example.petclinic.exception.DuplicateResourceException;
import com.example.petclinic.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.persistence.EntityManager;
import static org.assertj.core.api.Assertions.*;

/** Executes the actual JPA mappings, queries, version updates and Spring wiring using H2. */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:appointment;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.show-sql=false"
})
@Transactional
class AppointmentPersistenceTest {
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void medicalRecordGuardRequiresTheCallersWriteTransaction() {
        assertThatThrownBy(() -> service.requireCompletedForMedicalRecord(1L))
            .isInstanceOf(org.springframework.transaction.IllegalTransactionStateException.class);
    }
    @Autowired AppointmentService service;
    @Autowired PetOwnerRepository owners;
    @Autowired AppointmentPetRepository pets;
    @Autowired AppointmentDoctorRepository doctors;
    @Autowired AppointmentRepository appointments;
    @Autowired AppointmentGuestService guests;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager entityManager;

    private Pet insertPet(Long id, String name, PetOwner owner) {
        // Fixtures follow the shared Pet schema, including its mandatory species.
        jdbc.update("INSERT INTO pet (pet_id, name, species, owner_id) VALUES (?, ?, ?, ?)",
            id, name, "Cat", owner.getOwnerId());
        return pets.findById(id).orElseThrow();
    }

    @Test void guestLookupNormalizesInputForSharedOwnerServiceAndListsSelectedPets() {
        PetOwner owner = new PetOwner(); owner.setFirstName("อ้น"); owner.setLastName("ทดสอบ");
        owner.setEmail("guest-test@example.com"); owner.setPhone("0812345678"); owners.saveAndFlush(owner);
        PetOwner other = new PetOwner(); other.setFirstName("อื่น"); other.setLastName("ทดสอบ");
        other.setEmail("other-guest@example.com"); other.setPhone("0899999999"); owners.saveAndFlush(other);
        Pet pet = insertPet(1L, "มะลิ", owner);
        insertPet(2L, "ของคนอื่น", other);
        assertThat(guests.lookup("081 234 5678").ownerId()).isEqualTo(owner.getOwnerId());
        assertThat(guests.pets(owner.getOwnerId())).hasSize(1);
        assertThat(guests.pets(owner.getOwnerId()).get(0).petId()).isEqualTo(pet.getPetId());
        jdbc.update("UPDATE pet SET name = ? WHERE pet_id = ?", "มะลิชื่อใหม่", pet.getPetId());
        entityManager.clear();
        assertThat(guests.pets(owner.getOwnerId()).get(0).petName()).isEqualTo("มะลิชื่อใหม่");
    }

    @Test void persistsReschedulesAndCancellationReleasesSlot() {
        PetOwner owner = new PetOwner(); owner.setFirstName("อ้น"); owner.setLastName("ทดสอบ");
        owner.setEmail("appointment-test@example.com"); owner.setPhone("0812345678"); owners.saveAndFlush(owner);
        Pet pet = insertPet(1L, "มะลิ", owner);
        Doctor doctor = new Doctor("หมอ", "ทดสอบ", "ทั่วไป", "0823456789", "appointment-doctor@example.com", "ทุกวัน: 09:00 - 17:00");
        doctors.saveAndFlush(doctor);
        LocalDateTime time = LocalDate.now(ZoneId.of("Asia/Bangkok")).with(TemporalAdjusters.next(java.time.DayOfWeek.MONDAY)).atTime(9, 0);
        AppointmentRequestDTO request = new AppointmentRequestDTO(owner.getOwnerId(), pet.getPetId(), doctor.getDoctorId(), time, ServiceType.VACCINE, "ฉีดวัคซีน");
        var created = service.create(request);
        assertThatThrownBy(() -> service.requireCompletedForMedicalRecord(created.appointmentId()))
            .isInstanceOf(DuplicateResourceException.class);
        assertThat(created.version()).isEqualTo(0L);
        assertThat(service.list(owner.getOwnerId(), AppointmentStatus.PENDING, 0, 10, "appointmentDateTime", "asc").totalElements()).isEqualTo(1);
        assertThat(service.availability(doctor.getDoctorId(), time.toLocalDate())).doesNotContain(time);
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(DuplicateResourceException.class);
        var updated = service.update(created.appointmentId(), owner.getOwnerId(), new AppointmentUpdateDTO(doctor.getDoctorId(), time.plusMinutes(30), ServiceType.CONSULTATION, "ตรวจ", created.version()));
        assertThat(updated.version()).isEqualTo(1L);
        assertThat(service.availability(doctor.getDoctorId(), time.toLocalDate())).contains(time).doesNotContain(time.plusMinutes(30));
        service.cancel(created.appointmentId(), owner.getOwnerId());
        assertThat(service.get(created.appointmentId(), owner.getOwnerId()).status()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(service.availability(doctor.getDoctorId(), time.toLocalDate())).contains(time.plusMinutes(30));
        assertThat(service.create(request).appointmentId()).isNotEqualTo(created.appointmentId());
        assertThat(appointments.count()).isEqualTo(2);
        Appointment finished = appointments.findById(created.appointmentId()).orElseThrow();
        finished.setStatus(AppointmentStatus.COMPLETED); appointments.saveAndFlush(finished);
        assertThat(service.requireCompletedForMedicalRecord(finished.getAppointmentId()).petId())
            .isEqualTo(pet.getPetId());
    }
}
