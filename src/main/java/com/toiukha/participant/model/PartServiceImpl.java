package com.toiukha.participant.model;

import com.toiukha.groupactivity.service.ActHandlerService;
import com.toiukha.members.model.MembersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return partRepo.findMemberNamesAndStatusByActId(actId);
    }
    
    // ---未使用--- @Override
    // ---未使用--- public PartDTO getParticipantAsDTO(Integer actId, Integer memId) {
    // ---未使用---     return partRepo.findByActIdAndMemId(actId, memId)
    // ---未使用---             .map(PartDTO::fromVO)
    // ---未使用---             .orElse(null);
    // ---未使用--- }
    
    // ---未使用--- @Override
    // ---未使用--- public List<Integer> getJoinedActivities(Integer memId) {
    // ---未使用---     return partRepo.findActIdsByMemId(memId);
    // ---未使用--- }
    
    // ---未使用--- @Override
    // ---未使用--- public List<PartDTO> getJoinedActivitiesAsDTO(Integer memId) {
    // ---未使用---     List<PartVO> joinedActivities = partRepo.findByMemId(memId);
    // ---未使用---     return joinedActivities.stream()
    // ---未使用---             .map(PartDTO::fromVO)
    // ---未使用---             .collect(Collectors.toList());
    // ---未使用--- }

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