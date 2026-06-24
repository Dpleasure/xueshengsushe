package com.dormitory.controller;

import com.dormitory.Json.RestBean;
import com.dormitory.entity.Repair;
import com.dormitory.service.RepairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报修控制器
 */
@RestController
@RequestMapping("/api/repairs")
@CrossOrigin(origins = "*")
public class RepairController {
    
    @Autowired
    private RepairService repairService;
    
    /**
     * 获取所有报修记录
     */
    @GetMapping
    public RestBean<List<Repair>> getAllRepairs() {
        return RestBean.success(repairService.getAllRepairs());
    }
    
    /**
     * 根据ID获取报修记录
     */
    @GetMapping("/{id}")
    public RestBean<Repair> getRepairById(@PathVariable Long id) {
        return repairService.getRepairById(id)
                .map(RestBean::success)
                .orElse(new RestBean<>(404, false, null));
    }
    
    /**
     * 搜索报修记录
     */
    @GetMapping("/search")
    public RestBean<List<Repair>> searchRepairs(@RequestParam(required = false) String keyword) {
        return RestBean.success(repairService.searchRepairs(keyword));
    }
    
    /**
     * 根据状态获取报修记录
     */
    @GetMapping("/status/{status}")
    public RestBean<List<Repair>> getRepairsByStatus(@PathVariable Repair.RepairStatus status) {
        return RestBean.success(repairService.getRepairsByStatus(status));
    }
    
    /**
     * 添加报修记录
     */
    @PostMapping("/apply")
    public RestBean<Repair> applyForRepair(@RequestParam("studentName") String studentName,
                                           @RequestParam("studentId") String studentId,
                                           @RequestParam("dormitory") String dormitory,
                                           @RequestParam("description") String description,
                                           @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            Repair repair = new Repair();
            repair.setReporter(studentName);
            repair.setStudentId(studentId);
            repair.setDormitory(dormitory);
            repair.setDescription(description);
            repair.setStatus(Repair.RepairStatus.PENDING);
            repair.setRepairTime(LocalDateTime.now());

            if (image != null && !image.isEmpty()) {
                // 处理文件上传，例如保存到服务器并获取URL
                String imageUrl = saveImage(image); // 这是一个需要实现的辅助方法
                repair.setImages(imageUrl);
            }

            Repair savedRepair = repairService.addRepair(repair);
            return RestBean.success(savedRepair);
        } catch (Exception e) {
            return RestBean.failure(500, (Repair) null); // 返回一个类型匹配的RestBean
        }
    }

    @PutMapping("/{id}/status")
    public RestBean<Repair> updateRepairStatus(@PathVariable Long id, @RequestParam("status") String status) {
        try {
            Repair.RepairStatus repairStatus = Repair.RepairStatus.valueOf(status.toUpperCase());
            Repair repair = repairService.updateRepairStatus(id, repairStatus);
            return RestBean.success(repair);
        } catch (IllegalArgumentException e) {
            return RestBean.failure(400, (Repair) null); // 状态无效
        } catch (Exception e) {
            return RestBean.failure(500, (Repair) null); // 其他错误
        }
    }

    @DeleteMapping("/{id}")
    public RestBean<Void> deleteRepair(@PathVariable Long id) {
        repairService.deleteRepair(id);
        return RestBean.success();
    }

    @GetMapping("/student/{studentId}")
    public RestBean<List<Repair>> getRepairsByStudentId(@PathVariable String studentId) {
        // 这需要在RepairService和Repository中实现
        List<Repair> repairs = repairService.findRepairsByStudentId(studentId);
        return RestBean.success(repairs);
    }

    // 辅助方法，用于保存图片
    private String saveImage(MultipartFile image) throws java.io.IOException {
        // 实际应用中，应将文件保存到配置的路径，并返回可访问的URL
        // 这里仅为示例，返回文件名
        String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
        java.nio.file.Path path = java.nio.file.Paths.get("uploads/", fileName);
        java.nio.file.Files.createDirectories(path.getParent());
        java.nio.file.Files.copy(image.getInputStream(), path);
        return "/uploads/" + fileName;
    }
    
    /**
     * 更新报修记录
     */
    @PutMapping("/{id}")
    public RestBean<Repair> updateRepair(@PathVariable Long id, @RequestBody Repair repair) {
        repair.setId(id);
        return RestBean.success(repairService.updateRepair(repair));
    }
    

}
