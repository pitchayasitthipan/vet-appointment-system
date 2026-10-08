package com.example.petclinic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.petclinic.domain.entity.Pet;
import com.example.petclinic.domain.entity.PetOwner;
import com.example.petclinic.dto.request.PetRequestDTO;
import com.example.petclinic.dto.response.PetResponseDTO;
import com.example.petclinic.repository.PetRepository;
import com.example.petclinic.repository.PetOwnerRepository;
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

    @Test
    void createPet_shouldCreatePetSuccessfully() {

        Pet savedPet = new Pet();
        savedPet.setPetId(1L);
        savedPet.setName(request.getName());
        savedPet.setSpecies(request.getSpecies());
        savedPet.setBreed(request.getBreed());
        savedPet.setGender(request.getGender());
        savedPet.setBirthDate(request.getBirthDate());
        savedPet.setWeight(request.getWeight());
        savedPet.setMicrochipNumber(request.getMicrochipNumber());
        savedPet.setPetOwner(petOwner);

        when(petOwnerRepository.findById(1L))
                .thenReturn(java.util.Optional.of(petOwner));

        when(petRepository.save(any(Pet.class)))
                .thenReturn(savedPet);

        PetResponseDTO result = petService.createPet(request);

        assertNotNull(result);
        assertEquals(1L, result.getPetId());
        assertEquals("Milo", result.getName());
        assertEquals("Dog", result.getSpecies());
        assertEquals(1L, result.getOwnerId());
    }

    @Test
    void getPetById_shouldReturnPetSuccessfully() {

        Pet pet = new Pet();
        pet.setPetId(1L);
        pet.setName("Milo");
        pet.setSpecies("Dog");
        pet.setBreed("Golden Retriever");
        pet.setGender("Male");
        pet.setBirthDate(LocalDate.of(2022, 5, 10));
        pet.setWeight(20.5);
        pet.setMicrochipNumber("MC123456");
        pet.setPetOwner(petOwner);

        when(petRepository.findById(1L))
                .thenReturn(java.util.Optional.of(pet));

        PetResponseDTO result = petService.getPetById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getPetId());
        assertEquals("Milo", result.getName());
        assertEquals("Dog", result.getSpecies());
        assertEquals("Golden Retriever", result.getBreed());
        assertEquals("Male", result.getGender());
        assertEquals(LocalDate.of(2022, 5, 10), result.getBirthDate());
        assertEquals(20.5, result.getWeight());
        assertEquals("MC123456", result.getMicrochipNumber());
        assertEquals(1L, result.getOwnerId());

        verify(petRepository).findById(1L);
    }

    @Test
    void updatePet_shouldUpdatePetSuccessfully() {

        Pet existingPet = new Pet();
        existingPet.setPetId(1L);
        existingPet.setName("Milo");
        existingPet.setSpecies("Dog");
        existingPet.setBreed("Golden Retriever");
        existingPet.setGender("Male");
        existingPet.setBirthDate(LocalDate.of(2022, 5, 10));
        existingPet.setWeight(20.5);
        existingPet.setMicrochipNumber("MC123456");
        existingPet.setPetOwner(petOwner);

        PetRequestDTO updateRequest = new PetRequestDTO();
        updateRequest.setName("Milo Updated");
        updateRequest.setSpecies("Dog");
        updateRequest.setBreed("Labrador");
        updateRequest.setGender("Male");
        updateRequest.setBirthDate(LocalDate.of(2022, 5, 10));
        updateRequest.setWeight(22.0);
        updateRequest.setMicrochipNumber("MC999999");
        updateRequest.setOwnerId(1L);

        when(petRepository.findById(1L))
                .thenReturn(java.util.Optional.of(existingPet));

        when(petOwnerRepository.findById(1L))
                .thenReturn(java.util.Optional.of(petOwner));

        when(petRepository.save(any(Pet.class)))
                .thenReturn(existingPet);

        PetResponseDTO result = petService.updatePet(1L, updateRequest);

        assertNotNull(result);
        assertEquals(1L, result.getPetId());
        assertEquals("Milo Updated", result.getName());
        assertEquals("Dog", result.getSpecies());
        assertEquals("Labrador", result.getBreed());
        assertEquals("Male", result.getGender());
        assertEquals(22.0, result.getWeight());
        assertEquals("MC999999", result.getMicrochipNumber());
        assertEquals(1L, result.getOwnerId());

        verify(petRepository).findById(1L);
        verify(petOwnerRepository).findById(1L);
        verify(petRepository).save(any(Pet.class));
    }

    @Test
    void getPetsByOwnerId_shouldReturnPetsSuccessfully() {

        Pet pet1 = new Pet();
        pet1.setPetId(1L);
        pet1.setName("Milo");
        pet1.setSpecies("Dog");
        pet1.setBreed("Golden Retriever");
        pet1.setGender("Male");
        pet1.setWeight(20.5);
        pet1.setPetOwner(petOwner);

        Pet pet2 = new Pet();
        pet2.setPetId(2L);
        pet2.setName("Luna");
        pet2.setSpecies("Cat");
        pet2.setBreed("Persian");
        pet2.setGender("Female");
        pet2.setWeight(4.5);
        pet2.setPetOwner(petOwner);

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

        Pet pet = new Pet();
        pet.setPetId(1L);
        pet.setName("Milo");
        pet.setSpecies("Dog");
        pet.setBreed("Golden Retriever");
        pet.setGender("Male");
        pet.setWeight(20.5);
        pet.setPetOwner(petOwner);

        when(petRepository.findById(1L))
                .thenReturn(java.util.Optional.of(pet));

        petService.deletePet(1L);

        verify(petRepository).findById(1L);
        verify(petRepository).delete(pet);
    }
}