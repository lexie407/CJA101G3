package com.toiukha.forum.point.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.sql.Date;

@Entity
@Table(name = "pointchanges")
public class PointChanges implements Serializable{

	//	用來把序列化後的一連串序列拼回原本類別的識別ID
	private static final long serialVersionUID = -1020277113653549271L;  //序列化判斷用

	//  對應表格的欄位
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CHAID", nullable = false)
	private Integer chaId;

	@Column(name = "CHATIME", nullable = false)
	private Date chaTime;

	@Column(name = "MEMID", nullable = false)
	private Integer memId;

	@Column(name = "AMOUNT")
	private Integer amount;

	@Column(name = "CHADESCRIPTION")
	private String chaDescription;

	@Column(name = "CHACAT")
	private Byte chaCat;	// Byte型別對應到TintInt

	public PointChanges(Integer chaId, Date chaTime, Integer memId, Integer amount, String chaDescription,
						Byte chaCat) {
		super();
		this.chaId = chaId;
		this.chaTime = chaTime;
		this.memId = memId;
		this.amount = amount;
		this.chaDescription = chaDescription;
		this.chaCat = chaCat;
	}

	// 無參數建構子，給框架用
	public PointChanges() {
		super();
	}

	public Integer getChaId() {
		return chaId;
	}

	public void setChaId(Integer chaId) {
		this.chaId = chaId;
	}

	public Date getChaTime() {
		return chaTime;
	}

	public void setChaTime(Date chaTime) {
		this.chaTime = chaTime;
	}

	public Integer getMemId() {
		return memId;
	}

	public void setMemId(Integer memId) {
		this.memId = memId;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public String getChaDescription() {
		return chaDescription;
	}

	public void setChaDescription(String chaDescription) {
		this.chaDescription = chaDescription;
	}

	public Byte getChaCat() {
		return chaCat;
	}

	public void setChaCat(Byte chaCat) {
		this.chaCat = chaCat;
	}

	// 想覆寫toString方法，讓它不是回傳物件門牌，而是照我想要的方式
//	@Override
	public String toString() {
		String string = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
		string += "  [編號: " + chaId + ",異動時間: " + chaTime  + ",會員編號: " + memId + ",異動點數: " + amount + ",敘述: " + chaDescription +"]";
		return string;
	}

}
