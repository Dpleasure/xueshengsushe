package com.dormitory.service;

import com.dormitory.entity.Dormitory;
import com.dormitory.entity.Building;
import com.dormitory.repository.DormitoryRepository;
import com.dormitory.repository.BuildingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 宿舍服务
 */
@Service
public class DormitoryService {
    
    @Autowired
    private DormitoryRepository dormitoryRepository;

    @Autowired
    private BuildingRepository buildingRepository;
    
    /**
     * 获取所有宿舍
     */
    public List<Dormitory> getAllDormitories() {
        return dormitoryRepository.findAll();
    }
    
    /**
     * 根据ID获取宿舍
     */
    public Optional<Dormitory> getDormitoryById(Long id) {
        return dormitoryRepository.findById(id);
    }
    
    /**
     * 搜索宿舍
     */
    public List<Dormitory> searchDormitories(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllDormitories();
        }
        return dormitoryRepository.findByNumberOrBuildingContaining(keyword.trim());
    }
    
    /**
     * 根据宿舍楼获取宿舍
     */
    public List<Dormitory> getDormitoriesByBuilding(String building) {
        return dormitoryRepository.findByBuilding(building);
    }
    
    /**
     * 添加宿舍
     */
    public Dormitory addDormitory(Dormitory dormitory) {
        if (dormitory.getNumber() != null && dormitoryRepository.existsByNumber(dormitory.getNumber().trim())) {
            throw new RuntimeException("此宿舍已存在");
        }

        // 楼栋最大宿舍数约束：building.capacity 作为该楼栋最多允许的宿舍数量
        String buildingName = dormitory.getBuilding();
        if (buildingName != null && !buildingName.trim().isEmpty()) {
            Building building = buildingRepository.findByName(buildingName.trim())
                    .orElseThrow(() -> new RuntimeException("楼栋不存在"));

            long currentDormCount = dormitoryRepository.countByBuilding(building.getName());
            Integer maxDormCount = building.getCapacity();

            if (maxDormCount != null && currentDormCount >= maxDormCount) {
                throw new RuntimeException("超出最大寝室数：" + building.getName() + " 最多 " + maxDormCount + " 间宿舍");
            }
        }

        return dormitoryRepository.save(dormitory);
    }
    
    /**
     * 更新宿舍
     */
    public Dormitory updateDormitory(Dormitory dormitory) {
        return dormitoryRepository.save(dormitory);
    }
    
    /**
     * 删除宿舍
     */
    public void deleteDormitory(Long id) {
        Dormitory dormitory = dormitoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("寝室不存在"));

        if (dormitory.getOccupied() != null && dormitory.getOccupied() > 0) {
            throw new RuntimeException("该寝室已入住（已住人数>0），不能删除");
        }

        dormitoryRepository.deleteById(id);
    }
    
    /**
     * 批量删除宿舍
     */
    public void deleteDormitories(List<Long> ids) {
        dormitoryRepository.deleteAllById(ids);
    }
    
    /**
     * 根据宿舍号搜索宿舍
     */
    public List<Dormitory> searchDormitoriesByNumber(String number) {
        if (number == null || number.trim().isEmpty()) {
            return getAllDormitories();
        }
        return dormitoryRepository.findByNumberContaining(number.trim());
    }
    
    /**
     * 统计指定宿舍楼的宿舍数量
     */
    public long countDormitoriesByBuilding(String building) {
        return dormitoryRepository.countByBuilding(building);
    }
}
