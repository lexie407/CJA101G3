package com.toiukha.spot.controller.user;

import com.toiukha.spot.model.ApiResponse;
import com.toiukha.spot.model.SpotVO;
import com.toiukha.spot.service.SpotService;
import com.toiukha.spot.dto.SpotSearchRequest;
import com.toiukha.spot.dto.SpotDTO;
import com.toiukha.spot.dto.SpotMapper;
import com.toiukha.spot.service.SpotEnrichmentService;
import com.toiukha.spot.model.SpotImgVO;
import com.toiukha.spot.service.SpotImgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.util.Collections;

/**
 * 景點前台 API 控制器
 * 專門處理前台用戶的景點查詢和新增需求
 * 只提供上架景點的查詢功能和景點新增功能
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
@RestController
@RequestMapping("/api/spot/public")
public class SpotUserApiController {

    @Autowired
    private SpotService spotService;

    @Autowired
    private SpotMapper spotMapper;

    @Autowired
    private SpotEnrichmentService spotEnrichmentService;

    @Autowired
    private SpotImgService spotImgService;

    @Autowired
    private com.toiukha.spot.service.GooglePlacesService googlePlacesService;

    private static final Logger log = LoggerFactory.getLogger(SpotUserApiController.class);

    // ========== 1. 前台景點查詢API ==========

    /**
     * 取得所有上架景點 (前台用)
     * 支援地區與數量限制
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<SpotVO>>> getActiveSpots(
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "keyword", required = false) String keyword) {
        try {
            List<SpotVO> activeSpots;
            if (region != null && !region.isEmpty() && !region.equals("全部地區")) {
                List<String> cities = switch(region) {
                    case "北台灣" -> List.of("台北市", "新北市", "基隆市", "桃園市", "新竹市", "新竹縣", "宜蘭縣");
                    case "中台灣" -> List.of("台中市", "彰化縣", "南投縣", "雲林縣", "苗栗縣");
                    case "南台灣" -> List.of("高雄市", "台南市", "嘉義市", "嘉義縣", "屏東縣", "澎湖縣");
                    case "東台灣" -> List.of("花蓮縣", "台東縣");
                    default -> List.of(region);
                };
                if (keyword != null && !keyword.isEmpty()) {
                    activeSpots = spotService.searchPublicSpotsByCities(keyword, cities);
                } else {
                    activeSpots = spotService.getSpotsByCities(cities);
                }
            } else {
                if (keyword != null && !keyword.isEmpty()) {
                    activeSpots = spotService.searchPublicSpots(keyword);
                } else {
                    activeSpots = spotService.getActiveSpots();
                }
            }
            spotEnrichmentService.enrichSpotPictureUrlIfNeeded(activeSpots);
            if (limit != null && limit > 0 && activeSpots.size() > limit) {
                activeSpots = activeSpots.subList(0, limit);
            }
            return ResponseEntity.ok(ApiResponse.success("查詢成功", activeSpots));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("查詢失敗: " + e.getMessage()));
        }
    }

    /**
     * 根據ID取得景點詳情 (前台用)
     * 只能查詢上架狀態的景點
     * @param spotId 景點ID
     * @return API回應包含景點詳情
     */
    @GetMapping("/{spotId}")
    public ResponseEntity<ApiResponse<SpotVO>> getSpotById(@PathVariable Integer spotId) {
        try {
            SpotVO spot = spotService.getSpotById(spotId);
            
            // 檢查景點是否存在且為上架狀態
            if (spot == null) {
                return ResponseEntity.ok(ApiResponse.error("景點不存在"));
            }
            
            if (!spot.isActive()) {
                return ResponseEntity.ok(ApiResponse.error("景點尚未上架"));
            }
            
            // 處理圖片 URL
            spotEnrichmentService.enrichSpotPictureUrlIfNeeded(Collections.singletonList(spot));
            
            return ResponseEntity.ok(ApiResponse.success("查詢成功", spot));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("查詢失敗: " + e.getMessage()));
        }
    }

    // ========== 2. 前台搜尋API ==========

    /**
     * 關鍵字搜尋 (GET方式)
     * 只搜尋上架狀態的景點
     * @param keyword 搜尋關鍵字
     * @return API回應包含搜尋結果
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchSpots(@RequestParam String keyword) {
        try {
            List<SpotVO> spots = spotService.searchPublicSpots(keyword);
            spotEnrichmentService.enrichSpotPictureUrlIfNeeded(spots);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", spots
            ));
        } catch (Exception e) {
            log.error("前台搜尋景點時發生錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "message", "搜尋景點失敗"
                    ));
        }
    }

    /**
     * 複合搜尋 (POST方式)
     * 支援複雜搜尋條件，只搜尋上架景點
     * @param searchRequest 搜尋請求物件
     * @return API回應包含搜尋結果
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<SpotVO>>> advancedSearch(@RequestBody SpotSearchRequest searchRequest) {
        try {
            if (searchRequest == null || 
                (searchRequest.getKeyword() == null || searchRequest.getKeyword().trim().isEmpty())) {
                return ResponseEntity.ok(ApiResponse.error("搜尋條件不能為空"));
            }
            
            // 使用現有的搜尋方法，但只返回上架景點
            List<SpotVO> searchResults = spotService.searchPublicSpots(searchRequest.getKeyword().trim());
            spotEnrichmentService.enrichSpotPictureUrlIfNeeded(searchResults);
            
            return ResponseEntity.ok(ApiResponse.success("搜尋完成", searchResults));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("搜尋失敗: " + e.getMessage()));
        }
    }

    // ========== 3. 前台統計API ==========

    /**
     * 取得前台統計資訊
     * 只統計上架景點的資訊
     * @return API回應包含統計資料
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPublicStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 統計上架景點數量
            List<SpotVO> activeSpots = spotService.getActiveSpots();
            stats.put("totalActiveSpots", activeSpots.size());
            
            return ResponseEntity.ok(ApiResponse.success("統計資料查詢成功", stats));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("統計資料查詢失敗: " + e.getMessage()));
        }
    }

    // ========== 4. 地圖相關API ==========

    /**
     * 取得地圖用景點資料
     * 只返回有座標且上架的景點
     * @return API回應包含地圖景點資料
     */
    @GetMapping("/map-data")
    public ResponseEntity<ApiResponse<List<SpotVO>>> getMapSpots() {
        try {
            List<SpotVO> activeSpots = spotService.getActiveSpots();
            
            // 過濾出有有效座標的景點
            List<SpotVO> mapSpots = activeSpots.stream()
                .filter(spot -> spot.hasValidCoordinates())
                .toList();
            spotEnrichmentService.enrichSpotPictureUrlIfNeeded(mapSpots);
            
            return ResponseEntity.ok(ApiResponse.success("地圖資料查詢成功", mapSpots));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("地圖資料查詢失敗: " + e.getMessage()));
        }
    }

    // ========== 5. 前台景點新增API ==========

    /**
     * 用戶新增景點 (前台用)
     * 用戶新增的景點預設為待審核狀態，需要管理員審核後才能上架
     * @param spotDTO 景點資料傳輸物件
     * @param session HTTP Session
     * @return API回應
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<SpotVO>> submitSpot(
            @Valid @RequestBody SpotDTO spotDTO, 
            HttpSession session) {
        try {
            // 檢查會員是否已登入
            Integer currentUserId = getCurrentUserId(session);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("請先登入"));
            }
            
            // 檢查名稱是否重複
            if (spotService.existsBySpotNameAndSpotLoc(spotDTO.getSpotName(), spotDTO.getSpotLoc())) {
                return ResponseEntity.ok(ApiResponse.error("景點名稱已存在，請使用其他名稱"));
            }
            
            // 轉換DTO為VO
            SpotVO spotVO = spotMapper.toVO(spotDTO);
            
            // 設定前台新增的特殊屬性
            spotVO.setCrtId(currentUserId);      // 設定建立者ID
            spotVO.setSpotStatus((byte) 0);      // 預設為待審核狀態
            
            // 新增景點
            SpotVO savedSpot = spotService.addSpot(spotVO);
            
            return ResponseEntity.ok(ApiResponse.success("景點新增成功，已送審中", savedSpot));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("景點新增失敗: " + e.getMessage()));
        }
    }

    /**
     * 用戶新增景點 (表單+多圖)
     */
    @PostMapping("/submit-form")
    public ResponseEntity<Map<String, Object>> submitSpotForm(
            @Valid SpotDTO spotDTO,
            @RequestParam(value = "multiImages", required = false) List<MultipartFile> multiImages,
            @RequestParam(value = "multiImageDescs", required = false) List<String> multiImageDescs,
            HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        final int MAX_IMAGES = 8;
        final long MAX_SIZE = 2 * 1024 * 1024L;
        final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/jpg");
        try {
            // 檢查會員是否已登入
            Integer currentUserId = getCurrentUserId(session);
            if (currentUserId == null) {
                resp.put("success", false);
                resp.put("message", "請先登入");
                return ResponseEntity.status(401).body(resp);
            }
            
            // 移除圖片必須上傳的驗證
            // 如果有上傳圖片，才進行圖片驗證
            if (multiImages != null && !multiImages.isEmpty()) {
                int validImageCount = 0;
                for (MultipartFile mf : multiImages) {
                    if (mf != null && !mf.isEmpty()) validImageCount++;
                }
                
                if (validImageCount > MAX_IMAGES) {
                    resp.put("success", false);
                    resp.put("message", "最多只能上傳" + MAX_IMAGES + "張圖片");
                    return ResponseEntity.ok(resp);
                }
                
                // 驗證圖片格式和大小
                for (MultipartFile mf : multiImages) {
                    if (mf == null || mf.isEmpty()) continue;
                    if (!ALLOWED_TYPES.contains(mf.getContentType())) {
                        resp.put("success", false);
                        resp.put("message", "僅允許 JPG、PNG 格式圖片");
                        return ResponseEntity.ok(resp);
                    }
                    if (mf.getSize() > MAX_SIZE) {
                        resp.put("success", false);
                        resp.put("message", "單張圖片不可超過2MB");
                        return ResponseEntity.ok(resp);
                    }
                }
            }
            
            // 檢查名稱是否重複
            if (spotService.existsBySpotNameAndSpotLoc(spotDTO.getSpotName(), spotDTO.getSpotLoc())) {
                resp.put("success", false);
                resp.put("message", "同名同地點的景點已存在，請使用其他名稱或地點");
                return ResponseEntity.ok(resp);
            }
            
            // 轉換DTO為VO
            SpotVO spotVO = spotMapper.toVO(spotDTO);
            spotVO.setCrtId(currentUserId);
            spotVO.setSpotStatus((byte) 0); // 待審核
            SpotVO savedSpot = spotService.addSpot(spotVO);
            
            // 儲存多圖（如果有的話）
            String firstImagePath = null;
            if (multiImages != null && !multiImages.isEmpty()) {
                for (int i = 0; i < multiImages.size(); i++) {
                    MultipartFile mf = multiImages.get(i);
                    if (mf == null || mf.isEmpty()) continue;
                    String fileName = UUID.randomUUID() + "_" + mf.getOriginalFilename();
                    String uploadDir = "src/main/resources/static/images/spot/";
                    File dir = new File(uploadDir);
                    if (!dir.exists()) dir.mkdirs();
                    File dest = new File(uploadDir + fileName);
                    Files.copy(mf.getInputStream(), Paths.get(dest.toURI()));
                    
                    String imgPath = "/images/spot/" + fileName;
                    
                    // 儲存到 SpotImgVO 表
                    SpotImgVO img = new SpotImgVO();
                    img.setSpotId(savedSpot.getSpotId());
                    img.setImgPath(imgPath);
                    if (multiImageDescs != null && i < multiImageDescs.size())
                        img.setImgDesc(multiImageDescs.get(i));
                    img.setImgTime(java.time.LocalDateTime.now());
                    spotImgService.saveImage(img);
                    
                    // 記錄第一張圖片的路徑
                    if (i == 0) {
                        firstImagePath = imgPath;
                    }
                }
                
                // 更新景點的 firstPictureUrl（如果有上傳圖片）
                if (firstImagePath != null) {
                    savedSpot.setFirstPictureUrl(firstImagePath);
                    spotService.updateSpot(savedSpot);
                }
            }
            
            resp.put("success", true);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "景點新增失敗: " + e.getMessage());
            return ResponseEntity.ok(resp);
        }
    }

    /**
     * 前台 Google Place Info 查詢 API
     * 提供自動帶入地區、電話、網站功能
     */
    @PostMapping("/google-place-info")
    public Map<String, Object> getGooglePlaceInfo(@RequestBody Map<String, String> req) {
        String name = req.get("name");
        String address = req.get("address");
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 基本驗證
            if (name == null || name.trim().isEmpty() || address == null || address.trim().isEmpty()) {
                result.put("success", false);
                result.put("error", "景點名稱和地址不能為空");
                return result;
            }

            // 先查詢 placeId
            var placeInfo = googlePlacesService.searchPlace(name, address, null, null);
            
            // 驗證搜尋結果
            if (placeInfo != null && placeInfo.getPlaceId() != null) {
                var details = googlePlacesService.getPlaceDetails(placeInfo.getPlaceId());
                
                // 驗證電話格式
                String phone = details != null ? details.getPhoneNumber() : null;
                if (phone != null) {
                    // 移除所有非數字字元
                    phone = phone.replaceAll("[^0-9+]", "");
                    // 如果不是合理的電話號碼長度，設為 null
                    if (phone.length() < 8 || phone.length() > 15) {
                        phone = null;
                    }
                }
                
                // 驗證網站格式
                String website = details != null ? details.getWebsite() : null;
                if (website != null && !website.matches("^https?://.*")) {
                    if (website.matches("^[\\w-]+(\\.[\\w-]+)+.*")) {
                        website = "http://" + website;
                    } else {
                        website = null;
                    }
                }
                
                result.put("success", true);
                result.put("phoneNumber", phone);
                result.put("website", website);
                result.put("message", "已找到相符的地點資訊");
            } else {
                result.put("success", false);
                result.put("error", "找不到完全符合的地點資訊");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "查詢發生錯誤: " + e.getMessage());
        }
        return result;
    }

    // ========== 私有輔助方法 ==========

    /**
     * 取得當前登入用戶ID
     * 整合會員模組的登入系統
     * @param session HTTP Session
     * @return 用戶ID，如果未登入則返回null
     */
    private Integer getCurrentUserId(HttpSession session) {
        Object memberObj = session.getAttribute("member");
        if (memberObj instanceof com.toiukha.members.model.MembersVO) {
            com.toiukha.members.model.MembersVO member = (com.toiukha.members.model.MembersVO) memberObj;
            return member.getMemId();
        }
        return null;
    }
} 