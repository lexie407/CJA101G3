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
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import org.springframework.http.HttpStatus;
import java.util.ArrayList;
import com.toiukha.spot.model.SpotImgVO;
import com.toiukha.spot.service.SpotImgService;
import jakarta.servlet.http.HttpSession;

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

    @Autowired
    private com.toiukha.spot.service.SpotServiceImpl spotServiceImpl;

    @Autowired
    private SpotImgService spotImgService;

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
        // 新增：自動補 Google 圖片
        spotEnrichmentService.enrichSpotPictureUrlIfNeeded(allSpots);
        
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
     * 處理新增景點（後台）- 管理員新增的景點直接上架
     * @param spotVO 景點資料
     * @param result 驗證結果
     * @param redirectAttr 重導向屬性
     * @return 重導向到景點列表頁面
     */
    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> processAdd(@Valid @ModelAttribute SpotVO spotVO,
                                          BindingResult result,
                                          HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        if (result.hasErrors()) {
            resp.put("success", false);
            resp.put("message", "表單資料有誤，請修正後重試");
            return resp;
        }
        try {
            Integer adminId = getCurrentUserId(session);
            logger.info("[DEBUG] 新增景點時取得的 adminId: {}", adminId);
            spotVO.setCrtId(adminId);
            spotVO.setSpotStatus((byte) 1); // 直接上架
            spotService.save(spotVO);
            resp.put("success", true);
            resp.put("message", "景點新增成功");
        } catch (Exception e) {
            logger.error("新增景點失敗", e);
            resp.put("success", false);
            resp.put("message", "系統錯誤，請稍後再試");
        }
        return resp;
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
        // 查詢多圖
        model.addAttribute("spotImgList", spotImgService.getImagesBySpotId(spotId));
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
    @ResponseBody
    public Map<String, Object> processEdit(@PathVariable Integer spotId, @ModelAttribute SpotVO spotVO,
                            @RequestParam(value = "spotStatusEnabled", required = false) String spotStatusEnabled,
                            RedirectAttributes redirectAttr,
                            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                            @RequestParam(value = "multiImages", required = false) List<MultipartFile> multiImages,
                            @RequestParam(value = "multiImageDescs", required = false) List<String> multiImageDescs) {
        Map<String, Object> resp = new HashMap<>();
        final int MAX_IMAGES = 8;
        final long MAX_SIZE = 2 * 1024 * 1024L;
        final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/jpg");
        try {
            SpotVO originalSpot = spotService.getSpotById(spotId);
            if (originalSpot == null) {
                resp.put("success", false);
                resp.put("message", "景點不存在");
                return resp;
            }
            spotVO.setSpotId(spotId);
            spotVO.setCrtId(originalSpot.getCrtId());
            spotVO.setSpotCreateAt(originalSpot.getSpotCreateAt());
            if (originalSpot.getSpotStatus() == 0) {
                spotVO.setSpotStatus((byte) 0);
            } else if (originalSpot.getSpotStatus() == 2) {
                spotVO.setSpotStatus((byte) 2);
            } else {
                spotVO.setSpotStatus((byte) (spotStatusEnabled != null ? 1 : 3));
            }
            // 驗證總數量（含舊圖）
            int existCount = spotImgService.getImagesBySpotId(spotId).size();
            int newCount = (multiImages != null ? multiImages.size() : 0);
            if (existCount + newCount > MAX_IMAGES) {
                resp.put("success", false);
                resp.put("message", "最多只能上傳" + MAX_IMAGES + "張圖片");
                return resp;
            }
            // 驗證每張圖片
            if (multiImages != null) {
                for (MultipartFile mf : multiImages) {
                    if (mf == null || mf.isEmpty()) continue;
                    if (!ALLOWED_TYPES.contains(mf.getContentType())) {
                        resp.put("success", false);
                        resp.put("message", "僅允許 JPG、PNG 格式圖片");
                        return resp;
                    }
                    if (mf.getSize() > MAX_SIZE) {
                        resp.put("success", false);
                        resp.put("message", "單張圖片不可超過2MB");
                        return resp;
                    }
                }
            }
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
                String uploadDir = "src/main/resources/static/images/spot/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();
                File dest = new File(uploadDir + fileName);
                Files.copy(imageFile.getInputStream(), Paths.get(dest.toURI()));
                spotVO.setFirstPictureUrl("/images/spot/" + fileName);
            } else {
                spotVO.setFirstPictureUrl(originalSpot.getFirstPictureUrl());
            }
            spotService.updateSpot(spotVO);
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
                    SpotImgVO img = new SpotImgVO();
                    img.setSpotId(spotVO.getSpotId());
                    img.setImgPath("/images/spot/" + fileName);
                    if (multiImageDescs != null && i < multiImageDescs.size())
                        img.setImgDesc(multiImageDescs.get(i));
                    img.setImgTime(java.time.LocalDateTime.now());
                    spotImgService.saveImage(img);
                }
            }
            resp.put("success", true);
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "更新失敗：" + e.getMessage());
            return resp;
        }
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
        // 新增：自動補 Google 圖片
        spotEnrichmentService.enrichSpotPictureUrlIfNeeded(java.util.Collections.singletonList(spot));
        System.out.println("firstPictureUrl: " + spot.getFirstPictureUrl());
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

    /**
     * 從 Google Places API 取得景點資訊 (POST方法)
     * 用於表單頁面的評分獲取功能
     */
    @PostMapping("/api/google-place-info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> postGooglePlaceInfo(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String address = request.get("address");
        
        logger.info("接收到POST請求獲取Google Places資訊，景點名稱：{}，地址：{}", name, address);
        
        // 檢查參數
        if ((name == null || name.trim().isEmpty()) && 
            (address == null || address.trim().isEmpty())) {
            
            logger.warn("請求缺少必要參數：景點名稱或地址");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "請提供景點名稱或地址");
            
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 使用擴充服務取得 Google Places 資訊
            Map<String, Object> result = spotEnrichmentService.getGooglePlaceInfo(name, address);
            result.put("success", true);
            
            logger.info("Google Places API 請求結果：成功");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("處理 Google Places API 請求時發生錯誤：{}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "處理請求時發生錯誤：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 從 Google Places API 取得景點資訊
     * @param spotName 景點名稱
     * @param address 地址
     * @param latitude 緯度（可選）
     * @param longitude 經度（可選）
     * @return 景點資訊
     */
    @GetMapping("/api/google-places")
    public ResponseEntity<Map<String, Object>> getGooglePlacesInfo(
            @RequestParam(required = false) String spotName,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        
        logger.info("接收到 Google Places API 請求，景點名稱：{}，地址：{}", spotName, address);
        
        // 檢查參數
        if ((spotName == null || spotName.trim().isEmpty()) && 
            (address == null || address.trim().isEmpty())) {
            
            logger.warn("請求缺少必要參數：景點名稱或地址");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "請提供景點名稱或地址");
            
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 創建臨時景點對象
            SpotVO tempSpot = new SpotVO();
            tempSpot.setSpotName(spotName);
            tempSpot.setSpotLoc(address);
            
            if (latitude != null && longitude != null) {
                tempSpot.setSpotLat(latitude);
                tempSpot.setSpotLng(longitude);
            }
            
            // 使用擴充服務取得 Google Places 資訊
            Map<String, Object> result = spotEnrichmentService.enrichSpotWithGoogleData(tempSpot);
            
            logger.info("Google Places API 請求結果：{}", result.get("success"));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("處理 Google Places API 請求時發生錯誤：{}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "處理請求時發生錯誤：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 搜尋 Google Places 景點
     * 用於API匯入頁面的搜尋功能
     */
    @GetMapping("/api/google-places-search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchGooglePlaces(
            @RequestParam String keyword,
            @RequestParam(required = false) String district) {
        
        logger.info("接收到Google Places搜尋請求，關鍵字：{}，地區：{}", keyword, district);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 使用擴充服務取得 Google Places 資訊
            Map<String, Object> result = spotEnrichmentService.getGooglePlaceInfo(keyword, district);
            
            // 包裝成列表形式返回，因為前端期望的是列表
            List<Map<String, Object>> dataList = new ArrayList<>();
            
            // 如果找到了結果，將其添加到列表中
            if (result.get("placeId") != null) {
                dataList.add(result);
            }
            
            response.put("success", true);
            response.put("data", dataList);
            
            logger.info("Google Places搜尋成功，找到 {} 筆結果", dataList.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Google Places搜尋失敗：{}", e.getMessage());
            
            response.put("success", false);
            response.put("error", "搜尋失敗：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 管理員專用：自動補全所有沒有 Google Place ID 的景點，並存回 MySQL
     * POST /admin/spot/api/enrich-placeid
     */
    @PostMapping("/api/enrich-placeid")
    @ResponseBody
    public ResponseEntity<ApiResponse<String>> enrichAllSpotsWithGooglePlaceId() {
        int updated = spotServiceImpl.enrichAllSpotsWithGooglePlaceId();
        return ResponseEntity.ok(ApiResponse.success("成功補全 Place ID 的景點數：" + updated));
    }

    /**
     * 匯入景點資料
     * @param limit 匯入數量限制
     * @param session 使用者 Session
     * @return 匯入結果
     */
    @PostMapping("/api/import-spots")
    @ResponseBody
    public Map<String, Object> importSpots(@RequestParam(value = "limit", required = false, defaultValue = "10") int limit, HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Integer adminId = getCurrentUserId(session);
            GovernmentDataService.ImportResult result = governmentDataService.importGovernmentData(adminId, limit, null);
            if (result.isSuccess()) {
                resp.put("success", true);
                resp.put("message", "成功匯入 " + result.getSuccessCount() + " 筆景點資料");
            } else {
                resp.put("success", false);
                resp.put("message", result.getErrorMessage() != null ? result.getErrorMessage() : "匯入失敗");
            }
        } catch (Exception e) {
            logger.error("匯入景點失敗", e);
            resp.put("success", false);
            resp.put("message", "匯入失敗，請稍後再試");
        }
        return resp;
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

    @PostMapping("/img/delete")
    @ResponseBody
    public Map<String, Object> deleteSpotImg(@RequestParam Integer spotId, @RequestParam Integer imgId) {
        Map<String, Object> resp = new HashMap<>();
        try {
            spotImgService.deleteImage(spotId, imgId);
            resp.put("success", true);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
        }
        return resp;
    }

    /**
     * 從 Session 中獲取當前使用者ID
     * @param session HTTP Session
     * @return 使用者ID
     */
    private Integer getCurrentUserId(HttpSession session) {
        Object adminId = session.getAttribute("adminId");
        return adminId != null ? (Integer) adminId : null;
    }
} 