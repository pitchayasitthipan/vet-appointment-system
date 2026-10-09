package com.example.petclinic.controller.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:appointmentviews;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class AppointmentViewControllerTest {
    @Autowired MockMvc mvc;
    @Test void usesSharedStylesAndLatestStaffNavbarOnBothAppointmentPages() throws Exception {
        MockHttpSession staff = new MockHttpSession(); staff.setAttribute("isStaff", true);
        for (String url : new String[]{"/appointments", "/appointments/new"}) {
            mvc.perform(get(url).session(staff)).andExpect(status().isOk())
                .andExpect(content().string(containsString("/css/pawcare.css")))
                .andExpect(content().string(containsString("/css/icons.css")))
                .andExpect(content().string(containsString("href=\"/medical-records\"")));
            mvc.perform(get(url)).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("href=\"/medical-records\""))));
        }
    }
    @Test void bookingRendersSharedNavbarAndKeepsOwnerQueryWithoutRedirect() throws Exception {
        mvc.perform(get("/appointments/new?ownerId=1")).andExpect(status().isOk())
            .andExpect(view().name("appointment/create"))
            .andExpect(content().string(containsString("href=\"/owners\"")))
            .andExpect(content().string(containsString("href=\"/owners/staff\"")))
            .andExpect(content().string(not(containsString("th:replace"))));
    }
    @Test void listAndOldLinksRenderTheSameTemplate() throws Exception {
        for (String url : new String[]{"/appointments?ownerId=1", "/appointments.html?ownerId=1"}) {
            mvc.perform(get(url)).andExpect(status().isOk()).andExpect(view().name("appointment/list"))
                .andExpect(content().string(containsString("id=\"staff-tools\"")));
        }
        mvc.perform(get("/appointment-create.html?ownerId=1"))
            .andExpect(status().isOk()).andExpect(view().name("appointment/create"));
    }
}
