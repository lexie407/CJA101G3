package com.toiukha.members.controller; // 確保這是你的 Controller 實際套件路徑

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.toiukha.members.model.MembersVO;
import com.toiukha.email.EmailService;
import com.toiukha.members.model.MembersService;

import jakarta.validation.Valid;

@Controller
public class MembersController {
	 @Autowired
	    private MembersService membersService;
	 @Autowired
	 private EmailService emailService;

    @GetMapping("/membersregister") 
    public String showRegisterPage(Model model) { 
        
        model.addAttribute("membersVO", new MembersVO()); 
        model.addAttribute("errorMsgs", new ArrayList<>()); 

        
        return "front-end/members/membersRegister";
    }
    
    @GetMapping("/members/verification-sent")
    public String showVerificationSentPage(@RequestParam(value = "email", required = false) String email, Model model) {
    	 if (email == null || email.isBlank()) {
    	        // 若沒有 email，代表不是從註冊流程過來的，導回註冊頁
    	        return "redirect:/membersregister";
    	    }

    	    model.addAttribute("email", email);
    	    return "front-end/members/emailVerificationSent";
    	}
    
   
//      接收註冊表單 POST 請求，處理註冊流程
   
    @PostMapping("/register")
    public String processRegister(
            @Valid @ModelAttribute("membersVO") MembersVO membersVO,
            BindingResult result,
            Model model,
            @RequestParam("memAvatar") MultipartFile memAvatar) throws IOException {

        /*************************** 1.接收請求參數 - 輸入格式與邏輯錯誤處理 ************************/

        // 去除BindingResult中 memAvatar 欄位的FieldError紀錄
        result = removeFieldError(membersVO, result, "memAvatar");

     // 檢查是否為圖片檔案（避免上傳非圖片）
        if (!memAvatar.isEmpty()) {
            String contentType = memAvatar.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.rejectValue("memAvatar", "error.membersVO", "請上傳圖片格式檔案（ex:jpg、png）");
            } else {
                byte[] buf = memAvatar.getBytes();
                membersVO.setMemAvatar(buf);
            }
        }

        // 加入帳號/Email 重複錯誤檢查（放在 result 中）
        if (membersService.existsByMemAcc(membersVO.getMemAcc())) {
            result.rejectValue("memAcc", "error.membersVO", "帳號已存在，請使用其他帳號");
        }
        if (membersService.existsByMemEmail(membersVO.getMemEmail())) {
            result.rejectValue("memEmail", "error.membersVO", "Email 已被註冊，請使用其他信箱");
        }

        // 若欄位驗證失敗（格式 + 唯一鍵錯誤都會列出）
        if (result.hasErrors()) {
            List<String> errorMsgs = result.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());
            model.addAttribute("errorMsgs", errorMsgs);
            return "front-end/members/membersRegister";
        }

        /*************************** 2.開始新增資料 *****************************************/
        membersService.registerMember(membersVO);

        /*************************** 3.新增完成,準備轉交(Send the Success view) **************/
        return "redirect:/members/verification-sent?email=" + membersVO.getMemEmail();
    }
    
    

    /**
     * 使用者點擊信件中的驗證連結，啟用帳號
     */
    @GetMapping("/member/verify")
    public String verifyEmail(@RequestParam("token") String token, Model model) {
        boolean success = membersService.verifyAndActivateMember(token);
        if (success) {
            return "redirect:/login"; // 驗證成功導向登入頁
        } else {
            model.addAttribute("errorMsg", "驗證連結無效或已過期！");
            return "front-end/members/emailVerificationFailed"; // 你可以自訂這個頁面
        }
    }
    
    
    @PostMapping("/resend-verification")
    public String resendVerification(@RequestParam("email") String email, RedirectAttributes redirectAttr) {
        MembersVO member = membersService.getByEmail(email);
        
        if (member != null && member.getMemStatus() == 0) { // 0 = 尚未啟用
            // 重送驗證信
            emailService.sendVerificationEmail(email, member.getMemId());
            redirectAttr.addFlashAttribute("msg", "驗證信已重新發送，請查收！");
        } else {
            // 安全性考量，避免洩漏帳號是否存在
            redirectAttr.addFlashAttribute("msg", "此信箱尚未註冊或已啟用。");
        }

        return "redirect:/members/verification-sent?email=" + email;
    }
    
    
    @GetMapping("/members/testUpdate")
    public String showUpdateTestPage(Model model) {
        
        model.addAttribute("membersVO", new MembersVO());
        return "front-end/members/membersUpdate"; 
    }
    
    @GetMapping("/test/email-fail")
    public String showEmailVerificationFailed() {
        return "front-end/members/emailVerificationFailed";
    }
    
    
    
    
    
    
    
    
 // 去除 BindingResult 中某個欄位的 FieldError 紀錄
    private BindingResult removeFieldError(MembersVO membersVO, BindingResult result, String removedFieldname) {
        List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
                .filter(fieldError -> !fieldError.getField().equals(removedFieldname))
                .collect(Collectors.toList());

        result = new BeanPropertyBindingResult(membersVO, "membersVO");
        for (FieldError fieldError : errorsListToKeep) {
            result.addError(fieldError);
        }
        return result;
    }
}