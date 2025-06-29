package com.toiukha.administrant.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.toiukha.administrant.model.AdministrantService;
import com.toiukha.administrant.model.AdministrantVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admins")
public class AdministrantLoginController {
	
	@Autowired
    private AdministrantService administrantService;
	
	
	// 顯示管理員登入頁面
    @GetMapping("/login")
    public String showLoginPage() {
        return "back-end/administrant/login";  // 對應 templates/back-end/admins/login.html
    }
    
    // 處理登入
    @PostMapping("/login")
    public String processLogin(
            @RequestParam String adminAcc,
            @RequestParam String adminPwd,
            HttpSession session,
            Model model) {

        // 1. 判斷帳號是否存在
        Optional<AdministrantVO> opt = administrantService.findByAdminAcc(adminAcc);
        if (opt.isEmpty()) {
            model.addAttribute("errorMsgs", List.of("管理員帳號錯誤"));
            model.addAttribute("adminAcc", adminAcc);
            return "back-end/administrant/login";
        }
        AdministrantVO admin = opt.get();

        // 2. 密碼比對
        if (!admin.getAdminPwd().equals(adminPwd)) {
            model.addAttribute("errorMsgs", List.of("管理員密碼錯誤"));
            model.addAttribute("adminAcc", adminAcc);
            return "back-end/administrant/login";
        }

        // 3. 帳號狀態檢查：0 = 啟用；1 = 停權
        if (admin.getAdminStatus() == 1) {
            model.addAttribute("errorMsgs", List.of("帳號已被停權，如有問題請聯絡最高管理員"));
            model.addAttribute("adminAcc", adminAcc);
            return "back-end/administrant/login";
        }

        // 4. 登入成功：放入 Session
        session.setAttribute("admin", admin);

        // 5. 回跳原始頁面或導到管理員列表
        String location = (String) session.getAttribute("location");
        if (location != null) {
            session.removeAttribute("location");
            return "redirect:" + location;
        }
        return "redirect:/";
    }
    
    
    
    
 // 處理登出
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admins/login";
    }

}
