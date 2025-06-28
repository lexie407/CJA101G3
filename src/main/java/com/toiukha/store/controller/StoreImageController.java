package com.toiukha.store.controller;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.toiukha.store.model.StoreService;
import com.toiukha.store.model.StoreVO;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/store")
public class StoreImageController {
	
	@Autowired
    private StoreService storeService;

    @GetMapping("/DBGifReader")
    public void getImage(
            @RequestParam("storeId") Integer storeId,
            HttpServletResponse response) throws IOException {

        StoreVO store = storeService.getById(storeId);
        byte[] imageBytes = store.getStoreImg();

        response.setContentType("image/png"); // 可視實際格式調整
        ServletOutputStream out = response.getOutputStream();

        if (imageBytes != null && imageBytes.length > 0) {
            out.write(imageBytes);
        } else {
            InputStream defaultImg = getClass().getResourceAsStream("static/images/user.png");
            out.write(defaultImg.readAllBytes());
        }

        out.flush();
        out.close();
    }

}
