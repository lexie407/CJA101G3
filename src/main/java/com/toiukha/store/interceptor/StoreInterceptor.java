package com.toiukha.store.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.*;

@Component
public class StoreInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // 嘗試取得既有 session，不要自動建立
        HttpSession session = request.getSession(false);

        // 如果沒登入（session 為 null 或裡面沒 store）
        if (session == null || session.getAttribute("store") == null) {
            // 記錄原始請求 URI，登入後可跳回
            String uri = request.getRequestURI();
            session = request.getSession();          // 這裡才建立 session
            session.setAttribute("location", uri);

            // 重導到商家登入頁
            response.sendRedirect(request.getContextPath() + "/store/login");
            return false;
        }

        // 已登入就放行
        return true;
    }
}
