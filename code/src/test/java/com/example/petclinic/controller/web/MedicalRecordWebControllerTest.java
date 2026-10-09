
package com.example.petclinic.controller.web;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.mockito.ArgumentMatchers.anyLong;

import java.util.List;

import com.example.petclinic.dto.response.MedicalRecordResponseDTO;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.petclinic.exception.ResourceNotFoundException;
import com.example.petclinic.service.MedicalRecordService;

@WebMvcTest(MedicalRecordWebController.class)
class MedicalRecordWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MedicalRecordService medicalRecordService;

    @Test
    @DisplayName("เปิดรายละเอียดที่ไม่มีอยู่จริง ต้อง Redirect กลับหน้ารายการ")
    void viewMedicalRecord_notFound_redirectsToList() throws Exception {

        when(medicalRecordService.getMedicalRecordById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "ไม่พบประวัติการรักษาที่มีรหัส: 999"));

        mockMvc.perform(get("/medical-records/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/medical-records"))
                .andExpect(flash().attribute(
                        "errorMessage", "ไม่พบประวัติการรักษาที่ต้องการ"));
    }

    @Test
    @DisplayName("เปิดหน้าแก้ไขที่ไม่มีอยู่จริง ต้อง Redirect กลับหน้ารายการ")
    void showEditForm_notFound_redirectsToList() throws Exception {

        when(medicalRecordService.getMedicalRecordById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "ไม่พบประวัติการรักษาที่มีรหัส: 999"));

        mockMvc.perform(get("/medical-records/999/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/medical-records"))
                .andExpect(flash().attribute(
                        "errorMessage", "ไม่พบประวัติการรักษาที่ต้องการ"));
    }

    @Test
    @DisplayName("ลบข้อมูลที่ไม่มีอยู่จริง ต้อง Redirect กลับหน้ารายการ")
    void deleteMedicalRecord_notFound_redirectsToList() throws Exception {

        doThrow(new ResourceNotFoundException("ไม่พบประวัติการรักษา"))
                .when(medicalRecordService)
                .deleteMedicalRecord(999L);

        mockMvc.perform(post("/medical-records/999/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/medical-records"))
                .andExpect(flash().attribute(
                        "errorMessage", "ไม่พบประวัติการรักษาที่ต้องการ"));
    }

    @Test
    @DisplayName("แก้ไขข้อมูลที่ไม่มีอยู่จริง ต้อง Redirect กลับหน้ารายการ")
    void updateMedicalRecord_notFound_redirectsToList() throws Exception {

        when(medicalRecordService.updateMedicalRecord(
                eq(999L),
                org.mockito.ArgumentMatchers.any()))
                .thenThrow(new ResourceNotFoundException("ไม่พบประวัติการรักษา"));

        mockMvc.perform(post("/medical-records/999/edit")
                    .param("appointmentId", "1")
                    .param("diagnosis", "ตรวจสุขภาพทั่วไป"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/medical-records"))
                .andExpect(flash().attribute(
                        "errorMessage", "ไม่พบประวัติการรักษาที่ต้องการ"));
    }

    // ตรวจว่า Thymeleaf แสดงหน้ารายการได้จริง
    @Test
    @DisplayName("GET /medical-records - ต้องแสดง HTML สำเร็จ")
    void listMedicalRecords_rendersHtml() throws Exception {
        when(medicalRecordService.getAllMedicalRecords())
                .thenReturn(List.of());

        mockMvc.perform(get("/medical-records"))
                .andExpect(status().isOk())
                .andExpect(view().name("medicalrecord/list"))
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }

    // ตรวจว่า Thymeleaf แสดงฟอร์มเพิ่มข้อมูลได้จริง
    @Test
    @DisplayName("GET /medical-records/new - ต้องแสดง HTML สำเร็จ")
    void showCreateForm_rendersHtml() throws Exception {
        mockMvc.perform(get("/medical-records/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("medicalrecord/form"))
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }

    // ตรวจว่า Thymeleaf แสดงหน้ารายละเอียดได้จริง
    @Test
    @DisplayName("GET /medical-records/{id} - ต้องแสดง HTML สำเร็จ")
    void viewMedicalRecord_rendersHtml() throws Exception {
        MedicalRecordResponseDTO record = new MedicalRecordResponseDTO();

        when(medicalRecordService.getMedicalRecordById(1L))
                .thenReturn(record);

        mockMvc.perform(get("/medical-records/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("medicalrecord/detail"))
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }
}
