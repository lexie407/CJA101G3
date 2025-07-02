package com.toiukha.spot.service;

import com.toiukha.spot.model.SpotRepository;
import com.toiukha.spot.model.SpotVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 景點業務邏輯服務
 * 提供景點相關的業務操作
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
@Service
public class SpotService {

    private static final Logger logger = LoggerFactory.getLogger(SpotService.class);

    @Autowired
    private SpotRepository spotRepository;
    
    @Autowired
    private GeocodeService geocodeService;
    
    @Autowired(required = false) // 設為非必要依賴，避免啟動失敗
    private com.toiukha.notification.model.NotificationService notificationService;
    
    @PersistenceContext
    private EntityManager entityManager;

    // ========== 1. 景點基本 CRUD 操作 ==========

    /**
     * 新增景點
     * @param spotVO 景點資料
     * @return 儲存後的景點資料
     */
    @Transactional
    public SpotVO addSpot(SpotVO spotVO) {
        // 設定建立時間
        spotVO.setSpotCreateAt(LocalDateTime.now());
        // 預設為待審核狀態
        if (spotVO.getSpotStatus() == null) {
            spotVO.setSpotStatus((byte) 0);
        }
        // 🚀 新功能：自動獲取座標
        if (spotVO.getSpotLat() == null || spotVO.getSpotLng() == null) {
            if (spotVO.getSpotLoc() != null && !spotVO.getSpotLoc().trim().isEmpty()) {
                try {
                    logger.info("正在為新景點 '{}' 自動獲取座標...", spotVO.getSpotName());
                    double[] coordinates = geocodeService.getCoordinatesForSpot(spotVO);
                    if (coordinates != null) {
                        spotVO.setSpotLat(coordinates[0]);
                        spotVO.setSpotLng(coordinates[1]);
                        logger.info("景點 '{}' 座標獲取成功: [{}, {}]", 
                                   spotVO.getSpotName(), coordinates[0], coordinates[1]);
                    } else {
                        logger.warn("景點 '{}' 座標獲取失敗，地址: {}", 
                                   spotVO.getSpotName(), spotVO.getSpotLoc());
                    }
                } catch (Exception e) {
                    logger.error("景點 '{}' 座標獲取過程發生錯誤: {}", 
                               spotVO.getSpotName(), e.getMessage());
                    // 不影響景點新增，繼續儲存
                }
            }
        }
        // 直接存，讓 DB 自動產生主鍵
        return spotRepository.save(spotVO);
    }

    /**
     * 更新景點資料
     * @param spotVO 要更新的景點資料
     * @return 更新後的景點資料
     */
    @Transactional
    public SpotVO updateSpot(SpotVO spotVO) {
        // 設定更新時間
        spotVO.setSpotUpdatedAt(LocalDateTime.now());
        return spotRepository.save(spotVO);
    }

    /**
     * 根據ID刪除景點
     * @param spotId 景點ID
     */
    @Transactional
    public void deleteSpot(Integer spotId) {
        spotRepository.deleteById(spotId);
    }

    /**
     * 根據ID查詢景點
     * @param spotId 景點ID
     * @return 景點資料，不存在則返回null
     */
    public SpotVO getSpotById(Integer spotId) {
        Optional<SpotVO> optional = spotRepository.findById(spotId);
        return optional.orElse(null);
    }

    /**
     * 查詢所有景點 (後台管理用)
     * @return 所有景點列表
     */
    public List<SpotVO> getAllSpots() {
        List<SpotVO> spots = spotRepository.findAll();
        return spots != null ? spots : new ArrayList<>();
    }

    /**
     * 查詢所有景點（分頁）
     * @param pageable 分頁參數
     * @return 景點的分頁結果
     */
    public Page<SpotVO> getAllSpots(Pageable pageable) {
        return spotRepository.findAll(pageable);
    }

    // ========== 2. 狀態管理 ==========

    /**
     * 景點上架（審核通過）
     * @param spotId 景點ID
     * @return 操作是否成功
     */
    @Transactional
    public boolean activateSpot(Integer spotId) {
        Optional<SpotVO> optional = spotRepository.findById(spotId);
        if (optional.isPresent()) {
            SpotVO spot = optional.get();
            byte oldStatus = spot.getSpotStatus();
            spot.setSpotStatus((byte) 1);
            spot.setSpotUpdatedAt(LocalDateTime.now());
            SpotVO savedSpot = spotRepository.save(spot);
            
            // 如果是從待審核狀態(0)變為上架狀態(1)，發送通過通知
            if (oldStatus == 0) {
                sendApprovalNotification(savedSpot);
            }
            
            return true;
        }
        return false;
    }

