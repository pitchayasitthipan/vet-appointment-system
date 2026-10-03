package com.example.petclinic.dto.response;

import com.example.petclinic.domain.entity.Doctor;

public class DoctorResponseDTO {

    private Long doctorId;
    private String firstName;
    private String lastName;
    private String specialization;
    private String phone;
    private String email;
    private String workSchedule;

    public DoctorResponseDTO() {
    }

    public DoctorResponseDTO(Long doctorId, String firstName, String lastName, String specialization, String phone, String email, String workSchedule) {
        this.doctorId = doctorId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.phone = phone;
        this.email = email;
        this.workSchedule = workSchedule;
    }

    public static DoctorResponseDTO fromEntity(Doctor doctor) {
        if (doctor == null) {
            return null;
        }
        return new DoctorResponseDTO(
                doctor.getDoctorId(),
                doctor.getFirstName(),
                doctor.getLastName(),
                doctor.getSpecialization(),
                doctor.getPhone(),
                doctor.getEmail(),
                doctor.getWorkSchedule()
        );
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWorkSchedule() {
        return workSchedule;
    }

    public void setWorkSchedule(String workSchedule) {
        this.workSchedule = workSchedule;
    }
}
