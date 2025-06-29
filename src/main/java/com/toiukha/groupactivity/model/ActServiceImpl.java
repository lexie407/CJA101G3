package com.toiukha.groupactivity.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service("actService")
public class ActServiceImpl implements ActService {
    //查詢回傳 VO；表單接收 DTO，轉為 VO 後儲

    @Autowired
    private ActRepository actRepo;
    
    @Autowired
    private DefaultImageService defImgSvc;
    
    @Autowired
    private ActHandlerService actHandlerSvc;

    //前後台通用基本功能
    //新增
    @Override
    public void addAct(ActDTO actDto) {
        ActVO actVo = actDto.toVO();
        // 若未上傳圖片，則保留原圖或設定預設圖
        if (actVo.getActImg() == null || actVo.getActImg().length == 0) {
            actVo.setActImg(defImgSvc.getDefaultImage());
        }
        actHandlerSvc.handleActivityCreation(actVo);
    }

    //修改
    @Override
    public void updateAct(ActDTO actDto) {
        ActVO actVo = actDto.toVO(); // DTO 轉 VO 後更新資料庫
        
        // 若未上傳圖片，則保留原圖或設定預設圖
        if (actVo.getActImg() == null || actVo.getActImg().length == 0) {
            ActVO existingAct = actRepo.findById(actVo.getActId()).orElse(null);
            if (existingAct != null) {
                actVo.setActImg(existingAct.getActImg());
            } else {
                actVo.setActImg(defImgSvc.getDefaultImage()); // 如果找不到原有活動，使用預設圖片
            }
        }
        
        actRepo.save(actVo);
    }

    //刪除
    // TODO:未來改為變更狀態，不直接刪除
    @Override
    public void deleteAct(Integer actId){
        actHandlerSvc.handleActivityDeletion(actId);
    }

    //查詢
    @Override
    public ActVO getOneAct(Integer actId) {
        return actRepo.findById(actId).orElse(null);
    }

    @Override
    public List<ActVO> getAll() {
        return actRepo.findAll();
    }

    //===========複合查詢區段===========
    //後台複合查詢
    @Override
    public List<ActVO> searchActsAll(Specification<ActVO> spec) {
        return actRepo.findAll(spec);
    }

    //依使用者身份查詢：團主或參加者
    @Override
    public List<ActVO> getByHost(Integer hostId) {
        return actRepo.findByHostIdOrderByActIdDesc(hostId);
    }
    
    @Override
    public List<ActVO> getJoinedActs(Integer memId) {
        // 使用 ActHandlerService 來查詢，避免循環依賴
        // 或者直接注入 ParticipateRepository
        return actHandlerSvc.getJoinedActivities(memId);
    }
    
    @Override
    public List<ActCardDTO> getJoinedActsAsCard(Integer memId) {
        List<ActVO> joinedActs = getJoinedActs(memId);
        return joinedActs.stream()
                .map(this::convertToCardDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    //前台分頁查詢
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
        // 強制僅顯示公開活動
        Byte isPublic = (byte) 1;
        Specification<ActVO> spec = ActSpecification.buildSpec(
            recruitStatus, actName, hostId, isPublic, actStart, maxCap
        );
        // 依 recruitStatus（招募中優先）再依 actStart 降冪排序
        Pageable sortedPageable = org.springframework.data.domain.PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Order.asc("recruitStatus"),
                org.springframework.data.domain.Sort.Order.desc("actStart")
            )
        );
        Page<ActVO> actPage = actRepo.findAll(spec, sortedPageable);
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

    //===========狀態相關業務邏輯===========
    
    /**
     * 檢查活動是否為招募中狀態
     */
    @Override
    public boolean isRecruiting(ActVO actVo) {
        System.out.println("=== ActServiceImpl.isRecruiting() 被調用 ===");
        System.out.println("actVo: " + actVo);
        if (actVo == null || actVo.getRecruitStatus() == null) {
            System.out.println("actVo 為 null 或 recruitStatus 為 null，返回 false");
            return false;
        }
        boolean result = actVo.getRecruitStatus() == ActStatus.OPEN.getValue();
        System.out.println("recruitStatus: " + actVo.getRecruitStatus() + ", ActStatus.OPEN.getValue(): " + ActStatus.OPEN.getValue() + ", 結果: " + result);
        return result;
    }
    
    /**
     * 檢查活動是否為成團狀態
     */
    @Override
    public boolean isFull(ActVO actVo) {
        System.out.println("=== ActServiceImpl.isFull() 被調用 ===");
        if (actVo == null || actVo.getRecruitStatus() == null) {
            return false;
        }
        return actVo.getRecruitStatus() == ActStatus.FULL.getValue();
    }
    
    /**
     * 檢查活動是否可以報名
     */
    @Override
    public boolean canSignUp(ActVO actVo) {
        System.out.println("=== ActServiceImpl.canSignUp() 被調用 ===");
        return isRecruiting(actVo);
    }
    
    /**
     * 取得活動狀態顯示名稱
     */
    @Override
    public String getRecruitStatusDisplayName(ActVO actVo) {
        System.out.println("=== ActServiceImpl.getRecruitStatusDisplayName() 被調用 ===");
        if (actVo == null || actVo.getRecruitStatus() == null) {
            return "未知狀態";
        }
        ActStatus status = ActStatus.fromValueOrNull(actVo.getRecruitStatus());
        return status != null ? status.getDisplayName() : "未知狀態";
    }
    
    /**
     * 取得活動狀態CSS類別
     */
    @Override
    public String getRecruitStatusCssClass(ActVO actVo) {
        System.out.println("=== ActServiceImpl.getRecruitStatusCssClass() 被調用 ===");
        if (actVo == null || actVo.getRecruitStatus() == null) {
            return "ended";
        }
        ActStatus status = ActStatus.fromValueOrNull(actVo.getRecruitStatus());
        return status != null ? status.getCssClass() : "ended";
    }
    
    /**
     * 檢查活動是否為公開狀態
     */
    @Override
    public boolean isPublic(ActVO actVo) {
        System.out.println("=== ActServiceImpl.isPublic() 被調用 ===");
        if (actVo == null) {
            return false;
        }
        return actVo.getIsPublic() == 1;
    }
    
    /**
     * 檢查活動是否允許取消報名
     */
    @Override
    public boolean allowCancel(ActVO actVo) {
        System.out.println("=== ActServiceImpl.allowCancel() 被調用 ===");
        if (actVo == null) {
            return false;
        }
        return actVo.getAllowCancel() == 1;
    }

    //===========其他功能===========
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
        ActVO actVo = new ActVO();
        actVo.setActName("測試活動");
        actVo.setActDesc("這是一個測試活動");
        actVo.setItnId(1);
        actVo.setHostId(1);
        actVo.setSignupStart(LocalDateTime.now());
        actVo.setSignupEnd(LocalDateTime.now().plusDays(7));
        actVo.setMaxCap(10);
        actVo.setActStart(LocalDateTime.now().plusDays(14));
        actVo.setActEnd(LocalDateTime.now().plusDays(16));
        actVo.setIsPublic((byte) 1);
        actVo.setAllowCancel((byte) 1);
        actVo.setRecruitStatus(ActStatus.OPEN.getValue());
        
        return actRepo.save(actVo);
    }
}
