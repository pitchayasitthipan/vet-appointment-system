package com.example.petclinic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.petclinic.domain.entity.Pet;

public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findByPetOwnerOwnerId(Long ownerId);
}