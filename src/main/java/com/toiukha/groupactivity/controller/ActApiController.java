package com.toiukha.groupactivity.controller;

import com.toiukha.groupactivity.model.ActDTO;
import com.toiukha.groupactivity.model.ActService;
import com.toiukha.groupactivity.model.ActSpecification;
import com.toiukha.groupactivity.model.ActVO;
import com.toiukha.groupactivity.model.DefaultImageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/act")
// 提供前端 AJAX 呼叫
public class ActApiController {

    @Autowired
    private ActService actSvc;

    @Autowired
    private DefaultImageService defaultImageService;

    @PostMapping("/add")
    public String addAct(@Valid @RequestBody ActDTO actDto) {
        // 從前端收到 DTO 交由 Service 處理
        actSvc.addAct(actDto);
        return "success";
    }

    @PutMapping("/update")
    public String updateAct(@Valid @RequestBody ActDTO actDto) {
        // 更新活動資料
        actSvc.updateAct(actDto);
        return "updated";
    }

    @GetMapping("/get/{id}")
    public ActVO getOne(@PathVariable Integer id) {
        return actSvc.getOneAct(id);
    }

    @GetMapping("/all")
    public List<ActVO> getAll() {
        return actSvc.getAll();
    }

    @GetMapping("/my/{hostId}")
    public List<ActVO> getMyActs(@PathVariable Integer hostId) {
        return actSvc.getByHost(hostId);
    }

    /**
     * 搜尋活動，支援多條件組合
     */
    @GetMapping("/search")
    public Page<ActVO> search(@RequestParam(required = false) Byte recruitStatus,
                              @RequestParam(required = false) String actName,
                              @RequestParam(required = false) Integer hostId,
                              @RequestParam(required = false) Byte isPublic,
                              @RequestParam(required = false) LocalDateTime actStart,
                              @RequestParam(required = false) Integer maxCap,
                              @PageableDefault(size = 20) Pageable pageable) {
        Specification<ActVO> spec = ActSpecification.buildSpec(recruitStatus, actName, hostId,
                isPublic, actStart, maxCap);
        return actSvc.searchActs(spec, pageable);
    }

    /**
     * 搜尋活動（不分頁版本）
     */
    @GetMapping("/search/all")
    public List<ActVO> searchAll(@RequestParam(required = false) Byte recruitStatus,
                                 @RequestParam(required = false) String actName,
                                 @RequestParam(required = false) Integer hostId,
                                 @RequestParam(required = false) Byte isPublic,
                                 @RequestParam(required = false) LocalDateTime actStart,
                                 @RequestParam(required = false) Integer maxCap) {
        Specification<ActVO> spec = ActSpecification.buildSpec(recruitStatus, actName, hostId,
                isPublic, actStart, maxCap);
        return actSvc.searchActsAll(spec);
    }

    /** 變更狀態 */
    @PutMapping("/{actId}/status/{status}")
    public String changeStatus(@PathVariable Integer actId,
                               @PathVariable Byte status,
                               @RequestParam Integer operatorId,
                               @RequestParam(defaultValue = "false") boolean admin) {
        actSvc.changeStatus(actId, status, operatorId, admin);
        return "statusChanged";
    }

    // ========================================
    // 測試相關端點 - 開發完成後可移除
    // ========================================

    /**
     * 測試端點 - 檢查資料庫狀態
     * 用途：檢查資料庫中的活動資料和圖片狀態
     * 移除時機：開發完成後
     */
    @GetMapping("/debug")
    public Object debug() {
        List<ActVO> allActs = actSvc.getAll();
        return Map.of(
            "totalCount", allActs.size(),
            "sampleData", allActs.stream().limit(3).map(act -> Map.of(
                "actId", act.getActId(),
                "actName", act.getActName(),
                "recruitStatus", act.getRecruitStatus(),
                "hasImage", act.getActImg() != null && act.getActImg().length > 0
            )).toList(),
            "timestamp", LocalDateTime.now()
        );
    }

    /**
     * 測試圖片上傳端點
     * 用途：測試前端傳送的 base64 圖片是否能正確解碼
     * 移除時機：開發完成後
     */
    @PostMapping("/test-image")
    public Object testImage(@RequestBody Map<String, Object> request) {
        String base64 = (String) request.get("actImgBase64");
        if (base64 != null && !base64.isEmpty()) {
            try {
                byte[] imageBytes = java.util.Base64.getDecoder().decode(base64);
                return Map.of(
                    "success", true,
                    "imageSize", imageBytes.length,
                    "message", "圖片解碼成功"
                );
            } catch (Exception e) {
                return Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "message", "圖片解碼失敗"
                );
            }
        } else {
            return Map.of(
                "success", false,
                "message", "沒有收到圖片資料"
            );
        }
    }

    /**
     * 測試圖片回傳端點
     * 用途：檢查特定活動的圖片資料格式和狀態
     * 移除時機：開發完成後
     */
    @GetMapping("/test-image-response/{actId}")
    public Object testImageResponse(@PathVariable Integer actId) {
        ActVO act = actSvc.getOneAct(actId);
        if (act != null) {
            return Map.of(
                "actId", act.getActId(),
                "actName", act.getActName(),
                "hasActImg", act.getActImg() != null && act.getActImg().length > 0,
                "actImgLength", act.getActImg() != null ? act.getActImg().length : 0,
                "message", "圖片資料檢查完成"
            );
        } else {
            return Map.of("error", "找不到活動");
        }
    }

    /**
     * 測試預設圖片端點
     * 用途：檢查預設圖片功能是否正常
     * 移除時機：開發完成後
     */
    @GetMapping("/test-default-image")
    public Object testDefaultImage() {
        try {
            byte[] defaultImage = defaultImageService.getDefaultImage();
            return Map.of(
                "success", true,
                "defaultImageSize", defaultImage.length,
                "message", "預設圖片載入成功"
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "error", e.getMessage(),
                "message", "預設圖片載入失敗"
            );
        }
    }
}