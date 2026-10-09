
package com.example.petclinic.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.petclinic.domain.entity.Pet;
import com.example.petclinic.domain.entity.PetOwner;
import com.example.petclinic.dto.request.PetRequestDTO;
import com.example.petclinic.dto.response.PetResponseDTO;
import com.example.petclinic.repository.PetOwnerRepository;
import com.example.petclinic.repository.PetRepository;
import com.example.petclinic.service.impl.PetServiceImpl;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private PetOwnerRepository petOwnerRepository;

    @InjectMocks
    private PetServiceImpl petService;

    private PetOwner petOwner;
    private PetRequestDTO request;

    @BeforeEach
    void setUp() {
        petOwner = new PetOwner();
        petOwner.setOwnerId(1L);
        petOwner.setFirstName("John");
        petOwner.setLastName("Doe");

        request = new PetRequestDTO();
        request.setName("Milo");
        request.setSpecies("Dog");
        request.setBreed("Golden Retriever");
        request.setGender("Male");
        request.setBirthDate(LocalDate.of(2022, 5, 10));
        request.setWeight(20.5);
        request.setMicrochipNumber("MC123456");
        request.setOwnerId(1L);
    }

    private Pet createPet(Long id, String name, String species) {
        Pet pet = new Pet();
        pet.setPetId(id);
        pet.setName(name);
        pet.setSpecies(species);
        pet.setBreed("Golden Retriever");
        pet.setGender("Male");
        pet.setBirthDate(LocalDate.of(2022, 5, 10));
        pet.setWeight(20.5);
        pet.setMicrochipNumber("MC" + id);
        pet.setPetOwner(petOwner);
        return pet;
    }

    @Test
    void createPet_shouldCreatePetSuccessfully() {
        Pet savedPet = createPet(1L, "Milo", "Dog");

        when(petOwnerRepository.findById(1L))
                .thenReturn(Optional.of(petOwner));
        when(petRepository.save(any(Pet.class)))
                .thenReturn(savedPet);

        PetResponseDTO result = petService.createPet(request);

        assertNotNull(result);
        assertEquals(1L, result.getPetId());
        assertEquals("Milo", result.getName());
        assertEquals("Dog", result.getSpecies());
        assertEquals(1L, result.getOwnerId());

        verify(petOwnerRepository).findById(1L);
        verify(petRepository).save(any(Pet.class));
    }

    @Test
    void getPetById_shouldReturnPetSuccessfully() {
        Pet pet = createPet(1L, "Milo", "Dog");

        when(petRepository.findById(1L))
                .thenReturn(Optional.of(pet));

        PetResponseDTO result = petService.getPetById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getPetId());
        assertEquals("Milo", result.getName());
        assertEquals("Dog", result.getSpecies());
        assertEquals("Golden Retriever", result.getBreed());
        assertEquals("Male", result.getGender());
        assertEquals(LocalDate.of(2022, 5, 10), result.getBirthDate());
        assertEquals(20.5, result.getWeight());
        assertEquals("MC1", result.getMicrochipNumber());
        assertEquals(1L, result.getOwnerId());
        assertEquals("John Doe", result.getOwnerName());

        verify(petRepository).findById(1L);
    }

    @Test
    void updatePet_shouldUpdatePetAndPreserveOriginalOwner() {
        Pet existingPet = createPet(1L, "Milo", "Dog");

        PetRequestDTO updateRequest = new PetRequestDTO();
        updateRequest.setName("Milo Updated");
        updateRequest.setSpecies("Dog");
        updateRequest.setBreed("Labrador");
        updateRequest.setGender("Male");
        updateRequest.setBirthDate(LocalDate.of(2022, 5, 10));
        updateRequest.setWeight(22.0);
        updateRequest.setMicrochipNumber("MC999999");

        // จำลองผู้ใช้ส่ง ownerId ของคนอื่นเข้ามา
        updateRequest.setOwnerId(999L);

        when(petRepository.findById(1L))
                .thenReturn(Optional.of(existingPet));
        when(petRepository.save(any(Pet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PetResponseDTO result =
                petService.updatePet(1L, updateRequest);

        assertNotNull(result);
        assertEquals(1L, result.getPetId());
        assertEquals("Milo Updated", result.getName());
        assertEquals("Dog", result.getSpecies());
        assertEquals("Labrador", result.getBreed());
        assertEquals("Male", result.getGender());
        assertEquals(22.0, result.getWeight());
        assertEquals("MC999999", result.getMicrochipNumber());

        // ต้องยังเป็นเจ้าของเดิม แม้ request ส่ง ownerId = 999
        assertEquals(1L, result.getOwnerId());
        assertEquals(1L, existingPet.getPetOwner().getOwnerId());

        verify(petRepository).findById(1L);
        verify(petRepository).save(any(Pet.class));

        // การแก้ไขไม่ควรค้นหาเจ้าของใหม่หรือเปลี่ยนเจ้าของ
        verify(petOwnerRepository, never()).findById(any());
    }

    @Test
    void getPetsByOwnerId_shouldReturnPetsSuccessfully() {
        Pet pet1 = createPet(1L, "Milo", "Dog");
        Pet pet2 = createPet(2L, "Luna", "Cat");

        when(petRepository.findByPetOwnerOwnerId(1L))
                .thenReturn(List.of(pet1, pet2));

        List<PetResponseDTO> result =
                petService.getPetsByOwnerId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getPetId());
        assertEquals("Milo", result.get(0).getName());
        assertEquals("Dog", result.get(0).getSpecies());
        assertEquals(1L, result.get(0).getOwnerId());

        assertEquals(2L, result.get(1).getPetId());
        assertEquals("Luna", result.get(1).getName());
        assertEquals("Cat", result.get(1).getSpecies());
        assertEquals(1L, result.get(1).getOwnerId());

        verify(petRepository).findByPetOwnerOwnerId(1L);
    }

    @Test
    void deletePet_shouldDeletePetSuccessfully() {
        Pet pet = createPet(1L, "Milo", "Dog");

        when(petRepository.findById(1L))
                .thenReturn(Optional.of(pet));

        petService.deletePet(1L);

        verify(petRepository).findById(1L);
        verify(petRepository).delete(pet);
    }
}