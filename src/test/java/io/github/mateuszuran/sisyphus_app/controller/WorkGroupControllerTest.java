package io.github.mateuszuran.sisyphus_app.controller;

import io.github.mateuszuran.sisyphus_app.dto.WorkGroupDTO;
import io.github.mateuszuran.sisyphus_app.model.WorkGroup;
import io.github.mateuszuran.sisyphus_app.repository.WorkGroupRepository;
import io.github.mateuszuran.sisyphus_app.service.WorkGroupServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkGroupController.class)
@AutoConfigureMockMvc
class WorkGroupControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    WorkGroupRepository repository;
    @MockBean
    WorkGroupServiceImpl service;

    List<WorkGroupDTO> groupDTOS = new ArrayList<>();

    @BeforeEach
    void setUp() {
        groupDTOS = List.of(
                WorkGroupDTO.builder().creationTime("date1").build(),
                WorkGroupDTO.builder().creationTime("date2").build(),
                WorkGroupDTO.builder().creationTime("date2").build());
    }

    @Test
    void givenNothing_whenGet_thenReturnAllWorkGroups() throws Exception {
        //given + when
        when(service.getAllMappedWorkGroups()).thenReturn(groupDTOS);

        //then
        mockMvc.perform(get("/group/all"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].creationTime").value("date1"));
    }

    @Test
    void givenPdfFile_whenAdd_thenCreateWorkGroupObject() throws Exception {
        //given
        MockMultipartFile file = new MockMultipartFile(
                "cv",
                "cv.pdf", MediaType.APPLICATION_PDF.getType(),
                "CV-PDF".getBytes());

        //when + then
        mockMvc.perform(multipart("/group/create").file(file))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void givenWorkGroupId_whenGet_thenReturnSingleWorkGroup() throws Exception {
        //given
        when(service.getMappedSingleWorkGroup(anyString())).thenReturn(groupDTOS.get(0));

        //when + then
        mockMvc.perform(get("/group/single/{workGroupId}", "1234"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creationTime").value("date1"));
    }

    @Test
    void givenWorkGroupId_whenDelete_thenReturnStatus() throws Exception {
        //when + then
        mockMvc.perform(delete("/group/delete/{workGroupId}", "1234"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}