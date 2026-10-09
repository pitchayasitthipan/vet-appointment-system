package com.example.petclinic.controller.api;

import com.example.petclinic.controller.DoctorController;
import com.example.petclinic.exception.AppointmentExceptionHandler;
import com.example.petclinic.exception.InvalidAppointmentException;
import com.example.petclinic.service.DoctorService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Another module's MVC slice must load the advice without AppointmentTimeConfig. */
@WebMvcTest(DoctorController.class)
class AppointmentAdviceSliceTest {
    @Autowired ApplicationContext context;
    @Autowired MockMvc mvc;
    @MockitoBean DoctorService doctors;

    @Test void anotherModulesMvcSliceLoadsWithoutClockBean() throws Exception {
        assertThat(context.getBeansOfType(Clock.class)).isEmpty();
        AppointmentExceptionHandler advice = context.getBean(AppointmentExceptionHandler.class);
        LocalDateTime timestamp = (LocalDateTime) advice.invalid(new InvalidAppointmentException("test"))
            .getBody().get("timestamp");
        assertThat(timestamp).isBetween(LocalDateTime.now(ZoneId.of("Asia/Bangkok")).minusSeconds(5),
            LocalDateTime.now(ZoneId.of("Asia/Bangkok")).plusSeconds(5));
        when(doctors.getAllDoctors()).thenReturn(java.util.List.of());
        mvc.perform(get("/api/doctors")).andExpect(status().isOk());
    }
}
