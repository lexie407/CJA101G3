package com.toiukha.members.controller; // 確保這是你的 Controller 實際套件路徑

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // 必須引入 Model
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping; // 如果你有處理 POST 請求
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute; // 如果你有 @ModelAttribute
import org.springframework.validation.BindingResult; // 如果有驗證
import jakarta.validation.Valid; // 如果有 @Valid

import java.util.ArrayList; // 引入 ArrayList (為了 errorMsgs)
import java.util.List; // 引入 List (為了 errorMsgs)

// 【關鍵修改】匯入你實際的 MembersVO 類別，它位於 com.toiukha.members.model 套件下
import com.toiukha.members.model.MembersVO;

@Controller
public class MembersController {

    @GetMapping("/membersregister") 
    public String showRegisterPage(Model model) { 
        
        model.addAttribute("membersVO", new MembersVO()); 
        model.addAttribute("errorMsgs", new ArrayList<>()); 

        
        return "front-end/members/membersRegister";
    }
    
    @GetMapping("/members/verification-sent")
    public String showVerificationSentPage(@RequestParam(value = "email", required = false) String email, Model model) {
        
        model.addAttribute("email", email);

        
        
        return "front-end/members/emailVerificationSent"; 
    }
    
}