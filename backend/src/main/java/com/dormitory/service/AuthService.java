package com.dormitory.service;

import com.dormitory.entity.User;
import com.dormitory.repository.UserRepository;
import com.dormitory.util.JwtUtil;
import com.dormitory.util.VerificationCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证服务
 */
@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private VerificationCodeUtil verificationCodeUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 模拟存储验证码（实际生产中应使用Redis）
    // Key: email, Value: code
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();
    
    /**
     * 用户登录
     */
    public Map<String, Object> login(String username, String password, String studentId, String role) {
        Map<String, Object> result = new HashMap<>();
        
        // 查找用户
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        
        User user = userOpt.get();
        
        // 验证密码（使用BCrypt验证加密后的密码）
        if (!passwordEncoder.matches(password, user.getPassword())) {
            result.put("success", false);
            result.put("message", "密码错误");
            return result;
        }
        
        // 验证工号或学号
        if (studentId == null || !studentId.equals(user.getStudentId())) {
            result.put("success", false);
            result.put("message", "工号或学号错误");
            return result;
        }
        
        // 验证角色（严格匹配，不区分大小写）
        String userRoleName = user.getRole().name().toUpperCase(); // ADMIN 或 STUDENT
        String requestRole = role != null ? role.toUpperCase() : ""; // 前端发送的角色转换为大写
        
        if (!userRoleName.equals(requestRole)) {
            result.put("success", false);
            result.put("message", "角色不匹配：用户角色为 " + user.getRole().name() + "，但选择了 " + role);
            return result;
        }
        
        // 生成JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("id", user.getId());
        result.put("token", token);
        result.put("username", user.getUsername());
        result.put("name", user.getName());
        result.put("role", user.getRole().name());
        result.put("studentId", user.getStudentId()); // 添加学号/工号
        result.put("phone", user.getPhone());
        result.put("email", user.getEmail());
        result.put("avatar", user.getAvatar());
        
        return result;
    }

    /**
     * 发送验证码
     */
    public String sendVerificationCode(String email) {
        // 简单校验邮箱格式
        if (email == null || !email.contains("@")) {
            throw new RuntimeException("邮箱格式不正确");
        }

        String code = verificationCodeUtil.generateCode();
        verificationCodes.put(email, code);
        
        // 模拟发送邮件（实际应调用邮件服务）
        System.out.println("向邮箱 " + email + " 发送验证码: " + code);
        
        return "验证码已发送（模拟：请查看控制台输出）";
    }
    
    /**
     * 用户注册
     */
    public String register(User user, String verificationCode) {
        // 检查验证码
        String storedCode = verificationCodes.get(user.getEmail());
        if (storedCode == null || !storedCode.equals(verificationCode)) {
            return "验证码错误或已过期";
        }

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            return "用户名已存在";
        }
        
        // 设置默认角色为学生
        if (user.getRole() == null) {
            user.setRole(User.UserRole.STUDENT);
        }

        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 保存用户
        userRepository.save(user);
        
        // 注册成功后清除验证码
        verificationCodes.remove(user.getEmail());
        
        return "注册成功";
    }
    
    /**
     * 验证用户凭据（用户名和密码）
     */
    public boolean validateCredentials(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return false;
        }
        User user = userOpt.get();
        return passwordEncoder.matches(password, user.getPassword());
    }
    
    /**
     * 根据角色统计用户数量
     */
    public long countUsersByRole(User.UserRole role) {
        return userRepository.countByRole(role);
    }
    
    /**
     * 检查用户名是否存在
     */
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 重置管理员密码
     */
    public String resetAdminPassword(String username, String email, String phone, String newPassword, String verificationCode) {
        // 校验验证码
        String storedCode = verificationCodes.get(email);
        if (storedCode == null || !storedCode.equals(verificationCode)) {
            return "验证码错误或已过期";
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return "管理员账号不存在";
        }

        User user = userOpt.get();
        if (user.getRole() != User.UserRole.ADMIN) {
            return "该用户不是管理员";
        }

        if (!email.equals(user.getEmail())) {
            return "邮箱不匹配";
        }

        if (!phone.equals(user.getPhone())) {
            return "手机号不匹配";
        }

        // 密码加密
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 清除验证码
        verificationCodes.remove(email);

        return "密码重置成功";
    }
}
