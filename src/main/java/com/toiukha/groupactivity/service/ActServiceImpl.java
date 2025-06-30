package com.toiukha.groupactivity.service;

import com.toiukha.groupactivity.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service("actService")
public class ActServiceImpl implements ActService {

    @Autowired
    private ActRepository actRepo;
    
    @Autowired
    private DefaultImageService defImgSvc;
    
    @Autowired
    private ActHandlerService actHandlerSvc;
    
    @Autowired
    private ActTagService tagService;


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

    //=======前後台通用基本功能==========

    //新增活動
    @Override
    public void addAct(ActDTO actDto) {
        ActVO actVo = actDto.toVO();
        // 若未上傳圖片，則保留原圖或設定預設圖
        if (actVo.getActImg() == null || actVo.getActImg().length == 0) {
            actVo.setActImg(defImgSvc.getDefaultImage());
        }
        actHandlerSvc.handleActivityCreation(actVo);
        
        // 儲存活動標籤到Redis
        if (actDto.getActType() != null && actDto.getActCity() != null) {
            tagService.saveActTags(actVo.getActId(), actDto.getTypeTag(), actDto.getCityTag());
        }
    }

    //更新活動
    @Override
    public void updateAct(ActDTO actDto) {
        ActVO actVo = actDto.toVO();

        // 若未上傳圖片，則保留原圖或設定預設圖
        if (actVo.getActImg() == null || actVo.getActImg().length == 0) {
            ActVO existingAct = actRepo.findById(actVo.getActId()).orElse(null);
            if (existingAct != null) {
                actVo.setActImg(existingAct.getActImg());
            } else {
                actVo.setActImg(defImgSvc.getDefaultImage());
            }
        }
        //儲存VO物件到mySQL
        actRepo.save(actVo);
        
        // 更新活動標籤到Redis
        if (actDto.getActType() != null && actDto.getActCity() != null) {
            tagService.saveActTags(actVo.getActId(), actDto.getTypeTag(), actDto.getCityTag());
        }
    }

    //刪除活動
    // TODO:未來改為變更狀態，不直接刪除
    @Override
    public void deleteAct(Integer actId){
        actHandlerSvc.handleActivityDeletion(actId);
    }

