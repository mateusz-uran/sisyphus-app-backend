package io.github.mateuszuran.sisyphus_app.service;

import io.github.mateuszuran.sisyphus_app.model.ApplicationStatus;
import io.github.mateuszuran.sisyphus_app.model.WorkApplications;

import java.util.List;

public interface WorkApplicationsService {
    void createWorkApplication(List<WorkApplications> application, String workGroupId);

    void deleteWorkApplication(String applicationId);

    WorkApplications updateApplicationStatus(ApplicationStatus status, String applicationId);
}
