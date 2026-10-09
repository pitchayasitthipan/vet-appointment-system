package com.example.petclinic.service;

import java.util.*;
import com.example.petclinic.domain.entity.*;
import com.example.petclinic.exception.*;
import com.example.petclinic.repository.*;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentGuestServiceTest {
    AppointmentGuestOwnerRepository owners = mock(AppointmentGuestOwnerRepository.class);
    AppointmentPetRepository pets = mock(AppointmentPetRepository.class);
    AppointmentGuestService service = new AppointmentGuestService(owners, pets);
    PetOwner owner() {
        PetOwner o = new PetOwner(); o.setOwnerId(1L); o.setFirstName("อ้น"); o.setLastName("ทดสอบ"); return o;
    }
    @Test void normalizesPhoneAndReturnsSelectedOwner() {
        when(owners.findByNormalizedPhone("0812345678")).thenReturn(List.of(owner()));
        assertThat(service.lookup("081-234-5678").ownerId()).isEqualTo(1L);
        verify(owners).findByNormalizedPhone("0812345678");
    }
    @Test void missingPhoneReturnsNotFound() {
        when(owners.findByNormalizedPhone("0812345678")).thenReturn(List.of());
        assertThatThrownBy(() -> service.lookup("0812345678")).isInstanceOf(ResourceNotFoundException.class);
    }
    @Test void refusesAmbiguousPhoneInsteadOfSelectingAnotherOwner() {
        when(owners.findByNormalizedPhone("0812345678")).thenReturn(List.of(owner(), owner()));
        assertThatThrownBy(() -> service.lookup("0812345678")).isInstanceOf(DuplicateResourceException.class);
    }
    @Test void rejectsInvalidPhoneBeforeQuery() {
        for (String phone : Arrays.asList(null, "123", "+66812345678", "081234567a")) {
            assertThatThrownBy(() -> service.lookup(phone)).isInstanceOf(InvalidAppointmentException.class);
        }
        verifyNoInteractions(owners);
    }
    @Test void returnsOnlyPetsOfSpecifiedOwner() {
        AppointmentPet p = new AppointmentPet(); p.setPetId(2L); p.setPetName("มะลิ");
        when(owners.existsById(1L)).thenReturn(true);
        when(pets.findByPetOwnerOwnerIdOrderByPetNameAsc(1L)).thenReturn(List.of(p));
        assertThat(service.pets(1L)).hasSize(1);
        assertThat(service.pets(1L).get(0).petId()).isEqualTo(2L);
        verify(pets, times(2)).findByPetOwnerOwnerIdOrderByPetNameAsc(1L);
    }
    @Test void missingOwnerDoesNotListPets() {
        assertThatThrownBy(() -> service.pets(9L)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.pets(0L)).isInstanceOf(InvalidAppointmentException.class);
        verifyNoInteractions(pets);
    }
}
