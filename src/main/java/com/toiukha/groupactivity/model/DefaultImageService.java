package com.toiukha.groupactivity.model;

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
            // 嘗試從檔案系統載入
            Path filePath = Paths.get(DEFAULT_IMAGE_PATH);
            if (Files.exists(filePath)) {
                defaultImageBytes = Files.readAllBytes(filePath);
                return;
            }
            
            // 如果檔案系統沒有，嘗試從 classpath 載入
            ClassPathResource resource = new ClassPathResource("static/images/act/default.png");
            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    defaultImageBytes = inputStream.readAllBytes();
                    return;
                }
            }
            
            // 如果都沒有，建立一個簡單的預設圖片
            defaultImageBytes = createSimpleDefaultImage();
            
        } catch (IOException e) {
            // 如果載入失敗，建立一個簡單的預設圖片
            defaultImageBytes = createSimpleDefaultImage();
        }
    }
    
    /**
     * 建立簡單的預設圖片（1x1 像素的透明圖片）
     * @return 簡單預設圖片的 byte[] 資料
     */
    private byte[] createSimpleDefaultImage() {
        // 建立一個 1x1 像素的透明 PNG 圖片
        return new byte[] {
            (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47,  // PNG 簽名
            (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0D,  // IHDR chunk
            (byte) 0x49, (byte) 0x48, (byte) 0x44, (byte) 0x52,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,  // 寬度 1
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,  // 高度 1
            (byte) 0x08, (byte) 0x06, (byte) 0x00, (byte) 0x00,  // 位元深度 8, 顏色類型 6 (RGBA)
            (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0C,  // IDAT chunk
            (byte) 0x49, (byte) 0x44, (byte) 0x41, (byte) 0x54,
            (byte) 0x08, (byte) 0x99, (byte) 0x01, (byte) 0x01,  // 壓縮資料
            (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02,  // 0x00, 0x00
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,  // IEND chunk
            (byte) 0x49, (byte) 0x45, (byte) 0x4E, (byte) 0x44,
            (byte) 0xAE, (byte) 0x42, (byte) 0x60, (byte) 0x82
        };
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