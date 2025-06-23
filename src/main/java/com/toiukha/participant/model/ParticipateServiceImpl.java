package com.toiukha.participant.model;

import com.toiukha.groupactivity.model.ActRepository;
import com.toiukha.groupactivity.model.ActStatus;
import com.toiukha.groupactivity.model.ActVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 參加者服務實作，處理報名與取消邏輯
 */
@Service
public class ParticipateServiceImpl implements ParticipateService {

    @Autowired
    private ActRepository actRepository;

    @Autowired
    private ParticipateRepository participateRepository;

    @Override
    public void signup(Integer actId, Integer memId) {
        // 檢查活動是否存在
        ActVO act = actRepository.findById(actId).orElseThrow();
        
        // 檢查活動狀態
        if (act.getRecruitStatus() != ActStatus.OPEN)
            throw new IllegalStateException("活動未開放報名");
        
        // 檢查人數限制
        if (act.getSignupCnt() != null && act.getSignupCnt() >= act.getMaxCap())
            throw new IllegalStateException("人數已滿");
        
        // 檢查是否已報名
        Optional<ParticipantVO> exist = participateRepository.findByActIdAndMemId(actId, memId);
        if (exist.isPresent())
            throw new IllegalStateException("已報名");

        // 建立參加記錄
        ParticipantVO part = new ParticipantVO();
        part.setActId(actId);
        part.setMemId(memId);
        part.setMemType("團員");
        part.setJoinTime(LocalDateTime.now());
        part.setJoinStatus((byte) 1); // 1 = 已加入
        participateRepository.save(part);

        // 更新活動報名人數
        act.setSignupCnt((act.getSignupCnt() == null ? 0 : act.getSignupCnt()) + 1);
        if (act.getSignupCnt() >= act.getMaxCap()) {
            act.setRecruitStatus((byte) ActStatus.FULL);
        }
        actRepository.save(act);
    }

    @Override
    public void cancel(Integer actId, Integer memId) {
        // 查找參加記錄
        ParticipantVO part = participateRepository.findByActIdAndMemId(actId, memId)
                .orElseThrow();
        
        // 刪除參加記錄
        participateRepository.delete(part);
        
        // 更新活動報名人數
        ActVO act = part.getActVO();
        act.setSignupCnt(act.getSignupCnt() - 1);
        if (act.getRecruitStatus() == ActStatus.FULL && act.getSignupCnt() < act.getMaxCap()) {
            act.setRecruitStatus((byte) ActStatus.OPEN);
        }
        actRepository.save(act);
    }

    @Override
    public List<Integer> getParticipants(Integer actId) {
        return participateRepository.findMemIdsByActId(actId);
    }
}
