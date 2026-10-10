
package com.example.petclinic.controller.api;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.petclinic.dto.request.MedicalRecordRequestDTO;
import com.example.petclinic.dto.response.MedicalRecordResponseDTO;
import com.example.petclinic.exception.ResourceNotFoundException;
import com.example.petclinic.service.MedicalRecordService;

@WebMvcTest(MedicalRecordController.class)
class MedicalRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MedicalRecordService medicalRecordService;

    // ===== Tests เดิม: เจ้าหน้าที่เข้าสู่ระบบแล้ว =====

    @Test
    @DisplayName("GET all - คืนสถานะ 200 และข้อมูลแบบ Page")
    void getAllMedicalRecords_returnsPage() throws Exception {
        when(medicalRecordService.getAllMedicalRecords(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/medical-records")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "medicalRecordId,desc")
                        .sessionAttr("isStaff", true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(medicalRecordService)
                .getAllMedicalRecords(any(Pageable.class));
    }

    @Test
    @DisplayName("GET by ID - ไม่พบข้อมูลต้องคืน 404")
    void getMedicalRecordById_notFound() throws Exception {
        when(medicalRecordService.getMedicalRecordById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "ไม่พบประวัติการรักษาที่มีรหัส: 999"));

        mockMvc.perform(get("/api/v1/medical-records/999")
                        .sessionAttr("isStaff", true))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST - ไม่มี appointmentId ต้องคืน 400")
    void createMedicalRecord_missingAppointmentId() throws Exception {
        String invalidJson = """
                {
                    "diagnosis": "ตรวจสุขภาพทั่วไป",
                    "treatment": "ตรวจร่างกาย"
                }
                """;

        mockMvc.perform(post("/api/v1/medical-records")
                        .sessionAttr("isStaff", true)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.appointmentId").exists());
    }

    @Test
    @DisplayName("POST - ข้อมูลถูกต้องต้องคืน 201")
    void createMedicalRecord_success() throws Exception {
        String validJson = """
                {
                    "appointmentId": 1,
                    "diagnosis": "ตรวจสุขภาพทั่วไป",
                    "treatment": "ตรวจร่างกาย"
                }
                """;

        when(medicalRecordService.createMedicalRecord(
                any(MedicalRecordRequestDTO.class)))
                .thenReturn(new MedicalRecordResponseDTO());

        mockMvc.perform(post("/api/v1/medical-records")
                        .sessionAttr("isStaff", true)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isCreated());

        verify(medicalRecordService)
                .createMedicalRecord(any(MedicalRecordRequestDTO.class));
    }

    @Test
    @DisplayName("POST - appointmentId เป็น 0 ต้องคืน 400")
    void createMedicalRecord_zeroAppointmentId() throws Exception {
        String invalidJson = """
                {
                    "appointmentId": 0,
                    "diagnosis": "ตรวจสุขภาพทั่วไป"
                }
                """;

        mockMvc.perform(post("/api/v1/medical-records")
                        .sessionAttr("isStaff", true)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.appointmentId").exists());

        verify(medicalRecordService, never())
                .createMedicalRecord(any(MedicalRecordRequestDTO.class));
    }

    @Test
    @DisplayName("POST - appointmentId ติดลบต้องคืน 400")
    void createMedicalRecord_negativeAppointmentId() throws Exception {
        String invalidJson = """
                {
                    "appointmentId": -1,
                    "diagnosis": "ตรวจสุขภาพทั่วไป"
                }
                """;

        mockMvc.perform(post("/api/v1/medical-records")
                        .sessionAttr("isStaff", true)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.appointmentId").exists());

        verify(medicalRecordService, never())
                .createMedicalRecord(any(MedicalRecordRequestDTO.class));
    }

    @Test
    @DisplayName("PUT - appointmentId เป็น 0 ต้องคืน 400")
    void updateMedicalRecord_zeroAppointmentId() throws Exception {
        String invalidJson = """
                {
                    "appointmentId": 0,
                    "diagnosis": "แก้ไขผลตรวจ"
                }
                """;

        mockMvc.perform(put("/api/v1/medical-records/1")
                        .sessionAttr("isStaff", true)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.appointmentId").exists());

        verify(medicalRecordService, never())
                .updateMedicalRecord(any(Long.class),
                        any(MedicalRecordRequestDTO.class));
    }

    @Test
    @DisplayName("PUT - appointmentId ติดลบต้องคืน 400")
    void updateMedicalRecord_negativeAppointmentId() throws Exception {
        String invalidJson = """
                {
                    "appointmentId": -1,
                    "diagnosis": "แก้ไขผลตรวจ"
                }
                """;

        mockMvc.perform(put("/api/v1/medical-records/1")
                        .sessionAttr("isStaff", true)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.appointmentId").exists());

        verify(medicalRecordService, never())
                .updateMedicalRecord(any(Long.class),
                        any(MedicalRecordRequestDTO.class));
    }

    // ===== Tests ใหม่: ไม่มีสิทธิ์ Staff ต้องถูกปฏิเสธ =====

    @Test
    @DisplayName("GET all - ไม่มีสิทธิ์ Staff ต้องคืน 403")
    void getAll_withoutStaff_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/medical-records"))
                .andExpect(status().isForbidden());

        verify(medicalRecordService, never())
                .getAllMedicalRecords(any(Pageable.class));
    }

    @Test
    @DisplayName("GET by ID - ไม่มีสิทธิ์ Staff ต้องคืน 403")
    void getById_withoutStaff_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/medical-records/1"))
                .andExpect(status().isForbidden());

        verify(medicalRecordService, never())
                .getMedicalRecordById(1L);
    }

    @Test
    @DisplayName("GET by Appointment - ไม่มีสิทธิ์ Staff ต้องคืน 403")
    void getByAppointment_withoutStaff_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/medical-records/appointment/1"))
                .andExpect(status().isForbidden());

        verify(medicalRecordService, never())
                .getMedicalRecordsByAppointmentId(1L);
    }

    @Test
    @DisplayName("POST - ไม่มีสิทธิ์ Staff ต้องคืน 403")
    void create_withoutStaff_returns403() throws Exception {
        String validJson = """
                {
                    "appointmentId": 1,
                    "diagnosis": "ตรวจสุขภาพทั่วไป"
                }
                """;

        mockMvc.perform(post("/api/v1/medical-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isForbidden());

        verify(medicalRecordService, never())
                .createMedicalRecord(any(MedicalRecordRequestDTO.class));
    }

    @Test
    @DisplayName("PUT - ไม่มีสิทธิ์ Staff ต้องคืน 403")
    void update_withoutStaff_returns403() throws Exception {
        String validJson = """
                {
                    "appointmentId": 1,
                    "diagnosis": "แก้ไขผลตรวจ"
                }
                """;

        mockMvc.perform(put("/api/v1/medical-records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isForbidden());

        verify(medicalRecordService, never())
                .updateMedicalRecord(any(Long.class),
                        any(MedicalRecordRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE - ไม่มีสิทธิ์ Staff ต้องคืน 403")
    void delete_withoutStaff_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/medical-records/1"))
                .andExpect(status().isForbidden());

        verify(medicalRecordService, never())
                .deleteMedicalRecord(1L);
    }

    @Test
    @DisplayName("POST - ชื่อวัคซีนยาวเกิน 150 ตัว (ความยาวคอลัมน์ vaccine_name) ต้องคืน 400")
    void createMedicalRecord_vaccineNameTooLong() throws Exception {
        String invalidJson = """
                {
                    "appointmentId": 1,
                    "vaccineName": "%s"
                }
                """.formatted("ว".repeat(151));

        mockMvc.perform(post("/api/v1/medical-records")
                        .sessionAttr("isStaff", true)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.vaccineName").exists());

        verify(medicalRecordService, never())
                .createMedicalRecord(any(MedicalRecordRequestDTO.class));
    }
}
