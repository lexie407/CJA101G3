package com.toiukha.itinerary.controller;

import com.toiukha.itinerary.service.ItineraryService;
import com.toiukha.itinerary.model.ItineraryVO;
import com.toiukha.itinerary.model.ItnSpotVO;
import com.toiukha.favItn.model.FavItnService;
import com.toiukha.members.model.MembersService;
import com.toiukha.members.model.MembersVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 行程模組頁面控制器
 * 處理行程相關的頁面路由
 */
@Controller
@RequestMapping("/itinerary")
public class ItineraryPageController {

    @Autowired
    private ItineraryService itineraryService;

    @Autowired
    private FavItnService favItnService;

    @Autowired
    private MembersService membersService;

    /**
     * 行程列表頁面
     */
    @GetMapping("/itnlist")
    public String itnlist(
                          @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer isPublic,
            @RequestParam(required = false) Integer creatorType,
            HttpSession session,
            Model model) {
        
        try {
            // 獲取當前登入會員ID
            Integer memId = getMemIdFromSession(session);
            
            // 根據條件查詢行程列表
            List<ItineraryVO> itineraryList;
            if (keyword != null && !keyword.trim().isEmpty()) {
                itineraryList = itineraryService.searchItineraries(keyword, isPublic);
            } else if (isPublic != null) {
                itineraryList = itineraryService.getItinerariesByPublicStatus(isPublic);
            } else {
                itineraryList = itineraryService.getPublicItineraries();
            }
            
            // 根據建立者類型篩選
            // 允許會員(1)、管理員(2)和揪團活動(3)建立的行程
            final List<Byte> allowedCreatorTypes = List.of((byte) 1, (byte) 2, (byte) 3); 

            if (creatorType != null) {
                // 如果有指定類型，則只篩選該類型
                itineraryList = itineraryList.stream()
                    .filter(itinerary -> itinerary.getCreatorType() != null && 
                            itinerary.getCreatorType().equals(creatorType.byteValue()))
                    .collect(Collectors.toList());
            } else {
                // 如果未指定類型，則顯示所有允許的類型
                itineraryList = itineraryList.stream()
                    .filter(itinerary -> itinerary.getCreatorType() != null && 
                            allowedCreatorTypes.contains(itinerary.getCreatorType()))
                    .collect(Collectors.toList());
            }
            
            // 設置建立者顯示名稱
            itineraryService.setCreatorDisplayNames(itineraryList, memId);
            
            // 檢查每個行程的收藏狀態
            if (memId != null) {
            List<Integer> favoriteIds = favItnService.findFavoriteIdsByMemId(memId);
            for (ItineraryVO itinerary : itineraryList) {
                itinerary.setIsFavorited(favoriteIds.contains(itinerary.getItnId()));
                }
            }
            
            model.addAttribute("itineraryList", itineraryList);
            model.addAttribute("currentPage", "itinerary");
            
            return "front-end/itinerary/itnlist";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "載入行程列表失敗：" + e.getMessage());
            model.addAttribute("currentPage", "itinerary");
        return "front-end/itinerary/itnlist";
        }
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
        
        // 明確設置為非編輯模式
        model.addAttribute("isEdit", false);
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
            
            if (itnDesc.length() < 5 || itnDesc.length() > 500) {
                model.addAttribute("errorMessage", "行程描述長度必須在5-500字之間");
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
    public String editPage(@PathVariable Integer id, Model model, HttpSession session) {
        try {
            // 檢查會員是否已登入
            Integer memId = getMemIdFromSession(session);
            if (memId == null) {
                // 未登入，重定向到登入頁面
                return "redirect:/members/login?redirect=/itinerary/edit/" + id;
            }
            
            // 根據ID載入行程資料，包含景點資料
            ItineraryVO itinerary = itineraryService.getItineraryWithSpots(id);
            
            if (itinerary == null) {
                model.addAttribute("errorMessage", "找不到指定的行程");
                return "404error";
            }
            
            // 檢查是否有權限編輯此行程（只能編輯自己建立的行程）
            if (!itinerary.getCrtId().equals(memId)) {
                model.addAttribute("errorMessage", "您沒有權限編輯此行程");
                return "redirect:/itinerary/detail/" + id;
            }
            
            // 獲取當前行程已選景點的ID列表
            List<Integer> selectedSpotIds = itinerary.getItnSpots().stream()
                                                     .map(itnSpot -> itnSpot.getSpot().getSpotId())
                                                     .collect(Collectors.toList());
            
            // 傳遞行程資料到表單
            model.addAttribute("itinerary", itinerary);
            model.addAttribute("itineraryId", id);
            model.addAttribute("itnName", itinerary.getItnName());
            model.addAttribute("itnDesc", itinerary.getItnDesc());
            model.addAttribute("isPublic", itinerary.getIsPublic());
            model.addAttribute("selectedSpotIds", selectedSpotIds);
            model.addAttribute("isEdit", true);
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "載入行程資料時發生錯誤：" + e.getMessage());
            return "500error";
        }
        
        return "front-end/itinerary/add"; // 使用同一個表單頁面
    }
    