    /**
     * 景點下架（下架）
     * @param spotId 景點ID
     * @return 操作是否成功
     */
    @Transactional
    public boolean deactivateSpot(Integer spotId) {
        Optional<SpotVO> optional = spotRepository.findById(spotId);
        if (optional.isPresent()) {
            SpotVO spot = optional.get();
            spot.setSpotStatus((byte) 3); // 3=下架
            spot.setSpotUpdatedAt(LocalDateTime.now());
            spotRepository.save(spot);
            return true;
        }
        return false;
    }

    /**
     * 景點退回（審核不通過）
     * @param spotId 景點ID
     * @param reason 退回原因
     * @param remark 補充說明
     * @return 操作是否成功
     */
    @Transactional
    public boolean rejectSpot(Integer spotId, String reason, String remark) {
        Optional<SpotVO> optional = spotRepository.findById(spotId);
        if (optional.isPresent()) {
            SpotVO spot = optional.get();
            spot.setSpotStatus((byte) 2); // 2=退回
            
            // 記錄退回原因
            String fullRemark = reason + (remark != null && !remark.isBlank() ? (" - " + remark) : "");
            try {
                java.lang.reflect.Method m = spot.getClass().getMethod("setSpotAuditRemark", String.class);
                m.invoke(spot, fullRemark);
            } catch (Exception ignore) {}
            
            spot.setSpotUpdatedAt(LocalDateTime.now());
            SpotVO savedSpot = spotRepository.save(spot);
            
            // 發送退回通知給投稿用戶
            sendRejectionNotification(savedSpot, fullRemark);
            
            return true;
        }
        return false;
    }

    /**
     * 發送景點退回通知給用戶
     * TODO: 需與通知模組組員溝通以下事項：
     * 1. NotificationService 的正確介面和方法名稱
     * 2. NotificationVO 的建構方式和欄位名稱
     * 3. 通知狀態值的定義（0=未讀是否正確）
     * 4. 通知內容格式是否符合需求
     * 
     * @param spot 被退回的景點
     * @param reason 退回原因
     */
    private void sendRejectionNotification(SpotVO spot, String reason) {
        try {
            // 檢查通知服務是否可用且景點有建立者ID
            if (notificationService != null && spot.getCrtId() != null) {
                logger.info("準備發送景點退回通知: 用戶ID={}, 景點={}, 原因={}", 
                           spot.getCrtId(), spot.getSpotName(), reason);
                
                // TODO: 以下程式碼需要與通知組員確認介面後再上架
                /*
                // 建立通知物件
                com.toiukha.notification.model.NotificationVO notification = 
                    new com.toiukha.notification.model.NotificationVO();
                
                notification.setMemId(spot.getCrtId());
                notification.setAdminId(1); // 系統管理員ID
                notification.setNotiTitle("景點投稿審核結果");
                notification.setNotiCont(String.format(
                    "您投稿的景點「%s」經審核後未能通過。\n\n退回原因：%s\n\n您可以修改後重新投稿，謝謝！",
                    spot.getSpotName(), reason
                ));
                notification.setNotiSendAt(new java.sql.Timestamp(System.currentTimeMillis()));
                notification.setNotiStatus((byte) 0); // 0=未讀
                
                // 發送通知
                notificationService.addOneNoti(notification);
                */
                
                logger.info("景點退回通知已準備完成，等待通知模組整合");
            } else {
                logger.debug("通知服務未上架或景點無建立者ID，跳過通知發送");
            }
        } catch (Exception e) {
            logger.warn("發送景點退回通知失敗: {}", e.getMessage());
            // 不影響主要業務流程，只記錄警告
        }
    }

