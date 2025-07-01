package com.toiukha.administrantauth.model;

import java.io.Serializable;

import com.toiukha.administrant.model.AdministrantVO;
import com.toiukha.managefunction.model.ManageFunctionVO;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.IdClass;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "administrantauth")
@IdClass(AdministrantAuthId.class)
public class AdministrantAuthVO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ADMINID")
    private AdministrantVO administrant;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MANAGEFUNCID")
    private ManageFunctionVO manageFunction;

	public AdministrantAuthVO() {
		super();
	}

	public AdministrantVO getAdministrant() {
		return administrant;
	}

	public void setAdministrant(AdministrantVO administrant) {
		this.administrant = administrant;
	}

	public ManageFunctionVO getManageFunction() {
		return manageFunction;
	}

	public void setManageFunction(ManageFunctionVO manageFunction) {
		this.manageFunction = manageFunction;
	}

    

}