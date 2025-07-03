package com.toiukha.groupactivity.controller;

import com.toiukha.groupactivity.model.*;
import com.toiukha.groupactivity.security.AuthService;
import com.toiukha.groupactivity.service.ActService;
import com.toiukha.groupactivity.service.DefaultImageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    
    @Autowired
    private com.toiukha.groupactivity.service.ActStatusScheduler actStatusScheduler;

    // 新增活動
    @PostMapping("/add")
    public ResponseEntity<?> addAct(
            @Valid @ModelAttribute ActDTO actDto,
            BindingResult bindingResult,
            @RequestParam(value = "actImg", required = false) MultipartFile actImg,
            HttpServletRequest request) throws IOException {
        
        // 1. 登入驗證
        HttpSession session = request.getSession();
        AuthService.MemberInfo memberInfo = authService.getCurrentMember(session);
        
        if (!memberInfo.isLoggedIn()) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "error", "請先登入"
            ));
        }
        
        // 2. 設定團主ID（從session獲取，避免前端偽造）
        actDto.setHostId(memberInfo.getMemId());
        
        // 3. 移除圖片欄位的FieldError（避免MultipartFile與byte[]型別不符的錯誤）
        bindingResult = removeFieldError(actDto, bindingResult, "actImg");
        
        // 4. 檢查驗證錯誤
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = new ArrayList<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                String message = error.getDefaultMessage();
                // 過濾掉Spring框架的技術錯誤，只保留業務驗證錯誤
                if (message != null) {
                    errorMessages.add(message);
                }
            }
            // 如果沒有有效的業務錯誤訊息，回傳通用錯誤
            if (errorMessages.isEmpty()) {
                errorMessages.add("表單資料格式錯誤，請檢查輸入內容");
            }
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "errors", errorMessages
            ));
        }
        
        // 5. 處理圖片上傳
        if (actImg != null && !actImg.isEmpty()) {
            if (actImg.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "圖片檔案大小不能超過5MB"
                ));
            }
            actDto.setActImg(actImg.getBytes());
        }
        
        // 6. 設定標籤
        try {
            if (actDto.getActType() != null) {
                actDto.setTypeTag(ActTag.valueOf(actDto.getActType()));
            }
            if (actDto.getActCity() != null) {
                actDto.setCityTag(ActTag.valueOf(actDto.getActCity()));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "無效的活動類型或城市標籤"
            ));
        }
        
        // 7. 執行新增
        try {
            actSvc.addAct(actDto);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "活動新增成功",
                "hostId", memberInfo.getMemId()
            ));
        } catch (Exception e) {
            System.err.println("新增活動時發生錯誤: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "系統錯誤，請稍後再試"
            ));
        }
    }

    //修改活動
    @PutMapping("/update")
    public ResponseEntity<?> updateAct(
            @Valid @ModelAttribute ActDTO actDto,
            BindingResult bindingResult,
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
        ActVO existingAct = null;
        if (actDto.getActId() != null) {
            existingAct = actSvc.getOneAct(actDto.getActId());
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
            
            // === 設定團主ID（從現有活動資料獲取，確保不會被前端偽造） ===
            actDto.setHostId(existingAct.getHostId());
        }
        
        // === 移除編輯時不適用的驗證錯誤 ===
        // 移除圖片欄位的FieldError（避免MultipartFile與byte[]型別不符的錯誤）
        bindingResult = removeFieldError(actDto, bindingResult, "actImg");
        
        // 移除時間相關的 @Future 驗證錯誤（編輯時允許過去時間）
        bindingResult = removeTimeValidationErrors(actDto, bindingResult);
        
        // === 驗證錯誤處理 ===
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = new ArrayList<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                String message = error.getDefaultMessage();
                // 過濾掉Spring框架的技術錯誤，只保留業務驗證錯誤
                if (message != null && !isSpringTechnicalError(message)) {
                    errorMessages.add(message);
                }
            }
            // 如果沒有有效的業務錯誤訊息，回傳通用錯誤
            if (errorMessages.isEmpty()) {
                errorMessages.add("表單資料格式錯誤，請檢查輸入內容");
            }
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "errors", errorMessages
            ));
        }
        
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
        
        // === 設定標籤 ===
        try {
            if (actDto.getActType() != null) {
                actDto.setTypeTag(ActTag.valueOf(actDto.getActType()));
            }
            if (actDto.getActCity() != null) {
                actDto.setCityTag(ActTag.valueOf(actDto.getActCity()));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "無效的活動類型或城市標籤"
            ));
        }
        
        // === 業務邏輯處理 ===
        try {
            actSvc.updateAct(actDto);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "活動修改成功",
                "actId", actDto.getActId(),
                "hostId", actDto.getHostId()
            ));
            
        } catch (Exception e) {
            System.err.println("修改活動失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "系統錯誤，請稍後再試"
            ));
        }
    }

    //變更活動狀態
    @PutMapping("/{actId}/status/{status}")
    public String changeStatus(@PathVariable Integer actId,
                               @PathVariable Byte status,
                               @RequestParam Integer operatorId,
                               @RequestParam(defaultValue = "false") boolean admin,
                               HttpServletRequest request) {
        // 取得session並檢查登入狀態
        HttpSession session = request.getSession();
        AuthService.MemberInfo memberInfo = authService.getCurrentMember(session);

        actSvc.changeStatus(actId, status, operatorId, admin);
        return "statusChanged";
    }

    //查詢所有活動(測試用api)
    @GetMapping("/all")
    public List<ActCardDTO> getAllAct() {
        List<ActVO> allActs = actSvc.getAll();
        return allActs.stream().map(ActCardDTO::fromVO).toList();
    }

    //多條件查詢（限公開活動）
    @GetMapping("/search")
    public PageResDTO<ActCardDTO> search(@RequestParam(required = false) Byte recruitStatus,
                                         @RequestParam(required = false) String actName,
                                         @RequestParam(required = false) Integer hostId,
                                         @RequestParam(required = false) LocalDateTime actStart,
                                         @RequestParam(required = false) Integer maxCap,
                                         HttpServletRequest request,
                                         @PageableDefault(size = 9, sort = {"actStart"}, direction = Sort.Direction.DESC) Pageable pageable) {
        // 取得當前用戶
        AuthService.MemberInfo memberInfo = authService.getCurrentMember(request.getSession());
        Integer currentUserId = memberInfo.isLoggedIn() ? memberInfo.getMemId() : null;
        
        // 呼叫新的搜尋方法
        Page<ActCardDTO> page = actSvc.searchPublicActs(recruitStatus, actName, hostId, actStart, maxCap, currentUserId, pageable);
        return PageResDTO.of(page);
    }

    //根據標籤搜尋活動
    @GetMapping("/searchByTags")
    public PageResDTO<ActCardDTO> searchByTags(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "city", required = false) String city,
            @PageableDefault(size = 9, sort = {"actStart"}, direction = Sort.Direction.DESC) Pageable pageable) {

        ActTag typeTag = null;
        ActTag cityTag = null;

        try {
            if (type != null && !type.isEmpty()) {
                typeTag = ActTag.valueOf(type);
            }
            if (city != null && !city.isEmpty()) {
                cityTag = ActTag.valueOf(city);
            }
        } catch (IllegalArgumentException e) {
            // 無效的標籤值，使用預設搜尋
        }

        Page<ActCardDTO> page = actSvc.searchByTags(typeTag, cityTag, pageable);
        return PageResDTO.of(page);
    }

    //查詢單一活動
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

    //查詢我揪的團
    @GetMapping("/my/{hostId}")
    public ResponseEntity<?> getMyActs(@PathVariable Integer hostId, HttpServletRequest request) {
        HttpSession session = request.getSession();
        
        // 登入驗證：限團主身份
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
        
        List<ActVO> myActs = actSvc.getByHost(authorizedHostId);
        List<ActCardDTO> result = myActs.stream().map(ActCardDTO::fromVO).toList();
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", result,
            "hostId", authorizedHostId
        ));
    }
    
    //查詢我跟的團
    @GetMapping("/myJoin/{memId}")
    public ResponseEntity<?> getMyJoinedActs(@PathVariable Integer memId, HttpServletRequest request) {
        HttpSession session = request.getSession();
        
        // 登入驗證：限團圓身份
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
        
        List<ActCardDTO> result = actSvc.getJoinedActsAsCard(authorizedMemId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", result,
            "memId", authorizedMemId
        ));
    }

    // 會員刪除活動（僅限未公開活動）
    @DeleteMapping("/member/delete/{actId}")
    public ResponseEntity<Map<String, Object>> memberDeleteActivity(@PathVariable Integer actId, HttpServletRequest request) {
        try {
            // 權限驗證
            AuthService.MemberInfo memberInfo = authService.getCurrentMember(request.getSession());
            
            if (!memberInfo.isLoggedIn()) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "請先登入"
                ));
            }
            
            // 執行刪除
            actSvc.memDelete(actId, memberInfo.getMemId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "活動刪除成功"
            ));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "系統錯誤，請稍後再試"
            ));
        }
    }

    //================其他方法=================

    // 圖片顯示API - 參考members模組的錯誤處理模式
    @GetMapping("/image/{actId}")
    public void getActImage(
            @PathVariable Integer actId,
            HttpServletRequest req,
            HttpServletResponse res) throws IOException {
        
        res.setContentType("image/jpeg");
        
        try {
            byte[] imageBytes = actSvc.getActImageOnly(actId);
            
            if (imageBytes != null && imageBytes.length > 0) {
                res.getOutputStream().write(imageBytes);
            } else {
                // 沒有圖片時使用預設圖片
                byte[] defaultImage = defaultImageService.getDefaultImage();
                res.getOutputStream().write(defaultImage);
            }
            
        } catch (Exception e) {
            // 參考members模組的錯誤處理：發生錯誤時使用預設圖片
            System.err.println("獲取活動圖片錯誤 (actId: " + actId + "): " + e.getMessage());
            try {
                byte[] defaultImage = defaultImageService.getDefaultImage();
                res.getOutputStream().write(defaultImage);
            } catch (Exception defaultImageError) {
                System.err.println("載入預設圖片失敗: " + defaultImageError.getMessage());
                // 如果連預設圖片都無法載入，寫入空的圖片資料
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    // 取得所有活動標籤
    @GetMapping("/tags")
    public Map<String, Object> getTags() {
        List<Map<String, String>> types = ActTag.getTypesTags().stream()
                .map(tag -> Map.of("value", tag.name(), "label", tag.getDisplayName()))
                .collect(java.util.stream.Collectors.toList());

        List<Map<String, String>> cities = ActTag.getCityTags().stream()
                .map(tag -> Map.of("value", tag.name(), "label", tag.getDisplayName()))
                .collect(java.util.stream.Collectors.toList());

        return Map.of(
                "types", types,
                "cities", cities
        );
    }

    //獲取單一活動標籤
    @GetMapping("/tags/{actId}")
    public Map<String, Object> getActTags(@PathVariable Integer actId) {
        Map<String, ActTag> tags = actSvc.getActTags(actId);

        return Map.of(
                "actId", actId,
                "typeTag", Map.of(
                        "value", tags.get("type").name(),
                        "label", tags.get("type").getDisplayName()
                ),
                "cityTag", Map.of(
                        "value", tags.get("city").name(),
                        "label", tags.get("city").getDisplayName()
                )
        );
    }
    




    // ========================================
    // 測試相關端點 - 開發完成後可移除
    // ========================================

    /**
     * 測試端點 - 檢查資料庫狀態
     * 用途：檢查資料庫中的活動資料和圖片狀態
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
     * 測試端點 - 圖片上傳
     * 用途：測試前端傳送的 base64 圖片是否能正確解碼
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
     * 測試端點 - 圖片回傳
     * 用途：檢查特定活動的圖片資料格式和狀態
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
     * 測試端點 - 預設圖片
     * 用途：檢查預設圖片功能是否正常
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
    
    /**
     * 測試端點 - 活動狀態排程器
     * 用途：手動觸發活動狀態檢查（僅供開發測試用）
     */
    @PostMapping("/test-scheduler")
    public ResponseEntity<?> testScheduler(HttpServletRequest request) {
        try {
            // 權限驗證：僅限管理員或開發測試
            AuthService.MemberInfo memberInfo = authService.getCurrentMember(request.getSession());
            
            if (!memberInfo.isLoggedIn()) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "請先登入"
                ));
            }
            
            // 手動觸發排程器檢查
            actStatusScheduler.checkActStatus();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "活動狀態檢查已執行",
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "執行排程器時發生錯誤: " + e.getMessage()
            ));
        }
    }

    /**
     * 判斷是否為Spring技術錯誤訊息，過濾掉不適合顯示給用戶的技術訊息
     */
    private boolean isSpringTechnicalError(String message) {
        if (message == null) {
            return true;
        }
        
        // 過濾包含Spring框架技術關鍵字的錯誤訊息
        String[] technicalKeywords = {
            "Failed to convert",
            "PropertyEditor",
            "org.springframework",
            "StandardMultipartHttpServletRequest", 
            "Cannot convert value of type",
            "required type",
            "property editor",
            "type mismatch",
            "ConversionFailedException",
            "CustomNumberEditor",
            "returned inappropriate value"
        };
        
        String lowerMessage = message.toLowerCase();
        for (String keyword : technicalKeywords) {
            if (lowerMessage.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 去除 BindingResult 中某個欄位的 FieldError 紀錄
     * 用於解決 MultipartFile 與 byte[] 型別不符導致的驗證錯誤
     * 
     * @param actDto 活動DTO物件
     * @param result 驗證結果
     * @param removedFieldname 要移除錯誤的欄位名稱
     * @return 移除指定欄位錯誤後的 BindingResult
     */
    private BindingResult removeFieldError(ActDTO actDto, BindingResult result, String removedFieldname) {
        List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
                .filter(fieldError -> !fieldError.getField().equals(removedFieldname))
                .collect(java.util.stream.Collectors.toList());

        result = new BeanPropertyBindingResult(actDto, "actDTO");
        for (FieldError fieldError : errorsListToKeep) {
            result.addError(fieldError);
        }
        return result;
    }

    /**
     * 移除編輯時不適用的時間驗證錯誤
     * 編輯活動時允許過去的時間（例如已開始或已結束的活動）
     * 
     * @param actDto 活動DTO物件
     * @param result 驗證結果
     * @return 移除時間驗證錯誤後的 BindingResult
     */
    private BindingResult removeTimeValidationErrors(ActDTO actDto, BindingResult result) {
        List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
                .filter(fieldError -> {
                    String field = fieldError.getField();
                    String message = fieldError.getDefaultMessage();
                    
                    // 移除時間欄位的 @Future 驗證錯誤
                    if ((field.equals("actStart") || field.equals("actEnd")) && 
                        message != null && message.contains("日期必須是在今日(不含)之後")) {
                        return false;
                    }
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());

        result = new BeanPropertyBindingResult(actDto, "actDTO");
        for (FieldError fieldError : errorsListToKeep) {
            result.addError(fieldError);
        }
        return result;
    }

}