package com.toiukha.participant.model;

import com.toiukha.groupactivity.service.ActHandlerService;
import com.toiukha.members.model.MembersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 參加者服務實作，處理報名與取消邏輯
 */
@Service
public class PartServiceImpl implements PartService {

    @Autowired
    private ActHandlerService actHandlerSvc;

    @Autowired
    private PartRepository partRepo;

    @Autowired
    private MembersService memSvc;

    @Override
    public void signup(Integer actId, Integer memId) {
        actHandlerSvc.handleParticipantSignup(actId, memId);
    }

    @Override
    public void cancel(Integer actId, Integer memId) {
        actHandlerSvc.handleParticipantCancellation(actId, memId);
    }

    @Override
    public List<Integer> getParticipants(Integer actId) {
        return partRepo.findMemIdsByActId(actId);
    }
    
    @Override
    public List<PartDTO> getParticipantsAsDTO(Integer actId) {
        List<PartVO> participants = partRepo.findByActId(actId);
        return participants.stream()
                .map(vo -> PartDTO.fromVO(vo, memSvc.getOneMember(vo.getMemId()) != null ? memSvc.getOneMember(vo.getMemId()).getMemName() : null))
                .collect(Collectors.toList());
    }
    
    @Override
    public PartDTO getParticipantAsDTO(Integer actId, Integer memId) {
        return partRepo.findByActIdAndMemId(actId, memId)
                .map(PartDTO::fromVO)
                .orElse(null);
    }
    
    @Override
    public List<Integer> getJoinedActivities(Integer memId) {
        return partRepo.findActIdsByMemId(memId);
    }
    
    @Override
    public List<PartDTO> getJoinedActivitiesAsDTO(Integer memId) {
        List<PartVO> joinedActivities = partRepo.findByMemId(memId);
        return joinedActivities.stream()
                .map(PartDTO::fromVO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateJoinStatus(Integer actId, Integer memId, Byte joinStatus) {
        if (joinStatus == null || (joinStatus != 1 && joinStatus != 2)) {
            throw new IllegalArgumentException("joinStatus 只能為 1(已參加) 或 2(已剔除)");
        }
        PartVO part = partRepo.findByActIdAndMemId(actId, memId)
                .orElseThrow(() -> new IllegalArgumentException("找不到該成員"));
        part.setJoinStatus(joinStatus);
        partRepo.save(part);
    }
} 