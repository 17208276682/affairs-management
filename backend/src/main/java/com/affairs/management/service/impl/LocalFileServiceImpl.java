package com.affairs.management.service.impl;

import com.affairs.management.common.BusinessException;
import com.affairs.management.common.ErrorCode;
import com.affairs.management.dto.response.AttachmentVO;
import com.affairs.management.entity.Attachment;
import com.affairs.management.mapper.AttachmentMapper;
import com.affairs.management.service.FileService;
import com.affairs.management.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地文件存储实现（临时方案，后续切换为 MinIO）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileServiceImpl implements FileService {

    private final AttachmentMapper attachmentMapper;
    private final IdGenerator idGenerator;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public AttachmentVO upload(MultipartFile file, String uploadedBy) {
        try {
            // 确保目录存在
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID().toString().replace("-", "") + ext;

            // 保存文件
            File dest = new File(dir, storedName);
            file.transferTo(dest);

            // 存入数据库
            Attachment attachment = new Attachment();
            attachment.setId(idGenerator.nextAttachmentId());
            attachment.setFileName(originalName);
            attachment.setFileUrl("/api/files/" + storedName);
            attachment.setFileSize(file.getSize());
            attachment.setMimeType(file.getContentType());
            attachment.setUploadedBy(uploadedBy);
            attachmentMapper.insert(attachment);

            // 返回 VO
            AttachmentVO vo = new AttachmentVO();
            vo.setId(attachment.getId());
            vo.setName(attachment.getFileName());
            vo.setUrl(attachment.getFileUrl());
            vo.setType(attachment.getMimeType());
            vo.setSize(attachment.getFileSize());
            vo.setUploadedAt(LocalDateTime.now().format(DTF));
            return vo;

        } catch (IOException e) {
            log.error("File upload failed", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}
