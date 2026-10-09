package com.example.petclinic.service;

import java.util.List;

import com.example.petclinic.dto.request.PetRequestDTO;
import com.example.petclinic.dto.response.PetResponseDTO;

public interface PetService {

    PetResponseDTO createPet(PetRequestDTO request);

    PetResponseDTO getPetById(Long petId);

    List<PetResponseDTO> getPetsByOwnerId(Long ownerId);

    PetResponseDTO updatePet(Long petId, PetRequestDTO request);

    void deletePet(Long petId);
}