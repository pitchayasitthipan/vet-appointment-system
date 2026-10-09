
package com.example.petclinic.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PetPageController {

    @GetMapping("/pets")
    public String petsPage() {
        return "redirect:/pets.html";
    }
}
