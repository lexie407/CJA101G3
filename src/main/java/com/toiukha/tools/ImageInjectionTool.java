package com.toiukha.tools;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.*;
import java.util.Iterator;

/**
 * 活動圖片注入工具（含圖片壓縮功能）
 * 用途：將 uploads/actimg 資料夾中的圖片壓縮後寫入資料庫
 * 功能：自動壓縮圖片到 800x600，品質 0.8，確保檔案小於 5MB
 * 使用方式：直接在 IDE 中執行此 Java 程式
 */
public class ImageInjectionTool {
    
    // 圖片壓縮設定
    private static final int MAX_WIDTH = 800;
    private static final int MAX_HEIGHT = 600;
    private static final float COMPRESSION_QUALITY = 0.8f;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public static void main(String argv[]) {
        Connection con = null;
        PreparedStatement pstmt = null;
        
        // 資料庫連線設定
        String url = "jdbc:mysql://localhost:3306/toiukha?serverTimezone=Asia/Taipei";
        String userid = "root";
        String passwd = "123456";
        
        // 圖片資料夾路徑
        String photos = "src/main/resources/static/DB_photos2"; // 圖片目錄
        
        // SQL 更新語句
        String update = "UPDATE groupactivity SET ACTIMG = ? WHERE ACTID = ?";

        try {
            // 建立資料庫連線
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(update);
            
            // 取得圖片資料夾
            File photoDir = new File(photos);
            if (!photoDir.exists()) {
                System.out.println("錯誤：圖片資料夾不存在 - " + photoDir.getAbsolutePath());
                return;
            }
            
            File[] photoFiles = photoDir.listFiles();
            if (photoFiles == null || photoFiles.length == 0) {
                System.out.println("錯誤：圖片資料夾為空 - " + photoDir.getAbsolutePath());
                return;
            }
            
            int successCount = 0;
            int errorCount = 0;
            
            System.out.println("開始注入圖片到資料庫（含壓縮處理）...");
            System.out.println("圖片資料夾：" + photoDir.getAbsolutePath());
            System.out.println("壓縮設定：" + MAX_WIDTH + "x" + MAX_HEIGHT + "，品質：" + (COMPRESSION_QUALITY * 100) + "%");
            System.out.println("檔案大小限制：" + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
            System.out.println("找到 " + photoFiles.length + " 個檔案");
            System.out.println("========================================");
            
            for (File f : photoFiles) {
                try {
                    // 檢查是否為圖片檔案
                    if (!isImageFile(f)) {
                        System.out.println("跳過非圖片檔案：" + f.getName());
                        continue;
                    }
                    
                    // 從檔名解析活動編號
                    Integer actId = extractActIdFromFileName(f.getName());
                    if (actId == null) {
                        System.out.println("無法解析活動編號：" + f.getName());
                        errorCount++;
                        continue;
                    }
                    
                    // 檢查活動是否存在
                    if (!actExists(con, actId)) {
                        System.out.println("活動不存在，ID：" + actId + " (" + f.getName() + ")");
                        errorCount++;
                        continue;
                    }
                    
                    System.out.println("處理活動 " + actId + " 的圖片：" + f.getName());
                    
                    // 顯示原始圖片資訊
                    long originalSize = f.length();
                    System.out.println("  原始檔案大小：" + formatFileSize(originalSize));
                    
                    // 壓縮圖片
                    byte[] compressedImageData = compressImage(f);
                    
                    if (compressedImageData == null) {
                        System.out.println("  ✗ 圖片壓縮失敗");
                        errorCount++;
                        continue;
                    }
                    
                    // 顯示壓縮後資訊
                    System.out.println("  壓縮後大小：" + formatFileSize(compressedImageData.length));
                    System.out.println("  壓縮率：" + String.format("%.1f%%", 
                        (1.0 - (double)compressedImageData.length / originalSize) * 100));
                    
                    // 檢查檔案大小
                    if (compressedImageData.length > MAX_FILE_SIZE) {
                        System.out.println("  ✗ 壓縮後檔案仍超過大小限制");
                        errorCount++;
                        continue;
                    }
                    
                    // 更新資料庫
                    pstmt.setBytes(1, compressedImageData);
                    pstmt.setInt(2, actId);
                    
                    int result = pstmt.executeUpdate();
                    if (result > 0) {
                        System.out.println("  ✓ 活動 " + actId + " 圖片更新成功");
                        successCount++;
                    } else {
                        System.out.println("  ✗ 活動 " + actId + " 資料庫更新失敗");
                        errorCount++;
                    }
                    
                } catch (Exception e) {
                    System.out.println("  ✗ 處理檔案失敗：" + f.getName() + " - " + e.getMessage());
                    errorCount++;
                }
                System.out.println("----------------------------------------");
            }
            
            System.out.println("圖片注入完成！");
            System.out.println("成功：" + successCount + " 個");
            System.out.println("失敗：" + errorCount + " 個");
            System.out.println("總計：" + photoFiles.length + " 個檔案");
            
        } catch (Exception e) {
            System.out.println("程式執行錯誤：" + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 壓縮圖片
     * 自動縮放到指定尺寸並壓縮品質
     */
    private static byte[] compressImage(File imageFile) {
        try {
            // 讀取原始圖片
            BufferedImage originalImage = ImageIO.read(imageFile);
            if (originalImage == null) {
                System.out.println("  無法讀取圖片格式");
                return null;
            }
            
            // 計算縮放尺寸
            Dimension targetSize = calculateTargetSize(
                originalImage.getWidth(), 
                originalImage.getHeight()
            );
            
            System.out.println("  原始尺寸：" + originalImage.getWidth() + "x" + originalImage.getHeight());
            System.out.println("  目標尺寸：" + targetSize.width + "x" + targetSize.height);
            
            // 建立縮放後的圖片
            BufferedImage resizedImage = new BufferedImage(
                targetSize.width, 
                targetSize.height, 
                BufferedImage.TYPE_INT_RGB
            );
            
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.drawImage(originalImage, 0, 0, targetSize.width, targetSize.height, null);
            g2d.dispose();
            
            // 壓縮圖片為 JPEG 格式
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // 取得 JPEG 寫入器
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) {
                System.out.println("  找不到 JPEG 寫入器");
                return null;
            }
            
            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            
            // 設定壓縮品質
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(COMPRESSION_QUALITY);
            
            // 寫入壓縮圖片
            ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
            writer.setOutput(ios);
            writer.write(null, new javax.imageio.IIOImage(resizedImage, null, null), param);
            
            // 清理資源
            writer.dispose();
            ios.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            System.out.println("  圖片壓縮錯誤：" + e.getMessage());
            return null;
        }
    }
    
    /**
     * 計算目標尺寸（保持比例）
     */
    private static Dimension calculateTargetSize(int originalWidth, int originalHeight) {
        double aspectRatio = (double) originalWidth / originalHeight;
        
        int targetWidth = MAX_WIDTH;
        int targetHeight = MAX_HEIGHT;
        
        // 根據長寬比調整尺寸
        if (aspectRatio > (double) MAX_WIDTH / MAX_HEIGHT) {
            // 寬度為限制因子
            targetHeight = (int) (MAX_WIDTH / aspectRatio);
        } else {
            // 高度為限制因子
            targetWidth = (int) (MAX_HEIGHT * aspectRatio);
        }
        
        return new Dimension(targetWidth, targetHeight);
    }
    
    /**
     * 格式化檔案大小顯示
     */
    private static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
    
    /**
     * 檢查是否為圖片檔案
     */
    private static boolean isImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || 
               fileName.endsWith(".jpeg") || 
               fileName.endsWith(".png") || 
               fileName.endsWith(".gif") || 
               fileName.endsWith(".bmp");
    }
    
    /**
     * 從檔名解析活動編號
     * 支援格式：1.jpg, act1.png, activity_1.jpeg 等
     */
    private static Integer extractActIdFromFileName(String fileName) {
        try {
            // 移除副檔名
            String nameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
            
            // 嘗試直接解析數字
            if (nameWithoutExtension.matches("\\d+")) {
                return Integer.parseInt(nameWithoutExtension);
            }
            
            // 嘗試解析包含文字的檔名
            String[] patterns = {
                "act(\\d+)",           // act1, act2, ...
                "activity[_-]?(\\d+)", // activity1, activity_1, activity-1
                "img(\\d+)",           // img1, img2, ...
                "image[_-]?(\\d+)"     // image1, image_1, image-1
            };
            
            for (String pattern : patterns) {
                if (nameWithoutExtension.toLowerCase().matches(".*" + pattern + ".*")) {
                    String number = nameWithoutExtension.toLowerCase().replaceAll(".*" + pattern + ".*", "$1");
                    return Integer.parseInt(number);
                }
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 檢查活動是否存在
     */
    private static boolean actExists(Connection con, int actId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM groupactivity WHERE ACTID = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, actId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
} 