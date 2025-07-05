package com.toiukha.spot.controller;

import com.toiukha.spot.model.ApiResponse;
import com.toiukha.spot.model.SpotVO;
import com.toiukha.spot.service.GovernmentDataService;
import com.toiukha.spot.service.SpotEnrichmentService;
import com.toiukha.spot.service.SpotService;
import com.toiukha.administrant.model.AdministrantVO;
import com.toiukha.administrant.model.AdministrantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import org.springframework.http.HttpStatus;

/**
 * 景點管理後端總控制器
 * 提供景點相關的管理頁面路由及所有後台 API
 *
 * @author CJA101G3 景點模組開發
 * @version 1.3
 */
@Controller
@RequestMapping("/admin/spot")
public class SpotAdminController {

    private static final Logger logger = LoggerFactory.getLogger(SpotAdminController.class);

    @Autowired
    private GovernmentDataService governmentDataService;

    @Autowired
    private SpotEnrichmentService spotEnrichmentService;

    @Autowired
    private SpotService spotService;

    // 移除未使用的administrantService欄位


    // ===================================================================================
    // 頁面路由 (Page Routing)
    // ===================================================================================

    /**
     * 景點列表頁面 - 新路由映射 (spotlist)
     */
    @GetMapping("/spotlist")
    public String spotlistPage(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) Integer status,
            @RequestParam(name = "region", required = false) String region,
            @RequestParam(name = "sort", defaultValue = "spotId") String sort,
            @RequestParam(name = "direction", defaultValue = "desc") String direction,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model) {
        // 調用現有的業務邏輯，但返回新的模板路徑
        listPage(keyword, status, region, sort, direction, page, size, model);
        return "back-end/spot/spotlist";
    }

    /**
     * 景點審核頁面 - 新路由映射 (spotreview)
     */
    @GetMapping("/spotreview")
    public String spotreviewPage(Model model) {
        // 調用現有的業務邏輯，但返回新的模板路徑
        reviewPage(model);
        return "back-end/spot/spotreview";
    }

    /**
     * 顯示景點列表頁面 (分頁) - 支援複合搜尋
     * @param keyword 搜尋關鍵字
     * @param status 過濾狀態
     * @param region 地區篩選
     * @param sort 排序欄位
     * @param direction 排序方向
     * @param page 當前頁碼
     * @param size 每頁筆數
     * @param model 模型
     * @return 景點列表頁面
     */
    @GetMapping("/list")
    public String listPage(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) Integer status,
            @RequestParam(name = "region", required = false) String region,
            @RequestParam(name = "sort", defaultValue = "spotId") String sort,
            @RequestParam(name = "direction", defaultValue = "desc") String direction,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model) {

        logger.info("=== 景點列表頁請求參數 ===");
        logger.info("keyword: '{}' ({})", keyword, keyword != null ? keyword.length() : "null");
        logger.info("status: {}", status);
        logger.info("region: '{}'", region);
        logger.info("sort: '{}', direction: '{}'", sort, direction);
        logger.info("page: {}, size: {}", page, size);

        // 建立排序物件
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortObj = Sort.by(sortDirection, sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        // 景點列表顯示已審核的景點（上架1和退回2），過濾掉待審核的景點（狀態0）
        Page<SpotVO> spotPage = spotService.searchReviewedSpotsForAdmin(keyword, status, region, pageable);
        
        // 為了前端JavaScript分頁，也查詢所有符合條件的資料（不分頁）
        List<SpotVO> allSpots = spotService.searchAllReviewedSpotsForAdmin(keyword, status, region, sortObj);
        logger.info("從 Service 獲取的 allSpots 總筆數: {}", allSpots.size());
        
        // 如果沒有篩選條件，直接查詢所有已審核資料作為備用
        if (allSpots.size() <= 10 && keyword == null && status == null && region == null) {
            logger.info("查詢結果較少，嘗試直接查詢所有已審核景點...");
            List<SpotVO> allSpotsBackup = spotService.getAllReviewedSpots();
            logger.info("直接查詢getAllReviewedSpots()結果: {} 筆", allSpotsBackup.size());
            
            if (allSpotsBackup.size() > allSpots.size()) {
                logger.info("使用 getAllReviewedSpots() 的結果，共 {} 筆", allSpotsBackup.size());
                allSpots = allSpotsBackup;
            }
        }
        
        // 詳細記錄每筆資料的ID和名稱
        logger.info("=== allSpots 詳細資料 ===");
        for (int i = 0; i < Math.min(allSpots.size(), 15); i++) { // 只顯示前15筆以避免日誌過多
            SpotVO spot = allSpots.get(i);
            logger.info("  {}. ID: {}, 名稱: {}, 狀態: {}, 地區: {}", 
                       i + 1, spot.getSpotId(), spot.getSpotName(), spot.getSpotStatus(), spot.getRegion());
        }
        if (allSpots.size() > 15) {
            logger.info("  ... 還有 {} 筆資料", allSpots.size() - 15);
        }
        logger.info("=========================");

        // 將 SpotVO 列表轉換為簡單的 Map 列表，避免序列化問題
        List<Map<String, Object>> allSpotsJsonData = allSpots.stream().map(spot -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("spotId", spot.getSpotId());
            map.put("spotName", spot.getSpotName());
            map.put("spotDesc", spot.getSpotDesc());
            map.put("spotLoc", spot.getSpotLoc());
            map.put("spotStatus", spot.getSpotStatus());
            map.put("spotCreateAt", spot.getSpotCreateAt() != null ? spot.getSpotCreateAt().toString() : null);
            map.put("firstPictureUrl", spot.getFirstPictureUrl());
            map.put("region", spot.getRegion());
            return map;
        }).collect(java.util.stream.Collectors.toList());
        
        logger.info("轉換後的 allSpotsJsonData 總筆數: {}", allSpotsJsonData.size());
        logger.info("=== 前5筆 JSON 資料檢查 ===");
        for (int i = 0; i < Math.min(allSpotsJsonData.size(), 5); i++) {
            Map<String, Object> map = allSpotsJsonData.get(i);
            logger.info("  {}. ID: {}, 名稱: {}", i + 1, map.get("spotId"), map.get("spotName"));
        }
        logger.info("==============================");

        // 設定模型屬性
        model.addAttribute("spotPage", spotPage);
        model.addAttribute("spotList", spotPage.getContent());
        model.addAttribute("allSpotsJsonData", allSpotsJsonData); // 使用新的 Map 列表
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", spotPage.getTotalPages());
        model.addAttribute("totalSpots", spotService.getTotalSpotCount());
        model.addAttribute("pendingCount", spotService.getSpotCountByStatus(0));
        model.addAttribute("approvedCount", spotService.getSpotCountByStatus(1));
        model.addAttribute("rejectedCount", spotService.getSpotCountByStatus(2));
        
        // 搜尋條件 (用於保持表單狀態)
        model.addAttribute("currentKeyword", keyword);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentRegion", region);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDirection", direction);
        
        // 取得所有地區供下拉選單使用
        List<String> allRegions = spotService.getAllRegions();
        logger.info("=== 地區選項資料 ===");
        logger.info("查詢到的地區列表: {}", allRegions);
        logger.info("地區數量: {}", allRegions.size());
        
        // 臨時使用固定地區列表進行測試
        if (allRegions.isEmpty()) {
            logger.info("region 欄位無資料，使用預設地區列表");
            allRegions = java.util.Arrays.asList("台北市", "新北市", "桃園市", "台中市", "台南市", "高雄市", "金門縣", "南投縣", "花蓮縣", "台東縣", "宜蘭縣");
            logger.info("使用預設地區列表: {}", allRegions);
        }
        
        model.addAttribute("allRegions", allRegions);

        logger.info("=== 搜尋結果 ===");
        logger.info("總筆數: {}, 當前頁筆數: {}, 總頁數: {}", 
                   spotPage.getTotalElements(), spotPage.getNumberOfElements(), spotPage.getTotalPages());
        logger.info("==================");

        return "back-end/spot/spotlist";
    }

    /**
     * 顯示地理編碼管理頁面
     * @return 地理編碼管理頁面
     */
    @GetMapping("/geocode")
    public String geocodePage(Model model) {
        model.addAttribute("currentPage", "spotManagement");
        return "back-end/spot/geocode-test";
    }

    /**
     * 顯示 API 匯入管理頁面
     * @return API 匯入管理頁面
     */
    @GetMapping("/api-import")
    public String apiImportPage(Model model) {
        model.addAttribute("currentPage", "spotManagement");
        return "back-end/spot/api-import";
    }

    /**
     * 顯示景點審核頁面（僅顯示待審核景點）
     */
    @GetMapping("/review")
    public String reviewPage(Model model) {
        // 查詢 SPOTSTATUS=0 的景點（待審核），自動過濾不合格
        List<SpotVO> pendingList = spotService.getPendingSpotsWithAutoCheck();
        model.addAttribute("pendingList", pendingList);
        model.addAttribute("currentPage", "spotReview");
        return "back-end/spot/spotreview";
    }

    /**
     * 顯示新增景點表單（後台）
     * @param model 模型物件
     * @return 新增表單模板
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        SpotVO spotVO = new SpotVO();
        // 預設設定必要欄位，避免表單驗證錯誤
        spotVO.setCrtId(1); // 假設管理員ID為1
        spotVO.setSpotStatus((byte) 0); // 預設為待審核狀態
        
        model.addAttribute("spotVO", spotVO);
        model.addAttribute("currentPage", "spotManagement");
        return "back-end/spot/form";
    }

    /**
     * 處理新增景點（後台）- 新增後進入待審核狀態
     * @param spotVO 景點資料
     * @param result 驗證結果
     * @param redirectAttr 重導向屬性
     * @return 重導向到審核管理頁面
     */
    @PostMapping("/add")
    public String processAdd(@Valid @ModelAttribute SpotVO spotVO,
                           BindingResult result,
                           RedirectAttributes redirectAttr) {
        try {
            // 檢查驗證錯誤
            if (result.hasErrors()) {
                String errorMessage = formatValidationErrors(result);
                redirectAttr.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/admin/spot/add";
            }
            
            // 設定預設值
            spotVO.setCrtId(1); // 假設管理員ID為1，實際應從session取得
            // 後台新增的景點強制設為待審核狀態，不受前端狀態開關影響
            spotVO.setSpotStatus((byte) 0);
            
            spotService.addSpot(spotVO);
            
            // 新增成功後跳轉到審核管理頁面
            redirectAttr.addFlashAttribute("successMessage", "景點新增成功！已加入待審核列表。");
            return "redirect:/admin/spot/spotreview";
        } catch (Exception e) {
            String errorMessage = formatExceptionMessage(e);
            redirectAttr.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/admin/spot/add";
        }
    }

    /**
     * 顯示編輯景點表單
     * @param spotId 景點ID
     * @param model 模型物件
     * @return 編輯表單模板
     */
    @GetMapping("/edit/{spotId}")
    public String showEditForm(@PathVariable Integer spotId, Model model) {
        SpotVO spot = spotService.getSpotById(spotId);
        if (spot == null) {
            return "redirect:/admin/spot/spotlist";
        }
        model.addAttribute("spotVO", spot);
        model.addAttribute("isEdit", true);
        model.addAttribute("currentPage", "spotManagement");
        return "back-end/spot/form";
    }

    /**
     * 處理編輯景點
     * @param spotId 景點ID
     * @param spotVO 景點資料
     * @param spotStatusEnabled 狀態開關
     * @param redirectAttr 重導向屬性
     * @return 重導向到列表頁
     */
    @PostMapping("/edit/{spotId}")
    public String processEdit(@PathVariable Integer spotId, @ModelAttribute SpotVO spotVO,
                            @RequestParam(value = "spotStatusEnabled", required = false) String spotStatusEnabled,
                            RedirectAttributes redirectAttr) {
        try {
            // 先獲取原始景點資料
            SpotVO originalSpot = spotService.getSpotById(spotId);
            if (originalSpot == null) {
                redirectAttr.addFlashAttribute("errorMessage", "景點不存在");
                return "redirect:/admin/spot/spotlist";
            }
            
            // 設定必要欄位
            spotVO.setSpotId(spotId);
            spotVO.setCrtId(originalSpot.getCrtId()); // 保留原始建立者ID
            spotVO.setSpotCreateAt(originalSpot.getSpotCreateAt()); // 保留原始建立時間
            
            // 處理狀態開關 - 編輯時只能在上架(1)和下架(3)之間切換
            // 如果原始狀態是待審核(0)或退回(2)，不允許編輯狀態
            if (originalSpot.getSpotStatus() == 0) {
                // 待審核狀態的景點不能編輯狀態，保持原狀態
                spotVO.setSpotStatus((byte) 0);
            } else if (originalSpot.getSpotStatus() == 2) {
                // 退回狀態的景點不能編輯狀態，保持原狀態
                spotVO.setSpotStatus((byte) 2);
            } else {
                // 只有上架(1)和下架(3)狀態的景點可以切換
                spotVO.setSpotStatus((byte) (spotStatusEnabled != null ? 1 : 3));
            }
            
            spotService.updateSpot(spotVO);
            redirectAttr.addFlashAttribute("successMessage", "景點更新成功！");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("errorMessage", "更新失敗：" + e.getMessage());
        }
        return "redirect:/admin/spot/spotlist";
    }

    /**
     * 刪除景點
     * @param spotId 景點ID
     * @param redirectAttr 重導向屬性
     * @return 重導向到列表頁
     */
    @PostMapping("/delete/{spotId}")
    public String deleteSpot(@PathVariable Integer spotId, RedirectAttributes redirectAttr) {
        try {
            spotService.deleteSpot(spotId);
            redirectAttr.addFlashAttribute("successMessage", "景點刪除成功！");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("errorMessage", "刪除失敗：" + e.getMessage());
        }
        return "redirect:/admin/spot/spotlist";
    }

    /**
     * 批量刪除景點
     * @param spotIds 景點ID列表
     * @param redirectAttr 重導向屬性
     * @return 重導向到列表頁
     */
    @PostMapping("/batch-delete")
    public String batchDeleteSpots(@RequestParam("spotIds") List<Integer> spotIds,
                                  RedirectAttributes redirectAttr) {
        try {
            if (spotIds == null || spotIds.isEmpty()) {
                redirectAttr.addFlashAttribute("errorMessage", "請選擇要刪除的景點");
                return "redirect:/admin/spot/spotlist";
            }

            int deletedCount = 0;
            int errorCount = 0;
            
            for (Integer spotId : spotIds) {
                try {
                    spotService.deleteSpot(spotId);
                    deletedCount++;
                } catch (Exception e) {
                    errorCount++;
                    // 記錄錯誤但繼續處理其他景點
                }
            }
            
            if (errorCount == 0) {
                redirectAttr.addFlashAttribute("successMessage",
                    String.format("成功刪除 %d 個景點！", deletedCount));
            } else {
                redirectAttr.addFlashAttribute("successMessage",
                    String.format("成功刪除 %d 個景點，%d 個景點刪除失敗", deletedCount, errorCount));
            }
            
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("errorMessage", "批量刪除失敗：" + e.getMessage());
        }
        return "redirect:/admin/spot/spotlist";
    }

    /**
     * 切換景點狀態（上架/下架）
     * @param spotId 景點ID
     * @param redirectAttr 重導向屬性
     * @return 重導向到列表頁
     */
    @PostMapping("/toggle-status/{spotId}")
    public String toggleStatus(@PathVariable Integer spotId, RedirectAttributes redirectAttr) {
        try {
            SpotVO spot = spotService.getSpotById(spotId);
            if (spot != null) {
                // 只允許在上架(1)和下架(3)之間切換
                if (spot.getSpotStatus() == 1 || spot.getSpotStatus() == 3) {
                    byte newStatus = (byte) (spot.getSpotStatus() == 1 ? 3 : 1);
                    spot.setSpotStatus(newStatus);
                    spotService.updateSpot(spot);
                    
                    String statusText = newStatus == 1 ? "上架" : "下架";
                    redirectAttr.addFlashAttribute("successMessage", "景點已" + statusText + "！");
                } else {
                    redirectAttr.addFlashAttribute("errorMessage", "只有上架或下架狀態的景點可以切換狀態");
                }
            }
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("errorMessage", "狀態切換失敗：" + e.getMessage());
        }
        return "redirect:/admin/spot/spotlist";
    }

    /**
     * 景點詳情頁面（後台）
     * @param spotId 景點ID
     * @param model 模型物件
     * @return 詳情頁模板
     */
    @GetMapping("/detail/{spotId}")
    public String adminSpotDetail(@PathVariable Integer spotId, Model model) {
        SpotVO spot = spotService.getSpotById(spotId);
        if (spot == null) {
            return "redirect:/admin/spot/spotlist";
        }
        model.addAttribute("spot", spot);
        model.addAttribute("currentPage", "spotManagement");
        return "back-end/spot/detail";
    }

    /**
     * 重置景點表自動遞增值
     * @param redirectAttr 重導向屬性
     * @return 重導向到列表頁
     */
    @PostMapping("/reset-auto-increment")
    public String resetAutoIncrement(RedirectAttributes redirectAttr) {
        try {
            spotService.resetAutoIncrement();
            redirectAttr.addFlashAttribute("successMessage", "景點表自動遞增值已重置！");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("errorMessage", "重置失敗：" + e.getMessage());
        }
        return "redirect:/admin/spot/spotlist";
    }

    /**
     * 批量上架景點
     */
    @PostMapping("/batch-activate")
    public String batchActivateSpots(@RequestParam("spotIds") List<Integer> spotIds,
                                     RedirectAttributes redirectAttr) {
        try {
            int count = spotService.batchActivateSpots(spotIds);
            redirectAttr.addFlashAttribute("successMessage", "成功上架 " + count + " 個景點！");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("errorMessage", "批量上架失敗：" + e.getMessage());
        }
        return "redirect:/admin/spot/spotlist";
    }

    /**
     * 批量下架景點
     */
    @PostMapping("/batch-deactivate")
    public String batchDeactivateSpots(@RequestParam("spotIds") List<Integer> spotIds,
                                       RedirectAttributes redirectAttr) {
        try {
            if (spotIds == null || spotIds.isEmpty()) {
                redirectAttr.addFlashAttribute("errorMessage", "請選擇要下架的景點");
                return "redirect:/admin/spot/spotlist";
            }
            spotService.batchUpdateStatus(spotIds, (byte) 3); // 3: 下架
            redirectAttr.addFlashAttribute("successMessage", "已批量下架 " + spotIds.size() + " 個景點");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("errorMessage", "批量下架失敗：" + e.getMessage());
        }
        return "redirect:/admin/spot/spotlist";
    }

    /**
     * 批量退回景點
     * @param spotIds 景點ID列表
     * @param redirectAttr 重導向屬性
     * @return 重導向到列表頁
     */
    @PostMapping("/batch-reject")
    public String batchRejectSpots(@RequestParam("spotIds") List<Integer> spotIds,
                                   RedirectAttributes redirectAttr) {
        try {
            if (spotIds == null || spotIds.isEmpty()) {
                redirectAttr.addFlashAttribute("errorMessage", "請選擇要退回的景點");
                return "redirect:/admin/spot/spotlist";
            }
            
            int rejectedCount = 0;
            for (Integer spotId : spotIds) {
                try {
                    spotService.rejectSpot(spotId, "批次退回", "");
                    rejectedCount++;
                } catch (Exception e) {
                    logger.warn("退回景點 {} 時發生錯誤: {}", spotId, e.getMessage());
                }
            }
            
            redirectAttr.addFlashAttribute("successMessage", "成功退回 " + rejectedCount + " 個景點");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("errorMessage", "批量退回失敗：" + e.getMessage());
        }
        return "redirect:/admin/spot/spotlist";
    }

    /**
     * 後台首頁 (Dashboard)
     * @param model 模型
     * @return 後台首頁
     */
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // 取得統計資料
        model.addAttribute("totalSpots", spotService.getTotalSpotCount());
        model.addAttribute("pendingCount", spotService.getSpotCountByStatus(0));
        model.addAttribute("approvedCount", spotService.getSpotCountByStatus(1));
        model.addAttribute("rejectedCount", spotService.getSpotCountByStatus(2));
        return "back-end/spot/dashboard";
    }

    /**
     * 權限不足頁面
     * @return 權限不足頁面
     */
    @GetMapping("/forbidden")
    public String forbiddenPage() {
        return "redirect:/admins/login";
    }

    @GetMapping({"", "/"})
    public String redirectToList() {
        return "redirect:/admins/dashboard";
    }

    // ===================================================================================
    // API 端點 (API Endpoints)
    // ===================================================================================

    /**
     * API 連線測試
     * @return 測試結果
     */
    @GetMapping("/api/test-api")
    @ResponseBody
    public ResponseEntity<ApiResponse<String>> testApiConnection() {
        logger.info("執行 API 連線測試...");
        try {
            boolean isAvailable = governmentDataService.isApiAvailable();
            if (isAvailable) {
                logger.info("API 連線測試成功");
                return ResponseEntity.ok(ApiResponse.success("政府觀光 API 連線正常"));
            } else {
                logger.warn("API 連線測試失敗");
                return ResponseEntity.ok(ApiResponse.error("無法連線至政府觀光 API，請檢查網路或 API 服務狀態"));
            }
        } catch (Exception e) {
            logger.error("API 連線測試時發生未知錯誤", e);
            return ResponseEntity.status(500).body(ApiResponse.error("系統發生未知錯誤，請聯繫管理員"));
        }
    }

    /**
     * 匯入所有景點資料
     * @param limit 匯入筆數
     * @param crtId 建立者ID
     * @return 匯入結果
     */
    @PostMapping("/api/import-spots")
    @ResponseBody
    public ResponseEntity<ApiResponse<GovernmentDataService.ImportResult>> importAllSpots(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "1") Integer crtId) {
        logger.info("開始匯入全台景點資料，上限 {} 筆，操作人員 ID: {}", limit, crtId);
        try {
            GovernmentDataService.ImportResult result = governmentDataService.importGovernmentData(crtId, limit, null);
            return ResponseEntity.ok(ApiResponse.success("全台景點資料匯入完成", result));
        } catch (Exception e) {
            logger.error("匯入全台景點資料時發生錯誤", e);
            return ResponseEntity.status(500).body(ApiResponse.error("系統發生未知錯誤，請聯繫管理員"));
        }
    }

    /**
     * 依縣市匯入景點資料
     * @param city 縣市名稱
     * @param limit 匯入筆數
     * @param crtId 建立者ID
     * @return 匯入結果
     */
    @PostMapping("/api/import-spots-by-city")
    @ResponseBody
    public ResponseEntity<ApiResponse<GovernmentDataService.ImportResult>> importSpotsByCity(
            @RequestParam String city,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "1") Integer crtId) {
        logger.info("開始匯入 {} 的景點資料，上限 {} 筆，操作人員 ID: {}", city, limit, crtId);
        
        // 修正常見的錯誤城市代碼
        String correctedCity = correctCityCode(city);
        if (!city.equals(correctedCity)) {
            logger.info("城市代碼已修正: {} -> {}", city, correctedCity);
            city = correctedCity;
        }
        
        try {
            GovernmentDataService.ImportResult result = governmentDataService.importGovernmentData(crtId, limit, city);
            return ResponseEntity.ok(ApiResponse.success(city + " 景點資料匯入完成", result));
        } catch (Exception e) {
            logger.error("匯入 {} 的景點資料時發生錯誤", city, e);
            return ResponseEntity.status(500).body(ApiResponse.error("系統發生未知錯誤，請聯繫管理員"));
        }
    }
    
    /**
     * 修正常見的錯誤城市代碼
     * @param city 原始城市代碼
     * @return 修正後的城市代碼
     */
    private String correctCityCode(String city) {
        if (city == null) return null;
        
        // 城市代碼修正對照表
        Map<String, String> corrections = Map.ofEntries(
            Map.entry("PenghuCounty", "Penghu"),
            Map.entry("TaitungCounty", "Taitung"),
            Map.entry("HualienCounty", "Hualien"),
            Map.entry("YilanCounty", "Yilan"),
            Map.entry("KinmenCounty", "Kinmen"),
            Map.entry("LienchiangCounty", "Lienchiang"),
            Map.entry("YunlinCounty", "Yunlin"),
            Map.entry("NantouCounty", "Nantou"),
            Map.entry("ChanghuaCounty", "Changhua"),
            Map.entry("MiaoliCounty", "Miaoli"),
            Map.entry("PingtungCounty", "Pingtung"),
            Map.entry("TaoyuanCounty", "Taoyuan"),
            Map.entry("NewTaipeiCity", "NewTaipei"),
            Map.entry("TaichungCity", "Taichung"),
            Map.entry("TainanCity", "Tainan"),
            Map.entry("KaohsiungCity", "Kaohsiung")
        );
        
        return corrections.getOrDefault(city, city);
    }

    // ===================================================================================
    // 從 SpotDataImportController 移轉過來的方法
    // ===================================================================================

    /**
     * 完整的資料整合流程 (匯入 + 豐富化)
     * @param crtId 建立者ID
     * @return 整合結果
     */
    @PostMapping("/api/complete-integration")
    @ResponseBody
    public ResponseEntity<ApiResponse<SpotEnrichmentService.EnrichmentResult>> completeDataIntegration(
            @RequestParam(value = "crtId", defaultValue = "1") Integer crtId) {
        logger.info("開始完整資料整合流程，建立者ID: {}", crtId);
        try {
            SpotEnrichmentService.EnrichmentResult result = spotEnrichmentService.completeDataIntegration(crtId);
            if (result.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.success("資料整合成功", result));
            } else {
                return ResponseEntity.ok(ApiResponse.error("資料整合失敗: " + result.getErrorMessage()));
            }
        } catch (Exception e) {
            logger.error("資料整合時發生錯誤", e);
            return ResponseEntity.status(500).body(ApiResponse.error("資料整合時發生錯誤: " + e.getMessage()));
        }
    }

    /**
     * 豐富化所有景點資料 (異步執行)
     * @return 執行狀態
     */
    @PostMapping("/api/enrich/all")
    @ResponseBody
    public ResponseEntity<ApiResponse<String>> enrichAllSpots() {
        logger.info("開始異步豐富化所有景點資料");
        try {
            spotEnrichmentService.enrichSpotDataAsync();
            return ResponseEntity.ok(ApiResponse.success("豐富化任務已開始，將在背景執行"));
        } catch (Exception e) {
            logger.error("啟動豐富化任務時發生錯誤", e);
            return ResponseEntity.status(500).body(ApiResponse.error("啟動豐富化任務時發生錯誤: " + e.getMessage()));
        }
    }

    /**
     * 豐富化指定景點
     * @param spotIds 景點ID列表
     * @return 豐富化結果
     */
    @PostMapping("/api/enrich/spots")
    @ResponseBody
    public ResponseEntity<ApiResponse<SpotEnrichmentService.EnrichmentResult>> enrichSpecificSpots(
            @RequestBody List<Integer> spotIds) {
        logger.info("開始豐富化指定景點，數量: {}", spotIds.size());
        try {
            SpotEnrichmentService.EnrichmentResult result = spotEnrichmentService.enrichSpecificSpots(spotIds);
            return ResponseEntity.ok(ApiResponse.success("指定景點豐富化完成", result));
        } catch (Exception e) {
            logger.error("豐富化指定景點時發生錯誤", e);
            return ResponseEntity.status(500).body(ApiResponse.error("豐富化指定景點時發生錯誤: " + e.getMessage()));
        }
    }

    /**
     * 取得匯入統計資訊
     * @return 統計資訊
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> getImportStats() {
        try {
            List<SpotVO> allSpots = spotService.getAllSpots();
            long totalSpots = allSpots.size();
            long governmentDataSpots = allSpots.stream().filter(SpotVO::isFromGovernmentData).count();
            long spotsWithGoogleData = allSpots.stream().filter(SpotVO::hasGoogleRating).count();
            long spotsWithCoordinates = allSpots.stream().filter(SpotVO::hasValidCoordinates).count();
            long activeSpots = allSpots.stream().filter(SpotVO::isActive).count();
            
            Map<String, Object> stats = Map.of(
                "totalSpots", totalSpots,
                "governmentDataSpots", governmentDataSpots,
                "spotsWithGoogleData", spotsWithGoogleData,
                "spotsWithCoordinates", spotsWithCoordinates,
                "activeSpots", activeSpots,
                "governmentDataPercentage", totalSpots > 0 ? (governmentDataSpots * 100.0 / totalSpots) : 0,
                "googleDataPercentage", totalSpots > 0 ? (spotsWithGoogleData * 100.0 / totalSpots) : 0,
                "coordinatePercentage", totalSpots > 0 ? (spotsWithCoordinates * 100.0 / totalSpots) : 0
            );
            return ResponseEntity.ok(ApiResponse.success("統計資訊查詢成功", stats));
        } catch (Exception e) {
            logger.error("查詢統計資訊時發生錯誤", e);
            return ResponseEntity.status(500).body(ApiResponse.error("查詢統計資訊時發生錯誤: " + e.getMessage()));
        }
    }

    /**
     * 根據縣市取得景點統計
     * @return 縣市統計
     */
    @GetMapping("/api/stats/region")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Long>>> getRegionStats() {
        try {
            List<SpotVO> allSpots = spotService.getActiveSpots();
            Map<String, Long> regionStats = allSpots.stream()
                .filter(spot -> spot.getRegion() != null && !spot.getRegion().trim().isEmpty())
                .collect(java.util.stream.Collectors.groupingBy(
                    SpotVO::getRegion,
                    java.util.stream.Collectors.counting()
                ));
            return ResponseEntity.ok(ApiResponse.success("縣市統計查詢成功", regionStats));
        } catch (Exception e) {
            logger.error("查詢縣市統計時發生錯誤", e);
            return ResponseEntity.status(500).body(ApiResponse.error("查詢縣市統計時發生錯誤: " + e.getMessage()));
        }
    }

    /**
     * 根據縣市查詢景點
     * @param region 縣市名稱
     * @return 景點列表
     */
    @GetMapping("/api/region/{region}")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<SpotVO>>> getSpotsByRegion(@PathVariable String region) {
        try {
            List<SpotVO> spots = spotService.getSpotsByRegion(region);
            return ResponseEntity.ok(ApiResponse.success("區域景點查詢成功", spots));
        } catch (Exception e) {
            logger.error("查詢區域景點時發生錯誤", e);
            return ResponseEntity.status(500).body(ApiResponse.error("查詢區域景點時發生錯誤: " + e.getMessage()));
        }
    }

    /**
     * 取得地圖顯示用的景點資料 (有座標的景點)
     * @return 有座標的景點列表
     */
    @GetMapping("/api/map")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<SpotVO>>> getSpotsForMap() {
        try {
            List<SpotVO> spots = spotService.getActiveSpotsWithCoordinates();
            return ResponseEntity.ok(ApiResponse.success("地圖景點資料查詢成功", spots));
        } catch (Exception e) {
            logger.error("查詢地圖景點資料時發生錯誤", e);
            return ResponseEntity.status(500).body(ApiResponse.error("查詢地圖景點資料時發生錯誤: " + e.getMessage()));
        }
    }

    /**
     * 取得已審核景點資料供景點列表頁面使用（狀態1、2、3）
     * @return 已審核景點列表
     */
    @GetMapping("/api/all")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<SpotVO>>> getAllSpotsForAdmin() {
        try {
            logger.info("=== API /api/all 被調用（景點列表頁面）===");
            // 只返回已審核的景點（狀態1、2、3），過濾掉待審核的景點（狀態0）
            Sort sortObj = Sort.by(Sort.Direction.DESC, "spotId");
            List<SpotVO> spots = spotService.searchAllReviewedSpotsForAdmin(null, null, null, sortObj);
            logger.info("API返回已審核景點數量: {}", spots.size());
            
            // 記錄前5筆資料
            for (int i = 0; i < Math.min(spots.size(), 5); i++) {
                SpotVO spot = spots.get(i);
                logger.info("  {}. ID: {}, 名稱: {}, 狀態: {} ({})", 
                           i + 1, spot.getSpotId(), spot.getSpotName(), 
                           spot.getSpotStatus(), spot.getStatusText());
            }
            logger.info("=========================");
            
            return ResponseEntity.ok(ApiResponse.success("已審核景點資料查詢成功", spots));
        } catch (Exception e) {
            logger.error("查詢已審核景點資料時發生錯誤", e);
            return ResponseEntity.status(500).body(ApiResponse.error("查詢已審核景點資料時發生錯誤: " + e.getMessage()));
        }
    }

    // ======================== 景點審核 API ========================

    /**
     * 批次通過景點審核
     */
    @PostMapping("/api/batch-approve")
    @ResponseBody
    public ApiResponse<String> batchApprove(@RequestBody List<Integer> spotIds) {
        int count = 0;
        for (Integer id : spotIds) {
            if (spotService.activateSpot(id)) count++;
        }
        return ApiResponse.success("已通過 " + count + " 筆景點審核");
    }

    /**
     * 單筆通過景點審核
     */
    @PostMapping("/api/approve/{id}")
    @ResponseBody
    public ApiResponse<String> approveSpot(@PathVariable Integer id) {
        boolean ok = spotService.activateSpot(id);
        return ok ? ApiResponse.success("景點已通過審核") : ApiResponse.error("通過失敗");
    }

    /**
     * 單筆退回景點審核（含退回原因）
     */
    @PostMapping("/api/reject/{id}")
    @ResponseBody
    public ApiResponse<String> rejectSpot(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "");
        String remark = body.getOrDefault("remark", "");
        boolean ok = spotService.rejectSpot(id, reason, remark);
        return ok ? ApiResponse.success("景點已退回") : ApiResponse.error("退回失敗");
    }

    /**
     * 批次退回景點審核
     */
    @PostMapping("/api/batch-reject")
    @ResponseBody
    public ApiResponse<String> batchRejectSpots(@RequestBody List<Integer> spotIds) {
        int count = 0;
        for (Integer id : spotIds) {
            if (spotService.rejectSpot(id, "批次退回", "")) count++;
        }
        return ApiResponse.success("已退回 " + count + " 筆景點");
    }

    /**
     * 批次移至待審核狀態
     */
    @PostMapping("/api/batch-pending")
    @ResponseBody
    public ApiResponse<String> batchPendingSpots(@RequestBody List<Integer> spotIds) {
        int count = spotService.batchUpdateStatus(spotIds, (byte) 0); // 0=待審核
        return ApiResponse.success("已將 " + count + " 筆景點移至待審");
    }

    /**
     * 匯出所有景點審核結果（CSV）
     */
    @GetMapping("/api/export")
    @ResponseBody
    public ResponseEntity<byte[]> exportSpotsCsv() {
        List<SpotVO> spots = spotService.getAllSpots();
        StringBuilder sb = new StringBuilder();
        sb.append("ID,名稱,地點,狀態,審核意見,建立時間\n");
        for (SpotVO s : spots) {
            sb.append(s.getSpotId()).append(",")
              .append(s.getSpotName() != null ? s.getSpotName().replace(",", " ") : "").append(",")
              .append(s.getSpotLoc() != null ? s.getSpotLoc().replace(",", " ") : "").append(",")
              .append(s.getSpotStatus()).append(",")
              .append(s.getSpotAuditRemark() != null ? s.getSpotAuditRemark().replace(",", " ") : "").append(",")
              .append(s.getSpotCreateAt() != null ? s.getSpotCreateAt().toString() : "").append("\n");
        }
        byte[] csvBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=spot_audit_export.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csvBytes);
    }

    /**
     * 查詢單筆景點的審核狀態與審核意見（RESTful API）
     */
    @GetMapping("/api/status/{id}")
    @ResponseBody
    public ApiResponse<Map<String, Object>> getSpotStatus(@PathVariable Integer id) {
        SpotVO spot = spotService.getSpotById(id);
        if (spot == null) {
            return ApiResponse.error("景點不存在");
        }
        Map<String, Object> data = Map.of(
            "spotId", spot.getSpotId(),
            "spotName", spot.getSpotName(),
            "status", spot.getSpotStatus(),
            "statusText", spot.getStatusText(),
            "auditRemark", spot.getSpotAuditRemark(),
            "createAt", spot.getSpotCreateAt()
        );
        return ApiResponse.success("查詢成功", data);
    }

    /**
     * 修正已匯入景點的地區信息
     * 特別針對花蓮縣和台東縣的混淆問題
     */
    @PostMapping("/api/correct-region")
    public ResponseEntity<ApiResponse<Map<String, Object>>> correctRegionInfo() {
        logger.info("開始修正景點地區信息");
        try {
            int correctedCount = governmentDataService.correctRegionInfo();
            Map<String, Object> result = new HashMap<>();
            result.put("correctedCount", correctedCount);
            return ResponseEntity.ok(ApiResponse.success("地區信息修正完成", result));
        } catch (Exception e) {
            logger.error("修正地區信息時發生錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("修正地區信息時發生錯誤: " + e.getMessage()));
        }
    }

    // ===================================================================================
    // 私有輔助方法 (Private Helper Methods)
    // ===================================================================================

    /**
     * 格式化驗證錯誤訊息
     * @param result 驗證結果
     * @return 格式化後的錯誤訊息
     */
    private String formatValidationErrors(BindingResult result) {
        StringBuilder errorMessage = new StringBuilder("表單驗證失敗：\n");
        
        for (FieldError error : result.getFieldErrors()) {
            errorMessage.append("• ").append(error.getDefaultMessage()).append("\n");
        }
        
        return errorMessage.toString().trim();
    }

    /**
     * 格式化異常錯誤訊息
     * @param e 異常物件
     * @return 格式化後的錯誤訊息
     */
    private String formatExceptionMessage(Exception e) {
        // 處理 ConstraintViolationException（JPA 驗證異常）
        if (e instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) e;
            StringBuilder errorMessage = new StringBuilder("資料驗證失敗：\n");
            
            for (ConstraintViolation<?> violation : cve.getConstraintViolations()) {
                errorMessage.append("• ").append(violation.getMessage()).append("\n");
            }
            
            return errorMessage.toString().trim();
        }
        
        // 處理其他異常
        String message = e.getMessage();
        if (message != null && message.contains("Validation failed")) {
            // 解析 Hibernate Validator 的錯誤訊息
            return parseHibernateValidationError(message);
        }
        
        return "新增失敗：" + (message != null ? message : "未知錯誤");
    }

    /**
     * 解析 Hibernate Validator 的錯誤訊息
     * @param message 原始錯誤訊息
     * @return 格式化後的錯誤訊息
     */
    private String parseHibernateValidationError(String message) {
        StringBuilder formattedMessage = new StringBuilder("資料驗證失敗：\n");
        
        try {
            // 提取 ConstraintViolationImpl 部分
            String[] parts = message.split("ConstraintViolationImpl\\{");
            
            for (int i = 1; i < parts.length; i++) {
                String part = parts[i];
                
                // 提取 interpolatedMessage
                int messageStart = part.indexOf("interpolatedMessage='");
                if (messageStart != -1) {
                    messageStart += "interpolatedMessage='".length();
                    int messageEnd = part.indexOf("'", messageStart);
                    if (messageEnd != -1) {
                        String errorMsg = part.substring(messageStart, messageEnd);
                        formattedMessage.append("• ").append(errorMsg).append("\n");
                    }
                }
            }
            
            String result = formattedMessage.toString().trim();
            return result.equals("資料驗證失敗：") ? "新增失敗：資料格式不正確" : result;
            
        } catch (Exception parseError) {
            logger.warn("解析驗證錯誤訊息失敗: {}", parseError.getMessage());
            return "新增失敗：資料驗證錯誤";
        }
    }
} 