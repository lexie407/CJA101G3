package com.toiukha.groupactivity.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 預設圖片服務
 * 負責載入和管理預設圖片
 */
@Service
public class DefaultImageService {
    //改成全組通用的位置
    private static final String DEFAULT_IMAGE_PATH = "uploads/common/default.png";
//    private static final String DEFAULT_IMAGE_PATH = "src/main/resources/static/act/default.png"; //目前是錯誤
    private byte[] defaultImageBytes;
    
    /**
     * 取得預設圖片資料
     * @return 預設圖片的 byte[] 資料
     */
    public byte[] getDefaultImage() {
        if (defaultImageBytes == null) {
            loadDefaultImage();
        }
        return defaultImageBytes;
    }
    
    /**
     * 載入預設圖片 
     */
    private void loadDefaultImage() {
        try {
            // 嘗試從uploads目錄載入
            Path path = Paths.get(DEFAULT_IMAGE_PATH);
            if (Files.exists(path)) {
                defaultImageBytes = Files.readAllBytes(path);
                System.out.println("成功載入預設圖片: " + DEFAULT_IMAGE_PATH);
                return;
            }
            
            // 如果檔案不存在，嘗試從classpath載入
            try (InputStream inputStream = new ClassPathResource("static/images/act/default.png").getInputStream()) {
                defaultImageBytes = inputStream.readAllBytes();
                System.out.println("從classpath載入預設圖片");
                return;
            }
        } catch (IOException e) {
            System.err.println("載入預設圖片失敗: " + e.getMessage());
            // 如果無法載入預設圖片，創建一個最小的空白圖片
            createEmptyImageBytes();
        }
    }
    
    /**
     * 創建一個最小的空白圖片 - 最後的錯誤處理機制
     */
    private void createEmptyImageBytes() {
        try {
            // 創建一個1x1像素的透明PNG圖片
            String base64EmptyImage = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
            defaultImageBytes = java.util.Base64.getDecoder().decode(base64EmptyImage);
            System.out.println("使用空白圖片作為預設圖片");
        } catch (Exception e) {
            System.err.println("創建空白圖片失敗: " + e.getMessage());
            defaultImageBytes = new byte[0]; // 最後的防護：空陣列
        }
    }
    
    /**
     * 檢查是否為預設圖片
     * @param imageBytes 圖片資料
     * @return 是否為預設圖片
     */
    public boolean isDefaultImage(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            return true;
        }
        
        byte[] defaultImage = getDefaultImage();
        if (imageBytes.length != defaultImage.length) {
            return false;
        }
        
        // 比較前幾個位元組來判斷是否為預設圖片
        for (int i = 0; i < Math.min(16, imageBytes.length); i++) {
            if (imageBytes[i] != defaultImage[i]) {
                return false;
            }
        }
        return true;
    }
} 