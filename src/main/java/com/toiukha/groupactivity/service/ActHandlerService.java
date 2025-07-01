package com.toiukha.groupactivity.service;

import com.toiukha.groupactivity.model.ActRepository;
import com.toiukha.groupactivity.model.ActStatus;
import com.toiukha.groupactivity.model.ActVO;
import com.toiukha.participant.model.PartRepository;
import com.toiukha.participant.model.PartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活動處理服務
 * 統一處理活動新增、刪除和參加者報名退出的業務邏輯，避免服務層循環依賴
 */
@Service
public class ActHandlerService {
    
    @Autowired
    private ActRepository actRepo;
    
    @Autowired
    private PartRepository partRepo;
    
    /**
     * 處理活動新增：團主自動報名 → 人數+1
     */
    @Transactional
    public void handleActivityCreation(ActVO actVO) {
        // 1. 設定初始狀態
        actVO.setSignupCnt(0);
        actVO.setRecruitStatus(ActStatus.OPEN.getValue());
        ActVO savedAct = actRepo.save(actVO);
        
        // 2. 團主自動報名
        PartVO hostParticipant = new PartVO();
        hostParticipant.setActId(savedAct.getActId());
        hostParticipant.setMemId(savedAct.getHostId());
        hostParticipant.setMemType("團主");
        hostParticipant.setJoinTime(LocalDateTime.now());
        hostParticipant.setJoinStatus((byte) 1);
        partRepo.save(hostParticipant);
        
        // 3. 更新活動人數
        savedAct.setSignupCnt(1);
        actRepo.save(savedAct);
    }
    
    /**
     * 處理活動刪除：刪除所有參加者 → 人數歸零
     */
    @Transactional
    public void handleActivityDeletion(Integer actId) {
        partRepo.deleteByActId(actId);
        actRepo.deleteById(actId);
    }
    
    /**
     * 處理參加者報名：人數+1 → 可能觸發「成團」狀態
     */
    @Transactional
    public void handleParticipantSignup(Integer actId, Integer memId) {
        // 檢查活動狀態
        ActVO act = actRepo.findById(actId).orElseThrow();
        if (act.getRecruitStatus() != ActStatus.OPEN.getValue()) {
            throw new IllegalStateException("活動未開放報名");
        }
        
        // 檢查是否為團主
        if (act.getHostId().equals(memId)) {
            throw new IllegalStateException("團主無需報名自己的活動");
        }
        
        // 檢查人數限制
        if (act.getSignupCnt() >= act.getMaxCap()) {
            throw new IllegalStateException("人數已滿");
        }
        
        // 檢查是否已報名
        if (partRepo.findByActIdAndMemId(actId, memId).isPresent()) {
            throw new IllegalStateException("已報名");
        }
        
        // 建立參加記錄
        PartVO participant = new PartVO();
        participant.setActId(actId);
        participant.setMemId(memId);
        participant.setMemType("團員");
        participant.setJoinTime(LocalDateTime.now());
        participant.setJoinStatus((byte) 1);
        partRepo.save(participant);
        
        // 更新活動狀態
        updateActivityStatus(act, act.getSignupCnt() + 1);
    }
    
    /**
     * 處理參加者退出：人數-1 → 可能觸發「重新開放招募」
     */
    @Transactional
    public void handleParticipantCancellation(Integer actId, Integer memId) {
        ActVO act = actRepo.findById(actId).orElseThrow();
        
        // 檢查是否為團主
        if (act.getHostId().equals(memId)) {
            throw new IllegalStateException("團主不能退出自己的活動，請改為取消活動");
        }
        
        partRepo.deleteByActIdAndMemId(actId, memId);
        updateActivityStatus(act, act.getSignupCnt() - 1);
    }
    
    /**
     * 更新活動狀態
     */
    private void updateActivityStatus(ActVO act, Integer newCount) {
        act.setSignupCnt(newCount);
        
        if (newCount >= act.getMaxCap()) {
            act.setRecruitStatus(ActStatus.FULL.getValue());
        } else if (act.getRecruitStatus() == ActStatus.FULL.getValue() && newCount < act.getMaxCap()) {
            act.setRecruitStatus(ActStatus.OPEN.getValue());
        }
        
        actRepo.save(act);
    }
    
    /**
     * 查詢會員參加的活動（不包括自己當團主的活動）
     */
    public List<ActVO> getJoinedActivities(Integer memId) {
        List<Integer> actIds = partRepo.findActIdsByMemId(memId);
        List<ActVO> activities = actRepo.findAllById(actIds);
        
        // 過濾掉自己當團主的活動
        activities = activities.stream()
                .filter(activity -> !activity.getHostId().equals(memId))
                .collect(java.util.stream.Collectors.toList());
                
        activities.sort((a, b) -> Integer.compare(b.getActId(), a.getActId()));
        return activities;
    }
} 