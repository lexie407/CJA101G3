package com.toiukha.members.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebFilter(urlPatterns = "/members/update")   //需搭配application上註冊 @ServletComponentScan
public class LoginFilter implements Filter {

    private FilterConfig config;

    @Override
    public void init(FilterConfig config) {
        this.config = config;
    }

    @Override
    public void destroy() {
        config = null;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws ServletException, IOException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession();
        Object member = session.getAttribute("member"); 

        if (member == null) {
            session.setAttribute("location", req.getRequestURI());
            res.sendRedirect(req.getContextPath() + "/members/login"); 
        } else {
            chain.doFilter(request, response);
        }
    }
}
