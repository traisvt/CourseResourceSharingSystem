package com.kit.sharingsystem.mapper;

import com.kit.sharingsystem.entity.Assignment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AssignmentMapper {
    @Insert("INSERT INTO assignments (title, description, teacher_id, due_date, created_at) " +
            "VALUES (#{title}, #{description}, #{teacher.id}, #{dueDate}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Assignment assignment);

    @Select("SELECT a.*, " +
            "u.id as 'teacher.id', u.username as 'teacher.username', " +
            "u.name as 'teacher.name', u.email as 'teacher.email', " +
            "u.role as 'teacher.role' " +
            "FROM assignments a " +
            "LEFT JOIN users u ON a.teacher_id = u.id " +
            "WHERE a.id = #{id}")
    Assignment findById(@Param("id") Long id);

    @Select("SELECT a.*, " +
            "u.id as 'teacher.id', u.username as 'teacher.username', " +
            "u.name as 'teacher.name', u.email as 'teacher.email', " +
            "u.role as 'teacher.role' " +
            "FROM assignments a " +
            "LEFT JOIN users u ON a.teacher_id = u.id " +
            "ORDER BY a.created_at DESC")
    List<Assignment> findAll();

    @Select("SELECT a.*, " +
            "u.id as 'teacher.id', u.username as 'teacher.username', " +
            "u.name as 'teacher.name', u.email as 'teacher.email', " +
            "u.role as 'teacher.role' " +
            "FROM assignments a " +
            "LEFT JOIN users u ON a.teacher_id = u.id " +
            "ORDER BY a.created_at DESC " +
            "LIMIT #{size} OFFSET #{offset}")
    List<Assignment> findAllWithPaging(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM assignments")
    long count();

    @Select("SELECT a.*, " +
            "u.id as 'teacher.id', u.username as 'teacher.username', " +
            "u.name as 'teacher.name', u.email as 'teacher.email', " +
            "u.role as 'teacher.role' " +
            "FROM assignments a " +
            "LEFT JOIN users u ON a.teacher_id = u.id " +
            "WHERE a.teacher_id = #{teacherId} " +
            "ORDER BY a.created_at DESC")
    List<Assignment> findByTeacherId(@Param("teacherId") Long teacherId);

    @Select("SELECT a.*, " +
            "u.id as 'teacher.id', u.username as 'teacher.username', " +
            "u.name as 'teacher.name', u.email as 'teacher.email', " +
            "u.role as 'teacher.role' " +
            "FROM assignments a " +
            "LEFT JOIN users u ON a.teacher_id = u.id " +
            "WHERE a.teacher_id = #{teacherId} " +
            "ORDER BY a.created_at DESC " +
            "LIMIT #{size} OFFSET #{offset}")
    List<Assignment> findByTeacherIdWithPaging(@Param("teacherId") Long teacherId,
                                              @Param("offset") int offset,
                                              @Param("size") int size);

    @Select("SELECT COUNT(*) FROM assignments WHERE teacher_id = #{teacherId}")
    long countByTeacherId(@Param("teacherId") Long teacherId);

    @Update("UPDATE assignments SET " +
            "title = #{title}, " +
            "description = #{description}, " +
            "due_date = #{dueDate} " +
            "WHERE id = #{id}")
    void update(Assignment assignment);

    @Delete("DELETE FROM assignments WHERE id = #{id}")
    void deleteById(@Param("id") Long id);
}
