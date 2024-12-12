package com.kit.sharingsystem.mapper;

import com.kit.sharingsystem.entity.Homework;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface HomeworkMapper {
    @Insert("INSERT INTO assignments (title, description, due_date, teacher_id, created_at) " +
            "VALUES (#{title}, #{description}, #{dueDate}, #{teacher.id}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Homework homework);

    @Select("SELECT a.*, u.* FROM assignments a " +
            "LEFT JOIN users u ON a.teacher_id = u.id " +
            "WHERE a.id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "dueDate", column = "due_date"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "teacher", column = "teacher_id",
                one = @One(select = "com.kit.sharingsystem.mapper.UserMapper.findById"))
    })
    Homework findById(Long id);

    @Select("SELECT a.*, u.* FROM assignments a " +
            "LEFT JOIN users u ON a.teacher_id = u.id " +
            "ORDER BY a.created_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "dueDate", column = "due_date"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "teacher", column = "teacher_id",
                one = @One(select = "com.kit.sharingsystem.mapper.UserMapper.findById"))
    })
    List<Homework> findAll();

    @Select("SELECT a.*, u.* FROM assignments a " +
            "LEFT JOIN users u ON a.teacher_id = u.id " +
            "WHERE a.teacher_id = #{teacherId} " +
            "ORDER BY a.created_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "dueDate", column = "due_date"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "teacher", column = "teacher_id",
                one = @One(select = "com.kit.sharingsystem.mapper.UserMapper.findById"))
    })
    List<Homework> findByTeacherId(Long teacherId);

    @Update("UPDATE assignments SET title = #{title}, description = #{description}, " +
            "due_date = #{dueDate} WHERE id = #{id}")
    void update(Homework homework);

    @Delete("DELETE FROM assignments WHERE id = #{id}")
    void delete(Long id);
}
