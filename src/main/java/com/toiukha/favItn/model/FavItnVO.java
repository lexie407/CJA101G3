package com.toiukha.favItn.model;

import java.io.Serializable;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "favitn")
public class FavItnVO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@EmbeddedId // 標記這個屬性為嵌入式主鍵
    private FavItnId id; // 將 FavItnId 物件作為主鍵屬性
	
	public FavItnVO(FavItnId id) {
		super();
		this.id = id;
	}
	
	

	public FavItnVO() {
		super();
	}



	public FavItnId getId() {
		return id;
	}

	public void setId(FavItnId id) {
		this.id = id;
	}

    public Integer getFavItnId() {
        return id != null ? id.getFavItnId() : null;
    }

    public Integer getMemId() {
        return id != null ? id.getMemId() : null;
    }

}
