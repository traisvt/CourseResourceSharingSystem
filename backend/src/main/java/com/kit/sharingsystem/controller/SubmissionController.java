package com.kit.sharingsystem.controller;

import com.kit.sharingsystem.dto.BatchGradeRequestDTO;
import com.kit.sharingsystem.dto.BatchGradeResponseDTO;
import com.kit.sharingsystem.dto.SubmissionStatisticsDTO;
import com.kit.sharingsystem.entity.Submission;
import com.kit.sharingsystem.entity.User;
import com.kit.sharingsystem.service.SubmissionService;
import com.kit.sharingsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final UserService userService;

    @PostMapping("/{assignmentId}")
    public ResponseEntity<Submission> submitAssignment(
            @PathVariable Long assignmentId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws Exception {
        User currentUser = userService.getUserByUsername(authentication.getName());
        Submission submission = submissionService.submit(assignmentId, file, currentUser);
        return ResponseEntity.ok(submission);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadSubmission(
            @PathVariable Long id,
            Authentication authentication) throws Exception {
        User currentUser = userService.getUserByUsername(authentication.getName());
        Resource resource = submissionService.loadSubmissionFile(id, currentUser);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping("/{id}/grade")
    public ResponseEntity<Void> gradeSubmission(
            @PathVariable Long id,
            @RequestParam Double score,
            @RequestParam String feedback,
            Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        submissionService.gradeSubmission(id, score, feedback, currentUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-submissions")
    public ResponseEntity<Page<Submission>> getMySubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        Page<Submission> submissions = submissionService.getMySubmissions(currentUser, PageRequest.of(page, size));
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/all-submissions")
    public ResponseEntity<Page<Submission>> getAllSubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        Page<Submission> submissions = submissionService.getAllSubmissions(PageRequest.of(page, size));
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Submission> getSubmission(
            @PathVariable Long id,
            Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        Submission submission = submissionService.getSubmission(id, currentUser);
        return ResponseEntity.ok(submission);
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    public ResponseEntity<Page<Submission>> getAssignmentSubmissions(
            @PathVariable Long assignmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        Page<Submission> submissions = submissionService.getAssignmentSubmissions(assignmentId, 
                PageRequest.of(page, size), currentUser);
        return ResponseEntity.ok(submissions);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubmission(
            @PathVariable Long id,
            Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        submissionService.deleteSubmission(id, currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/batch-grade")
    public ResponseEntity<BatchGradeResponseDTO> batchGrade(
            @RequestBody BatchGradeRequestDTO request,
            Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        BatchGradeResponseDTO response = submissionService.batchGrade(request, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignments/{assignmentId}/statistics")
    public ResponseEntity<SubmissionStatisticsDTO> getSubmissionStatistics(
            @PathVariable Long assignmentId,
            Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        SubmissionStatisticsDTO statistics = submissionService.getSubmissionStatistics(assignmentId, currentUser);
        return ResponseEntity.ok(statistics);
    }
}
