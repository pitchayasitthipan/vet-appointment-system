package com.example.petclinic.controller.api;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.petclinic.dto.request.DoctorRequestDTO;
import com.example.petclinic.dto.response.DoctorResponseDTO;
import com.example.petclinic.exception.DuplicateResourceException;
import com.example.petclinic.exception.ResourceNotFoundException;
import com.example.petclinic.service.DoctorService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(DoctorController.class)
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DoctorService doctorService;

    @Autowired
    private ObjectMapper objectMapper;

    private DoctorRequestDTO requestDTO;
    private DoctorResponseDTO responseDTO;

    // session ของเจ้าหน้าที่ / คนทั่วไป
    private MockHttpSession staffSession;
    private MockHttpSession guestSession;

    @BeforeEach
    void setUp() {
        staffSession = new MockHttpSession();
        staffSession.setAttribute("isStaff", true);
        guestSession = new MockHttpSession();

        requestDTO = new DoctorRequestDTO("นันทิดา", "รักษ์สัตว์", "อายุรกรรมทั่วไป",
                "0811112233", "nantida.r@vetclinic.com", "จันทร์ - ศุกร์: 09:00 - 17:00 น. (ห้องตรวจ 1)");
        responseDTO = new DoctorResponseDTO(1L, "นันทิดา", "รักษ์สัตว์", "อายุรกรรมทั่วไป",
                "0811112233", "nantida.r@vetclinic.com", "จันทร์ - ศุกร์: 09:00 - 17:00 น. (ห้องตรวจ 1)");
    }

    @Test
    @DisplayName("GET /api/v1/doctors: ทุกคนดูรายชื่อแบบแบ่งหน้าได้ 200")
    void getDoctors_returnsPage() throws Exception {
        given(doctorService.getDoctors(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(responseDTO), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/doctors").param("page", "0").param("size", "10").session(guestSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("นันทิดา"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/doctors: ส่ง page/size/sort ไปให้ Service ตามที่ขอ")
    void getDoctors_passesPageable() throws Exception {
        given(doctorService.getDoctors(any(Pageable.class))).willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/doctors").param("page", "2").param("size", "5").param("sort", "firstName"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(doctorService).getDoctors(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
        assertThat(captor.getValue().getSort().getOrderFor("firstName")).isNotNull();
    }

    @Test
    @DisplayName("GET /api/v1/doctors/all: รายชื่อทั้งหมดสำหรับช่องเลือกหมอ 200")
    void getAllDoctors_returnsList() throws Exception {
        given(doctorService.getAllDoctors()).willReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/v1/doctors/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doctorId").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/doctors/{id}: ไม่พบ -> 404")
    void getDoctorById_notFound_returns404() throws Exception {
        given(doctorService.getDoctorById(99L)).willThrow(new ResourceNotFoundException("ไม่พบข้อมูลสัตวแพทย์ที่มีรหัส: 99"));

        mockMvc.perform(get("/api/v1/doctors/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/doctors: ไม่ใช่เจ้าหน้าที่ -> 403 และไม่บันทึก")
    void createDoctor_guest_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/doctors").session(guestSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());

        verify(doctorService, never()).createDoctor(any());
    }

    @Test
    @DisplayName("POST /api/v1/doctors: เจ้าหน้าที่เพิ่มได้ 201")
    void createDoctor_staff_returns201() throws Exception {
        given(doctorService.createDoctor(any(DoctorRequestDTO.class))).willReturn(responseDTO);

        mockMvc.perform(post("/api/v1/doctors").session(staffSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.doctorId").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/doctors: เบอร์มีขีด 081-111-2233 -> ตัดขีดแล้วผ่าน")
    void createDoctor_phoneWithDashes_isNormalized() throws Exception {
        given(doctorService.createDoctor(any(DoctorRequestDTO.class))).willReturn(responseDTO);
        requestDTO.setPhone("081-111-2233");

        mockMvc.perform(post("/api/v1/doctors").session(staffSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());

        ArgumentCaptor<DoctorRequestDTO> captor = ArgumentCaptor.forClass(DoctorRequestDTO.class);
        verify(doctorService).createDoctor(captor.capture());
        assertThat(captor.getValue().getPhone()).isEqualTo("0811112233");
    }

    @Test
    @DisplayName("POST /api/v1/doctors: ข้อมูลไม่ครบ/เบอร์ผิด -> 400 พร้อมบอกช่องที่ผิด")
    void createDoctor_invalid_returns400() throws Exception {
        DoctorRequestDTO invalid = new DoctorRequestDTO("", "รักษ์สัตว์", null, "12345", "not-email", null);

        mockMvc.perform(post("/api/v1/doctors").session(staffSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.phone").exists())
                .andExpect(jsonPath("$.errors.email").exists());

        verify(doctorService, never()).createDoctor(any());
    }

    @Test
    @DisplayName("POST /api/v1/doctors: อีเมลซ้ำ -> 409")
    void createDoctor_duplicateEmail_returns409() throws Exception {
        given(doctorService.createDoctor(any(DoctorRequestDTO.class)))
                .willThrow(new DuplicateResourceException("อีเมลนี้มีอยู่ในระบบแล้ว"));

        mockMvc.perform(post("/api/v1/doctors").session(staffSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/v1/doctors/{id}: ไม่ใช่เจ้าหน้าที่ -> 403")
    void updateDoctor_guest_returns403() throws Exception {
        mockMvc.perform(put("/api/v1/doctors/1").session(guestSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());

        verify(doctorService, never()).updateDoctor(any(), any());
    }

    @Test
    @DisplayName("PUT /api/v1/doctors/{id}: เจ้าหน้าที่แก้ไขได้ 200")
    void updateDoctor_staff_returns200() throws Exception {
        given(doctorService.updateDoctor(eq(1L), any(DoctorRequestDTO.class))).willReturn(responseDTO);

        mockMvc.perform(put("/api/v1/doctors/1").session(staffSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("รักษ์สัตว์"));
    }

    @Test
    @DisplayName("DELETE /api/v1/doctors/{id}: ไม่ใช่เจ้าหน้าที่ -> 403 และไม่ลบ")
    void deleteDoctor_guest_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/doctors/1").session(guestSession))
                .andExpect(status().isForbidden());

        verify(doctorService, never()).deleteDoctor(any());
    }

    @Test
    @DisplayName("DELETE /api/v1/doctors/{id}: เจ้าหน้าที่ลบได้ 200")
    void deleteDoctor_staff_returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/doctors/1").session(staffSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        verify(doctorService).deleteDoctor(1L);
    }

    @Test
    @DisplayName("DELETE /api/v1/doctors/{id}: ยังมีนัดหมาย -> 409")
    void deleteDoctor_withAppointments_returns409() throws Exception {
        org.mockito.Mockito.doThrow(new DuplicateResourceException("ไม่สามารถลบสัตวแพทย์ที่ยังมีนัดหมายในระบบได้"))
                .when(doctorService).deleteDoctor(1L);

        mockMvc.perform(delete("/api/v1/doctors/1").session(staffSession))
                .andExpect(status().isConflict());
    }
}
