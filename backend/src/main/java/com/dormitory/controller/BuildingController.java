package com.dormitory.controller;

import com.dormitory.Json.RestBean;
import com.dormitory.entity.Building;
import com.dormitory.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 宿舍楼控制器
 */
@RestController
@RequestMapping("/buildings")
@CrossOrigin(origins = "*")
public class BuildingController {
    
    @Autowired
    private BuildingService buildingService;
    
    /**
     * 获取所有宿舍楼
     */
    @GetMapping
    public RestBean<List<Building>> getAllBuildings() {
        return RestBean.success(buildingService.getAllBuildings());
    }
    
    /**
     * 根据ID获取宿舍楼
     */
    @GetMapping("/{id}")
    public RestBean<Building> getBuildingById(@PathVariable Long id) {
        return buildingService.getBuildingById(id)
                .map(RestBean::success)
                .orElse(RestBean.failure("宿舍楼不存在"));
    }
    
    /**
     * 搜索宿舍楼
     */
    @GetMapping("/search")
    public RestBean<List<Building>> searchBuildings(@RequestParam(required = false) String keyword) {
        return RestBean.success(buildingService.searchBuildings(keyword));
    }
    
    /**
     * 添加宿舍楼
     */
    @PostMapping
    public RestBean<Building> addBuilding(@RequestBody Building building) {
        return RestBean.success(buildingService.addBuilding(building));
    }
    
    /**
     * 更新宿舍楼
     */
    @PutMapping("/{id}")
    public RestBean<Building> updateBuilding(@PathVariable Long id, @RequestBody Building building) {
        building.setId(id);
        return RestBean.success(buildingService.updateBuilding(building));
    }
    
    /**
     * 删除宿舍楼
     */
    @DeleteMapping("/{id}")
    public RestBean<String> deleteBuilding(@PathVariable Long id) {
        buildingService.deleteBuilding(id);
        return RestBean.success("删除成功");
    }
    
    /**
     * 批量删除宿舍楼
     */
    @DeleteMapping("/batch")
    public RestBean<String> deleteBuildings(@RequestBody List<Long> ids) {
        buildingService.deleteBuildings(ids);
        return RestBean.success("批量删除成功");
    }
}
