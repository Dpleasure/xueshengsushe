package com.dormitory.service;

import com.dormitory.controller.RoomChangeController;
import com.dormitory.entity.Accommodation;
import com.dormitory.entity.RoomChange;
import com.dormitory.repository.AccommodationRepository;
import com.dormitory.entity.Dormitory;
import com.dormitory.repository.DormitoryRepository;
import com.dormitory.repository.RoomChangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 换寝服务
 */
@Service
public class RoomChangeService {
    
    @Autowired
    private RoomChangeRepository roomChangeRepository;
    
    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private DormitoryRepository dormitoryRepository;
    
    /**
     * 获取所有换寝记录
     */
    public List<RoomChange> getAllRoomChanges() {
        return roomChangeRepository.findAll();
    }
    
    /**
     * 根据ID获取换寝记录
     */
    public Optional<RoomChange> getRoomChangeById(Long id) {
        return roomChangeRepository.findById(id);
    }
    
    /**
     * 搜索换寝记录
     */
    public List<RoomChange> searchRoomChanges(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllRoomChanges();
        }
        return roomChangeRepository.findByStudentAContainingOrStudentBContaining(keyword.trim(), keyword.trim());
    }
    
    /**
     * 添加换寝记录
     */
    public RoomChange addRoomChange(RoomChange roomChange) {
        return roomChangeRepository.save(roomChange);
    }
    
    /**
     * 更新换寝记录
     */
    public RoomChange updateRoomChange(RoomChange roomChange) {
        return roomChangeRepository.save(roomChange);
    }
    
    /**
     * 删除换寝记录
     */
    public void deleteRoomChange(Long id) {
        roomChangeRepository.deleteById(id);
    }
    
    /**
     * 根据学生A搜索换寝记录
     */
    public List<RoomChange> searchRoomChangesByStudentA(String studentA) {
        if (studentA == null || studentA.trim().isEmpty()) {
            return getAllRoomChanges();
        }
        return roomChangeRepository.findByStudentAContaining(studentA.trim());
    }
    
    /**
     * 根据学生B搜索换寝记录
     */
    public List<RoomChange> searchRoomChangesByStudentB(String studentB) {
        if (studentB == null || studentB.trim().isEmpty()) {
            return getAllRoomChanges();
        }
        return roomChangeRepository.findByStudentBContaining(studentB.trim());
    }
    
    /**
     * 学生提交换寝申请
     * @param request 换寝请求
     * @return 换寝申请记录
     */
    @Transactional
    public RoomChange applyChange(RoomChangeController.ChangeRoomRequest request) {
        // 1. 查找住宿信息
        Accommodation accommodation = accommodationRepository.findById(request.getAccommodationId())
                .orElseThrow(() -> new RuntimeException("住宿信息不存在"));

        // 2. 检查是否有待处理的申请
        boolean hasPendingChange = roomChangeRepository.existsByStudentAAndStatus(accommodation.getStudentName(), RoomChange.ChangeStatus.PENDING);
        if (hasPendingChange) {
            throw new RuntimeException("您有待处理的换寝申请，请勿重复提交");
        }

        // 3. 创建换寝申请记录
        RoomChange roomChange = new RoomChange();
        roomChange.setStudentA(accommodation.getStudentName());
        roomChange.setOldDormitoryA(accommodation.getDormitory());
        roomChange.setOldBedA(accommodation.getBed());
        roomChange.setNewDormitoryA(request.getNewDormitory());
        roomChange.setNewBedA(request.getNewBed());
        roomChange.setChangeTime(LocalDateTime.now());
        roomChange.setStatus(RoomChange.ChangeStatus.PENDING); // 设置状态为待处理

        // 4. 保存申请记录
        return roomChangeRepository.save(roomChange);
    }


    @Transactional
    public RoomChange approveChange(Long changeId) {
        // 1. 查找换寝申请记录
        RoomChange roomChange = roomChangeRepository.findById(changeId)
                .orElseThrow(() -> new RuntimeException("换寝申请不存在"));

        // 2. 检查状态是否为待处理
        if (roomChange.getStatus() != RoomChange.ChangeStatus.PENDING) {
            throw new RuntimeException("该申请已被处理，请勿重复操作");
        }

        // 3. 查找住宿信息
        Accommodation accommodation = accommodationRepository.findByStudentName(roomChange.getStudentA())
                .orElseThrow(() -> new RuntimeException("学生住宿信息不存在"));

        // 4. 执行换寝逻辑
        Dormitory oldDorm = dormitoryRepository.findByNumber(accommodation.getDormitory())
                .orElseThrow(() -> new RuntimeException("原宿舍不存在"));
        Dormitory newDorm = dormitoryRepository.findByNumber(roomChange.getNewDormitoryA())
                .orElseThrow(() -> new RuntimeException("新宿舍不存在"));

        if (newDorm.getOccupied() >= newDorm.getCapacity()) {
            throw new RuntimeException("新宿舍已满");
        }

        oldDorm.setOccupied(Math.max(0, oldDorm.getOccupied() - 1));
        newDorm.setOccupied(newDorm.getOccupied() + 1);
        dormitoryRepository.save(oldDorm);
        dormitoryRepository.save(newDorm);

        // 5. 更新住宿信息
        accommodation.setDormitory(roomChange.getNewDormitoryA());
        accommodation.setBed(roomChange.getNewBedA());
        accommodationRepository.save(accommodation);

        // 6. 更新申请记录状态
        roomChange.setStatus(RoomChange.ChangeStatus.APPROVED);
        return roomChangeRepository.save(roomChange);
    }

    @Transactional
    public RoomChange rejectChange(Long changeId) {
        // 1. 查找换寝申请记录
        RoomChange roomChange = roomChangeRepository.findById(changeId)
                .orElseThrow(() -> new RuntimeException("换寝申请不存在"));

        // 2. 检查状态是否为待处理
        if (roomChange.getStatus() != RoomChange.ChangeStatus.PENDING) {
            throw new RuntimeException("该申请已被处理，请勿重复操作");
        }

        // 3. 更新申请记录状态
        roomChange.setStatus(RoomChange.ChangeStatus.REJECTED);
        return roomChangeRepository.save(roomChange);
    }

    @Transactional
    public RoomChange changeRoom(Long accommodationId, String newDormitory, String newBed) {
        // 1. 查找住宿信息
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new RuntimeException("住宿信息不存在"));
        
        // 2. 创建换寝记录
        RoomChange roomChange = new RoomChange();
        roomChange.setStudentA(accommodation.getStudentName());
        roomChange.setOldDormitoryA(accommodation.getDormitory());
        roomChange.setOldBedA(accommodation.getBed());
        roomChange.setNewDormitoryA(newDormitory);
        roomChange.setNewBedA(newBed);
        roomChange.setChangeTime(LocalDateTime.now());
        
        // 3. 更新原宿舍和新宿舍的入住人数
        Dormitory oldDorm = dormitoryRepository.findByNumber(accommodation.getDormitory())
                .orElseThrow(() -> new RuntimeException("原宿舍不存在"));
        Dormitory newDorm = dormitoryRepository.findByNumber(newDormitory)
                .orElseThrow(() -> new RuntimeException("新宿舍不存在"));

        if (newDorm.getOccupied() >= newDorm.getCapacity()) {
            throw new RuntimeException("新宿舍已满");
        }

        oldDorm.setOccupied(Math.max(0, oldDorm.getOccupied() - 1));
        newDorm.setOccupied(newDorm.getOccupied() + 1);
        dormitoryRepository.save(oldDorm);
        dormitoryRepository.save(newDorm);

        // 4. 更新住宿信息
        accommodation.setDormitory(newDormitory);
        accommodation.setBed(newBed);
        accommodationRepository.save(accommodation);
        

        
        // 4. 保存换寝记录
        return roomChangeRepository.save(roomChange);
    }
}
