package io.github.mateuszuran.sisyphus_app.unit.service;

import io.github.mateuszuran.sisyphus_app.dto.WorkApplicationDTO;
import io.github.mateuszuran.sisyphus_app.model.ApplicationStatus;
import io.github.mateuszuran.sisyphus_app.model.WorkApplications;
import io.github.mateuszuran.sisyphus_app.repository.WorkApplicationsRepository;
import io.github.mateuszuran.sisyphus_app.service.WorkApplicationsServiceImpl;
import io.github.mateuszuran.sisyphus_app.service.WorkGroupServiceImpl;
import io.github.mateuszuran.sisyphus_app.util.TimeUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkApplicationsServiceTest {

    @Mock
    TimeUtil util;
    @Mock
    WorkGroupServiceImpl groupService;
    @Mock
    WorkApplicationsRepository repository;

    @InjectMocks
    WorkApplicationsServiceImpl serviceImpl;

    @Test
    public void givenWorkApplicationAndWorkGroupId_whenAdd_thenSaveWorkApplication() {
        //given
        String workGroupId = "123";
        WorkApplications application1 = WorkApplications.builder().workUrl("work1").build();
        WorkApplicationDTO applicationDTO = WorkApplicationDTO.builder().workUrl("work1").build();

        //when
        serviceImpl.createWorkApplication(List.of(applicationDTO), workGroupId);

        //then
        verify(repository).saveAll(assertArg(arg -> {
            var savedWorkApplication = arg.iterator().next();
            assertEquals(savedWorkApplication.getWorkUrl(), "work1");
        }));
    }

    @Test
    public void givenWorkApplicationId_whenDelete_thenDoNothing() {
        //given
        String workApplicationId = "1234";
        var workApplication = WorkApplications.builder().build();
        when(repository.findById(workApplicationId)).thenReturn(Optional.of(workApplication));

        //when
        serviceImpl.deleteWorkApplication(workApplicationId);

        //then
        verify(repository).delete(workApplication);
    }

    @Test
    public void givenApplicationIdAndStatus_whenUpdate_thenReturnUpdatedApplication() {
        //given
        String workApplicationId = "1234";
        var newStatus = "DENIED";
        WorkApplications work = WorkApplications.builder().workUrl("work1").status(ApplicationStatus.IN_PROGRESS).build();
        when(repository.findById(workApplicationId)).thenReturn(Optional.of(work));

        WorkApplications updatedWork = WorkApplications.builder().workUrl("work1").status(ApplicationStatus.valueOf(newStatus)).build();
        when(repository.save(any(WorkApplications.class))).thenReturn(updatedWork);

        //when
        var result = serviceImpl.updateApplicationStatus(workApplicationId, newStatus);

        //then
        assertThat(result).isEqualTo(updatedWork);
    }

    @Test
    public void givenApplicationIdAndStatus_whenNotFound_thenThrow() {
        //given
        String workApplicationId = "1234";
        //when
        assertThrows(IllegalArgumentException.class, () -> serviceImpl.updateApplicationStatus("SEND", null));
        verify(repository, never()).findById(workApplicationId);
        verify(repository, never()).save(any());
    }

    @Test
    public void givenWorkApplicationIdAndUrl_whenUpdate_thenReturnUpdatedWorkApplication() {
        //given
        String workId = "1234";
        String newWorkUrl = "new_url";
        WorkApplications oldWork = WorkApplications.builder().workUrl("old_url").build();
        when(repository.findById(workId)).thenReturn(Optional.of(oldWork));
        when(repository.save(any(WorkApplications.class))).thenReturn(WorkApplications.builder().workUrl(newWorkUrl).build());

        //when
        var updatedWork = serviceImpl.updateWorkApplicationUrl(workId, newWorkUrl);

        //then
        assertThat(updatedWork.getWorkUrl()).isEqualTo(newWorkUrl);
    }
}
