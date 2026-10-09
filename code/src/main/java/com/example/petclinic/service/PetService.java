
package com.example.petclinic.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.petclinic.dto.request.PetRequestDTO;
import com.example.petclinic.dto.response.PetResponseDTO;

public interface PetService {

    PetResponseDTO createPet(PetRequestDTO request);

    PetResponseDTO getPetById(Long petId);

    List<PetResponseDTO> getPetsByOwnerId(Long ownerId);

    Page<PetResponseDTO> getAllPets(Pageable pageable);

    PetResponseDTO updatePet(
            Long petId,
            PetRequestDTO request
    );

    void deletePet(Long petId);
}
