package io.github.mateuszuran.sisyphus_app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mateuszuran.sisyphus_app.AbstractIntegrationTest;
import io.github.mateuszuran.sisyphus_app.SisyphusAppApplication;
import io.github.mateuszuran.sisyphus_app.model.ApplicationStatus;
import io.github.mateuszuran.sisyphus_app.model.WorkApplications;
import io.github.mateuszuran.sisyphus_app.model.WorkGroup;
import io.github.mateuszuran.sisyphus_app.repository.WorkApplicationsRepository;
import io.github.mateuszuran.sisyphus_app.repository.WorkGroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = SisyphusAppApplication.class)
@AutoConfigureMockMvc
public class WorkApplicationsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WorkGroupRepository groupRepository;

    @Autowired
    private WorkApplicationsRepository applicationsRepository;

    @BeforeEach
    public void setUp() throws Exception {
        applicationsRepository.deleteAll();
        groupRepository.deleteAll();
    }

    @Test
    void givenListOfWorkApplicationsAndWorkGroupId_whenSave_thenReturnStatusCreated() throws Exception {
        //given
        WorkGroup group = WorkGroup.builder().creationTime("tomorrow").build();
        var savedGroup = groupRepository.save(group);

        WorkApplications work = WorkApplications.builder().workUrl("work_url").build();

        //when
        mockMvc.perform(post("/applications/save/" + savedGroup.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(work))))
                .andExpect(status().isCreated());

        //then
        var groupResult = groupRepository.findById(savedGroup.getId()).orElseThrow();
        Assertions.assertNotNull(groupResult);
        Assertions.assertFalse(groupResult.getWorkApplications().isEmpty());

        var savedWork = groupResult.getWorkApplications()
                .stream()
                .filter(g -> g.getWorkUrl().equalsIgnoreCase(work.getWorkUrl()))
                .findFirst();

        Assertions.assertTrue(savedWork.isPresent());
        Assertions.assertEquals(savedWork.get().getStatus(), ApplicationStatus.SEND);
        Assertions.assertEquals(groupResult.getSend(), 1);
    }

    @Test
    void givenWorkApplication_whenDelete_thenReturnStatus() throws Exception {
        //given
        WorkApplications work1 = WorkApplications.builder().workUrl("work_url1").status(ApplicationStatus.SEND).build();
        WorkApplications work2 = WorkApplications.builder().workUrl("work_url2").status(ApplicationStatus.IN_PROGRESS).build();
        applicationsRepository.saveAll(List.of(work1, work2));

        WorkGroup group = WorkGroup.builder()
                .cv_url(null)
                .creationTime("today")
                .send(15)
                .denied(4)
                .inProgress(12)
                .workApplications(new ArrayList<>())
                .build();
        group.getWorkApplications().addAll(List.of(work1, work2));
        groupRepository.save(group);

        //when
        mockMvc.perform(delete("/applications/delete/" + work1.getId()))
                .andExpect(status().isOk());

        //then
        Assertions.assertNotNull(applicationsRepository.findById(work1.getId()));

        var editedGroup = groupRepository.findById(group.getId()).orElseThrow();
        Assertions.assertEquals(14, editedGroup.getSend());
    }

    @Test
    void givenApplicationIdAndStatus_whenUpdateStatus_thenReturnWorkApplicationAndUpdateWorkGroupCounters() throws Exception {
        //given
        String newStatus = "send";
        WorkApplications work1 = WorkApplications.builder().workUrl("work_url1").status(ApplicationStatus.DENIED).build();
        applicationsRepository.save(work1);

        WorkGroup group = WorkGroup.builder()
                .cv_url(null)
                .creationTime("today")
                .send(15)
                .denied(4)
                .inProgress(12)
                .workApplications(new ArrayList<>())
                .build();
        group.getWorkApplications().add(work1);
        var savedGroup = groupRepository.save(group);

        //when
        mockMvc.perform(patch("/applications/update/" + work1.getId() + "/" + newStatus))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(newStatus.toUpperCase()));

        //then
        var updatedGroup = groupRepository.findById(savedGroup.getId()).orElseThrow();
        Assertions.assertNotNull(updatedGroup);
        Assertions.assertEquals(3, updatedGroup.getDenied());
        Assertions.assertEquals(16, updatedGroup.getSend());
    }
}
