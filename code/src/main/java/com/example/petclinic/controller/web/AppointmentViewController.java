package com.example.petclinic.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AppointmentViewController {
    @GetMapping({"/appointments", "/appointments.html"})
    public String appointments() { return "appointment/list"; }
    @GetMapping({"/appointments/new", "/appointment-create.html"})
    public String create() { return "appointment/create"; }
}
