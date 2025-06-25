package com.toiukha.groupactivity.model;

import jakarta.persistence.*;

/**
 * 活動與行程關聯 VO，對應資料庫表格 actitn
 */
@Entity
@Table(name = "actitn")
//@IdClass(ActItn.class)
public class ActItnVO {

    @Id
    @Column(name = "ACTID", nullable = false)
    private Integer actId;

//    @Id
    @Column(name = "ITNID", nullable = false)
    private Integer itnId;

    /** 對應的活動 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTID", insertable = false, updatable = false, nullable = false)
    private ActVO actVO;

    //待行程模組上傳後更新
//    /** 行程實體關聯 */
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "ACTID", insertable = false, nullable = false)
//    private ItineraryVO itnVO;

    //不需要的欄位
//    @Column(name = "DAYORDER")
//    private Integer dayOrder;

    // ===== Constructor =====

    public ActItnVO() {
        super();
    }

    // ===== Getter / Setter =====

    public Integer getActId() {
        return actId;
    }

    public void setActId(Integer id) {
        this.actId = id;
    }

    public ActVO getActVO() {
        return actVO;
    }

    public void setActVO(ActVO actVO) {
        this.actVO = this.actVO;
    }

    public Integer getItnId() { return itnId;}

    public void setItnId(Integer itnId) {
        this.itnId = itnId;
    }

    //待行程模組上傳後更新
//    public itineraryVO getItnVO() {
//        return itnVO;
//    }
//
//    public void setItnVO(itineraryVO itnVO) {
//        this.itnVO = itnVO;
//    }

    //不需要的欄位
//    public Integer getDayOrder() {
//        return dayOrder;
//    }
//
//    public void setDayOrder(Integer dayOrder) {
//        this.dayOrder = dayOrder;
//    }

    // ===== toString =====

    @Override
    public String toString() {
        return "ActItnVO{" +
                "id=" + actId +
                ", actId=" + (actVO != null ? actVO.getActId() : null) +
                ", itnId=" + itnId +
                '}';
    }
}
