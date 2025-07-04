package com.toiukha.forum.article.controller;

import com.toiukha.forum.article.entity.ArticlePictures;
import com.toiukha.forum.article.exception.ImageUploadException;
import com.toiukha.forum.article.model.ArticlePicturesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/forum/artImage")
public class ArticlePicturesController {

    @Autowired
    private ArticlePicturesService apService;

    // 上傳圖片，並回傳圖片ID
    @PostMapping("/upload")
    @ResponseBody
    public Map<String, Object> uploadImage(@RequestParam("image") MultipartFile file) {
        Map<String, Object> response = new HashMap<>(); // 用來回傳結果的Map
        Integer picId = apService.saveImage(file, null); // 有錯會自動拋例外

        response.put("success", true);
        response.put("message", "圖片上傳成功");
        response.put("id", picId);
        return response;
    }

    // 根據圖片ID取得圖片資料
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Integer id) {
        ArticlePictures pic = apService.getImageById(id);

        if (pic == null || pic.getPicture() == null) {
            return ResponseEntity.notFound().build(); // 找不到圖片
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(pic.getMimeType()))
                .body(pic.getPicture()); // 回傳圖片原始位元資料
    }

//    @PostMapping("/uploadBase64")
//    public ResponseEntity<Integer> uploadBase64Image(@RequestBody Map<String, String> body) {
//        String base64 = body.get("imageData");
//        if (base64 == null || !base64.startsWith("data:image/")) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        ArticlePictures pic = new ArticlePictures();
//        pic.setPicture(base64);
//
//        ArticlePictures savedPic = apService.saveArticlePicture(pic);
//
//        return ResponseEntity.ok(pic.getPicId());
//    }
//
//    // 回傳base64
//    @GetMapping("/showBase64/{id}")
//    public ResponseEntity<String> getBase64Image(@PathVariable Integer id) {
//        Optional<ArticlePictures> opt = apService.findById(id);
//        if (opt.isEmpty()) return ResponseEntity.notFound().build();
//
//        return ResponseEntity.ok(opt.get().getPicture());
//    }

    /*

    // 上傳圖片並做資料驗證
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestBody Map<String, String> payload) {
        try {
            String base64Data = payload.get("imageData");
            String mimeType = payload.get("mimeType");

            if (base64Data == null || mimeType == null)
                return ResponseEntity.badRequest().body("圖片上傳缺少必要資料");

            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            System.out.println("收到圖片，長度: " + imageBytes.length + " bytes, mimeType: " + mimeType);

            ArticlePictures pic = new ArticlePictures();
            pic.setMimeType(mimeType);
            pic.setPicture(imageBytes);

            ArticlePictures savedPic = apService.saveArticlePicture(pic);

            if (savedPic.getPicId() == null) {
                System.err.println("圖片儲存失敗，未取得圖片ID");
                return ResponseEntity.status(500).body("圖片儲存失敗，未取得圖片ID");
            }

            return ResponseEntity.ok("/forum/artImage/" + savedPic.getPicId());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("圖片上傳發生例外：" + ex.getMessage());
        }
    }

    // 根據圖片ID取得圖片資料
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getArticleImage(@PathVariable Integer id) {
        Optional<ArticlePictures> optionalPic = apService.getArticlePicture(id);

        if (optionalPic.isPresent()) {
            ArticlePictures pic = optionalPic.get();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(pic.getMimeType()));

            return new ResponseEntity<>(pic.getPicture(), headers, HttpStatus.OK);
        } else {
            System.err.println("找不到圖片ID: " + id); // 增加404 log
            return ResponseEntity.notFound().build();
        }
    }

     */


    /************************* 錯誤處理 *************************/

    // 輸入格式的錯誤處理，由uploadBase64Image丟出IllegalArgumentException時被呼叫
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // 圖片上傳失敗的錯誤處理，由uploadImage丟出ImageUploadException時被呼叫
    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<?> handleImageUpload(ImageUploadException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", e.getMessage()
        ));
    }


}