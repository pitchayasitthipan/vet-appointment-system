package com.example.petclinic.controller.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import com.example.petclinic.dto.request.PetOwnerRequestDTO;
import com.example.petclinic.dto.response.PetOwnerResponseDTO;
import com.example.petclinic.exception.DuplicateResourceException;
import com.example.petclinic.exception.ResourceNotFoundException;
import com.example.petclinic.service.PetOwnerService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(PetOwnerController.class)
class PetOwnerControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private PetOwnerService petOwnerService;

        @Autowired
        private ObjectMapper objectMapper;

        private PetOwnerResponseDTO responseDTO;
        private PetOwnerRequestDTO requestDTO;

        // session ที่ใส่รหัสเจ้าหน้าที่แล้ว (API ดูทั้งหมด/ดูตาม Id/แก้ไข/ลบ ต้องใช้)
        private MockHttpSession staffSession;

        @BeforeEach
        void setUp() {
                staffSession = new MockHttpSession();
                staffSession.setAttribute("isStaff", true);

                requestDTO = new PetOwnerRequestDTO();
                requestDTO.setFirstName("John");
                requestDTO.setLastName("Doe");
                requestDTO.setEmail("test@example.com");
                requestDTO.setPhone("0876543210");
                requestDTO.setAddress("Khon Kaen");
                requestDTO.setEmergencyContactName("familyMember");
                requestDTO.setEmergencyContactPhone("0987654321");

                responseDTO = new PetOwnerResponseDTO();
                responseDTO.setOwnerId(1L);
                responseDTO.setFirstName("John");
                responseDTO.setLastName("Doe");
                responseDTO.setEmail("test@example.com");
                responseDTO.setPhone("0876543210");
                responseDTO.setAddress("Khon Kaen");
                responseDTO.setEmergencyContactName("familyMember");
                responseDTO.setEmergencyContactPhone("0987654321");
        }

        @Test
        @DisplayName("ดึงข้อมูลเจ้าของสัตว์เลี้ยงทั้งหมด: สำเร็จ")
        void testGetAllPetOwners_Success() throws Exception {
                // Controller ใส่ค่า sort เริ่มต้น Id น้อยไปมาก เลยสร้าง pageable ให้ตรงกัน
                Pageable pageable = PageRequest.of(0, 5, Sort.by("ownerId"));
                given(petOwnerService.getAllPetOwners(pageable))
                                .willReturn(new PageImpl<>(java.util.List.of(responseDTO), pageable, 1));

                mockMvc.perform(get("/api/v1/owners?page=0&size=5").session(staffSession)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].ownerId").value(1))
                                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                                .andExpect(jsonPath("$.content[0].email").value("test@example.com"))
                                .andExpect(jsonPath("$.totalElements").value(1))
                                .andExpect(jsonPath("$.totalPages").value(1));

                verify(petOwnerService, times(1)).getAllPetOwners(pageable);
        }

        @Test
        @DisplayName("ค้นหาเจ้าของสัตว์เลี้ยงด้วย Id: พบข้อมูล")
        void testGetPetOwnerById_Found() throws Exception {
                given(petOwnerService.getPetOwnerById(1L)).willReturn(responseDTO);

                mockMvc.perform(get("/api/v1/owners/1").session(staffSession)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.ownerId").value(1))
                                .andExpect(jsonPath("$.firstName").value("John"))
                                .andExpect(jsonPath("$.address").value("Khon Kaen"));

                verify(petOwnerService, times(1)).getPetOwnerById(1L);
        }

        @Test
        @DisplayName("ค้นหาเจ้าของสัตว์เลี้ยงด้วย Id: ไม่พบข้อมูล (404 Not Found)")
        void testGetPetOwnerById_NotFound() throws Exception {
                given(petOwnerService.getPetOwnerById(99L))
                                .willThrow(new ResourceNotFoundException("ไม่พบข้อมูลเจ้าของสัตว์เลี้ยงรหัส: 99"));

                mockMvc.perform(get("/api/v1/owners/99").session(staffSession)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());

                verify(petOwnerService, times(1)).getPetOwnerById(99L);
        }

        @Test
        @DisplayName("ค้นหาเจ้าของสัตว์เลี้ยงด้วยเบอร์โทร: พบข้อมูล (เจ้าหน้าที่เห็นครบ)")
        void testGetPetOwnerByPhone_Found() throws Exception {
                given(petOwnerService.getPetOwnerByPhone("0876543210")).willReturn(responseDTO);

                mockMvc.perform(get("/api/v1/owners/search?phone=0876543210").session(staffSession)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.ownerId").value(1))
                                .andExpect(jsonPath("$.phone").value("0876543210"));

                verify(petOwnerService, times(1)).getPetOwnerByPhone("0876543210");
        }

        @Test
        @DisplayName("ค้นหาเจ้าของสัตว์เลี้ยงด้วยเบอร์โทร: ลูกค้าเห็นแค่ ownerId กับชื่อ")
        void testGetPetOwnerByPhone_Guest() throws Exception {
                given(petOwnerService.getPetOwnerByPhone("0876543210")).willReturn(responseDTO);

                mockMvc.perform(get("/api/v1/owners/search?phone=0876543210")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.ownerId").value(1))
                                .andExpect(jsonPath("$.firstName").value(responseDTO.getFirstName()))
                                .andExpect(jsonPath("$.email").doesNotExist())
                                .andExpect(jsonPath("$.address").doesNotExist())
                                .andExpect(jsonPath("$.emergencyContactPhone").doesNotExist());
        }

        @Test
        @DisplayName("ค้นหาเจ้าของสัตว์เลี้ยงด้วยเบอร์โทร: ไม่พบข้อมูล (404 Not Found)")
        void testGetPetOwnerByPhone_NotFound() throws Exception {
                given(petOwnerService.getPetOwnerByPhone("0800000000"))
                                .willThrow(new ResourceNotFoundException(
                                                "ไม่พบเจ้าของสัตว์เลี้ยงที่ใช้เบอร์: 0800000000"));

                mockMvc.perform(get("/api/v1/owners/search?phone=0800000000")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("ค้นหาเจ้าของสัตว์เลี้ยงด้วยเบอร์โทร: ไม่ส่งเบอร์มา (400 Bad Request)")
        void testGetPetOwnerByPhone_MissingPhone() throws Exception {
                mockMvc.perform(get("/api/v1/owners/search")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("สร้างข้อมูลเจ้าของสัตว์เลี้ยง: สำเร็จ")
        void testCreatePetOwner_Success() throws Exception {
                given(petOwnerService.createPetOwner(any(PetOwnerRequestDTO.class))).willReturn(responseDTO);

                mockMvc.perform(post("/api/v1/owners")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.ownerId").value(1))
                                .andExpect(jsonPath("$.firstName").value("John"))
                                .andExpect(jsonPath("$.email").value("test@example.com"));

                verify(petOwnerService, times(1)).createPetOwner(any(PetOwnerRequestDTO.class));
        }

        @Test
        @DisplayName("สร้างข้อมูลเจ้าของสัตว์เลี้ยง: Validation ไม่ผ่าน (400 Bad Request)")
        void testCreatePetOwner_InvalidEmail() throws Exception {
                requestDTO.setEmail("invalid-email");

                mockMvc.perform(post("/api/v1/owners")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isBadRequest());

                verify(petOwnerService, times(0)).createPetOwner(any(PetOwnerRequestDTO.class));
        }

        @Test
        @DisplayName("สร้างข้อมูลเจ้าของสัตว์เลี้ยง: อีเมลซ้ำ (409 Conflict)")
        void testCreatePetOwner_DuplicateEmailCaseSensitive() throws Exception {
                requestDTO.setEmail("ABC@gmail.com");

                given(petOwnerService.createPetOwner(any(PetOwnerRequestDTO.class)))
                                .willThrow(new DuplicateResourceException("อีเมลนี้ถูกใช้งานแล้ว"));

                mockMvc.perform(post("/api/v1/owners")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isConflict()); // 409 ข้อมูลซ้ำ

                verify(petOwnerService, times(1)).createPetOwner(any(PetOwnerRequestDTO.class));
        }

        @Test
        @DisplayName("แก้ไขข้อมูลเจ้าของสัตว์เลี้ยง: สำเร็จ")
        void testUpdatePetOwner_Success() throws Exception {
                given(petOwnerService.updatePetOwner(eq(1L), any(PetOwnerRequestDTO.class))).willReturn(responseDTO);

                mockMvc.perform(put("/api/v1/owners/1").session(staffSession)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.ownerId").value(1))
                                .andExpect(jsonPath("$.firstName").value("John"));

                verify(petOwnerService, times(1)).updatePetOwner(eq(1L), any(PetOwnerRequestDTO.class));
        }

        @Test
        @DisplayName("แก้ไขข้อมูลเจ้าของสัตว์เลี้ยง: ไม่พบ Id (404 Not Found)")
        void testUpdatePetOwner_NotFound() throws Exception {
                given(petOwnerService.updatePetOwner(eq(99L), any(PetOwnerRequestDTO.class)))
                                .willThrow(new ResourceNotFoundException("ไม่พบเจ้าของสัตว์เลี้ยงรหัส 99"));

                mockMvc.perform(put("/api/v1/owners/99").session(staffSession)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isNotFound());

                verify(petOwnerService, times(1)).updatePetOwner(eq(99L), any(PetOwnerRequestDTO.class));
        }

        @Test
        @DisplayName("แก้ไขข้อมูลเจ้าของสัตว์เลี้ยง: อีเมลซ้ำ (409 Conflict)")
        void testUpdatePetOwner_DuplicateEmailCaseSensitive() throws Exception {
                requestDTO.setEmail("ABC@gmail.com");

                given(petOwnerService.updatePetOwner(eq(1L), any(PetOwnerRequestDTO.class)))
                                .willThrow(new DuplicateResourceException("อีเมลนี้ถูกใช้งานแล้ว"));

                mockMvc.perform(put("/api/v1/owners/1").session(staffSession)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isConflict()); // 409 ข้อมูลซ้ำ

                verify(petOwnerService, times(1)).updatePetOwner(eq(1L), any(PetOwnerRequestDTO.class));
        }

        @Test
        @DisplayName("ลบข้อมูลเจ้าของสัตว์เลี้ยง: สำเร็จ (204 No Content)")
        void testDeletePetOwner_Success() throws Exception {
                mockMvc.perform(delete("/api/v1/owners/1").session(staffSession))
                                .andExpect(status().isNoContent());

                verify(petOwnerService, times(1)).deletePetOwner(1L);
        }

        @Test
        @DisplayName("ลบข้อมูลเจ้าของสัตว์เลี้ยง: ไม่พบ Id (404 Not Found)")
        void testDeletePetOwner_NotFound() throws Exception {
                doThrow(new ResourceNotFoundException("ไม่พบเจ้าของสัตว์เลี้ยงรหัส 99"))
                                .when(petOwnerService).deletePetOwner(99L);

                mockMvc.perform(delete("/api/v1/owners/99").session(staffSession))
                                .andExpect(status().isNotFound());

                verify(petOwnerService, times(1)).deletePetOwner(99L);
        }

        @Test
        @DisplayName("ยังไม่ใส่รหัสเจ้าหน้าที่: ดึงข้อมูลทั้งหมดไม่ได้ (403 Forbidden)")
        void testGetAllPetOwners_NotStaff() throws Exception {
                mockMvc.perform(get("/api/v1/owners"))
                                .andExpect(status().isForbidden());

                verify(petOwnerService, never()).getAllPetOwners(any(Pageable.class));
        }

        @Test
        @DisplayName("ยังไม่ใส่รหัสเจ้าหน้าที่: ดูข้อมูลตาม Id ไม่ได้ (403 Forbidden)")
        void testGetPetOwnerById_NotStaff() throws Exception {
                mockMvc.perform(get("/api/v1/owners/1"))
                                .andExpect(status().isForbidden());

                verify(petOwnerService, never()).getPetOwnerById(1L);
        }

        @Test
        @DisplayName("ยังไม่ใส่รหัสเจ้าหน้าที่: แก้ไขข้อมูลไม่ได้ (403 Forbidden)")
        void testUpdatePetOwner_NotStaff() throws Exception {
                mockMvc.perform(put("/api/v1/owners/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                                .andExpect(status().isForbidden());

                verify(petOwnerService, never()).updatePetOwner(eq(1L), any(PetOwnerRequestDTO.class));
        }

        @Test
        @DisplayName("ยังไม่ใส่รหัสเจ้าหน้าที่: ลบข้อมูลไม่ได้ (403 Forbidden)")
        void testDeletePetOwner_NotStaff() throws Exception {
                mockMvc.perform(delete("/api/v1/owners/1"))
                                .andExpect(status().isForbidden());

                verify(petOwnerService, never()).deletePetOwner(1L);
        }
}