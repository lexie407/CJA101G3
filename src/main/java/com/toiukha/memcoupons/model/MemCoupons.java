package com.toiukha.memcoupons.model;

public class MemCoupons implements java.io.Serializable {
	private Integer memId; // 會員編號
	private Integer could; // 優惠券活動編號
	private Integer disctVal; // 折抵金額
	private Integer coupSta; // 優惠券狀態
								// 0:可使用, 1:已使用, 2:已過期, 3:作廢

	public Integer getMemId() {
		return memId;
	}

	public void setMemId(Integer memId) {
		this.memId = memId;
	}

	public Integer getCould() {
		return could;
	}

	public void setCould(Integer could) {
		this.could = could;
	}

	public Integer getDisctVal() {
		return disctVal;
	}

	public void setDisctVal(Integer disctVal) {
		this.disctVal = disctVal;
	}

	public Integer getCoupSta() {
		return coupSta;
	}

	public void setCoupSta(Integer coupSta) {
		this.coupSta = coupSta;
	}

}
