package com.dormitory;

import com.dormitory.entity.User;
import com.dormitory.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * 学生宿舍管理系统启动类
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication
public class DormitoryManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(DormitoryManagementApplication.class, args);
        System.out.println("=================================");
        System.out.println("学生宿舍管理系统启动成功！");
        System.out.println("=================================");
    }

    /**
     * 启动时自动加密数据库中的明文密码
     */
    @Bean
    public CommandLineRunner encryptPasswords(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            List<User> users = userRepository.findAll();
            for (User user : users) {
                // 简单判断：如果密码长度小于 60（BCrypt加密后通常是60位），则认为是明文，需要加密
                if (user.getPassword().length() < 60) {
                    String encodedPassword = passwordEncoder.encode(user.getPassword());
                    user.setPassword(encodedPassword);
                    userRepository.save(user);
                    System.out.println("已加密用户 " + user.getUsername() + " 的密码");
                }
            }
        };
    }
}
