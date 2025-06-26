package com.toiukha.groupactivity.controller;

import com.toiukha.groupactivity.model.ActDTO;
import com.toiukha.groupactivity.model.ActService;
import com.toiukha.groupactivity.model.ActVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
/**
 * 揪團模組 - 後台管理控制器
 * <p>
 * 這個控制器將採用傳統的 Servlet 風格進行實作，以供學習和比較。
 * 它會直接操作 HttpServletRequest 和 HttpServletResponse (在需要時)，
 * 並使用 Model-View-Controller 的經典模式，將資料傳遞給 Thymeleaf 樣板。
 * 
 * [Deprecated] 本檔案內的圖片顯示API（如 /admin/act/getImage）已被統一的 /api/act/image/{actId} 取代。
 * 為相容舊版頁面暫時保留，後續可安全移除。
 * 請前端一律改用 /api/act/image/{actId}。
 */
@Controller
@RequestMapping("/admin/act")
public class ActAdminController {

    @Autowired
    private ActService actService;

    /**
     * 處理查詢所有活動的請求，並導向後台列表頁面。
     *
     * @param model Spring MVC 的 Model 物件，用於將資料傳遞給視圖。
     * @return 視圖的邏輯名稱 (templates 路徑下的 html 檔案)。
     */
    @GetMapping("/listAllAct")
    public String listAllActs(Model model, HttpSession session) {
        List<ActVO> actList = actService.getAll();
        
        model.addAttribute("actList", actList);
        List<String> formattedSignupStartDates = actList.stream()
            .map(ActVO::getFormattedSignupStart)
            .collect(Collectors.toList());
        List<String> formattedSignupEndDates = actList.stream()
            .map(ActVO::getFormattedSignupEnd)
            .collect(Collectors.toList());
        List<String> formattedActStartDates = actList.stream()
            .map(ActVO::getFormattedActStart)
            .collect(Collectors.toList());
        List<String> formattedActEndDates = actList.stream()
            .map(ActVO::getFormattedActEnd)
            .collect(Collectors.toList());

        model.addAttribute("formattedSignupStartDates", formattedSignupStartDates);
        model.addAttribute("formattedSignupEndDates", formattedSignupEndDates);
        model.addAttribute("formattedActStartDates", formattedActStartDates);
        model.addAttribute("formattedActEndDates", formattedActEndDates);

        model.addAttribute("recruitStatusMap", getRecruitStatusMap());

        return "act/admin-listAllAct";
    }

    /**
     * 傳統風格的圖片讀取方法。
     * 直接操作 HttpServletResponse 來回傳圖片的 byte[] 資料。
     *
     * @param actId 活動 ID
     * @param res   HttpServletResponse
     * @throws IOException
     */
    @GetMapping("/getImage")
    public void getImage(@RequestParam Integer actId, HttpServletResponse res) throws IOException {
        res.setContentType("image/jpeg");
        ActVO act = actService.getOneAct(actId);
        if (act != null && act.getActImg() != null) {
            try (OutputStream out = res.getOutputStream()) {
                out.write(act.getActImg());
            }
        }
    }

