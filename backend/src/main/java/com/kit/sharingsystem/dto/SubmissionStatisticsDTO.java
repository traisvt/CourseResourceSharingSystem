package com.kit.sharingsystem.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SubmissionStatisticsDTO {
    private int totalSubmissions;
    private int gradedCount;
    private int ungradedCount;
    private double averageScore;
    private double highestScore;
    private double lowestScore;
    private Map<String, Integer> scoreDistribution;
}
