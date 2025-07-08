package com.toiukha.groupactivity.service;

import com.toiukha.groupactivity.model.*;
import com.toiukha.itinerary.model.ItineraryVO;
import com.toiukha.itinerary.service.ItineraryService;
import com.toiukha.notification.model.NotificationService;
import com.toiukha.notification.model.NotificationVO;
import com.toiukha.participant.model.PartRepository;
import com.toiukha.participant.model.PartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service("actService")
public class ActServiceImpl implements ActService {

    @Autowired
    private ActRepository actRepo;
    
    @Autowired
    private DefaultImageService defImgSvc;
    
    @Autowired
    private ActHandlerService actHandlerSvc;
    
    @Autowired
    private ActTagService tagSvc;
    
    @Autowired
    private PartRepository partRepo;
    
    @Autowired
    private PartService partSvc;
    
    @Autowired
    private NotificationService notiSvc;

    @Autowired
    private ItineraryService itnSvc;

    //=======前後台通用基本功能==========

    //新增活動
    @Override
    public void addAct(ActDTO actDto) {
        // 驗證行程是否存在
        if (actDto.getItnId() != null && !validateItinerary(actDto.getItnId())) {
            throw new IllegalArgumentException("指定的行程不存在");
        }
        
        ActVO actVo = actDto.toVO();
        // 若未上傳圖片，則保留原圖或設定預設圖
        if (actVo.getActImg() == null || actVo.getActImg().length == 0) {
            actVo.setActImg(defImgSvc.getDefaultImage());
        }
        actHandlerSvc.handleActivityCreation(actVo);
        
        // 儲存活動標籤到Redis
        if (actDto.getActType() != null && actDto.getActCity() != null) {
            tagSvc.saveActTags(actVo.getActId(), actDto.getTypeTag(), actDto.getCityTag());
        }
    }

    //更新活動
    @Override
    public void updateAct(ActDTO actDto) {
        // 驗證行程是否存在
        if (actDto.getItnId() != null && !validateItinerary(actDto.getItnId())) {
            throw new IllegalArgumentException("指定的行程不存在");
        }
        
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
        
        // 使用 ActHandlerService 處理活動編輯邏輯
        actHandlerSvc.handleActivityUpdate(actVo);
        
        // 更新活動標籤到Redis
        if (actDto.getActType() != null && actDto.getActCity() != null) {
            tagSvc.saveActTags(actVo.getActId(), actDto.getTypeTag(), actDto.getCityTag());
        }
    }

    //刪除活動
    // TODO:未來改為變更狀態，不直接刪除
    @Override
    public void deleteAct(Integer actId){
        actHandlerSvc.handleActivityDeletion(actId);
    }

