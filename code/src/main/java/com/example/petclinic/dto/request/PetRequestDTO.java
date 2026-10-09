
package com.example.petclinic.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PetRequestDTO {

    @NotBlank(message = "กรุณาระบุชื่อสัตว์เลี้ยง")
    private String name;

    @NotBlank(message = "กรุณาระบุประเภทสัตว์")
    private String species;

    private String breed;
    private String gender;
    private LocalDate birthDate;

    @Positive(message = "น้ำหนักต้องมากกว่า 0")
    private Double weight;

    private String microchipNumber;

    @NotNull(message = "กรุณาระบุรหัสเจ้าของสัตว์เลี้ยง")
    @Positive(message = "รหัสเจ้าของสัตว์เลี้ยงต้องมากกว่า 0")
    private Long ownerId;

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
}
