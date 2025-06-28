package com.toiukha.forum.article.model;

public enum ArticleStatus {
    PUBLISHED((byte) 1),
    UNPUBLISHED((byte) 2),;

    private final byte  artSta;

    ArticleStatus(byte artSta) {
        this.artSta = artSta;
    }

    public Byte getValue() {
        return artSta;
    }
}
