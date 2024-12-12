package com.kit.sharingsystem.service;

import com.kit.sharingsystem.entity.User;
import com.kit.sharingsystem.entity.UserRole;
import com.kit.sharingsystem.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(User user) {
        // 设置默认角色为学生
        if (user.getRole() == null) {
            user.setRole(UserRole.STUDENT);
        }
        
        // 验证必填字段
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("姓名不能为空");
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        userMapper.insert(user);
        return user;
    }

    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    public User getUserByUsername(String username) {
        User user = findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found with username: " + username);
        }
        return user;
    }

    public boolean existsByUsername(String username) {
        return userMapper.countByUsername(username) > 0;
    }

    public User findById(Long id) {
        return userMapper.findById(id);
    }

    @Transactional
    public void updateUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.update(user);
    }
}
