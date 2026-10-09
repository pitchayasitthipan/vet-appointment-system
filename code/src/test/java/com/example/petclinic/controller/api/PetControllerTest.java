package com.example.petclinic.controller.api;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
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

import com.example.petclinic.dto.request.PetRequestDTO;
import com.example.petclinic.dto.response.PetResponseDTO;
import com.example.petclinic.exception.ResourceNotFoundException;
import com.example.petclinic.service.PetService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(PetController.class)
class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PetService petService;

    @Autowired
    private ObjectMapper objectMapper;

    private PetRequestDTO requestDTO;
    private PetResponseDTO responseDTO;

    // session ของเจ้าหน้าที่ / เจ้าของคนที่ 1 / คนที่ยังไม่ค้นหาเบอร์
    private MockHttpSession staffSession;
    private MockHttpSession ownerSession;
    private MockHttpSession guestSession;

    @BeforeEach
    void setUp() {
        staffSession = new MockHttpSession();
        staffSession.setAttribute("isStaff", true);

        ownerSession = new MockHttpSession();
        ownerSession.setAttribute("myOwnerId", 1L);

        guestSession = new MockHttpSession();

        requestDTO = new PetRequestDTO();
        requestDTO.setName("Milo");
        requestDTO.setSpecies("Dog");
        requestDTO.setOwnerId(1L);

        responseDTO = new PetResponseDTO();
        responseDTO.setPetId(10L);
        responseDTO.setName("Milo");
        responseDTO.setSpecies("Dog");
        responseDTO.setOwnerId(1L);
    }

    @Test
    @DisplayName("GET /api/v1/pets/{id}: เจ้าของดูสัตว์ของตัวเองได้ 200")
    void getPetById_owner_returns200() throws Exception {
        given(petService.getPetById(10L)).willReturn(responseDTO);

        mockMvc.perform(get("/api/v1/pets/10").session(ownerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Milo"));
    }

    @Test
    @DisplayName("GET /api/v1/pets/{id}: ไม่ใช่เจ้าของ ได้ 403")
    void getPetById_otherOwner_returns403() throws Exception {
        given(petService.getPetById(10L)).willReturn(responseDTO);
        MockHttpSession otherOwner = new MockHttpSession();
        otherOwner.setAttribute("myOwnerId", 2L);

        mockMvc.perform(get("/api/v1/pets/10").session(otherOwner))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/pets/{id}: ไม่พบสัตว์ ได้ 404")
    void getPetById_notFound_returns404() throws Exception {
        given(petService.getPetById(99L)).willThrow(new ResourceNotFoundException("ไม่พบสัตว์เลี้ยงรหัส 99"));

        mockMvc.perform(get("/api/v1/pets/99").session(staffSession))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/pets/owner/{ownerId}: ยังไม่ค้นหาเบอร์ ได้ 403")
    void getPetsByOwner_guest_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/pets/owner/1").session(guestSession))
                .andExpect(status().isForbidden());

        verify(petService, never()).getPetsByOwnerId(any());
    }

    @Test
    @DisplayName("GET /api/v1/pets/owner/{ownerId}: เจ้าของดูรายการของตัวเองได้ 200")
    void getPetsByOwner_owner_returns200() throws Exception {
        given(petService.getPetsByOwnerId(1L)).willReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/v1/pets/owner/1").session(ownerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].petId").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/pets: เจ้าหน้าที่ดูทั้งหมดแบบแบ่งหน้าได้ 200")
    void getAllPets_staff_returns200() throws Exception {
        given(petService.getAllPets(any(Pageable.class))).willReturn(new PageImpl<>(List.of(responseDTO)));

        mockMvc.perform(get("/api/v1/pets").session(staffSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Milo"));
    }

    @Test
    @DisplayName("GET /api/v1/pets: ลูกค้าดูทั้งหมดไม่ได้ 403")
    void getAllPets_owner_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/pets").session(ownerSession))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/pets: เจ้าของเพิ่มสัตว์ของตัวเองได้ 201")
    void createPet_owner_returns201() throws Exception {
        given(petService.createPet(any(PetRequestDTO.class))).willReturn(responseDTO);

        mockMvc.perform(post("/api/v1/pets").session(ownerSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.petId").value(10));
    }

    @Test
    @DisplayName("POST /api/v1/pets: เพิ่มสัตว์ให้เจ้าของคนอื่น ได้ 403")
    void createPet_forOtherOwner_returns403() throws Exception {
        requestDTO.setOwnerId(2L);

        mockMvc.perform(post("/api/v1/pets").session(ownerSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());

        verify(petService, never()).createPet(any());
    }

    @Test
    @DisplayName("POST /api/v1/pets: ชื่อยาวเกิน 100 ตัวอักษร ได้ 400")
    void createPet_nameTooLong_returns400() throws Exception {
        requestDTO.setName("a".repeat(101));

        mockMvc.perform(post("/api/v1/pets").session(ownerSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/pets/{id}: ไม่ใช่เจ้าของ แก้ไขไม่ได้ 403")
    void updatePet_guest_returns403() throws Exception {
        given(petService.getPetById(10L)).willReturn(responseDTO);

        mockMvc.perform(put("/api/v1/pets/10").session(guestSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());

        verify(petService, never()).updatePet(any(), any());
    }

    @Test
    @DisplayName("PUT /api/v1/pets/{id}: เจ้าหน้าที่แก้ไขได้ 200")
    void updatePet_staff_returns200() throws Exception {
        given(petService.getPetById(10L)).willReturn(responseDTO);
        given(petService.updatePet(eq(10L), any(PetRequestDTO.class))).willReturn(responseDTO);

        mockMvc.perform(put("/api/v1/pets/10").session(staffSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/pets/{id}: ไม่ใช่เจ้าของ ลบไม่ได้ 403")
    void deletePet_guest_returns403() throws Exception {
        given(petService.getPetById(10L)).willReturn(responseDTO);

        mockMvc.perform(delete("/api/v1/pets/10").session(guestSession))
                .andExpect(status().isForbidden());

        verify(petService, never()).deletePet(any());
    }

    @Test
    @DisplayName("DELETE /api/v1/pets/{id}: เจ้าของลบสัตว์ของตัวเองได้ 200")
    void deletePet_owner_returns200() throws Exception {
        given(petService.getPetById(10L)).willReturn(responseDTO);

        mockMvc.perform(delete("/api/v1/pets/10").session(ownerSession))
                .andExpect(status().isOk());

        verify(petService).deletePet(10L);
    }
}