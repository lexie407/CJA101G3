package com.toiukha.forum.util;

import com.toiukha.forum.article.dto.ArticleDTO;
import com.toiukha.forum.article.dto.ArticleForm;
import com.toiukha.forum.article.entity.Article;

public class ArticleMapper {

    // 將 Entity → Form（後台顯示）
    public static ArticleForm toForm(Article entity) {
        if (entity == null) return null;

        ArticleForm form = new ArticleForm();
        form.setArtId(entity.getArtId());
        form.setArtTitle(entity.getArtTitle());
        form.setArtCon(entity.getArtCon());
        form.setArtCat(entity.getArtCat());
        form.setArtSta(entity.getArtSta());
        form.setArtHol(entity.getArtHol());
        return form;
    }

    // 將 Form → Entity（後台更新用）
    public static void updateEntity(Article entity, ArticleForm form) {
        if (entity == null || form == null) return;

        entity.setArtTitle(form.getArtTitle());
        entity.setArtCon(form.getArtCon());
        entity.setArtCat(form.getArtCat());
        entity.setArtSta(form.getArtSta());
        // artId 和 artHol 通常不會更新
    }

    // 將 Entity → DTO（前台顯示）
    public static ArticleDTO toDTO(Article entity, String mamName) {
        if (entity == null) return null;

        return new ArticleDTO(
                entity.getArtId(),
                entity.getArtCat(),
                entity.getArtSta(),
                mamName,
                entity.getArtLike(),
                entity.getArtTitle(),
                entity.getArtCon(),
                entity.getArtCreTime()
        );

    }
}
