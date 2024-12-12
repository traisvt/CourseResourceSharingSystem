package com.kit.sharingsystem.service;

import com.kit.sharingsystem.entity.Homework;
import com.kit.sharingsystem.entity.User;
import com.kit.sharingsystem.entity.UserRole;
import com.kit.sharingsystem.mapper.HomeworkMapper;
import com.kit.sharingsystem.mapper.SubmissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HomeworkService {
    @Autowired
    private HomeworkMapper homeworkMapper;

    @Autowired
    private SubmissionMapper submissionMapper;

    @Transactional
    public Homework createHomework(Homework homework, User teacher) {
        homework.setTeacher(teacher);
        homework.setCreatedAt(LocalDateTime.now());
        homeworkMapper.insert(homework);
        return homework;
    }

    public Homework getHomework(Long id) {
        return homeworkMapper.findById(id);
    }

    public List<Homework> getAllHomework() {
        return homeworkMapper.findAll();
    }

    public List<Homework> getHomeworkByTeacher(User teacher) {
        return homeworkMapper.findByTeacherId(teacher.getId());
    }

    @Transactional
    public Homework updateHomework(Long id, Homework updatedHomework, User currentUser) {
        Homework existingHomework = homeworkMapper.findById(id);
        if (existingHomework == null) {
            throw new RuntimeException("Homework not found");
        }

        // 检查权限：只有管理员或作业发布者可以更新
        if (currentUser.getRole() != UserRole.ADMIN && 
            !existingHomework.getTeacher().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No permission to update this homework");
        }

        existingHomework.setTitle(updatedHomework.getTitle());
        existingHomework.setDescription(updatedHomework.getDescription());
        existingHomework.setDueDate(updatedHomework.getDueDate());

        homeworkMapper.update(existingHomework);
        return existingHomework;
    }

    @Transactional
    public void deleteHomework(Long id, User currentUser) {
        Homework homework = homeworkMapper.findById(id);
        if (homework == null) {
            throw new RuntimeException("Homework not found");
        }

        // 检查权限：只有管理员或作业发布者可以删除
        if (currentUser.getRole() != UserRole.ADMIN && 
            !homework.getTeacher().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No permission to delete this homework");
        }

        // 先删除相关的提交记录
        submissionMapper.deleteByHomeworkId(id);
        // 再删除作业
        homeworkMapper.delete(id);
    }
}
