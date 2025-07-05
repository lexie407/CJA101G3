package com.toiukha.spot.controller;

import com.toiukha.spot.service.SpotService;
import com.toiukha.spot.model.SpotVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 景點選擇器API控制器
 * 提供行程新增頁面景點選擇功能的API端點
 */
@RestController
@RequestMapping("/api/spot-selector")
public class SpotApiController {

    @Autowired
    private SpotService spotService;

    /**
     * 獲取上架狀態的景點
     * @param limit 限制返回的景點數量
     * @return 上架狀態的景點列表
     */
    @GetMapping("/active")
    public ResponseEntity<List<SpotVO>> getActiveSpots(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        
        // 獲取狀態為上架(1)的景點
        List<SpotVO> activeSpots = spotService.getSpotsByStatus(1, limit);
        return ResponseEntity.ok(activeSpots);
    }
    
    /**
     * 搜索景點
     * @param keyword 搜索關鍵字
     * @return 符合搜索條件的景點列表
     */
    @GetMapping("/search")
    public ResponseEntity<List<SpotVO>> searchSpots(
            @RequestParam(value = "keyword") String keyword) {
        
        // 搜索景點（優先返回上架狀態的景點）
        List<SpotVO> searchResults = spotService.searchSpotsByKeyword(keyword);
        return ResponseEntity.ok(searchResults);
    }
} 