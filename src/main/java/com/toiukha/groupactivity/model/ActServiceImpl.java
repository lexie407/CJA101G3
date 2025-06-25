package com.toiukha.groupactivity.model;

import com.toiukha.participant.model.ParticipateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActServiceImpl implements ActService {
    //查詢透過:VO
//表單透過:DTO轉VO

    @Autowired
    private ActRepository actRepo;
    
    @Autowired
    private DefaultImageService defaultImageService;
    
    @Autowired
    private ParticipateService participateService;

    @Override
    public void addAct(ActDTO actDto) {
        ActVO actVo = actDto.toVO(); // 前台傳入 DTO，於此轉為 VO 後交由 JPA 儲存
        
        // 如果沒有上傳圖片，使用預設圖片
        if (actVo.getActImg() == null || actVo.getActImg().length == 0) {
            actVo.setActImg(defaultImageService.getDefaultImage());
        }
        
        actRepo.save(actVo);
    }

    @Override
    public void updateAct(ActDTO actDto) {
        ActVO actVo = actDto.toVO(); // DTO 轉 VO 後更新資料庫
        
        // 如果沒有上傳新圖片，保持原有圖片
        if (actVo.getActImg() == null || actVo.getActImg().length == 0) {
            ActVO existingAct = actRepo.findById(actVo.getActId()).orElse(null);
            if (existingAct != null) {
                actVo.setActImg(existingAct.getActImg());
            } else {
                // 如果找不到原有活動，使用預設圖片
                actVo.setActImg(defaultImageService.getDefaultImage());
            }
        }
        
        actRepo.save(actVo);
    }

    @Override
    public void deleteAct(Integer actId){
        actRepo.deleteById(actId);
    }

    @Override
    public ActVO getOneAct(Integer actId) {
        return actRepo.findById(actId).orElse(null);
    }

    @Override
    public List<ActVO> getAll() {
        return actRepo.findAll();
    }

    @Override
    public List<ActVO> getByHost(Integer hostId) {
        return actRepo.findByHostIdOrderByActIdDesc(hostId);
    }
    
    @Override
    public List<ActVO> getJoinedActs(Integer memId) {
        // 取得會員參加的活動ID列表
        List<Integer> actIds = participateService.getJoinedActivities(memId);
        // 根據活動ID列表查詢活動詳細資料，並按actId降序排序
        List<ActVO> activities = actRepo.findAllById(actIds);
        activities.sort((a, b) -> Integer.compare(b.getActId(), a.getActId()));
        return activities;
    }
    
    @Override
    public List<ActCardDTO> getJoinedActsAsCard(Integer memId) {
        List<ActVO> joinedActs = getJoinedActs(memId);
        return joinedActs.stream()
                .map(this::convertToCardDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    //===========複雜方法===========
    //原本模糊搜尋(關鍵字)
//    @Override
//    public List<ActVO> searchByName(String keyword) {
//        return actRepo.findByActNameContaining(keyword);
//    }
//->改成
//    @Override
//    public List<ActVO> searchActs(String keyword, LocalDate startDate, LocalDate endDate, Integer recruitStatus) {
//        return actRepo.searchActs(keyword, startDate, endDate, recruitStatus);
//    }
    @Override
    public Page<ActVO> searchActs(Specification<ActVO> spec, Pageable pageable) {
        return actRepo.findAll(spec, pageable);
    }

    @Override
    public Page<ActCardDTO> searchActsAsCard(Specification<ActVO> spec, Pageable pageable) {
        Page<ActVO> actPage = actRepo.findAll(spec, pageable);
        return actPage.map(this::convertToCardDTO);
    }
    
    @Override
    public Page<ActCardDTO> searchPublicActs(Byte recruitStatus, String actName, 
                                           Integer hostId, LocalDateTime actStart, 
                                           Integer maxCap, Pageable pageable) {
        // 商業邏輯：強制設定 isPublic=1，確保只搜尋公開活動
        // 這裡不允許外部控制 isPublic 參數，防止前端篡改查看私人活動
        Byte isPublic = (byte) 1; // 強制公開活動
        
        Specification<ActVO> spec = ActSpecification.buildSpec(
            recruitStatus, actName, hostId, isPublic, actStart, maxCap
        );
        
        Page<ActVO> actPage = actRepo.findAll(spec, pageable);
        return actPage.map(this::convertToCardDTO);
    }

    private ActCardDTO convertToCardDTO(ActVO actVO) {
        ActCardDTO dto = new ActCardDTO();
        dto.setActId(actVO.getActId());
        dto.setActName(actVO.getActName());
        dto.setActDesc(actVO.getActDesc());
        dto.setActStart(actVO.getActStart());
        dto.setSignupCnt(actVO.getSignupCnt());
        dto.setMaxCap(actVO.getMaxCap());
        dto.setRecruitStatus(actVO.getRecruitStatus());
        dto.setHostId(actVO.getHostId());
        return dto;
    }

    @Override
    public List<ActVO> searchActsAll(Specification<ActVO> spec) {
        return actRepo.findAll(spec);
    }

    //變更活動狀態
    @Override
    public void changeStatus(Integer actId, Byte status, Integer operatorId, boolean admin) {
        ActVO act = actRepo.findById(actId).orElseThrow();
        if (!admin && !act.getHostId().equals(operatorId)) {
            throw new SecurityException("無權限變更");
        }
        act.setRecruitStatus(status);
        actRepo.save(act);
    }


    //===========測試資料寫入DB，回傳 JSON 結果===========
    @Override
    public ActVO saveTestAct() {
        ActVO act = new ActVO();
        // 必填欄位設置
        act.setActName("測試活動名稱");
        act.setActDesc("這是一個測試活動說明");
        act.setItnId(1); // 行程編號，先寫死
        act.setHostId(33); // 團主編號，先寫死
        act.setSignupStart(LocalDateTime.now());
        act.setSignupEnd(LocalDateTime.now().plusDays(1));
        act.setActStart(LocalDateTime.now().plusDays(2));
        act.setActEnd(LocalDateTime.now().plusDays(3));
        act.setMaxCap(5);
        act.setRecruitStatus((byte) 1);
        act.setActImg(defaultImageService.getDefaultImage()); // 使用預設圖片
        return actRepo.save(act);
    }
}
