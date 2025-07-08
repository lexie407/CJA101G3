package com.toiukha.groupactivity.controller;

import com.toiukha.groupactivity.model.ActRepository;
import com.toiukha.groupactivity.service.DefaultImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 活動圖片控制器
 * 專門處理活動圖片的載入，避免在列表查詢時載入圖片
 * 參考 DBGifReader 的做法
 */
@Controller
@RequestMapping("/act")
public class ActImageController {
    
    @Autowired
    private ActRepository actRepo;
    
    @Autowired
    private DefaultImageService defaultImageService;
    
    /**
     * 載入活動圖片
     * 參考 DBGifReader 的做法，只查詢圖片資料
     */
    @GetMapping("/DBGifReader")
    public void getActImage(
            @RequestParam("actId") Integer actId,
            HttpServletResponse response) throws IOException {
        
        ServletOutputStream out = null;
        try {
            // 只查詢圖片資料，不載入其他欄位
            byte[] imageBytes = actRepo.findActImgByActId(actId);
            
            response.setContentType("image/jpeg");
            out = response.getOutputStream();
            
            if (imageBytes != null && imageBytes.length > 0) {
                out.write(imageBytes);
            } else {
                // 使用預設圖片
                try {
                    byte[] defaultImage = defaultImageService.getDefaultImage();
                    out.write(defaultImage);
                } catch (Exception e) {
                    System.err.println("載入預設圖片失敗: " + e.getMessage());
                    // 如果連預設圖片都無法載入，返回一個最小的透明PNG
                    String emptyPng = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
                    byte[] emptyImage = java.util.Base64.getDecoder().decode(emptyPng);
                    response.setContentType("image/png");
                    out.write(emptyImage);
                }
            }
        } catch (Exception e) {
            System.err.println("載入活動圖片失敗 (actId: " + actId + "): " + e.getMessage());
            // 確保回應正確關閉
            if (out != null) {
                try {
                    String emptyPng = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
                    byte[] emptyImage = java.util.Base64.getDecoder().decode(emptyPng);
                    response.setContentType("image/png");
                    out.write(emptyImage);
                } catch (Exception ex) {
                    System.err.println("寫入空白圖片失敗: " + ex.getMessage());
                }
            }
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    System.err.println("關閉輸出流失敗: " + e.getMessage());
                }
            }
        }
    }
} 