package com.example.petclinic.controller.web;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.petclinic.dto.response.PetOwnerResponseDTO;
import com.example.petclinic.dto.response.PetResponseDTO;
import com.example.petclinic.exception.DuplicateResourceException;
import com.example.petclinic.service.PetOwnerService;
import com.example.petclinic.service.PetService;
import com.example.petclinic.service.StaffPasscodeService;

// ทดสอบการแบ่งสิทธิ์ 2 ฝั่ง: ลูกค้า (ค้นด้วยเบอร์ตัวเอง) และ เจ้าหน้าที่ (รหัส 8 หลัก)
@WebMvcTest(PetOwnerWebController.class)
class PetOwnerWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PetOwnerService petOwnerService;

    @MockitoBean
    private StaffPasscodeService staffPasscodeService;

    // แฟ้มเจ้าของดึงรายการสัตว์เลี้ยงจากโมดูล Pet
    @MockitoBean
    private PetService petService;

    private PetOwnerResponseDTO owner;

    @BeforeEach
    void setUp() {
        owner = new PetOwnerResponseDTO();
        owner.setOwnerId(1L);
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setEmail("test@example.com");
        owner.setPhone("0876543210");
        owner.setEmergencyContactPhone("0987654321");
    }

    @Test
    @DisplayName("เจ้าหน้าที่: ยังไม่ใส่รหัส -> แสดงหน้ากรอกรหัส")
    void testStaffPage_WithoutPasscode() throws Exception {
        mockMvc.perform(get("/owners/staff"))
                .andExpect(status().isOk())
                .andExpect(view().name("petowner/staff-lock"));

        verify(petOwnerService, never()).getAllPetOwners(any(Pageable.class));
    }

    @Test
    @DisplayName("เจ้าหน้าที่: ใส่รหัสผิด -> ยังเข้าหน้ารายชื่อไม่ได้")
    void testUnlock_WrongPasscode() throws Exception {
        MockHttpSession session = new MockHttpSession();

        given(staffPasscodeService.verify(anyString(), anyString()))
                .willReturn(new StaffPasscodeService.Result(false, false, 0));

        mockMvc.perform(post("/owners/staff/unlock").param("passcode", "11111111").session(session))
                .andExpect(redirectedUrl("/owners/staff"));

        assertNull(session.getAttribute("isStaff"));
    }

    @Test
    @DisplayName("เจ้าหน้าที่: ใส่รหัสถูก -> เห็นรายชื่อทั้งหมด")
    void testUnlock_CorrectPasscode() throws Exception {
        MockHttpSession session = new MockHttpSession();
        given(petOwnerService.getAllPetOwners(any(Pageable.class))).willReturn(new PageImpl<>(List.of(owner)));

        given(staffPasscodeService.verify(anyString(), anyString()))
                .willReturn(new StaffPasscodeService.Result(true, false, 0));

        mockMvc.perform(post("/owners/staff/unlock").param("passcode", "12345678").session(session))
                .andExpect(redirectedUrl("/owners/staff"));
        assertEquals(true, session.getAttribute("isStaff"));

        mockMvc.perform(get("/owners/staff").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("petowner/list"));
    }

    @Test
    @DisplayName("เจ้าหน้าที่: ออกจากระบบ -> กลับหน้ากรอกรหัส")
    void testLogoutStaff() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("isStaff", true);

        mockMvc.perform(post("/owners/staff/logout").session(session))
                .andExpect(redirectedUrl("/owners/staff"));

        assertNull(session.getAttribute("isStaff"));
    }

    @Test
    @DisplayName("ลูกค้า: ยังไม่ค้นด้วยเบอร์ -> เปิดแฟ้มไม่ได้")
    void testCustomer_DetailWithoutSearch() throws Exception {
        mockMvc.perform(get("/owners/1"))
                .andExpect(redirectedUrl("/owners"));

        verify(petOwnerService, never()).getPetOwnerById(anyLong());
    }

    @Test
    @DisplayName("ลูกค้า: ค้นเจอเบอร์ตัวเอง -> เปิดแฟ้มตัวเองได้ แต่เปิดแฟ้มคนอื่นไม่ได้")
    void testCustomer_OwnFileOnly() throws Exception {
        MockHttpSession session = new MockHttpSession();
        given(petOwnerService.getPetOwnerByPhone("0876543210")).willReturn(owner);
        given(petOwnerService.getPetOwnerById(1L)).willReturn(owner);

        mockMvc.perform(get("/owners").param("phone", "0876543210").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("petowner/search"));

        mockMvc.perform(get("/owners/1").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("petowner/detail"));

        mockMvc.perform(get("/owners/2").session(session))
                .andExpect(redirectedUrl("/owners"));
    }

    @Test
    @DisplayName("ลูกค้า: แก้ไขหรือลบแฟ้มไม่ได้ รวมถึงแฟ้มของตัวเอง")
    void testCustomer_CannotEditOrDelete() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("myOwnerId", 1L);

        mockMvc.perform(get("/owners/1/edit").session(session))
                .andExpect(redirectedUrl("/owners/staff"));

        mockMvc.perform(post("/owners/1/delete").session(session))
                .andExpect(redirectedUrl("/owners/staff"));

        verify(petOwnerService, never()).deletePetOwner(1L);
    }

    @Test
    @DisplayName("เจ้าหน้าที่: ลบแฟ้มได้")
    void testStaff_CanDelete() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("isStaff", true);

        mockMvc.perform(post("/owners/1/delete").session(session))
                .andExpect(redirectedUrl("/owners/staff"));

        verify(petOwnerService).deletePetOwner(1L);
    }

    @Test
    @DisplayName("เจ้าหน้าที่: ลบเจ้าของที่ยังมีสัตว์เลี้ยง -> กลับหน้ารายชื่อพร้อมข้อความเตือน")
    void testStaff_DeleteOwnerWithPets() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("isStaff", true);
        doThrow(new DuplicateResourceException("ไม่สามารถลบเจ้าของที่ยังมีสัตว์เลี้ยงในระบบได้"))
                .when(petOwnerService).deletePetOwner(1L);

        mockMvc.perform(post("/owners/1/delete").session(session))
                .andExpect(redirectedUrl("/owners/staff"))
                .andExpect(flash().attribute("errorMessage", "ไม่สามารถลบเจ้าของที่ยังมีสัตว์เลี้ยงในระบบได้"));
    }

    // รายการสัตว์เลี้ยงในแฟ้มเจ้าของ (ข้อมูลจากโมดูล Pet)

    private PetResponseDTO pet(Long id, String name, String species) {
        PetResponseDTO pet = new PetResponseDTO();
        pet.setPetId(id);
        pet.setName(name);
        pet.setSpecies(species);
        pet.setGender("Male");
        pet.setOwnerId(1L);
        return pet;
    }

    @Test
    @DisplayName("แฟ้มเจ้าของ: แสดงสัตว์เลี้ยงจริงของเจ้าของคนนั้น")
    void testDetail_ShowsOwnerPets() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("isStaff", true);
        given(petOwnerService.getPetOwnerById(1L)).willReturn(owner);
        given(petService.getPetsByOwnerId(1L)).willReturn(List.of(pet(10L, "Milo", "Dog"), pet(11L, "Luna", "Cat")));

        mockMvc.perform(get("/owners/1").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pets", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Milo")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("สุนัข")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("(2 ตัว)")));
    }

    @Test
    @DisplayName("แฟ้มเจ้าของ: ยังไม่มีสัตว์ -> ขึ้นกล่องว่าง")
    void testDetail_NoPets_ShowsEmpty() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("isStaff", true);
        given(petOwnerService.getPetOwnerById(1L)).willReturn(owner);
        given(petService.getPetsByOwnerId(1L)).willReturn(List.of());

        mockMvc.perform(get("/owners/1").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ยังไม่มีข้อมูลสัตว์เลี้ยง")));
    }

    @Test
    @DisplayName("ลูกค้าค้นหาเบอร์เจอ: แสดงสัตว์เลี้ยงของตัวเอง")
    void testSearch_ShowsOwnerPets() throws Exception {
        given(petOwnerService.getPetOwnerByPhone("0876543210")).willReturn(owner);
        given(petService.getPetsByOwnerId(1L)).willReturn(List.of(pet(10L, "Milo", "Dog")));

        mockMvc.perform(get("/owners").param("phone", "0876543210"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pets", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Milo")));
    }

    @Test
    @DisplayName("เจ้าหน้าที่: กล่องข้อมูลย่อของคนที่เลือก แสดงสัตว์เลี้ยงของคนนั้น")
    void testStaffList_ShowsSelectedOwnerPets() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("isStaff", true);
        given(petOwnerService.getAllPetOwners(any(Pageable.class))).willReturn(new PageImpl<>(List.of(owner)));
        given(petService.getPetsByOwnerId(1L)).willReturn(List.of(pet(10L, "Milo", "Dog")));

        mockMvc.perform(get("/owners/staff").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pets", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/pets?ownerId=1")));
    }
}