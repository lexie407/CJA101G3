package com.toiukha.forum.util;

import com.toiukha.forum.article.dto.ArticleSearchCriteria;
import com.toiukha.forum.article.entity.Article;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

// TODO: 理解這是幹嘛的
// 進階查詢用
public class ArticleSpecifications {

    public static Specification<Article> withCriteria(ArticleSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getArtHol() != null) {
                predicates.add(cb.equal(root.get("artHol"), criteria.getArtHol()));
            }

            if (criteria.getArtTitle() != null && !criteria.getArtTitle().isBlank()) {
                predicates.add(cb.like(root.get("artTitle"), "%" + criteria.getArtTitle().trim() + "%"));
            }

            if (criteria.getArtCategory() != null) {
                predicates.add(cb.equal(root.get("artCat"), criteria.getArtCategory()));
            }

            if (criteria.getArtStatus() != null) {
                predicates.add(cb.equal(root.get("artSta"), criteria.getArtStatus()));
            }

            if (criteria.getStartTimeAsTimestamp() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("artCreTime"), criteria.getStartTimeAsTimestamp()));
            }

            if (criteria.getEndTimeAsTimestamp() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("artCreTime"), criteria.getEndTimeAsTimestamp()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
