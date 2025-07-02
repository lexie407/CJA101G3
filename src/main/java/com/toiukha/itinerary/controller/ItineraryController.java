package com.toiukha.itinerary.controller;

import com.toiukha.itinerary.service.ItineraryService;
import com.toiukha.itinerary.model.ItineraryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/itinerary")
public class ItineraryController {

    @Autowired
    private ItineraryService itineraryService;

    // ========== 基本 CRUD 操作 ==========

    //查全部，POST方法
    @PostMapping("/getAllItineraries")
    public List<ItineraryVO> getAllItineraries() {
        return itineraryService.getAllItineraries();
    }

    //查一個，路徑後面是要查詢的編號，GET方法
    @GetMapping("/{itnId}")
    public ItineraryVO getItineraryById(@PathVariable Integer itnId) {
        return itineraryService.getItineraryById(itnId);
    }

    /* 新增一個，POST方法
     * JSON格式
     * {
     * 	"itnName": ,
     * 	"crtId": ,
     * 	"itnDesc":
     * }
     */
    @PostMapping("/createItinerary")
    public ItineraryVO createItinerary(@RequestBody ItineraryVO itineraryVO) {
        return itineraryService.addItinerary(itineraryVO);
    }

    /* 修改一個，路徑後面的是要修改的景點編號，POST方法
     * JSON格式
     * {
     * 	需要的資料就是下面方法中的註解
     * }
     */
    @PostMapping("/{itnId}")
    public ItineraryVO updateItinerary(@PathVariable Integer itnId, @RequestBody ItineraryVO itineraryDetails) {
        ItineraryVO itineraryVO = itineraryService.getItineraryById(itnId);
        if (itineraryVO != null) {
            //以下JSON需要的資料
            itineraryVO.setItnName(itineraryDetails.getItnName());
            itineraryVO.setCrtId(itineraryDetails.getCrtId());
            itineraryVO.setItnDesc(itineraryDetails.getItnDesc());
            itineraryVO.setIsPublic(itineraryDetails.getIsPublic());
            itineraryVO.setItnStatus(itineraryDetails.getItnStatus());
            //以上JSON需要的資料
            return itineraryService.updateItinerary(itineraryVO);
        }
        return null;
    }

    // 刪除行程
    @DeleteMapping("/{itnId}")
    public ResponseEntity<Map<String, String>> deleteItinerary(@PathVariable Integer itnId) {
        Map<String, String> response = new HashMap<>();
        try {
            itineraryService.deleteItinerary(itnId);
            response.put("message", "行程刪除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "行程刪除失敗: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ========== 查詢功能 ==========

    // 分頁查詢所有行程
    @GetMapping("/page")
    public Page<ItineraryVO> getItinerariesByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return itineraryService.getAllItineraries(pageable);
    }

    // 查詢公開行程
    @GetMapping("/public")
    public List<ItineraryVO> getPublicItineraries() {
        return itineraryService.getPublicItineraries();
    }

    // 分頁查詢公開行程
    @GetMapping("/public/page")
    public Page<ItineraryVO> getPublicItinerariesByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return itineraryService.getPublicItineraries(pageable);
    }

    // 根據建立者查詢行程
    @GetMapping("/creator/{crtId}")
    public List<ItineraryVO> getItinerariesByCreator(@PathVariable Integer crtId) {
        return itineraryService.getItinerariesByCrtId(crtId);
    }

    // 分頁查詢建立者的行程
    @GetMapping("/creator/{crtId}/page")
    public Page<ItineraryVO> getItinerariesByCreatorPage(
            @PathVariable Integer crtId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return itineraryService.getItinerariesByCrtId(crtId, pageable);
    }

    // 查詢建立者的公開行程
    @GetMapping("/creator/{crtId}/public")
    public List<ItineraryVO> getPublicItinerariesByCreator(@PathVariable Integer crtId) {
        return itineraryService.getPublicItinerariesByCrtId(crtId);
    }

    // 查詢建立者的私人行程
    @GetMapping("/creator/{crtId}/private")
    public List<ItineraryVO> getPrivateItinerariesByCreator(@PathVariable Integer crtId) {
        return itineraryService.getPrivateItinerariesByCrtId(crtId);
    }

    // ========== 搜尋功能 ==========

    // 根據名稱搜尋行程
    @GetMapping("/search")
    public List<ItineraryVO> searchItinerariesByName(@RequestParam String itnName) {
        return itineraryService.searchItinerariesByName(itnName);
    }

    // 搜尋公開行程
    @GetMapping("/search/public")
    public List<ItineraryVO> searchPublicItineraries(@RequestParam String keyword) {
        return itineraryService.searchPublicItineraries(keyword);
    }

    // 分頁搜尋公開行程
    @GetMapping("/search/public/page")
    public Page<ItineraryVO> searchPublicItinerariesPage(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return itineraryService.searchPublicItineraries(keyword, pageable);
    }

    // ========== 統計功能 ==========

    // 取得統計資訊
    @GetMapping("/stats")
    public Map<String, Object> getItineraryStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", itineraryService.getTotalItineraryCount());
        stats.put("publicCount", itineraryService.getPublicItineraryCount());
        stats.put("privateCount", itineraryService.getPrivateItineraryCount());
        return stats;
    }

