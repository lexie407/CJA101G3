package com.toiukha.groupactivity.controller;

import com.toiukha.groupactivity.model.ActDTO;
import com.toiukha.groupactivity.model.ActStatus;
import com.toiukha.groupactivity.model.ActVO;
import com.toiukha.groupactivity.service.ActService;
import com.toiukha.groupactivity.service.ActHandlerService;
import com.toiukha.groupactivity.service.DefaultImageService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * 後端轉送用controller
 */

// 後台管理員功能
@Controller
@RequestMapping("/act/admin")
public class ActBackController {

        @Autowired
    ActService actSvc;
    
    @Autowired
    private DefaultImageService defaultImageService;
    
    @Autowired
    private ActHandlerService actHandlerSvc;

    @ModelAttribute("recruitStatusMap")
    public Map<Byte, String> getRecruitStatusMap() {
        return ActStatus.getStatusMap();
    }


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

    //新增活動頁面
    @GetMapping("/addAct")
    public String addActPage(Model model) {
        model.addAttribute("actVo", new ActVO());
        return "back-end/groupactivity/addAct";
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
//    @PostMapping("/insert")
//    public String insertAct(@Valid @ModelAttribute("actVo") ActDTO actDto,
//                            BindingResult bindingResult,
//                            @RequestParam(value = "upFile", required = false) MultipartFile file,
//                            Model model) throws IOException {
//        if (bindingResult.hasErrors()) {
//            // 保持畫面已填欄位
//            model.addAttribute("actVo", actDto);
//            return "back-end/groupactivity/addAct"; // 回到新增畫面，顯示所有錯誤
//        }
//        if (file != null && !file.isEmpty()) {
//            actDto.setActImg(file.getBytes());
//        } else {
//            actDto.setActImg(defaultImageService.getDefaultImage());
//        }
//        actSvc.addAct(actDto);
//        return "redirect:/act/admin/listAllAct";
//    }

    //編輯活動頁面
    @GetMapping("/editAct/{actId}")
    public String editAct(@PathVariable Integer actId, Model model) {
        ActVO actVo = actSvc.getOneAct(actId);
        
        // 檢查活動是否被凍結
        if (actVo != null && actVo.getRecruitStatus() == 4) {
            // 活動被凍結，重定向到活動列表頁面
            return "redirect:/act/admin/listAllAct?error=frozen";
        }
        
        model.addAttribute("actVo", actVo);
        return "back-end/groupactivity/editAct";
    }

    @PostMapping("/delete/{actId}")
    public String deleteAct(@PathVariable Integer actId) {
        actSvc.deleteAct(actId);
        return "redirect:/act/admin/listAllAct";
    }

//    @PostMapping("/update")
//    public String updateAct(@Valid @ModelAttribute("actVo") ActDTO actDto,
//                            @RequestParam(value = "upFile", required = false) MultipartFile file) throws IOException {
//        // 如果沒有上傳新圖片，保留原有圖片
//        if (file != null && !file.isEmpty()) {
//            actDto.setActImg(file.getBytes());
//        } else {
//            // 取得原有活動資料，保留原有圖片
//            ActVO originalAct = actSvc.getOneAct(actDto.getActId());
//            if (originalAct != null && originalAct.getActImg() != null && originalAct.getActImg().length > 0) {
//                actDto.setActImg(originalAct.getActImg());
//            } else {
//                // 如果原本就沒有圖片，使用預設圖片
//                actDto.setActImg(defaultImageService.getDefaultImage());
//            }
//        }
//        actSvc.updateAct(actDto);
//        return "redirect:/act/admin/listAllAct";
//    }

    @PostMapping("/update")
    public String updateAct(
            @Valid @ModelAttribute("actVo") ActDTO actDto,
            BindingResult bindingResult,
            @RequestParam(value = "upFile", required = false) MultipartFile file,
            Model model
    ) throws IOException {
        if (bindingResult.hasErrors()) {
            // 保留原有圖片顯示
            ActVO originalAct = actSvc.getOneAct(actDto.getActId());
            if (originalAct != null) {
                actDto.setActImg(originalAct.getActImg());
            }
            model.addAttribute("actVo", actDto);
            return "back-end/groupactivity/editAct";
        }
        
        // 處理圖片
        if (file != null && !file.isEmpty()) {
            actDto.setActImg(file.getBytes());
        } else {
            ActVO originalAct = actSvc.getOneAct(actDto.getActId());
            if (originalAct != null && originalAct.getActImg() != null && originalAct.getActImg().length > 0) {
                actDto.setActImg(originalAct.getActImg());
            } else {
                actDto.setActImg(defaultImageService.getDefaultImage());
            }
        }
        
        // 使用 ActHandlerService 處理管理員活動更新，可以完整設置所有欄位
        ActVO actVo = actDto.toVO();
        actHandlerSvc.handleActivityUpdateByAdmin(actVo);
        
        return "redirect:/act/admin/listAllAct";
    }

    // 活動圖片載入
//    @GetMapping("/DBGifReader")
//    public void getActImage(
//            @RequestParam("actId") Integer actId,
//            HttpServletResponse response) throws IOException {
//
//        ActVO act = actSvc.getOneAct(actId);
//        byte[] imageBytes = act.getActImg();
//
//        response.setContentType("image/jpeg");
//        ServletOutputStream out = response.getOutputStream();
//
//        if (imageBytes != null && imageBytes.length > 0) {
//            out.write(imageBytes);
//        } else {
//            // 使用預設圖片
//            byte[] defaultImage = defaultImageService.getDefaultImage();
//            out.write(defaultImage);
//        }
//
//        out.flush();
//        out.close();
//    }

    // 活動圖片載入
    @GetMapping("/DBGifReader")
    public void getActImage(
            @RequestParam("actId") Integer actId,
            HttpServletResponse response) throws IOException {

        ServletOutputStream out = null;
        try {
            ActVO act = actSvc.getOneAct(actId);
            byte[] imageBytes = null;

            if (act != null) {
                imageBytes = act.getActImg();
            }

            response.setContentType("image/jpeg");
            out = response.getOutputStream();

            if (imageBytes != null && imageBytes.length > 0) {
                out.write(imageBytes);
            } else {
                // 使用預設圖片
                try {
                    byte[] defaultImage = defaultImageService.getDefaultImage();
                    out.write(defaultImage);
                } catch (Exception e) {
                    System.err.println("載入預設圖片失敗: " + e.getMessage());
                    // 如果連預設圖片都無法載入，返回一個最小的透明PNG
                    String emptyPng = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
                    byte[] emptyImage = java.util.Base64.getDecoder().decode(emptyPng);
                    response.setContentType("image/png");
                    out.write(emptyImage);
                }
            }
        } catch (Exception e) {
            System.err.println("圖片載入錯誤 (actId: " + actId + "): " + e.getMessage());
            // 確保回應正確關閉
            if (out != null) {
                try {
                    String emptyPng = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
                    byte[] emptyImage = java.util.Base64.getDecoder().decode(emptyPng);
                    response.setContentType("image/png");
                    out.write(emptyImage);
                } catch (Exception ex) {
                    System.err.println("寫入空白圖片失敗: " + ex.getMessage());
                }
            }
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    System.err.println("關閉輸出流失敗: " + e.getMessage());
                }
            }
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

    // ---未使用--- 測試端點 - 建立測試資料
    /**
     * 測試端點 - 建立測試資料
     * 用途：快速建立測試用的活動資料
     * 移除時機：開發完成後
     */
    @GetMapping("/test")
    @ResponseBody
    public Map<String, Object> createTestData() {
        try {
            // 動態取得第一個公開行程，如果沒有則使用null
            Integer testItnId = null;
            List<com.toiukha.itinerary.model.ItineraryVO> publicItineraries = actSvc.getPublicItineraries();
            if (!publicItineraries.isEmpty()) {
                testItnId = publicItineraries.get(0).getItnId();
            }
            
            // 建立測試活動
            ActDTO testAct = new ActDTO();
            testAct.setActName("測試活動 " + System.currentTimeMillis());
            testAct.setActDesc("這是一個測試活動的內容");
            testAct.setItnId(testItnId);
            testAct.setHostId(1); // 假設團主ID為1
            testAct.setSignupStart(LocalDateTime.now());
            testAct.setSignupEnd(LocalDateTime.now().plusDays(1));
            testAct.setActStart(LocalDateTime.now().plusDays(1));
            testAct.setActEnd(LocalDateTime.now().plusDays(2));
            testAct.setMaxCap(10);
            testAct.setRecruitStatusEnum(ActStatus.OPEN);
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

    // ---未使用--- 測試端點 - 驗證編輯功能
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
