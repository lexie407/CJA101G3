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

    /**
     * 處理活動新增：團主自動報名 → 人數+1
     */
    @Transactional
    public void handleActivityCreation(ActVO actVO) {
        // 1. 設定初始狀態
        actVO.setSignupCnt(0);
        actVO.setRecruitStatus(ActStatus.OPEN.getValue());
        
        // 2. 設定預設的allowCancel值（如果為null則設為1，表示允許退出）
        if (actVO.getAllowCancel() == null) {
            actVO.setAllowCancel((byte) 1);
        }
        
        ActVO savedAct = actRepo.save(actVO);
        
        // 3. 團主自動報名
        PartVO hostParticipant = new PartVO();
        hostParticipant.setActId(savedAct.getActId());
        hostParticipant.setMemId(savedAct.getHostId());
        hostParticipant.setMemType("團主");
        hostParticipant.setJoinTime(LocalDateTime.now());
        hostParticipant.setJoinStatus((byte) 1);
        partRepo.save(hostParticipant);
        
        // 4. 更新活動人數
        savedAct.setSignupCnt(1);
        actRepo.save(savedAct);
        
        // 5. 記錄創建操作（可選）
        System.out.println("活動創建完成 - 活動ID: " + savedAct.getActId() + 
                          ", 初始人數: " + savedAct.getSignupCnt() + 
                          ", 初始狀態: " + savedAct.getRecruitStatus() + 
                          ", 允許退出: " + savedAct.getAllowCancel());
    }
    
    /**
     * 處理活動編輯：保持現有人數統計和參加者關係
     */
    @Transactional
    public void handleActivityUpdate(ActVO actVO) {
        // 1. 獲取現有活動資料
        ActVO existingAct = actRepo.findById(actVO.getActId())
                .orElseThrow(() -> new IllegalArgumentException("活動不存在"));
        
        // 2. 保持現有的系統計算欄位不變
        actVO.setSignupCnt(existingAct.getSignupCnt());
        actVO.setRecruitStatus(existingAct.getRecruitStatus());
        
        // 3. 如果allowCancel為null，保持原有設定
        if (actVO.getAllowCancel() == null) {
            actVO.setAllowCancel(existingAct.getAllowCancel());
        }
        
        // 4. 儲存更新後的活動資料
        actRepo.save(actVO);
        
        // 5. 記錄編輯操作（可選）
        System.out.println("活動編輯完成 - 活動ID: " + actVO.getActId() + 
                          ", 保持人數: " + actVO.getSignupCnt() + 
                          ", 保持狀態: " + actVO.getRecruitStatus() + 
                          ", 允許退出: " + actVO.getAllowCancel());
    }
    
    /**
     * 管理員專用：處理活動編輯 - 可以完整設置所有欄位
     */
    @Transactional
    public void handleActivityUpdateByAdmin(ActVO actVO) {
        // 1. 獲取現有活動資料
        ActVO existingAct = actRepo.findById(actVO.getActId())
                .orElseThrow(() -> new IllegalArgumentException("活動不存在"));
        
        // 2. 自動注入既有的活動相關資料（如果新值為null）
        if (actVO.getSignupCnt() == null) {
            actVO.setSignupCnt(existingAct.getSignupCnt());
        }
        if (actVO.getRecruitStatus() == null) {
            actVO.setRecruitStatus(existingAct.getRecruitStatus());
        }
        if (actVO.getAllowCancel() == null) {
            actVO.setAllowCancel(existingAct.getAllowCancel());
        }
        
        // 3. 儲存更新後的活動資料
        actRepo.save(actVO);
        
        // 4. 記錄管理員編輯操作
        System.out.println("管理員活動編輯完成 - 活動ID: " + actVO.getActId() + 
                          ", 人數: " + actVO.getSignupCnt() + 
                          ", 狀態: " + actVO.getRecruitStatus() + 
                          ", 允許退出: " + actVO.getAllowCancel());
    }
    
    /**
     * 處理參加者報名：人數+1 → 可能觸發「成團」狀態
     */
    @Transactional
    public void handleParticipantSignup(Integer actId, Integer memId) {
        // 檢查活動狀態
        ActVO actVo = actRepo.findById(actId).orElseThrow();
        if (actVo.getRecruitStatus() != ActStatus.OPEN.getValue()) {
            throw new IllegalStateException("活動未開放報名");
        }
        
        // 檢查報名截止時間
        if (LocalDateTime.now().isAfter(actVo.getSignupEnd())) {
            throw new IllegalStateException("報名已截止");
        }
        
        // 檢查是否為團主
        if (actVo.getHostId().equals(memId)) {
            throw new IllegalStateException("團主無需報名自己的活動");
        }
        
        // 檢查人數限制
        if (actVo.getSignupCnt() >= actVo.getMaxCap()) {
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
        updateActivityStatus(actVo, actVo.getSignupCnt() + 1);
    }
    
    /**
     * 處理參加者退出：人數-1 → 可能觸發「重新開放招募」
     */
    @Transactional
    public void handleParticipantCancellation(Integer actId, Integer memId) {
        ActVO actVo = actRepo.findById(actId).orElseThrow();
        
        // 檢查是否為團主
        if (actVo.getHostId().equals(memId)) {
            throw new IllegalStateException("團主不能退出自己的活動，請改為取消活動");
        }

        // 新增：檢查是否允許退出
        if (actVo.getAllowCancel() != null && actVo.getAllowCancel() == 0) {
            throw new IllegalStateException("此活動不允許退出");
        }

        // 檢查報名截止與活動開始
        LocalDateTime now = LocalDateTime.now();
        if (actVo.getSignupEnd() != null && now.isAfter(actVo.getSignupEnd())) {
            throw new IllegalStateException("報名已截止，無法退出");
        }
        if (actVo.getActStart() != null && now.isAfter(actVo.getActStart())) {
            throw new IllegalStateException("活動已開始，無法退出");
        }

        partRepo.deleteByActIdAndMemId(actId, memId);
        updateActivityStatus(actVo, actVo.getSignupCnt() - 1);
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
     * 更新活動狀態:系統自動運算
     */
    private void updateActivityStatus(ActVO actVo, Integer newCount) {
        actVo.setSignupCnt(newCount);
        
        if (newCount >= actVo.getMaxCap()) {
            actVo.setRecruitStatus(ActStatus.FULL.getValue());
        } else if (actVo.getRecruitStatus() == ActStatus.FULL.getValue() &&
                   newCount < actVo.getMaxCap() &&
                   LocalDateTime.now().isBefore(actVo.getSignupEnd())) {
            // 重新開放招募的條件：
            // 1. 原本是成團狀態
            // 2. 現在人數 < 上限
            // 3. 報名還沒截止
            actVo.setRecruitStatus(ActStatus.OPEN.getValue());
        }
        // 如果報名截止時間到，即使人數不足也不會重新開放招募

        actRepo.save(actVo);
    }

    /**
     * 團主/一般權限：活動狀態切換
     */
    public void updateActivityStatusByHost(Integer actId, Byte newStatus, Integer operatorId) {
        ActVO actVo = actRepo.findById(actId)
            .orElseThrow(() -> new IllegalArgumentException("活動不存在"));
        Byte currentStatus = actVo.getRecruitStatus();
        LocalDateTime now = LocalDateTime.now();
        // 禁止對 FROZEN, CANCELLED, ENDED 狀態操作
        if (currentStatus == ActStatus.FROZEN.getValue() ||
            currentStatus == ActStatus.CANCELLED.getValue() ||
            currentStatus == ActStatus.ENDED.getValue()) {
            throw new IllegalStateException("活動已凍結、取消或結束，無法再更改狀態");
        }
        // 一般狀態切換
        if (now.isBefore(actVo.getActStart())) {
            // 活動未開始
            if ((currentStatus == ActStatus.OPEN.getValue() || currentStatus == ActStatus.FULL.getValue()) && newStatus == ActStatus.CANCELLED.getValue()) {
                actVo.setRecruitStatus(ActStatus.CANCELLED.getValue());
                actRepo.save(actVo);
                return;
            } else {
                throw new IllegalStateException("活動未開始僅能取消活動");
            }
        } else {
            // 活動已開始
            if (currentStatus == ActStatus.FULL.getValue() && newStatus == ActStatus.ENDED.getValue()) {
                actVo.setRecruitStatus(ActStatus.ENDED.getValue());
                actRepo.save(actVo);
                return;
            } else {
                throw new IllegalStateException("活動已開始僅能結束活動");
            }
        }
    }

    /**
     * 管理員專屬：凍結活動
     */
    public void freezeActivity(Integer actId, Integer adminId) {
        ActVO act = actRepo.findById(actId)
            .orElseThrow(() -> new IllegalArgumentException("活動不存在"));
        if (act.getRecruitStatus() == ActStatus.FROZEN.getValue()) {
            throw new IllegalStateException("活動已經是凍結狀態");
        }
        act.setRecruitStatus(ActStatus.FROZEN.getValue());
        // 新增：凍結時自動設置為不允許退出
        act.setAllowCancel((byte) 0);
        actRepo.save(act);
        // 可加操作日誌
        System.out.println("活動已凍結 - 活動ID: " + actId + 
                          ", 設置為不允許退出: " + act.getAllowCancel());
    }

    /**
     * 管理員專屬：解除凍結（恢復到指定狀態）
     */
    public void unfreezeActivity(Integer actId, Byte restoreStatus, Integer adminId) {
        ActVO act = actRepo.findById(actId)
            .orElseThrow(() -> new IllegalArgumentException("活動不存在"));
        if (act.getRecruitStatus() != ActStatus.FROZEN.getValue()) {
            throw new IllegalStateException("活動目前不是凍結狀態");
        }
        // 僅允許恢復到 OPEN, FULL, CANCELLED, ENDED, FAILED
        if (restoreStatus == null || restoreStatus == ActStatus.FROZEN.getValue()) {
            throw new IllegalArgumentException("解除凍結時必須指定有效狀態");
        }
        act.setRecruitStatus(restoreStatus);
        // 新增：解除凍結時恢復為允許退出
        act.setAllowCancel((byte) 1);
        actRepo.save(act);
        // 可加操作日誌
        System.out.println("活動已解除凍結 - 活動ID: " + actId + 
                          ", 恢復為允許退出: " + act.getAllowCancel());
    }
} 