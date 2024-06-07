package io.github.mateuszuran.sisyphus_app.service;

import io.github.mateuszuran.sisyphus_app.model.ApplicationStatus;
import io.github.mateuszuran.sisyphus_app.model.WorkApplications;
import io.github.mateuszuran.sisyphus_app.repository.WorkApplicationsRepository;
import io.github.mateuszuran.sisyphus_app.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkApplicationsServiceImpl implements WorkApplicationsService {

    private final WorkApplicationsRepository repository;
    private final WorkGroupServiceImpl groupServiceImpl;
    private final TimeUtil timeUtil;

    @Override
    public void createWorkApplication(List<WorkApplications> applications, String workGroupId) {
        String creationTime = timeUtil.formatCreationTime();
        var workApplicationList = applications
                .stream()
                .map(work -> WorkApplications.builder()
                        .workUrl(work.getWorkUrl())
                        .appliedDate(creationTime)
                        .status(ApplicationStatus.SEND)
                        .build())
                .toList();

        repository.saveAll(workApplicationList);
        groupServiceImpl.updateWorkGroupWithWorkApplications(workApplicationList, workGroupId);
    }


    @Override
    public void deleteWorkApplication(String applicationId) {
        var applicationToDelete = getSingleApplication(applicationId);
        groupServiceImpl.updateGroupWhenWorkDelete(applicationToDelete);
        repository.delete(applicationToDelete);
    }

    @Override
    public WorkApplications updateApplicationStatus(String applicationId, String newStatus) {
        var workToUpdate = getSingleApplication(applicationId);
        String oldStatus = workToUpdate.getStatus().name();

        if (workToUpdate.getStatus().equals(ApplicationStatus.getByUpperCaseStatus(newStatus))) {
            // TODO: 07.06.2024 add custom exception
            log.info("Status are equal, cant update.");
            return null;
        }

        workToUpdate.setStatus(ApplicationStatus.getByUpperCaseStatus(newStatus));
        var savedWork = repository.save(workToUpdate);

        groupServiceImpl.updateWorkGroupCounters(savedWork, workToUpdate.getStatus().name(), oldStatus);

        return savedWork;
    }

    @Override
    public WorkApplications updateWorkApplicationUrl(String applicationId, String applicationUrl) {
        var workToUpdate = repository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Work application with given ID not found."));
        workToUpdate.setWorkUrl(applicationUrl);
        return repository.save(workToUpdate);
    }

    public List<WorkApplications> getAllApplicationsByWorkGroupId(String workGroupId) {
        return groupServiceImpl.getAllWorkApplicationsFromWorkGroup(workGroupId);
    }

    public WorkApplications getSingleApplication(String applicationId) {
        return repository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Work application with given id no exists."));
    }
}
