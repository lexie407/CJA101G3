package com.toiukha.groupactivity.security;

import com.toiukha.members.model.MembersVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 開發階段的權限驗證實作
 * 只處理一般會員的權限檢查，管理員將有獨立模組
 */
@Service
public class DevAuthServiceImpl implements AuthService {
    
    // 開發階段配置
    private static final boolean DEV_MODE = true;
    private static final Integer DEV_USER_ID = 30;
    
    @Override
    public MemberInfo getCurrentMember(HttpSession session) {
        Object member = session.getAttribute("member");
        
        if (member != null) {
            // === 開發模式：處理假用戶字串格式 ===
            if (member instanceof String) {
                String devUser = (String) member;
                if (devUser.startsWith("DEV_USER_")) {
                    try {
                        Integer memId = Integer.parseInt(devUser.substring(9));
                        return new MemberInfo(memId, "開發用戶" + memId, true);
                    } catch (NumberFormatException e) {
                        System.err.println("無法解析開發用戶ID: " + devUser);
                    }
                }
            }
            
            // === 生產模式：處理真實MembersVO ===
            if (member instanceof MembersVO) {
                MembersVO memberVO = (MembersVO) member;
                return new MemberInfo(
                    memberVO.getMemId(),
                    memberVO.getMemName(),
                    true
                );
            }
        }
        
        // === 開發階段：使用假用戶 ===
        if (DEV_MODE) {
            return new MemberInfo(DEV_USER_ID, "開發用戶" + DEV_USER_ID, true);
        }
        
        // 未登入
        return new MemberInfo(null, null, false);
    }
    
    @Override
    public Integer getAuthorizedHostId(HttpSession session, Integer requestedHostId) {
        MemberInfo memberInfo = getCurrentMember(session);
        
        if (!memberInfo.isLoggedIn()) {
            return null;
        }
        
        // 簡單規則：只能查看自己的活動列表
        if (Objects.equals(memberInfo.getMemId(), requestedHostId)) {
            return requestedHostId;
        }
        
        return null;
    }
    
    @Override
    public Integer getAuthorizedMemId(HttpSession session, Integer requestedMemId) {
        MemberInfo memberInfo = getCurrentMember(session);
        
        if (!memberInfo.isLoggedIn()) {
            return null;
        }
        
        // 簡單規則：只能查看自己的參加活動列表
        if (Objects.equals(memberInfo.getMemId(), requestedMemId)) {
            return requestedMemId;
        }
        
        return null;
    }
    
    @Override
    public boolean canModifyActivity(HttpSession session, Integer actHostId) {
        MemberInfo memberInfo = getCurrentMember(session);
        
        if (!memberInfo.isLoggedIn()) {
            return false;
        }
        
        // 簡單規則：只能修改自己當團主的活動
        return Objects.equals(memberInfo.getMemId(), actHostId);
    }
} 