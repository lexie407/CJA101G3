package com.toiukha.forum.article.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.sql.Date;

// 對應到資料表，代表是資料表中的某個欄位
@Entity
@Table(name = "article")
public class Article implements Serializable{
	//	用來把序列化後的一連串序列拼回原本類別的識別ID
	private static final long serialVersionUID = -1848561202771945566L; //序列化判斷用

	//  對應表格的欄位
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ARTID", updatable = false)
	private Integer artId;	//文章編號
	
	@Column(name = "ARTCAT")
	private Byte artCat;		//文章類別
	
	@Column(name = "ARTSTA")
	private Byte artSta;		//文章狀態
	
	@Column(name = "ARTHOL")
	private Integer artHol;		//會員編號
	
	@Column(name = "ARTLIKE")
	private Integer artLike;	//文章按讚數
	
	@Column(name = "ARTTITLE")
	private String artTitle;	//文章標題
	
	//可能試著改用圖片轉base64的做法，或是用CKeditor
	@Column(name = "ARTCON")
	private String artCon;		//文章內容
	
	@Column(name = "ARTCRETIME")
	private Date artCreTime;	//文章建立時間

	//只有部分資料填入，給Insert語法用(因為部分欄位由資料庫生成)
	public Article(Byte artCat, Byte artSta, Integer artHol
			, String artTitle, String artCon){
		super();
		this.artCat = artCat;
		this.artSta = artSta;
		this.artHol = artHol;
		this.artLike = 0;
		this.artTitle = artTitle;
		this.artCon = artCon;
	}

	public Article(Integer artId, Byte artCat, Byte artSta, Integer artHol, Integer artLike
			, String artTitle, String artCon, Date artCreTime) {
		super();
		this.artId = artId;
		this.artCat = artCat;
		this.artSta = artSta;
		this.artHol = artHol;
		this.artLike = artLike;
		this.artTitle = artTitle;
		this.artCon = artCon;
		this.artCreTime = artCreTime;
	}

	public Article() {
		super();
	}

	public Integer getArtId() {
		return artId;
	}

	public void setArtId(Integer artId) {
		this.artId = artId;
	}

	public Byte getArtCat() {
		return artCat;
	}

	public void setArtCat(Byte artCat) {
		this.artCat = artCat;
	}

	public Byte getArtSta() {
		return artSta;
	}

	public void setArtSta(Byte artSta) {
		this.artSta = artSta;
	}

	public Integer getArtHol() {
		return artHol;
	}

	public void setArtHol(Integer artHol) {
		this.artHol = artHol;
	}

	public Integer getArtLike() {
		return artLike;
	}

	public void setArtLike(Integer artLike) {
		this.artLike = artLike;
	}

	public String getArtTitle() {
		return artTitle;
	}

	public void setArtTitle(String artTitle) {
		this.artTitle = artTitle;
	}

	public String getArtCon() {
		return artCon;
	}

	public void setArtCon(String artCon) {
		this.artCon = artCon;
	}

	public Date getArtCreTime() {
		return artCreTime;
	}

	public void setArtCreTime(Date artCreTime) {
		this.artCreTime = artCreTime;
	}

	// 覆寫toString方法，讓它不是回傳物件門牌，而是照我想要的方式
	@Override
	public String  toString() {
		String string = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
		string += "[<文章編號> = " + artId + ", <文章類別> = " + artCat + ", <文章狀態> " + artSta + ", <會員編號> " + artHol + ", <文章按讚數> " + artLike + ", <文章標題> " + artTitle + ", <文章內容> = " + artCon + ", <文章建立時間> = " + artCreTime + "]";
		return string;
	}
}