    //會員刪除活動（僅限未公開活動）
    @Override
    public void memDelete(Integer actId, Integer hostId) {
        ActVO act = getOneAct(actId);
        if (act == null) {
            throw new IllegalArgumentException("活動不存在");
        }
        
        // 檢查是否為團主
        if (!act.getHostId().equals(hostId)) {
            throw new SecurityException("只有團主可以刪除活動");
        }
        
        // 檢查是否為未公開活動
        if (act.getIsPublic() == 1) {
            throw new IllegalStateException("公開活動無法刪除");
        }
        
        // 使用 ActHandlerService 處理刪除
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

    // ---未使用--- @Override
    // ---未使用--- public List<ActVO> getJoinedActs(Integer memId) {
    // ---未使用---     return actHandlerSvc.getJoinedActivities(memId); // 使用 ActHandlerService 來查詢，避免循環依賴
    // ---未使用--- }

    @Override
    public List<ActCardDTO> getJoinedActsAsCard(Integer memId) {
        // 直接使用 ActHandlerService 來查詢，避免循環依賴
        List<ActVO> joinedActs = actHandlerSvc.getJoinedActivities(memId);
        
        // 查詢每個活動的團員狀態
        return joinedActs.stream()
                .map(actVO -> {
                    ActCardDTO dto = ActCardDTO.fromVO(actVO);
                    
                    // 查詢該會員在此活動中的參加狀態
                    try {
                        com.toiukha.participant.model.PartVO part = partRepo.findByActIdAndMemId(actVO.getActId(), memId)
                                .orElse(null);
                        if (part != null) {
                            dto.setJoinStatus(part.getJoinStatus());
                        }
                    } catch (Exception e) {
                        System.err.println("查詢團員狀態時發生錯誤: " + e.getMessage());
                        dto.setJoinStatus(null);
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }


    //查詢圖片（單獨處理）
    @Override
    public byte[] getActImageOnly(Integer actId) {
        return actRepo.findActImgByActId(actId);
    }


    //===========分頁查詢===========
    // ---未使用--- 後台複合查詢（不分頁）
    // ---未使用--- @Override
    // ---未使用--- public List<ActVO> searchActsAll(Specification<ActVO> spec) {
    // ---未使用---     return actRepo.findAll(spec);
    // ---未使用--- }


    // ---未使用--- 前台查詢（分頁）
    // ---未使用--- @Override
    // ---未使用--- public Page<ActVO> searchActs(Specification<ActVO> spec, Pageable pageable) {
    // ---未使用---     return actRepo.findAll(spec, pageable);
    // ---未使用--- }

    // ---未使用--- @Override
    // ---未使用--- public Page<ActCardDTO> searchActsAsCard(Specification<ActVO> spec, Pageable pageable) {
    // ---未使用---     Page<ActVO> actPage = actRepo.findAll(spec, pageable);
    // ---未使用---     return actPage.map(ActCardDTO::fromVO);
    // ---未使用--- }

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
                org.springframework.data.domain.Sort.Order.desc("actId")
            )
        );
        Page<ActVO> actPage = actRepo.findAll(spec, sortedPageable);
        
        // 轉換為 ActCardDTO 並加入行程資訊
        return actPage.map(actVO -> {
            ActCardDTO dto = ActCardDTO.fromVO(actVO);
            // 如果有行程ID，查詢行程資訊
            if (actVO.getItnId() != null) {
                ItineraryVO itinerary = getItineraryById(actVO.getItnId());
                if (itinerary != null) {
                    dto.setItnName(itinerary.getItnName());
                    dto.setItnDesc(itinerary.getItnDesc());
                }
            }
            return dto;
        });
    }

    //分頁查詢已公開活動（含當前用戶參與狀態）
    @Override
    public Page<ActCardDTO> searchPublicActs(Byte recruitStatus, String actName, 
                                           Integer hostId, LocalDateTime actStart, 
                                           Integer maxCap, Integer currentUserId, 
                                           Pageable pageable) {
        // 呼叫原有方法
        Page<ActCardDTO> result = searchPublicActs(recruitStatus, actName, hostId, actStart, maxCap, pageable);
        
        // 如果有當前用戶，查詢參與狀態
        if (currentUserId != null) {
            List<Integer> joinedActIds = partRepo.findActIdsByMemId(currentUserId);
            
            // 設定參與狀態
            result.getContent().forEach(dto -> {
                dto.setIsCurrentUserParticipant(joinedActIds.contains(dto.getActId()));
            });
        } else {
            // 未登入用戶，全部設為 false
            result.getContent().forEach(dto -> {
                dto.setIsCurrentUserParticipant(false);
            });
        }
        
        return result;
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

        // 當狀態變更為成團、取消、凍結時，發送通知給團員和團主
        if (status == ActStatus.FULL.getValue() || 
            status == ActStatus.CANCELLED.getValue() || 
            status == ActStatus.FROZEN.getValue()) {
            
            String statusMessage = "";
            switch (status) {
                case 1: // FULL
                    statusMessage = "已成團";
                    break;
                case 3: // CANCELLED
                    statusMessage = "已取消";
                    break;
                case 4: // FROZEN
                    statusMessage = "已凍結";
                    break;
                default:
                    statusMessage = "狀態已變更";
            }
            
            // 通知團員
            List<Integer> memberIds = partSvc.getParticipants(actId);
            for (Integer memId : memberIds) {
                NotificationVO noti =
                    new NotificationVO(
                        "活動狀態通知",
                        "活動「" + act.getActName() + "」" + statusMessage,
                        memId,
                        new java.sql.Timestamp(System.currentTimeMillis())
                    );
                notiSvc.addOneNoti(noti);
            }
            
            // 通知團主（如果團主不在團員列表中）
            if (!memberIds.contains(act.getHostId())) {
                NotificationVO hostNoti =
                    new NotificationVO(
                        "活動狀態通知",
                        "您的活動「" + act.getActName() + "」" + statusMessage,
                        act.getHostId(),
                        new java.sql.Timestamp(System.currentTimeMillis())
                    );
                notiSvc.addOneNoti(hostNoti);
            }
        }
    }

    @Override
    public void updateActivityStatusByHost(Integer actId, Byte newStatus, Integer operatorId) {
        ActVO act = actRepo.findById(actId)
                .orElseThrow(() -> new IllegalArgumentException("活動不存在"));
        Byte currentStatus = act.getRecruitStatus();
        LocalDateTime now = LocalDateTime.now();
        // 禁止對 FROZEN, CANCELLED, ENDED 狀態操作
        if (currentStatus == ActStatus.FROZEN.getValue() ||
                currentStatus == ActStatus.CANCELLED.getValue() ||
                currentStatus == ActStatus.ENDED.getValue()) {
            throw new IllegalStateException("活動已凍結、取消或結束，無法再更改狀態");
        }
        // 一般狀態切換
        if (now.isBefore(act.getActStart())) {
            // 活動未開始
            if ((currentStatus == ActStatus.OPEN.getValue() || currentStatus == ActStatus.FULL.getValue()) && newStatus == ActStatus.CANCELLED.getValue()) {
                act.setRecruitStatus(ActStatus.CANCELLED.getValue());
                actRepo.save(act);
                return;
            } else {
                throw new IllegalStateException("活動未開始僅能取消活動");
            }
        } else {
            // 活動已開始
            if (currentStatus == ActStatus.FULL.getValue() && newStatus == ActStatus.ENDED.getValue()) {
                act.setRecruitStatus(ActStatus.ENDED.getValue());
                actRepo.save(act);
                return;
            } else {
                throw new IllegalStateException("活動已開始僅能結束活動");
            }
        }
    }

    @Override
    public void freezeActivity(Integer actId, Integer adminId) {
        ActVO act = actRepo.findById(actId)
                .orElseThrow(() -> new IllegalArgumentException("活動不存在"));
        if (act.getRecruitStatus() == ActStatus.FROZEN.getValue()) {
            throw new IllegalStateException("活動已經是凍結狀態");
        }
        act.setRecruitStatus(ActStatus.FROZEN.getValue());
        actRepo.save(act);
        // 可加操作日誌
    }

    @Override
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
        actRepo.save(act);
        // 可加操作日誌
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
        tagSvc.saveActTags(actId, typeTag, cityTag);
    }

