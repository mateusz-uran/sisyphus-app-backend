package io.github.mateuszuran.sisyphus_app.service;

import io.github.mateuszuran.sisyphus_app.model.ApplicationStatus;
import io.github.mateuszuran.sisyphus_app.model.WorkApplications;
import io.github.mateuszuran.sisyphus_app.repository.WorkApplicationsRepository;
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

    @Override
    public void createWorkApplication(List<WorkApplications> applications, String workGroupId) {
        repository.saveAll(applications);
        groupServiceImpl.updateWorkGroupWithWorkApplications(applications, workGroupId);
    }


    @Override
    public void deleteWorkApplication(String applicationId) {
        repository.deleteById(applicationId);
    }

    @Override
    public WorkApplications updateApplicationStatus(ApplicationStatus status, String applicationId) {
        var workToUpdate = repository.findById(applicationId)
                .orElseThrow(() -> new IllegalStateException("Work application with given id no exists."));
        workToUpdate.setStatus(status);
        return repository.save(workToUpdate);
    }

    public List<WorkApplications> getAllApplicationsByWorkGroupId(String workGroupId) {
        return groupServiceImpl.getAllWorkApplicationsFromWorkGroup(workGroupId);
    }
}