    // 取得建立者的統計資訊
    @GetMapping("/creator/{crtId}/stats")
    public Map<String, Object> getCreatorStats(@PathVariable Integer crtId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", itineraryService.getItineraryCountByCrtId(crtId));
        stats.put("publicCount", itineraryService.getPublicItinerariesByCrtId(crtId).size());
        stats.put("privateCount", itineraryService.getPrivateItinerariesByCrtId(crtId).size());
        return stats;
    }

    // ========== 驗證功能 ==========

    // 檢查行程名稱是否存在
    @GetMapping("/check-name")
    public Map<String, Boolean> checkItineraryName(@RequestParam String itnName) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", itineraryService.isItineraryNameExists(itnName));
        return response;
    }

    // 檢查行程是否存在
    @GetMapping("/{itnId}/exists")
    public Map<String, Boolean> checkItineraryExists(@PathVariable Integer itnId) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", itineraryService.isItineraryExists(itnId));
        return response;
    }

    // ========== 行程景點管理 ==========

    // 為行程添加景點
    @PostMapping("/{itnId}/spots/{spotId}")
    public ResponseEntity<Map<String, String>> addSpotToItinerary(
            @PathVariable Integer itnId,
            @PathVariable Integer spotId) {
        Map<String, String> response = new HashMap<>();
        try {
            itineraryService.addSpotToItinerary(itnId, spotId);
            response.put("message", "景點添加成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "景點添加失敗: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 從行程移除景點
    @DeleteMapping("/{itnId}/spots/{spotId}")
    public ResponseEntity<Map<String, String>> removeSpotFromItinerary(
            @PathVariable Integer itnId,
            @PathVariable Integer spotId) {
        Map<String, String> response = new HashMap<>();
        try {
            itineraryService.removeSpotFromItinerary(itnId, spotId);
            response.put("message", "景點移除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "景點移除失敗: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 取得行程的所有景點
    @GetMapping("/{itnId}/spots")
    public List<com.toiukha.itinerary.model.ItnSpotVO> getSpotsByItinerary(@PathVariable Integer itnId) {
        return itineraryService.getSpotsByItineraryId(itnId);
    }

    // 取得行程的景點數量
    @GetMapping("/{itnId}/spots/count")
    public Map<String, Long> getSpotCountByItinerary(@PathVariable Integer itnId) {
        Map<String, Long> response = new HashMap<>();
        response.put("count", itineraryService.getSpotCountByItineraryId(itnId));
        return response;
    }
}
