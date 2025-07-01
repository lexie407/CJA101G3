package com.toiukha.itnSpot.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ItnSpotId implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "ITNID")
	private Integer itnId;   // 對應行程編號
	
	@Column(name = "SPOTID")
    private Integer spotId;  // 對應景點編號
    
	public ItnSpotId() {
		super();
	}
	
	
	public ItnSpotId(Integer itnId, Integer spotId) {
		super();
		this.itnId = itnId;
		this.spotId = spotId;
	}



	public Integer getItnId() {
		return itnId;
	}
	public void setItnId(Integer itnId) {
		this.itnId = itnId;
	}
	public Integer getSpotId() {
		return spotId;
	}
	public void setSpotId(Integer spotId) {
		this.spotId = spotId;
	}
	@Override
	public int hashCode() {
		return Objects.hash(itnId, spotId);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ItnSpotId other = (ItnSpotId) obj;
		return Objects.equals(itnId, other.itnId) && Objects.equals(spotId, other.spotId);
	}
	
}
