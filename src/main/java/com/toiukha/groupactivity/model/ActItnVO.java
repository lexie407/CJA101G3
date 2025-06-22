package com.toiukha.groupactivity.model;

import jakarta.persistence.*;

/**
 * 活動與行程關聯 VO，對應資料庫表格 actitn
 */
@Entity
@Table(name = "actitn")
public class ActItnVO {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTID", nullable = false)
    private ActVO act;

    @Column(name = "ITNID", nullable = false)
    private Integer itnId;

    @Column(name = "DAYORDER")
    private Integer dayOrder;

    // ===== Constructor =====

    public ActItnVO() {
        super();
    }

    // ===== Getter / Setter =====

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ActVO getAct() {
        return act;
    }

    public void setAct(ActVO act) {
        this.act = act;
    }

    public Integer getItnId() {
        return itnId;
    }

    public void setItnId(Integer itnId) {
        this.itnId = itnId;
    }

    public Integer getDayOrder() {
        return dayOrder;
    }

    public void setDayOrder(Integer dayOrder) {
        this.dayOrder = dayOrder;
    }

    // ===== toString =====

    @Override
    public String toString() {
        return "ActItnVO{" +
                "id=" + id +
                ", actId=" + (act != null ? act.getActId() : null) +
                ", itnId=" + itnId +
                '}';
    }
}
