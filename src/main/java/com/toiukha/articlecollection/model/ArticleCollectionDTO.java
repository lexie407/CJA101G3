package com.toiukha.articlecollection.model;

public class ArticleCollectionDTO {

    private Integer artId;
    private String articleTitle;
    private Integer artHol;
    
    public ArticleCollectionDTO() {
    }

    public ArticleCollectionDTO(Integer artId, String articleTitle, Integer artHol) {
		super();
		this.artId = artId;
		this.articleTitle = articleTitle;
		this.artHol = artHol;
	}

	// Getters and Setters
    public Integer getArtId() {
        return artId;
    }

    public void setArtId(Integer artId) {
        this.artId = artId;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    public Integer getArtHol() {
        return artHol;
    }

    public void setArtHol(Integer artHol) {
        this.artHol = artHol;
    }
}
