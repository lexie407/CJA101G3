package com.toiukha.itinerary.controller;

import com.toiukha.itinerary.service.ItineraryService;
import com.toiukha.itinerary.model.ItineraryVO;
import com.toiukha.itinerary.model.ItnSpotVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * 行程模組頁面控制器
 * 處理行程相關的頁面路由
 */
@Controller
@RequestMapping("/itinerary")
public class ItineraryPageController {

    @Autowired
    private ItineraryService itineraryService;

    /**
     * 行程首頁
     */
    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("currentPage", "travel");
        return "front-end/itinerary/index";
    }
    
    /**
     * 行程首頁 - 別名路由
     */
    @GetMapping({"/index", "/home", "/main"})
    public String homePageAlias(Model model) {
        model.addAttribute("currentPage", "travel");
        return "front-end/itinerary/index";
    }

    /**
     * 行程列表頁面
     */
    @GetMapping("/itnlist")
    public String listPage(Model model,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Boolean isPublic) {
        try {
            List<ItineraryVO> itineraryList;
            
            // 根據參數決定查詢方式
            if (keyword != null && !keyword.trim().isEmpty()) {
                if (isPublic != null && isPublic) {
                    // 搜尋公開行程（不包含揪團模組行程）
                    itineraryList = itineraryService.searchPublicItinerariesNotFromActivity(keyword.trim());
                } else {
                    // 搜尋所有行程 (需要實作此方法，暫時使用名稱搜尋)
                    itineraryList = itineraryService.searchItinerariesByName(keyword.trim());
                }
            } else if (isPublic != null && isPublic) {
                // 只顯示公開行程（不包含揪團模組行程）
                itineraryList = itineraryService.getPublicItinerariesNotFromActivity();
            } else {
                // 顯示所有公開行程（不包含揪團模組行程）(預設只顯示公開的)
                itineraryList = itineraryService.getPublicItinerariesNotFromActivity();
            }
            
            model.addAttribute("itineraryList", itineraryList);
            model.addAttribute("keyword", keyword);
            model.addAttribute("isPublic", isPublic);
            
        } catch (Exception e) {
            // 如果發生錯誤，傳遞空列表
            model.addAttribute("itineraryList", List.of());
            model.addAttribute("errorMessage", "載入行程資料時發生錯誤");
        }
        
        return "front-end/itinerary/itnlist";
    }

    /**
     * 行程詳情頁面
     */
    @GetMapping("/detail/{id}")
    public String detailPage(@PathVariable Integer id, Model model) {
        try {
            // 根據ID載入行程資料（包含景點）
            ItineraryVO itinerary = itineraryService.getItineraryWithSpots(id);
            
            // 除錯日誌
            System.out.println("=== 行程詳情載入除錯 ===");
            System.out.println("行程ID: " + id);
            System.out.println("行程物件: " + (itinerary != null ? "已載入" : "null"));
            
            if (itinerary != null) {
                System.out.println("行程名稱: " + itinerary.getItnName());
                System.out.println("景點數量: " + (itinerary.getItnSpots() != null ? itinerary.getItnSpots().size() : "null"));
                
                if (itinerary.getItnSpots() != null) {
                    for (int i = 0; i < itinerary.getItnSpots().size(); i++) {
                        ItnSpotVO itnSpot = itinerary.getItnSpots().get(i);
                        System.out.println("景點 " + (i + 1) + ":");
                        System.out.println("  - ItnSpot ID: " + itnSpot.getItnSpotId());
                        System.out.println("  - Spot物件: " + (itnSpot.getSpot() != null ? "已載入" : "null"));
                        
                        if (itnSpot.getSpot() != null) {
                            System.out.println("  - 景點ID: " + itnSpot.getSpot().getSpotId());
                            System.out.println("  - 景點名稱: " + itnSpot.getSpot().getSpotName());
                            System.out.println("  - 景點地址: " + itnSpot.getSpot().getSpotLoc());
                            System.out.println("  - 景點描述: " + itnSpot.getSpot().getSpotDesc());
                        }
                    }
                }
            }
            System.out.println("=== 除錯結束 ===");
            
            if (itinerary == null) {
                model.addAttribute("errorMessage", "找不到指定的行程");
                return "404error";
            }
            
            model.addAttribute("itinerary", itinerary);
            model.addAttribute("itineraryId", id);
            
        } catch (Exception e) {
            e.printStackTrace(); // 印出完整錯誤堆疊
            model.addAttribute("errorMessage", "載入行程資料時發生錯誤：" + e.getMessage());
            return "500error";
        }
        
        return "front-end/itinerary/detail";
    }

    /**
     * 建立行程頁面
     */
    @GetMapping("/add")
    public String addPage(HttpSession session, Model model) {
        // 檢查會員是否已登入
        Integer memId = getMemIdFromSession(session);
        if (memId == null) {
            // 未登入，重定向到登入頁面
            return "redirect:/members/login?redirect=/itinerary/add";
        }
        
        return "front-end/itinerary/add";
    }

    /**
     * 處理建立行程表單提交
     */
    @PostMapping("/add")
    public String addItinerary(@RequestParam String itnName,
                          @RequestParam String itnDesc,
                          @RequestParam(defaultValue = "1") Byte isPublic,
                          @RequestParam(required = false) List<Integer> spotIds,
                          HttpSession session,
                          Model model) {
        // 檢查會員是否已登入
        Integer memId = getMemIdFromSession(session);
        if (memId == null) {
            // 未登入，重定向到登入頁面
            return "redirect:/members/login?redirect=/itinerary/add";
        }
        
        try {
            // 驗證行程名稱
            if (itnName == null || itnName.trim().isEmpty()) {
                model.addAttribute("errorMessage", "行程名稱不能為空");
                return returnFormWithData(model, itnName, itnDesc, isPublic);
            }
            
            if (itnName.length() < 2 || itnName.length() > 50) {
                model.addAttribute("errorMessage", "行程名稱長度必須在2-50字之間");
                return returnFormWithData(model, itnName, itnDesc, isPublic);
            }
            
            // 檢查特殊字符
            if (containsSpecialCharacters(itnName)) {
                model.addAttribute("errorMessage", "行程名稱不能包含特殊字符: < > { } [ ] | \\ \" '");
                return returnFormWithData(model, itnName, itnDesc, isPublic);
            }
            
            // 驗證行程描述
            if (itnDesc == null || itnDesc.trim().isEmpty()) {
                model.addAttribute("errorMessage", "行程描述不能為空");
                return returnFormWithData(model, itnName, itnDesc, isPublic);
            }
            
            if (itnDesc.length() < 10 || itnDesc.length() > 500) {
                model.addAttribute("errorMessage", "行程描述長度必須在10-500字之間");
                return returnFormWithData(model, itnName, itnDesc, isPublic);
            }
            
            // 驗證景點選擇
            if (spotIds == null || spotIds.isEmpty()) {
                model.addAttribute("errorMessage", "請至少選擇一個景點");
                return returnFormWithData(model, itnName, itnDesc, isPublic);
            }
            
            // 建立新的行程物件
            ItineraryVO newItinerary = new ItineraryVO();
            newItinerary.setItnName(itnName);
            newItinerary.setItnDesc(itnDesc);
            newItinerary.setIsPublic(isPublic);
            newItinerary.setCrtId(memId); // 使用當前登入會員ID
            newItinerary.setItnStatus((byte) 1); // 預設為上架狀態
            newItinerary.setCreatorType((byte) 1); // 會員建立
            
            // 儲存行程
            ItineraryVO savedItinerary = itineraryService.addItinerary(newItinerary);
            
            // 添加景點到行程
            if (spotIds != null && !spotIds.isEmpty()) {
                for (Integer spotId : spotIds) {
                    try {
                        itineraryService.addSpotToItinerary(savedItinerary.getItnId(), spotId);
                    } catch (Exception e) {
                        // 記錄錯誤但繼續處理其他景點
                        System.err.println("添加景點失敗 ID=" + spotId + ": " + e.getMessage());
                    }
                }
            }
            
            // 導向到行程詳情頁面
            return "redirect:/itinerary/detail/" + savedItinerary.getItnId();
            
        } catch (Exception e) {
            // 如果發生錯誤，回到建立頁面並顯示錯誤訊息
            model.addAttribute("errorMessage", "建立行程失敗：" + e.getMessage());
            return returnFormWithData(model, itnName, itnDesc, isPublic);
        }
    }

    /**
     * 返回表單並保留已填寫的數據
     */
    private String returnFormWithData(Model model, String itnName, String itnDesc, Byte isPublic) {
        model.addAttribute("itnName", itnName);
        model.addAttribute("itnDesc", itnDesc);
        model.addAttribute("isPublic", isPublic);
        return "front-end/itinerary/add";
    }

    /**
     * 檢查字串是否包含特殊字符
     */
    private boolean containsSpecialCharacters(String text) {
        String specialChars = "<>{}[]|\\\"\\'";
        for (char c : specialChars.toCharArray()) {
            if (text.indexOf(c) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 從 Session 中獲取會員 ID
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
     * 編輯行程頁面
     */
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Integer id, Model model) {
        try {
            // 根據ID載入行程資料
            ItineraryVO itinerary = itineraryService.getItineraryById(id);
            
            if (itinerary == null) {
                model.addAttribute("errorMessage", "找不到指定的行程");
                return "404error";
            }
            
            model.addAttribute("itinerary", itinerary);
            model.addAttribute("itineraryId", id);
            model.addAttribute("isEdit", true);
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "載入行程資料時發生錯誤：" + e.getMessage());
            return "500error";
        }
        
        return "front-end/itinerary/add"; // 使用同一個表單頁面
    }

    /**
     * 我的行程頁面
     */
    @GetMapping("/my")
    public String myPage() {
        return "front-end/itinerary/my";
    }

    /**
     * 我的收藏頁面
     */
    @GetMapping("/favorites")
    public String favoritesPage() {
        return "front-end/itinerary/favorites";
    }
} 