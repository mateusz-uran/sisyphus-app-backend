package io.github.mateuszuran.sisyphus_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mateuszuran.sisyphus_app.model.ApplicationStatus;
import io.github.mateuszuran.sisyphus_app.model.WorkApplications;
import io.github.mateuszuran.sisyphus_app.repository.WorkApplicationsRepository;
import io.github.mateuszuran.sisyphus_app.service.WorkApplicationsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkApplicationsController.class)
@AutoConfigureMockMvc
class WorkApplicationsControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    WorkApplicationsServiceImpl serviceImpl;

    @MockBean
    WorkApplicationsRepository repository;


    List<WorkApplications> works = new ArrayList<>();

    @BeforeEach
    void setUp() {
        works = List.of(
                WorkApplications.builder().workUrl("url1").build(),
                WorkApplications.builder().workUrl("url2").build(),
                WorkApplications.builder().workUrl("url3").build());
    }

    @Test
    void givenWorkGroupIdAndApplicationsList_whenPost_thenCreateNewApplications() throws Exception {
        //given
        var workJson = "[{\"workUrl\":\"url1\"}]";

        //when + then
        mockMvc.perform(post("/applications/save/1234")
                .contentType(MediaType.APPLICATION_JSON)
                .content(workJson))
                .andExpect(status().isCreated());
    }

    @Test
    void givenWorkGroupId_whenDelete_thenReturnStatus() throws Exception {
        mockMvc.perform(delete("/applications/delete/1234"))
                .andExpect(status().isOk());
    }

    @Test
    void givenNewStatusAndApplicationId_whenUpdate_thenReturnWorkApplication() throws Exception {
        //given
        var newStatus = ApplicationStatus.IN_PROGRESS;
        var updatedWork = works.get(0);
        updatedWork.setStatus(newStatus);
        when(serviceImpl.updateApplicationStatus("1234", "IN_PROGRESS")).thenReturn(updatedWork);

        //when + then
        mockMvc.perform(patch("/applications/update/1234")
                .content("IN_PROGRESS"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(newStatus.name()));
    }
}