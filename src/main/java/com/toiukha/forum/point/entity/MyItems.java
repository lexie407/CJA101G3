package com.toiukha.forum.point.entity;

import java.io.Serializable;
import java.sql.Date;

public class MyItems implements Serializable {
	
	private static final long serialVersionUID = -8388236857108588609L;  //序列化判斷用
	
	private Integer memId;
	private Integer itemId;
	private Date excDate;
	private Boolean itemUesd;

	public MyItems(Integer memId, Integer itemId, Date excDate, Boolean itemUesd) {
		super();
		this.memId = memId;
		this.itemId = itemId;
		this.excDate = excDate;
		this.itemUesd = itemUesd;
	}

	public MyItems() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Integer getMemId() {
		return memId;
	}

	public void setMemId(Integer memId) {
		this.memId = memId;
	}

	public Integer getItemId() {
		return itemId;
	}

	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}

	public Date getExcDate() {
		return excDate;
	}

	public void setExcDate(Date excDate) {
		this.excDate = excDate;
	}

	public Boolean getItemUesd() {
		return itemUesd;
	}

	public void setItemUesd(Boolean itemUesd) {
		this.itemUesd = itemUesd;
	}

}
