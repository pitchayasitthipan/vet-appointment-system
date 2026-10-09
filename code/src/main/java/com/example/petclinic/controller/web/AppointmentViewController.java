package com.example.petclinic.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AppointmentViewController {
    @GetMapping("/appointments") public String appointments() { return "redirect:/appointments.html"; }
    @GetMapping("/appointments/new") public String create() { return "redirect:/appointment-create.html"; }
}
