package com.toiukha.administrant.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AdministrantInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// 先嘗試取現有 Session，不要自動建立
		HttpSession session = request.getSession(false);

		// 如果沒登入（session 為 null 或裡面沒 admin），攔截並跳到管理員登入頁
		if (session == null || session.getAttribute("admin") == null) {
			// 記錄原始請求路徑，方便登入後跳回
			String uri = request.getRequestURI();
			session = request.getSession(); // 這裡才建立 Session
			session.setAttribute("location", uri);
			// 重導到後台管理員登入頁
			response.sendRedirect(request.getContextPath() + "/admins/login");
			return false;
		}

		// 已登入，放行
		return true;
	}
}
