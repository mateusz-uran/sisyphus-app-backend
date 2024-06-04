package io.github.mateuszuran.sisyphus_app.controller;

import io.github.mateuszuran.sisyphus_app.service.WorkGroupServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class WorkGroupController {
    private final WorkGroupServiceImpl service;

    @PostMapping("/addgroup")
    public void addNewWorkGroup(@RequestParam("cv")MultipartFile file) {
        service.createNewWorkGroup(file);
    }

    @GetMapping("/getgroup")
    public ResponseEntity<ByteArrayResource> getPdf(@RequestParam String id) {
        var group = service.getWorkGroup(id);
        byte[] pdfData = group.getCv_url().getData();

        // Set the response headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "workgroup.pdf");

        // Return the binary data in the response body
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(pdfData.length)
                .body(new ByteArrayResource(pdfData));
    }

    @GetMapping("/getgroupv2")
    public ResponseEntity<?> getPdf2(@RequestParam String id) {
        return ResponseEntity.ok().body(service.getWorkGroup(id));
    }

}
