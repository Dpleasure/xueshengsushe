package com.dormitory.service;

import com.dormitory.entity.Visit;
import com.dormitory.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 来访登记服务
 */
@Service
public class VisitService {

    @Autowired
    private VisitRepository visitRepository;

    /**
     * 获取所有来访记录
     */
    public List<Visit> getAllVisits() {
        return visitRepository.findAll();
    }

    /**
     * 根据学生学号获取来访申请记录
     */
    public List<Visit> getVisitsByStudentId(String studentId) {
        return visitRepository.findByStudentId(studentId);
    }

    /**
     * 学生提交来访申请
     */
    public Visit applyForVisit(Visit visit) {
        visit.setStatus(Visit.VisitStatus.PENDING);
        return visitRepository.save(visit);
    }

    /**
     * 同意来访申请
     */
    public Visit approveVisit(Long id) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("来访申请不存在"));
        
        if (visit.getStatus() != Visit.VisitStatus.PENDING) {
            throw new RuntimeException("该申请已被处理，请勿重复操作");
        }
        
        visit.setStatus(Visit.VisitStatus.APPROVED);
        return visitRepository.save(visit);
    }

    /**
     * 拒绝来访申请
     */
    public Visit rejectVisit(Long id) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("来访申请不存在"));
        
        if (visit.getStatus() != Visit.VisitStatus.PENDING) {
            throw new RuntimeException("该申请已被处理，请勿重复操作");
        }
        
        visit.setStatus(Visit.VisitStatus.REJECTED);
        return visitRepository.save(visit);
    }

    /**
     * 删除来访记录
     */
    public void deleteVisit(Long id) {
        visitRepository.deleteById(id);
    }
}
