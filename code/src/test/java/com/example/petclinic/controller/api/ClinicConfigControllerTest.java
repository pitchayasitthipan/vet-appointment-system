package com.example.petclinic.controller.api;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.petclinic.dto.response.ClinicConfigResponseDTO;
import com.example.petclinic.service.ClinicConfigService;

@WebMvcTest(ClinicConfigController.class)
class ClinicConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClinicConfigService clinicConfigService;

    private MockHttpSession staffSession;
    private MockHttpSession guestSession;

    @BeforeEach
    void setUp() {
        staffSession = new MockHttpSession();
        staffSession.setAttribute("isStaff", true);
        guestSession = new MockHttpSession();

        given(clinicConfigService.getConfigDTO()).willReturn(new ClinicConfigResponseDTO(
                "PawCare Pet Clinic & Vaccination", "กรุงเทพฯ", "02-123-4567", "089-999-8888",
                "09:00 - 20:00 น.", "09:00 - 18:00 น.", "09:00 - 17:00 น.",
                new BigDecimal("300.00"), new BigDecimal("150.00"), new BigDecimal("1500.00"), "1.0.0", 1));
    }

    @Test
    @DisplayName("GET /api/v1/config: ทุกคนดูข้อมูลคลินิกได้ 200")
    void getConfig_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/config").session(guestSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clinicName").value("PawCare Pet Clinic & Vaccination"));
    }

    @Test
    @DisplayName("GET /api/v1/config/singleton-check: แสดงข้อมูล Singleton 200")
    void singletonCheck_returns200() throws Exception {
        given(clinicConfigService.getInstanceHashCode()).willReturn(12345);

        mockMvc.perform(get("/api/v1/config/singleton-check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instanceIdentityHashCode").value(12345));
    }

    @Test
    @DisplayName("PUT /api/v1/config/fees: ไม่ใช่เจ้าหน้าที่ -> 403 และค่าบริการไม่เปลี่ยน")
    void updateFees_guest_returns403() throws Exception {
        mockMvc.perform(put("/api/v1/config/fees").param("consultation", "1").session(guestSession))
                .andExpect(status().isForbidden());

        verify(clinicConfigService, never()).updatePricePolicy(any(), any(), any());
    }

    @Test
    @DisplayName("PUT /api/v1/config/fees: เจ้าหน้าที่แก้ไขได้ 200")
    void updateFees_staff_returns200() throws Exception {
        mockMvc.perform(put("/api/v1/config/fees").param("consultation", "350").session(staffSession))
                .andExpect(status().isOk());

        verify(clinicConfigService).updatePricePolicy(new BigDecimal("350"), null, null);
    }

    @Test
    @DisplayName("PUT /api/v1/config/fees: ค่าบริการติดลบ -> 400")
    void updateFees_negative_returns400() throws Exception {
        doThrow(new IllegalArgumentException("ค่าบริการต้องไม่ติดลบ"))
                .when(clinicConfigService).updatePricePolicy(new BigDecimal("-1"), null, null);

        mockMvc.perform(put("/api/v1/config/fees").param("consultation", "-1").session(staffSession))
                .andExpect(status().isBadRequest());
    }
}
