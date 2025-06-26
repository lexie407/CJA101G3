package com.toiukha.groupactivity.controller;

import com.toiukha.groupactivity.model.*;
import com.toiukha.groupactivity.security.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// 提供前端 AJAX 呼叫(送出表單、測試用api)
@RestController
@RequestMapping("/api/act")
public class ActApiController {

    @Autowired
    private ActService actSvc;

    @Autowired
    private DefaultImageService defaultImageService;
    
    @Autowired
    private AuthService authService;

    // 新增活動（AJAX專用，回傳JSON）
    @PostMapping("/add")
    public ResponseEntity<?> addAct(
            @RequestParam("actName") String actName,
            @RequestParam("actDesc") String actDesc,
            @RequestParam("itnId") Integer itnId,
            @RequestParam("hostId") Integer hostId,
            @RequestParam("signupStart") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime signupStart,
            @RequestParam("signupEnd") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime signupEnd,
            @RequestParam("actStart") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime actStart,
            @RequestParam("actEnd") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime actEnd,

            @RequestParam("maxCap") Integer maxCap,
            @RequestParam(value = "isPublic", required = false, defaultValue = "0") Byte isPublic,
            @RequestParam(value = "allowCancel", required = false, defaultValue = "1") Byte allowCancel,
            @RequestParam(value = "recruitStatus", required = false, defaultValue = "0") Byte recruitStatus,
            @RequestParam(value = "actImg", required = false) MultipartFile actImg,
            HttpServletRequest request) throws IOException {
        
        System.out.println("=== 開始處理新增活動請求 ===");
        System.out.println("actName: " + actName);
        System.out.println("itnId: " + itnId);
        System.out.println("hostId: " + hostId);
        System.out.println("signupStart: " + signupStart);
        System.out.println("actStart: " + actStart);
        
        // === 基本參數驗證 ===
        if (actName == null || actName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "活動名稱不能為空"
            ));
        }
        
        if (signupStart.isAfter(actStart)) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "報名開始時間不能晚於活動開始時間"
            ));
        }
        
        if (signupEnd.isAfter(actStart)) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "報名結束時間不能晚於活動開始時間"
            ));
        }
        
        if (actStart.isAfter(actEnd)) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "活動開始時間不能晚於活動結束時間"
            ));
        }
        
        // === 創建 ActDTO 對象 ===
        ActDTO actDto = new ActDTO();
        actDto.setActName(actName);
        actDto.setActDesc(actDesc);
        actDto.setItnId(itnId);
        actDto.setHostId(hostId);
        actDto.setSignupStart(signupStart);
        actDto.setSignupEnd(signupEnd);
        actDto.setActStart(actStart);
        actDto.setActEnd(actEnd);
        actDto.setMaxCap(maxCap);
        actDto.setIsPublic(isPublic);
        actDto.setAllowCancel(allowCancel);
        actDto.setRecruitStatus(recruitStatus);
        
        try {
            // === 會員認證和權限檢查 ===
            AuthService.MemberInfo memberInfo = authService.getCurrentMember(request.getSession());
            System.out.println("會員認證結果: " + memberInfo.isLoggedIn() + ", ID: " + memberInfo.getMemId());
            
            if (!memberInfo.isLoggedIn()) {
                System.out.println("會員未登入");
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "請先登入",
                    "redirectTo", "/members/login"
                ));
            }
            
            // === 設定活動團主（簡化邏輯：只能設定自己為團主）===
            Integer finalHostId = memberInfo.getMemId();
            actDto.setHostId(finalHostId);
            System.out.println("設定團主ID: " + finalHostId);
            
            // === 處理圖片上傳 ===
            if (actImg != null && !actImg.isEmpty()) {
                System.out.println("處理圖片上傳，大小: " + actImg.getSize() + " bytes");
                if (actImg.getSize() > 5 * 1024 * 1024) { // 5MB限制
                    System.out.println("圖片檔案過大");
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "圖片檔案大小不能超過5MB"
                    ));
                }
                actDto.setActImg(actImg.getBytes());
            } else {
                System.out.println("無圖片上傳");
            }
            
            // === 業務邏輯處理 ===
            System.out.println("準備呼叫 actSvc.addAct()");
            actSvc.addAct(actDto);
            System.out.println("actSvc.addAct() 執行完成");
            
            System.out.println("會員 " + memberInfo.getMemId() + " 成功新增活動: " + actDto.getActName());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "活動新增成功",
                "hostId", actDto.getHostId()
            ));
            
        } catch (Exception e) {
            System.err.println("新增活動失敗，詳細錯誤: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace(); // 印出完整的錯誤堆疊
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "系統錯誤，請稍後再試",
                "detail", e.getMessage() // 開發階段顯示詳細錯誤
            ));
        }
    }

    //修改活動
    @PutMapping("/update")
    public ResponseEntity<?> updateAct(
            @RequestParam("actId") Integer actId,
            @RequestParam("actName") String actName,
            @RequestParam("actDesc") String actDesc,
            @RequestParam("itnId") Integer itnId,
            @RequestParam("hostId") Integer hostId,
            @RequestParam("signupStart") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime signupStart,
            @RequestParam("signupEnd") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime signupEnd,
            @RequestParam("actStart") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime actStart,
            @RequestParam("actEnd") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime actEnd,
            @RequestParam("maxCap") Integer maxCap,
            @RequestParam("signupCnt") Integer signupCnt,
            @RequestParam(value = "isPublic", required = false, defaultValue = "0") Byte isPublic,
            @RequestParam(value = "allowCancel", required = false, defaultValue = "1") Byte allowCancel,
            @RequestParam(value = "recruitStatus", required = false, defaultValue = "0") Byte recruitStatus,
            @RequestParam(value = "actImg", required = false) MultipartFile actImg,
            HttpServletRequest request) throws IOException {
        
        // === 會員認證檢查 ===
        AuthService.MemberInfo memberInfo = authService.getCurrentMember(request.getSession());
        
        if (!memberInfo.isLoggedIn()) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "error", "請先登入"
            ));
        }
        
        // === 權限檢查：檢查是否可以修改此活動 ===
        if (actId != null) {
            ActVO existingAct = actSvc.getOneAct(actId);
            if (existingAct == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "找不到指定的活動"
                ));
            }
            
            if (!authService.canModifyActivity(request.getSession(), existingAct.getHostId())) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "error", "您沒有權限修改此活動"
                ));
            }
        }
        
        // === 創建ActDTO物件 ===
        ActDTO actDto = new ActDTO();
        actDto.setActId(actId);
        actDto.setActName(actName);
        actDto.setActDesc(actDesc);
        actDto.setItnId(itnId);
        actDto.setHostId(hostId);
        actDto.setSignupStart(signupStart);
        actDto.setSignupEnd(signupEnd);
        actDto.setActStart(actStart);
        actDto.setActEnd(actEnd);
        actDto.setMaxCap(maxCap);
        actDto.setSignupCnt(signupCnt);
        actDto.setIsPublic(isPublic);
        actDto.setAllowCancel(allowCancel);
        actDto.setRecruitStatus(recruitStatus);
        
        // === 處理圖片上傳 ===
        if (actImg != null && !actImg.isEmpty()) {
            if (actImg.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "圖片檔案大小不能超過5MB"
                ));
            }
            actDto.setActImg(actImg.getBytes());
        }
        
        // === 業務邏輯處理 ===
        try {
            actSvc.updateAct(actDto);
            
            System.out.println("會員 " + memberInfo.getMemId() + " 成功修改活動: " + actDto.getActId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "活動修改成功",
                "actId", actDto.getActId()
            ));
            
        } catch (Exception e) {
            System.err.println("修改活動失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "系統錯誤，請稍後再試"
            ));
        }
    }

    //查詢所有活動(測試用api) - 改為回傳ActCardDTO避免序列化byte[]
    @GetMapping("/all")
    public List<ActCardDTO> getAllAct() {
        List<ActVO> allActs = actSvc.getAll();
        return allActs.stream().map(this::convertToCardDTO).toList();
    }

    //查詢單一活動 - 移除actImg欄位避免序列化問題
    @GetMapping("/get/{actId}")
    public Map<String, Object> getOneAct(@PathVariable Integer actId) {
        ActVO actVo = actSvc.getOneAct(actId);
        if (actVo == null) {
            return Map.of("error", "找不到活動");
        }
        
        // 手動構建回應，排除actImg欄位
        Map<String, Object> result = new HashMap<>();
        result.put("actId", actVo.getActId());
        result.put("actName", actVo.getActName());
        result.put("actDesc", actVo.getActDesc() != null ? actVo.getActDesc() : "");
        result.put("itnId", actVo.getItnId());
        result.put("hostId", actVo.getHostId());
        result.put("signupStart", actVo.getSignupStart());
        result.put("signupEnd", actVo.getSignupEnd());
        result.put("maxCap", actVo.getMaxCap());
        result.put("signupCnt", actVo.getSignupCnt() != null ? actVo.getSignupCnt() : 0);
        result.put("actStart", actVo.getActStart());
        result.put("actEnd", actVo.getActEnd());
        result.put("isPublic", actVo.getIsPublic());
        result.put("allowCancel", actVo.getAllowCancel());
        result.put("recruitStatus", actVo.getRecruitStatus());
        return result;
    }

    //我揪的團 - 加入安全驗證，避免序列化byte[]
    @GetMapping("/my/{hostId}")
    public ResponseEntity<?> getMyActs(@PathVariable Integer hostId, HttpServletRequest request) {
        HttpSession session = request.getSession();
        
        // 安全驗證：檢查用戶是否有權限查看指定的活動列表
        Integer authorizedHostId = authService.getAuthorizedHostId(session, hostId);
        
        if (authorizedHostId == null) {
            AuthService.MemberInfo memberInfo = authService.getCurrentMember(session);
            
            if (!memberInfo.isLoggedIn()) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "請先登入",
                    "redirectTo", "/members/login"
                ));
            } else {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "error", "您沒有權限查看此會員的活動列表",
                    "redirectTo", "/act/member/listMy/" + memberInfo.getMemId()
                ));
            }
        }
        
        // 測試用：印出安全驗證結果
        System.out.println("=== Security Check: API MyList Access ===");
        System.out.println("URI: /api/act/my/" + hostId);
        System.out.println("Requested HostId: " + hostId);
        System.out.println("Authorized HostId: " + authorizedHostId);
        System.out.println("=======================================");
        
        List<ActVO> myActs = actSvc.getByHost(authorizedHostId);
        List<ActCardDTO> result = myActs.stream().map(this::convertToCardDTO).toList();
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", result,
            "hostId", authorizedHostId
        ));
    }
    
    //我跟的團 - 加入安全驗證，回傳 ActCardDTO 避免序列化 byte[]
    @GetMapping("/myJoin/{memId}")
    public ResponseEntity<?> getMyJoinedActs(@PathVariable Integer memId, HttpServletRequest request) {
        HttpSession session = request.getSession();
        
        // 安全驗證：檢查用戶是否有權限查看指定的參加活動列表
        Integer authorizedMemId = authService.getAuthorizedMemId(session, memId);
        
        if (authorizedMemId == null) {
            AuthService.MemberInfo memberInfo = authService.getCurrentMember(session);
            
            if (!memberInfo.isLoggedIn()) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "請先登入",
                    "redirectTo", "/members/login"
                ));
            } else {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "error", "您沒有權限查看此會員的參加活動列表",
                    "redirectTo", "/act/member/listMyJoin/" + memberInfo.getMemId()
                ));
            }
        }
        
        // 測試用：印出安全驗證結果
        System.out.println("=== Security Check: API MyJoin Access ===");
        System.out.println("URI: /api/act/myJoin/" + memId);
        System.out.println("Requested MemId: " + memId);
        System.out.println("Authorized MemId: " + authorizedMemId);
        System.out.println("==========================================");
        
        List<ActCardDTO> result = actSvc.getJoinedActsAsCard(authorizedMemId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", result,
            "memId", authorizedMemId
        ));
    }

    // 輔助方法：將ActVO轉換為ActCardDTO
    private ActCardDTO convertToCardDTO(ActVO actVo) {
        ActCardDTO cardDTO = new ActCardDTO();
        cardDTO.setActId(actVo.getActId());
        cardDTO.setActName(actVo.getActName());
        cardDTO.setActDesc(actVo.getActDesc());
        cardDTO.setActStart(actVo.getActStart());
        cardDTO.setSignupCnt(actVo.getSignupCnt() != null ? actVo.getSignupCnt() : 0);
        cardDTO.setMaxCap(actVo.getMaxCap());
        cardDTO.setRecruitStatus(actVo.getRecruitStatus());
        cardDTO.setHostId(actVo.getHostId());
        return cardDTO;
    }

    /**
     * 搜尋公開活動，支援多條件組合
     * 商業邏輯：強制只顯示公開活動（isPublic=1），私人活動視為草稿僅在 MyList 顯示
     * 前端無法透過參數篡改來查看私人活動
     */
    @GetMapping("/search")
    public Page<ActCardDTO> search(@RequestParam(required = false) Byte recruitStatus,
                                   @RequestParam(required = false) String actName,
                                   @RequestParam(required = false) Integer hostId,
                                   @RequestParam(required = false) LocalDateTime actStart,
                                   @RequestParam(required = false) Integer maxCap,
                                   @PageableDefault(size = 9, sort = {"actStart"}, direction = Sort.Direction.DESC) Pageable pageable) {
        // 使用專門的公開活動搜尋方法，不接受 isPublic 參數
        return actSvc.searchPublicActs(recruitStatus, actName, hostId, actStart, maxCap, pageable);
    }

    /**
     * 變更狀態 */
    @PutMapping("/{actId}/status/{status}")
    public String changeStatus(@PathVariable Integer actId,
                               @PathVariable Byte status,
                               @RequestParam Integer operatorId,
                               @RequestParam(defaultValue = "false") boolean admin,
                               HttpServletRequest request) {
        // 取得session並檢查登入狀態
        HttpSession session = request.getSession();
        Object member = session.getAttribute("member");
        // TODO: 若未登入可回傳未授權訊息
        // if (member == null) { return "unauthorized"; }
        
        // 測試用：印出session中的會員ID
        System.out.println("=== Test: Current Session Member ID ===");
        System.out.println("URI: /api/act/" + actId + "/status/" + status);
        System.out.println("Member: " + (member != null ? member : "Not logged in"));
        System.out.println("=====================================");
        
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

    // 圖片顯示API，瀏覽器直接<img src="/api/act/image/{actId}">
    @GetMapping("/image/{actId}")
    public void getActImage(
            @PathVariable Integer actId,
            HttpServletRequest req,
            HttpServletResponse res) throws IOException {
        res.setContentType("image/jpeg");
        byte[] imageBytes = null;
        ActVO act = actSvc.getOneAct(actId);
        if (act != null && act.getActImg() != null && act.getActImg().length > 0) {
            imageBytes = act.getActImg();
        } else {
            // 若無圖，回傳預設圖
            imageBytes = defaultImageService.getDefaultImage();
        }
        res.getOutputStream().write(imageBytes);
    }
}