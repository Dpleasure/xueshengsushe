package com.dormitory.service;

import com.dormitory.entity.Repair;
import com.dormitory.repository.RepairRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 报修服务
 */
@Service
public class RepairService {
    
    @Autowired
    private RepairRepository repairRepository;
    
    /**
     * 获取所有报修记录
     */
    public List<Repair> getAllRepairs() {
        return repairRepository.findAll();
    }
    
    /**
     * 根据ID获取报修记录
     */
    public Optional<Repair> getRepairById(Long id) {
        return repairRepository.findById(id);
    }
    
    /**
     * 搜索报修记录
     */
    public List<Repair> searchRepairs(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllRepairs();
        }
        return repairRepository.findByReporterOrDormitoryContaining(keyword.trim());
    }
    
    /**
     * 根据状态获取报修记录
     */
    public List<Repair> getRepairsByStatus(Repair.RepairStatus status) {
        return repairRepository.findByStatus(status);
    }
    
    /**
     * 添加报修记录
     */
    public Repair addRepair(Repair repair) {
        // 可以在这里添加业务逻辑，例如检查用户信息等
        return repairRepository.save(repair);
    }

    /**
     * 删除报修记录
     */
    public void deleteRepair(Long id) {
        repairRepository.deleteById(id);
    }
    
    /**
     * 更新报修记录
     */
    public Repair updateRepair(Repair repair) {
        return repairRepository.save(repair);
    }
    
    /**
     * 更新报修状态
     */
    public Repair updateRepairStatus(Long id, Repair.RepairStatus status) {
        Optional<Repair> repairOpt = repairRepository.findById(id);
        if (repairOpt.isPresent()) {
            Repair repair = repairOpt.get();
            repair.setStatus(status);
            return repairRepository.save(repair);
        }
        return null;
    }
    
    /**
     * 根据报修人搜索报修记录
     */
    public List<Repair> searchRepairsByReporter(String reporter) {
        if (reporter == null || reporter.trim().isEmpty()) {
            return getAllRepairs();
        }
        return repairRepository.findByReporterContaining(reporter.trim());
    }
    
    /**
     * 根据宿舍号搜索报修记录
     */
    public List<Repair> searchRepairsByDormitory(String dormitory) {
        if (dormitory == null || dormitory.trim().isEmpty()) {
            return getAllRepairs();
        }
        return repairRepository.findByDormitoryContaining(dormitory.trim());
    }

    /**
     * 根据学号搜索报修记录
     */
    public List<Repair> findRepairsByStudentId(String studentId) {
        return repairRepository.findByStudentId(studentId);
    }
}
