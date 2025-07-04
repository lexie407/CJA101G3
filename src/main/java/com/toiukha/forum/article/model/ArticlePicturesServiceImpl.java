package com.toiukha.forum.article.model;

import com.toiukha.forum.article.entity.ArticlePictures;
import com.toiukha.forum.article.exception.ImageUploadException;
import com.toiukha.forum.util.Debug;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    // 上傳圖片，並返回圖片的ID
    // 上傳失敗時會丟出ImageUploadException
    @Override
    public Integer saveImage(MultipartFile file, Integer artId) {
         if (file.isEmpty()) {
             Debug.errorLog("上傳的檔案為空");
             throw new ImageUploadException("上傳的檔案為空，請選擇有效的圖片");
         }
        try {
            ArticlePictures pic = new ArticlePictures();
            pic.setArtId(artId);
            pic.setPicture(file.getBytes());
            pic.setMimeType(file.getContentType());

            return apRep.save(pic).getPicId();
        } catch (IOException e) {
            Debug.errorLog("圖片上傳失敗：" + e.getMessage());
            e.printStackTrace();

            // 拋出ImageUploadException，讓Controller處理
            throw new ImageUploadException("圖片上傳失敗，請稍後再試");
        }
    }

    // 通常由文章controller呼叫，讓文章的圖片綁定文章id
    // 因為圖片上傳時沒有綁定文章ID，所以需要在這裡進行綁定
    @Override
    public void bindPicturesToArticle (List<Integer> picIds, Integer artId) {
        for (Integer picId : picIds) {
            Optional<ArticlePictures> pic = apRep.findById(picId);
            if (pic.isPresent()) {
                ArticlePictures ap = pic.get();
                ap.setArtId(artId); // 綁定文章ID
                apRep.save(ap); // 更新圖片資料
            }else {
                Debug.errorLog("找不到圖片ID：" + picId);
            }
        }
    }

    public void refreshPictureBindings(Integer artId, List<Integer> newPicIds) {
        // 取得目前這篇文章綁定的所有圖片
        List<ArticlePictures> currentPics = apRep.findByArtId(artId);
        Set<Integer> oldPicIds = currentPics.stream()
                .map(ArticlePictures::getPicId)
                .collect(Collectors.toSet());

        Set<Integer> newSet = new HashSet<>(newPicIds);

        // 需要新增的圖片
        Set<Integer> toAdd = new HashSet<>(newSet);
        toAdd.removeAll(oldPicIds);

        // 需要移除的圖片
        Set<Integer> toRemove = new HashSet<>(oldPicIds);
        toRemove.removeAll(newSet);

        // 綁定新加的
        List<ArticlePictures> addPics = apRep.findAllById(toAdd);
        for (ArticlePictures pic : addPics) {
            if (pic.getArtId() == null) {
                pic.setArtId(artId);
            }
        }

        // 清除不再使用的
        List<ArticlePictures> removePics = apRep.findAllById(toRemove);
        for (ArticlePictures pic : removePics) {
            pic.setArtId(null); // 或刪除也可以
        }

        apRep.saveAll(addPics);
        apRep.saveAll(removePics);
    }


    @Override
    public ArticlePictures getImageById(Integer picId) {
        return apRep.findById(picId).orElse(null);
    }

    @Override
    public List<ArticlePictures> getImagesByArticleId(Integer artId) {
        List<ArticlePictures> pics = apRep.findByArtId(artId);
        return pics;
    }

    @Override
    public void deleteImage(Integer picId) {
        apRep.deleteById(picId);
    }


//    // 保存圖片
//    public ArticlePictures saveArticlePicture(ArticlePictures ap) {
//        // 使用articleRepository保存文章圖片
//        return apRep.save(ap);
//    }
//
//    // 根據圖片ID查詢圖片資料
//    //  Optional可以在找不到圖片時返回某個預設值，一般用來防止 NullPointerException ，但在這裡沒有作用
//    public Optional<ArticlePictures> getArticlePicture(Integer picId) {
//        // 使用articleRepository查詢文章圖片
//        // 這裡返回Optional<ArticlePictures>，如果找不到則返回空值
//        return apRep.findById(picId);
//    }
//
//
//    @Override
//    public Optional<ArticlePictures> findById(Integer id) {
//        // 根據ID查找圖片資料
//        return apRep.findById(id);
//    }

    // 上傳圖片，並返回圖片的URL




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
//            // 因為會噴java.io.IOException，此方法廢棄(找老師求助QQ)
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



