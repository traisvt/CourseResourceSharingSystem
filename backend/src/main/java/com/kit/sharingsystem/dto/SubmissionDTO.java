package com.kit.sharingsystem.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SubmissionDTO {
    private Long id;
    private AssignmentDTO assignment;
    private StudentDTO student;
    private LocalDateTime submitTime;
    private String filename;
    private Float score;
    private String feedback;

    @Data
    public static class AssignmentDTO {
        private Long id;
        private String title;
    }

    @Data
    public static class StudentDTO {
        private String username;
        private String name;
    }
}