    //查詢活動
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
        return actHandlerSvc.getJoinedActivities(memId); // 使用 ActHandlerService 來查詢，避免循環依賴
    }

    @Override
    public List<ActCardDTO> getJoinedActsAsCard(Integer memId) {
        List<ActVO> joinedActs = getJoinedActs(memId);
        return joinedActs.stream()
                .map(this::convertToCardDTO)
                .collect(java.util.stream.Collectors.toList());
    }


    //查詢圖片（單獨處理）
    @Override
    public byte[] getActImageOnly(Integer actId) {
        return actRepo.findActImgByActId(actId);
    }


    //===========分頁查詢===========
    //後台複合查詢（不分頁）
    @Override
    public List<ActVO> searchActsAll(Specification<ActVO> spec) {
        return actRepo.findAll(spec);
    }


    //前台查詢（分頁）
    @Override
    public Page<ActVO> searchActs(Specification<ActVO> spec, Pageable pageable) {
        return actRepo.findAll(spec, pageable);
    }

    @Override
    public Page<ActCardDTO> searchActsAsCard(Specification<ActVO> spec, Pageable pageable) {
        Page<ActVO> actPage = actRepo.findAll(spec, pageable);
        return actPage.map(this::convertToCardDTO);
    }

    //分頁查詢已公開活動
    @Override
    public Page<ActCardDTO> searchPublicActs(Byte recruitStatus, String actName, 
                                           Integer hostId, LocalDateTime actStart, 
                                           Integer maxCap, Pageable pageable) {
        //參數清洗
        Byte cleanedRecruitStatus = (recruitStatus != null && recruitStatus >= 0 && recruitStatus <= 5) ? recruitStatus : null;
        String cleanedActName = (actName != null && !actName.trim().isEmpty()) ? actName.trim() : null;
        Integer cleanedHostId = (hostId != null && hostId > 0) ? hostId : null;
        LocalDateTime cleanedActStart = actStart;
        Integer cleanedMaxCap = (maxCap != null && maxCap > 0) ? maxCap : null;
        
        // 只顯示公開活動
        Byte isPublic = (byte) 1;
        Specification<ActVO> spec = ActSpecification.buildSpec(
            cleanedRecruitStatus, cleanedActName, cleanedHostId, isPublic, cleanedActStart, cleanedMaxCap
        );

        // 招募中優先，再依活動開始時間降冪排序
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

    //===========活動狀態===========

    //取得活動狀態名稱
    @Override
    public String getRecruitStatusDisplayName(ActVO actVo) {
        if (actVo == null || actVo.getRecruitStatus() == null) {
            return "未知狀態";
        }
        ActStatus status = ActStatus.fromValueOrNull(actVo.getRecruitStatus());
        return status != null ? status.getDisplayName() : "未知狀態";
    }

    //取得活動狀態CSS類別
    @Override
    public String getRecruitStatusCssClass(ActVO actVo) {
        if (actVo == null || actVo.getRecruitStatus() == null) {
            return "ended";
        }
        ActStatus status = ActStatus.fromValueOrNull(actVo.getRecruitStatus());
        return status != null ? status.getCssClass() : "ended";
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

    //檢查活動狀態（招募中）
    @Override
    public boolean isRecruiting(ActVO actVo) {
        if (actVo == null || actVo.getRecruitStatus() == null) {
            return false;
        }
        return actVo.getRecruitStatus() == ActStatus.OPEN.getValue();
    }

    //檢查活動狀態（成團）
    @Override
    public boolean isFull(ActVO actVo) {
        if (actVo == null || actVo.getRecruitStatus() == null) {
            return false;
        }
        return actVo.getRecruitStatus() == ActStatus.FULL.getValue();
    }

    //檢查活動狀態（可報名）
    @Override
    public boolean canSignUp(ActVO actVo) {
        return isRecruiting(actVo);
    }

    //檢查活動狀態（是否公開）
    @Override
    public boolean isPublic(ActVO actVo) {
        if (actVo == null) {
            return false;
        }
        return actVo.getIsPublic() == 1;
    }

    //檢查活動狀態（是否允許退出）
    @Override
    public boolean allowCancel(ActVO actVo) {
        if (actVo == null) {
            return false;
        }
        return actVo.getAllowCancel() == 1;
    }


    //===========活動標籤(redis)===========

    //儲存活動標籤
    @Override
    public void saveActTags(Integer actId, ActTag typeTag, ActTag cityTag) {
        tagService.saveActTags(actId, typeTag, cityTag);
    }

    //獲取活動標籤
    @Override
    public java.util.Map<String, ActTag> getActTags(Integer actId) {
        return tagService.getActTags(actId);
    }

    //根據標籤搜尋活動
    @Override
    public Page<ActCardDTO> searchByTags(ActTag typeTag, ActTag cityTag, Pageable pageable) {
        // 建立搜尋標籤集合
        java.util.Set<ActTag> searchTags = new java.util.HashSet<>();
        if (typeTag != null) searchTags.add(typeTag);
        if (cityTag != null) searchTags.add(cityTag);

        java.util.Set<Integer> actIds;
        if (searchTags.isEmpty()) {
            // 無標籤條件：返回所有公開活動
            return searchPublicActs(null, null, null, null, null, pageable);
        } else {
            // 有標籤條件：按標籤搜尋
            actIds = tagService.getActsByTags(searchTags);
        }

        if (actIds.isEmpty()) {
            return new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0);
        }

        // 查詢並分頁
        List<ActCardDTO> results = actRepo.findAllById(actIds).stream()
                .filter(act -> act.getIsPublic() == 1)
                .sorted((a, b) -> b.getActStart().compareTo(a.getActStart()))
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .map(this::convertToCardDTO)
                .collect(java.util.stream.Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(results, pageable, actIds.size());
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
