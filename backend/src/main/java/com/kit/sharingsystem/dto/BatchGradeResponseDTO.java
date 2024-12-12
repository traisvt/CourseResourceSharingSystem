package com.kit.sharingsystem.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class BatchGradeResponseDTO {
    private List<Long> successIds;
    private Map<Long, String> failures;
}
