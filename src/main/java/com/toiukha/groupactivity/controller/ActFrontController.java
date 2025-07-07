package com.toiukha.groupactivity.controller;

import com.toiukha.groupactivity.model.ActDTO;
import com.toiukha.groupactivity.model.ActTag;
import com.toiukha.groupactivity.model.ActVO;
import com.toiukha.groupactivity.security.AuthService;
import com.toiukha.groupactivity.service.ActService;
import com.toiukha.groupactivity.service.DefaultImageService;
import com.toiukha.itinerary.model.ItineraryVO;
import com.toiukha.participant.model.PartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.Map;

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
    private AuthService authSvc;

    //新增活動
    @GetMapping("/add")
    public String addActPage(@RequestParam(value = "itnId", required = false) Integer itnId,
                             HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        AuthService.MemberInfo memberInfo = authSvc.getCurrentMember(session);
        if (!memberInfo.isLoggedIn()) {
            return "redirect:/members/login";
        }
        model.addAttribute("currentMemberId", memberInfo.getMemId());
        model.addAttribute("itnId", itnId);
        return "front-end/groupactivity/addAct_ajax";
    }

    //編輯活動
    @GetMapping("/edit/{actId}")
    public String editAct(@PathVariable Integer actId, Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        
        // 先取得活動資料
        ActVO actVo = actSvc.getOneAct(actId);
        if (actVo == null) {
            // 活動不存在，重定向到搜尋頁面
            return "redirect:/act/member/search";
        }

        // 檢查活動是否被凍結
        if (actVo.getRecruitStatus() == 4) {
            // 活動被凍結，重定向到活動詳情頁面
            return "redirect:/act/member/view/" + actId + "?error=frozen";
        }
        
        // 安全驗證：檢查會員是否有權限編輯此活動
        if (!authSvc.canModifyActivity(session, actVo.getHostId())) {
            // 無權限：重定向到活動詳情頁面或登入頁
            AuthService.MemberInfo memberInfo = authSvc.getCurrentMember(session);
            
            if (!memberInfo.isLoggedIn()) {
                // 未登入：導向登入頁面
                return "redirect:/members/login";
            } else {
                // 已登入但無權限：重定向到活動詳情頁面
                return "redirect:/act/member/view/" + actId;
            }
        }
        
        // 將 ActVO 轉換為 ActDTO，並從 Redis 取得標籤資訊
        ActDTO actDto = new ActDTO();
        actDto.setActId(actVo.getActId());
        actDto.setActName(actVo.getActName());
        actDto.setActDesc(actVo.getActDesc());
        actDto.setActImg(actVo.getActImg());
        actDto.setItnId(actVo.getItnId());
        actDto.setHostId(actVo.getHostId());
        actDto.setSignupStart(actVo.getSignupStart());
        actDto.setSignupEnd(actVo.getSignupEnd());
        actDto.setMaxCap(actVo.getMaxCap());
        actDto.setSignupCnt(actVo.getSignupCnt());
        actDto.setActStart(actVo.getActStart());
        actDto.setActEnd(actVo.getActEnd());
        actDto.setIsPublic(actVo.getIsPublic());
        actDto.setAllowCancel(actVo.getAllowCancel());
        actDto.setRecruitStatus(actVo.getRecruitStatus());
        // 新增：查詢行程名稱
        ItineraryVO itinerary = actSvc.getItineraryById(actVo.getItnId());
        actDto.setItnName(itinerary != null ? itinerary.getItnName() : "");
        
        // 從 Redis 取得活動標籤並設定到 DTO 中
        Map<String, ActTag> tags = actSvc.getActTags(actId);
        if (tags.containsKey("type")) {
            actDto.setActType(tags.get("type").name());
        }
        if (tags.containsKey("city")) {
            actDto.setActCity(tags.get("city").name());
        }
        
        model.addAttribute("actVo", actDto);
        model.addAttribute("currentMemberId", actVo.getHostId());
        return "front-end/groupactivity/editAct_ajax";
    }

    //搜尋所有揪團活動
    @GetMapping("/search")
    public String searchActPage(ModelMap model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        AuthService.MemberInfo memberInfo = authSvc.getCurrentMember(session);
        model.addAttribute("currentPage", "groups");
        model.addAttribute("currentMemberId", memberInfo.isLoggedIn() ? memberInfo.getMemId() : null);
        return "front-end/groupactivity/searchAct";
    }

    //檢視活動詳情
    @GetMapping("/view/{actId}")
    public String viewAct(@PathVariable Integer actId, Model model, HttpServletRequest request) {
        ActVO actVo = actSvc.getOneAct(actId);
        model.addAttribute("actVo", actVo);
        model.addAttribute("currentPage", "groups");

        boolean isParticipant = false;
        boolean isHost = false;

        HttpSession session = request.getSession();
        AuthService.MemberInfo memberInfo = authSvc.getCurrentMember(session);

        if (memberInfo.isLoggedIn()) {
            Integer memId = memberInfo.getMemId();
            
            // 檢查是否為團主
            if (actVo.getHostId().equals(memId)) {
                isHost = true;
            } else {
                // 如果不是團主，再檢查是否為團員
                isParticipant = partSvc.getParticipants(actId).contains(memId);
            }
        }
        
        model.addAttribute("isHost", isHost);
        model.addAttribute("isParticipant", isParticipant);
        // 新增：將登入會員ID傳入 Thymeleaf
        if (memberInfo.isLoggedIn()) {
            model.addAttribute("currentMemberId", memberInfo.getMemId());
        } else {
            model.addAttribute("currentMemberId", null);
        }
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
        Integer authorizedHostId = authSvc.getAuthorizedHostId(session, hostId);
        if (authorizedHostId == null) {
            AuthService.MemberInfo memberInfo = authSvc.getCurrentMember(session);
            if (!memberInfo.isLoggedIn()) {
                return "redirect:/members/login";
            } else {
                return "redirect:/act/member/listMy/" + memberInfo.getMemId();
            }
        }
        model.addAttribute("actList", actSvc.getByHost(authorizedHostId));
        model.addAttribute("hostId", authorizedHostId);
        model.addAttribute("currentPage", "groups");
        AuthService.MemberInfo memberInfo = authSvc.getCurrentMember(session);
        model.addAttribute("currentMemberId", memberInfo.isLoggedIn() ? memberInfo.getMemId() : null);
        return "front-end/groupactivity/listMyAct";
    }
    
    //我跟的團 - 加入安全驗證
    @GetMapping("/listMyJoin/{memId}")
    public String listMyJoinAct(@PathVariable Integer memId, Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        // 安全驗證：檢查會員是否有權限查看指定的參加活動列表
        Integer authorizedMemId = authSvc.getAuthorizedMemId(session, memId);
        if (authorizedMemId == null) {
            // 無權限：重定向到自己的參加活動列表或登入頁
            AuthService.MemberInfo memberInfo = authSvc.getCurrentMember(session);
            if (!memberInfo.isLoggedIn()) {
                // 未登入：導向登入頁面
                return "redirect:/members/login";
            } else {
                // 已登入但無權限：重定向到自己的參加活動列表
                return "redirect:/act/member/listMyJoin/" + memberInfo.getMemId();
            }
        }
        //memId:用來讓後端查詢符合身分是參團者的的活動getJoinedActsAsCard()
        AuthService.MemberInfo memberInfo = authSvc.getCurrentMember(session);
        model.addAttribute("memId", authorizedMemId);
        model.addAttribute("currentPage", "groups");
        // currentMemberId:用來讓前端判斷用戶身分，控制UI顯示的內容
        model.addAttribute("currentMemberId", memberInfo.isLoggedIn() ? memberInfo.getMemId() : null);
        return "front-end/groupactivity/listMyJoinAct";
    }

    @GetMapping("/listOneAct")
    public String listOneAct(@RequestParam("actId") Integer actId, @RequestParam(value = "atPage", required = false, defaultValue = "") String atPage, Model model, HttpServletRequest request) {
        ActVO actVo = actSvc.getOneAct(actId);
        model.addAttribute("actVo", actVo);
        model.addAttribute("currentPage", "groups");

        boolean isParticipant = false;
        boolean isHost = false;

        HttpSession session = request.getSession();
        AuthService.MemberInfo memberInfo = authSvc.getCurrentMember(session);

        if (memberInfo.isLoggedIn()) {
            Integer memId = memberInfo.getMemId();
            if (actVo.getHostId().equals(memId)) {
                isHost = true;
            } else {
                isParticipant = partSvc.getParticipants(actId).contains(memId);
            }
        }
        model.addAttribute("isHost", isHost);
        model.addAttribute("isParticipant", isParticipant);
        if (memberInfo.isLoggedIn()) {
            model.addAttribute("currentMemberId", memberInfo.getMemId());
        } else {
            model.addAttribute("currentMemberId", null);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        model.addAttribute("formattedSignupStart", actVo.getSignupStart().format(formatter));
        model.addAttribute("formattedSignupEnd", actVo.getSignupEnd().format(formatter));
        model.addAttribute("formattedActStart", actVo.getActStart().format(formatter));
        model.addAttribute("formattedActEnd", actVo.getActEnd().format(formatter));
        model.addAttribute("atPage", atPage);
        return "front-end/groupactivity/listOneAct";
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
