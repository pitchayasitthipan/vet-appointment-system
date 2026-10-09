
package com.example.petclinic.controller.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.petclinic.controller.api.MedicalRecordController;
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

    // ทดสอบ API ดึงรายการแบบแบ่งหน้า
    @Test
    @DisplayName("GET all - คืนสถานะ 200 และข้อมูลแบบ Page")
    void getAllMedicalRecords_returnsPage() throws Exception {
        when(medicalRecordService.getAllMedicalRecords(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/medical-records")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "medicalRecordId,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(medicalRecordService)
                .getAllMedicalRecords(any(Pageable.class));
    }

    // ทดสอบกรณีไม่พบประวัติการรักษา
    @Test
    @DisplayName("GET by ID - ไม่พบข้อมูลต้องคืน 404")
    void getMedicalRecordById_notFound() throws Exception {
        when(medicalRecordService.getMedicalRecordById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "ไม่พบประวัติการรักษาที่มีรหัส: 999"));

        mockMvc.perform(get("/api/v1/medical-records/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    // ทดสอบข้อมูลไม่ผ่าน Validation
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.appointmentId").exists());
    }

    // ทดสอบสร้างประวัติการรักษาสำเร็จ
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isCreated());

        verify(medicalRecordService)
                .createMedicalRecord(any(MedicalRecordRequestDTO.class));
    }
}
