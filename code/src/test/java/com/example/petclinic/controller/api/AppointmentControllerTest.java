package com.example.petclinic.controller.api;

import java.time.*;
import java.util.List;
import com.example.petclinic.domain.enums.*;
import com.example.petclinic.dto.response.*;
import com.example.petclinic.exception.*;
import com.example.petclinic.service.AppointmentService;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.*;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppointmentControllerTest {
    AppointmentService service;
    MockMvc mvc;
    MockHttpSession ownerSession;
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
        ownerSession = new MockHttpSession(); ownerSession.setAttribute("myOwnerId", 1L);
        mvc = MockMvcBuilders.standaloneSetup(new AppointmentController(service))
            .setControllerAdvice(new AppointmentExceptionHandler(Clock.system(ZoneId.of("Asia/Bangkok"))), new GlobalExceptionHandler()).build();
    }
    @Test void postReturnsCreatedAndLocation() throws Exception {
        when(service.create(any())).thenReturn(response());
        mvc.perform(post("/api/v1/appointments").contentType("application/json").content(body).session(ownerSession))
            .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/v1/appointments/4?ownerId=1"))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }
    @Test void validatesRequiredFieldsBeforeService() throws Exception {
        mvc.perform(post("/api/v1/appointments").contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.petId").exists());
        verifyNoInteractions(service);
    }
    @Test void malformedEnumAndMissingOwnerReturnBadRequest() throws Exception {
        mvc.perform(post("/api/v1/appointments").contentType("application/json").content(body.replace("VACCINE", "UNKNOWN")))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/appointments")).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/appointments/availability").param("doctorId", "3").param("date", "bad-date"))
            .andExpect(status().isBadRequest());
    }
    @Test void mapsConflictNotFoundAndScheduleValidation() throws Exception {
        when(service.create(any())).thenThrow(new DuplicateResourceException("คิวซ้ำ"));
        mvc.perform(post("/api/v1/appointments").contentType("application/json").content(body).session(ownerSession)).andExpect(status().isConflict());
        when(service.get(4L, 1L)).thenThrow(new ResourceNotFoundException("ไม่พบ"));
        mvc.perform(get("/api/v1/appointments/4").param("ownerId", "1").session(ownerSession)).andExpect(status().isNotFound());
        doThrow(new InvalidAppointmentException("ไม่มีเวร")).when(service).create(any());
        mvc.perform(post("/api/v1/appointments").contentType("application/json").content(body).session(ownerSession)).andExpect(status().isBadRequest());
    }
    @Test void listsAndCancelsForSpecifiedOwner() throws Exception {
        when(service.list(1L, null, 0, 10, "appointmentDateTime", "asc"))
            .thenReturn(new AppointmentPageDTO(List.of(response()), 0, 10, 1, 1));
        mvc.perform(get("/api/v1/appointments").param("ownerId", "1").session(ownerSession))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].ownerId").value(1));
        when(service.cancel(4L, 1L)).thenReturn(response());
        mvc.perform(patch("/api/v1/appointments/4/cancel").param("ownerId", "1").session(ownerSession)).andExpect(status().isOk());
        verify(service).cancel(4L, 1L);
    }

    @Test void rejectsMissingSessionOnPersonalEndpoints() throws Exception {
        mvc.perform(post("/api/v1/appointments").contentType("application/json").content(body)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/appointments").param("ownerId", "1")).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/appointments/4").param("ownerId", "1")).andExpect(status().isForbidden());
        mvc.perform(patch("/api/v1/appointments/4/cancel").param("ownerId", "1")).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/appointments/4").param("ownerId", "1")
            .contentType("application/json").content(updateBody())).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    private String updateBody() {
        return "{\"doctorId\":3,\"appointmentDateTime\":\"2027-01-04T09:00:00\",\"serviceType\":\"VACCINE\",\"symptoms\":\"test\",\"version\":0}";
    }
    @Test void blocksChangingOwnerIdInUrlOrBodyBeforeCallingService() throws Exception {
        mvc.perform(post("/api/v1/appointments").session(ownerSession).contentType("application/json")
            .content(body.replace("\"ownerId\":1", "\"ownerId\":9"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/appointments").session(ownerSession).param("ownerId", "9")).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/appointments/4").session(ownerSession).param("ownerId", "9")).andExpect(status().isForbidden());
        mvc.perform(patch("/api/v1/appointments/4/cancel").session(ownerSession).param("ownerId", "9")).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/appointments/4").session(ownerSession).param("ownerId", "9")
            .contentType("application/json").content(updateBody())).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test void usesSessionOwnerWhenQueryIsOmitted() throws Exception {
        when(service.get(4L, 1L)).thenReturn(response());
        mvc.perform(get("/api/v1/appointments/4").session(ownerSession)).andExpect(status().isOk());
        verify(service).get(4L, 1L);
    }
    @Test void staffSessionCanManageAnExplicitOwnerButClientFlagsCannotGrantStaff() throws Exception {
        MockHttpSession staff = new MockHttpSession(); staff.setAttribute("isStaff", true);
        when(service.get(4L, 9L)).thenReturn(response());
        mvc.perform(get("/api/v1/appointments/4").session(staff).param("ownerId", "9")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/appointments/4").param("ownerId", "9").param("isStaff", "true"))
            .andExpect(status().isForbidden());
        verify(service, times(1)).get(4L, 9L);
    }

    @Test void staffEndpointsRejectGuestAndForgedFlag() throws Exception {
        mvc.perform(get("/api/v1/appointments/staff").session(ownerSession)).andExpect(status().isForbidden())
            .andExpect(jsonPath("$.path").value("/api/v1/appointments/staff"))
            .andExpect(jsonPath("$.error").value("Forbidden"));
        mvc.perform(get("/api/v1/appointments/staff?isStaff=true")).andExpect(status().isForbidden());
        mvc.perform(patch("/api/v1/appointments/4/status").session(ownerSession).contentType("application/json")
            .content("{\"status\":\"CONFIRMED\",\"version\":0}")).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test void staffCanChangeStatusButCannotAfterLogout() throws Exception {
        MockHttpSession staff = new MockHttpSession(); staff.setAttribute("isStaff", true);
        when(service.changeStatus(eq(4L), any())).thenReturn(response());
        mvc.perform(patch("/api/v1/appointments/4/status").session(staff).contentType("application/json")
            .content("{\"status\":\"CONFIRMED\",\"version\":0}")).andExpect(status().isOk());
        mvc.perform(patch("/api/v1/appointments/4/status").session(staff).contentType("application/json")
            .content("{\"status\":\"COMPLETED\",\"version\":-1}")).andExpect(status().isBadRequest());
        staff.removeAttribute("isStaff");
        mvc.perform(patch("/api/v1/appointments/4/status").session(staff).contentType("application/json")
            .content("{\"status\":\"COMPLETED\",\"version\":0}")).andExpect(status().isForbidden());
        verify(service, times(1)).changeStatus(eq(4L), any());
    }

}
