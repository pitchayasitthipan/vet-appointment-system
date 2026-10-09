package com.example.petclinic.controller.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.petclinic.dto.response.PetOwnerResponseDTO;
import com.example.petclinic.service.PetOwnerService;

@WebMvcTest(PetPageController.class)
class PetPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PetOwnerService petOwnerService;

    private PetOwnerResponseDTO owner;

    @BeforeEach
    void setUp() {
        owner = new PetOwnerResponseDTO();
        owner.setOwnerId(1L);
        owner.setFirstName("John");
        owner.setLastName("Doe");
    }

    @Test
    @DisplayName("GET /pets ยังไม่ค้นหาเบอร์ -> กลับไปหน้าค้นหา")
    void petsPage_guest_redirectsToSearch() throws Exception {
        mockMvc.perform(get("/pets"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owners"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("GET /pets ลูกค้าเห็นสัตว์ของตัวเอง แม้ใส่ ownerId คนอื่นใน URL")
    void petsPage_owner_usesSessionOwner() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("myOwnerId", 1L);
        given(petOwnerService.getPetOwnerById(1L)).willReturn(owner);

        mockMvc.perform(get("/pets").param("ownerId", "2").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("pet/list"))
                .andExpect(model().attribute("owner", owner))
                .andExpect(model().attribute("openAddForm", false));
    }

    @Test
    @DisplayName("GET /pets/new เปิดหน้าพร้อมฟอร์มเพิ่มสัตว์")
    void newPetPage_owner_opensAddForm() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("myOwnerId", 1L);
        given(petOwnerService.getPetOwnerById(1L)).willReturn(owner);

        mockMvc.perform(get("/pets/new").param("ownerId", "1").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("openAddForm", true));
    }

    @Test
    @DisplayName("GET /pets เจ้าหน้าที่ไม่ระบุเจ้าของ -> หน้าสัตว์เลี้ยงทั้งหมดในคลินิก")
    void petsPage_staffWithoutOwner_showsAllPets() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("isStaff", true);

        mockMvc.perform(get("/pets").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("pet/list"))
                .andExpect(model().attribute("allPets", true))
                .andExpect(model().attributeDoesNotExist("owner"));
    }

    @Test
    @DisplayName("GET /pets?ownerId= เจ้าหน้าที่ดูสัตว์ของเจ้าของคนที่เลือก")
    void petsPage_staffWithOwner_showsThatOwner() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("isStaff", true);
        given(petOwnerService.getPetOwnerById(1L)).willReturn(owner);

        mockMvc.perform(get("/pets").param("ownerId", "1").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("owner", owner))
                .andExpect(model().attribute("allPets", false));
    }

    @Test
    @DisplayName("GET /pets/new?returnTo=appointment เพิ่มเสร็จแล้วกลับไปหน้าจองนัด")
    void newPetPage_fromAppointment_setsReturnFlag() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("myOwnerId", 1L);
        given(petOwnerService.getPetOwnerById(1L)).willReturn(owner);

        mockMvc.perform(get("/pets/new").param("ownerId", "1").param("returnTo", "appointment").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("returnToAppointment", true))
                .andExpect(model().attribute("openAddForm", true));
    }
}