package com.example.petclinic.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class DoctorRequestDTO {

    @NotBlank(message = "กรุณากรอกชื่อสัตวแพทย์")
    @Size(max = 100, message = "ชื่อต้องมีความยาวไม่เกิน 100 ตัวอักษร")
    private String firstName;

    @NotBlank(message = "กรุณากรอกนามสกุล")
    @Size(max = 100, message = "นามสกุลต้องมีความยาวไม่เกิน 100 ตัวอักษร")
    private String lastName;

    @Size(max = 100, message = "ความเชี่ยวชาญต้องมีความยาวไม่เกิน 100 ตัวอักษร")
    private String specialization;

    @NotBlank(message = "กรุณากรอกเบอร์โทรศัพท์")
    @Pattern(regexp = "^0[0-9]{8,9}$", message = "เบอร์โทรศัพท์ต้องขึ้นต้นด้วย 0 และมีความยาว 9-10 หลัก (เช่น 0812345678)")
    private String phone;

    @NotBlank(message = "กรุณากรอกอีเมล")
    @Email(message = "รูปแบบอีเมลไม่ถูกต้อง")
    @Size(max = 255, message = "อีเมลต้องมีความยาวไม่เกิน 255 ตัวอักษร")
    private String email;

    private String workSchedule;

    public DoctorRequestDTO() {
    }

    public DoctorRequestDTO(String firstName, String lastName, String specialization, String phone, String email, String workSchedule) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.phone = phone;
        this.email = email;
        this.workSchedule = workSchedule;
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
