package com.toiukha.report.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "report")
public class ReportVO {

		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		@Column(name = "REPID", insertable = false, updatable = false)
	    private Integer repId;        // 異常回報編號（PK）
		
		@Column(name = "MEMID", nullable = false, updatable = false)
	    private Integer memId;        // 回報會員編號（FK）
		
		@Column(name = "REPTYPE", nullable = false, length = 50)
	    private String repType;       // 回報類型簡述
		
		@Column(name = "REPDESC", nullable = false, length = 500)
	    private String repDesc;       // 異常狀態描述
		
		@Column(name = "REPSTATUS", insertable = false, nullable = false)
	    private Byte repStatus;       // 處理狀態（0:未處理, 1:處理中, 2:完成）
		
		@Column(name = "REPAT", insertable = false, updatable = false)
	    private Timestamp repAt;  // 回報時間
		
		@Column(name = "CLOSEXPLAN", length = 500)
	    private String closExplan;    // 結案說明


		// Getter & Setter

	    public Integer getRepId() {
	        return repId;
	    }

	    public void setRepId(Integer repId) {
	        this.repId = repId;
	    }

	    public Integer getMemId() {
	        return memId;
	    }

	    public void setMemId(Integer memId) {
	        this.memId = memId;
	    }

	    public String getRepType() {
	        return repType;
	    }

	    public void setRepType(String repType) {
	        this.repType = repType;
	    }

	    public String getRepDesc() {
	        return repDesc;
	    }

	    public void setRepDesc(String repDesc) {
	        this.repDesc = repDesc;
	    }

	    public Byte getRepStatus() {
	        return repStatus;
	    }

	    public void setRepStatus(Byte repStatus) {
	        this.repStatus = repStatus;
	    }

	    public Timestamp getRepAt() {
	        return repAt;
	    }

	    public void setRepAt(Timestamp repAt) {
	        this.repAt = repAt;
	    }

	    public String getClosExplan() {
	        return closExplan;
	    }

	    public void setClosExplan(String closExplan) {
	        this.closExplan = closExplan;
	    }
	
}
