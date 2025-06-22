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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.toiukha.members.model.MembersVO;
import com.toiukha.email.EmailService;
import com.toiukha.members.model.MembersService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/members")
public class MembersController {
	 @Autowired
	    private MembersService membersService;
	 @Autowired
	 private EmailService emailService;

	 @GetMapping("/register")    
    public String showRegisterPage(Model model) { 
        
        model.addAttribute("membersVO", new MembersVO()); 
        model.addAttribute("errorMsgs", new ArrayList<>()); 

        
        return "front-end/members/register";
    }
    
	 @GetMapping("/verification-sent") 
    public String showVerificationSentPage(@RequestParam(value = "email", required = false) String email, Model model) {
    	 if (email == null || email.isBlank()) {
    	        // 若沒有 email，代表不是從註冊流程過來的，導回註冊頁
    	        return "redirect:/members/register";
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
            return "front-end/members/register";
        }

        /*************************** 2.開始新增資料 *****************************************/
        membersService.registerMember(membersVO);

        /*************************** 3.新增完成,準備轉交(Send the Success view) **************/
        return "redirect:/members/verification-sent?email=" + membersVO.getMemEmail();
    }
    
    

    /**
     * 使用者點擊信件中的驗證連結，啟用帳號
     */
    @GetMapping("/verify")
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
    
    
    @GetMapping("/update")
    public String showUpdatePage(HttpSession session, Model model) {
        MembersVO sessionMember = (MembersVO) session.getAttribute("member");
        model.addAttribute("membersVO", sessionMember);
        return "front-end/members/update";
    }
    
    @PostMapping("/update")
    public String updateMember(
            @Valid @ModelAttribute("membersVO") MembersVO membersVO,
            BindingResult result,
            Model model,
            HttpSession session,
            @RequestParam("memAvatarFile") MultipartFile memAvatarFile,
            @RequestParam("memAvatarFrameFile") MultipartFile memAvatarFrameFile
    ) throws IOException {

        MembersVO sessionMember = (MembersVO) session.getAttribute("member");
        if (sessionMember == null) {
            return "redirect:/members/login";
        }

        // 強制使用 Session 中的 memId & memAcc（防止偽造）
        membersVO.setMemId(sessionMember.getMemId());
        membersVO.setMemAcc(sessionMember.getMemAcc());
        membersVO.setMemRegTime(sessionMember.getMemRegTime());
        
        membersVO.setMemGroupAuth(sessionMember.getMemGroupAuth());
        membersVO.setMemGroupPoint(sessionMember.getMemGroupPoint());
        membersVO.setMemStatus(sessionMember.getMemStatus());
        membersVO.setMemStoreAuth(sessionMember.getMemStoreAuth());
        membersVO.setMemStorePoint(sessionMember.getMemStorePoint());
        membersVO.setMemPoint(sessionMember.getMemPoint());
        membersVO.setMemLogErrCount(sessionMember.getMemLogErrCount());
        membersVO.setMemLogErrTime(sessionMember.getMemLogErrTime());

        // 處理頭像（若上傳新圖片才更新）
        if (!memAvatarFile.isEmpty() && memAvatarFile.getContentType().startsWith("image/")) {
            membersVO.setMemAvatar(memAvatarFile.getBytes());
        } else {
            membersVO.setMemAvatar(sessionMember.getMemAvatar());
        }

        if (!memAvatarFrameFile.isEmpty() && memAvatarFrameFile.getContentType().startsWith("image/")) {
            membersVO.setMemAvatarFrame(memAvatarFrameFile.getBytes());
        } else {
            membersVO.setMemAvatarFrame(sessionMember.getMemAvatarFrame());
        }

        // 若驗證有錯誤，回填資料並返回表單
        if (result.hasErrors()) {
            List<String> errorMsgs = result.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());
            model.addAttribute("errorMsgs", errorMsgs);
            return "front-end/members/update";
        }

        // 更新會員資料
        membersService.updateMembers(membersVO);

        // 更新 session 裡的資料（避免還是舊資料）
        session.setAttribute("member", membersVO);

        return "redirect:/members/update?success"; // 可以在 update 頁面用參數判斷顯示「修改成功」
    }
    
    @GetMapping("/email-fail")
    public String showEmailVerificationFailed() {
        return "front-end/members/emailVerificationFailed";
    }
    
    
    @GetMapping("/selectPage")
    public String showSelectPage(Model model) {
        model.addAttribute("membersList", membersService.findAllMembers());
        return "back-end/members/selectPage";
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