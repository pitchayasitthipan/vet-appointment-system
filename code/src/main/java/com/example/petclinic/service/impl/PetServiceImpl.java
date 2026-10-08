package com.example.petclinic.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.petclinic.domain.entity.Pet;
import com.example.petclinic.domain.entity.PetOwner;
import com.example.petclinic.dto.request.PetRequestDTO;
import com.example.petclinic.dto.response.PetResponseDTO;
import com.example.petclinic.repository.PetRepository;
import com.example.petclinic.repository.PetOwnerRepository;
import com.example.petclinic.service.PetService;

@Service
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;
    private final PetOwnerRepository petOwnerRepository;

    public PetServiceImpl(
            PetRepository petRepository,
            PetOwnerRepository petOwnerRepository) {
        this.petRepository = petRepository;
        this.petOwnerRepository = petOwnerRepository;
    }

    @Override
    public PetResponseDTO createPet(PetRequestDTO request) {
        return null;
    }

    @Override
    public PetResponseDTO getPetById(Long petId) {
        return null;
    }

    @Override
    public List<PetResponseDTO> getPetsByOwnerId(Long ownerId) {
        return null;
    }

    @Override
    public PetResponseDTO updatePet(Long petId, PetRequestDTO request) {
        return null;
    }

    @Override
    public void deletePet(Long petId) {
    }
}