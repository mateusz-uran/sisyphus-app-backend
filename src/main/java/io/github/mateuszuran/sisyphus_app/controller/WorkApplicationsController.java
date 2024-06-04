package io.github.mateuszuran.sisyphus_app.controller;

import io.github.mateuszuran.sisyphus_app.model.WorkApplications;
import io.github.mateuszuran.sisyphus_app.service.WorkApplicationsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class WorkApplicationsController {
    private final WorkApplicationsServiceImpl service;

    @PostMapping("/add-work")
    public ResponseEntity<?> addWorkApplications(@RequestBody List<WorkApplications> applications, @RequestParam String workGroupId) {
        service.createWorkApplication(applications, workGroupId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Work applications added");
    }
}
