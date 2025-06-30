package com.toiukha.administrantauth.model;

import java.io.Serializable;
import java.util.Objects;

public class AdministrantAuthId implements Serializable {
	
	private Integer administrant;
	private Integer manageFunction;
	
	public AdministrantAuthId() {
		super();
	}

	public AdministrantAuthId(Integer administrant, Integer manageFunction) {
		super();
		this.administrant = administrant;
		this.manageFunction = manageFunction;
	}

	public Integer getAdministrant() {
		return administrant;
	}

	public void setAdministrant(Integer administrant) {
		this.administrant = administrant;
	}

	public Integer getManageFunction() {
		return manageFunction;
	}

	public void setManageFunction(Integer manageFunction) {
		this.manageFunction = manageFunction;
	}

	@Override
	public int hashCode() {
		return Objects.hash(administrant, manageFunction);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AdministrantAuthId other = (AdministrantAuthId) obj;
		return Objects.equals(administrant, other.administrant) && Objects.equals(manageFunction, other.manageFunction);
	}
	
	

}
