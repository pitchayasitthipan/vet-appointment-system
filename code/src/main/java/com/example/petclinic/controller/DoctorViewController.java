package com.example.petclinic.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DoctorViewController {

    @GetMapping({"/doctors", "/veterinarians"})
    public String doctorPage() {
        return "forward:/doctors.html";
    }
}
