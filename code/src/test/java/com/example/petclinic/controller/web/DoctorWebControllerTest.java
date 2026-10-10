package com.example.petclinic.controller.web;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.petclinic.dto.response.ClinicConfigResponseDTO;
import com.example.petclinic.dto.response.DoctorResponseDTO;
import com.example.petclinic.service.ClinicConfigService;
import com.example.petclinic.service.DoctorService;

@WebMvcTest(DoctorWebController.class)
class DoctorWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DoctorService doctorService;

    @MockitoBean
    private ClinicConfigService clinicConfigService;

    private MockHttpSession staffSession;

    @BeforeEach
    void setUp() {
        staffSession = new MockHttpSession();
        staffSession.setAttribute("isStaff", true);

        given(clinicConfigService.getConfigDTO()).willReturn(new ClinicConfigResponseDTO(
                "PawCare Pet Clinic & Vaccination", "กรุงเทพฯ", "02-123-4567", "089-999-8888",
                "09:00 - 20:00 น.", "09:00 - 18:00 น.", "09:00 - 17:00 น.",
                new BigDecimal("300.00"), new BigDecimal("150.00"), new BigDecimal("1500.00"), "1.0.0", 1));
        given(doctorService.getAllDoctors()).willReturn(List.of(
                new DoctorResponseDTO(1L, "นันทิดา", "รักษ์สัตว์", "อายุรกรรมทั่วไป, โรคผิวหนัง",
                        "0811112233", "nantida.r@vetclinic.com", "จันทร์ - ศุกร์: 09:00 - 17:00 น. (ห้องตรวจ 1)"),
                new DoctorResponseDTO(2L, "กิตติศักดิ์", "เจริญกิจ", "ศัลยกรรม",
                        "082-222-3344", "kittisak.k@vetclinic.com", null)));
    }

    @Test
    @DisplayName("/doctors: แสดงรายชื่อสัตวแพทย์จริงจากฐานข้อมูล + navbar กลาง")
    void doctorPage_showsRealDoctors() throws Exception {
        mockMvc.perform(get("/doctors"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/list"))
                .andExpect(model().attribute("doctors", hasSize(2)))
                .andExpect(content().string(containsString("นันทิดา รักษ์สัตว์")))
                .andExpect(content().string(containsString("พบสัตวแพทย์ทั้งหมด 2 ท่าน")))
                // เบอร์แบบมีขีดในข้อมูลเก่า ยังแสดงถูกรูปแบบ
                .andExpect(content().string(containsString("082-222-3344")))
                // ปุ่มจองนัดไปหน้าจองนัดหมายจริงพร้อมเลือกหมอ
                .andExpect(content().string(containsString("/appointments/new?doctorId=1")))
                // navbar กลางมีเมนู Staff Only
                .andExpect(content().string(containsString("Staff Only")))
                // ค่าบริการจาก ClinicConfigService (Singleton)
                .andExpect(content().string(containsString("300 บาท")));
    }

    @Test
    @DisplayName("/doctors: คนทั่วไปไม่เห็นปุ่มเพิ่ม/แก้ไข/ลบ")
    void doctorPage_guest_hidesStaffActions() throws Exception {
        mockMvc.perform(get("/doctors"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("เพิ่มข้อมูลสัตวแพทย์"))))
                .andExpect(content().string(not(containsString("doctorFormModal"))));
    }

    @Test
    @DisplayName("/doctors: เจ้าหน้าที่เห็นปุ่มเพิ่ม/แก้ไข/ลบ")
    void doctorPage_staff_showsStaffActions() throws Exception {
        mockMvc.perform(get("/doctors").session(staffSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("เพิ่มข้อมูลสัตวแพทย์")))
                .andExpect(content().string(containsString("doctorFormModal")))
                .andExpect(content().string(containsString("deleteDoctor(1)")));
    }

    @Test
    @DisplayName("/doctors: ยังไม่มีสัตวแพทย์ -> แสดงกล่องว่าง")
    void doctorPage_empty_showsEmptyState() throws Exception {
        given(doctorService.getAllDoctors()).willReturn(List.of());

        mockMvc.perform(get("/doctors"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ยังไม่มีข้อมูลสัตวแพทย์")));
    }

    @Test
    @DisplayName("/doctors.html (ลิงก์เก่า) -> redirect ไป /doctors")
    void oldStaticPage_redirects() throws Exception {
        mockMvc.perform(get("/doctors.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctors"));
    }
}
