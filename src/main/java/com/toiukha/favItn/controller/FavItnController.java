package com.toiukha.favItn.controller;

import com.toiukha.favItn.model.FavItnId;
import com.toiukha.favItn.model.FavItnService;
import com.toiukha.favItn.model.FavItnVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favItn")
public class FavItnController {

    @Autowired
    private FavItnService favItnService;

    //查全部，GET方法
    @GetMapping
    public List<FavItnVO> getAllFavItns() {
        return favItnService.findAll();
    }

    /* 執行收藏，有紀錄就刪除，沒紀錄就新增
     * JSON格式
     * {
     * 	"favItnId": ,
     *  "memId": 
     * }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> doFavItn(@RequestBody FavItnId favItnId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 使用線程安全的切換方法
            boolean isFavorited = favItnService.toggleFavorite(favItnId);
            
            if(isFavorited) {
                response.put("message", "已加入收藏");
                response.put("isFavorited", true);
            } else {
                response.put("message", "已取消收藏");
                response.put("isFavorited", false);
            }
            
            response.put("success", true);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "操作失敗：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
    	}
    }

}
