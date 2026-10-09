package com.example.petclinic.service;

import java.util.*;
import com.example.petclinic.domain.entity.*;
import com.example.petclinic.exception.*;
import com.example.petclinic.repository.*;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentGuestServiceTest {
    PetOwnerService owners = mock(PetOwnerService.class);
    AppointmentPetRepository pets = mock(AppointmentPetRepository.class);
    AppointmentGuestService service = new AppointmentGuestService(owners, pets);
    com.example.petclinic.dto.response.PetOwnerResponseDTO owner() {
        var o = new com.example.petclinic.dto.response.PetOwnerResponseDTO(); o.setOwnerId(1L); o.setFirstName("อ้น"); o.setLastName("ทดสอบ"); return o;
    }
    @Test void normalizesPhoneAndReturnsSelectedOwner() {
        when(owners.getPetOwnerByPhone("0812345678")).thenReturn(owner());
        assertThat(service.lookup("081-234-5678").ownerId()).isEqualTo(1L);
        verify(owners).getPetOwnerByPhone("0812345678");
    }
    @Test void missingPhoneReturnsNotFound() {
        when(owners.getPetOwnerByPhone("0812345678")).thenThrow(new ResourceNotFoundException("ไม่พบเบอร์"));
        assertThatThrownBy(() -> service.lookup("0812345678")).isInstanceOf(ResourceNotFoundException.class);
    }
    @Test void refusesAmbiguousPhoneInsteadOfSelectingAnotherOwner() {
        when(owners.getPetOwnerByPhone("0812345678")).thenThrow(new DuplicateResourceException("เบอร์ซ้ำ"));
        assertThatThrownBy(() -> service.lookup("0812345678")).isInstanceOf(DuplicateResourceException.class);
    }
    @Test void rejectsInvalidPhoneBeforeQuery() {
        for (String phone : Arrays.asList(null, "123", "+66812345678", "081234567a")) {
            assertThatThrownBy(() -> service.lookup(phone)).isInstanceOf(InvalidAppointmentException.class);
        }
        verifyNoInteractions(owners);
    }
    @Test void returnsOnlyPetsOfSpecifiedOwner() {
        Pet p = new Pet(); p.setPetId(2L); p.setName("มะลิ");
        when(owners.getPetOwnerById(1L)).thenReturn(owner());
        when(pets.findByPetOwnerOwnerIdOrderByNameAsc(1L)).thenReturn(List.of(p));
        assertThat(service.pets(1L)).hasSize(1);
        assertThat(service.pets(1L).get(0).petId()).isEqualTo(2L);
        verify(pets, times(2)).findByPetOwnerOwnerIdOrderByNameAsc(1L);
    }
    @Test void missingOwnerDoesNotListPets() {
        when(owners.getPetOwnerById(9L)).thenThrow(new ResourceNotFoundException("ไม่พบเจ้าของ"));
        assertThatThrownBy(() -> service.pets(9L)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.pets(0L)).isInstanceOf(InvalidAppointmentException.class);
        verifyNoInteractions(pets);
    }
}
