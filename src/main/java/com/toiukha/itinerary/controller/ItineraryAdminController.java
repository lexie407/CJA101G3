package com.toiukha.itinerary.controller;

import com.toiukha.itinerary.model.ItineraryVO;
import com.toiukha.itinerary.service.ItineraryService;
import com.toiukha.administrant.model.AdministrantService;
import com.toiukha.administrant.model.AdministrantVO;
import com.toiukha.spot.model.SpotVO;
import com.toiukha.spot.service.SpotService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 行程後台管理控制器
 * 負責處理行程的後台管理功能
 * 
 * @author CJA101G3 行程模組開發
 * @version 1.0
 */
@Controller
@RequestMapping("/admin/itinerary")
public class ItineraryAdminController {

    private static final Logger logger = LoggerFactory.getLogger(ItineraryAdminController.class);

    @Autowired
    private ItineraryService itineraryService;

    @Autowired
    private AdministrantService administrantService;

    @Autowired
    private SpotService spotService;

    /**
     * 行程列表頁面 - 新路由映射 (itnlist)
     */
    @GetMapping("/itnlist")
    public String itnlistPage(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) Integer status,
            @RequestParam(name = "isPublic", required = false) Integer isPublic,
            @RequestParam(name = "sort", defaultValue = "itnId") String sort,
            @RequestParam(name = "direction", defaultValue = "desc") String direction,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model) {
        try {
            // 查詢所有行程資料（不使用分頁，前端自己處理）
            List<ItineraryVO> allItineraries = itineraryService.getAllItineraries();
            
            // 創建簡化的 DTO 物件，避免 Hibernate 懶加載序列化問題
            List<Map<String, Object>> simplifiedItineraries = allItineraries.stream()
                .map(itinerary -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("itnId", itinerary.getItnId());
                    dto.put("itnName", itinerary.getItnName());
                    dto.put("itnDesc", itinerary.getItnDesc());
                    dto.put("crtId", itinerary.getCrtId());
                    dto.put("isPublic", itinerary.getIsPublic());
                    dto.put("itnStatus", itinerary.getItnStatus());
                    dto.put("itnCreateDat", itinerary.getItnCreateDat());
                    dto.put("itnUpdateDat", itinerary.getItnUpdateDat());
                    dto.put("spotCount", itinerary.getSpotCount());
                    dto.put("publicStatusText", itinerary.getPublicStatusText());
                    dto.put("statusText", itinerary.getStatusText());
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
            
            // 添加到模型
            model.addAttribute("itineraries", allItineraries); // 用於 Thymeleaf 模板渲染
            model.addAttribute("simplifiedItineraries", simplifiedItineraries); // 用於 JavaScript
            model.addAttribute("currentPage", page);
            model.addAttribute("size", size);
            model.addAttribute("sort", sort);
            model.addAttribute("direction", direction);
            model.addAttribute("keyword", keyword);
            model.addAttribute("status", status);
            model.addAttribute("isPublic", isPublic);
            
        } catch (Exception e) {
            logger.error("載入行程列表時發生錯誤", e);
            model.addAttribute("errorMessage", "載入行程列表失敗：" + e.getMessage());
        }
        return "back-end/itinerary/itnlist";
    }

    /**
     * 行程列表頁面
     */
    @GetMapping("/list")
    public String listPage(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) Integer status,
            @RequestParam(name = "isPublic", required = false) Integer isPublic,
            @RequestParam(name = "sort", defaultValue = "itnId") String sort,
            @RequestParam(name = "direction", defaultValue = "desc") String direction,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model) {
        
        try {
            // 查詢所有行程資料（不使用分頁，前端自己處理）
            List<ItineraryVO> allItineraries = itineraryService.getAllItineraries();
            
            // 創建簡化的 DTO 物件，避免 Hibernate 懶加載序列化問題
            List<Map<String, Object>> simplifiedItineraries = allItineraries.stream()
                .map(itinerary -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("itnId", itinerary.getItnId());
                    dto.put("itnName", itinerary.getItnName());
                    dto.put("itnDesc", itinerary.getItnDesc());
                    dto.put("crtId", itinerary.getCrtId());
                    dto.put("isPublic", itinerary.getIsPublic());
                    dto.put("itnStatus", itinerary.getItnStatus());
                    dto.put("itnCreateDat", itinerary.getItnCreateDat());
                    dto.put("itnUpdateDat", itinerary.getItnUpdateDat());
                    dto.put("spotCount", itinerary.getSpotCount());
                    dto.put("publicStatusText", itinerary.getPublicStatusText());
                    dto.put("statusText", itinerary.getStatusText());
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
            
            // 添加到模型
            model.addAttribute("itineraries", allItineraries); // 用於 Thymeleaf 模板渲染
            model.addAttribute("simplifiedItineraries", simplifiedItineraries); // 用於 JavaScript
            model.addAttribute("currentPage", page);
            model.addAttribute("size", size);
            model.addAttribute("sort", sort);
            model.addAttribute("direction", direction);
            model.addAttribute("keyword", keyword);
            model.addAttribute("status", status);
            model.addAttribute("isPublic", isPublic);
            
        } catch (Exception e) {
            logger.error("載入行程列表時發生錯誤", e);
            model.addAttribute("errorMessage", "載入行程列表失敗：" + e.getMessage());
        }
        
        return "back-end/itinerary/itnlist";
    }

    /**
     * 新增行程頁面
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("itinerary", new ItineraryVO());
        return "back-end/itinerary/add";
    }

    /**
     * 處理新增行程
     */
    @PostMapping("/add")
    public String processAdd(@RequestParam String itnName,
                           @RequestParam String itnDesc,
                           @RequestParam(required = false) String isPublic,
                           @RequestParam(required = false) List<Integer> spotIds,
                           HttpSession session,
                           RedirectAttributes redirectAttr) {
        
        try {
            // 驗證行程名稱
            if (itnName == null || itnName.trim().isEmpty() || itnName.length() < 2 || itnName.length() > 50) {
                redirectAttr.addFlashAttribute("errorMessage", "行程名稱必須為2-50個字元");
            return "redirect:/admin/itinerary/add";
        }
        
            // 驗證行程描述
            if (itnDesc == null || itnDesc.trim().isEmpty() || itnDesc.length() < 5 || itnDesc.length() > 500) {
                redirectAttr.addFlashAttribute("errorMessage", "行程描述必須為5-500個字元");
                return "redirect:/admin/itinerary/add";
            }
            
            // 從 session 中獲取當前登入的管理員
            AdministrantVO admin = (AdministrantVO) session.getAttribute("admin");
            if (admin == null) {
                redirectAttr.addFlashAttribute("errorMessage", "請先登入後台系統");
                return "redirect:/admin/login";
            }
            
            // 創建新行程物件
            ItineraryVO itinerary = new ItineraryVO();
            itinerary.setItnName(itnName);
            itinerary.setItnDesc(itnDesc);
            itinerary.setCrtId(admin.getAdminId()); // 使用管理員ID作為創建者
            itinerary.setCreatorType((byte) 2); // 2=管理員建立
            
            // 設置公開狀態
            itinerary.setIsPublic(isPublic != null ? (byte) 1 : (byte) 0);
            
            // 設置行程狀態（預設為已發布）
            itinerary.setItnStatus((byte) 1);
            
            // 保存行程
            ItineraryVO savedItinerary = itineraryService.addItinerary(itinerary);
            
            // 如果有選擇景點，則添加到行程中
            if (spotIds != null && !spotIds.isEmpty()) {
                // 添加景點到行程
                itineraryService.addSpotsToItinerary(savedItinerary.getItnId(), spotIds);
                logger.info("已添加 {} 個景點到行程 ID: {}", spotIds.size(), savedItinerary.getItnId());
            }
            
            redirectAttr.addFlashAttribute("successMessage", "官方行程新增成功！");
            return "redirect:/admin/itinerary/itnlist";
            
        } catch (Exception e) {
            logger.error("新增行程時發生錯誤", e);
            redirectAttr.addFlashAttribute("errorMessage", "新增行程失敗：" + e.getMessage());
            return "redirect:/admin/itinerary/add";
        }
    }

    /**
     * 編輯行程頁面
     */
    @GetMapping("/edit/{itnId}")
    public String showEditForm(@PathVariable Integer itnId, Model model) {
        try {
            ItineraryVO itinerary = itineraryService.getItineraryWithSpots(itnId);
            if (itinerary == null) {
                model.addAttribute("errorMessage", "找不到指定的行程");
                return "redirect:/admin/itinerary/itnlist";
            }
            
            // 獲取所有景點列表
            List<SpotVO> allSpots = spotService.getAllSpots();

            // 獲取當前行程已選景點的ID列表
            List<Integer> selectedSpotIds = itinerary.getItnSpots().stream()
                                                     .map(itnSpot -> itnSpot.getSpot().getSpotId())
                                                     .collect(Collectors.toList());

            model.addAttribute("itinerary", itinerary);
            model.addAttribute("allSpots", allSpots);

            model.addAttribute("selectedSpotIds", selectedSpotIds);
            
            return "back-end/itinerary/edit";
        } catch (Exception e) {
            logger.error("載入行程編輯頁面時發生錯誤", e);
            model.addAttribute("errorMessage", "載入行程資料失敗：" + e.getMessage());
            return "redirect:/admin/itinerary/itnlist";
        }
    }

    /**
     * 處理編輯行程
     */
    @PostMapping("/edit/{itnId}")
    public String processEdit(@PathVariable Integer itnId,
                            @RequestParam String itnName,
                            @RequestParam String itnDesc,
                            @RequestParam(required = false) String isPublic,
                            @RequestParam(required = false) List<Integer> spotIds,
                            RedirectAttributes redirectAttr) {

        try {
            ItineraryVO itinerary = itineraryService.getItineraryById(itnId);
            if (itinerary == null) {
                redirectAttr.addFlashAttribute("errorMessage", "找不到要更新的行程");
                return "redirect:/admin/itinerary/itnlist";
            }

            itinerary.setItnName(itnName);
            itinerary.setItnDesc(itnDesc);
            itinerary.setIsPublic(isPublic != null ? (byte) 1 : (byte) 0);
            
            // 更新行程基本資訊
            itineraryService.updateItinerary(itinerary);

            // 更新行程景點
            logger.info("更新行程景點 - 行程ID: {}, 景點IDs: {}", itnId, spotIds);
            itineraryService.updateSpotsForItinerary(itnId, spotIds);
            
            redirectAttr.addFlashAttribute("successMessage", "行程更新成功！");
            return "redirect:/admin/itinerary/detail/" + itnId;
            
        } catch (Exception e) {
            logger.error("更新行程時發生錯誤", e);
            redirectAttr.addFlashAttribute("errorMessage", "更新行程失敗：" + e.getMessage());
            return "redirect:/admin/itinerary/edit/" + itnId;
        }
    }

    /**
     * 行程詳情頁面
     */
    @GetMapping("/detail/{itnId}")
    public String showDetail(@PathVariable Integer itnId, Model model) {
        try {
            // 使用 getItineraryWithSpots 方法載入完整的行程資料（包含景點）
            ItineraryVO itinerary = itineraryService.getItineraryWithSpots(itnId);
            if (itinerary == null) {
                model.addAttribute("errorMessage", "找不到指定的行程");
                return "redirect:/admin/itinerary/itnlist";
            }
            
            // 調試日誌：檢查資料庫中的實際數據
            logger.info("=== 行程詳情調試資訊 ===");
            logger.info("行程ID: {}", itinerary.getItnId());
            logger.info("行程名稱: {}", itinerary.getItnName());
            logger.info("建立者ID: {}", itinerary.getCrtId());
            logger.info("CreatorType: {}", itinerary.getCreatorType());
            logger.info("是否為管理員建立: {}", itinerary.isCreatedByAdmin());
            logger.info("景點數量: {}", itinerary.getSpotCount());
            logger.info("是否有景點: {}", itinerary.hasSpots());
            if (itinerary.getItnSpots() != null) {
                logger.info("景點列表: {}", itinerary.getItnSpots().size());
                for (int i = 0; i < itinerary.getItnSpots().size(); i++) {
                    var itnSpot = itinerary.getItnSpots().get(i);
                    logger.info("  景點 {}: ID={}, 名稱={}", 
                        i + 1, 
                        itnSpot.getSpot() != null ? itnSpot.getSpot().getSpotId() : "null",
                        itnSpot.getSpot() != null ? itnSpot.getSpot().getSpotName() : "null");
                }
            }
            logger.info("========================");
            
            model.addAttribute("itinerary", itinerary);
            return "back-end/itinerary/detail";
        } catch (Exception e) {
            logger.error("載入行程詳情時發生錯誤", e);
            model.addAttribute("errorMessage", "載入行程詳情失敗：" + e.getMessage());
            return "redirect:/admin/itinerary/itnlist";
        }
    }

    /**
     * 刪除行程
     */
    @PostMapping("/delete/{itnId}")
    public String deleteItinerary(@PathVariable Integer itnId, RedirectAttributes redirectAttr) {
        try {
            itineraryService.deleteItinerary(itnId);
            redirectAttr.addFlashAttribute("successMessage", "行程刪除成功！");
        } catch (Exception e) {
            logger.error("刪除行程時發生錯誤", e);
            redirectAttr.addFlashAttribute("errorMessage", "刪除行程失敗：" + e.getMessage());
        }
        return "redirect:/admin/itinerary/itnlist";
    }

    /**
     * 切換行程狀態（上架/下架）
     */
    @PostMapping("/toggle-status/{itnId}")
    public String toggleStatus(@PathVariable Integer itnId, RedirectAttributes redirectAttr) {
        try {
            ItineraryVO itinerary = itineraryService.getItineraryById(itnId);
            if (itinerary != null) {
                byte newStatus = (byte) (itinerary.getItnStatus() == 1 ? 0 : 1);
                itinerary.setItnStatus(newStatus);
                itineraryService.updateItinerary(itinerary);
                
                String statusText = newStatus == 1 ? "上架" : "下架";
                redirectAttr.addFlashAttribute("successMessage", "行程已" + statusText + "！");
            }
        } catch (Exception e) {
            logger.error("切換行程狀態時發生錯誤", e);
            redirectAttr.addFlashAttribute("errorMessage", "狀態切換失敗：" + e.getMessage());
        }
        return "redirect:/admin/itinerary/itnlist";
    }

    /**
     * 切換行程公開狀態
     */
    @PostMapping("/toggle-public/{itnId}")
    public String togglePublic(@PathVariable Integer itnId, RedirectAttributes redirectAttr) {
        try {
            ItineraryVO itinerary = itineraryService.getItineraryById(itnId);
            if (itinerary != null) {
                byte newPublicStatus = (byte) (itinerary.getIsPublic() == 1 ? 0 : 1);
                itinerary.setIsPublic(newPublicStatus);
                itineraryService.updateItinerary(itinerary);
                
                String publicText = newPublicStatus == 1 ? "公開" : "私人";
                redirectAttr.addFlashAttribute("successMessage", "行程已設為" + publicText + "！");
            }
        } catch (Exception e) {
            logger.error("切換行程公開狀態時發生錯誤", e);
            redirectAttr.addFlashAttribute("errorMessage", "公開狀態切換失敗：" + e.getMessage());
        }
        return "redirect:/admin/itinerary/itnlist";
    }

    /**
     * 批量刪除行程
     */
    @PostMapping("/batch-delete")
    public String batchDeleteItineraries(@RequestParam("itineraryIds") List<Integer> itineraryIds,
                                       RedirectAttributes redirectAttr) {
        try {
            if (itineraryIds == null || itineraryIds.isEmpty()) {
                redirectAttr.addFlashAttribute("errorMessage", "請選擇要刪除的行程");
                return "redirect:/admin/itinerary/itnlist";
            }
            
            int deletedCount = 0;
            for (Integer itnId : itineraryIds) {
                try {
                    itineraryService.deleteItinerary(itnId);
                    deletedCount++;
                } catch (Exception e) {
                    logger.warn("刪除行程 {} 時發生錯誤: {}", itnId, e.getMessage());
                }
            }
            
            redirectAttr.addFlashAttribute("successMessage", "成功刪除 " + deletedCount + " 個行程");
        } catch (Exception e) {
            logger.error("批量刪除行程時發生錯誤", e);
            redirectAttr.addFlashAttribute("errorMessage", "批量刪除失敗：" + e.getMessage());
        }
        return "redirect:/admin/itinerary/itnlist";
    }

    /**
     * 批量設為公開
     */
    @PostMapping("/batch-activate")
    public String batchActivateItineraries(@RequestParam("itineraryIds") List<Integer> itineraryIds,
                                         RedirectAttributes redirectAttr) {
        try {
            if (itineraryIds == null || itineraryIds.isEmpty()) {
                redirectAttr.addFlashAttribute("errorMessage", "請選擇要公開的行程");
                return "redirect:/admin/itinerary/itnlist";
            }
            
            int updatedCount = 0;
            for (Integer itnId : itineraryIds) {
                try {
                    ItineraryVO itinerary = itineraryService.getItineraryById(itnId);
                    if (itinerary != null) {
                        itinerary.setIsPublic((byte) 1);
                        itineraryService.updateItinerary(itinerary);
                        updatedCount++;
                    }
                } catch (Exception e) {
                    logger.warn("公開行程 {} 時發生錯誤: {}", itnId, e.getMessage());
                }
            }
            
            redirectAttr.addFlashAttribute("successMessage", "成功公開 " + updatedCount + " 個行程");
        } catch (Exception e) {
            logger.error("批量公開行程時發生錯誤", e);
            redirectAttr.addFlashAttribute("errorMessage", "批量公開失敗：" + e.getMessage());
        }
        return "redirect:/admin/itinerary/itnlist";
    }

    /**
     * 批量設為私人
     */
    @PostMapping("/batch-deactivate")
    public String batchDeactivateItineraries(@RequestParam("itineraryIds") List<Integer> itineraryIds,
                                           RedirectAttributes redirectAttr) {
        try {
            if (itineraryIds == null || itineraryIds.isEmpty()) {
                redirectAttr.addFlashAttribute("errorMessage", "請選擇要設為私人的行程");
                return "redirect:/admin/itinerary/itnlist";
            }
            
            int updatedCount = 0;
            for (Integer itnId : itineraryIds) {
                try {
                    ItineraryVO itinerary = itineraryService.getItineraryById(itnId);
                    if (itinerary != null) {
                        itinerary.setIsPublic((byte) 0);
                        itineraryService.updateItinerary(itinerary);
                        updatedCount++;
                    }
                } catch (Exception e) {
                    logger.warn("設為私人行程 {} 時發生錯誤: {}", itnId, e.getMessage());
                }
            }
            
            redirectAttr.addFlashAttribute("successMessage", "成功設為私人 " + updatedCount + " 個行程");
        } catch (Exception e) {
            logger.error("批量設為私人行程時發生錯誤", e);
            redirectAttr.addFlashAttribute("errorMessage", "批量設為私人失敗：" + e.getMessage());
        }
        return "redirect:/admin/itinerary/itnlist";
    }

    // ===================================================================================
    // API 端點
    // ===================================================================================



    // ===================================================================================
    // 輔助方法
    // ===================================================================================

    /**
     * 臨時方法：修正所有行程的creatorType欄位
     * 訪問路徑：/admin/itinerary/fix-creator-type
     */
    @GetMapping("/fix-creator-type")
    public String fixCreatorType(RedirectAttributes redirectAttr) {
        try {
            // 獲取所有行程
            List<ItineraryVO> allItineraries = itineraryService.getAllItineraries();
            int updatedCount = 0;
            
            logger.info("開始修正 creatorType 欄位，總共 {} 個行程", allItineraries.size());
            
            for (ItineraryVO itinerary : allItineraries) {
                Integer crtId = itinerary.getCrtId();
                Byte currentCreatorType = itinerary.getCreatorType();
                
                logger.info("行程ID: {}, 建立者ID: {}, 當前CreatorType: {}", 
                    itinerary.getItnId(), crtId, currentCreatorType);
                
                // 判斷建立者類型的邏輯
                // 假設管理員ID範圍是 1-100，會員ID範圍是 1000+
                Byte correctCreatorType;
                if (crtId != null && crtId <= 100) {
                    correctCreatorType = (byte) 2; // 管理員
                } else {
                    correctCreatorType = (byte) 1; // 會員
                }
                
                // 如果需要更新
                if (currentCreatorType == null || !currentCreatorType.equals(correctCreatorType)) {
                    itinerary.setCreatorType(correctCreatorType);
                    itineraryService.updateItinerary(itinerary);
                    updatedCount++;
                    
                    logger.info("已更新行程ID: {}, 建立者ID: {}, CreatorType: {} -> {}", 
                        itinerary.getItnId(), crtId, currentCreatorType, correctCreatorType);
                }
            }
            
            logger.info("修正完成，共更新 {} 個行程", updatedCount);
            redirectAttr.addFlashAttribute("successMessage", 
                "CreatorType 修正完成！共更新 " + updatedCount + " 個行程");
            
        } catch (Exception e) {
            logger.error("修正 CreatorType 時發生錯誤", e);
            redirectAttr.addFlashAttribute("errorMessage", "修正失敗：" + e.getMessage());
        }
        
        return "redirect:/admin/itinerary/itnlist";
    }

    /**
     * 格式化驗證錯誤訊息
     */
    private String formatValidationErrors(BindingResult result) {
        StringBuilder sb = new StringBuilder();
        result.getFieldErrors().forEach(error -> {
            if (sb.length() > 0) sb.append(", ");
            sb.append(error.getDefaultMessage());
        });
        return sb.toString();
    }

    /**
     * 權限不足頁面
     * @return 重定向到管理員登入頁面
     */
    @GetMapping("/forbidden")
    public String forbiddenPage() {
        return "redirect:/admins/login";
    }

    /**
     * 重定向到列表頁
     */
    @GetMapping({"", "/"})
    public String redirectToList() {
        return "redirect:/admins/dashboard";
    }
} 