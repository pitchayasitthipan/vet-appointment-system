package com.example.petclinic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

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
}