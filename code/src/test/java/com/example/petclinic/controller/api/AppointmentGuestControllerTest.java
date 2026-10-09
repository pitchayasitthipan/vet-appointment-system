package com.example.petclinic.controller.api;

import java.time.Clock;
import java.util.List;
import com.example.petclinic.dto.response.*;
import com.example.petclinic.exception.*;
import com.example.petclinic.service.AppointmentGuestService;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.*;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppointmentGuestControllerTest {
    AppointmentGuestService service;
    MockMvc mvc;
    @BeforeEach void setup() {
        service = mock(AppointmentGuestService.class);
        mvc = MockMvcBuilders.standaloneSetup(new AppointmentGuestController(service, "/owners.html"))
            .setControllerAdvice(new AppointmentExceptionHandler(Clock.systemUTC()), new GlobalExceptionHandler()).build();
    }
    @Test void lookupReturnsIdentityWithoutEmailOrPhone() throws Exception {
        when(service.lookup("0812345678")).thenReturn(new AppointmentGuestOwnerDTO(1L, "อ้น", "ทดสอบ"));
        mvc.perform(post("/api/v1/appointment-guests/lookup").contentType("application/json").content("{\"phone\":\"0812345678\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.ownerId").value(1))
            .andExpect(jsonPath("$.email").doesNotExist()).andExpect(jsonPath("$.phone").doesNotExist())
            .andExpect(request().sessionAttribute("myOwnerId", 1L));
    }
    @Test void lookupValidatesMissingPhoneAndReportsUnknownOwner() throws Exception {
        mvc.perform(post("/api/v1/appointment-guests/lookup").contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest());
        when(service.lookup("0812345678")).thenThrow(new ResourceNotFoundException("ลงทะเบียนเจ้าของใหม่"));
        mvc.perform(post("/api/v1/appointment-guests/lookup").contentType("application/json").content("{\"phone\":\"0812345678\"}"))
            .andExpect(status().isNotFound());
    }
    @Test void petsEndpointAndConfigSupplyBookingFlow() throws Exception {
        when(service.pets(1L)).thenReturn(List.of(new AppointmentGuestPetDTO(2L, "มะลิ")));
        MockHttpSession selected = new MockHttpSession(); selected.setAttribute("myOwnerId", 1L);
        mvc.perform(get("/api/v1/appointment-guests/1/pets").session(selected)).andExpect(status().isOk()).andExpect(jsonPath("$[0].petId").value(2));
        mvc.perform(get("/api/v1/appointment-guests/config")).andExpect(status().isOk())
            .andExpect(jsonPath("$.ownerRegistrationPath").value("/owners.html"))
            .andExpect(jsonPath("$.ownerRegistrationEnabled").value("false"))
            .andExpect(jsonPath("$.petRegistrationEnabled").value("false"));
    }

    @Test void blocksPetSelectionWithoutSessionOrForAnotherOwner() throws Exception {
        MockHttpSession selected = new MockHttpSession(); selected.setAttribute("myOwnerId", 1L);
        mvc.perform(get("/api/v1/appointment-guests/1/pets")).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/appointment-guests/9/pets").session(selected)).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test void unsuccessfulLookupClearsPreviouslySelectedOwner() throws Exception {
        MockHttpSession selected = new MockHttpSession(); selected.setAttribute("myOwnerId", 1L);
        when(service.lookup("0899999999")).thenThrow(new ResourceNotFoundException("ไม่พบเบอร์"));
        mvc.perform(post("/api/v1/appointment-guests/lookup").session(selected).contentType("application/json")
            .content("{\"phone\":\"0899999999\"}")).andExpect(status().isNotFound())
            .andExpect(request().sessionAttributeDoesNotExist("myOwnerId"));
    }
}
