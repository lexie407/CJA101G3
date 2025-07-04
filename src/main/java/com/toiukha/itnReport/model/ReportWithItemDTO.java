package com.toiukha.itnReport.model;

import com.toiukha.item.model.ItemVO;

/**
 * 檢舉記錄與商品資訊的組合DTO
 * 用於在前端顯示會員的檢舉記錄時同時顯示商品詳細信息
 */
public class ReportWithItemDTO {
    
    private ItnRptVO report;  // 檢舉記錄
    private ItemVO item;      // 商品資訊
    
    public ReportWithItemDTO() {
    }
    
    public ReportWithItemDTO(ItnRptVO report, ItemVO item) {
        this.report = report;
        this.item = item;
    }
    
    public ItnRptVO getReport() {
        return report;
    }
    
    public void setReport(ItnRptVO report) {
        this.report = report;
    }
    
    public ItemVO getItem() {
        return item;
    }
    
    public void setItem(ItemVO item) {
        this.item = item;
    }
    
    // 便利方法：獲取檢舉狀態描述
    public String getStatusText() {
        if (report == null || report.getRepStatus() == null) {
            return "未知";
        }
        switch (report.getRepStatus()) {
            case 0: return "待處理";
            case 1: return "檢舉通過";
            case 2: return "檢舉未通過";
            default: return "未知";
        }
    }
    
    // 便利方法：獲取檢舉狀態CSS類名
    public String getStatusClass() {
        if (report == null || report.getRepStatus() == null) {
            return "status-unknown";
        }
        switch (report.getRepStatus()) {
            case 0: return "status-pending";
            case 1: return "status-approved";
            case 2: return "status-rejected";
            default: return "status-unknown";
        }
    }
} 