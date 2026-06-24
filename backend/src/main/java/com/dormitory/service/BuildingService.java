package com.dormitory.service;

import com.dormitory.entity.Building;
import com.dormitory.repository.BuildingRepository;
import com.dormitory.repository.DormitoryRepository;
import com.dormitory.repository.AccommodationRepository;
import com.dormitory.repository.RepairRepository;
import com.dormitory.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

/**
 * 宿舍楼服务
 */
@Service
public class BuildingService {
    
    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private DormitoryRepository dormitoryRepository;

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private RepairRepository repairRepository;

    @Autowired
    private VisitRepository visitRepository;
    
    /**
     * 获取所有宿舍楼
     */
    public List<Building> getAllBuildings() {
        return buildingRepository.findAll();
    }
    
    /**
     * 根据ID获取宿舍楼
     */
    public Optional<Building> getBuildingById(Long id) {
        return buildingRepository.findById(id);
    }
    
    /**
     * 搜索宿舍楼
     */
    public List<Building> searchBuildings(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBuildings();
        }
        return buildingRepository.findByNameOrLocationContaining(keyword.trim());
    }
    
    /**
     * 添加宿舍楼
     */
    public Building addBuilding(Building building) {
        if (building.getName() != null && buildingRepository.existsByName(building.getName().trim())) {
            throw new RuntimeException("该楼栋已存在");
        }
        return buildingRepository.save(building);
    }
    
    /**
     * 更新宿舍楼
     */
    public Building updateBuilding(Building building) {
        return buildingRepository.save(building);
    }
    
    /**
     * 删除宿舍楼
     */
    @Transactional
    public void deleteBuilding(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("楼栋不存在"));

        String buildingName = building.getName();

        // 规则：楼栋下只要存在任意已入住(occupied>0)的寝室，就禁止删除
        boolean hasOccupiedDormitory = dormitoryRepository.existsByBuildingAndOccupiedGreaterThan(buildingName, 0);
        if (hasOccupiedDormitory) {
            throw new RuntimeException("该楼栋下存在已入住的寝室，不能删除");
        }

        // 级联删除（仅在“该楼栋下所有宿舍都无人入住”前提下执行）
        // 1) 删除该楼栋下所有宿舍相关数据（住宿/报修/来访）
        // 2) 删除该楼栋下所有宿舍
        // 3) 删除楼栋
        List<com.dormitory.entity.Dormitory> dorms = dormitoryRepository.findByBuilding(buildingName);
        for (com.dormitory.entity.Dormitory dorm : dorms) {
            String dormNumber = dorm.getNumber();
            // 删除该寝室的住宿记录（空宿舍理论上没有，但为了数据一致性兜底）
            accommodationRepository.deleteByDormitory(dormNumber);
            // 删除该寝室的报修记录
            repairRepository.deleteByDormitory(dormNumber);
            // 删除该寝室的来访记录
            visitRepository.deleteByDormitory(dormNumber);
            // 注：换寝记录(room_changes)当前没有按宿舍号的删除方法，且字段较复杂，这里不做级联删除
        }

        // 删除该楼栋下的宿舍
        dormitoryRepository.deleteAll(dorms);

        // 最后删除楼栋
        buildingRepository.deleteById(id);
    }
    
    /**
     * 批量删除宿舍楼
     */
    public void deleteBuildings(List<Long> ids) {
        // 批量删除时复用单个删除的约束与级联逻辑
        for (Long id : ids) {
            deleteBuilding(id);
        }
    }
    
    /**
     * 根据名称搜索宿舍楼
     */
    public List<Building> searchBuildingsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllBuildings();
        }
        return buildingRepository.findByNameContaining(name.trim());
    }
    
    /**
     * 根据位置搜索宿舍楼
     */
    public List<Building> searchBuildingsByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return getAllBuildings();
        }
        return buildingRepository.findByLocationContaining(location.trim());
    }
}
