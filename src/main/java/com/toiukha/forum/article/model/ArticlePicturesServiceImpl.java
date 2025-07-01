package com.toiukha.forum.article.model;

import com.toiukha.forum.article.entity.ArticlePictures;
import com.toiukha.forum.util.Debug;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// 與@Component相似，不過@Service更適合用於業務邏輯層
// 這裡負責圖片上傳的邏輯，除了CRUD之外也包含圖片路徑等等
@Service("articlePicturesService")
public class ArticlePicturesServiceImpl implements ArticlePicturesService {

    private final ArticlePicturesRepository apRep;

    // 建構子注入
     @Autowired
    public ArticlePicturesServiceImpl(ArticlePicturesRepository apRep) {
        this.apRep = apRep;
    }

    // 保存圖片
    public ArticlePictures saveArticlePicture(ArticlePictures ap) {
        // 使用articleRepository保存文章圖片
        return apRep.save(ap);
    }

    // 根據圖片ID查詢圖片資料
    //  Optional可以在找不到圖片時返回某個預設值，一般用來防止 NullPointerException ，但在這裡沒有作用
    public Optional<ArticlePictures> getArticlePicture(Integer picId) {
        // 使用articleRepository查詢文章圖片
        // 這裡返回Optional<ArticlePictures>，如果找不到則返回空值
        return apRep.findById(picId);
    }


    @Override
    public Optional<ArticlePictures> findById(Integer id) {
        // 根據ID查找圖片資料
        return apRep.findById(id);
    }

    // 上傳圖片，並返回圖片的URL


    @Override
    public void bindPicturesToArticle(List<Integer> picIds, Integer artId) {
        // 將圖片ID綁定到文章ID
        for (Integer picId : picIds) {
            Optional<ArticlePictures> optionalPic = apRep.findById(picId);
            if (optionalPic.isPresent()) {
                ArticlePictures pic = optionalPic.get();
                pic.setArtId(artId);  // 設定文章ID
                apRep.save(pic);  // 保存更新後的圖片資料
            } else {
                Debug.log("找不到圖片ID: " + picId);
            }
        }
    }



    //以下是舊版上傳圖片(使用I/O存圖片資料夾)，已經廢棄

//    // 更改Spring Boot 預設的上傳檔案大小限制(1MB)
//    public Map<String,String> uploadArticlePicture(MultipartFile file){
//        // 取得專案根目錄
////        String projectRoot = Paths.get("").toAbsolutePath().toString();
//        System.out.println("目前專案根目錄：" + uploadFolder);
//
//        // 將 folder 設定為「相對專案根目錄的資料夾」
//        File dir = new File(System.getProperty("user.dir"), uploadFolder);
////        File dir = new File(uploadFolder);
//        if (!dir.exists()) {
//            Debug.log("討論區圖片上傳資料夾不存在，建立中……");
//            dir.mkdirs();
//        }
//
//        // 取得原始檔名，產生更安全的檔名，洗掉emoji或特殊字元
//        String originalFilename = file.getOriginalFilename();
//        String safeName = getBaseFileName(originalFilename);
//        String extension = getFileExtension(originalFilename);
//        String finalFilename = safeName + extension;
//
//        File dest = new File(dir , finalFilename);  // 檔案目的地
//        System.out.println("圖片儲存目標：" + dest.getAbsolutePath());
//
//
//        // 放resource可以不用手動關InputStream
//        try(InputStream is = file.getInputStream()) {
//            // FIXME:因為會噴java.io.IOException，此方法廢棄(找老師求助QQ)
//            // java.io.FileNotFoundException: C:\Users\TMP-214\AppData\Local\Temp\tomcat.8080.1819086076714018823\work\Tomcat\localhost\ROOT\forum-uploads\article-images\測試_1750078345493.png (系統找不到指定的路徑。)
//            //	at org.apache.catalina.core.ApplicationPart.write(ApplicationPart.java:119)
////            file.transferTo(dest); // 存檔
//
//
//            //nio方法，把資料從來源「複製」到目的地，如果目標檔案已經存在，就覆蓋掉
//            Files.copy(is, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
//
////            is.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new ImageException("檔案寫入失敗！",e);  //會丟給Controller的ExceptionHandler接手
//        }finally {
//
//        }
//        String dbPath = urlPrefix + finalFilename;
//        // 圖片寫入成功之後，建立圖片資料物件
//        ArticlePictures ap = new ArticlePictures();
//        ap.setPicture(dbPath);
//        apRep.save(ap);
//
//        return Map.of("url", "/forum-uploads/article-images/" + finalFilename);
//    }

//    public static String getFileExtension(String filename) {
//        if (filename == null || !filename.contains(".")) {
//            return ""; // 沒有副檔名
//        }
//        return filename.substring(filename.lastIndexOf("."));
//    }

//    public static String getBaseFileName(String filename) {
//        if (filename == null) {
//            throw new IllegalArgumentException("無效的檔案名稱");
//        }
//        // 若檔案包含副檔名，去除副檔名
//        if (filename.contains(".")) {
//            filename =  filename.substring(0, filename.lastIndexOf("."));
//        }
//        // 檔名只保留常用中文字、英文大小寫、數字、-、_
//        filename  = filename.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9-_]", "");
//        // 若處理完變空，就補預設名稱
//        if (filename.isBlank()) {
//            filename = "image";
//        }
//        //檔名後面加現在時間timestamp，增加唯一性，減少重複覆蓋機率
//        String timestamp = String.valueOf(System.currentTimeMillis());
//        filename += "_" + timestamp;
//
//        return filename;
//    }


}


