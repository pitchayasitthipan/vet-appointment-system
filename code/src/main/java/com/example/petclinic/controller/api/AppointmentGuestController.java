package com.example.petclinic.controller.api;

import java.util.*;
import com.example.petclinic.dto.request.PhoneLookupRequestDTO;
import com.example.petclinic.dto.response.*;
import com.example.petclinic.service.AppointmentGuestService;
import jakarta.validation.Valid;
import com.example.petclinic.controller.StaffAccess;
import jakarta.servlet.http.HttpSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appointment-guests")
@Tag(name = "Appointment Guests", description = "เลือกแฟ้มเจ้าของด้วยเบอร์โทรและเก็บสิทธิ์ใน session")
public class AppointmentGuestController {
    private final AppointmentGuestService service;
    private final String registrationPath;
    @Value("${appointments.owner-registration-enabled:true}")
    private boolean registrationEnabled;
    @Value("${appointments.pet-registration-enabled:true}")
    private boolean petRegistrationEnabled;
    public AppointmentGuestController(AppointmentGuestService service,
            @Value("${appointments.owner-registration-path:/owners/new}") String registrationPath) {
        this.service = service; this.registrationPath = registrationPath;
    }
    @PostMapping("/lookup")
    @Operation(summary = "ค้นเบอร์และเลือกแฟ้มเจ้าของสำหรับ session นี้")
    public AppointmentGuestOwnerDTO lookup(@Valid @RequestBody PhoneLookupRequestDTO request, HttpSession session) {
        session.removeAttribute(AppointmentAccess.OWNER_KEY);
        AppointmentGuestOwnerDTO owner = service.lookup(request.phone());
        session.setAttribute(AppointmentAccess.OWNER_KEY, owner.ownerId());
        return owner;
    }
    @GetMapping("/{ownerId}/pets")
    @Operation(summary = "ดูสัตว์ของแฟ้มที่เลือกใน session หรือแฟ้มที่ Staff เลือก")
    public List<AppointmentGuestPetDTO> pets(@PathVariable Long ownerId, HttpSession session) {
        return service.pets(AppointmentAccess.owner(session, ownerId));
    }

    @GetMapping("/me")
    @Operation(summary = "อ่านแฟ้มที่ค้นหาแล้วและสิทธิ์ Staff จาก session ปัจจุบัน")
    public AppointmentGuestSessionDTO me(@RequestParam(required = false) Long ownerId, HttpSession session) {
        boolean staff = StaffAccess.isStaff(session);
        if (staff && ownerId == null) return new AppointmentGuestSessionDTO(null, null, null, true);
        AppointmentGuestOwnerDTO owner = service.owner(AppointmentAccess.owner(session, ownerId));
        return new AppointmentGuestSessionDTO(owner.ownerId(), owner.firstName(), owner.lastName(), staff);
    }

    @GetMapping("/config")
    @Operation(summary = "อ่านเส้นทางและสถานะความพร้อมของจุดเชื่อมหน้าลงทะเบียน")
    public Map<String, String> config() {
        return Map.of("ownerRegistrationPath", registrationPath, "bookingReturnPath", "/appointments/new",
            "ownerRegistrationEnabled", Boolean.toString(registrationEnabled),
            "petRegistrationEnabled", Boolean.toString(petRegistrationEnabled));
    }
}
