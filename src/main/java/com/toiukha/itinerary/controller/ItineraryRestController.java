package com.toiukha.itinerary.controller;

import com.toiukha.itinerary.model.ItineraryVO;
import com.toiukha.itinerary.service.ItineraryService;
import com.toiukha.members.model.MembersVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 行程模組 REST API 控制器
 * 處理行程相關的資料交換
 */
@RestController
@RequestMapping("/api/itinerary")
public class ItineraryRestController {

    private static final Logger logger = LoggerFactory.getLogger(ItineraryRestController.class);

    @Autowired
    private ItineraryService itineraryService;

    @Autowired
    private com.toiukha.favItn.model.FavItnService favItnService;

    @Autowired
    private com.toiukha.spot.service.SpotService spotService;

    /**
     * 取得行程列表
     */
    @GetMapping("/list")
    public ResponseEntity<?> getItineraryList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isPublic) {
        
        // TODO: 實作行程列表查詢邏輯
        Map<String, Object> response = new HashMap<>();
        response.put("message", "行程列表查詢成功");
        response.put("page", page);
        response.put("size", size);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 取得行程詳情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getItineraryDetail(@PathVariable Integer id, HttpSession session) {
        try {
            logger.info("查詢行程詳情，行程 ID: {}", id);
            
            // 檢查行程是否存在
            if (!itineraryService.isItineraryExists(id)) {
                logger.warn("嘗試查詢不存在的行程 ID: {}", id);
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "找不到指定的行程"
                ));
            }
            
            // 獲取行程詳情（包含景點）
            ItineraryVO itinerary = itineraryService.getItineraryWithSpots(id);
            
            // 檢查收藏狀態（如果用戶已登入）
            boolean isFavorited = false;
            MembersVO member = (MembersVO) session.getAttribute("member");
            if (member != null) {
                com.toiukha.favItn.model.FavItnId favItnId = new com.toiukha.favItn.model.FavItnId();
                favItnId.setFavItnId(id);
                favItnId.setMemId(member.getMemId());
                isFavorited = favItnService.isFavorited(favItnId);
            }
            
            // 創建回應數據
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "行程詳情查詢成功");
            response.put("itnId", itinerary.getItnId());
            response.put("itnName", itinerary.getItnName());
            response.put("itnDesc", itinerary.getItnDesc());
            response.put("isPublic", itinerary.getIsPublic() == 1);
            response.put("crtId", itinerary.getCrtId());
            response.put("itnCreateDat", itinerary.getItnCreateDat());
            response.put("itnUpdateDat", itinerary.getItnUpdateDat());
            response.put("isFavorited", isFavorited);
            
            // 添加景點信息
            if (itinerary.getItnSpots() != null && !itinerary.getItnSpots().isEmpty()) {
                List<Map<String, Object>> spots = itinerary.getItnSpots().stream()
                    .map(itnSpot -> {
                        Map<String, Object> spotDto = new HashMap<>();
                        if (itnSpot.getSpot() != null) {
                            spotDto.put("spotId", itnSpot.getSpot().getSpotId());
                            spotDto.put("spotName", itnSpot.getSpot().getSpotName());
                            spotDto.put("spotLoc", itnSpot.getSpot().getSpotLoc());
                            spotDto.put("spotDesc", itnSpot.getSpot().getSpotDesc());
                        }
                        spotDto.put("seq", itnSpot.getSeq());
                        return spotDto;
                    })
                    .collect(java.util.stream.Collectors.toList());
                response.put("spots", spots);
                response.put("spotCount", spots.size());
            } else {
                response.put("spots", new ArrayList<>());
                response.put("spotCount", 0);
            }
            
            logger.info("成功查詢行程詳情，行程 ID: {}, 景點數量: {}", id, 
                       itinerary.getItnSpots() != null ? itinerary.getItnSpots().size() : 0);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("查詢行程詳情時發生錯誤，行程 ID: {}", id, e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "系統錯誤：" + e.getMessage()
            ));
        }
    }

    /**
     * 新增行程
     */
    @PostMapping("/add")
    public ResponseEntity<?> addItinerary(@RequestBody Map<String, Object> request) {
        // TODO: 實作新增行程邏輯
        Map<String, Object> response = new HashMap<>();
        response.put("message", "行程新增成功");
        response.put("data", request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 更新行程
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateItinerary(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        // TODO: 實作更新行程邏輯
        Map<String, Object> response = new HashMap<>();
        response.put("message", "行程更新成功");
        response.put("id", id);
        response.put("data", request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 刪除行程
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItinerary(@PathVariable Long id) {
        // TODO: 實作刪除行程邏輯
        Map<String, Object> response = new HashMap<>();
        response.put("message", "行程刪除成功");
        response.put("id", id);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 取得我的行程列表
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyItineraries(
            HttpSession session,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isPublic) {
        
        try {
            // 從 Session 獲取當前登入會員
            MembersVO member = (MembersVO) session.getAttribute("member");
            if (member == null) {
                logger.warn("未登入用戶嘗試訪問我的行程 API");
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "請先登入會員"
                ));
            }
            
            Integer memId = member.getMemId();
            logger.info("獲取會員 ID {} 的行程列表", memId);
            
            List<ItineraryVO> itineraries;
            
            // 根據參數決定查詢方式
            if (isPublic != null) {
                if (isPublic) {
                    // 只查詢公開行程
                    itineraries = itineraryService.getPublicItinerariesByCrtId(memId);
                    logger.info("查詢會員 ID {} 的公開行程，找到 {} 筆", memId, itineraries.size());
                } else {
                    // 只查詢私人行程
                    itineraries = itineraryService.getPrivateItinerariesByCrtId(memId);
                    logger.info("查詢會員 ID {} 的私人行程，找到 {} 筆", memId, itineraries.size());
                }
            } else {
                // 查詢所有行程
                itineraries = itineraryService.getItinerariesByCrtId(memId);
                logger.info("查詢會員 ID {} 的所有行程，找到 {} 筆", memId, itineraries.size());
            }
            
            // 如果有關鍵字，進行篩選
            if (keyword != null && !keyword.isEmpty()) {
                String lowerKeyword = keyword.toLowerCase();
                itineraries = itineraries.stream()
                    .filter(itn -> 
                        (itn.getItnName() != null && itn.getItnName().toLowerCase().contains(lowerKeyword)) || 
                        (itn.getItnDesc() != null && itn.getItnDesc().toLowerCase().contains(lowerKeyword))
                    )
                    .collect(java.util.stream.Collectors.toList());
                logger.info("關鍵字篩選後剩餘 {} 筆", itineraries.size());
            }
            
            // 創建簡化的 DTO 物件，避免 Hibernate 懶加載序列化問題
            List<Map<String, Object>> simplifiedItineraries = new ArrayList<>();
            for (ItineraryVO itinerary : itineraries) {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", itinerary.getItnId());
                dto.put("name", itinerary.getItnName());
                dto.put("description", itinerary.getItnDesc());
                dto.put("isPublic", itinerary.getIsPublic() == 1);
                dto.put("createdAt", itinerary.getItnCreateDat());
                dto.put("updatedAt", itinerary.getItnUpdateDat());
                dto.put("spotCount", itinerary.getSpotCount());
                
                simplifiedItineraries.add(dto);
            }
            
            // 計算用戶自己的行程統計數據
            long totalCount = itineraries.size();
            long publicCount = itineraries.stream().filter(i -> i.getIsPublic() == 1).count();
            long privateCount = totalCount - publicCount;
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "我的行程查詢成功");
            response.put("itineraries", simplifiedItineraries);
            response.put("stats", Map.of(
                "total", totalCount,
                "public", publicCount,
                "private", privateCount
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("獲取我的行程列表時發生錯誤", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "系統錯誤：" + e.getMessage()
            ));
        }
    }

    /**
     * 取得我的收藏列表
     */
    @GetMapping("/favorites")
    public ResponseEntity<?> getMyFavorites(HttpSession session) {
        try {
            // 從 Session 獲取當前登入會員
            MembersVO member = (MembersVO) session.getAttribute("member");
            if (member == null) {
                logger.warn("未登入用戶嘗試訪問收藏行程 API");
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "請先登入會員"
                ));
            }
            
            Integer memId = member.getMemId();
            logger.info("獲取會員 ID {} 的收藏行程列表", memId);
            List<ItineraryVO> favorites = itineraryService.findFavoriteItinerariesByMemId(memId);
            
            // 轉換為前端需要的格式
            logger.info("查詢會員 ID {} 的收藏行程，找到 {} 筆", memId, favorites.size());
            
            // 轉換為前端需要的格式
            List<Map<String, Object>> result = favorites.stream()
                .map(itn -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("itnId", itn.getItnId());
                    dto.put("itnName", itn.getItnName());
                    dto.put("itnDesc", itn.getItnDesc());
                    dto.put("isPublic", itn.getIsPublic() == 1);
                    dto.put("crtId", itn.getCrtId());
                    dto.put("itnCreateDat", itn.getItnCreateDat());
                    dto.put("isFavorited", true); // 既然在收藏列表中，一定是已收藏
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "收藏行程查詢成功");
            response.put("itineraries", result);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("獲取收藏行程列表時發生錯誤", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "系統錯誤：" + e.getMessage()
            ));
        }
    }

    /**
     * 切換行程收藏狀態
     */
    @PostMapping("/{id}/favorite")
    public ResponseEntity<?> toggleFavorite(@PathVariable Integer id, HttpSession session) {
        try {
            // 從 session 取得會員資訊
            MembersVO member = (MembersVO) session.getAttribute("member");
            if (member == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "請先登入"
                ));
            }
            
            Integer memId = member.getMemId();
            
            // 建立收藏ID
            com.toiukha.favItn.model.FavItnId favItnId = new com.toiukha.favItn.model.FavItnId();
            favItnId.setFavItnId(id);
            favItnId.setMemId(memId);
            
            // 切換收藏狀態
            boolean isFavorited = favItnService.toggleFavorite(favItnId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "isFavorited", isFavorited,
                "message", isFavorited ? "已加入收藏" : "已取消收藏"
            ));
            
        } catch (Exception e) {
            logger.error("切換收藏狀態時發生錯誤", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "系統錯誤：" + e.getMessage()
            ));
        }
    }

    /**
     * 檢查是否已有此行程的複製版本
     */
    @GetMapping("/{id}/check-duplicate")
    public ResponseEntity<?> checkDuplicate(@PathVariable Integer id, HttpSession session) {
        try {
            // 從 Session 獲取當前登入會員
            MembersVO member = (MembersVO) session.getAttribute("member");
            if (member == null) {
                logger.warn("未登入用戶嘗試檢查重複行程 ID: {}", id);
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "請先登入會員"
                ));
            }
            
            Integer memId = member.getMemId();
            
            // 檢查行程是否存在
            if (!itineraryService.isItineraryExists(id)) {
                logger.warn("嘗試檢查不存在的行程 ID: {}", id);
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "找不到指定的行程"
                ));
            }
            
            // 獲取原始行程
            ItineraryVO originalItinerary = itineraryService.getItineraryById(id);
            String originalName = originalItinerary.getItnName();
            
            // 檢查用戶的行程中是否已有此行程的複製版本或相同名稱的行程
            List<ItineraryVO> userItineraries = itineraryService.getItinerariesByCrtId(memId);
            
            // 檢查是否有完全相同名稱的行程（不區分大小寫）
            boolean hasExactSameName = userItineraries.stream()
                .anyMatch(itn -> itn.getItnName() != null && 
                        itn.getItnName().equalsIgnoreCase(originalName));
                
            // 計算包含原始名稱並帶有"複製"字樣的行程數量
            long duplicateCount = userItineraries.stream()
                .filter(itn -> itn.getItnName() != null && 
                       (itn.getItnName().contains(originalName) && 
                        itn.getItnName().contains("複製")))
                .count();
            
            // 返回結果
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("hasDuplicate", hasExactSameName || duplicateCount > 0);
            response.put("hasExactSameName", hasExactSameName);
            response.put("duplicateCount", duplicateCount);
            response.put("originalName", originalName);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("檢查重複行程時發生錯誤 ID: {}", id, e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "系統錯誤：" + e.getMessage()
            ));
        }
    }

    /**
     * 複製行程
     */
    @PostMapping("/{id}/copy")
    public ResponseEntity<?> copyItinerary(@PathVariable Integer id, @RequestBody(required = false) Map<String, Object> requestBody, HttpSession session) {
        try {
            // 從 Session 獲取當前登入會員
            MembersVO member = (MembersVO) session.getAttribute("member");
            if (member == null) {
                logger.warn("未登入用戶嘗試複製行程 ID: {}", id);
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "請先登入會員"
                ));
            }
            
            Integer memId = member.getMemId();
            logger.info("會員 ID {} 嘗試複製行程 ID: {}", memId, id);
            
            // 檢查行程是否存在
            if (!itineraryService.isItineraryExists(id)) {
                logger.warn("嘗試複製不存在的行程 ID: {}", id);
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "找不到指定的行程"
                ));
            }
            
            // 獲取原始行程
            ItineraryVO originalItinerary = itineraryService.getItineraryWithSpots(id);
            
            // 獲取自定義名稱或使用默認名稱
            String newName;
            if (requestBody != null && requestBody.containsKey("customName")) {
                newName = (String) requestBody.get("customName");
            } else {
                // 檢查是否已有此行程的複製版本
                List<ItineraryVO> userItineraries = itineraryService.getItinerariesByCrtId(memId);
                long duplicateCount = userItineraries.stream()
                    .filter(itn -> itn.getItnName() != null && 
                           (itn.getItnName().contains(originalItinerary.getItnName()) && 
                            itn.getItnName().contains("複製")))
                    .count();
                
                // 如果已有複製版本，添加序號
                if (duplicateCount > 0) {
                    newName = originalItinerary.getItnName() + " (複製 " + (duplicateCount + 1) + ")";
                } else {
                    newName = originalItinerary.getItnName() + " (複製)";
                }
            }
            
            // 創建新行程
            ItineraryVO newItinerary = new ItineraryVO();
            newItinerary.setItnName(newName);
            newItinerary.setItnDesc(originalItinerary.getItnDesc());
            newItinerary.setIsPublic((byte) 0); // 預設為私人
            newItinerary.setItnStatus((byte) 1); // 正常狀態
            newItinerary.setCrtId(memId); // 設置為當前會員
            newItinerary.setCreatorType((byte) 1); // 會員建立
            
            // 保存新行程
            ItineraryVO savedItinerary = itineraryService.addItinerary(newItinerary);
            logger.info("成功創建複製行程 ID: {}", savedItinerary.getItnId());
            
            // 複製景點關聯
            if (originalItinerary.getItnSpots() != null && !originalItinerary.getItnSpots().isEmpty()) {
                originalItinerary.getItnSpots().forEach(itnSpot -> {
                    try {
                        itineraryService.addSpotToItinerary(savedItinerary.getItnId(), itnSpot.getSpotId());
                    } catch (Exception e) {
                        logger.error("複製景點時發生錯誤 spotId: {}", itnSpot.getSpotId(), e);
                    }
                });
                logger.info("成功複製 {} 個景點到新行程", originalItinerary.getItnSpots().size());
            }
            
            // 返回成功響應
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "行程複製成功");
            response.put("newItineraryId", savedItinerary.getItnId());
            response.put("originalItineraryId", id);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("複製行程時發生錯誤 ID: {}", id, e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "系統錯誤：" + e.getMessage()
            ));
        }
    }

    /**
     * 搜尋景點（用於建立行程時選擇景點）
     */
    @GetMapping("/searchSpots")
    public ResponseEntity<?> searchSpots(@RequestParam String keyword) {
        return ResponseEntity.ok(spotService.searchPublicSpots(keyword));
    }

    /**
     * 切換行程公開狀態
     */
    @PostMapping("/{id}/toggle-visibility")
    public ResponseEntity<?> toggleVisibility(
            @PathVariable Integer id,
            @RequestBody Map<String, Boolean> request,
            HttpSession session) {
        
        try {
            // 從 Session 獲取當前登入會員
            MembersVO member = (MembersVO) session.getAttribute("member");
            if (member == null) {
                logger.warn("未登入用戶嘗試切換行程公開狀態，行程 ID: {}", id);
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "請先登入會員"
                ));
            }
            
            // 檢查行程是否存在
            ItineraryVO itinerary = itineraryService.getItineraryById(id);
            if (itinerary == null) {
                logger.warn("嘗試切換不存在的行程公開狀態，行程 ID: {}", id);
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "找不到指定的行程"
                ));
            }
            
            // 檢查是否為行程擁有者
            if (!itinerary.getCrtId().equals(member.getMemId())) {
                logger.warn("用戶 {} 嘗試切換非自己的行程公開狀態，行程 ID: {}", member.getMemId(), id);
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "您沒有權限修改此行程"
                ));
            }
            
            // 獲取要設置的狀態
            Boolean makePublic = request.get("makePublic");
            if (makePublic == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "缺少必要參數"
                ));
            }
            
            // 更新公開狀態
            itinerary.setIsPublic(makePublic ? (byte)1 : (byte)0);
            itineraryService.updateItinerary(itinerary);
            
            logger.info("成功切換行程公開狀態，行程 ID: {}，新狀態: {}", id, makePublic ? "公開" : "私人");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("已將行程設為%s", makePublic ? "公開" : "私人"),
                "isPublic", makePublic
            ));
            
        } catch (Exception e) {
            logger.error("切換行程公開狀態時發生錯誤，行程 ID: {}", id, e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "系統錯誤：" + e.getMessage()
            ));
        }
    }
} 