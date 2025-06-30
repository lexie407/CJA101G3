package com.toiukha.administrant.model;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 表單 DTO：用於「新增／編輯管理員」的表單綁定
 */
public class AdministrantDTO {

	/** 管理員 ID（隱藏欄位） */
	private Integer adminId;

	@NotEmpty(message = "管理員帳號：請勿空白")
	@Pattern(regexp = "^[a-zA-Z0-9_]{6,10}$", message = "只能是英文字母、數字和_，長度6~10")
	private String adminAcc;

	@NotEmpty(message = "管理員密碼：請勿空白")
	@Pattern(regexp = "^[a-zA-Z0-9_]{4,10}$", message = "只能是英文字母、數字和_，長度4~10")
	private String adminPwd;

	@NotEmpty(message = "管理員姓名：請勿空白")
	@Size(max = 20, message = "管理員姓名最長20個字元")
	private String adminName;

	/** 0 = 啟用, 1 = 停權 */
	private byte adminStatus;

	/** 多選：接收功能 ID 清單 */
	private List<Integer> manageFuncIds;

	// ===== getters / setters =====

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

	public byte getAdminStatus() {
		return adminStatus;
	}

	public void setAdminStatus(byte adminStatus) {
		this.adminStatus = adminStatus;
	}

	public List<Integer> getManageFuncIds() {
		return manageFuncIds;
	}

	public void setManageFuncIds(List<Integer> manageFuncIds) {
		this.manageFuncIds = manageFuncIds;
	}
}
