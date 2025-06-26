package com.toiukha.groupactivity.security;

import jakarta.servlet.http.HttpSession;

/**
 * 權限驗證服務介面
 * 專門處理一般會員的權限檢查，管理員將有獨立的模組處理
 */
public interface AuthService {
    
    /**
     * 會員信息封裝類
     */
    class MemberInfo {
        private Integer memId;
        private String memName;
        private boolean isLoggedIn;
        
        public MemberInfo(Integer memId, String memName, boolean isLoggedIn) {
            this.memId = memId;
            this.memName = memName;
            this.isLoggedIn = isLoggedIn;
        }
        
        // Getters
        public Integer getMemId() { return memId; }
        public String getMemName() { return memName; }
        public boolean isLoggedIn() { return isLoggedIn; }
    }
    
    /**
     * 從Session中獲取當前會員信息
     * @param session HTTP Session
     * @return MemberInfo 會員信息
     */
    MemberInfo getCurrentMember(HttpSession session);
    
    /**
     * 檢查會員是否有權限查看指定團主的活動列表
     * 規則：只能查看自己的活動列表
     * @param session HTTP Session
     * @param requestedHostId 請求查看的團主ID
     * @return 驗證通過的團主ID，失敗則回傳null
     */
    Integer getAuthorizedHostId(HttpSession session, Integer requestedHostId);
    
    /**
     * 檢查會員是否有權限查看指定會員的參加活動列表
     * 規則：只能查看自己的參加活動列表
     * @param session HTTP Session
     * @param requestedMemId 請求查看的會員ID
     * @return 驗證通過的會員ID，失敗則回傳null
     */
    Integer getAuthorizedMemId(HttpSession session, Integer requestedMemId);
    
    /**
     * 檢查會員是否有權限修改指定活動
     * 規則：只能修改自己當團主的活動
     * @param session HTTP Session
     * @param actHostId 活動的團主ID
     * @return true if authorized
     */
    boolean canModifyActivity(HttpSession session, Integer actHostId);
} 