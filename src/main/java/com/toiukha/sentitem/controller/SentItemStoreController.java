package com.toiukha.sentitem.controller;

import com.toiukha.sentitem.model.SentItemService;
import com.toiukha.sentitem.model.SentItemVO;
import com.toiukha.store.model.StoreVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/store")
public class SentItemStoreController {

    @Autowired
    SentItemService sentItemService;

    @GetMapping("/listAllSentItem")
    public String listAllSentItem(Model model, HttpSession session) {
    	Object storeObj = session.getAttribute("store");
		StoreVO store = (StoreVO) storeObj;
		int storeId = store.getStoreId();
		session.setAttribute("storeId", storeId);
		
        List<SentItemVO> sentItemList = sentItemService.findByStoreId(storeId);
        model.addAttribute("sentItemList", sentItemList);

        // 將 sentItemList 轉為 JSON 字串，以解決前端 Linter 報錯問題
        ObjectMapper mapper = new ObjectMapper();
        try {
            String sentItemListJson = mapper.writeValueAsString(sentItemList);
            model.addAttribute("sentItemListJson", sentItemListJson);
        } catch (JsonProcessingException e) {
            model.addAttribute("sentItemListJson", "[]"); // 發生錯誤時給一個空的 JSON 陣列
        }

        model.addAttribute("currentPage", "sentItem");
        model.addAttribute("activeItem", "sentItem");
        return "back-end/sentitem/listAllSentItem_store";
    }
} 