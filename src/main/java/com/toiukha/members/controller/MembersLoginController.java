package com.toiukha.members.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.toiukha.email.EmailService;
import com.toiukha.members.model.MembersService;
import com.toiukha.members.model.MembersVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/members")
public class MembersLoginController {

    @Autowired
    private MembersService membersService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 顯示登入表單
    @GetMapping("/login")
    public String showLoginForm() {
        return "front-end/members/login";
    }

    // 處理登入邏輯
    @PostMapping("/login")
    public String processLogin(@RequestParam("memAcc") String account,
                               @RequestParam("memPwd") String password,
                               HttpSession session,
                               Model model) {

        MembersVO member = membersService.getByMemAcc(account);

        if (member == null) {
            model.addAttribute("errorMsgs", List.of("帳號錯誤"));
            model.addAttribute("memAcc", account);
            return "front-end/members/login";
        }

        if (!member.getMemPwd().equals(password)) {
            model.addAttribute("errorMsgs", List.of("密碼錯誤"));
            model.addAttribute("memAcc", account);
            return "front-end/members/login";
        }

        // 登入成功，放入 Session
        session.setAttribute("member", member);
        return "redirect:/"; // 導回首頁或會員中心
    }

    // 顯示忘記密碼頁面
    @GetMapping("/forgotPassword")
    public String showForgotPasswordPage() {
        return "front-end/members/forgotPassword";
    }

    // 處理忘記密碼寄信流程
    @PostMapping("/forgotPassword")
    public String processForgotPassword(@RequestParam("email") String email,
                                        RedirectAttributes redirectAttrs) {
        membersService.processForgotPassword(email); // 呼叫 Service 寄出重設連結
        redirectAttrs.addFlashAttribute("success", "若信箱存在，重設連結已寄出");
        return "redirect:/members/forgotPasswordSent"; // 導向已寄出提示頁
    }
    
    //顯示「重設密碼連結已寄出」提示畫面
    @GetMapping("/forgotPasswordSent")
    public String showForgotPasswordSentPage() {
        return "front-end/members/forgotPasswordSent";
    }
    
    
    
//      處理「點擊重設密碼連結」的畫面顯示。
    
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        Integer memId = emailService.verifyResetToken(token);
        
        if (memId == null) {
            model.addAttribute("errorMsg", "重設連結已失效或無效，請重新申請。");
            return "front-end/members/resetPasswordError"; // 錯誤頁面
        }

        model.addAttribute("token", token);
        return "front-end/members/resetPassword"; // 顯示輸入新密碼頁面
    }
    
    
 // 處理新密碼提交
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("newPassword") String newPassword,
                                       Model model) {
        Integer memId = emailService.verifyResetToken(token);
        if (memId == null) {
            model.addAttribute("errorMsg", "連結已失效，請重新申請重設密碼。");
            return "front-end/members/resetPasswordError"; // 錯誤頁（與 GET 共用）
        }

        MembersVO member = membersService.getOneMember(memId);
        if (member == null) {
            model.addAttribute("errorMsg", "找不到會員帳號。");
            return "front-end/members/resetPasswordError";
        }

        member.setMemPwd(newPassword); // 實務可加密
        membersService.updateMembers(member);

        // 刪除 token，避免被重複使用
        redisTemplate.delete("reset:" + token);

        return "redirect:/members/login"; // 重設成功，導向登入
    }
    
}
