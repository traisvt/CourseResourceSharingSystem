package com.kit.sharingsystem.controller;

import com.kit.sharingsystem.entity.Homework;
import com.kit.sharingsystem.entity.User;
import com.kit.sharingsystem.entity.UserRole;
import com.kit.sharingsystem.service.HomeworkService;
import com.kit.sharingsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/homework")
public class HomeworkController {
    @Autowired
    private HomeworkService homeworkService;

    @Autowired
    private UserService userService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Homework> createHomework(
            @RequestBody Homework homework,
            Authentication authentication) {
        User teacher = userService.findByUsername(authentication.getName());
        return ResponseEntity.ok(homeworkService.createHomework(homework, teacher));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Homework> getHomework(@PathVariable Long id) {
        return ResponseEntity.ok(homeworkService.getHomework(id));
    }

    @GetMapping
    public ResponseEntity<List<Homework>> getAllHomework(Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName());
        // 如果是教师，只返回自己发布的作业
        if (currentUser.getRole() == UserRole.TEACHER) {
            return ResponseEntity.ok(homeworkService.getHomeworkByTeacher(currentUser));
        }
        // 管理员和学生可以看到所有作业
        return ResponseEntity.ok(homeworkService.getAllHomework());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Homework> updateHomework(
            @PathVariable Long id,
            @RequestBody Homework homework,
            Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName());
        try {
            return ResponseEntity.ok(homeworkService.updateHomework(id, homework, currentUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> deleteHomework(
            @PathVariable Long id,
            Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName());
        try {
            homeworkService.deleteHomework(id, currentUser);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