    /**
     * 處理編輯行程表單提交
     */
    @PostMapping("/edit/{id}")
    public String updateItinerary(@PathVariable Integer id,
                          @RequestParam String itnName,
                          @RequestParam String itnDesc,
                          @RequestParam(defaultValue = "1") Byte isPublic,
                          @RequestParam(required = false) List<Integer> spotIds,
                          HttpSession session,
                          Model model) {
        // 檢查會員是否已登入
        Integer memId = getMemIdFromSession(session);
        if (memId == null) {
            // 未登入，重定向到登入頁面
            return "redirect:/members/login?redirect=/itinerary/edit/" + id;
        }
        
        try {
            // 根據ID載入行程資料
            ItineraryVO itinerary = itineraryService.getItineraryById(id);
            
            if (itinerary == null) {
                model.addAttribute("errorMessage", "找不到指定的行程");
                return "404error";
            }
            
            // 檢查是否有權限編輯此行程（只能編輯自己建立的行程）
            if (!itinerary.getCrtId().equals(memId)) {
                model.addAttribute("errorMessage", "您沒有權限編輯此行程");
                return "redirect:/itinerary/detail/" + id;
            }
            
            // 驗證行程名稱
            if (itnName == null || itnName.trim().isEmpty()) {
                model.addAttribute("errorMessage", "行程名稱不能為空");
                return returnEditFormWithData(model, id, itnName, itnDesc, isPublic);
            }
            
            if (itnName.length() < 2 || itnName.length() > 50) {
                model.addAttribute("errorMessage", "行程名稱長度必須在2-50字之間");
                return returnEditFormWithData(model, id, itnName, itnDesc, isPublic);
            }
            
            // 檢查特殊字符
            if (containsSpecialCharacters(itnName)) {
                model.addAttribute("errorMessage", "行程名稱不能包含特殊字符: < > { } [ ] | \\ \" '");
                return returnEditFormWithData(model, id, itnName, itnDesc, isPublic);
            }
            
            // 驗證行程描述
            if (itnDesc == null || itnDesc.trim().isEmpty()) {
                model.addAttribute("errorMessage", "行程描述不能為空");
                return returnEditFormWithData(model, id, itnName, itnDesc, isPublic);
            }
            
            if (itnDesc.length() < 5 || itnDesc.length() > 500) {
                model.addAttribute("errorMessage", "行程描述長度必須在5-500字之間");
                return returnEditFormWithData(model, id, itnName, itnDesc, isPublic);
            }
            
            // 驗證景點選擇
            if (spotIds == null || spotIds.isEmpty()) {
                model.addAttribute("errorMessage", "請至少選擇一個景點");
                return returnEditFormWithData(model, id, itnName, itnDesc, isPublic);
            }
            
            // 更新行程資料
            itinerary.setItnName(itnName);
            itinerary.setItnDesc(itnDesc);
            itinerary.setIsPublic(isPublic);
            
            // 儲存行程
            ItineraryVO updatedItinerary = itineraryService.updateItinerary(itinerary);
            
            // 清除原有景點關聯
            itineraryService.clearItinerarySpots(id);
            
            // 添加景點到行程
            if (spotIds != null && !spotIds.isEmpty()) {
                for (Integer spotId : spotIds) {
                    try {
                        itineraryService.addSpotToItinerary(updatedItinerary.getItnId(), spotId);
                    } catch (Exception e) {
                        // 記錄錯誤但繼續處理其他景點
                        System.err.println("添加景點失敗 ID=" + spotId + ": " + e.getMessage());
                    }
                }
            }
            
            // 導向到行程詳情頁面
            return "redirect:/itinerary/detail/" + updatedItinerary.getItnId();
            
        } catch (Exception e) {
            // 如果發生錯誤，回到編輯頁面並顯示錯誤訊息
            model.addAttribute("errorMessage", "編輯行程失敗：" + e.getMessage());
            return returnEditFormWithData(model, id, itnName, itnDesc, isPublic);
        }
    }
    