    // ================== 新增功能 ==================
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("actVO", new ActVO());
        model.addAttribute("recruitStatusMap", getRecruitStatusMap());
        return "act/admin-addAct";
    }

    @PostMapping("/insert")
    public String insertAct(
        @ModelAttribute ActVO actVO,
        @RequestParam("actImgFile") MultipartFile actImgFile,
        RedirectAttributes redirectAttributes)
        throws IOException {

        if (actImgFile != null && !actImgFile.isEmpty()) {
            actVO.setActImg(actImgFile.getBytes());
        }
        
        // VO to DTO Conversion
        ActDTO actDTO = new ActDTO();
        actDTO.setActName(actVO.getActName());
        actDTO.setActDesc(actVO.getActDesc());
        actDTO.setActImg(actVO.getActImg());
        actDTO.setItnId(actVO.getItnId());
        actDTO.setHostId(actVO.getHostId());
        actDTO.setSignupStart(actVO.getSignupStart());
        actDTO.setSignupEnd(actVO.getSignupEnd());
        actDTO.setActStart(actVO.getActStart());
        actDTO.setActEnd(actVO.getActEnd());
        actDTO.setMaxCap(actVO.getMaxCap());
        actDTO.setRecruitStatus(actVO.getRecruitStatus());
        actDTO.setIsPublic(actVO.getIsPublic());
        actDTO.setAllowCancel(actVO.getAllowCancel());

        actService.addAct(actDTO);

        redirectAttributes.addFlashAttribute(
            "successMessage", "活動「" + actVO.getActName() + "」新增成功！");
        return "redirect:/admin/act/listAllAct";
    }

    // ================== 編輯功能 ==================
    @GetMapping("/edit/{actId}")
    public String showEditForm(@PathVariable Integer actId, Model model) {
        ActVO actVO = actService.getOneAct(actId);
        model.addAttribute("actVO", actVO);
        model.addAttribute("recruitStatusMap", getRecruitStatusMap());
        return "act/admin-editAct";
    }

    @PostMapping("/update")
    public String updateAct(
        @ModelAttribute ActVO actVO,
        @RequestParam("actImgFile") MultipartFile actImgFile,
        RedirectAttributes redirectAttributes)
        throws IOException {

        // 檢查是否有上傳新圖片
        if (actImgFile != null && !actImgFile.isEmpty()) {
            actVO.setActImg(actImgFile.getBytes());
        } else {
            // 如果沒有上傳，則從資料庫中讀取舊的圖片並設置回去，避免圖片被清空
            ActVO existingAct = actService.getOneAct(actVO.getActId());
            if (existingAct != null) {
                actVO.setActImg(existingAct.getActImg());
            }
        }

        // VO to DTO Conversion
        ActDTO actDTO = new ActDTO();
        actDTO.setActId(actVO.getActId());
        actDTO.setActName(actVO.getActName());
        actDTO.setActDesc(actVO.getActDesc());
        actDTO.setActImg(actVO.getActImg());
        actDTO.setItnId(actVO.getItnId());
        actDTO.setHostId(actVO.getHostId());
        actDTO.setSignupStart(actVO.getSignupStart());
        actDTO.setSignupEnd(actVO.getSignupEnd());
        actDTO.setActStart(actVO.getActStart());
        actDTO.setActEnd(actVO.getActEnd());
        actDTO.setSignupCnt(actVO.getSignupCnt());
        actDTO.setMaxCap(actVO.getMaxCap());
        actDTO.setRecruitStatus(actVO.getRecruitStatus());
        actDTO.setIsPublic(actVO.getIsPublic());
        actDTO.setAllowCancel(actVO.getAllowCancel());

        actService.updateAct(actDTO);

        redirectAttributes.addFlashAttribute(
            "successMessage", "活動編號 " + actVO.getActId() + " 更新成功！");
        return "redirect:/admin/act/listAllAct";
    }

    // ================== 刪除功能 ==================
    @PostMapping("/delete/{actId}")
    public String deleteAct(@PathVariable Integer actId, RedirectAttributes redirectAttributes) {
        actService.deleteAct(actId);
        redirectAttributes.addFlashAttribute("successMessage", "活動編號 " + actId + " 已成功刪除。");
        return "redirect:/admin/act/listAllAct";
    }

    @ModelAttribute("recruitStatusMap")
    public Map<Byte, String> getRecruitStatusMap() {
        Map<Byte, String> statusMap = new LinkedHashMap<>();
        statusMap.put((byte) 0, "招募中");
        statusMap.put((byte) 1, "已成團");
        statusMap.put((byte) 2, "未成團");
        statusMap.put((byte) 3, "團主取消");
        statusMap.put((byte) 4, "系統凍結");
        statusMap.put((byte) 5, "活動結束");
        return statusMap;
    }
} 