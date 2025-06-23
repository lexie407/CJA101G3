package com.toiukha.groupactivity.model;

/**
 * 活動狀態常數定義
 */
public class ActStatus {
    public static final byte OPEN = 0;      // 招募中
    public static final byte FULL = 1;      // 成團
    public static final byte FAILED = 2;    // 未成團
    public static final byte CANCELLED = 3; // 團主取消
    public static final byte FROZEN = 4;    // 系統凍結
    public static final byte ENDED = 5;     // 活動結束
} 