package io.github.mateuszuran.sisyphus_app.controller;

import io.github.mateuszuran.sisyphus_app.dto.WorkGroupDTO;
import io.github.mateuszuran.sisyphus_app.service.WorkGroupServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
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
    public ResponseEntity<?> createWorKGroup(@RequestParam("cv") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        } else {
            service.createNewWorkGroup(file);
            return new ResponseEntity<String>(HttpStatus.OK);
        }
    }

    @GetMapping("/single")
    public ResponseEntity<WorkGroupDTO> getSingleWorkGroup(@RequestParam String workGroupId) {
        return ResponseEntity.ok()
                .body(service.getMappedSingleWorkGroup(workGroupId));
    }
}
