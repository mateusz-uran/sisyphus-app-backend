package io.github.mateuszuran.sisyphus_app.unit.controller;

import io.github.mateuszuran.sisyphus_app.controller.WorkApplicationsController;
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
    void givenWorkGroupId_whenGetAllApplications_thenReturnListOfWorkApplications() throws Exception {
        //given
        String workGroupId = "1234";
        WorkApplications applications1 = WorkApplications.builder().workUrl("url1").status(ApplicationStatus.SEND).build();
        WorkApplications applications2 = WorkApplications.builder().workUrl("url2").status(ApplicationStatus.DENIED).build();
        WorkApplications applications3 = WorkApplications.builder().workUrl("url3").status(ApplicationStatus.IN_PROGRESS).build();
        List<WorkApplications> expectedList = List.of(applications1, applications2, applications3);

        //when
        when(serviceImpl.getAllApplicationsByWorkGroupId(workGroupId)).thenReturn(expectedList);

        mockMvc.perform(get("/applications/all/" + workGroupId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].workUrl").value("url1"))
                .andExpect(jsonPath("$.[0].status").value("SEND"));
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
        String applicationId = "1234";
        String status = "IN_PROGRESS";
        var newStatus = ApplicationStatus.IN_PROGRESS;
        var updatedWork = works.get(0);
        updatedWork.setStatus(newStatus);
        when(serviceImpl.updateApplicationStatus(applicationId, status)).thenReturn(updatedWork);

        //when + then
        mockMvc.perform(patch("/applications/update/{applicationId}/{status}", applicationId, status))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(newStatus.name()));
    }
}