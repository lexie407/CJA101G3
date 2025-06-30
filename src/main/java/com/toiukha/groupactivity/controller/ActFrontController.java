package com.toiukha.groupactivity.controller;

import com.toiukha.groupactivity.model.ActVO;
import com.toiukha.groupactivity.security.AuthService;
import com.toiukha.groupactivity.service.ActService;
import com.toiukha.groupactivity.service.DefaultImageService;
import com.toiukha.participant.model.PartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.format.DateTimeFormatter;

/*
 * 前端轉送用controller
 */
@Controller
@RequestMapping("/act/member")
public class ActFrontController {

    @Autowired
    private ActService actSvc;
    
    @Autowired
    private DefaultImageService defaultImageService;

    @Autowired
    private PartService partSvc;
    
    @Autowired
    private AuthService authService;

    //新增活動
    @GetMapping("/add")
    public String addActPage(HttpServletRequest request) {
        HttpSession session = request.getSession();
        AuthService.MemberInfo memberInfo = authService.getCurrentMember(session);
        
        return "front-end/groupactivity/addAct_ajax";
    }

    //編輯活動
    @GetMapping("/edit/{id}")
    public String editAct(@PathVariable Integer id, Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        
        // 先取得活動資料
        ActVO actVo = actSvc.getOneAct(id);
        if (actVo == null) {
            // 活動不存在，重定向到搜尋頁面
            return "redirect:/act/member/search";
        }
        
        // 安全驗證：檢查會員是否有權限編輯此活動
        if (!authService.canModifyActivity(session, actVo.getHostId())) {
            // 無權限：重定向到活動詳情頁面或登入頁
            AuthService.MemberInfo memberInfo = authService.getCurrentMember(session);
            
            if (!memberInfo.isLoggedIn()) {
                // 未登入：導向登入頁面
                return "redirect:/members/login";
            } else {
                // 已登入但無權限：重定向到活動詳情頁面
                return "redirect:/act/member/view/" + id;
            }
        }
        
        model.addAttribute("actVo", actVo);
        return "front-end/groupactivity/editAct_ajax";
    }

    //搜尋所有揪團活動
    @GetMapping("/search")
    public String searchActPage(HttpServletRequest request) {
        HttpSession session = request.getSession();
        AuthService.MemberInfo memberInfo = authService.getCurrentMember(session);
        
        return "front-end/groupactivity/searchAct";
    }

    //檢視活動詳情
    @GetMapping("/view/{id}")
    public String viewAct(@PathVariable Integer id, Model model, HttpServletRequest request) {
        ActVO actVo = actSvc.getOneAct(id);
        model.addAttribute("actVo", actVo);

        boolean isParticipant = false;
        boolean isHost = false;

        HttpSession session = request.getSession();
        AuthService.MemberInfo memberInfo = authService.getCurrentMember(session);

        if (memberInfo.isLoggedIn()) {
            Integer memId = memberInfo.getMemId();
            
            // 檢查是否為團主
            if (actVo.getHostId().equals(memId)) {
                isHost = true;
            } else {
                // 如果不是團主，再檢查是否為團員
                isParticipant = partSvc.getParticipants(id).contains(memId);
            }
        }
        
        model.addAttribute("isHost", isHost);
        model.addAttribute("isParticipant", isParticipant);
        
        // [優化] 在後端格式化日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        model.addAttribute("formattedSignupStart", actVo.getSignupStart().format(formatter));
        model.addAttribute("formattedSignupEnd", actVo.getSignupEnd().format(formatter));
        model.addAttribute("formattedActStart", actVo.getActStart().format(formatter));
        model.addAttribute("formattedActEnd", actVo.getActEnd().format(formatter));

        return "front-end/groupactivity/listOneAct";
    }

    //我揪的團 - 加入安全驗證
    @GetMapping("/listMy/{hostId}")
    public String listMyAct(@PathVariable Integer hostId, Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        
        // 安全驗證：檢查會員是否有權限查看指定的活動列表
        Integer authorizedHostId = authService.getAuthorizedHostId(session, hostId);
        
        if (authorizedHostId == null) {
            // 無權限：重定向到自己的活動列表或登入頁
            AuthService.MemberInfo memberInfo = authService.getCurrentMember(session);
            
            if (!memberInfo.isLoggedIn()) {
                // 未登入：導向登入頁面
                return "redirect:/members/login";
            } else {
                // 已登入但無權限：重定向到自己的活動列表
                return "redirect:/act/member/listMy/" + memberInfo.getMemId();
            }
        }
        
        model.addAttribute("actList", actSvc.getByHost(authorizedHostId));
        model.addAttribute("hostId", authorizedHostId);
        return "front-end/groupactivity/listMyAct";
    }
    
    //我跟的團 - 加入安全驗證
    @GetMapping("/listMyJoin/{memId}")
    public String listMyJoinAct(@PathVariable Integer memId, Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        
        // 安全驗證：檢查會員是否有權限查看指定的參加活動列表
        Integer authorizedMemId = authService.getAuthorizedMemId(session, memId);
        
        if (authorizedMemId == null) {
            // 無權限：重定向到自己的參加活動列表或登入頁
            AuthService.MemberInfo memberInfo = authService.getCurrentMember(session);
            
            if (!memberInfo.isLoggedIn()) {
                // 未登入：導向登入頁面
                return "redirect:/members/login";
            } else {
                // 已登入但無權限：重定向到自己的參加活動列表
                return "redirect:/act/member/listMyJoin/" + memberInfo.getMemId();
            }
        }
        
        model.addAttribute("memId", authorizedMemId);
        return "front-end/groupactivity/listMyJoinAct";
    }


    /**
     * 圖片顯示端點 - 參考members模組的錯誤處理模式
     * 如果活動沒有圖片或發生錯誤，回傳預設圖片
     */
    @GetMapping(value = "/image/{actId}", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@PathVariable Integer actId) {
        try {
            // 嘗試獲取活動圖片
            byte[] imageBytes = actSvc.getActImageOnly(actId);
            
            if (imageBytes != null && imageBytes.length > 0) {
                return ResponseEntity.ok(imageBytes);
            } else {
                // 如果沒有圖片，回傳預設圖片
                return ResponseEntity.ok(defaultImageService.getDefaultImage());
            }
            
        } catch (Exception e) {
            // 發生任何錯誤時，返回預設圖片而非拋出異常
            System.err.println("獲取活動圖片錯誤 (actId: " + actId + "): " + e.getMessage());
            try {
                return ResponseEntity.ok(defaultImageService.getDefaultImage());
            } catch (Exception defaultImageError) {
                // 如果連預設圖片都無法載入，返回空的圖片回應
                System.err.println("載入預設圖片失敗: " + defaultImageError.getMessage());
                return ResponseEntity.notFound().build();
            }
        }
    }
}
