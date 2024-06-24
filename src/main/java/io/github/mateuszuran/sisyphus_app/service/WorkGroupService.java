package io.github.mateuszuran.sisyphus_app.service;

import io.github.mateuszuran.sisyphus_app.model.WorkApplications;
import io.github.mateuszuran.sisyphus_app.model.WorkGroup;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface WorkGroupService {
    void createNewWorkGroup(MultipartFile cvFile) throws IOException;

    WorkGroup getWorkGroup(String workGroupId);

    WorkGroup updateWorkGroupWithWorkApplications(List<WorkApplications> applications, String workGroupId);

    List<WorkGroup> getAllGroups();

    void deleteSingleGroup(String workGroupId);

    List<WorkApplications> getAllWorkApplicationsFromWorkGroup(String workGroupId);

    void updateGroupWhenWorkUpdate(WorkApplications work, String newStatus, String oldStatus);

    void updateGroupWhenWorkDelete(WorkApplications work);

}
