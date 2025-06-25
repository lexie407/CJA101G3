package com.toiukha.participant.model;

import com.toiukha.groupactivity.model.ActRepository;
import com.toiukha.groupactivity.model.ActStatus;
import com.toiukha.groupactivity.model.ActVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        
        // 直接透過 actId 查找活動，避免使用關聯物件
        ActVO act = actRepository.findById(actId).orElseThrow();
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
    
    @Override
    public List<ParticipantDTO> getParticipantsAsDTO(Integer actId) {
        List<ParticipantVO> participants = participateRepository.findByActId(actId);
        return participants.stream()
                .map(ParticipantDTO::fromVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public ParticipantDTO getParticipantAsDTO(Integer actId, Integer memId) {
        Optional<ParticipantVO> participant = participateRepository.findByActIdAndMemId(actId, memId);
        return participant.map(ParticipantDTO::fromVO).orElse(null);
    }
    
    @Override
    public List<Integer> getJoinedActivities(Integer memId) {
        return participateRepository.findActIdsByMemId(memId);
    }
    
    @Override
    public List<ParticipantDTO> getJoinedActivitiesAsDTO(Integer memId) {
        List<ParticipantVO> joinedActivities = participateRepository.findByMemId(memId);
        return joinedActivities.stream()
                .map(ParticipantDTO::fromVO)
                .collect(Collectors.toList());
    }
}
