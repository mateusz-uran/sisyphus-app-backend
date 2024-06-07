package io.github.mateuszuran.sisyphus_app.service;

import io.github.mateuszuran.sisyphus_app.model.ApplicationStatus;
import io.github.mateuszuran.sisyphus_app.model.WorkApplications;
import io.github.mateuszuran.sisyphus_app.model.WorkGroup;
import io.github.mateuszuran.sisyphus_app.repository.WorkGroupRepository;
import io.github.mateuszuran.sisyphus_app.util.TimeUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkGroupServiceTest {

    @Mock
    TimeUtil util;
    @Mock
    WorkGroupRepository repository;
    @InjectMocks
    WorkGroupServiceImpl serviceImpl;


    @Test
    public void givenCvUrl_whenCreateNewWorkGroup_thenCreatePlainWorkGroup() {
        //given
        String filename = "test-cv.pdf";
        byte[] fileContent = "Mock file content".getBytes();
        MockMultipartFile mockFile = new MockMultipartFile("file", filename, "application/pdf", fileContent);

        var time = util.formatCreationTime();
        //when
        serviceImpl.createNewWorkGroup(mockFile);
        //then
        verify(repository).save(assertArg(arg -> {
            byte[] cvUrlBytes = arg.getCv_url().getData();
            var creationTime = arg.getCreationTime();
            assertArrayEquals(cvUrlBytes, fileContent);
            assertEquals(creationTime, time);
        }));
    }

    @Test
    public void givenCvUrl_whenCreateNewWorkGroup_thenThrowException() {
        String filename = "test-cv.pdf";
        byte[] fileContent = "Mock file content".getBytes();
        MockMultipartFile mockFile = new MockMultipartFile("file", filename, "application/pdf", fileContent);
        //when
        serviceImpl.createNewWorkGroup(mockFile);
        //then
        doThrow(new RuntimeException("Failed to read file")).when(repository).save(any(WorkGroup.class));

        assertThrows(RuntimeException.class, () -> serviceImpl.createNewWorkGroup(mockFile));
    }

    @Test
    public void givenWorkGroupId_whenGet_thenReturnWorkGroupObject() {
        //given
        String workGroupId = "12345";
        WorkGroup group = WorkGroup.builder()
                .id(workGroupId).build();
        when(repository.findById(workGroupId)).thenReturn(Optional.of(group));
        //when
        var result = serviceImpl.getWorkGroup(workGroupId);
        //then
        assertNotNull(result);
        assertEquals(result.getId(), workGroupId);
    }

    @Test
    public void givenWorkApplicationsAndWorkGroupId_whenUpdate_thenAddWorkGroupApplications() {
        //given
        String workGroupId = "123";
        WorkGroup group = WorkGroup.builder().id(workGroupId).workApplications(new ArrayList<>()).build();
        when(repository.findById(workGroupId)).thenReturn(Optional.of(group));

        WorkApplications application1 = WorkApplications.builder().workUrl("work1").build();
        WorkApplications application2 = WorkApplications.builder().workUrl("work2").build();
        WorkApplications application3 = WorkApplications.builder().workUrl("work3").build();
        var applicationsList = List.of(application1, application2, application3);

        int applicationSize = applicationsList.size();
        when(repository.save(group)).thenReturn(WorkGroup.builder().id(workGroupId).send(applicationSize).inProgress(applicationSize).workApplications(applicationsList).build());

        //when
        var updatedGroup = serviceImpl.updateWorkGroupWithWorkApplications(applicationsList, workGroupId);

        //then
        assertNotNull(updatedGroup.getWorkApplications());
        assertThat(updatedGroup.getWorkApplications())
                .hasSize(3)
                .extracting(WorkApplications::getWorkUrl)
                .containsExactlyInAnyOrder("work1", "work2", "work3");
        assertThat(updatedGroup.getSend()).isEqualTo(applicationsList.size());
        assertThat(updatedGroup.getInProgress()).isEqualTo(applicationsList.size());

    }

    @Test
    public void givenWorkApplicationEmptyList_whenUpdate_thenThrow() {
        //given
        String workGroupId = "123";

        //when + then
        assertThrows(IllegalStateException.class, () -> serviceImpl.updateWorkGroupWithWorkApplications(null, workGroupId));
        verify(repository, never()).findById(workGroupId);
        verify(repository, never()).save(any());

    }

    @Test
    public void givenNothing_whenGet_thenReturnListOfWorKGroups() {
        //given
        WorkGroup group1 = WorkGroup.builder().creationTime("date1").build();
        WorkGroup group2 = WorkGroup.builder().creationTime("date2").build();
        WorkGroup group3 = WorkGroup.builder().creationTime("date3").build();
        var groupList = List.of(group1, group2, group3);
        when(repository.findAll()).thenReturn(groupList);
        //when
        var returnedList = serviceImpl.getAllGroups();
        //then
        assertThat(returnedList)
                .hasSize(groupList.size())
                .extracting(WorkGroup::getCreationTime)
                .containsExactly("date1", "date2", "date3");

    }

    @Test
    public void givenWorkGroupId_whenExists_thenDeleteWorkGroup() {
        //given
        String workGroupId = "123";
        WorkGroup groupToDelete = WorkGroup.builder().build();
        when(repository.findById(workGroupId)).thenReturn(Optional.of(groupToDelete));

        //when
        serviceImpl.deleteSingleGroup(workGroupId);

        //then
        verify(repository).delete(any(WorkGroup.class));
    }

    @Test
    public void givenWorkGroupId_whenGetAllApplications_thenReturnListOf() {
        //given
        String workGroupId = "123";
        WorkGroup group = WorkGroup.builder().id(workGroupId).workApplications(new ArrayList<>()).build();

        WorkApplications application1 = WorkApplications.builder().workUrl("work1").build();
        WorkApplications application2 = WorkApplications.builder().workUrl("work2").build();
        WorkApplications application3 = WorkApplications.builder().workUrl("work3").build();
        var applicationsList = List.of(application1, application2, application3);
        group.getWorkApplications().addAll(applicationsList);
        when(repository.findById(workGroupId)).thenReturn(Optional.of(group));

        //when
        var workApplications = serviceImpl.getAllWorkApplicationsFromWorkGroup(workGroupId);

        //then
        assertThat(workApplications)
                .hasSize(applicationsList.size())
                .extracting(WorkApplications::getWorkUrl)
                .containsExactly("work1", "work2", "work3");
    }

    @Test
    void givenWorkApplicationAndStatus_whenChangeDeniedToOther_thenDecrementCounter() {
        //given
        WorkApplications application = WorkApplications.builder().id("1234").status(ApplicationStatus.DENIED).build();
        WorkGroup group = WorkGroup.builder().send(5).inProgress(3).denied(2).workApplications(List.of(application)).build();

        when(repository.findAll()).thenReturn(List.of(group));

        ArgumentCaptor<WorkGroup> groupCaptor = ArgumentCaptor.forClass(WorkGroup.class);

        // when
        serviceImpl.updateWorkGroupCounters(application, ApplicationStatus.IN_PROGRESS.name());

        // then
        verify(repository).findAll();
        verify(repository).save(groupCaptor.capture());

        WorkGroup capturedGroup = groupCaptor.getValue();
        assertNotNull(capturedGroup);

        assertEquals(1, capturedGroup.getDenied());
    }

    @Test
    void givenWorkApplicationAndStatus_whenChangeOtherToDenied_thenIncrementCounter() {
        //given
        WorkApplications application = WorkApplications.builder().id("1234").status(ApplicationStatus.SEND).build();
        WorkGroup group = WorkGroup.builder().send(5).inProgress(3).denied(2).workApplications(List.of(application)).build();

        when(repository.findAll()).thenReturn(List.of(group));

        ArgumentCaptor<WorkGroup> groupCaptor = ArgumentCaptor.forClass(WorkGroup.class);

        // when
        serviceImpl.updateWorkGroupCounters(application, ApplicationStatus.DENIED.name());

        // then
        verify(repository).findAll();
        verify(repository).save(groupCaptor.capture());

        WorkGroup capturedGroup = groupCaptor.getValue();
        assertNotNull(capturedGroup);

        assertEquals(3, capturedGroup.getDenied());
    }

    @Test
    void givenWorkApplicationAndStatus_whenWorkGroupNotFound_thenThrowException() {
        //given
        WorkApplications application = WorkApplications.builder().status(ApplicationStatus.SEND).build();

        //when + then
        assertThrows(IllegalArgumentException.class, () -> serviceImpl.updateWorkGroupCounters(application, ApplicationStatus.DENIED.name()));
        verify(repository, never()).save(any());
    }
}
