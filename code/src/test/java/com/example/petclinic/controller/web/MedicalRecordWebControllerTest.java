
package com.example.petclinic.controller.web;

import java.util.List;

import com.example.petclinic.dto.response.MedicalRecordResponseDTO;
import com.example.petclinic.exception.ResourceNotFoundException;
import com.example.petclinic.service.MedicalRecordService;
import com.example.petclinic.exception.DuplicateResourceException;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(MedicalRecordWebController.class)
class MedicalRecordWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MedicalRecordService medicalRecordService;

    // ===== Tests เดิม: จำลองว่าเข้าสู่ระบบ Staff แล้ว =====

    @Test
    @DisplayName("เปิดรายละเอียดที่ไม่มีอยู่จริง ต้อง Redirect กลับหน้ารายการ")
    void viewMedicalRecord_notFound_redirectsToList() throws Exception {

        when(medicalRecordService.getMedicalRecordById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "ไม่พบประวัติการรักษาที่มีรหัส: 999"));

        mockMvc.perform(get("/medical-records/999")
                        .sessionAttr("isStaff", true))
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

        mockMvc.perform(get("/medical-records/999/edit")
                        .sessionAttr("isStaff", true))
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

        mockMvc.perform(post("/medical-records/999/delete")
                        .sessionAttr("isStaff", true))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/medical-records"))
                .andExpect(flash().attribute(
                        "errorMessage", "ไม่พบประวัติการรักษาที่ต้องการ"));
    }

    @Test
    @DisplayName("แก้ไขข้อมูลที่ไม่มีอยู่จริง ต้อง Redirect กลับหน้ารายการ")
    void updateMedicalRecord_notFound_redirectsToList() throws Exception {

        // ตรวจว่าประวัติมีอยู่ก่อนแก้ไข -> ไม่พบ ต้องกลับหน้ารายการ
        when(medicalRecordService.getMedicalRecordById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "ไม่พบประวัติการรักษา"));

        mockMvc.perform(post("/medical-records/999/edit")
                        .param("appointmentId", "1")
                        .param("diagnosis", "ตรวจสุขภาพทั่วไป")
                        .sessionAttr("isStaff", true))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/medical-records"))
                .andExpect(flash().attribute(
                        "errorMessage", "ไม่พบประวัติการรักษาที่ต้องการ"));
    }

    @Test
    @DisplayName("GET /medical-records - ต้องแสดง HTML สำเร็จ")
    void listMedicalRecords_rendersHtml() throws Exception {

        when(medicalRecordService.getAllMedicalRecords())
                .thenReturn(List.of());

        mockMvc.perform(get("/medical-records")
                        .sessionAttr("isStaff", true))
                .andExpect(status().isOk())
                .andExpect(view().name("medicalrecord/list"))
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }

    @Test
    @DisplayName("GET /medical-records/new - ต้องแสดง HTML สำเร็จ")
    void showCreateForm_rendersHtml() throws Exception {

        mockMvc.perform(get("/medical-records/new")
                        .sessionAttr("isStaff", true))
                .andExpect(status().isOk())
                .andExpect(view().name("medicalrecord/form"))
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }

    @Test
    @DisplayName("GET /medical-records/{id} - ต้องแสดง HTML สำเร็จ")
    void viewMedicalRecord_rendersHtml() throws Exception {

        MedicalRecordResponseDTO record = new MedicalRecordResponseDTO();

        when(medicalRecordService.getMedicalRecordById(1L))
                .thenReturn(record);

        mockMvc.perform(get("/medical-records/1")
                        .sessionAttr("isStaff", true))
                .andExpect(status().isOk())
                .andExpect(view().name("medicalrecord/detail"))
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }

    // ===== Tests ใหม่: ผู้ที่ไม่ได้เป็น Staff ต้องเข้าถึงไม่ได้ =====

    @Test
    @DisplayName("ผู้ที่ไม่ใช่ Staff ไม่สามารถดูรายการประวัติการรักษาได้")
    void listMedicalRecords_withoutStaff_redirectsToLogin() throws Exception {

        mockMvc.perform(get("/medical-records"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owners/staff"));

        verify(medicalRecordService, never()).getAllMedicalRecords();
    }

    @Test
    @DisplayName("ผู้ที่ไม่ใช่ Staff ไม่สามารถเปิดฟอร์มเพิ่มประวัติได้")
    void showCreateForm_withoutStaff_redirectsToLogin() throws Exception {

        mockMvc.perform(get("/medical-records/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owners/staff"));
    }

    @Test
    @DisplayName("ผู้ที่ไม่ใช่ Staff ไม่สามารถลบประวัติการรักษาได้")
    void deleteMedicalRecord_withoutStaff_redirectsToLogin() throws Exception {

        mockMvc.perform(post("/medical-records/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owners/staff"));

        verify(medicalRecordService, never()).deleteMedicalRecord(1L);
    }

// ===== บันทึกกับนัดหมายที่ใช้ไม่ได้: ต้องกลับฟอร์มเดิมพร้อมข้อความใต้ช่องรหัสนัดหมาย =====

    @Test
    @DisplayName("สร้างประวัติกับนัดที่ยังไม่ COMPLETED ต้องกลับฟอร์มเดิม ไม่ใช่ JSON")
    void createMedicalRecord_appointmentNotCompleted_showsFormError() throws Exception {

        when(medicalRecordService.createMedicalRecord(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new DuplicateResourceException(
                        "บันทึกประวัติการรักษาได้เฉพาะนัดที่เสร็จสิ้นแล้ว (COMPLETED)"));

        mockMvc.perform(post("/medical-records")
                        .param("appointmentId", "1")
                        .param("diagnosis", "ตรวจสุขภาพทั่วไป")
                        .sessionAttr("isStaff", true))
                .andExpect(status().isOk())
                .andExpect(view().name("medicalrecord/form"))
                .andExpect(model().attributeHasFieldErrors("medicalRecord", "appointmentId"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "บันทึกประวัติการรักษาได้เฉพาะนัดที่เสร็จสิ้นแล้ว")));
    }

    @Test
    @DisplayName("สร้างประวัติกับนัดที่ไม่มีอยู่จริง ต้องกลับฟอร์มเดิมพร้อมข้อความ")
    void createMedicalRecord_appointmentNotFound_showsFormError() throws Exception {

        when(medicalRecordService.createMedicalRecord(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new ResourceNotFoundException(
                        "ไม่พบนัดหมายสำหรับบันทึกประวัติการรักษา"));

        mockMvc.perform(post("/medical-records")
                        .param("appointmentId", "99999")
                        .param("diagnosis", "ตรวจสุขภาพทั่วไป")
                        .sessionAttr("isStaff", true))
                .andExpect(status().isOk())
                .andExpect(view().name("medicalrecord/form"))
                .andExpect(model().attributeHasFieldErrors("medicalRecord", "appointmentId"));
    }

    @Test
    @DisplayName("แก้ไขประวัติเป็นนัดที่ยังไม่ COMPLETED ต้องกลับฟอร์มแก้ไขเดิม")
    void updateMedicalRecord_appointmentNotCompleted_showsFormError() throws Exception {

        when(medicalRecordService.updateMedicalRecord(
                eq(1L),
                org.mockito.ArgumentMatchers.any()))
                .thenThrow(new DuplicateResourceException(
                        "บันทึกประวัติการรักษาได้เฉพาะนัดที่เสร็จสิ้นแล้ว (COMPLETED)"));

        mockMvc.perform(post("/medical-records/1/edit")
                        .param("appointmentId", "1")
                        .param("diagnosis", "ตรวจสุขภาพทั่วไป")
                        .sessionAttr("isStaff", true))
                .andExpect(status().isOk())
                .andExpect(view().name("medicalrecord/form"))
                .andExpect(model().attribute("recordId", 1L))
                .andExpect(model().attributeHasFieldErrors("medicalRecord", "appointmentId"));
    }

}
