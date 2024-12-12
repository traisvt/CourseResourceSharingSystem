package com.kit.sharingsystem.mapper;

import com.kit.sharingsystem.entity.Submission;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SubmissionMapper {

    @Insert("INSERT INTO submissions (assignment_id, student_id, file_path, filename, submitted_at) " +
            "VALUES (#{assignment.id}, #{student.id}, #{filePath}, #{filename}, #{submittedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Submission submission);

    @Update("UPDATE submissions SET file_path = #{filePath}, filename = #{filename}, " +
            "submitted_at = #{submittedAt}, score = #{score}, feedback = #{feedback} " +
            "WHERE id = #{id}")
    void update(Submission submission);

    @Select("SELECT s.*, " +
            "a.id as assignment_id, a.title as assignment_title, " +
            "u.id as student_id, u.username as student_username, u.name as student_name " +
            "FROM submissions s " +
            "JOIN assignments a ON s.assignment_id = a.id " +
            "JOIN users u ON s.student_id = u.id " +
            "WHERE s.id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "assignment.id", column = "assignment_id"),
            @Result(property = "assignment.title", column = "assignment_title"),
            @Result(property = "student.id", column = "student_id"),
            @Result(property = "student.username", column = "student_username"),
            @Result(property = "student.name", column = "student_name"),
            @Result(property = "filePath", column = "file_path"),
            @Result(property = "filename", column = "filename"),
            @Result(property = "submittedAt", column = "submitted_at"),
            @Result(property = "score", column = "score"),
            @Result(property = "feedback", column = "feedback")
    })
    Submission findById(Long id);

    @Select("SELECT s.*, " +
            "a.id as assignment_id, a.title as assignment_title, " +
            "u.id as student_id, u.username as student_username, u.name as student_name " +
            "FROM submissions s " +
            "JOIN assignments a ON s.assignment_id = a.id " +
            "JOIN users u ON s.student_id = u.id " +
            "WHERE s.student_id = #{studentId} " +
            "ORDER BY s.submitted_at DESC " +
            "LIMIT #{offset}, #{size}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "assignment.id", column = "assignment_id"),
            @Result(property = "assignment.title", column = "assignment_title"),
            @Result(property = "student.id", column = "student_id"),
            @Result(property = "student.username", column = "student_username"),
            @Result(property = "student.name", column = "student_name"),
            @Result(property = "filePath", column = "file_path"),
            @Result(property = "filename", column = "filename"),
            @Result(property = "submittedAt", column = "submitted_at"),
            @Result(property = "score", column = "score"),
            @Result(property = "feedback", column = "feedback")
    })
    List<Submission> findByStudentIdWithPaging(@Param("studentId") Long studentId,
                                              @Param("offset") int offset,
                                              @Param("size") int size);

    @Select("SELECT COUNT(*) FROM submissions WHERE student_id = #{studentId}")
    long countByStudentId(Long studentId);

    @Select("SELECT s.*, " +
            "a.id as assignment_id, a.title as assignment_title, " +
            "u.id as student_id, u.username as student_username, u.name as student_name " +
            "FROM submissions s " +
            "JOIN assignments a ON s.assignment_id = a.id " +
            "JOIN users u ON s.student_id = u.id " +
            "ORDER BY s.submitted_at DESC " +
            "LIMIT #{offset}, #{size}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "assignment.id", column = "assignment_id"),
            @Result(property = "assignment.title", column = "assignment_title"),
            @Result(property = "student.id", column = "student_id"),
            @Result(property = "student.username", column = "student_username"),
            @Result(property = "student.name", column = "student_name"),
            @Result(property = "filePath", column = "file_path"),
            @Result(property = "filename", column = "filename"),
            @Result(property = "submittedAt", column = "submitted_at"),
            @Result(property = "score", column = "score"),
            @Result(property = "feedback", column = "feedback")
    })
    List<Submission> findAllWithPaging(@Param("offset") int offset,
                                      @Param("size") int size);

    @Select("SELECT COUNT(*) FROM submissions")
    long count();

    @Select("SELECT s.*, " +
            "a.id as assignment_id, a.title as assignment_title, " +
            "u.id as student_id, u.username as student_username, u.name as student_name " +
            "FROM submissions s " +
            "JOIN assignments a ON s.assignment_id = a.id " +
            "JOIN users u ON s.student_id = u.id " +
            "WHERE s.assignment_id = #{assignmentId} AND s.student_id = #{studentId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "assignment.id", column = "assignment_id"),
            @Result(property = "assignment.title", column = "assignment_title"),
            @Result(property = "student.id", column = "student_id"),
            @Result(property = "student.username", column = "student_username"),
            @Result(property = "student.name", column = "student_name"),
            @Result(property = "filePath", column = "file_path"),
            @Result(property = "filename", column = "filename"),
            @Result(property = "submittedAt", column = "submitted_at"),
            @Result(property = "score", column = "score"),
            @Result(property = "feedback", column = "feedback")
    })
    Submission findByAssignmentIdAndStudentId(@Param("assignmentId") Long assignmentId,
                                             @Param("studentId") Long studentId);

    @Delete("DELETE FROM submissions WHERE id = #{id}")
    void deleteById(@Param("id") Long id);

    @Delete("DELETE FROM submissions WHERE assignment_id = #{assignmentId}")
    void deleteByHomeworkId(@Param("assignmentId") Long assignmentId);

    @Select("SELECT s.*, " +
            "a.id as assignment_id, a.title as assignment_title, " +
            "u.id as student_id, u.username as student_username, u.name as student_name " +
            "FROM submissions s " +
            "JOIN assignments a ON s.assignment_id = a.id " +
            "JOIN users u ON s.student_id = u.id " +
            "WHERE s.assignment_id = #{assignmentId} " +
            "ORDER BY s.submitted_at DESC " +
            "LIMIT #{offset}, #{size}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "assignment.id", column = "assignment_id"),
            @Result(property = "assignment.title", column = "assignment_title"),
            @Result(property = "student.id", column = "student_id"),
            @Result(property = "student.username", column = "student_username"),
            @Result(property = "student.name", column = "student_name"),
            @Result(property = "filePath", column = "file_path"),
            @Result(property = "filename", column = "filename"),
            @Result(property = "submittedAt", column = "submitted_at"),
            @Result(property = "score", column = "score"),
            @Result(property = "feedback", column = "feedback")
    })
    List<Submission> findByAssignmentIdWithPaging(@Param("assignmentId") Long assignmentId,
                                                 @Param("offset") int offset,
                                                 @Param("size") int size);

    @Select("SELECT COUNT(*) FROM submissions WHERE assignment_id = #{assignmentId}")
    long countByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Select("SELECT s.*, " +
            "a.id as assignment_id, a.title as assignment_title, " +
            "u.id as student_id, u.username as student_username, u.name as student_name " +
            "FROM submissions s " +
            "JOIN assignments a ON s.assignment_id = a.id " +
            "JOIN users u ON s.student_id = u.id " +
            "WHERE s.assignment_id = #{assignmentId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "assignment.id", column = "assignment_id"),
            @Result(property = "assignment.title", column = "assignment_title"),
            @Result(property = "student.id", column = "student_id"),
            @Result(property = "student.username", column = "student_username"),
            @Result(property = "student.name", column = "student_name"),
            @Result(property = "filePath", column = "file_path"),
            @Result(property = "filename", column = "filename"),
            @Result(property = "submittedAt", column = "submitted_at"),
            @Result(property = "score", column = "score"),
            @Result(property = "feedback", column = "feedback")
    })
    List<Submission> findByAssignmentId(@Param("assignmentId") Long assignmentId);
}
