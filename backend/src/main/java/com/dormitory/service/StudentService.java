package com.dormitory.service;

import com.dormitory.entity.User;
import com.dormitory.repository.UserRepository;
import com.dormitory.repository.AccommodationRepository;
import com.dormitory.repository.DormitoryRepository;
import com.dormitory.entity.Accommodation;
import com.dormitory.entity.Dormitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 学生服务
 */
@Service
public class StudentService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private DormitoryRepository dormitoryRepository;
    
    /**
     * 获取所有学生
     */
    public List<User> getAllStudents() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.UserRole.STUDENT)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 根据ID获取学生
     */
    public Optional<User> getStudentById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * 搜索学生
     */
    public List<User> searchStudents(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStudents();
        }
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.UserRole.STUDENT)
                .filter(user -> user.getUsername().contains(keyword) || user.getName().contains(keyword))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 添加学生
     */
    public User addStudent(User student) {
        student.setRole(User.UserRole.STUDENT);

        // 允许姓名重名，但学号必须唯一
        if (student.getStudentId() != null && userRepository.existsByStudentId(student.getStudentId())) {
            throw new RuntimeException("该学号已存在");
        }

        return userRepository.save(student);
    }
    
    /**
     * 更新学生
     */
    public User updateStudent(User student) {
        // 查找现有学生
        return userRepository.findById(student.getId())
                .map(existingStudent -> {
                    // 更新基本信息
                    existingStudent.setUsername(student.getUsername());
                    existingStudent.setName(student.getName());
                    existingStudent.setStudentId(student.getStudentId());
                    existingStudent.setPhone(student.getPhone());
                    existingStudent.setEmail(student.getEmail());
                    
                    // 只有当密码不为空时才更新密码
                    if (student.getPassword() != null && !student.getPassword().trim().isEmpty()) {
                        existingStudent.setPassword(student.getPassword());
                    }
                    
                    // 确保角色保持为学生
                    existingStudent.setRole(User.UserRole.STUDENT);
                    
                    return userRepository.save(existingStudent);
                })
                .orElseThrow(() -> new RuntimeException("学生不存在"));
    }
    
    /**
     * 删除学生
     */
    @Transactional
    public void deleteStudent(Long id) {
        // 先查学生，拿到学号
        User student = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生不存在"));

        String studentId = student.getStudentId();

        // 若该学生已存在住宿记录，先删除住宿记录并将对应宿舍人数-1
        if (studentId != null) {
            accommodationRepository.findByStudentId(studentId).ifPresent(accommodation -> {
                // 更新宿舍入住人数
                Dormitory dormitory = dormitoryRepository.findByNumber(accommodation.getDormitory()).orElse(null);
                if (dormitory != null) {
                    dormitory.setOccupied(Math.max(0, dormitory.getOccupied() - 1));
                    dormitoryRepository.save(dormitory);
                }
                // 删除住宿记录（释放床位）
                accommodationRepository.deleteById(accommodation.getId());
            });
        }

        // 最后删除学生用户
        userRepository.deleteById(id);
    }
    
    /**
     * 批量删除学生
     */
    public void deleteStudents(List<Long> ids) {
        userRepository.deleteAllById(ids);
    }
}
