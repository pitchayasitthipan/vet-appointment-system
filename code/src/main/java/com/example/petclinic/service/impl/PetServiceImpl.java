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

        PetOwner petOwner = petOwnerRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Pet owner not found"));

        Pet pet = new Pet();

        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setBreed(request.getBreed());
        pet.setGender(request.getGender());
        pet.setBirthDate(request.getBirthDate());
        pet.setWeight(request.getWeight());
        pet.setMicrochipNumber(request.getMicrochipNumber());
        pet.setPetOwner(petOwner);

        Pet savedPet = petRepository.save(pet);

        PetResponseDTO response = new PetResponseDTO();

        response.setPetId(savedPet.getPetId());
        response.setName(savedPet.getName());
        response.setSpecies(savedPet.getSpecies());
        response.setBreed(savedPet.getBreed());
        response.setGender(savedPet.getGender());
        response.setBirthDate(savedPet.getBirthDate());
        response.setWeight(savedPet.getWeight());
        response.setMicrochipNumber(savedPet.getMicrochipNumber());
        response.setOwnerId(savedPet.getPetOwner().getOwnerId());

        return response;
    }

    @Override
    public PetResponseDTO getPetById(Long petId) {

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found"));

        PetResponseDTO response = new PetResponseDTO();

        response.setPetId(pet.getPetId());
        response.setName(pet.getName());
        response.setSpecies(pet.getSpecies());
        response.setBreed(pet.getBreed());
        response.setGender(pet.getGender());
        response.setBirthDate(pet.getBirthDate());
        response.setWeight(pet.getWeight());
        response.setMicrochipNumber(pet.getMicrochipNumber());

        if (pet.getPetOwner() != null) {
            response.setOwnerId(pet.getPetOwner().getOwnerId());
        }

        return response;
    }

    @Override
    public List<PetResponseDTO> getPetsByOwnerId(Long ownerId) {

        List<Pet> pets = petRepository.findByPetOwnerOwnerId(ownerId);

        return pets.stream()
            .map(pet -> {
                PetResponseDTO response = new PetResponseDTO();

                response.setPetId(pet.getPetId());
                response.setName(pet.getName());
                response.setSpecies(pet.getSpecies());
                response.setBreed(pet.getBreed());
                response.setGender(pet.getGender());
                response.setBirthDate(pet.getBirthDate());
                response.setWeight(pet.getWeight());
                response.setMicrochipNumber(pet.getMicrochipNumber());
                response.setOwnerId(ownerId);

                return response;
            })
            .toList();
    }

    @Override
    public PetResponseDTO updatePet(Long petId, PetRequestDTO request) {
        return null;
    }

    @Override
    public void deletePet(Long petId) {
    }
}