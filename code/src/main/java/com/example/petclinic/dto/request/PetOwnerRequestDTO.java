package com.example.petclinic.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PetOwnerRequestDTO {

    @NotBlank(message = "กรุณากรอกชื่อจริงของคุณ")
    @Size(max = 50, message = "ชื่อจริงต้องมีความยาวไม่เกิน 50 ตัวอักษร")
    private String firstName;

    @NotBlank(message = "กรุณากรอกนามสกุล")
    @Size(max = 50, message = "นามสกุลต้องมีความยาวไม่เกิน 50 ตัวอักษร")
    private String lastName;

    @NotBlank(message = "กรุณากรอกอีเมล")
    @Email(message = "รูปแบบอีเมลไม่ถูกต้อง")
    private String email;

    @NotBlank(message = "กรุณากรอกเบอร์โทรศัพท์")
    @Pattern(regexp = "^0[0-9]{8,9}$", message = "เบอร์โทรศัพท์ที่กรอกต้องไม่เกิน 10 หลัก (เช่น 0812345678)")
    private String phone;

    private String address;
    private String emergencyContactName;

    @Pattern(regexp = "^\\(|^0[0-9]{8,9}\\)", message = "เบอร์โทรศัพท์ที่กรอกต้องไม่เกิน 10 หลัก (เช่น 0812345678)")
    private String emergencyContactPhone;

    public PetOwnerRequestDTO() {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }
}