    /**
     * 發送景點通過審核通知給用戶
     * TODO: 需與通知模組組員溝通介面規格
     * 
     * @param spot 通過審核的景點
     */
    private void sendApprovalNotification(SpotVO spot) {
        try {
            // 檢查通知服務是否可用且景點有建立者ID
            if (notificationService != null && spot.getCrtId() != null) {
                logger.info("準備發送景點通過審核通知: 用戶ID={}, 景點={}", 
                           spot.getCrtId(), spot.getSpotName());
                
                // TODO: 以下程式碼需要與通知組員確認介面後再上架
                /*
                // 建立通知物件
                com.toiukha.notification.model.NotificationVO notification = 
                    new com.toiukha.notification.model.NotificationVO();
                
                notification.setMemId(spot.getCrtId());
                notification.setAdminId(1); // 系統管理員ID
                notification.setNotiTitle("景點投稿審核結果");
                notification.setNotiCont(String.format(
                    "恭喜！您投稿的景點「%s」已通過審核並成功上架。\n\n現在其他用戶可以在前台看到您的景點了，感謝您的投稿！",
                    spot.getSpotName()
                ));
                notification.setNotiSendAt(new java.sql.Timestamp(System.currentTimeMillis()));
                notification.setNotiStatus((byte) 0); // 0=未讀
                
                // 發送通知
                notificationService.addOneNoti(notification);
                */
                
                logger.info("景點通過審核通知已準備完成，等待通知模組整合");
            } else {
                logger.debug("通知服務未上架或景點無建立者ID，跳過通知發送");
            }
        } catch (Exception e) {
            logger.warn("發送景點通過審核通知失敗: {}", e.getMessage());
            // 不影響主要業務流程，只記錄警告
        }
    }

    // ========== 3. 前台查詢功能 ==========

    /**
     * 查詢所有上架景點 (前台用)
     * @return 上架景點列表
     */
    public List<SpotVO> getActiveSpots() {
        return spotRepository.findActiveSpots();
    }

