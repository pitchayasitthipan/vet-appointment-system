package com.example.petclinic.controller;

import java.time.*;
import java.util.List;
import com.example.petclinic.domain.enums.*;
import com.example.petclinic.dto.response.*;
import com.example.petclinic.exception.*;
import com.example.petclinic.service.AppointmentService;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppointmentControllerTest {
    AppointmentService service;
    MockMvc mvc;
    final String body = """
        {"ownerId":1,"petId":2,"doctorId":3,"appointmentDateTime":"2027-01-04T09:00:00",
         "serviceType":"VACCINE","symptoms":"วัคซีน"}
        """;
    AppointmentResponseDTO response() {
        return new AppointmentResponseDTO(4L, 1L, 2L, "มะลิ", 3L, "หมอ ใจดี",
            LocalDateTime.of(2027,1,4,9,0), ServiceType.VACCINE, AppointmentStatus.PENDING, "วัคซีน", "สมุดวัคซีน", 0L);
    }
    @BeforeEach void setup() {
        service = mock(AppointmentService.class);
        mvc = MockMvcBuilders.standaloneSetup(new AppointmentController(service))
            .setControllerAdvice(new AppointmentExceptionHandler(Clock.system(ZoneId.of("Asia/Bangkok"))), new GlobalExceptionHandler()).build();
    }
    @Test void postReturnsCreatedAndLocation() throws Exception {
        when(service.create(any())).thenReturn(response());
        mvc.perform(post("/api/appointments").contentType("application/json").content(body))
            .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/appointments/4?ownerId=1"))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }
    @Test void validatesRequiredFieldsBeforeService() throws Exception {
        mvc.perform(post("/api/appointments").contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.petId").exists());
        verifyNoInteractions(service);
    }
    @Test void malformedEnumAndMissingOwnerReturnBadRequest() throws Exception {
        mvc.perform(post("/api/appointments").contentType("application/json").content(body.replace("VACCINE", "UNKNOWN")))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/appointments")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/appointments/availability").param("doctorId", "3").param("date", "bad-date"))
            .andExpect(status().isBadRequest());
    }
    @Test void mapsConflictNotFoundAndScheduleValidation() throws Exception {
        when(service.create(any())).thenThrow(new DuplicateResourceException("คิวซ้ำ"));
        mvc.perform(post("/api/appointments").contentType("application/json").content(body)).andExpect(status().isConflict());
        when(service.get(4L, 9L)).thenThrow(new ResourceNotFoundException("ไม่พบ"));
        mvc.perform(get("/api/appointments/4").param("ownerId", "9")).andExpect(status().isNotFound());
        doThrow(new InvalidAppointmentException("ไม่มีเวร")).when(service).create(any());
        mvc.perform(post("/api/appointments").contentType("application/json").content(body)).andExpect(status().isBadRequest());
    }
    @Test void listsAndCancelsForSpecifiedOwner() throws Exception {
        when(service.list(1L, null, 0, 10, "appointmentDateTime", "asc"))
            .thenReturn(new AppointmentPageDTO(List.of(response()), 0, 10, 1, 1));
        mvc.perform(get("/api/appointments").param("ownerId", "1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].ownerId").value(1));
        when(service.cancel(4L, 1L)).thenReturn(response());
        mvc.perform(patch("/api/appointments/4/cancel").param("ownerId", "1")).andExpect(status().isOk());
        verify(service).cancel(4L, 1L);
    }
}
