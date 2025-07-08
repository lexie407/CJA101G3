package com.toiukha.spot.controller;

import com.toiukha.spot.service.SpotService;
import com.toiukha.spot.model.SpotVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 景點資料初始化控制器
 * 用於初始化測試資料
 */
@Controller
@RequestMapping("/spot/admin")
public class SpotDataInitController {

    @Autowired
    private SpotService spotService;

    /**
     * 清理測試景點資料
     * @return 清理結果
     */
    @GetMapping("/clean-test-data")
    @ResponseBody
    public String cleanTestData() {
        try {
            // 定義測試景點的名稱列表
            List<String> testSpotNames = List.of(
                "日月潭", "阿里山", "太魯閣國家公園", "墾丁國家公園", 
                "九份老街", "清境農場", "野柳地質公園", "溪頭自然教育園區", 
                "花蓮七星潭", "台東三仙台"
            );
            
            int deletedCount = 0;
            for (String spotName : testSpotNames) {
                // 找到所有符合名稱的景點
                List<SpotVO> spots = spotService.searchSpots(spotName);
                for (SpotVO spot : spots) {
                    // 檢查是否為測試資料（通過 crtId 判斷）
                    if (spot.getCrtId() != null && spot.getCrtId() >= 10 && spot.getCrtId() <= 19) {
                        spotService.deleteById(spot.getSpotId());
                        deletedCount++;
                    }
                }
            }
            
            return "成功清理 " + deletedCount + " 個測試景點資料";
        } catch (Exception e) {
            e.printStackTrace();
            return "清理失敗: " + e.getMessage();
        }
    }

    /**
     * 初始化景點測試資料
     * @return 初始化結果
     */
    @GetMapping("/init-data")
    @ResponseBody
    public String initSpotData() {
        try {
            List<SpotVO> testSpots = createTestSpots();
            List<SpotVO> spotsToInsert = new ArrayList<>();
            
            // 過濾已存在的景點
            for (SpotVO spot : testSpots) {
                if (!spotService.existsBySpotNameAndSpotLoc(spot.getSpotName(), spot.getSpotLoc())) {
                    spotsToInsert.add(spot);
                }
            }
            
            if (spotsToInsert.isEmpty()) {
                return "所有測試景點資料已存在，無需重複初始化";
            }
            
            // 使用批次匯入方法提升效能
            List<SpotVO> savedSpots = spotService.addSpotsInBatch(spotsToInsert);
            
            return "成功初始化 " + savedSpots.size() + " 個景點資料（使用批次匯入）";
        } catch (Exception e) {
            e.printStackTrace();
            return "初始化失敗: " + e.getMessage();
        }
    }

    /**
     * 創建測試景點資料
     * @return 景點列表
     */
    private List<SpotVO> createTestSpots() {
        List<SpotVO> spots = new ArrayList<>();
        
        // 景點資料
        String[][] spotData = {
            {"日月潭", "南投縣魚池鄉", "23.857", "120.915", "台灣最著名的湖泊景點，風景優美", "10"},
            {"阿里山", "嘉義縣阿里山鄉", "23.511", "120.813", "著名的日出景點和櫻花勝地", "11"},
            {"太魯閣國家公園", "花蓮縣秀林鄉", "24.194", "121.621", "壯麗的峽谷地形和原住民文化", "12"},
            {"墾丁國家公園", "屏東縣恆春鎮", "22.006", "120.745", "台灣最南端的熱帶風情", "13"},
            {"九份老街", "新北市瑞芳區", "25.109", "121.845", "山城老街，電影悲情城市拍攝地", "14"},
            {"清境農場", "南投縣仁愛鄉", "24.063", "121.166", "高山牧場，綿羊秀表演", "15"},
            {"野柳地質公園", "新北市萬里區", "25.209", "121.693", "奇特的海蝕地形景觀", "16"},
            {"溪頭自然教育園區", "南投縣鹿谷鄉", "23.675", "120.795", "森林浴和竹林步道", "17"},
            {"花蓮七星潭", "花蓮縣新城鄉", "24.028", "121.623", "美麗的礫石海灘", "18"},
            {"台東三仙台", "台東縣成功鎮", "23.133", "121.413", "八拱跨海步橋連接離岸小島", "19"}
        };
        
        for (String[] data : spotData) {
            SpotVO spot = new SpotVO();
            spot.setSpotName(data[0]);
            spot.setSpotLoc(data[1]);
            spot.setSpotLat(Double.parseDouble(data[2]));
            spot.setSpotLng(Double.parseDouble(data[3]));
            spot.setSpotDesc(data[4]);
            spot.setCrtId(Integer.parseInt(data[5]));
            spot.setSpotStatus((byte) 1); // 上架狀態
            spot.setSpotCreateAt(LocalDateTime.now());
            spots.add(spot);
        }
        
        return spots;
    }
} 