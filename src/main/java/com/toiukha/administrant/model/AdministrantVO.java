package com.toiukha.administrant.model;

import java.io.Serializable;
import java.sql.Timestamp;
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
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "administrant")
public class AdministrantVO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ADMINID", updatable = false)
	private Integer adminId;

	@Column(name = "ADMINACC", updatable = false)
	@NotEmpty(message = "管理員帳號: 請勿空白")
	@Pattern(regexp = "^[a-zA-Z0-9_]{6,10}$", message = "管理員帳號: 只能是英文字母、數字和_，長度必需在6到10之間")
	private String adminAcc;

	@Column(name = "ADMINPWD")
	@NotEmpty(message = "管理員密碼: 請勿空白")
	@Pattern(regexp = "^[a-zA-Z0-9_]{4,10}$", message = "管理員密碼: 只能是英文字母、數字和_，長度必需在4到10之間")
	private String adminPwd;

	@Column(name = "ADMINNAME")
	@NotEmpty(message = "管理員姓名: 請勿空白")
	@Pattern(regexp = "^[\u4e00-\u9fa5a-zA-Z0-9_]{2,20}$", message = "管理員姓名: 只能是中、英文字母、數字和_，長度必需在2到20之間")
	private String adminName;


	@Column(name = "ADMINCREATEDAT", updatable = false)
	private Timestamp adminCreatedAt;
	
	@Column(name = "ADMINSTATUS")
	private Byte adminStatus;

	@Column(name = "ADMINUPDATEDAT")
	private Timestamp adminUpdatedAt;
	
	@OneToMany(
	        mappedBy = "administrant",
	        cascade = CascadeType.ALL,
	        orphanRemoval = true
	    )
	    private Set<AdministrantAuthVO> auths = new HashSet<>();

	public AdministrantVO() {
		super();
	}

	public Integer getAdminId() {
		return adminId;
	}

	public void setAdminId(Integer adminId) {
		this.adminId = adminId;
	}

	public String getAdminAcc() {
		return adminAcc;
	}

	public void setAdminAcc(String adminAcc) {
		this.adminAcc = adminAcc;
	}

	public String getAdminPwd() {
		return adminPwd;
	}

	public void setAdminPwd(String adminPwd) {
		this.adminPwd = adminPwd;
	}

	public String getAdminName() {
		return adminName;
	}

	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}

	public Timestamp getAdminCreatedAt() {
		return adminCreatedAt;
	}

	public void setAdminCreatedAt(Timestamp adminCreatedAt) {
		this.adminCreatedAt = adminCreatedAt;
	}

	public Byte getAdminStatus() {
		return adminStatus;
	}

	public void setAdminStatus(Byte adminStatus) {
		this.adminStatus = adminStatus;
	}

	public Timestamp getAdminUpdatedAt() {
		return adminUpdatedAt;
	}

	public void setAdminUpdatedAt(Timestamp adminUpdatedAt) {
		this.adminUpdatedAt = adminUpdatedAt;
	}

	public Set<AdministrantAuthVO> getAuths() {
		return auths;
	}

	public void setAuths(Set<AdministrantAuthVO> auths) {
		this.auths = auths;
	}

	
	
	
	
	
}
