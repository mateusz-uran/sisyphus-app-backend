package io.github.mateuszuran.sisyphus_app.controller;

import io.github.mateuszuran.sisyphus_app.model.WorkApplications;
import io.github.mateuszuran.sisyphus_app.service.WorkApplicationsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class WorkApplicationsController {
    private final WorkApplicationsServiceImpl service;

    @PostMapping("/save/{workGroupId}")
    public ResponseEntity<String> addWorkApp(@RequestBody List<WorkApplications> applications, @PathVariable String workGroupId) {
        service.createWorkApplication(applications, workGroupId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/delete/{applicationId}")
    public ResponseEntity<String> deleteSingleWorkApplication(@PathVariable String applicationId) {
        service.deleteWorkApplication(applicationId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/update/{applicationId}")
    public ResponseEntity<WorkApplications> updateWorkStatus(@PathVariable String applicationId, @RequestBody String applicationStatus) {
        var updatedWork = service.updateApplicationStatus(applicationId, applicationStatus);
        return ResponseEntity.ok(updatedWork);
    }
}
