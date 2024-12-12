package com.kit.sharingsystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Submission {
    private Long id;

    private Assignment assignment;

    private User student;

    private String filePath;

    private String filename;

    private String feedback;

    private Float score;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;
}
