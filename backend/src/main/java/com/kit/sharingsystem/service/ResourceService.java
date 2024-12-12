package com.kit.sharingsystem.service;

import com.kit.sharingsystem.entity.DownloadRecord;
import com.kit.sharingsystem.entity.Resource;
import com.kit.sharingsystem.entity.User;
import com.kit.sharingsystem.exception.FileOperationException;
import com.kit.sharingsystem.mapper.ResourceMapper;
import com.kit.sharingsystem.mapper.DownloadRecordMapper;
import com.kit.sharingsystem.model.PageResult;
import com.kit.sharingsystem.util.FilePathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

@Service
public class ResourceService {
    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private DownloadRecordMapper downloadRecordMapper;

    @Autowired
    private FilePathUtil filePathUtil;

    private static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "image/jpeg",
        "image/png",
        "image/gif",
        "text/plain"
    ));

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Allowed types are: " + 
                String.join(", ", ALLOWED_CONTENT_TYPES));
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10MB
            throw new IllegalArgumentException("File size exceeds maximum limit (10MB)");
        }
    }

    private String determineFileType(String contentType, String extension) {
        if (contentType == null || contentType.isEmpty()) {
            // 根据扩展名设置默认类型
            switch (extension.toLowerCase()) {
                case ".pdf":
                    return "application/pdf";
                case ".doc":
                case ".docx":
                    return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                case ".xls":
                case ".xlsx":
                    return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                case ".ppt":
                case ".pptx":
                    return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                case ".jpg":
                case ".jpeg":
                    return "image/jpeg";
                case ".png":
                    return "image/png";
                case ".gif":
                    return "image/gif";
                case ".txt":
                    return "text/plain";
                default:
                    return "application/octet-stream";
            }
        }
        return contentType;
    }

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = filePathUtil.getUploadPath();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new FileOperationException("Could not create upload directory", e);
        }
    }

    @Transactional
    public com.kit.sharingsystem.entity.Resource uploadResource(MultipartFile file, User uploader, com.kit.sharingsystem.entity.Resource resource) {
        try {
            validateFile(file);

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new IllegalArgumentException("Invalid file name");
            }

            String extension = "";
            int lastDotIdx = originalFilename.lastIndexOf(".");
            if (lastDotIdx > 0) {
                extension = originalFilename.substring(lastDotIdx);
            }

            // 生成唯一的文件名
            String uniqueFileName = UUID.randomUUID().toString() + extension;
            Path filePath = filePathUtil.getResourcePath(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 保存相对路径到数据库
            resource.setFilePath(filePathUtil.getRelativePath(uniqueFileName));
            resource.setFileType(determineFileType(file.getContentType(), extension));
            resource.setFileSize(file.getSize());
            resource.setUploader(uploader);
            resource.setDownloadCount(0);
            resource.setCreatedAt(LocalDateTime.now());
            resource.setUpdatedAt(LocalDateTime.now());

            resourceMapper.insert(resource);
            return resource;
        } catch (IOException e) {
            throw new FileOperationException("Could not store file " + file.getOriginalFilename(), e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDotIdx = filename.lastIndexOf('.');
        return lastDotIdx < 0 ? "" : filename.substring(lastDotIdx);
    }

    public com.kit.sharingsystem.entity.Resource getResource(Long id) {
        com.kit.sharingsystem.entity.Resource resource = resourceMapper.findById(id);
        if (resource == null) {
            throw new RuntimeException("Resource not found with id: " + id);
        }
        return resource;
    }

    public List<com.kit.sharingsystem.entity.Resource> getAllResources() {
        return resourceMapper.findAll();
    }

    public PageResult<com.kit.sharingsystem.entity.Resource> getAllResourcesPaged(int page, int size) {
        int offset = page * size;
        List<com.kit.sharingsystem.entity.Resource> resources = resourceMapper.getAllResourcesPaged(offset, size);
        long total = resourceMapper.countAllResources();
        return new PageResult<>(resources, total, page, size);
    }

    public List<com.kit.sharingsystem.entity.Resource> getResourcesByUploader(User uploader) {
        return resourceMapper.findByUploaderId(uploader.getId());
    }

    public List<com.kit.sharingsystem.entity.Resource> searchResources(String keyword) {
        return resourceMapper.searchByKeyword(keyword);
    }

    public PageResult<com.kit.sharingsystem.entity.Resource> searchResourcesPaged(String query, int page, int size) {
        int offset = page * size;
        List<com.kit.sharingsystem.entity.Resource> resources = resourceMapper.searchResourcesPaged(query, offset, size);
        long total = resourceMapper.countSearchResources(query);
        return new PageResult<>(resources, total, page, size);
    }

    @Transactional
    public com.kit.sharingsystem.entity.Resource updateResource(Long id, com.kit.sharingsystem.entity.Resource updatedResource) {
        com.kit.sharingsystem.entity.Resource existingResource = getResource(id);
        
        existingResource.setTitle(updatedResource.getTitle());
        existingResource.setDescription(updatedResource.getDescription());
        existingResource.setUpdatedAt(LocalDateTime.now());
        
        resourceMapper.update(existingResource);
        return existingResource;
    }

    @Transactional
    public void deleteResource(Long id) {
        com.kit.sharingsystem.entity.Resource resource = getResource(id);
        if (resource != null) {
            // 先删除相关的下载记录
            downloadRecordMapper.deleteByResourceId(id);

            // 删除物理文件
            try {
                Path filePath = filePathUtil.getResourcePath(resource.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // 即使文件删除失败，也继续删除数据库记录
                e.printStackTrace();
            }
            // 删除资源记录
            resourceMapper.deleteById(id);
        }
    }

    public Path getResourceFilePath(Long resourceId) {
        com.kit.sharingsystem.entity.Resource resource = getResource(resourceId);
        if (resource == null) {
            throw new FileOperationException("Resource not found with id: " + resourceId);
        }
        String fileName = Paths.get(resource.getFilePath()).getFileName().toString();
        return filePathUtil.getResourcePath(fileName);
    }

    @Transactional
    public void recordDownloadNew(com.kit.sharingsystem.entity.Resource resource, User user, String ipAddress) {
        // 更新下载次数
        resource.setDownloadCount(resource.getDownloadCount() + 1);
        resource.setUpdatedAt(LocalDateTime.now());
        resourceMapper.update(resource);

        // 记录下载历史
        DownloadRecord record = new DownloadRecord();
        record.setResource(resource);
        record.setUser(user);
        record.setIpAddress(ipAddress);
        record.setDownloadedAt(LocalDateTime.now());
        downloadRecordMapper.insert(record);
    }

    public ResponseEntity<org.springframework.core.io.Resource> downloadResource(Long resourceId) throws IOException {
        com.kit.sharingsystem.entity.Resource resource = getResource(resourceId);
        Path filePath = getResourceFilePath(resourceId);
        String fileName = Paths.get(resource.getFilePath()).getFileName().toString();
        
        org.springframework.core.io.Resource fileResource = new FileSystemResource(filePath.toFile());
        
        if (!fileResource.exists()) {
            throw new FileOperationException("File not found: " + fileName);
        }

        // 根据文件类型设置正确的Content-Type
        String contentType = resource.getFileType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // 设置响应头
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"")
                .body(fileResource);
    }
}
