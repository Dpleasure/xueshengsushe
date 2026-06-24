package com.dormitory.controller;

import com.dormitory.Json.RestBean;
import com.dormitory.entity.Building;
import com.dormitory.entity.Dormitory;
import com.dormitory.entity.Repair;
import com.dormitory.entity.User;
import com.dormitory.repository.BuildingRepository;
import com.dormitory.repository.DormitoryRepository;
import com.dormitory.repository.RepairRepository;
import com.dormitory.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计控制器
 */
@RestController
@RequestMapping("/stats")
@CrossOrigin(origins = "*")
public class StatsController {
    
    @Autowired
    private BuildingRepository buildingRepository;
    
    @Autowired
    private DormitoryRepository dormitoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RepairRepository repairRepository;
    
    /**
     * 获取系统统计数据
     */
    @GetMapping
    public RestBean<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 宿舍楼总数
        long buildingCount = buildingRepository.count();
        stats.put("buildingCount", buildingCount);
        
        // 宿舍总数
        long dormitoryCount = dormitoryRepository.count();
        stats.put("dormitoryCount", dormitoryCount);
        
        // 学生总数
        long studentCount = userRepository.countByRole(User.UserRole.STUDENT);
        stats.put("studentCount", studentCount);
        
        // 报修记录总数
        long repairCount = repairRepository.count();
        stats.put("repairCount", repairCount);
        
        return RestBean.success(stats);
    }
}