    /**
     * 搜尋景點 (前台用)
     * @param keyword 搜尋關鍵字
     * @return 符合條件的上架景點列表
     */
    public List<SpotVO> searchSpots(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getActiveSpots();
        }
        return spotRepository.searchActiveSpots(keyword.trim());
    }

    /**
     * 搜尋景點（支援關鍵字和地區篩選）
     * @param keyword 搜尋關鍵字 (可為null)
     * @param region 地區篩選 (可為null)
     * @return 符合條件的景點列表
     */
    public List<SpotVO> searchSpotsWithFilters(String keyword, String region) {
        List<SpotVO> activeSpots = getActiveSpots();
        
        if (activeSpots.isEmpty()) {
            return activeSpots;
        }
        
        // 如果沒有任何篩選條件，返回所有活躍景點
        if ((keyword == null || keyword.trim().isEmpty()) && 
            (region == null || region.trim().isEmpty())) {
            return activeSpots;
        }
        
        List<SpotVO> filteredSpots = new ArrayList<>(activeSpots);
        
        // 關鍵字篩選
        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchKeyword = keyword.trim().toLowerCase();
            filteredSpots = filteredSpots.stream()
                .filter(spot -> 
                    (spot.getSpotName() != null && spot.getSpotName().toLowerCase().contains(searchKeyword)) ||
                    (spot.getSpotDesc() != null && spot.getSpotDesc().toLowerCase().contains(searchKeyword)) ||
                    (spot.getSpotLoc() != null && spot.getSpotLoc().toLowerCase().contains(searchKeyword))
                )
                .collect(java.util.stream.Collectors.toList());
        }
        
        // 地區篩選
        if (region != null && !region.trim().isEmpty()) {
            String searchRegion = region.trim();
            filteredSpots = filteredSpots.stream()
                .filter(spot -> {
                    if (spot.getSpotLoc() == null) return false;
                    String location = spot.getSpotLoc();
                    
                    // 根據地區分類進行篩選
                    switch (searchRegion) {
                        case "北部":
                            return location.contains("台北") || location.contains("臺北") || 
                                   location.contains("新北") || location.contains("基隆") || 
                                   location.contains("桃園") || location.contains("新竹");
                        case "中部":
                            return location.contains("台中") || location.contains("臺中") || 
                                   location.contains("彰化") || location.contains("南投") || 
                                   location.contains("雲林") || location.contains("苗栗");
                        case "南部":
                            return location.contains("台南") || location.contains("臺南") || 
                                   location.contains("高雄") || location.contains("嘉義") || 
                                   location.contains("屏東");
                        case "東部":
                            return location.contains("花蓮") || location.contains("台東") || 
                                   location.contains("臺東") || location.contains("宜蘭");
                        default:
                            // 如果是具體地區名稱，直接比對
                            return location.contains(searchRegion);
                    }
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        return filteredSpots;
    }

    /**
     * 查詢相關景點推薦
     * @param spotId 當前景點ID
     * @param limit 返回數量限制
     * @return 相關景點列表
     */
    public List<SpotVO> getRelatedSpots(Integer spotId, int limit) {
        try {
            SpotVO currentSpot = getSpotById(spotId);
            if (currentSpot == null) {
                return new ArrayList<>();
            }
            
            // 獲取所有活躍景點
            List<SpotVO> allSpots = getActiveSpots();
            
            // 過濾掉當前景點
            List<SpotVO> candidateSpots = allSpots.stream()
                .filter(spot -> !spot.getSpotId().equals(spotId))
                .collect(java.util.stream.Collectors.toList());
            
            if (candidateSpots.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 優先推薦同地區的景點
            String currentLocation = currentSpot.getSpotLoc();
            List<SpotVO> relatedSpots = new ArrayList<>();
            
            if (currentLocation != null && !currentLocation.trim().isEmpty()) {
                // 提取縣市資訊進行匹配
                String currentCity = extractCityFromLocation(currentLocation);
                
                // 同縣市的景點
                List<SpotVO> sameCitySpots = candidateSpots.stream()
                    .filter(spot -> {
                        if (spot.getSpotLoc() == null) return false;
                        String spotCity = extractCityFromLocation(spot.getSpotLoc());
                        return currentCity.equals(spotCity);
                    })
                    .limit(limit)
                    .collect(java.util.stream.Collectors.toList());
                
                relatedSpots.addAll(sameCitySpots);
            }
            
            // 如果同地區景點不足，補充其他景點
            if (relatedSpots.size() < limit) {
                int remaining = limit - relatedSpots.size();
                List<SpotVO> otherSpots = candidateSpots.stream()
                    .filter(spot -> !relatedSpots.contains(spot))
                    .limit(remaining)
                    .collect(java.util.stream.Collectors.toList());
                
                relatedSpots.addAll(otherSpots);
            }
            
            return relatedSpots;
            
        } catch (Exception e) {
            logger.error("查詢相關景點失敗 - spotId: {}, error: {}", spotId, e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 從地址中提取縣市名稱
     * @param location 完整地址
     * @return 縣市名稱
     */
    private String extractCityFromLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return "";
        }
        
        // 常見的縣市名稱模式
        String[] cities = {
            "台北市", "臺北市", "新北市", "桃園市", "台中市", "臺中市", 
            "台南市", "臺南市", "高雄市", "基隆市", "新竹市", "新竹縣",
            "苗栗縣", "彰化縣", "南投縣", "雲林縣", "嘉義市", "嘉義縣",
            "屏東縣", "宜蘭縣", "花蓮縣", "台東縣", "臺東縣", "澎湖縣",
            "金門縣", "連江縣"
        };
        
        for (String city : cities) {
            if (location.contains(city)) {
                return city;
            }
        }
        
        // 如果沒有找到完整縣市名，嘗試提取前3個字
        if (location.length() >= 3) {
            return location.substring(0, 3);
        }
        
        return location;
    }

    // ========== 4. 後台管理功能 ==========

    /**
     * 根據狀態查詢景點 (後台用)
     * @param status 景點狀態
     * @return 符合條件的景點列表
     */
    public List<SpotVO> getSpotsByStatus(Byte status) {
        return spotRepository.findBySpotStatus(status);
    }

    /**
     * 根據狀態查詢景點 (分頁)
     * @param status 景點狀態
     * @param pageable 分頁資訊
     * @return 景點分頁結果
     */
    public Page<SpotVO> findSpotsByStatus(Integer status, Pageable pageable) {
        return spotRepository.findBySpotStatus(status, pageable);
    }

    /**
     * 根據建立者查詢景點
     * @param crtId 建立者ID
     * @return 符合條件的景點列表
     */
    public List<SpotVO> getSpotsByCreator(Integer crtId) {
        return spotRepository.findByCrtId(crtId);
    }

    /**
     * 查詢所有待審核景點，並自動過濾不合格資料（名稱為空、描述過短、地點格式不符），不合格者自動退回
     * @return 合格的待審核景點列表
     */
    public List<SpotVO> getPendingSpotsWithAutoCheck() {
        List<SpotVO> pending = getSpotsByStatus((byte) 0);
        List<SpotVO> valid = new ArrayList<>();
        for (SpotVO spot : pending) {
            String reason = null;
            if (spot.getSpotName() == null || spot.getSpotName().trim().isEmpty()) {
                reason = "名稱為空";
            } else if (spot.getSpotDesc() == null || spot.getSpotDesc().trim().length() < 3) {
                reason = "描述過短";
            } else if (spot.getSpotLoc() == null || spot.getSpotLoc().trim().length() < 5) {
                reason = "地點格式不符";
            }
            if (reason != null) {
                rejectSpot(spot.getSpotId(), reason, "自動審核退回");
            } else {
                valid.add(spot);
            }
        }
        return valid;
    }

    // ========== 5. 驗證方法 ==========

    /**
     * 檢查景點名稱是否已存在
     * @param spotName 景點名稱
     * @return true 如果名稱已存在
     */
    public boolean existsBySpotName(String spotName) {
        return spotRepository.existsBySpotName(spotName);
    }

    /**
     * 檢查景點是否存在
     * @param spotId 景點ID
     * @return true 如果景點存在
     */
    public boolean existsById(Integer spotId) {
        return spotRepository.existsById(spotId);
    }

    /**
     * 根據政府資料ID檢查景點是否已存在
     * @param govtId 政府資料ID
     * @return true 如果景點已存在
     */
    public boolean existsByGovtId(String govtId) {
        return spotRepository.existsByGovtId(govtId);
    }

    /**
     * 根據縣市查詢上架景點
     * @param region 縣市名稱
     * @return 符合條件的景點列表
     */
    public List<SpotVO> getSpotsByRegion(String region) {
        return spotRepository.findActiveSpotsByRegion(region);
    }

    /**
     * 查詢有經緯度的上架景點 (用於地圖顯示)
     * @return 有座標的上架景點列表
     */
    public List<SpotVO> getActiveSpotsWithCoordinates() {
        return spotRepository.findActiveSpotsWithCoordinates();
    }

    /**
     * 查詢沒有座標的景點 (用於地理編碼)
     * @return 沒有座標的景點列表
     */
    public List<SpotVO> getSpotsWithoutCoordinates() {
        return spotRepository.findSpotsWithoutCoordinates();
    }

    /**
     * 後台複合搜尋 - 支援關鍵字、狀態、地區篩選和排序
     * @param keyword 搜尋關鍵字 (可為null或空字串)
     * @param status 景點狀態 (可為null表示不限)
     * @param region 地區 (可為null或空字串或"all"表示不限)
     * @param pageable 分頁和排序資訊
     * @return 符合條件的景點分頁結果
     */
    public Page<SpotVO> searchSpotsForAdmin(String keyword, Integer status, String region, Pageable pageable) {
        // 清理參數
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanRegion = (region != null && !region.trim().isEmpty() && !"all".equals(region)) ? region.trim() : null;
        
        logger.debug("後台搜尋參數 - keyword: {}, status: {}, region: {}", cleanKeyword, status, cleanRegion);
        
        return spotRepository.findByKeywordAndStatusAndRegion(cleanKeyword, status, cleanRegion, pageable);
    }

    /**
     * 後台複合搜尋 - 查詢所有符合條件的資料（不分頁，供前端JavaScript使用）
     * @param keyword 搜尋關鍵字 (可為null或空字串)
     * @param status 景點狀態 (可為null表示不限)
     * @param region 地區 (可為null或空字串或"all"表示不限)
     * @param sort 排序資訊
     * @return 符合條件的所有景點列表
     */
    public List<SpotVO> searchAllSpotsForAdmin(String keyword, Integer status, String region, Sort sort) {
        // 清理參數
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanRegion = (region != null && !region.trim().isEmpty() && !"all".equals(region)) ? region.trim() : null;
        
        logger.info("=== SpotService.searchAllSpotsForAdmin ===");
        logger.info("原始參數 - keyword: '{}', status: {}, region: '{}'", keyword, status, region);
        logger.info("清理後參數 - cleanKeyword: '{}', status: {}, cleanRegion: '{}'", cleanKeyword, status, cleanRegion);
        logger.info("排序: {}", sort);
        
        List<SpotVO> result = spotRepository.findAllByKeywordAndStatusAndRegion(cleanKeyword, status, cleanRegion, sort);
        logger.info("Repository 查詢結果數量: {}", result.size());
        
        // 也查詢總數量作為對照
        long totalCount = spotRepository.count();
        logger.info("資料庫總景點數量: {}", totalCount);
        
        return result;
    }
    
    /**
     * 獲取所有地區列表 (用於下拉選單)
     * @return 地區列表
     */
    public List<String> getAllRegions() {
        return spotRepository.findDistinctRegions();
    }

    /**
     * 從景點地址中提取地區資訊 (備用方案)
     * @return 地區列表
     */
    public List<String> getRegionsFromLocations() {
        return spotRepository.findDistinctRegionsFromLocation();
    }

    /**
     * 查詢所有已通過審核的景點（狀態1）- 用於景點列表
     * @return 已通過審核的景點列表
     */
    public List<SpotVO> getAllApprovedSpots() {
        return spotRepository.findApprovedSpots();
    }

    /**
     * 查詢所有已審核的景點（狀態1和2）- 用於其他管理功能
     * @return 已審核的景點列表
     */
    public List<SpotVO> getAllReviewedSpots() {
        return spotRepository.findReviewedSpots();
    }

    /**
     * 後台景點列表 - 只查詢已通過審核的景點（狀態1），過濾掉待審核（狀態0）和退回（狀態2）的景點
     * @param keyword 搜尋關鍵字 (可為null或空字串)
     * @param status 景點狀態 (只能是1，null表示不限)
     * @param region 地區 (可為null或空字串或"all"表示不限)
     * @param pageable 分頁和排序資訊
     * @return 符合條件的已通過審核景點分頁結果
     */
    public Page<SpotVO> searchApprovedSpotsForAdmin(String keyword, Integer status, String region, Pageable pageable) {
        // 清理參數
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanRegion = (region != null && !region.trim().isEmpty() && !"all".equals(region)) ? region.trim() : null;
        
        // 如果指定了狀態，確保只能是1
        Integer approvedStatus = null;
        if (status != null && status == 1) {
            approvedStatus = status;
        }
        
        logger.debug("後台搜尋已通過審核景點參數 - keyword: {}, status: {}, region: {}", cleanKeyword, approvedStatus, cleanRegion);
        
        return spotRepository.findApprovedByKeywordAndStatusAndRegion(cleanKeyword, approvedStatus, cleanRegion, pageable);
    }

    /**
     * 後台景點列表 - 查詢所有符合條件的已通過審核景點（不分頁，供前端JavaScript使用）
     * @param keyword 搜尋關鍵字 (可為null或空字串)
     * @param status 景點狀態 (只能是1，null表示不限)
     * @param region 地區 (可為null或空字串或"all"表示不限)
     * @param sort 排序資訊
     * @return 符合條件的所有已通過審核景點列表
     */
    public List<SpotVO> searchAllApprovedSpotsForAdmin(String keyword, Integer status, String region, Sort sort) {
        // 清理參數
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanRegion = (region != null && !region.trim().isEmpty() && !"all".equals(region)) ? region.trim() : null;
        
        // 如果指定了狀態，確保只能是1
        Integer approvedStatus = null;
        if (status != null && status == 1) {
            approvedStatus = status;
        }
        
        logger.info("=== SpotService.searchAllApprovedSpotsForAdmin ===");
        logger.info("原始參數 - keyword: '{}', status: {}, region: '{}'", keyword, status, region);
        logger.info("清理後參數 - cleanKeyword: '{}', approvedStatus: {}, cleanRegion: '{}'", cleanKeyword, approvedStatus, cleanRegion);
        logger.info("排序: {}", sort);
        
        List<SpotVO> result = spotRepository.findAllApprovedByKeywordAndStatusAndRegion(cleanKeyword, approvedStatus, cleanRegion, sort);
        logger.info("Repository 查詢結果數量: {}", result.size());
        
        return result;
    }

    /**
     * 後台複合搜尋 - 只查詢已審核的景點（狀態1、2、3），過濾掉待審核的景點（狀態0）
     * @param keyword 搜尋關鍵字 (可為null或空字串)
     * @param status 景點狀態 (可為null表示不限，但只能是1、2、3)
     * @param region 地區 (可為null或空字串或"all"表示不限)
     * @param pageable 分頁和排序資訊
     * @return 符合條件的已審核景點分頁結果
     */
    public Page<SpotVO> searchReviewedSpotsForAdmin(String keyword, Integer status, String region, Pageable pageable) {
        // 清理參數
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanRegion = (region != null && !region.trim().isEmpty() && !"all".equals(region)) ? region.trim() : null;
        
        // 如果指定了狀態，確保只能是1、2、3
        Integer reviewedStatus = null;
        if (status != null && (status == 1 || status == 2 || status == 3)) {
            reviewedStatus = status;
        }
        
        logger.debug("後台搜尋已審核景點參數 - keyword: {}, status: {}, region: {}", cleanKeyword, reviewedStatus, cleanRegion);
        
        return spotRepository.findReviewedByKeywordAndStatusAndRegion(cleanKeyword, reviewedStatus, cleanRegion, pageable);
    }

    /**
     * 後台複合搜尋 - 查詢所有符合條件的已審核景點（狀態1和2）（不分頁，供前端JavaScript使用）
     * @param keyword 搜尋關鍵字 (可為null或空字串)
     * @param status 景點狀態 (可為null表示不限，但只能是1或2)
     * @param region 地區 (可為null或空字串或"all"表示不限)
     * @param sort 排序資訊
     * @return 符合條件的所有已審核景點列表
     */
    public List<SpotVO> searchAllReviewedSpotsForAdmin(String keyword, Integer status, String region, Sort sort) {
        // 清理參數
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanRegion = (region != null && !region.trim().isEmpty() && !"all".equals(region)) ? region.trim() : null;
        
        // 如果指定了狀態，確保只能是1、2、3
        Integer reviewedStatus = null;
        if (status != null && (status == 1 || status == 2 || status == 3)) {
            reviewedStatus = status;
        }
        
        logger.info("=== SpotService.searchAllReviewedSpotsForAdmin ===");
        logger.info("原始參數 - keyword: '{}', status: {}, region: '{}'", keyword, status, region);
        logger.info("清理後參數 - cleanKeyword: '{}', reviewedStatus: {}, cleanRegion: '{}'", cleanKeyword, reviewedStatus, cleanRegion);
        logger.info("排序: {}", sort);
        
        List<SpotVO> result = spotRepository.findAllReviewedByKeywordAndStatusAndRegion(cleanKeyword, reviewedStatus, cleanRegion, sort);
        logger.info("Repository 查詢結果數量: {}", result.size());
        
        return result;
    }

    // ========== 6. 批量操作 ==========

    /**
     * 批次新增景點 (優化版本，用於大量資料匯入)
     * @param spotVOList 景點資料列表
     * @return 成功新增的景點列表
     */
    @Transactional
    public List<SpotVO> addSpotsInBatch(List<SpotVO> spotVOList) {
        if (spotVOList == null || spotVOList.isEmpty()) {
            return new ArrayList<>();
        }
        logger.info("開始批次新增 {} 個景點", spotVOList.size());
        LocalDateTime currentTime = LocalDateTime.now();
        for (SpotVO spot : spotVOList) {
            spot.setSpotCreateAt(currentTime);
            if (spot.getSpotStatus() == null) {
                spot.setSpotStatus((byte) 0);
            }
        }
        // 直接批次存，讓 DB 自動產生主鍵
        List<SpotVO> savedSpots = spotRepository.saveAll(spotVOList);
        logger.info("批次新增完成，成功儲存 {} 個景點", savedSpots.size());
        return savedSpots;
    }

    /**
     * 批次新增景點並自動獲取座標 (完整版本)
     * @param spotVOList 景點資料列表
     * @return 批次處理結果
     */
    @Transactional
    public BatchResult addSpotsWithGeocoding(List<SpotVO> spotVOList) {
        if (spotVOList == null || spotVOList.isEmpty()) {
            return new BatchResult(0, 0, 0, new ArrayList<>());
        }
        logger.info("開始批次新增 {} 個景點並自動獲取座標", spotVOList.size());
        BatchResult result = new BatchResult();
        List<SpotVO> spotsToSave = new ArrayList<>();
        List<SpotVO> spotsWithCoordinates = new ArrayList<>();
        LocalDateTime currentTime = LocalDateTime.now();
        for (SpotVO spot : spotVOList) {
            spot.setSpotCreateAt(currentTime);
            if (spot.getSpotStatus() == null) {
                spot.setSpotStatus((byte) 0);
            }
            if (spot.getSpotLat() == null || spot.getSpotLng() == null) {
                if (spot.getSpotLoc() != null && !spot.getSpotLoc().trim().isEmpty()) {
                    spotsWithCoordinates.add(spot);
                }
            }
            spotsToSave.add(spot);
        }
        List<SpotVO> savedSpots = spotRepository.saveAll(spotsToSave);
        result.setSuccessCount(savedSpots.size());
        if (!spotsWithCoordinates.isEmpty()) {
            processGeocodingAsync(spotsWithCoordinates, result);
        }
        logger.info("批次新增完成，成功儲存 {} 個景點，其中 {} 個需要獲取座標", 
                   savedSpots.size(), spotsWithCoordinates.size());
        return result;
    }

    /**
     * 非同步處理座標獲取
     * @param spots 需要獲取座標的景點列表
     * @param result 批次結果物件
     */
    private void processGeocodingAsync(List<SpotVO> spots, BatchResult result) {
        // 使用新的執行緒來處理座標獲取，避免阻塞主流程
        new Thread(() -> {
            int geocodingSuccess = 0;
            int geocodingError = 0;
            
            for (SpotVO spot : spots) {
                try {
                    double[] coordinates = geocodeService.getCoordinatesForSpot(spot);
                    if (coordinates != null) {
                        spot.setSpotLat(coordinates[0]);
                        spot.setSpotLng(coordinates[1]);
                        spot.setSpotUpdatedAt(LocalDateTime.now());
                        spotRepository.save(spot);
                        geocodingSuccess++;
                        
                        logger.debug("景點 '{}' 座標獲取成功: [{}, {}]", 
                                   spot.getSpotName(), coordinates[0], coordinates[1]);
                    } else {
                        geocodingError++;
                        logger.debug("景點 '{}' 座標獲取失敗", spot.getSpotName());
                    }
                } catch (Exception e) {
                    geocodingError++;
                    logger.warn("景點 '{}' 座標獲取過程發生錯誤: {}", 
                               spot.getSpotName(), e.getMessage());
                }
                
                // 避免過於頻繁的API調用
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            result.setGeocodingSuccessCount(geocodingSuccess);
            result.setGeocodingErrorCount(geocodingError);
            
            logger.info("座標獲取完成，成功: {}, 失敗: {}", geocodingSuccess, geocodingError);
        }).start();
    }

    /**
     * 批次處理結果類別
     */
    public static class BatchResult {
        private int successCount = 0;
        private int geocodingSuccessCount = 0;
        private int geocodingErrorCount = 0;
        private List<String> errorMessages = new ArrayList<>();

        public BatchResult() {}

        public BatchResult(int successCount, int geocodingSuccessCount, int geocodingErrorCount, List<String> errorMessages) {
            this.successCount = successCount;
            this.geocodingSuccessCount = geocodingSuccessCount;
            this.geocodingErrorCount = geocodingErrorCount;
            this.errorMessages = errorMessages;
        }

        // Getter 和 Setter
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        
        public int getGeocodingSuccessCount() { return geocodingSuccessCount; }
        public void setGeocodingSuccessCount(int geocodingSuccessCount) { this.geocodingSuccessCount = geocodingSuccessCount; }
        
        public int getGeocodingErrorCount() { return geocodingErrorCount; }
        public void setGeocodingErrorCount(int geocodingErrorCount) { this.geocodingErrorCount = geocodingErrorCount; }
        
        public List<String> getErrorMessages() { return errorMessages; }
        public void setErrorMessages(List<String> errorMessages) { this.errorMessages = errorMessages; }

        @Override
        public String toString() {
            return String.format("BatchResult{success=%d, geocodingSuccess=%d, geocodingError=%d, errors=%d}", 
                               successCount, geocodingSuccessCount, geocodingErrorCount, errorMessages.size());
        }
    }

    /**
     * 批次儲存景點資料
     * @param spotVOList 景點列表
     * @return 成功儲存的筆數
     */
    @Transactional
    public int saveAll(List<SpotVO> spotVOList) {
        List<SpotVO> savedSpots = spotRepository.saveAll(spotVOList);
        return savedSpots.size();
    }

    /**
     * 批量下架景點（下架）
     * @param spotIds 景點ID列表
     * @return 成功下架的數量
     */
    @Transactional
    public int batchDeactivateSpots(List<Integer> spotIds) {
        if (spotIds == null || spotIds.isEmpty()) {
            return 0;
        }
        return spotRepository.updateSpotStatusBatch(spotIds, (byte) 3); // 3 = 下架
    }

    /**
     * 批量上架景點（上架）
     * @param spotIds 景點ID列表
     * @return 成功上架的數量
     */
    @Transactional
    public int batchActivateSpots(List<Integer> spotIds) {
        if (spotIds == null || spotIds.isEmpty()) {
            return 0;
        }
        return spotRepository.updateSpotStatusBatch(spotIds, (byte) 1); // 1 = 上架
    }

    /**
     * 批量更新景點狀態
     * @param spotIds 景點ID列表
     * @param status 新的狀態
     * @return 更新的筆數
     */
    @Transactional
    public int batchUpdateStatus(List<Integer> spotIds, byte status) {
        if (spotIds == null || spotIds.isEmpty()) {
            return 0;
        }
        return spotRepository.updateSpotStatusBatch(spotIds, status);
    }

    /**
     * 重置 SPOT 資料表的自動遞增值（MySQL/MariaDB）
     */
    @Transactional
    public void resetAutoIncrement() {
        // 根據實際資料表名稱調整（假設為 SPOT）
        entityManager.createNativeQuery("ALTER TABLE SPOT AUTO_INCREMENT = 1").executeUpdate();
    }

    // ========== 7. 統計方法 ==========

    /**
     * 獲取總景點數量
     * @return 總景點數量
     */
    public long getTotalSpotCount() {
        return spotRepository.count();
    }

    /**
     * 根據狀態獲取景點數量
     * @param status 景點狀態
     * @return 指定狀態的景點數量
     */
    public long getSpotCountByStatus(Integer status) {
        return spotRepository.countBySpotStatus(status);
    }
} 