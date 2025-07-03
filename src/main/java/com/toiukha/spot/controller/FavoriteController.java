package com.toiukha.spot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
// ... 其他你需要的 import

@Controller
public class FavoriteController {

    // 假設你已經 @Autowired 了你的 Service
    // @Autowired
    // private FavoriteService favoriteService;

    /**
     * 【路徑 1：給你獨立測試用】
     * 這個方法回傳【完整的 HTML 檔案】，讓你可以直接用 URL 看到畫面。
     * @param model 用來傳遞資料給前端
     * @return 完整的 HTML 檔案路徑
     */
    @GetMapping("/test/my-favorites")
    public String showStandaloneFavoritesPage(Model model) {
        
        // --- 你的商業邏輯：從資料庫撈取收藏列表 ---
        // List<FavoriteRecord> favoriteList = favoriteService.findMyFavorites();
        // model.addAttribute("favoriteRecords", favoriteList);
        // --- 商業邏輯結束 ---
        
        // 直接回傳上面那個完整的 HTML 檔案的路徑
        return "front-end/spot/my-favorites-main"; 
    }

    /**
     * 【路徑 2：給會員模組整合用】
     * 這個方法只回傳 HTML 檔案中，被標記為 "spot-favorites-content" 的那一小塊內容。
     * @param model 用來傳遞資料給前端
     * @return HTML 片段的路徑
     */
    @GetMapping("/fragments/favorites-content")
    public String getFavoritesContentFragment(Model model) {
        
        // --- 商業邏輯跟上面完全一樣 ---
        // List<FavoriteRecord> favoriteList = favoriteService.findMyFavorites();
        // model.addAttribute("favoriteRecords", favoriteList);
        // --- 商業邏輯結束 ---
        
        // 【注意看語法】 "檔案路徑 :: 片段名稱"
        // 這會告訴 Thymeleaf 只渲染那個檔案裡的指定 fragment。
        return "front-end/spot/my-favorites-main :: spot-favorites-content";
    }
}

