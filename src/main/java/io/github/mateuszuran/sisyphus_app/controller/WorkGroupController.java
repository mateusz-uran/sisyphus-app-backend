package io.github.mateuszuran.sisyphus_app.controller;

import io.github.mateuszuran.sisyphus_app.dto.WorkGroupDTO;
import io.github.mateuszuran.sisyphus_app.service.WorkGroupServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class WorkGroupController {
    private final WorkGroupServiceImpl service;

    @GetMapping("/all")
    public ResponseEntity<List<WorkGroupDTO>> getAllWorkGroups() {
        return ResponseEntity.ok()
                .body(service.getAllMappedWorkGroups());
    }

    @PostMapping("/create")
    public ResponseEntity<String> createWorKGroup(@RequestParam("cv") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            service.createNewWorkGroup(file);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @GetMapping("/single/{workGroupId}")
    public ResponseEntity<WorkGroupDTO> getSingleWorkGroup(@PathVariable String workGroupId) {
        return ResponseEntity.ok()
                .body(service.getMappedSingleWorkGroup(workGroupId));
    }

    @DeleteMapping("/delete/{workGroupId}")
    public ResponseEntity<String> deleteSingleWorkGroup(@PathVariable String workGroupId) {
        service.deleteSingleGroup(workGroupId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
