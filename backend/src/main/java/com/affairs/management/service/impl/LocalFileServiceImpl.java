package com.affairs.management.service.impl;

import com.affairs.management.common.BusinessException;
import com.affairs.management.common.ErrorCode;
import com.affairs.management.dto.response.AttachmentVO;
import com.affairs.management.config.MinioConfig;
import com.affairs.management.entity.Attachment;
import com.affairs.management.mapper.AttachmentMapper;
import com.affairs.management.service.FileService;
import com.affairs.management.util.IdGenerator;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 文件存储实现（MinIO）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileServiceImpl implements FileService {

    private final AttachmentMapper attachmentMapper;
    private final IdGenerator idGenerator;
    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public AttachmentVO upload(MultipartFile file, String uploadedBy) {
        try {
            ensureBucket();

            String originalName = file.getOriginalFilename();
            String attachmentId = idGenerator.nextAttachmentId();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String objectName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                    + "/" + attachmentId + "-" + UUID.randomUUID().toString().replace("-", "") + ext;
                String contentType = resolveContentType(file.getContentType(), originalName);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(contentType)
                            .build()
            );

            Attachment attachment = new Attachment();
            attachment.setId(attachmentId);
            attachment.setFileName(originalName);
            attachment.setFileUrl(objectName);
            attachment.setFileSize(file.getSize());
            attachment.setMimeType(contentType);
            attachment.setUploadedBy(uploadedBy);
            attachmentMapper.insert(attachment);

            AttachmentVO vo = new AttachmentVO();
            vo.setId(attachment.getId());
            vo.setName(attachment.getFileName());
            vo.setUrl(buildPreviewPath(attachment.getId()));
            vo.setType(attachment.getMimeType());
            vo.setSize(attachment.getFileSize());
            vo.setUploadedAt(LocalDateTime.now().format(DTF));
            return vo;

        } catch (Exception e) {
            log.error("File upload failed", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public String getPreviewUrl(String attachmentId) {
        Attachment attachment = getAttachment(attachmentId);
        try {
            String fileName = attachment.getFileName() == null ? attachment.getId() : attachment.getFileName();
            Map<String, String> query = new HashMap<>();
            query.put("response-content-disposition", "inline");
            if (attachment.getMimeType() != null && !attachment.getMimeType().isBlank()) {
                query.put("response-content-type", attachment.getMimeType());
            }
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(attachment.getFileUrl())
                            .expiry(30, TimeUnit.MINUTES)
                            .extraQueryParams(query)
                            .build()
            );
        } catch (Exception e) {
            log.error("Generate preview url failed, attachmentId={}", attachmentId, e);
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "附件预览地址生成失败");
        }
    }

    @Override
    public String getDownloadUrl(String attachmentId) {
        Attachment attachment = getAttachment(attachmentId);
        try {
            String fileName = attachment.getFileName() == null ? attachment.getId() : attachment.getFileName();
            Map<String, String> query = new HashMap<>();
            query.put("response-content-disposition", "attachment; filename*=UTF-8''" + urlEncode(fileName));
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(attachment.getFileUrl())
                            .expiry(30, TimeUnit.MINUTES)
                            .extraQueryParams(query)
                            .build()
            );
        } catch (Exception e) {
            log.error("Generate download url failed, attachmentId={}", attachmentId, e);
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "附件下载地址生成失败");
        }
    }

    private Attachment getAttachment(String attachmentId) {
        Attachment attachment = attachmentMapper.selectById(attachmentId);
        if (attachment == null) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        return attachment;
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(minioConfig.getBucketName()).build()
        );
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioConfig.getBucketName()).build());
        }
    }

    private String buildPreviewPath(String attachmentId) {
        return "/api/files/" + attachmentId + "/preview";
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private String resolveContentType(String contentType, String fileName) {
        if (contentType != null && !contentType.isBlank() && !"application/octet-stream".equalsIgnoreCase(contentType)) {
            return contentType;
        }
        if (fileName != null && !fileName.isBlank()) {
            try {
                String guessed = Files.probeContentType(Path.of(fileName));
                if (guessed != null && !guessed.isBlank()) {
                    return guessed;
                }
            } catch (Exception ignored) {
            }
        }
        return "application/octet-stream";
    }
}
