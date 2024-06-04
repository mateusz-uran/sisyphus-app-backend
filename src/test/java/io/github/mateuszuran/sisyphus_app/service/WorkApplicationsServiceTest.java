package io.github.mateuszuran.sisyphus_app.service;

import io.github.mateuszuran.sisyphus_app.model.ApplicationStatus;
import io.github.mateuszuran.sisyphus_app.model.WorkApplications;
import io.github.mateuszuran.sisyphus_app.model.WorkGroup;
import io.github.mateuszuran.sisyphus_app.repository.WorkApplicationsRepository;
import io.github.mateuszuran.sisyphus_app.repository.WorkGroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class WorkApplicationsServiceTest {

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

        //when
        serviceImpl.createWorkApplication(List.of(application1), workGroupId);

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

        //when
        serviceImpl.deleteWorkApplication(workApplicationId);

        //then
        verify(repository).deleteById(workApplicationId);
    }

    @Test
    public void givenApplicationIdAndStatus_whenUpdate_thenReturnUpdatedApplication() {
        //given
        String workApplicationId = "1234";
        var newStatus = ApplicationStatus.DENIED;
        WorkApplications work = WorkApplications.builder().workUrl("work1").status(ApplicationStatus.IN_PROGRESS).build();
        when(repository.findById(workApplicationId)).thenReturn(Optional.of(work));

        WorkApplications updatedWork = WorkApplications.builder().workUrl("work1").status(newStatus).build();
        when(repository.save(any(WorkApplications.class))).thenReturn(updatedWork);

        //when
        var result = serviceImpl.updateApplicationStatus(newStatus, workApplicationId);

        //then
        assertThat(result).isEqualTo(updatedWork);
    }

    @Test
    public void givenApplicationIdAndStatus_whenNotFound_thenThrow() {
        //given
        String workApplicationId = "1234";
        //when
        assertThrows(IllegalStateException.class, () -> serviceImpl.updateApplicationStatus(ApplicationStatus.SEND, null));
        verify(repository, never()).findById(workApplicationId);
        verify(repository, never()).save(any());
    }
}
