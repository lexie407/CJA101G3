package com.toiukha.favItn.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class FavItnId implements Serializable  {

	private static final long serialVersionUID = 1L;

    // 不再需要 @Column 註解，因為它們會在主實體中定義或自動對應
	@Column(name = "FAVITNID")
    private Integer favItnId; // 對應行程編號
	@Column(name = "MEMID")
    private Integer memId;    // 對應收藏的會員
	public Integer getFavItnId() {
		return favItnId;
	}
	public void setFavItnId(Integer favItnId) {
		this.favItnId = favItnId;
	}
	public Integer getMemId() {
		return memId;
	}
	public void setMemId(Integer memId) {
		this.memId = memId;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(favItnId, memId);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FavItnId other = (FavItnId) obj;
		return Objects.equals(favItnId, other.favItnId) && Objects.equals(memId, other.memId);
	}

	
	
}
