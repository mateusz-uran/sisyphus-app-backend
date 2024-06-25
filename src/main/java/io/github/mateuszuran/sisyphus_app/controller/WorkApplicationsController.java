package io.github.mateuszuran.sisyphus_app.controller;

import io.github.mateuszuran.sisyphus_app.dto.WorkApplicationDTO;
import io.github.mateuszuran.sisyphus_app.model.WorkApplications;
import io.github.mateuszuran.sisyphus_app.service.WorkApplicationsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class WorkApplicationsController {
    private final WorkApplicationsServiceImpl service;

    @GetMapping("/all/{workGroupId}")
    public ResponseEntity<List<WorkApplications>> getAllWorkApplicationsByGroup(@PathVariable String workGroupId) {
        return ResponseEntity.ok().body(service.getAllApplicationsByWorkGroupId(workGroupId));
    }

    @PostMapping("/save/{workGroupId}")
    public ResponseEntity<String> addWorkApp(@RequestBody List<WorkApplicationDTO> applications, @PathVariable String workGroupId) {
        service.createWorkApplication(applications, workGroupId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/delete/{applicationId}")
    public ResponseEntity<String> deleteSingleWorkApplication(@PathVariable String applicationId) {
        service.deleteWorkApplication(applicationId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/update/{applicationId}/{status}")
    public ResponseEntity<WorkApplications> updateWorkStatus(@PathVariable String applicationId, @PathVariable String status) {
        return ResponseEntity.ok().body(service.updateApplicationStatus(applicationId, status));
    }
}
