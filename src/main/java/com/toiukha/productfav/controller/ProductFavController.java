package com.toiukha.productfav.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.toiukha.members.model.MembersVO;
import com.toiukha.productfav.model.ProductFavService;
import com.toiukha.productfav.model.ProductFavVO;
import com.toiukha.item.model.ItemService;
import com.toiukha.item.model.ItemVO;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

/**
 * ProductFav Controller
 * 處理商品收藏相關的請求
 */
@Controller
@RequestMapping("/productfav")
public class ProductFavController {
    
    @Autowired
    private ProductFavService productFavService;
    
    @Autowired
    private ItemService itemService;
    
    /**
     * 檢查會員是否已登入
     * @param session HTTP Session
     * @return 會員VO，如果未登入則返回null
     */
    private MembersVO getCurrentMember(HttpSession session) {
        return (MembersVO) session.getAttribute("member");
    }
    
    /**
     * 檢查會員登入狀態
     * @param session HTTP Session
     * @return true 如果已登入，false 如果未登入
     */
    private boolean isMemberLoggedIn(HttpSession session) {
        return getCurrentMember(session) != null;
    }
    
    /**
     * 驗證前端傳來的 memId 是否與 session 中的會員一致
     * @param memIdFromFrontend 前端傳來的會員ID
     * @param session HTTP Session
     * @return true 如果一致，false 如果不一致
     */
    private boolean validateMemberId(String memIdFromFrontend, HttpSession session) {
        // 檢查會員是否登入
        if (!isMemberLoggedIn(session)) {
            return false;
        }
        
        MembersVO sessionMember = getCurrentMember(session);
        Integer sessionMemId = sessionMember.getMemId();
        
        try {
            Integer frontendMemId = Integer.valueOf(memIdFromFrontend);
            return sessionMemId.equals(frontendMemId);
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 新增商品收藏
     * @param itemId 商品ID
     * @param memId 會員ID (前端傳送)
     * @param session HTTP Session
     * @return 簡單字符串回應
     */
    @PostMapping("/add_to_favorite")
    @ResponseBody
    public String addToFavorite(@RequestParam("itemId") String itemId,
                               @RequestParam("memId") String memId,
                               HttpSession session) {
        try {
            // 驗證會員身份（雙重安全檢查）
            if (!validateMemberId(memId, session)) {
                return "not_logged_in";
            }
            
            // 參數轉換
            Integer itemIdInt = Integer.valueOf(itemId);
            Integer memIdInt = Integer.valueOf(memId);
            
            // 檢查是否已經收藏過
            if (productFavService.isFavorite(memIdInt, itemIdInt)) {
            	productFavService.removeFavorite(memIdInt, itemIdInt);
                return "取消收藏";
            }
            
            
            // 新增收藏
            productFavService.addFavorite(memIdInt, itemIdInt);
            return "success";
            
        } catch (Exception e) {
            return "error";
        }
    }
    
    /**
     * 移除商品收藏
     * @param itemId 商品ID
     * @param memId 會員ID (前端傳送)
     * @param session HTTP Session
     * @return 簡單字符串回應
     */
    @PostMapping("/remove_from_favorite")
    @ResponseBody
    public String removeFromFavorite(@RequestParam("itemId") String itemId,
                                   @RequestParam("memId") String memId,
                                   HttpSession session) {
        try {
            // 驗證會員身份（雙重安全檢查）
            if (!validateMemberId(memId, session)) {
                return "not_logged_in";
            }
            
            // 參數轉換
            Integer itemIdInt = Integer.valueOf(itemId);
            Integer memIdInt = Integer.valueOf(memId);
            
            // 移除收藏
            boolean removed = productFavService.removeFavorite(memIdInt, itemIdInt);
            
            if (removed) {
                return "success";
            } else {
                return "not_found";
            }
            
        } catch (Exception e) {
            return "error";
        }
    }
    
    /**
     * 切換收藏狀態（AJAX用）
     * @param itemId 商品ID
     * @param memId 會員ID (前端傳送)
     * @param session HTTP Session
     * @return 簡單字符串回應：added(已加入) 或 removed(已移除)
     */
    @PostMapping("/toggle_favorite")
    @ResponseBody
    public String toggleFavorite(@RequestParam("itemId") String itemId,
                               @RequestParam("memId") String memId,
                               HttpSession session) {
        try {
            // 驗證會員身份（雙重安全檢查）
            if (!validateMemberId(memId, session)) {
                return "not_logged_in";
            }
            
            // 參數轉換
            Integer itemIdInt = Integer.valueOf(itemId);
            Integer memIdInt = Integer.valueOf(memId);
            
            // 切換收藏狀態
            boolean isAdded = productFavService.toggleFavorite(memIdInt, itemIdInt);
            
            return isAdded ? "added" : "removed";
            
        } catch (Exception e) {
            return "error";
        }
    }
    
    /**
     * 查看會員的收藏清單
     * @param session HTTP Session
     * @param model Spring Model
     * @return 收藏清單頁面
     */
    @GetMapping("/myFavorites")
    public String showMyFavorites(HttpSession session, Model model, HttpServletRequest request) {
        // 檢查登入狀態
        if (!isMemberLoggedIn(session)) {
            // 保存原始請求URI，登入後可以重定向回來
            session.setAttribute("location", request.getRequestURI());
            return "redirect:/members/login";
        }
        
        try {
            MembersVO member = getCurrentMember(session);
            Integer memId = member.getMemId();
            
            // 獲取收藏清單
            List<ProductFavVO> favorites = productFavService.getMemberFavorites(memId);
            
            // 獲取商品詳細信息
            Map<Integer, ItemVO> itemDetails = new HashMap<>();
            Map<Integer, String> itemImages = new HashMap<>();
            for (ProductFavVO favorite : favorites) {
                ItemVO item = itemService.getOneItem(favorite.getItemId());
                if (item != null) {
                    itemDetails.put(favorite.getItemId(), item);
                    
                    // 處理圖片：轉換為 base64
                    if (item.getItemImg() != null && item.getItemImg().length > 0) {
                        try {
                            String base64Image = java.util.Base64.getEncoder().encodeToString(item.getItemImg());
                            itemImages.put(favorite.getItemId(), "data:image/jpeg;base64," + base64Image);
                        } catch (Exception e) {
                            System.err.println("圖片轉換失敗，商品ID: " + favorite.getItemId());
                            itemImages.put(favorite.getItemId(), "/images/default-product.png"); // 預設圖片
                        }
                    } else {
                        itemImages.put(favorite.getItemId(), "/images/default-product.png"); // 預設圖片
                    }
                }
            }
            
            model.addAttribute("favorites", favorites);
            model.addAttribute("itemDetails", itemDetails);
            model.addAttribute("itemImages", itemImages);
            model.addAttribute("favoriteCount", favorites.size());
            model.addAttribute("member", member);
            // 設定導覽列的 active 狀態
            model.addAttribute("activeItem", "myFavorites");
            
            return "front-end/productfav/myFavorites";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "載入收藏清單失敗：" + e.getMessage());
            // 即使發生錯誤，也要設定導覽列的 active 狀態
            model.addAttribute("activeItem", "myFavorites");
            return "front-end/productfav/myFavorites";
        }
    }
    
    /**
     * 檢查商品是否已收藏（AJAX用）
     * @param itemId 商品ID
     * @param memId 會員ID (前端傳送)
     * @param session HTTP Session
     * @return 簡單字符串回應：true(已收藏) 或 false(未收藏)
     */
    @PostMapping("/check_favorite")
    @ResponseBody
    public String checkFavorite(@RequestParam("itemId") String itemId,
                               @RequestParam("memId") String memId,
                               HttpSession session) {
        try {
            // 驗證會員身份（雙重安全檢查）
            if (!validateMemberId(memId, session)) {
                return "not_logged_in";
            }
            
            // 參數轉換
            Integer itemIdInt = Integer.valueOf(itemId);
            Integer memIdInt = Integer.valueOf(memId);
            
            // 檢查收藏狀態
            boolean isFavorite = productFavService.isFavorite(memIdInt, itemIdInt);
            
            return isFavorite ? "true" : "false";
            
        } catch (Exception e) {
            return "false";
        }
    }
    
    /**
     * 獲取會員收藏統計資訊
     * @param session HTTP Session
     * @return JSON 回應
     */
    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFavoriteStats(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        if (!isMemberLoggedIn(session)) {
            response.put("favoriteCount", 0);
            response.put("isLoggedIn", false);
        } else {
            MembersVO member = getCurrentMember(session);
            Integer memId = member.getMemId();
            
            long favoriteCount = productFavService.getMemberFavoriteCount(memId);
            response.put("favoriteCount", favoriteCount);
            response.put("isLoggedIn", true);
        }
        
        return ResponseEntity.ok(response);
    }
} 