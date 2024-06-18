package io.github.mateuszuran.sisyphus_app.service;

import io.github.mateuszuran.sisyphus_app.dto.WorkGroupDTO;
import io.github.mateuszuran.sisyphus_app.model.WorkApplications;
import io.github.mateuszuran.sisyphus_app.model.WorkGroup;
import io.github.mateuszuran.sisyphus_app.repository.WorkGroupRepository;
import io.github.mateuszuran.sisyphus_app.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkGroupServiceImpl implements WorkGroupService {
    private final WorkGroupRepository repository;
    private final TimeUtil utility;

    @Override
    public void createNewWorkGroup(MultipartFile file) {
        try {
            Binary cv = new Binary(BsonBinarySubType.BINARY, file.getBytes());
            String creationTime = utility.formatCreationTime();

            WorkGroup group = WorkGroup.builder()
                    .cvData(cv)
                    .cvFileName(file.getOriginalFilename())
                    .creationTime(creationTime)
                    .send(0)
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
        var appliedValue = groupToUpdate.getSend();

        if (appliedValue != 0) {
            groupToUpdate.setSend(appliedValue + applications.size());

        } else {
            groupToUpdate.setSend(applications.size());
        }

        return repository.save(groupToUpdate);
    }

    @Override
    public List<WorkGroup> getAllGroups() {
        return repository.findAll();
    }

    @Override
    public void deleteSingleGroup(String workGroupId) {
        var groupToDelete = getWorkGroup(workGroupId);
        repository.delete(groupToDelete);
    }

    @Override
    public List<WorkApplications> getAllWorkApplicationsFromWorkGroup(String workGroupId) {
        var groupToFind = getWorkGroup(workGroupId);
        return groupToFind.getWorkApplications();
    }

    @Override
    public void updateWorkGroupCounters(WorkApplications work, String newStatus, String oldStatus) {
        var group = findGroupByGivenWorkApplication(work);

        adjustCounter(group, oldStatus, -1);

        adjustCounter(group, newStatus, 1);

        repository.save(group);
    }

    @Override
    public void updateGroupWhenWorkDelete(WorkApplications work) {
        var group = findGroupByGivenWorkApplication(work);
        var sendValue = group.getSend();
        var inProgressValue = group.getInProgress();
        var deniedValue = group.getDenied();
        if ("DENIED".equalsIgnoreCase(work.getStatus().name())) {
            group.setDenied(deniedValue - 1);
        } else {
            group.setSend(sendValue - 1);
            group.setInProgress(inProgressValue - 1);
        }
        repository.save(group);
    }

    private void adjustCounter(WorkGroup group, String status, int adjustment) {
        switch (status.toUpperCase()) {
            case "SEND":
                group.setSend(group.getSend() + adjustment);
                break;
            case "IN_PROGRESS":
                group.setInProgress(group.getInProgress() + adjustment);
                break;
            case "DENIED":
                group.setDenied(group.getDenied() + adjustment);
                break;
        }
    }

    private WorkGroup findGroupByGivenWorkApplication(WorkApplications work) {
        return repository.findAll()
                .stream()
                .filter(workGroup -> workGroup.getWorkApplications() != null)
                .filter(workGroup -> workGroup.getWorkApplications()
                        .stream()
                        .anyMatch(workApplications -> workApplications.getId().equals(work.getId())))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Work group not found"));
    }

    private String encodeBinaryCv(Binary groupCv) {
        return Base64.getEncoder().encodeToString(groupCv.getData());
    }

    public WorkGroupDTO getMappedSingleWorkGroup(String workGroupId) {
        WorkGroup group = getWorkGroup(workGroupId);
        return WorkGroupDTO.builder()
                .id(group.getId())
                .cvData(encodeBinaryCv(group.getCvData()))
                .cvFileName(group.getCvFileName())
                .creationTime(group.getCreationTime())
                .applied(group.getSend())
                .denied(group.getDenied())
                .inProgress(group.getInProgress())
                .build();
    }

    public List<WorkGroupDTO> getAllMappedWorkGroups() {
        var allGroups = getAllGroups();
        return allGroups
                .stream()
                .map(group ->
                        WorkGroupDTO.builder()
                                .id(group.getId())
                                .cvData(encodeBinaryCv(group.getCvData()))
                                .cvFileName(group.getCvFileName())
                                .creationTime(group.getCreationTime())
                                .applied(group.getSend())
                                .denied(group.getDenied())
                                .inProgress(group.getInProgress())
                                .build())
                .toList();
    }
}
