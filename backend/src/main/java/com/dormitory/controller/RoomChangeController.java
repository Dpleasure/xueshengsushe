package com.dormitory.controller;

import com.dormitory.Json.RestBean;
import com.dormitory.entity.RoomChange;
import com.dormitory.service.RoomChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 换寝控制器
 */
@RestController
@RequestMapping("/room-changes")
@CrossOrigin(origins = "*")
public class RoomChangeController {
    
    @Autowired
    private RoomChangeService roomChangeService;
    
    /**
     * 获取所有换寝记录
     */
    @GetMapping
    public RestBean<List<RoomChange>> getAllRoomChanges() {
        return RestBean.success(roomChangeService.getAllRoomChanges());
    }
    
    /**
     * 根据ID获取换寝记录
     */
    @GetMapping("/{id}")
    public RestBean<RoomChange> getRoomChangeById(@PathVariable Long id) {
        return roomChangeService.getRoomChangeById(id)
                .map(RestBean::success)
                .orElse(RestBean.failure("换寝记录不存在"));
    }
    
    /**
     * 搜索换寝记录
     */
    @GetMapping("/search")
    public RestBean<List<RoomChange>> searchRoomChanges(@RequestParam(required = false) String keyword) {
        return RestBean.success(roomChangeService.searchRoomChanges(keyword));
    }
    
    /**
     * 添加换寝记录
     */
    @PostMapping
    public RestBean<RoomChange> addRoomChange(@RequestBody RoomChange roomChange) {
        return RestBean.success(roomChangeService.addRoomChange(roomChange));
    }
    
    /**
     * 更新换寝记录
     */
    @PutMapping("/{id}")
    public RestBean<RoomChange> updateRoomChange(@PathVariable Long id, @RequestBody RoomChange roomChange) {
        roomChange.setId(id);
        return RestBean.success(roomChangeService.updateRoomChange(roomChange));
    }
    
    /**
     * 删除换寝记录
     */
    @DeleteMapping("/{id}")
    public RestBean<String> deleteRoomChange(@PathVariable Long id) {
        roomChangeService.deleteRoomChange(id);
        return RestBean.success("删除成功");
    }

    /**
     * 同意换寝申请
     */
    @PostMapping("/{id}/approve")
    public RestBean<RoomChange> approveChange(@PathVariable Long id) {
        try {
            RoomChange roomChange = roomChangeService.approveChange(id);
            return RestBean.success(roomChange);
        } catch (Exception e) {
            return RestBean.failure(e.getMessage());
        }
    }

    /**
     * 拒绝换寝申请
     */
    @PostMapping("/{id}/reject")
    public RestBean<RoomChange> rejectChange(@PathVariable Long id) {
        try {
            RoomChange roomChange = roomChangeService.rejectChange(id);
            return RestBean.success(roomChange);
        } catch (Exception e) {
            return RestBean.failure(e.getMessage());
        }
    }
    
    /**
     * 学生提交换寝申请
     */
    @PostMapping("/apply-change")
    public RestBean<RoomChange> applyChange(@RequestBody ChangeRoomRequest request) {
        try {
            RoomChange roomChange = roomChangeService.applyChange(request);
            return RestBean.success(roomChange);
        } catch (Exception e) {
            return RestBean.failure(e.getMessage());
        }
    }

    /**
     * 单个学生换寝
     */
    @PostMapping("/change-room")
    public RestBean<RoomChange> changeRoom(@RequestBody ChangeRoomRequest request) {
        try {
            RoomChange roomChange = roomChangeService.changeRoom(
                request.getAccommodationId(),
                request.getNewDormitory(),
                request.getNewBed()
            );
            return RestBean.success(roomChange);
        } catch (Exception e) {
            return RestBean.failure(e.getMessage());
        }
    }
    
    /**
     * 换寝请求DTO
     */
    public static class ChangeRoomRequest {
        private Long accommodationId;
        private String newDormitory;
        private String newBed;
        
        public Long getAccommodationId() {
            return accommodationId;
        }
        
        public void setAccommodationId(Long accommodationId) {
            this.accommodationId = accommodationId;
        }
        
        public String getNewDormitory() {
            return newDormitory;
        }
        
        public void setNewDormitory(String newDormitory) {
            this.newDormitory = newDormitory;
        }
        
        public String getNewBed() {
            return newBed;
        }
        
        public void setNewBed(String newBed) {
            this.newBed = newBed;
        }
    }
}
