package com.toiukha.groupactivity.controller;

import com.toiukha.groupactivity.model.ActService;
import com.toiukha.groupactivity.model.ActVO;
import com.toiukha.groupactivity.model.DefaultImageService;
import com.toiukha.groupactivity.security.AuthService;
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
        
        // 顯示當前操作者會員ID
        System.out.println("=== Current User ===");
        System.out.println("URI: /act/member/add");
        System.out.println("Member ID: " + (memberInfo.isLoggedIn() ? memberInfo.getMemId() : "Not logged in"));
        System.out.println("Member Name: " + (memberInfo.isLoggedIn() ? memberInfo.getMemName() : "N/A"));
        System.out.println("===================");
        
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
        
        // TODO: 登入功能完成後，需要統一處理權限驗證
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
        
        // 顯示當前操作者會員ID
        System.out.println("=== Current User ===");
        System.out.println("URI: /act/member/edit/" + id);
        System.out.println("Member ID: " + authService.getCurrentMember(session).getMemId());
        System.out.println("===================");
        
        model.addAttribute("actVo", actVo);
        return "front-end/groupactivity/editAct_ajax";
    }

    //搜尋所有揪團活動
    @GetMapping("/search")
    public String searchActPage(HttpServletRequest request) {
        HttpSession session = request.getSession();
        AuthService.MemberInfo memberInfo = authService.getCurrentMember(session);
        
        // 顯示當前操作者會員ID
        System.out.println("=== Current User ===");
        System.out.println("URI: /act/member/search");
        System.out.println("Member ID: " + (memberInfo.isLoggedIn() ? memberInfo.getMemId() : "Not logged in"));
        System.out.println("Member Name: " + (memberInfo.isLoggedIn() ? memberInfo.getMemName() : "N/A"));
        System.out.println("===================");
        
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

        // 顯示當前操作者會員ID
        System.out.println("=== Current User ===");
        System.out.println("URI: /act/member/view/" + id);
        System.out.println("Member ID: " + (memberInfo.isLoggedIn() ? memberInfo.getMemId() : "Not logged in"));
        System.out.println("Member Name: " + (memberInfo.isLoggedIn() ? memberInfo.getMemName() : "N/A"));
        System.out.println("Is Host: " + isHost + ", Is Participant: " + isParticipant);
        System.out.println("===================");

        return "front-end/groupactivity/listOneAct";
    }

    //我揪的團 - 加入安全驗證
    @GetMapping("/listMy/{hostId}")
    public String listMyAct(@PathVariable Integer hostId, Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        
        // TODO: 登入功能完成後，需要統一處理權限驗證
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
        
        // 顯示當前操作者會員ID
        System.out.println("=== Current User ===");
        System.out.println("URI: /act/member/listMy/" + hostId);
        System.out.println("Member ID: " + authorizedHostId);
        System.out.println("===================");
        
        model.addAttribute("actList", actSvc.getByHost(authorizedHostId));
        model.addAttribute("hostId", authorizedHostId);
        return "front-end/groupactivity/listMyAct";
    }
    
    //我跟的團 - 加入安全驗證
    @GetMapping("/listMyJoin/{memId}")
    public String listMyJoinAct(@PathVariable Integer memId, Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        
        // TODO: 登入功能完成後，需要統一處理權限驗證
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
        
        // 顯示當前操作者會員ID
        System.out.println("=== Current User ===");
        System.out.println("URI: /act/member/listMyJoin/" + memId);
        System.out.println("Member ID: " + authorizedMemId);
        System.out.println("===================");
        
        model.addAttribute("memId", authorizedMemId);
        return "front-end/groupactivity/listMyJoinAct";
    }


    /**
     * 圖片顯示端點 - 直接回傳 byte[] 圖片資料
     * 如果活動沒有圖片，回傳預設圖片
     */
    @GetMapping(value = "/image/{actId}", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@PathVariable Integer actId) {
        ActVO actVo = actSvc.getOneAct(actId);
        if (actVo != null && actVo.getActImg() != null && actVo.getActImg().length > 0) {
            return ResponseEntity.ok(actVo.getActImg());
        } else {
            // 如果沒有圖片，回傳預設圖片
            return ResponseEntity.ok(defaultImageService.getDefaultImage());
        }
    }
}
