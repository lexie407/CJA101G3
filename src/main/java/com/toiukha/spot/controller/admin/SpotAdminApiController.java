package com.toiukha.spot.controller.admin;

import com.toiukha.spot.model.ApiResponse;
import com.toiukha.spot.model.SpotVO;
import com.toiukha.spot.service.SpotService;
import com.toiukha.spot.dto.SpotDTO;
import com.toiukha.spot.dto.SpotMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 景點後台管理 API 控制器
 * 專門處理後台管理員的景點管理需求
 * 包含所有管理功能：查詢所有狀態、狀態管理、批量操作等
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
@RestController
@RequestMapping("/api/spot/admin")
public class SpotAdminApiController {

    @Autowired
    private SpotService spotService;

    @Autowired
    private SpotMapper spotMapper;

    // ========== 1. 後台景點查詢API ==========

    /**
     * 取得所有景點 (後台用)
     * 包含所有狀態的景點，用於後台管理
     * @return API回應包含所有景點
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<SpotVO>>> getAllSpots() {
        try {
            List<SpotVO> spots = spotService.getAllSpots();
            return ResponseEntity.ok(ApiResponse.success("查詢成功", spots));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("查詢失敗: " + e.getMessage()));
        }
    }

    /**
     * 根據狀態取得景點 (後台用)
     * @param status 景點狀態 (0=待審核, 1=上架, 2=退回)
     * @return API回應包含篩選結果
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<SpotVO>>> getSpotsByStatus(@PathVariable Byte status) {
        try {
            List<SpotVO> spots = spotService.getSpotsByStatus(status);
            return ResponseEntity.ok(ApiResponse.success("查詢成功", spots));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("查詢失敗: " + e.getMessage()));
        }
    }

    /**
     * 根據ID取得景點詳情 (後台用)
     * 可以查詢任何狀態的景點
     * @param spotId 景點ID
     * @return API回應包含景點詳情
     */
    @GetMapping("/{spotId}")
    public ResponseEntity<ApiResponse<SpotVO>> getSpotById(@PathVariable Integer spotId) {
        try {
            SpotVO spot = spotService.getSpotById(spotId);
            
            if (spot == null) {
                return ResponseEntity.ok(ApiResponse.error("景點不存在"));
            }
            
            return ResponseEntity.ok(ApiResponse.success("查詢成功", spot));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("查詢失敗: " + e.getMessage()));
        }
    }

    // ========== 2. 後台景點新增API ==========

    /**
     * 新增景點 (後台用)
     * 管理員新增的景點可以直接設定為上架狀態
     * @param spotDTO 景點資料傳輸物件
     * @return API回應
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SpotVO>> addSpot(@Valid @RequestBody SpotDTO spotDTO) {
        try {
            // 檢查名稱是否重複
            if (spotService.existsBySpotName(spotDTO.getSpotName())) {
                return ResponseEntity.ok(ApiResponse.error("景點名稱已存在"));
            }
            
            // 轉換DTO為VO
            SpotVO spotVO = spotMapper.toVO(spotDTO);
            
            // 設定建立者ID (暫時使用固定值)
            spotVO.setCrtId(1);
            
            // 後台新增預設為上架狀態 (除非特別指定)
            if (spotVO.getSpotStatus() == null) {
                spotVO.setSpotStatus((byte) 1);
            }
            
            SpotVO savedSpot = spotService.addSpot(spotVO);
            return ResponseEntity.ok(ApiResponse.success("景點新增成功", savedSpot));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("新增失敗: " + e.getMessage()));
        }
    }

    // ========== 3. 後台景點更新API ==========

    /**
     * 更新景點 (後台用)
     * 管理員可以更新任何景點的任何欄位
     * @param spotId 景點ID
     * @param spotDTO 景點資料傳輸物件
     * @return API回應
     */
    @PutMapping("/{spotId}")
    public ResponseEntity<ApiResponse<SpotVO>> updateSpot(
            @PathVariable Integer spotId, 
            @Valid @RequestBody SpotDTO spotDTO) {
        try {
            SpotVO existingSpot = spotService.getSpotById(spotId);
            if (existingSpot == null) {
                return ResponseEntity.ok(ApiResponse.error("景點不存在"));
            }
            
            // 檢查名稱是否重複 (排除自己)
            if (spotService.existsBySpotName(spotDTO.getSpotName()) && 
                !existingSpot.getSpotName().equals(spotDTO.getSpotName())) {
                return ResponseEntity.ok(ApiResponse.error("景點名稱已存在"));
            }
            
            // 轉換DTO為VO並保持原有資料
            SpotVO spotVO = spotMapper.toVO(spotDTO);
            spotVO.setSpotId(spotId);
            spotVO.setCrtId(existingSpot.getCrtId());
            spotVO.setSpotCreateAt(existingSpot.getSpotCreateAt());
            
            SpotVO updatedSpot = spotService.updateSpot(spotVO);
            return ResponseEntity.ok(ApiResponse.success("景點更新成功", updatedSpot));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("更新失敗: " + e.getMessage()));
        }
    }

    // ========== 4. 後台景點刪除API ==========

    /**
     * 刪除景點 (後台用)
     * 管理員可以刪除任何景點
     * @param spotId 景點ID
     * @return API回應
     */
    @DeleteMapping("/{spotId}")
    public ResponseEntity<ApiResponse<Void>> deleteSpot(@PathVariable Integer spotId) {
        try {
            if (!spotService.existsById(spotId)) {
                return ResponseEntity.ok(ApiResponse.error("景點不存在"));
            }
            
            spotService.deleteSpot(spotId);
            return ResponseEntity.ok(ApiResponse.success("景點刪除成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("刪除失敗: " + e.getMessage()));
        }
    }

    // ========== 5. 狀態管理API ==========

    /**
     * 景點上架 (後台用)
     * @param spotId 景點ID
     * @return API回應
     */
    @PutMapping("/{spotId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateSpot(@PathVariable Integer spotId) {
        try {
            if (spotService.activateSpot(spotId)) {
                return ResponseEntity.ok(ApiResponse.success("景點上架成功"));
            } else {
                return ResponseEntity.ok(ApiResponse.error("景點上架失敗"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("上架失敗: " + e.getMessage()));
        }
    }

    /**
     * 景點下架 (後台用)
     * @param spotId 景點ID
     * @return API回應
     */
    @PutMapping("/{spotId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateSpot(@PathVariable Integer spotId) {
        try {
            if (spotService.deactivateSpot(spotId)) {
                return ResponseEntity.ok(ApiResponse.success("景點下架成功"));
            } else {
                return ResponseEntity.ok(ApiResponse.error("景點下架失敗"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("下架失敗: " + e.getMessage()));
        }
    }

    // ========== 6. 批量操作API ==========

    /**
     * 批量上架景點 (後台用)
     * @param spotIds 景點ID列表
     * @return API回應
     */
    @PutMapping("/batch/activate")
    public ResponseEntity<ApiResponse<String>> batchActivate(@RequestBody List<Integer> spotIds) {
        try {
            if (spotIds == null || spotIds.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("請選擇要上架的景點"));
            }
            
            int successCount = spotService.batchActivateSpots(spotIds);
            return ResponseEntity.ok(ApiResponse.success("成功上架 " + successCount + " 個景點"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("批量上架失敗: " + e.getMessage()));
        }
    }

    /**
     * 批量下架景點 (後台用)
     * @param spotIds 景點ID列表
     * @return API回應
     */
    @PutMapping("/batch/deactivate")
    public ResponseEntity<ApiResponse<String>> batchDeactivate(@RequestBody List<Integer> spotIds) {
        try {
            if (spotIds == null || spotIds.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("請選擇要下架的景點"));
            }
            
            int successCount = spotService.batchDeactivateSpots(spotIds);
            return ResponseEntity.ok(ApiResponse.success("成功下架 " + successCount + " 個景點"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("批量下架失敗: " + e.getMessage()));
        }
    }

    /**
     * 批量退回景點 (後台用)
     * @param spotIds 景點ID列表
     * @return API回應
     */
    @PutMapping("/batch/reject")
    public ResponseEntity<ApiResponse<String>> batchReject(@RequestBody List<Integer> spotIds) {
        try {
            if (spotIds == null || spotIds.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("請選擇要退回的景點"));
            }
            
            int successCount = spotService.batchUpdateStatus(spotIds, (byte) 2);
            return ResponseEntity.ok(ApiResponse.success("成功退回 " + successCount + " 個景點"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("批量退回失敗: " + e.getMessage()));
        }
    }

    // ========== 7. 後台統計API ==========

    /**
     * 取得後台統計資訊
     * 包含各種狀態的景點數量統計
     * @return API回應包含統計資料
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 總景點數量
            stats.put("totalSpots", spotService.getTotalSpotCount());
            
            // 各狀態景點數量
            stats.put("pendingSpots", spotService.getSpotCountByStatus(0));    // 待審核
            stats.put("activeSpots", spotService.getSpotCountByStatus(1));     // 上架
            stats.put("rejectedSpots", spotService.getSpotCountByStatus(2));   // 退回
            
            // 其他統計資訊
            stats.put("spotsWithCoordinates", spotService.getActiveSpotsWithCoordinates().size());
            stats.put("spotsWithoutCoordinates", spotService.getSpotsWithoutCoordinates().size());
            
            return ResponseEntity.ok(ApiResponse.success("統計資料查詢成功", stats));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("統計資料查詢失敗: " + e.getMessage()));
        }
    }

    // ========== 8. 批次匯入API ==========

    /**
     * 批次新增景點 (後台用)
     * @param spotDTOList 景點資料傳輸物件列表
     * @return API回應
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<SpotService.BatchResult>> addSpotsInBatch(@Valid @RequestBody List<SpotDTO> spotDTOList) {
        try {
            if (spotDTOList == null || spotDTOList.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("景點資料列表不能為空"));
            }
            
            // 檢查名稱是否重複
            for (SpotDTO spotDTO : spotDTOList) {
                if (spotService.existsBySpotName(spotDTO.getSpotName())) {
                    return ResponseEntity.ok(ApiResponse.error("景點名稱已存在: " + spotDTO.getSpotName()));
                }
            }
            
            // 轉換DTO為VO
            List<SpotVO> spotVOList = spotDTOList.stream()
                    .map(spotMapper::toVO)
                    .peek(spotVO -> {
                        spotVO.setCrtId(1); // 暫時使用固定值
                        // 後台批次匯入預設為上架狀態
                        if (spotVO.getSpotStatus() == null) {
                            spotVO.setSpotStatus((byte) 1);
                        }
                    })
                    .toList();
            
            // 使用批次匯入方法
            SpotService.BatchResult result = spotService.addSpotsWithGeocoding(spotVOList);
            return ResponseEntity.ok(ApiResponse.success("批次新增完成", result));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("批次新增失敗: " + e.getMessage()));
        }
    }


} 