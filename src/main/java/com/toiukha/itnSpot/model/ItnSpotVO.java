package com.toiukha.itnSpot.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity(name = "ItnSpotManagement")
@Table(name = "itnspot")
public class ItnSpotVO implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@EmbeddedId
	private ItnSpotId id;
	@Column(name = "SEQ", nullable = false)
	private  Integer seq;
	
	public ItnSpotVO() {
		super();
	}
	
	public ItnSpotVO(ItnSpotId id, Integer seq) {
		super();
		this.id = id;
		this.seq = seq;
	}



	public ItnSpotId getId() {
		return id;
	}

	public void setId(ItnSpotId id) {
		this.id = id;
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, seq);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ItnSpotVO other = (ItnSpotVO) obj;
		return Objects.equals(id, other.id) && Objects.equals(seq, other.seq);
	}
	
	
	
}
