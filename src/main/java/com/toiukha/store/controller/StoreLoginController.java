package com.toiukha.store.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.toiukha.email.EmailService;
import com.toiukha.store.model.StoreService;
import com.toiukha.store.model.StoreVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/store")
public class StoreLoginController {
	
	@Autowired
    private StoreService storeService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
    private RedisTemplate<String, String> redisTemplate;


    // GET /store/login：顯示登入頁
	@GetMapping("/login")
	public String showLoginForm(HttpSession session) {
	    // 如果已經登入（session 裡有 store），就跳到檢視頁
	    if (session.getAttribute("store") != null) {
	        return "redirect:/store/view";
	    }
	    // 否則顯示登入表單
	    return "front-end/store/login";
	}
    
    
    @PostMapping("/login")
    public String processLogin(
            @RequestParam String storeAcc,
            @RequestParam String storePwd,
            HttpSession session,
            Model model) {

        // 1. 判斷帳號是否存在
        Optional<StoreVO> opt = storeService.findByStoreAcc(storeAcc);
        if (opt.isEmpty()) {
            model.addAttribute("errorMsgs", List.of("商家帳號錯誤"));
            model.addAttribute("storeAcc", storeAcc);
            return "front-end/store/login";
        }
        StoreVO store = opt.get();

        // 2. 密碼比對
        if (!store.getStorePwd().equals(storePwd)) {
            model.addAttribute("errorMsgs", List.of("商家密碼錯誤"));
            model.addAttribute("storeAcc", storeAcc);
            return "front-end/store/login";
        }

        // 3. 狀態一個一個檢查
        // 0：待審核
        if (store.getStoreStatus() == 0) {
            model.addAttribute("errorMsgs", List.of("帳號尚在審核中，請稍後"));
            model.addAttribute("storeAcc", storeAcc);
            return "front-end/store/login";
        }
        // 2：駁回
        if (store.getStoreStatus() == 2) {
            model.addAttribute("errorMsgs", List.of("您的申請已被駁回，請聯絡客服"));
            model.addAttribute("storeAcc", storeAcc);
            return "front-end/store/login";
        }
        // 3：停權
        if (store.getStoreStatus() == 3) {
            model.addAttribute("errorMsgs", List.of("帳號已被停權，如有問題請聯絡客服"));
            model.addAttribute("storeAcc", storeAcc);
            return "front-end/store/login";
        }

        // 4. 登入成功（storeStatus == 1）
        session.setAttribute("store", store);

        // 5. 回跳原始頁面或導到商家首頁
        String location = (String) session.getAttribute("location");
        if (location != null) {
            session.removeAttribute("location");
            return "redirect:" + location;
        }
        return "redirect:/store/view";
    }
    
    
    // 顯示忘記密碼頁面
    @GetMapping("/forgotPassword")
    public String showForgotPasswordPage() {
        return "front-end/store/forgotPassword";
    }
    
 // 處理商家忘記密碼寄信流程
    @PostMapping("/forgotPassword")
    public String processStoreForgotPassword(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttrs) {

        // 呼叫 Service 去查 DB → 有的話寄信
        storeService.processForgotPassword(email);

        // 加入 flash attribute，跳轉到已寄出提示頁
        redirectAttrs.addFlashAttribute(
            "success",
            "若該信箱有對應商家帳號，重設連結已寄出，請至信箱查看。"
        );
        return "redirect:/store/forgotPasswordSent";
    }
    
 // 顯示「重設連結已寄出」提示頁面
    @GetMapping("/forgotPasswordSent")
    public String showForgotPasswordSentPage() {
        return "front-end/store/forgotPasswordSent";
    }
    
    
 // 顯示「設定新密碼」表單或錯誤提示
    @GetMapping("/resetPassword")
    public String showResetPasswordForm(
            @RequestParam("token") String token,
            Model model) {

        Integer storeId = emailService.verifyStoreResetToken(token);
        if (storeId == null) {
            // token 無效或逾時
            model.addAttribute("errorMsg", "重設連結已失效，請重新申請。");
            return "front-end/store/resetPasswordError";
        }

        // token 有效，顯示設定新密碼表單
        model.addAttribute("token", token);
        return "front-end/store/resetPassword";
    }
    
    
 // 處理商家重設密碼提交
    @PostMapping("/resetPassword")
    public String processStoreResetPassword(
            @RequestParam("token") String token,
            @RequestParam("newPassword") String newPassword,
            RedirectAttributes redirectAttrs,
            Model model) {

        // 1. 驗證 token
        Integer storeId = emailService.verifyStoreResetToken(token);
        if (storeId == null) {
            model.addAttribute("errorMsg", "重設連結已失效，請重新申請。");
            return "front-end/store/resetPasswordError";
        }

        // 2. 查商家
        StoreVO store = storeService.getById(storeId);
        if (store == null) {
            model.addAttribute("errorMsg", "找不到商家帳號。");
            return "front-end/store/resetPasswordError";
        }

        // 3. 更新密碼
        store.setStorePwd(newPassword);
        storeService.save(store);  

        // 4. 刪除 token，避免重複使用
        redisTemplate.delete("store:reset:" + token);

        // 5. 跳回登入並顯示一次性成功訊息
        redirectAttrs.addFlashAttribute(
            "successMsg",
            "密碼已重設成功，請使用新密碼登入！"
        );
        return "redirect:/store/login";
    }
    
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();      // 銷毀整個 session
        return "redirect:/";       
    }
    
    
}