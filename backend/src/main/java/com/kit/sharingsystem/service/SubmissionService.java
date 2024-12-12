package com.kit.sharingsystem.service;

import com.kit.sharingsystem.dto.*;
import com.kit.sharingsystem.entity.Assignment;
import com.kit.sharingsystem.entity.Submission;
import com.kit.sharingsystem.entity.User;
import com.kit.sharingsystem.exception.ResourceNotFoundException;
import com.kit.sharingsystem.exception.UnauthorizedException;
import com.kit.sharingsystem.mapper.AssignmentMapper;
import com.kit.sharingsystem.mapper.SubmissionMapper;
import com.kit.sharingsystem.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionService {
    private final SubmissionMapper submissionMapper;
    private final AssignmentMapper assignmentMapper;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;

    @Transactional
    public Submission submit(Long assignmentId, MultipartFile file, User currentUser) throws IOException {
        Assignment assignment = assignmentMapper.findById(assignmentId);
        if (assignment == null) {
            throw new ResourceNotFoundException("Assignment not found");
        }

        // Check if student has already submitted
        Submission existingSubmission = submissionMapper.findByAssignmentIdAndStudentId(assignmentId, currentUser.getId());
        if (existingSubmission != null) {
            // Delete old file
            fileStorageService.deleteFile(existingSubmission.getFilePath());
        }

        // Save new file
        String filePath = fileStorageService.storeFile(file);

        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(currentUser);
        submission.setFilePath(filePath);
        submission.setFilename(file.getOriginalFilename());
        submission.setSubmittedAt(LocalDateTime.now());

        if (existingSubmission != null) {
            submission.setId(existingSubmission.getId());
            submissionMapper.update(submission);
        } else {
            submissionMapper.insert(submission);
        }

        return submission;
    }

    public Page<Submission> getMySubmissions(User currentUser, Pageable pageable) {
        List<Submission> submissions = submissionMapper.findByStudentIdWithPaging(
                currentUser.getId(),
                pageable.getPageNumber() * pageable.getPageSize(),
                pageable.getPageSize()
        );
        long total = submissionMapper.countByStudentId(currentUser.getId());
        return new PageImpl<>(submissions, pageable, total);
    }

    public Page<Submission> getAllSubmissions(Pageable pageable) {
        List<Submission> submissions = submissionMapper.findAllWithPaging(
                pageable.getPageNumber() * pageable.getPageSize(),
                pageable.getPageSize()
        );
        long total = submissionMapper.count();
        return new PageImpl<>(submissions, pageable, total);
    }

    public Resource loadSubmissionFile(Long id, User currentUser) throws IOException {
        Submission submission = submissionMapper.findById(id);
        if (submission == null) {
            throw new ResourceNotFoundException("Submission not found");
        }

        // Check permission
        if (!currentUser.isTeacher() && !submission.getStudent().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have permission to download this submission");
        }

        return fileStorageService.loadFileAsResource(submission.getFilePath());
    }

    @Transactional
    public void gradeSubmission(Long id, Double score, String feedback, User currentUser) {
        if (!currentUser.isTeacher()) {
            throw new UnauthorizedException("Only teachers can grade submissions");
        }

        Submission submission = submissionMapper.findById(id);
        if (submission == null) {
            throw new ResourceNotFoundException("Submission not found");
        }

        submission.setScore(score.floatValue());
        submission.setFeedback(feedback);
        submissionMapper.update(submission);
    }

    public Submission getSubmission(Long id, User currentUser) {
        Submission submission = submissionMapper.findById(id);
        if (submission == null) {
            throw new ResourceNotFoundException("Submission not found");
        }

        // Check permission
        if (!currentUser.isTeacher() && !submission.getStudent().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have permission to view this submission");
        }

        return submission;
    }

    public Page<Submission> getAssignmentSubmissions(Long assignmentId, Pageable pageable, User currentUser) {
        if (!currentUser.isTeacher()) {
            throw new UnauthorizedException("Only teachers can view all submissions for an assignment");
        }

        Assignment assignment = assignmentMapper.findById(assignmentId);
        if (assignment == null) {
            throw new ResourceNotFoundException("Assignment not found");
        }

        List<Submission> submissions = submissionMapper.findByAssignmentIdWithPaging(
                assignmentId,
                pageable.getPageNumber() * pageable.getPageSize(),
                pageable.getPageSize()
        );
        long total = submissionMapper.countByAssignmentId(assignmentId);
        return new PageImpl<>(submissions, pageable, total);
    }

    @Transactional
    public void deleteSubmission(Long id, User currentUser) {
        Submission submission = submissionMapper.findById(id);
        if (submission == null) {
            throw new ResourceNotFoundException("Submission not found");
        }

        // Check permission
        if (!currentUser.isTeacher() && !submission.getStudent().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have permission to delete this submission");
        }

        // Delete file
        fileStorageService.deleteFile(submission.getFilePath());

        // Delete submission record
        submissionMapper.deleteById(id);
    }

    @Transactional
    public BatchGradeResponseDTO batchGrade(BatchGradeRequestDTO request, User currentUser) {
        if (!currentUser.isTeacher()) {
            throw new UnauthorizedException("Only teachers can perform batch grading");
        }

        BatchGradeResponseDTO response = new BatchGradeResponseDTO();
        List<Long> successIds = new ArrayList<>();
        Map<Long, String> failures = new HashMap<>();

        for (BatchGradeRequestDTO.GradeInfo gradeInfo : request.getGrades()) {
            try {
                Submission submission = submissionMapper.findById(gradeInfo.getSubmissionId());
                if (submission == null) {
                    failures.put(gradeInfo.getSubmissionId(), "Submission not found");
                    continue;
                }

                submission.setScore(gradeInfo.getScore().floatValue());
                submission.setFeedback(gradeInfo.getFeedback());
                submissionMapper.update(submission);
                successIds.add(gradeInfo.getSubmissionId());
            } catch (Exception e) {
                failures.put(gradeInfo.getSubmissionId(), e.getMessage());
            }
        }

        response.setSuccessIds(successIds);
        response.setFailures(failures);
        return response;
    }

    public SubmissionStatisticsDTO getSubmissionStatistics(Long assignmentId, User currentUser) {
        if (!currentUser.isTeacher()) {
            throw new UnauthorizedException("Only teachers can view submission statistics");
        }

        Assignment assignment = assignmentMapper.findById(assignmentId);
        if (assignment == null) {
            throw new ResourceNotFoundException("Assignment not found");
        }

        List<Submission> submissions = submissionMapper.findByAssignmentId(assignmentId);
        
        SubmissionStatisticsDTO statistics = new SubmissionStatisticsDTO();
        statistics.setTotalSubmissions(submissions.size());
        statistics.setGradedCount((int) submissions.stream()
                .filter(s -> s.getScore() != null)
                .count());
        statistics.setUngradedCount((int) submissions.stream()
                .filter(s -> s.getScore() == null)
                .count());

        if (!submissions.isEmpty()) {
            DoubleSummaryStatistics scoreStats = submissions.stream()
                    .filter(s -> s.getScore() != null)
                    .mapToDouble(s -> s.getScore())
                    .summaryStatistics();

            statistics.setAverageScore(scoreStats.getAverage());
            statistics.setHighestScore(scoreStats.getMax());
            statistics.setLowestScore(scoreStats.getMin());

            // Calculate score distribution
            Map<String, Integer> distribution = new HashMap<>();
            submissions.stream()
                    .filter(s -> s.getScore() != null)
                    .forEach(s -> {
                        String range = getScoreRange(s.getScore());
                        distribution.merge(range, 1, Integer::sum);
                    });
            statistics.setScoreDistribution(distribution);
        }

        return statistics;
    }

    private String getScoreRange(double score) {
        if (score >= 90) return "90-100";
        if (score >= 80) return "80-89";
        if (score >= 70) return "70-79";
        if (score >= 60) return "60-69";
        return "0-59";
    }
}
