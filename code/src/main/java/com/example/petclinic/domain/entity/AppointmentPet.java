package com.example.petclinic.domain.entity;

import jakarta.persistence.*;

/** Appointment-owned projection of the shared pet table; no Pet CRUD or team entity changes. */
@Entity
@org.hibernate.annotations.Immutable
@Table(name = "pet", indexes = @Index(name = "idx_pet_owner", columnList = "owner_id"))
public class AppointmentPet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_id")
    private Long petId;
    @Column(name = "name", nullable = false, length = 100)
    private String petName;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private PetOwner petOwner;

    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    public PetOwner getPetOwner() { return petOwner; }
    public void setPetOwner(PetOwner petOwner) { this.petOwner = petOwner; }
}
