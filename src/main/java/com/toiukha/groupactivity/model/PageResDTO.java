package com.toiukha.groupactivity.model;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分頁回應 DTO
 */
public class PageResDTO<T> {
    
    // 實際資料內容
    private List<T> content;
    
    // 分頁資訊
    private int page;           // 當前頁碼 (0-based)
    private int size;           // 每頁筆數
    private long totalElements; // 總筆數
    private int totalPages;     // 總頁數
    private boolean first;      // 是否為第一頁
    private boolean last;       // 是否為最後一頁
    private boolean hasNext;    // 是否有下一頁
    private boolean hasPrevious;// 是否有上一頁
    
    // 建構函數
    public PageResDTO() {
    }
    
    /**
     * 從 Spring Data JPA Page 物件建構 PageResponse
     * @param page Spring Data JPA 的 Page 物件
     */
    public PageResDTO(Page<T> page) {
        this.content = page.getContent();
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.first = page.isFirst();
        this.last = page.isLast();
        this.hasNext = page.hasNext();
        this.hasPrevious = page.hasPrevious();
    }
    
    /**
     * 靜態工廠方法：從 Page 物件創建 PageResponse
     * @param page Spring Data JPA 的 Page 物件
     * @return PageResponse 物件
     */
    public static <T> PageResDTO<T> of(Page<T> page) {
        return new PageResDTO<>(page);
    }
    
    // Getter 和 Setter
    public List<T> getContent() {
        return content;
    }
    
    public void setContent(List<T> content) {
        this.content = content;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public long getTotalElements() {
        return totalElements;
    }
    
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public boolean isFirst() {
        return first;
    }
    
    public void setFirst(boolean first) {
        this.first = first;
    }
    
    public boolean isLast() {
        return last;
    }
    
    public void setLast(boolean last) {
        this.last = last;
    }
    
    public boolean isHasNext() {
        return hasNext;
    }
    
    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    public boolean isHasPrevious() {
        return hasPrevious;
    }
    
    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
    
    @Override
    public String toString() {
        return "PageResponse{" +
                "content=" + content +
                ", page=" + page +
                ", size=" + size +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", first=" + first +
                ", last=" + last +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                '}';
    }
} 