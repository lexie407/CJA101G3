package com.toiukha.administrantauth.interceptor;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AdministrantAuthInterceptor implements HandlerInterceptor {

    // URL → 功能 ID 對應表
    private static final Map<String, Integer> URL_TO_FUNC = Map.of(
        "/admins/listAll",    1,  // 功能 1：列出所有管理員
        "/admins/selectPage", 1,
        "/admins/search",     1,
        "/admins/add",        2,  // 功能 2：新增管理員
        "/admins/edit",       3,  // 功能 3：編輯管理員
        "/admins/dashboard",  4   // 功能 4：儀表板
        // 如有更多需要授權的路徑，繼續加在這裡
    );

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 取 session（不要自動建）
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/admins/login");
            return false;
        }

        // 從 session 拿使用者的權限清單（登入時要放進 PERMISSIONS）
        @SuppressWarnings("unchecked")
        List<Integer> perms = (List<Integer>) session.getAttribute("adminFuncIds");
        if (perms == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "尚未授權");
            return false;
        }

        // 取得請求的相對路徑（去掉 contextPath）
        String path = request.getRequestURI()
                             .substring(request.getContextPath().length());
        Integer funcId = URL_TO_FUNC.get(path);

        // 如果這條路徑不需授權檢查，直接放行
        if (funcId == null) {
            return true;
        }

        // 檢查該使用者是否擁有這個功能
        if (!perms.contains(funcId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "沒有操作此功能的權限");
            return false;
        }

        // 權限足夠 → 放行
        return true;
    }
}