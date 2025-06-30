package com.toiukha.groupactivity.security;

import com.toiukha.members.model.MembersVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 生產環境的權限驗證實作
 * 
 * 啟用方式：
 * 1. 將此檔案改名為 MembersAuthServiceImpl.java
 * 2. 加上 @Primary 註解
 * 3. DevAuthServiceImpl 會自動被停用
 */
@Service
@Primary
public class MembersAuthServiceImpl implements AuthService {
    
    @Override
    public MemberInfo getCurrentMember(HttpSession session) {
        Object member = session.getAttribute("member");
        
        if (member instanceof MembersVO) {
            MembersVO memberVO = (MembersVO) member;
            return new MemberInfo(
                memberVO.getMemId(),
                memberVO.getMemName(),
                true
            );
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