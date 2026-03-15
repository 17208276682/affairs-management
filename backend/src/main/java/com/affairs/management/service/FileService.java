package com.affairs.management.service;

import com.affairs.management.dto.response.AttachmentVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    AttachmentVO upload(MultipartFile file, String uploadedBy);
}
