package com.toiukha.forum.article.model;

import com.toiukha.forum.article.entity.ArticlePictures;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ArticlePicturesService {
    Integer saveImage(MultipartFile file, Integer artId);
    ArticlePictures getImageById(Integer picId);
    List<ArticlePictures> getImagesByArticleId(Integer artId);
    public void deleteImage(Integer picId);
    // 通常由文章controller呼叫，讓文章的圖片綁定文章id
    public void bindPicturesToArticle (List<Integer> picIds, Integer artId) ;
    }
