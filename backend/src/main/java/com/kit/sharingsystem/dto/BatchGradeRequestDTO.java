package com.kit.sharingsystem.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchGradeRequestDTO {
    private List<GradeInfo> grades;

    @Data
    public static class GradeInfo {
        private Long submissionId;
        private Float score;
        private String feedback;
    }
}
