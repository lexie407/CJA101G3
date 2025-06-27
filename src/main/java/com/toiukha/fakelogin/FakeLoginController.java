package com.toiukha.fakelogin;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;

@Controller
public class FakeLoginController {

    // 顯示虛擬登入頁面
    @GetMapping("/fakeLogin")
    public String showLoginPage() {
        return "fakeLogin"; // 對應 resources/templates/fakeLogin.html
    }

 // 會員登入
    @PostMapping("/fakeLogin")
    public String doLogin(@RequestParam("memId") String memId, HttpSession session, Model model) {
        session.setAttribute("memId", memId);
        return "redirect:/";
    }

    // 廠商登入
    @PostMapping("/fakeStoreLogin")
    public String doStoreLogin(@RequestParam("storeId") String storeId, HttpSession session, Model model) {
        session.setAttribute("storeId", storeId);
        return "redirect:/";
    }

    // 取得目前 session 的 memId
    @GetMapping("/getCurrentMemId")
    @ResponseBody
    public String getCurrentMemId(HttpSession session) {
        Object memId = session.getAttribute("memId");
        if (memId != null) {
            return memId.toString();
        } else {
            return "尚未登入";
        }
    }

    // 取得目前 session 的 storeId
    @GetMapping("/getCurrentStoreId")
    @ResponseBody
    public String getCurrentStoreId(HttpSession session) {
        Object storeId = session.getAttribute("storeId");
        if (storeId != null) {
            return storeId.toString();
        } else {
            return "尚未登入";
        }
    }
} 