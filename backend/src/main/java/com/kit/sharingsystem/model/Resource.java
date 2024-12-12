package com.kit.sharingsystem.model;

import com.kit.sharingsystem.entity.User;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Resource {
    private Long id;
    private String title;
    private String description;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private User uploader;
    private Integer downloadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
