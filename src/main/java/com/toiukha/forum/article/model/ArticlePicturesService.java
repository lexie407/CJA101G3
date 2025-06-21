package com.toiukha.forum.article.model;

import com.toiukha.forum.article.entity.ArticlePictures;

import java.util.List;
import java.util.Optional;

public interface ArticlePicturesService {
    ArticlePictures saveArticlePicture(ArticlePictures pic);

    Optional<ArticlePictures> getArticlePicture(Integer id);

    void bindPicturesToArticle(List<Integer> picIds, Integer artId);

    Optional<ArticlePictures> findById(Integer id);
}
