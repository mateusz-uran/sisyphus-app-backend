package io.github.mateuszuran.sisyphus_app.service;

import io.github.mateuszuran.sisyphus_app.dto.WorkGroupDTO;
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

import java.util.Base64;
import java.util.List;

@Slf4j
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
                    .isHired(false)
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
        var sendValue = groupToUpdate.getSend();

        if (sendValue != 0) {
            groupToUpdate.setSend(sendValue + applications.size());

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
    public void updateGroupWhenWorkUpdate(WorkApplications work, String newStatus, String oldStatus) {
        var group = findGroupByGivenWorkApplication(work);

        adjustOldStatusCount(oldStatus, group);
        adjustNewStatusCount(newStatus, group);

        repository.save(group);
    }

    @Override
    public void updateGroupWhenWorkDelete(WorkApplications work) {
        var group = findGroupByGivenWorkApplication(work);

        adjustOldStatusCount(work.getStatus().name(), group);

        repository.save(group);
    }


    private void adjustOldStatusCount(String oldStatus, WorkGroup group) {
        switch (oldStatus.toUpperCase()) {
            case "SEND":
                group.setSend(Math.max(0, group.getSend() - 1));
                break;
            case "IN_PROGRESS":
                group.setInProgress(Math.max(0, group.getInProgress() - 1));
                break;
            case "DENIED":
                group.setDenied(Math.max(0, group.getDenied() - 1));
                break;
            case "HIRED":
                group.setHired(false);
        }
    }

    private void adjustNewStatusCount(String newStatus, WorkGroup group) {
        switch (newStatus.toUpperCase()) {
            case "SEND":
                group.setSend(group.getSend() + 1);
                break;
            case "IN_PROGRESS":
                group.setInProgress(group.getInProgress() + 1);
                break;
            case "DENIED":
                group.setDenied(group.getDenied() + 1);
                break;
            case "HIRED":
                group.setHired(true);
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
                .isHired(group.isHired())
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
                                .isHired(group.isHired())
                                .build())
                .toList();
    }
}
