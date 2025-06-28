package com.toiukha.forum.article.model;

public enum ArticleCategory {
    POST((byte) 1),
    OPEN_QUESTION((byte) 2),
    RESOLVED_QUESTION((byte) 3);

    private final byte artCat;

    private ArticleCategory(byte artCat) {
        this.artCat = artCat;
    }

    public Byte getValue() {
        return artCat;
    }
}
