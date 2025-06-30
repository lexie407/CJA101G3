package com.toiukha.managefunction.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.toiukha.administrantauth.model.AdministrantAuthVO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "managefunction")
public class ManageFunctionVO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "MANAGEFUNCID", updatable = false)
	private Integer manageFuncId;

	@Column(name = "MANAGEFUNCNAME", length = 50)
	@NotEmpty(message = "功能名稱：請勿空白")
	@Size(max = 50, message = "功能名稱最長 50 個字元")
	private String manageFuncName;

	@OneToMany(mappedBy = "manageFunction", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<AdministrantAuthVO> auths = new HashSet<>();

	public ManageFunctionVO() {
		super();
	}

	public Integer getManageFuncId() {
		return manageFuncId;
	}

	public void setManageFuncId(Integer manageFuncId) {
		this.manageFuncId = manageFuncId;
	}

	public String getManageFuncName() {
		return manageFuncName;
	}

	public void setManageFuncName(String manageFuncName) {
		this.manageFuncName = manageFuncName;
	}

	public Set<AdministrantAuthVO> getAuths() {
		return auths;
	}

	public void setAuths(Set<AdministrantAuthVO> auths) {
		this.auths = auths;
	}
	
	

}
