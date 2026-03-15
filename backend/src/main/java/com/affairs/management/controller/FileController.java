package com.affairs.management.controller;

import com.affairs.management.common.ApiResponse;
import com.affairs.management.dto.response.AttachmentVO;
import com.affairs.management.security.SecurityUtils;
import com.affairs.management.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ApiResponse<AttachmentVO> upload(@RequestParam("file") MultipartFile file) {
        String userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(fileService.upload(file, userId));
    }
}
