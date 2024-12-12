package com.kit.sharingsystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DownloadRecord {
    private Long id;
    private User user;
    private Resource resource;
    private String ipAddress;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime downloadedAt;
}
