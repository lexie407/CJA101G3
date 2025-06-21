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

    @GetMapping("/login")
    public String showLoginForm(HttpSession session) {
        // 如果已經有 member，就不用再看登入頁，直接導到 /members/view
        if (session.getAttribute("member") != null) {
            return "redirect:/members/view";
        }
        // 否則才顯示登入表單
        return "front-end/members/login";
    }

    // 處理登入邏輯
    @PostMapping("/login")
    public String processLogin(
            @RequestParam String memAcc,
            @RequestParam String memPwd,
            HttpSession session,
            Model model) {

        // 1. 判斷帳號是否存在
        MembersVO member = membersService.getByMemAcc(memAcc);
        if (member == null) {
            model.addAttribute("errorMsgs", List.of("帳號錯誤"));
            model.addAttribute("memAcc", memAcc);
            return "front-end/members/login";
        }

        // 2. 檢查鎖定
        if (membersService.isLocked(member)) {
            int waitMinutes = membersService.minutesLeftToUnlock(member);
            model.addAttribute("errorMsgs",
                List.of("嘗試次數過多，請於 " + waitMinutes + " 分鐘後再試"));
            model.addAttribute("memAcc", memAcc);
            return "front-end/members/login";
        }

        // 3. 密碼比對
        if (!member.getMemPwd().equals(memPwd)) {
            membersService.recordLoginError(member);
            model.addAttribute("errorMsgs", List.of("密碼錯誤"));
            model.addAttribute("memAcc", memAcc);
            return "front-end/members/login";
        }
        //4.狀態檢查 只有啟用時才能通過
        if (member.getMemStatus() == 0) {
        	model.addAttribute("errorMsgs", List.of("帳號尚未啟用，請先完成 Email 驗證"));
        	return "front-end/members/login";
        }
        if (member.getMemStatus() == 2) {
        	model.addAttribute("errorMsgs", List.of("帳號已被停權，如有問題請聯絡客服"));
        	return "front-end/members/login";
        }

     // 5. 登入成功：重置錯誤計數並放入 Session
        membersService.resetLoginError(member);
        session.setAttribute("member", member);

        // 6. 如果有原始網頁位置，就跳回去；沒有就導到首頁
        String location = (String) session.getAttribute("location");
        if (location != null) {
            session.removeAttribute("location");
            return "redirect:" + location;
        }
        return "redirect:/members/view";
    }


    // 顯示忘記密碼頁面
    @GetMapping("/forgotPassword")
    public String showForgotPasswordPage() {
        return "front-end/members/forgotPassword";
    }

    // 處理忘記密碼寄信流程
    @PostMapping("/forgotPassword")
    public String processForgotPassword(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttrs) {

        //  呼 Service：查 DB → (有的話) 寄信
        membersService.processForgotPassword(email);

        //  Controller 決定跳轉到「已寄出」提示頁
        redirectAttrs.addFlashAttribute(
            "success", "若信箱存在，重設連結已寄出，請至信箱查看。");
        return "redirect:/members/forgotPasswordSent";
    }
    
    //顯示「重設密碼連結已寄出」提示畫面
    @GetMapping("/forgotPasswordSent")
    public String showForgotPasswordSentPage() {
        return "front-end/members/forgotPasswordSent";
    }
    
    
    
//      處理「點擊重設密碼連結」的畫面顯示。
    
    @GetMapping("/resetPassword")
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
    @PostMapping("/resetPassword")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("newPassword") String newPassword,
                                       RedirectAttributes redirectAttrs,
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
        
        redirectAttrs.addFlashAttribute("successMsg", " 密碼已重設成功，請使用新密碼登入！");

        return "redirect:/members/login"; // 重設成功，導向登入
    }
    
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();      // 銷毀整個 session
        return "redirect:/";       
    }
    
}
