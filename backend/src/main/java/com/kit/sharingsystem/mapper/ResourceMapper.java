package com.kit.sharingsystem.mapper;

import com.kit.sharingsystem.entity.Resource;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ResourceMapper {
    @Insert({
        "INSERT INTO resources (title, description, file_type, file_size, file_path, uploader_id, created_at, updated_at, download_count)",
        "VALUES (#{title}, #{description}, #{fileType}, #{fileSize}, #{filePath}, #{uploader.id}, #{createdAt}, #{updatedAt}, #{downloadCount})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Resource resource);

    @Select("SELECT * FROM resources WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "uploader", column = "uploader_id",
            one = @One(select = "com.kit.sharingsystem.mapper.UserMapper.findById"))
    })
    Resource findById(Long id);

    @Select("SELECT * FROM resources")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "uploader", column = "uploader_id",
            one = @One(select = "com.kit.sharingsystem.mapper.UserMapper.findById"))
    })
    List<Resource> findAll();

    @Select("SELECT * FROM resources WHERE uploader_id = #{uploaderId}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "uploader", column = "uploader_id",
            one = @One(select = "com.kit.sharingsystem.mapper.UserMapper.findById"))
    })
    List<Resource> findByUploaderId(Long uploaderId);

    @Select("SELECT * FROM resources WHERE title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%')")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "uploader", column = "uploader_id",
            one = @One(select = "com.kit.sharingsystem.mapper.UserMapper.findById"))
    })
    List<Resource> searchByKeyword(String keyword);

    @Select("SELECT * FROM resources LIMIT #{size} OFFSET #{offset}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "uploader", column = "uploader_id",
            one = @One(select = "com.kit.sharingsystem.mapper.UserMapper.findById"))
    })
    List<Resource> getAllResourcesPaged(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM resources")
    long countAllResources();

    @Select("SELECT * FROM resources WHERE title LIKE CONCAT('%', #{query}, '%') OR description LIKE CONCAT('%', #{query}, '%') LIMIT #{size} OFFSET #{offset}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "uploader", column = "uploader_id",
            one = @One(select = "com.kit.sharingsystem.mapper.UserMapper.findById"))
    })
    List<Resource> searchResourcesPaged(@Param("query") String query, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM resources WHERE title LIKE CONCAT('%', #{query}, '%') OR description LIKE CONCAT('%', #{query}, '%')")
    long countSearchResources(@Param("query") String query);

    @Update({
        "UPDATE resources",
        "SET title = #{title},",
        "    description = #{description},",
        "    updated_at = #{updatedAt},",
        "    download_count = #{downloadCount}",
        "WHERE id = #{id}"
    })
    void update(Resource resource);

    @Delete("DELETE FROM resources WHERE id = #{id}")
    void deleteById(Long id);
}
