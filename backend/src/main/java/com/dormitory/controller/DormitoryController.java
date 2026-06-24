package com.dormitory.controller;

import com.dormitory.Json.RestBean;
import com.dormitory.entity.Dormitory;
import com.dormitory.service.DormitoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 宿舍控制器
 */
@RestController
@RequestMapping("/dormitories")
@CrossOrigin(origins = "*")
public class DormitoryController {
    
    @Autowired
    private DormitoryService dormitoryService;
    
    /**
     * 获取所有宿舍
     */
    @GetMapping
    public RestBean<List<Dormitory>> getAllDormitories() {
        return RestBean.success(dormitoryService.getAllDormitories());
    }
    
    /**
     * 根据ID获取宿舍
     */
    @GetMapping("/{id}")
    public RestBean<Dormitory> getDormitoryById(@PathVariable Long id) {
        return dormitoryService.getDormitoryById(id)
                .map(RestBean::success)
                .orElse(RestBean.failure("宿舍不存在"));
    }
    
    /**
     * 搜索宿舍
     */
    @GetMapping("/search")
    public RestBean<List<Dormitory>> searchDormitories(@RequestParam(required = false) String keyword) {
        return RestBean.success(dormitoryService.searchDormitories(keyword));
    }
    
    /**
     * 根据宿舍楼获取宿舍
     */
    @GetMapping("/building/{building}")
    public RestBean<List<Dormitory>> getDormitoriesByBuilding(@PathVariable String building) {
        return RestBean.success(dormitoryService.getDormitoriesByBuilding(building));
    }
    
    /**
     * 添加宿舍
     */
    @PostMapping
    public RestBean<Dormitory> addDormitory(@RequestBody Dormitory dormitory) {
        return RestBean.success(dormitoryService.addDormitory(dormitory));
    }
    
    /**
     * 更新宿舍
     */
    @PutMapping("/{id}")
    public RestBean<Dormitory> updateDormitory(@PathVariable Long id, @RequestBody Dormitory dormitory) {
        dormitory.setId(id);
        return RestBean.success(dormitoryService.updateDormitory(dormitory));
    }
    
    /**
     * 删除宿舍
     */
    @DeleteMapping("/{id}")
    public RestBean<String> deleteDormitory(@PathVariable Long id) {
        dormitoryService.deleteDormitory(id);
        return RestBean.success("删除成功");
    }
    
    /**
     * 批量删除宿舍
     */
    @DeleteMapping("/batch")
    public RestBean<String> deleteDormitories(@RequestBody List<Long> ids) {
        dormitoryService.deleteDormitories(ids);
        return RestBean.success("批量删除成功");
    }
}
