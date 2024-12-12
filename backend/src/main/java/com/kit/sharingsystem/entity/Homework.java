package com.kit.sharingsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Homework {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime dueDate;    // 对应数据库中的 due_date
    private LocalDateTime createdAt;  // 对应数据库中的 created_at
    private User teacher;             // 对应数据库中的 teacher_id
}
