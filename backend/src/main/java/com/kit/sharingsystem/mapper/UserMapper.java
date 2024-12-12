package com.kit.sharingsystem.mapper;

import com.kit.sharingsystem.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    @Insert("INSERT INTO users (username, password, name, email, role, phone, created_at, updated_at) " +
            "VALUES (#{username}, #{password}, #{name}, #{email}, #{role}, #{phone}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Select("SELECT id, username, password, name, email, role, phone, created_at as createdAt, updated_at as updatedAt " +
            "FROM users WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    @Select("SELECT id, username, password, name, email, role, phone, created_at as createdAt, updated_at as updatedAt " +
            "FROM users WHERE id = #{id}")
    User findById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM users WHERE username = #{username}")
    int countByUsername(@Param("username") String username);

    @Update("UPDATE users SET password = #{password}, name = #{name}, " +
            "email = #{email}, role = #{role}, phone = #{phone}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    void update(User user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(@Param("id") Long id);
}
