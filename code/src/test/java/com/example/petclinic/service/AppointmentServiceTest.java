package com.example.petclinic.service;

import java.time.*;
import java.util.*;
import com.example.petclinic.domain.entity.*;
import com.example.petclinic.domain.enums.*;
import com.example.petclinic.dto.request.AppointmentRequestDTO;
import com.example.petclinic.exception.*;
import com.example.petclinic.factory.*;
import com.example.petclinic.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
    @Mock AppointmentRepository appointments;
    @Mock PetRepository pets;
    @Mock AppointmentDoctorRepository doctors;
    AppointmentService service;
    Pet pet;
    Doctor doctor;
    final LocalDateTime time = LocalDateTime.of(2027, 1, 4, 9, 0);
    AppointmentRequestDTO request() { return new AppointmentRequestDTO(1L, 2L, 3L, time, ServiceType.VACCINE, "วัคซีน"); }

    @BeforeEach void setup() {
        PetOwner owner = new PetOwner(); owner.setOwnerId(1L);
        pet = new Pet(); pet.setPetId(2L); pet.setPetName("มะลิ"); pet.setPetOwner(owner);
        doctor = new Doctor(); doctor.setDoctorId(3L); doctor.setFirstName("หมอ"); doctor.setLastName("ใจดี");
        doctor.setWorkSchedule("จันทร์ - ศุกร์: 09:00 - 17:00");
        service = new AppointmentService(appointments, pets, doctors, new AppointmentFactoryRegistry(List.of(
            new ConsultationAppointmentFactory(), new VaccineAppointmentFactory(), new SurgeryAppointmentFactory())),
            new AppointmentSchedulePolicy(new ClinicConfigService(), Clock.fixed(Instant.parse("2027-01-04T01:00:00Z"), ZoneId.of("Asia/Bangkok"))));
    }
    void bookingResources() {
        when(doctors.findLockedById(3L)).thenReturn(Optional.of(doctor));
        when(pets.findLockedById(2L)).thenReturn(Optional.of(pet));
    }
    Appointment appointment() {
        Appointment a = new Appointment(); a.setAppointmentId(4L); a.setPet(pet); a.setDoctor(doctor);
        a.setAppointmentDateTime(time); a.setServiceType(ServiceType.VACCINE); return a;
    }

    @Test void createsPendingBookingWithOwnerAndFactoryInstructions() {
        bookingResources();
        when(appointments.saveAndFlush(any())).thenAnswer(call -> { Appointment a = call.getArgument(0); a.setAppointmentId(4L); return a; });
        var response = service.create(request());
        assertThat(response.appointmentId()).isEqualTo(4L);
        assertThat(response.ownerId()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(AppointmentStatus.PENDING);
        assertThat(response.preparationInstructions()).contains("สมุดวัคซีน");
        InOrder order = inOrder(doctors, pets, appointments);
        order.verify(doctors).findLockedById(3L); order.verify(pets).findLockedById(2L);
        order.verify(appointments).countConflicts(eq(3L), eq(2L), eq(time), isNull(), anyCollection());
        order.verify(appointments).saveAndFlush(any());
    }
    @Test void rejectsPetOfAnotherOwnerBeforeSave() {
        bookingResources(); pet.getPetOwner().setOwnerId(9L);
        assertThatThrownBy(() -> service.create(request())).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(appointments);
    }
    @Test void rejectsOccupiedSlot() {
        bookingResources(); when(appointments.countConflicts(eq(3L), eq(2L), eq(time), isNull(), anyCollection())).thenReturn(1L);
        assertThatThrownBy(() -> service.create(request())).isInstanceOf(DuplicateResourceException.class);
        verify(appointments, never()).saveAndFlush(any());
    }
    @Test void rejectsDoctorOffDuty() {
        bookingResources(); doctor.setWorkSchedule("อังคาร: 09:00 - 17:00");
        assertThatThrownBy(() -> service.create(request())).isInstanceOf(InvalidAppointmentException.class);
        verifyNoInteractions(appointments);
    }
    @Test void hidesAppointmentOutsideOwner() {
        when(appointments.findByAppointmentIdAndPetPetOwnerOwnerId(4L, 9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(4L, 9L)).isInstanceOf(ResourceNotFoundException.class);
    }
    @Test void listsOwnerWithStablePagination() {
        when(appointments.findByPetPetOwnerOwnerId(eq(1L), any())).thenReturn(new PageImpl<>(List.of(appointment())));
        assertThat(service.list(1L, null, 0, 10, "appointmentDateTime", "asc").content()).hasSize(1);
        ArgumentCaptor<Pageable> page = ArgumentCaptor.forClass(Pageable.class);
        verify(appointments).findByPetPetOwnerOwnerId(eq(1L), page.capture());
        assertThat(page.getValue().getSort().getOrderFor("appointmentId")).isNotNull();
    }
    @Test void rejectsInvalidPagingAndSort() {
        assertThatThrownBy(() -> service.list(1L, null, -1, 10, "appointmentDateTime", "asc")).isInstanceOf(InvalidAppointmentException.class);
        assertThatThrownBy(() -> service.list(1L, null, 0, 101, "appointmentDateTime", "asc")).isInstanceOf(InvalidAppointmentException.class);
        assertThatThrownBy(() -> service.list(1L, null, 0, 10, "pet.petOwner.phone", "asc")).isInstanceOf(InvalidAppointmentException.class);
        verifyNoInteractions(appointments);
    }
    @Test void availabilityRemovesBookedSlot() {
        when(doctors.findById(3L)).thenReturn(Optional.of(doctor));
        when(appointments.findByDoctorDoctorIdAndAppointmentDateTimeGreaterThanEqualAndAppointmentDateTimeLessThanAndStatusIn(
            eq(3L), any(), any(), anyCollection())).thenReturn(List.of(appointment()));
        assertThat(service.availability(3L, time.toLocalDate())).doesNotContain(time).contains(time.plusMinutes(30));
    }
}
