package com.toiukha.spot.controller.user;

import com.toiukha.spot.model.SpotVO;
import com.toiukha.spot.service.SpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/spot/user")
public class SpotListController {

    @Autowired
    private SpotService spotService;

    @GetMapping("/list")
    public String listSpots(Model model,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(required = false) String region,
                           @RequestParam(required = false) Double rating,
                           @RequestParam(required = false) String sortBy,
                           @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
        
        List<SpotVO> spots = spotService.getActiveSpots();
        
        // 地區分群對應表
        Map<String, List<String>> regionMap = Map.of(
            "北部", List.of("台北市", "新北市", "基隆市", "桃園市", "新竹市", "新竹縣", "宜蘭縣"),
            "中部", List.of("台中市", "彰化縣", "南投縣", "雲林縣", "苗栗縣"),
            "南部", List.of("高雄市", "台南市", "嘉義市", "嘉義縣", "屏東縣"),
            "東部", List.of("花蓮縣", "台東縣"),
            "離島", List.of("澎湖縣", "金門縣", "連江縣")
        );
        
        // 根據關鍵字過濾
        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchKeyword = keyword.toLowerCase();
            spots = spots.stream()
                    .filter(spot -> spot.getSpotName().toLowerCase().contains(searchKeyword) ||
                                  spot.getSpotLoc().toLowerCase().contains(searchKeyword))
                    .collect(Collectors.toList());
        }
        
        // 根據地區過濾（分群）
        if (region != null && !region.trim().isEmpty() && !"所有地區".equals(region)) {
            List<String> regionList = regionMap.get(region);
            if (regionList != null) {
                spots = spots.stream()
                        .filter(spot -> spot.getRegion() != null && regionList.contains(spot.getRegion()))
                        .collect(Collectors.toList());
            } else {
                // 若不是分群名稱，直接比對欄位
                spots = spots.stream()
                        .filter(spot -> spot.getRegion() != null && spot.getRegion().equals(region))
                        .collect(Collectors.toList());
            }
        }
        
        // 根據評分過濾
        if (rating != null) {
            spots = spots.stream()
                    .filter(spot -> spot.getGoogleRating() != null && spot.getGoogleRating() >= rating)
                    .collect(Collectors.toList());
        }
        
        // 根據排序條件進行排序
        if (sortBy != null) {
            Comparator<SpotVO> comparator = switch (sortBy) {
                case "name" -> Comparator.comparing(SpotVO::getSpotName);
                case "date" -> Comparator.comparing(SpotVO::getSpotUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
                case "rating" -> Comparator.comparing(SpotVO::getGoogleRating, Comparator.nullsLast(Comparator.naturalOrder()));
                default -> null;
            };
            
            if (comparator != null) {
                if ("desc".equals(sortOrder)) {
                    comparator = comparator.reversed();
                }
                spots.sort(comparator);
            }
        }
        
        model.addAttribute("spotList", spots);
        model.addAttribute("searchKeyword", keyword);
        model.addAttribute("selectedRegion", region);
        model.addAttribute("rating", rating);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortOrder", sortOrder);
        
        // 添加地區選項
        model.addAttribute("regions", Arrays.asList("北部", "中部", "南部", "東部", "離島"));
        
        return "front-end/spot/spotlist";
    }

    @GetMapping("/search")
    public String searchPage() {
        return "front-end/spot/search";
    }
} 