    /**
     * 返回編輯表單並保留已填寫的數據
     */
    private String returnEditFormWithData(Model model, Integer id, String itnName, String itnDesc, Byte isPublic) {
        model.addAttribute("itineraryId", id);
        model.addAttribute("itnName", itnName);
        model.addAttribute("itnDesc", itnDesc);
        model.addAttribute("isPublic", isPublic);
        model.addAttribute("isEdit", true);
        return "front-end/itinerary/add";
    }

    /**
     * 我的行程頁面
     */
    @GetMapping("/my")
    public String myPage(Model model, HttpSession session) {
        // 檢查會員是否已登入
        Integer memId = getMemIdFromSession(session);
        if (memId == null) {
            // 未登入，重定向到登入頁面
            return "redirect:/members/login?redirect=/itinerary/my";
        }
        
        try {
            // 載入會員的所有行程
            List<ItineraryVO> itineraryList = itineraryService.getItinerariesByCrtId(memId);
            model.addAttribute("itineraryList", itineraryList);
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "載入行程列表失敗：" + e.getMessage());
        }
        
        return "front-end/itinerary/my";
    }

    /**
     * 我的收藏頁面
     */
    @GetMapping("/favorites")
    public String favoritesPage(Model model) {
        model.addAttribute("activeItem", "favorites");
        return "front-end/itinerary/favorites";
    }
    
    /**
     * 切換行程公開/私人狀態
     */
    @PostMapping("/toggle-visibility/{id}")
    @ResponseBody
    public Map<String, Object> toggleVisibility(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 檢查會員是否已登入
            Integer memId = getMemIdFromSession(session);
            if (memId == null) {
                response.put("success", false);
                response.put("message", "請先登入");
                return response;
            }
            
            // 根據ID載入行程資料
            ItineraryVO itinerary = itineraryService.getItineraryById(id);
            
            if (itinerary == null) {
                response.put("success", false);
                response.put("message", "找不到指定的行程");
                return response;
            }
            
            // 檢查是否有權限編輯此行程（只能編輯自己建立的行程）
            if (!itinerary.getCrtId().equals(memId)) {
                response.put("success", false);
                response.put("message", "您沒有權限修改此行程");
                return response;
            }
            
            // 切換公開/私人狀態
            byte newStatus = (byte) (itinerary.getIsPublic() == 1 ? 0 : 1);
            itinerary.setIsPublic(newStatus);
            
            // 儲存行程
            itineraryService.updateItinerary(itinerary);
            
            response.put("success", true);
            response.put("isPublic", newStatus == 1);
            response.put("message", newStatus == 1 ? "行程已設為公開" : "行程已設為私人");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "修改狀態失敗：" + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 刪除行程
     */
    @PostMapping("/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteItinerary(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 檢查會員是否已登入
            Integer memId = getMemIdFromSession(session);
            if (memId == null) {
                response.put("success", false);
                response.put("message", "請先登入");
                return response;
            }
            
            // 根據ID載入行程資料
            ItineraryVO itinerary = itineraryService.getItineraryById(id);
            
            if (itinerary == null) {
                response.put("success", false);
                response.put("message", "找不到指定的行程");
                return response;
            }
            
            // 檢查是否有權限刪除此行程（只能刪除自己建立的行程）
            if (!itinerary.getCrtId().equals(memId)) {
                response.put("success", false);
                response.put("message", "您沒有權限刪除此行程");
                return response;
            }
            
            // 刪除行程
            itineraryService.deleteItinerary(id);
            
            response.put("success", true);
            response.put("message", "行程已成功刪除");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "刪除行程失敗：" + e.getMessage());
        }
        
        return response;
    }
} 