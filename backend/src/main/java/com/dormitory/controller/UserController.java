package com.dormitory.controller;

import com.dormitory.Json.RestBean;
import com.dormitory.entity.User;
import com.dormitory.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024;
    private static final Path AVATAR_DIR = Paths.get("uploads", "avatars");

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/avatar")
    public RestBean<Map<String, Object>> uploadAvatar(@RequestParam("avatar") MultipartFile avatar,
                                                      Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return RestBean.failure(401, null);
        }

        if (avatar == null || avatar.isEmpty()) {
            return RestBean.failure(400, null);
        }

        if (avatar.getSize() > MAX_AVATAR_SIZE) {
            return RestBean.failure(400, null);
        }

        String contentType = avatar.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            return RestBean.failure(400, null);
        }

        Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return RestBean.failure(404, null);
        }

        try {
            Files.createDirectories(AVATAR_DIR);
            String extension = getExtension(avatar.getOriginalFilename(), contentType);
            String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + extension;
            Path target = AVATAR_DIR.resolve(fileName);
            Files.copy(avatar.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String avatarUrl = "/uploads/avatars/" + fileName;
            User user = userOpt.get();
            user.setAvatar(avatarUrl);
            userRepository.save(user);

            Map<String, Object> data = new HashMap<>();
            data.put("avatar", avatarUrl);
            data.put("username", user.getUsername());
            data.put("name", user.getName());
            data.put("role", user.getRole().name());
            data.put("studentId", user.getStudentId());
            return RestBean.success(data);
        } catch (IOException e) {
            return RestBean.failure(500, null);
        }
    }

    private String getExtension(String originalFilename, String contentType) {
        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < originalFilename.length() - 1) {
                String extension = originalFilename.substring(dotIndex).toLowerCase();
                if (extension.matches("\\.(jpg|jpeg|png|gif|webp|bmp)$")) {
                    return extension;
                }
            }
        }

        if (contentType.contains("png")) return ".png";
        if (contentType.contains("gif")) return ".gif";
        if (contentType.contains("webp")) return ".webp";
        if (contentType.contains("bmp")) return ".bmp";
        return ".jpg";
    }
}
