package com.dormitory.controller;

import com.dormitory.Json.RestBean;
import com.dormitory.entity.Accommodation;
import com.dormitory.service.AccommodationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 住宿信息控制器
 */
@RestController
@RequestMapping("/accommodations")
@CrossOrigin(origins = "*")
public class AccommodationController {
    
    @Autowired
    private AccommodationService accommodationService;
    
    /**
     * 获取所有住宿信息
     */
    @GetMapping
    public RestBean<List<Accommodation>> getAllAccommodations() {
        return RestBean.success(accommodationService.getAllAccommodations());
    }
    
    /**
     * 根据ID获取住宿信息
     */
    @GetMapping("/{id}")
    public RestBean<Accommodation> getAccommodationById(@PathVariable Long id) {
        return accommodationService.getAccommodationById(id)
                .map(RestBean::success)
                .orElse(RestBean.failure("住宿信息不存在"));
    }
    
    /**
     * 搜索住宿信息（支持学号或姓名）
     */
    @GetMapping("/search")
    public RestBean<List<Accommodation>> searchAccommodations(
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String dormitory) {
        return RestBean.success(accommodationService.searchAccommodations(studentName, dormitory));
    }

    /**
     * 根据学号搜索住宿信息
     */
    @GetMapping("/search-by-id")
    public RestBean<List<Accommodation>> searchAccommodationsByStudentId(
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String dormitory) {
        return RestBean.success(accommodationService.searchAccommodationsByStudentId(studentId, dormitory));
    }
    
    /**
     * 添加住宿信息
     */
    @PostMapping
    public RestBean<Accommodation> addAccommodation(@RequestBody Accommodation accommodation) {
        return RestBean.success(accommodationService.addAccommodation(accommodation));
    }
    
    /**
     * 更新住宿信息
     */
    @PutMapping("/{id}")
    public RestBean<Accommodation> updateAccommodation(@PathVariable Long id, @RequestBody Accommodation accommodation) {
        accommodation.setId(id);
        return RestBean.success(accommodationService.updateAccommodation(accommodation));
    }
    
    /**
     * 删除住宿信息
     */
    @DeleteMapping("/{id}")
    public RestBean<String> deleteAccommodation(@PathVariable Long id) {
        accommodationService.deleteAccommodation(id);
        return RestBean.success("删除成功");
    }
}
