package com.toiukha.favItn.controller;

import com.toiukha.favItn.model.FavItnId;
import com.toiukha.favItn.model.FavItnService;
import com.toiukha.favItn.model.FavItnVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public void doFavItn(@RequestBody FavItnId favItnId) {
    	FavItnVO favItnVO = new FavItnVO(favItnId);
    	if(!favItnService.findById(favItnId).isEmpty()) {
    		favItnService.deleteById(favItnId);
    	}else {
    		favItnService.save(favItnVO);
    	}
    	
    }

}
