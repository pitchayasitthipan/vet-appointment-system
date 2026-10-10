
package com.example.petclinic.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petclinic.domain.entity.Pet;
import com.example.petclinic.domain.entity.PetOwner;
import com.example.petclinic.dto.request.PetRequestDTO;
import com.example.petclinic.dto.response.PetResponseDTO;
import com.example.petclinic.exception.ResourceNotFoundException;
import com.example.petclinic.repository.PetOwnerRepository;
import com.example.petclinic.repository.PetRepository;
import com.example.petclinic.repository.PetSpecifications;
import com.example.petclinic.service.PetService;

@Service
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;
    private final PetOwnerRepository petOwnerRepository;

    public PetServiceImpl(
            PetRepository petRepository,
            PetOwnerRepository petOwnerRepository
    ) {
        this.petRepository = petRepository;
        this.petOwnerRepository = petOwnerRepository;
    }

    @Override
    @Transactional
    public PetResponseDTO createPet(PetRequestDTO request) {

        PetOwner owner = petOwnerRepository
                .findById(request.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ไม่พบเจ้าของสัตว์เลี้ยงรหัส "
                                + request.getOwnerId()
                ));

        Pet pet = new Pet();

        applyFields(pet, request);
        pet.setPetOwner(owner);

        Pet savedPet = petRepository.save(pet);

        return PetResponseDTO.fromEntity(savedPet);
    }

    @Override
    @Transactional(readOnly = true)
    public PetResponseDTO getPetById(Long petId) {

        Pet pet = findPet(petId);

        return PetResponseDTO.fromEntity(pet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetResponseDTO> getPetsByOwnerId(Long ownerId) {

        return petRepository
                .findByPetOwnerOwnerId(ownerId)
                .stream()
                .map(PetResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PetResponseDTO> getAllPets(Pageable pageable) {

        return petRepository
                .findAll(pageable)
                .map(PetResponseDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PetResponseDTO> searchPets(String keyword, String species, Pageable pageable) {

        return petRepository
                .findAll(PetSpecifications.search(keyword, species), pageable)
                .map(PetResponseDTO::fromEntity);
    }

    @Override
    @Transactional
    public PetResponseDTO updatePet(
            Long petId,
            PetRequestDTO request
    ) {
        Pet pet = findPet(petId);

        // รักษาเจ้าของเดิม ไม่เปลี่ยนเจ้าของจาก ownerId ที่ส่งมา
        applyFields(pet, request);

        Pet updatedPet = petRepository.save(pet);

        return PetResponseDTO.fromEntity(updatedPet);
    }

    @Override
    @Transactional
    public void deletePet(Long petId) {

        Pet pet = findPet(petId);

        petRepository.delete(pet);
    }

    private Pet findPet(Long petId) {

        return petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ไม่พบสัตว์เลี้ยงรหัส " + petId
                ));
    }

    private void applyFields(
            Pet pet,
            PetRequestDTO request
    ) {
        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setBreed(request.getBreed());
        pet.setGender(request.getGender());
        pet.setBirthDate(request.getBirthDate());
        pet.setWeight(request.getWeight());
        pet.setMicrochipNumber(request.getMicrochipNumber());
    }
}