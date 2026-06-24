package com.dormitory.service;

import com.dormitory.entity.Accommodation;
import com.dormitory.entity.Dormitory;
import com.dormitory.entity.User;
import com.dormitory.repository.AccommodationRepository;
import com.dormitory.repository.DormitoryRepository;
import com.dormitory.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 住宿信息服务
 */
@Service
public class AccommodationService {
    
    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private DormitoryRepository dormitoryRepository;

    @Autowired
    private UserRepository userRepository;
    
    /**
     * 获取所有住宿信息
     */
    public List<Accommodation> getAllAccommodations() {
        return accommodationRepository.findAll();
    }
    
    /**
     * 根据ID获取住宿信息
     */
    public Optional<Accommodation> getAccommodationById(Long id) {
        return accommodationRepository.findById(id);
    }
    
    /**
     * 搜索住宿信息（支持学号或姓名）
     */
    public List<Accommodation> searchAccommodations(String studentName, String dormitory) {
        if ((studentName == null || studentName.trim().isEmpty()) && 
            (dormitory == null || dormitory.trim().isEmpty())) {
            return getAllAccommodations();
        }
        
        String studentNameParam = studentName == null ? "" : studentName.trim();
        String dormitoryParam = dormitory == null ? "" : dormitory.trim();
        
        return accommodationRepository.findByStudentNameAndDormitoryContaining(studentNameParam, dormitoryParam);
    }

    /**
     * 根据学号搜索住宿信息
     */
    public List<Accommodation> searchAccommodationsByStudentId(String studentId, String dormitory) {
        if ((studentId == null || studentId.trim().isEmpty()) && 
            (dormitory == null || dormitory.trim().isEmpty())) {
            return getAllAccommodations();
        }
        
        String studentIdParam = studentId == null ? "" : studentId.trim();
        String dormitoryParam = dormitory == null ? "" : dormitory.trim();
        
        return accommodationRepository.findByStudentIdAndDormitoryContaining(studentIdParam, dormitoryParam);
    }
    
    /**
     * 添加住宿信息
     */
    @Transactional
    public Accommodation addAccommodation(Accommodation accommodation) {
        // 1. 验证学生是否存在且角色为学生
        // 1. 验证学生是否存在且角色为学生
        // 注意：允许学生重名，所以这里必须优先用学号定位学生
        User student = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.UserRole.STUDENT)
                .filter(u -> accommodation.getStudentId() != null && accommodation.getStudentId().equals(u.getStudentId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("学生学号 '" + accommodation.getStudentId() + "' 不存在"));
        // 2. 验证学生是否已入住（按学号判断，避免重名误判）
        if (accommodation.getStudentId() != null && accommodationRepository.findByStudentId(accommodation.getStudentId()).isPresent()) {
            throw new RuntimeException("该学生已入住，不能重复添加");
        }

        // 3. 验证宿舍是否存在
        Dormitory dormitory = dormitoryRepository.findByNumber(accommodation.getDormitory())
                .orElseThrow(() -> new RuntimeException("宿舍不存在"));
        
        // 验证宿舍楼和寝室是否匹配
        if (accommodation.getBuilding() != null && !accommodation.getBuilding().equals(dormitory.getBuilding())) {
            throw new RuntimeException("宿舍楼和寝室不匹配");
        }
        
        // 验证宿舍人数是否超过容纳数
        if (dormitory.getOccupied() >= dormitory.getCapacity()) {
            throw new RuntimeException("宿舍已满，无法添加新成员");
        }
        
        // 设置宿舍楼（如果未设置）
        if (accommodation.getBuilding() == null || accommodation.getBuilding().isEmpty()) {
            accommodation.setBuilding(dormitory.getBuilding());
        }
        
        // 更新宿舍入住人数
        dormitory.setOccupied(dormitory.getOccupied() + 1);
        dormitoryRepository.save(dormitory);
        
        return accommodationRepository.save(accommodation);
    }
    
    /**
     * 更新住宿信息
     */
    public Accommodation updateAccommodation(Accommodation accommodation) {
        return accommodationRepository.save(accommodation);
    }
    
    /**
     * 删除住宿信息
     */
    @Transactional
    public void deleteAccommodation(Long id) {
        // 先查找住宿信息
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("住宿信息不存在"));

        // 更新宿舍入住人数
        Dormitory dormitory = dormitoryRepository.findByNumber(accommodation.getDormitory())
                .orElse(null); // 如果宿舍不存在，也允许删除
        
        if (dormitory != null) {
            dormitory.setOccupied(Math.max(0, dormitory.getOccupied() - 1)); // 防止出现负数
            dormitoryRepository.save(dormitory);
        }
        
        accommodationRepository.deleteById(id);
    }
    
    /**
     * 根据学生姓名搜索住宿信息
     */
    public List<Accommodation> searchAccommodationsByStudentName(String studentName) {
        if (studentName == null || studentName.trim().isEmpty()) {
            return getAllAccommodations();
        }
        return accommodationRepository.findByStudentNameContaining(studentName.trim());
    }
    
    /**
     * 根据宿舍号搜索住宿信息
     */
    public List<Accommodation> searchAccommodationsByDormitory(String dormitory) {
        if (dormitory == null || dormitory.trim().isEmpty()) {
            return getAllAccommodations();
        }
        return accommodationRepository.findByDormitoryContaining(dormitory.trim());
    }
}
