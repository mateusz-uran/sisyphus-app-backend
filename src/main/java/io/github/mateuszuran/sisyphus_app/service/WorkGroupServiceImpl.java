package io.github.mateuszuran.sisyphus_app.service;

import io.github.mateuszuran.sisyphus_app.model.WorkApplications;
import io.github.mateuszuran.sisyphus_app.model.WorkGroup;
import io.github.mateuszuran.sisyphus_app.repository.WorkGroupRepository;
import io.github.mateuszuran.sisyphus_app.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkGroupServiceImpl implements WorkGroupService {
    private final WorkGroupRepository repository;
    private final TimeUtil utility;

    @Override
    public void createNewWorkGroup(MultipartFile file) {
        try {
            Binary cv = new Binary(BsonBinarySubType.BINARY, file.getBytes());
            String creationTime = utility.formatCreationTime();
            WorkGroup group = WorkGroup.builder()
                    .cv_url(cv)
                    .creationTime(creationTime)
                    .applied(0)
                    .denied(0)
                    .inProgress(0)
                    .build();
            repository.save(group);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file", e);
        }
    }

    @Override
    public WorkGroup getWorkGroup(String workGroupId) {
        return repository.findById(workGroupId)
                .orElseThrow(() -> new RuntimeException("Work group with given ID not found"));
    }

    @Override
    public WorkGroup updateWorkGroupWithWorkApplications(List<WorkApplications> applications, String workGroupId) {
        if (applications == null) {
            throw new IllegalStateException("Applications list is empty");
        }
        WorkGroup groupToUpdate = getWorkGroup(workGroupId);
        groupToUpdate.getWorkApplications().addAll(applications);
        var appliedValue = groupToUpdate.getApplied();

        if (appliedValue != 0) {
            groupToUpdate.setApplied(appliedValue + applications.size());
        } else groupToUpdate.setApplied(applications.size());

        return repository.save(groupToUpdate);
    }

    @Override
    public List<WorkGroup> getAllGroups() {
        return repository.findAll();
    }

    @Override
    public void deleteSingleGroup(String workGroupId) {
        repository.deleteById(workGroupId);
    }

    @Override
    public List<WorkApplications> getAllWorkApplicationsFromWorkGroup(String workGroupId) {
        var groupToFind = getWorkGroup(workGroupId);
        return groupToFind.getWorkApplications();
    }
}
