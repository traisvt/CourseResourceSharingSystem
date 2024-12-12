package com.kit.sharingsystem.service;

import com.kit.sharingsystem.entity.Assignment;
import com.kit.sharingsystem.entity.User;
import com.kit.sharingsystem.mapper.AssignmentMapper;
import com.kit.sharingsystem.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssignmentService {

    @Autowired
    private AssignmentMapper assignmentMapper;

    @Transactional
    public Assignment createAssignment(Assignment assignment) {
        assignment.setCreatedAt(LocalDateTime.now());
        assignmentMapper.insert(assignment);
        return assignment;
    }

    public Assignment getAssignment(Long id) {
        Assignment assignment = assignmentMapper.findById(id);
        if (assignment == null) {
            throw new RuntimeException("Assignment not found with id: " + id);
        }
        return assignment;
    }

    public List<Assignment> getAllAssignments() {
        return assignmentMapper.findAll();
    }

    public PageResult<Assignment> getAllAssignmentsPaged(int page, int size) {
        int offset = page * size;
        List<Assignment> assignments = assignmentMapper.findAllWithPaging(offset, size);
        long total = assignmentMapper.count();
        return new PageResult<>(assignments, total, page, size);
    }

    @Transactional
    public Assignment updateAssignment(Long id, Assignment updatedAssignment) {
        Assignment existingAssignment = getAssignment(id);
        existingAssignment.setTitle(updatedAssignment.getTitle());
        existingAssignment.setDescription(updatedAssignment.getDescription());
        existingAssignment.setDueDate(updatedAssignment.getDueDate());
        assignmentMapper.update(existingAssignment);
        return existingAssignment;
    }

    public List<Assignment> getTeacherAssignments(User teacher) {
        return assignmentMapper.findByTeacherId(teacher.getId());
    }

    public PageResult<Assignment> getTeacherAssignmentsPaged(User teacher, int page, int size) {
        int offset = page * size;
        List<Assignment> assignments = assignmentMapper.findByTeacherIdWithPaging(teacher.getId(), offset, size);
        long total = assignmentMapper.countByTeacherId(teacher.getId());
        return new PageResult<>(assignments, total, page, size);
    }

    public boolean isAssignmentOverdue(Assignment assignment) {
        return LocalDateTime.now().isAfter(assignment.getDueDate());
    }

    @Transactional
    public void deleteAssignment(Long id) {
        assignmentMapper.deleteById(id);
    }
}
