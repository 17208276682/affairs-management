package com.affairs.management.controller;

import com.affairs.management.common.ApiResponse;
import com.affairs.management.dto.response.AttachmentVO;
import com.affairs.management.security.SecurityUtils;
import com.affairs.management.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/files/upload")
    public ApiResponse<AttachmentVO> uploadForFiles(@RequestParam("file") MultipartFile file) {
        String userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(fileService.upload(file, userId));
    }

    @PostMapping("/upload")
    public ApiResponse<AttachmentVO> upload(@RequestParam("file") MultipartFile file) {
        String userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(fileService.upload(file, userId));
    }

    @GetMapping("/files/{id}/preview")
    public ResponseEntity<Void> preview(@PathVariable("id") String id) {
        String url = fileService.getPreviewUrl(id);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .build();
    }

    @GetMapping("/files/{id}/download")
    public ResponseEntity<Void> download(@PathVariable("id") String id) {
        String url = fileService.getDownloadUrl(id);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url))
                .build();
    }
}
