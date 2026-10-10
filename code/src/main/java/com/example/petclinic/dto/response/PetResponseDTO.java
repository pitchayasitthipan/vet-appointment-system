
package com.example.petclinic.dto.response;

import java.time.LocalDate;

import com.example.petclinic.domain.entity.Pet;

public class PetResponseDTO {

    private Long petId;
    private String name;
    private String species;
    private String breed;
    private String gender;
    private LocalDate birthDate;
    private Double weight;
    private String microchipNumber;
    private Long ownerId;

    // ชื่อเจ้าของ ให้หน้าเจ้าหน้าที่แสดงว่าเป็นสัตว์ของใคร
    private String ownerName;

    public PetResponseDTO() {
    }

    public static PetResponseDTO fromEntity(Pet pet) {
        PetResponseDTO dto = new PetResponseDTO();

        dto.setPetId(pet.getPetId());
        dto.setName(pet.getName());
        dto.setSpecies(pet.getSpecies());
        dto.setBreed(pet.getBreed());
        dto.setGender(pet.getGender());
        dto.setBirthDate(pet.getBirthDate());
        dto.setWeight(pet.getWeight());
        dto.setMicrochipNumber(pet.getMicrochipNumber());

        if (pet.getPetOwner() != null) {
            dto.setOwnerId(pet.getPetOwner().getOwnerId());
            dto.setOwnerName(pet.getPetOwner().getFirstName() + " " + pet.getPetOwner().getLastName());
        }

        return dto;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getMicrochipNumber() {
        return microchipNumber;
    }

    public void setMicrochipNumber(String microchipNumber) {
        this.microchipNumber = microchipNumber;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}