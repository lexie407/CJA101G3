package com.toiukha.forum.util;

import com.toiukha.forum.article.dto.ArticleForm;
import com.toiukha.forum.article.entity.Article;

public class ArticleMapper {

    // 將 Entity → Form（顯示用）
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

    // 將 Form → Entity（更新用）
    public static void updateEntity(Article entity, ArticleForm form) {
        if (entity == null || form == null) return;

        entity.setArtTitle(form.getArtTitle());
        entity.setArtCon(form.getArtCon());
        entity.setArtCat(form.getArtCat());
        entity.setArtSta(form.getArtSta());
        // artId 和 artHol 通常不會更新
    }
}
