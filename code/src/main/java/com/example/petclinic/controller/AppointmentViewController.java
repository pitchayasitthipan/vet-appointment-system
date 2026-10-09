package com.example.petclinic.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.example.petclinic.exception.InvalidAppointmentException;

@Controller
public class AppointmentViewController {
    @GetMapping("/appointments") public String appointments() { return "redirect:/appointments.html"; }
    @GetMapping("/appointments/new") public String create() { return "redirect:/appointment-create.html"; }
    @GetMapping("/pets") public String pets() { return "redirect:/pets.html"; }
    @GetMapping("/pets/new")
    public String newPet(@RequestParam Long ownerId, @RequestParam(required = false) String returnTo) {
        if (ownerId < 1) throw new InvalidAppointmentException("รหัสเจ้าของไม่ถูกต้อง");
        return "redirect:/pets.html?ownerId=" + ownerId + "&new=1"
            + ("appointment".equals(returnTo) ? "&returnTo=appointment" : "");
    }
}