    //獲取活動標籤
    @Override
    public java.util.Map<String, ActTag> getActTags(Integer actId) {
        return tagSvc.getActTags(actId);
    }

    //根據標籤搜尋活動
    @Override
    public Page<ActCardDTO> searchByTags(List<ActTag> typeTags, List<ActTag> cityTags, Pageable pageable) {
        Set<Integer> actIds = new HashSet<>();
        // 只選地區
        if ((typeTags == null || typeTags.isEmpty()) && cityTags != null && !cityTags.isEmpty()) {
            for (ActTag cityTag : cityTags) {
                actIds.addAll(tagSvc.getActsByTag(cityTag));
            }
        }
        // 只選類型
        else if ((cityTags == null || cityTags.isEmpty()) && typeTags != null && !typeTags.isEmpty()) {
            for (ActTag typeTag : typeTags) {
                actIds.addAll(tagSvc.getActsByTag(typeTag));
            }
        }
        // 同時選類型與地區，取聯集
        else if (typeTags != null && !typeTags.isEmpty() && cityTags != null && !cityTags.isEmpty()) {
            for (ActTag typeTag : typeTags) {
                actIds.addAll(tagSvc.getActsByTag(typeTag));
            }
            for (ActTag cityTag : cityTags) {
                actIds.addAll(tagSvc.getActsByTag(cityTag));
            }
        }
        // 沒有標籤，回傳所有公開活動
        else {
            return searchPublicActs(null, null, null, null, null, pageable);
        }

        if (actIds.isEmpty()) {
            return new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0);
        }

        List<ActCardDTO> results = actRepo.findAllById(actIds).stream()
                .filter(act -> act.getIsPublic() == 1)
                .sorted((a, b) -> b.getActStart().compareTo(a.getActStart()))
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .map(ActCardDTO::fromVO)
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(results, pageable, actIds.size());
    }

    // ========== 行程相關方法 ==========

    //驗證行程是否存在
    public boolean validateItinerary(Integer itnId) {
        if (itnId == null) {
            return false;
        }
        return itnSvc.isItineraryExists(itnId);
    }

    //取得行程詳情
    public ItineraryVO getItineraryById(Integer itnId) {
        if (itnId == null) {
            return null;
        }
        return itnSvc.getItineraryById(itnId);
    }

    //取得所有公開行程
    public List<ItineraryVO> getPublicItineraries() {
        return itnSvc.getPublicItineraries();
    }


    // ---未使用--- ===========測試資料寫入DB，回傳 JSON 結果===========
    // ---未使用--- @Override
    // ---未使用--- public ActVO saveTestAct() {
    // ---未使用---     // 動態取得第一個公開行程，如果沒有則使用null
    // ---未使用---     Integer testItnId = null;
    // ---未使用---     List<com.toiukha.itinerary.model.ItineraryVO> publicItineraries = getPublicItineraries();
    // ---未使用---     if (!publicItineraries.isEmpty()) {
    // ---未使用---         testItnId = publicItineraries.get(0).getItnId();
    // ---未使用---     }
    // ---未使用---     
    // ---未使用---     ActVO actVo = new ActVO();
    // ---未使用---     actVo.setActName("測試活動");
    // ---未使用---     actVo.setActDesc("這是一個測試活動");
    // ---未使用---     actVo.setItnId(testItnId);
    // ---未使用---     actVo.setHostId(1);
    // ---未使用---     actVo.setSignupStart(LocalDateTime.now());
    // ---未使用---     actVo.setSignupEnd(LocalDateTime.now().plusDays(7));
    // ---未使用---     actVo.setMaxCap(10);
    // ---未使用---     actVo.setActStart(LocalDateTime.now().plusDays(14));
    // ---未使用---     actVo.setActEnd(LocalDateTime.now().plusDays(16));
    // ---未使用---     actVo.setIsPublic((byte) 1);
    // ---未使用---     actVo.setAllowCancel((byte) 1);
    // ---未使用---     actVo.setRecruitStatus(ActStatus.OPEN.getValue());
    // ---未使用---     
    // ---未使用---     return actRepo.save(actVo);
    // ---未使用--- }



}
