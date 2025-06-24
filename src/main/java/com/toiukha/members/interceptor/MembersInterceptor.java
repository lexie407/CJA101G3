package com.toiukha.members.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.*;

@Component
public class MembersInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {//  先用 false 拿既有 Session（如果沒有，就回傳 null）
		HttpSession session = request.getSession(false);

		//  如果沒有登入（session 為 null 或裡面沒 member），就攔截並導到登入
		if (session == null || session.getAttribute("member") == null) {
			// 記錄原始請求路徑，方便登入後跳回
			String uri = request.getRequestURI();
			session = request.getSession(); // 這裡才建立 Session
			session.setAttribute("location", uri);
			// 重導到登入頁
			response.sendRedirect(request.getContextPath() + "/members/login");
			return false; // 攔截
		}

		//  已登入：放行
		return true;
	}

}
