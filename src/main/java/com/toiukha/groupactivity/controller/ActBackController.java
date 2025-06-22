package com.toiukha.groupactivity.controller;

import com.toiukha.groupactivity.model.ActDTO;
import com.toiukha.groupactivity.model.ActService;
import com.toiukha.groupactivity.model.ActVO;
import com.toiukha.groupactivity.model.DefaultImageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// 後台管理員功能
@Controller
@RequestMapping("/act/admin")
public class ActBackController {

    @Autowired
    ActService actSvc;

    @Autowired
    private DefaultImageService defaultImageService;

    //列出所有的活動
    @GetMapping("/listAllAct")
    public String listAllAct(Model model) {
        model.addAttribute("actList", actSvc.getAll());
        return "back-end/groupactivity/listAllAct";
    }

    //檢視單一活動
    @GetMapping("/viewAct/{actId}")
    public String viewAct(@PathVariable Integer actId, Model model) {
        model.addAttribute("actVo", actSvc.getOneAct(actId));
        return "back-end/groupactivity/viewAct";
    }

    //編輯活動頁面
    @GetMapping("/editAct/{actId}")
    public String editAct(@PathVariable Integer actId, Model model) {
        model.addAttribute("actVo", actSvc.getOneAct(actId));
        return "back-end/groupactivity/editAct";
    }

    //新增活動頁面
    @GetMapping("/addAct")
    public String addActPage(Model model) {
        model.addAttribute("actVo", new ActVO());
        return "back-end/groupactivity/addAct";
    }

    @ModelAttribute("recruitStatusMap")
    public Map<Byte, String> getRecruitStatusMap() {
        Map<Byte, String> statusMap = new HashMap<>();
        statusMap.put((byte)0, "招募中");
        statusMap.put((byte)1, "成團");
        statusMap.put((byte)2, "未成團");
        statusMap.put((byte)3, "團主取消");
        statusMap.put((byte)4, "系統凍結");
        statusMap.put((byte)5, "活動結束");
        return statusMap;
    }

    @PostMapping("/update")
    public String updateAct(@Valid @ModelAttribute("actVo") ActDTO actDto,
                            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        // 如果沒有上傳新圖片，保留原有圖片
        if (file != null && !file.isEmpty()) {
            actDto.setActImg(file.getBytes());
        } else {
            // 取得原有活動資料，保留原有圖片
            ActVO originalAct = actSvc.getOneAct(actDto.getActId());
            if (originalAct != null && originalAct.getActImg() != null && originalAct.getActImg().length > 0) {
                actDto.setActImg(originalAct.getActImg());
            } else {
                // 如果原本就沒有圖片，使用預設圖片
                actDto.setActImg(defaultImageService.getDefaultImage());
            }
        }
        actSvc.updateAct(actDto);
        return "redirect:/act/admin/listAllAct";
    }

    @PostMapping("/insert")
    public String insertAct(@Valid @ModelAttribute("actVo") ActDTO actDto,
                           @RequestParam(value = "upFile", required = false) MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            actDto.setActImg(file.getBytes());
        } else {
            // 如果沒有上傳圖片，使用預設圖片
            actDto.setActImg(defaultImageService.getDefaultImage());
        }
        actSvc.addAct(actDto);
        return "redirect:/act/admin/listAllAct";
    }

    @PostMapping("/delete/{actId}")
    public String deleteAct(@PathVariable Integer actId) {
        actSvc.deleteAct(actId);
        return "redirect:/act/admin/listAllAct";
    }

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

    /** 變更狀態 */
    @PostMapping("/changeStatus")
    public String changeStatus(@RequestParam Integer actId,
                               @RequestParam Byte status,
                               @RequestParam Integer operatorId,
                               @RequestParam(defaultValue = "false") boolean admin,
                               Model model) {
        actSvc.changeStatus(actId, status, operatorId, admin);
        return "redirect:/act/admin/listAllAct";
    }

    // ========================================
    // 測試相關端點 - 開發完成後可移除
    // ========================================

    /**
     * 測試端點 - 建立測試資料
     * 用途：快速建立測試用的活動資料
     * 移除時機：開發完成後
     */
    @GetMapping("/test")
    @ResponseBody
    public Map<String, Object> createTestData() {
        try {
            // 建立測試活動
            ActDTO testAct = new ActDTO();
            testAct.setActName("測試活動 " + System.currentTimeMillis());
            testAct.setActDesc("這是一個測試活動的內容");
            testAct.setItnId(1); // 假設行程ID為1
            testAct.setHostId(1); // 假設團主ID為1
            testAct.setSignupStart(LocalDateTime.now());
            testAct.setSignupEnd(LocalDateTime.now().plusDays(1));
            testAct.setActStart(LocalDateTime.now().plusDays(1));
            testAct.setActEnd(LocalDateTime.now().plusDays(2));
            testAct.setMaxCap(10);
            testAct.setRecruitStatus((byte) 0);
            testAct.setIsPublic((byte) 1);
            testAct.setAllowCancel((byte) 1);

            // 建立測試圖片 (1x1 像素的透明 PNG)
            String testImageBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
            testAct.setActImgBase64(testImageBase64);

            actSvc.addAct(testAct);

            return Map.of(
                "success", true,
                "message", "測試資料建立成功",
                "timestamp", LocalDateTime.now()
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            );
        }
    }

    /**
     * 測試端點 - 驗證編輯功能
     * 用途：檢查編輯頁面是否能正確載入活動資料
     * 移除時機：開發完成後
     */
    @GetMapping("/test-edit/{actId}")
    @ResponseBody
    public Map<String, Object> testEditFunction(@PathVariable Integer actId) {
        try {
            ActVO actVo = actSvc.getOneAct(actId);
            if (actVo != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "活動資料載入成功");
                result.put("actId", actVo.getActId());
                result.put("actName", actVo.getActName());
                result.put("actDesc", actVo.getActDesc());
                result.put("itnId", actVo.getItnId());
                result.put("hostId", actVo.getHostId());
                result.put("signupStart", actVo.getSignupStart());
                result.put("signupEnd", actVo.getSignupEnd());
                result.put("maxCap", actVo.getMaxCap());
                result.put("actStart", actVo.getActStart());
                result.put("actEnd", actVo.getActEnd());
                result.put("isPublic", actVo.getIsPublic());
                result.put("allowCancel", actVo.getAllowCancel());
                result.put("recruitStatus", actVo.getRecruitStatus());
                result.put("hasImage", actVo.getActImg() != null && actVo.getActImg().length > 0);
                result.put("imageSize", actVo.getActImg() != null ? actVo.getActImg().length : 0);
                result.put("editUrl", "/act/admin/editAct/" + actId);
                result.put("timestamp", LocalDateTime.now());
                return result;
            } else {
                return Map.of(
                    "success", false,
                    "error", "找不到活動 ID: " + actId,
                    "timestamp", LocalDateTime.now()
                );
            }
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            );
        }
    }
}
