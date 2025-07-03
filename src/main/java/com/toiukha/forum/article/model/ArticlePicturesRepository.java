package com.toiukha.forum.article.model;

import com.toiukha.forum.article.entity.ArticlePictures;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticlePicturesRepository extends JpaRepository<ArticlePictures,Integer> {
    // JpaRepository 已經提供了基本的 CRUD 操作，只需要定義進階操作

    // 根據文章ID查找圖片
     List<ArticlePictures> findByArtId(Integer artId);

    List<ArticlePictures> findAllById(Iterable<Integer> ids);

    List<ArticlePictures> findByArtIdIsNull(); // 找未綁定的圖片

}
