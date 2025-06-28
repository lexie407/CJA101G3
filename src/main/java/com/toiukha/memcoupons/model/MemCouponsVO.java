package com.toiukha.memcoupons.model;

import java.io.Serializable;

import com.toiukha.coupon.model.CouponVO;
import com.toiukha.memcoupons.model.MemCouponsVO.CompositeDetail;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "memcoupons")
@IdClass(CompositeDetail.class)
public class MemCouponsVO implements java.io.Serializable {

	@Id
	@Column(name = "MEMID")
	private Integer memId; // 會員編號

	@Id
	@Column(name = "COUID")
	private Integer couId; // 優惠券活動編號

	@Column(name = "DISCTVAL")
	private Integer disctVal; // 折抵金額

	@Column(name = "COUPSTA")
	private Integer coupSta; // 優惠券狀態
								// 0:可使用, 1:已使用, 2:已過期, 3:作廢
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "COUID", referencedColumnName = "COUID", insertable = false, updatable = false)
	private CouponVO coupon;

//	public MemCoupons() {
//		super();
//	}
//
//	public MemCoupons(Integer memId, Integer could, Integer disctVal, Integer coupSta) {
//		super();
//		this.memId = memId;
//		this.could = could;
//		this.disctVal = disctVal;
//		this.coupSta = coupSta;
//	}

	public Integer getMemId() {
		return memId;
	}

	public void setMemId(Integer memId) {
		this.memId = memId;
	}

	public Integer getCouId() {
		return couId;
	}

	public void setCouId(Integer couId) {
		this.couId = couId;
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
	

	public CouponVO getCoupon() {
		return coupon;
	}

	public void setCoupon(CouponVO coupon) {
		this.coupon = coupon;
	}


	static class CompositeDetail implements Serializable {
		private static final long serialVersionUID = 1L;

		private Integer memId;
		private Integer couId;

		// 一定要有無參數建構子
		public CompositeDetail() {
			super();
		}

		public CompositeDetail(Integer memId, Integer couId) {
			super();
			this.memId = memId;
			this.couId = couId;
		}

		public Integer getMemId() {
			return memId;
		}

		public void setMemId(Integer memId) {
			this.memId = memId;
		}

		public Integer getCouId() {
			return couId;
		}

		public void setCouId(Integer couId) {
			this.couId = couId;
		}

		// 一定要 override 此類別的 hashCode() 與 equals() 方法！
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((couId == null) ? 0 : couId.hashCode());
			result = prime * result + ((memId == null) ? 0 : memId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;

			if (obj != null && getClass() == obj.getClass()) {
				CompositeDetail compositeKey = (CompositeDetail) obj;
				if (memId.equals(compositeKey.memId) && couId.equals(compositeKey.couId)) {
					return true;
				}
			}

			return false;
		}

	}

}