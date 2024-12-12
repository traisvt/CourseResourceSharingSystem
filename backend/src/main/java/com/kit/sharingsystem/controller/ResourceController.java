package com.kit.sharingsystem.controller;

import com.kit.sharingsystem.entity.Resource;
import com.kit.sharingsystem.model.PageResult;
import com.kit.sharingsystem.entity.User;
import com.kit.sharingsystem.entity.UserRole;
import com.kit.sharingsystem.exception.FileOperationException;
import com.kit.sharingsystem.service.ResourceService;
import com.kit.sharingsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {
    @Autowired
    private ResourceService resourceService;
    
    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<Resource> uploadResource(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            Authentication authentication) {
        User uploader = userService.findByUsername(authentication.getName());
        Resource resource = new Resource();
        resource.setTitle(title);
        resource.setDescription(description);
        resource.setDownloadCount(0);
        return ResponseEntity.ok(resourceService.uploadResource(file, uploader, resource));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getResource(@PathVariable Long id) {
        return ResponseEntity.ok(resourceService.getResource(id));
    }

    @GetMapping
    public ResponseEntity<PageResult<Resource>> getAllResources(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Resource> resourcePage = resourceService.getAllResourcesPaged(page, size);
        return ResponseEntity.ok(resourcePage);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResult<Resource>> searchResources(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Resource> resourcePage = resourceService.searchResourcesPaged(query, page, size);
        return ResponseEntity.ok(resourcePage);
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<Resource>> getTeacherResources(
            Authentication authentication) {
        User teacher = userService.findByUsername(authentication.getName());
        return ResponseEntity.ok(resourceService.getResourcesByUploader(teacher));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadResource(
            @PathVariable Long id, 
            Authentication authentication, 
            HttpServletRequest request) {
        try {
            Resource resource = resourceService.getResource(id);
            User user = userService.findByUsername(authentication.getName());
            
            // 记录下载
            resourceService.recordDownloadNew(resource, user, request.getRemoteAddr());
            
            // 使用新的下载方法
            return resourceService.downloadResource(id);
            
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "下载失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private String getFileExtensionFromType(String mimeType) {
        switch (mimeType.toLowerCase()) {
            case "application/pdf":
                return ".pdf";
            case "application/msword":
                return ".doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return ".docx";
            case "application/vnd.ms-excel":
                return ".xls";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                return ".xlsx";
            case "application/vnd.ms-powerpoint":
                return ".ppt";
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                return ".pptx";
            case "image/jpeg":
                return ".jpg";
            case "image/png":
                return ".png";
            case "image/gif":
                return ".gif";
            case "text/plain":
                return ".txt";
            default:
                // 如果找不到对应的扩展名，从 MIME 类型中提取
                String[] parts = mimeType.split("/");
                return parts.length > 1 ? "." + parts[1] : "";
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Resource> updateResource(
            @PathVariable Long id,
            @RequestBody Resource resource) {
        return ResponseEntity.ok(resourceService.updateResource(id, resource));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResource(@PathVariable Long id, Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName());
        Resource resource = resourceService.getResource(id);
        
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        // 检查权限：管理员可以删除所有资源，学生和教师可以删除自己的资源
        if ((currentUser.getRole() == UserRole.STUDENT || currentUser.getRole() == UserRole.TEACHER) && !resource.getUploader().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("You don't have permission to delete this resource");
        }

        resourceService.deleteResource(id);
        return ResponseEntity.ok().build();
    }
}
