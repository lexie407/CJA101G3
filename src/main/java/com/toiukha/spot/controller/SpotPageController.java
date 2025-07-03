package com.toiukha.spot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.toiukha.spot.model.SpotVO;
import com.toiukha.spot.service.SpotService;
import com.toiukha.spot.model.SpotFavoriteVO;
import com.toiukha.spot.service.SpotFavoriteService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;


/**
 * 景點前台頁面控制器
 * 處理景點前台相關的頁面請求
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
@Controller
@RequestMapping("/spot")
public class SpotPageController {

    @Autowired
    private SpotService spotService;

    @Autowired
    private SpotFavoriteService spotFavoriteService;

    // ========== 1. 首頁和景點列表 ==========

    /**
     * 景點首頁 (主頁面)
     * @param model 模型物件
     * @return 首頁模板
     */
    @GetMapping("/")
    public String spotRoot(Model model) {
        return spotIndex(model);
    }

    @GetMapping("/index")
    public String spotIndex(Model model) {
        try {
            List<SpotVO> activeSpots = spotService.getActiveSpots();
            model.addAttribute("spotList", activeSpots);
            model.addAttribute("currentPage", "home");
            return "index";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "載入景點資料失敗: " + e.getMessage());
            model.addAttribute("spotList", new ArrayList<>());
            model.addAttribute("currentPage", "home");
            return "index";
        }
    }

    /**
     * 景點列表頁面
     * @param keyword 搜尋關鍵字 (可選)
     * @param region 地區篩選 (可選)
     * @param model 模型物件
     * @return 列表頁模板
     */
    @GetMapping("/spotlist")
    public String spotList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "region", required = false) String region,
            Model model) {
        
        List<SpotVO> spotList;
        boolean hasFilter = false;
        
        try {
            // 如果有搜尋條件，執行篩選搜尋
            if ((keyword != null && !keyword.trim().isEmpty()) || 
                (region != null && !region.trim().isEmpty())) {
                
                spotList = spotService.searchSpotsWithFilters(keyword, region);
                hasFilter = true;
                
                // 記錄搜尋日誌
                System.out.println("景點列表搜尋 - 關鍵字: " + keyword + ", 地區: " + region + 
                                 ", 結果數量: " + spotList.size());
            } else {
                // 沒有搜尋條件，顯示所有活躍景點
                spotList = spotService.getActiveSpots();
                System.out.println("景點列表 - 顯示所有景點，共 " + spotList.size() + " 個");
            }
            
            model.addAttribute("spotList", spotList);
            model.addAttribute("hasFilter", hasFilter);
            model.addAttribute("searchKeyword", keyword);
            model.addAttribute("selectedRegion", region);
            model.addAttribute("currentPage", "list");
            
            // 如果有篩選但沒有結果，添加提示訊息
            if (hasFilter && spotList.isEmpty()) {
                model.addAttribute("msg", "沒有找到符合條件的景點，請嘗試調整搜尋條件");
            } else if (hasFilter) {
                model.addAttribute("msg", "找到 " + spotList.size() + " 個符合條件的景點");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMsgs", List.of("載入景點資料失敗: " + e.getMessage()));
            model.addAttribute("spotList", new ArrayList<>());
            model.addAttribute("currentPage", "list");
        }
        
        return "front-end/spot/spotlist";
    }

    // ========== 2. 景點搜尋 ==========

    /**
     * 景點地圖頁面
     * @param model 模型物件
     * @return 地圖頁模板
     */
    @GetMapping("/map")
    public String spotMap(Model model) {
        model.addAttribute("currentPage", "home");
        return "front-end/spot/map";
    }
    
    /**
     * Google API 測試頁面
     * @param model 模型物件
     * @return API測試頁面
     */
    @GetMapping("/api-test")
    public String apiTestPage(Model model) {
        model.addAttribute("currentPage", "api-test");
        return "front-end/spot/api-test";
    }

    /**
     * 景點搜尋頁面
     * @param keyword 搜尋關鍵字 (可選)
     * @param model 模型物件
     * @return 搜尋頁模板
     */
    @GetMapping("/search")
    public String spotSearch(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        model.addAttribute("currentPage", "home");
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 有搜尋關鍵字，執行搜尋
            List<SpotVO> searchResults = spotService.searchSpots(keyword.trim());
            model.addAttribute("spotList", searchResults);
            model.addAttribute("searchKeyword", keyword.trim());
            model.addAttribute("hasSearched", true);
            
            // 記錄搜尋日誌
            System.out.println("搜尋關鍵字: " + keyword.trim() + ", 找到 " + searchResults.size() + " 個結果");
        } else {
            // 沒有搜尋關鍵字，顯示所有景點
            List<SpotVO> allSpots = spotService.getActiveSpots();
            model.addAttribute("spotList", allSpots);
            model.addAttribute("hasSearched", false);
            
            // 記錄日誌
            System.out.println("顯示所有景點，共 " + allSpots.size() + " 個");
        }
        
        return "front-end/spot/search";
    }

    /**
     * 處理景點搜尋請求
     * @param keyword 搜尋關鍵字
     * @param model 模型物件
     * @return 搜尋結果頁模板
     */
    @PostMapping("/search")
    public String performSearch(@RequestParam("keyword") String keyword, Model model) {
        model.addAttribute("currentPage", "home");
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            List<SpotVO> searchResults = spotService.searchSpots(keyword.trim());
            model.addAttribute("spotList", searchResults);
            model.addAttribute("searchKeyword", keyword.trim());
            model.addAttribute("hasSearched", true);
        } else {
            model.addAttribute("spotList", new ArrayList<>());
            model.addAttribute("hasSearched", false);
        }
        
        return "front-end/spot/search";
    }

    /**
     * 附近景點頁面
     * @param model 模型物件
     * @return 附近景點頁模板
     */
    @GetMapping("/nearby")
    public String nearbySpots(Model model) {
        try {
            // 載入所有活躍景點，讓前端JavaScript處理地理位置
            List<SpotVO> allSpots = spotService.getActiveSpots();
            model.addAttribute("spotList", allSpots);
            model.addAttribute("currentPage", "home");
            
            System.out.println("附近景點頁面 - 載入所有景點，共 " + allSpots.size() + " 個");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "載入景點資料失敗: " + e.getMessage());
            model.addAttribute("spotList", new ArrayList<>());
            model.addAttribute("currentPage", "home");
        }
        
        return "front-end/spot/spotlist"; // 暫時使用列表模板
    }

    /**
     * 隨機推薦頁面
     * @param model 模型物件
     * @return 隨機推薦頁模板
     */
    @GetMapping("/random")
    public String randomSpot(Model model) {
        try {
            List<SpotVO> allSpots = spotService.getActiveSpots();
            
            if (!allSpots.isEmpty()) {
                // 隨機選擇一個景點
                Random random = new Random();
                SpotVO randomSpot = allSpots.get(random.nextInt(allSpots.size()));
                model.addAttribute("randomSpot", randomSpot);
                
                // 推薦相關景點（同地區的其他景點）
                List<SpotVO> relatedSpots = spotService.getRelatedSpots(randomSpot.getSpotId(), 5);
                model.addAttribute("relatedSpots", relatedSpots);
                
                System.out.println("隨機推薦景點: " + randomSpot.getSpotName());
            } else {
                model.addAttribute("randomSpot", null);
                model.addAttribute("relatedSpots", new ArrayList<>());
            }
            
            model.addAttribute("currentPage", "home");
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "載入隨機景點失敗: " + e.getMessage());
            model.addAttribute("randomSpot", null);
            model.addAttribute("relatedSpots", new ArrayList<>());
            model.addAttribute("currentPage", "home");
        }
        
        return "front-end/spot/detail"; // 暫時使用詳情模板
    }

    /**
     * 景點比較頁面
     * @param spotIds 要比較的景點ID列表（可選）
     * @param model 模型物件
     * @return 景點比較頁模板
     */
    @GetMapping("/compare")
    public String compareSpots(
            @RequestParam(value = "spots", required = false) List<Integer> spotIds,
            Model model) {
        try {
            List<SpotVO> spotsToCompare = new ArrayList<>();
            
            if (spotIds != null && !spotIds.isEmpty()) {
                // 載入指定的景點進行比較
                for (Integer spotId : spotIds) {
                    SpotVO spot = spotService.getSpotById(spotId);
                    if (spot != null && spot.isActive()) {
                        spotsToCompare.add(spot);
                    }
                }
                System.out.println("景點比較 - 比較 " + spotsToCompare.size() + " 個景點");
            }
            
            // 載入所有景點供選擇
            List<SpotVO> allSpots = spotService.getActiveSpots();
            model.addAttribute("allSpots", allSpots);
            model.addAttribute("spotsToCompare", spotsToCompare);
            model.addAttribute("currentPage", "home");
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "載入景點比較失敗: " + e.getMessage());
            model.addAttribute("allSpots", new ArrayList<>());
            model.addAttribute("spotsToCompare", new ArrayList<>());
            model.addAttribute("currentPage", "home");
        }
        
        return "front-end/spot/spotlist"; // 暫時使用列表模板
    }

    // ========== 3. 景點詳情 ==========

    /**
     * 景點詳情頁面
     * @param spotId 景點ID
     * @param model 模型物件
     * @param session 會話物件 (檢查收藏狀態)
     * @return 詳情頁模板
     */
    @GetMapping("/detail/{spotId}")
    public String spotDetail(@PathVariable("spotId") Integer spotId, Model model, HttpSession session) {
        try {
            SpotVO spot = spotService.getSpotById(spotId);
            
            if (spot == null || !spot.isActive()) {
                // 景點不存在或已下架，顯示錯誤頁面
                model.addAttribute("errorMessage", "找不到指定的景點，可能已被移除或暫時下架");
                model.addAttribute("currentPage", "home");
                return "front-end/spot/detail";
            }
            
            model.addAttribute("spot", spot);
            
            // 使用測試會員ID檢查收藏狀態
            Integer memId = 10;
            boolean isFavorited = spotFavoriteService.isFavorited(memId, spotId);
            model.addAttribute("isFavorited", isFavorited);
            
            // 查詢收藏數量
            Long favoriteCount = spotFavoriteService.getFavoriteCount(spotId);
            model.addAttribute("favoriteCount", favoriteCount);
            
            // 查詢相關景點推薦（同地區的其他景點，最多6個）
            List<SpotVO> relatedSpots = spotService.getRelatedSpots(spotId, 6);
            model.addAttribute("relatedSpots", relatedSpots);
            
            // 設定頁面標題
            model.addAttribute("pageTitle", spot.getSpotName() + " - 景點詳情");
            model.addAttribute("currentPage", "home");
            
            // 記錄瀏覽日誌
            System.out.println("景點詳情頁面 - 景點ID: " + spotId + ", 景點名稱: " + spot.getSpotName());
            
            return "front-end/spot/detail";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "載入景點詳情時發生錯誤，請稍後再試");
            model.addAttribute("currentPage", "home");
            return "front-end/spot/detail";
        }
    }

    // ========== 4. 新增景點 ==========

    /**
     * 顯示新增景點表單
     * @param model 模型物件
     * @param session 會話物件 (檢查登入狀態)
     * @return 新增表單模板
     */
    @GetMapping("/add")
    public String showAddSpotForm(Model model, HttpSession session) {
        // TODO: 後續整合會員登入檢查
        // MembersVO sessionMember = (MembersVO) session.getAttribute("member");
        // if (sessionMember == null) {
        //     return "redirect:/members/login";
        // }
        
        model.addAttribute("spotVO", new SpotVO());
        model.addAttribute("errorMsgs", new ArrayList<>());
        model.addAttribute("currentPage", "home");
        return "front-end/spot/add";
    }

    /**
     * 處理新增景點表單提交
     * @param spotVO 景點資料
     * @param result 驗證結果
     * @param model 模型物件
     * @param session 會話物件
     * @param redirectAttr 重導向屬性
     * @return 處理結果頁面
     */
    @PostMapping("/add")
    public String processAddSpot(
            @Valid @ModelAttribute("spotVO") SpotVO spotVO,
            BindingResult result,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttr) {

        // TODO: 後續整合會員登入檢查
        // MembersVO sessionMember = (MembersVO) session.getAttribute("member");
        // if (sessionMember == null) {
        //     return "redirect:/members/login";
        // }

        // 預先設定系統欄位
        spotVO.setCrtId(getCurrentUserId(session));
        spotVO.setSpotStatus((byte) 0);

        // 檢查景點名稱是否重複
        if (spotService.existsBySpotName(spotVO.getSpotName())) {
            result.rejectValue("spotName", "error.spotVO", "景點名稱已存在，請使用其他名稱");
        }

        // 若欄位驗證失敗
        if (result.hasErrors()) {
            List<String> errorMsgs = result.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());
            model.addAttribute("errorMsgs", errorMsgs);
            model.addAttribute("currentPage", "home");
            return "front-end/spot/add";
        }

        try {
            SpotVO savedSpot = spotService.addSpot(spotVO);
            redirectAttr.addFlashAttribute("msg", "景點新增成功！景點ID: " + savedSpot.getSpotId() + "，等待管理員審核後上架。");
            return "redirect:/spot/spotlist";
        } catch (Exception e) {
            model.addAttribute("errorMsgs", List.of("新增景點時發生錯誤：" + e.getMessage()));
            model.addAttribute("currentPage", "home");
            return "front-end/spot/add";
        }
    }

    // ========== 5. 收藏功能 ==========

    /**
     * 我的收藏頁面
     * @param model 模型物件
     * @param session 會話物件
     * @return 收藏頁面模板
     */
    @GetMapping("/favorites")
    public String myFavorites(Model model, HttpSession session) {
        // TODO: 暫時關閉登入檢查，供測試使用
        // Integer memId = getMemIdFromSession(session);
        // if (memId == null) {
        //     model.addAttribute("errorMessage", "請先登入");
        //     return "redirect:/members/login";
        // }
        
        // 使用測試會員ID (測試用)
        Integer memId = 10;

        try {
            List<SpotFavoriteVO> favoriteRecords = spotFavoriteService.getFavoriteRecords(memId);
            model.addAttribute("favoriteRecords", favoriteRecords);
            // 左側導覽：首頁高亮（因為景點屬於首頁）
            model.addAttribute("currentPage", "home");
            return "front-end/spot/favorites";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "載入收藏清單失敗");
            model.addAttribute("currentPage", "home");
            return "front-end/spot/favorites";
        }
    }

    // ========== 6. 錯誤處理 ==========

    /**
     * 處理找不到景點的情況
     * @param model 模型物件
     * @return 錯誤頁面
     */
    @GetMapping("/notfound")
    public String spotNotFound(Model model) {
        model.addAttribute("errorMsg", "景點不存在或已下架");
        model.addAttribute("currentPage", "home");
        return "front-end/spot/error";
    }

    // ========== 輔助方法 ==========

    /**
     * 從 Session 獲取當前登入的會員ID
     * 整合會員模組的登入系統
     * @param session HTTP Session
     * @return 會員ID，如果未登入則返回null
     */
    private Integer getMemIdFromSession(HttpSession session) {
        Object memberObj = session.getAttribute("member");
        if (memberObj instanceof com.toiukha.members.model.MembersVO) {
            com.toiukha.members.model.MembersVO member = (com.toiukha.members.model.MembersVO) memberObj;
            return member.getMemId();
        }
        return null;
    }



    /**
     * 獲取當前用戶ID (建立者ID)
     * 整合會員登入系統
     * @param session HTTP Session
     * @return 用戶ID，如果未登入則返回null
     */
    private Integer getCurrentUserId(HttpSession session) {
        return getMemIdFromSession(session);
    }
} 