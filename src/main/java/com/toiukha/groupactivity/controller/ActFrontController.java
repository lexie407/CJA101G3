package com.toiukha.groupactivity.controller;

import com.toiukha.groupactivity.model.ActService;
import com.toiukha.groupactivity.model.ActVO;
import com.toiukha.groupactivity.model.DefaultImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/act/member")
public class ActFrontController {

    @Autowired
    private ActService actSvc;
    
    @Autowired
    private DefaultImageService defaultImageService;

    @GetMapping("/listMy/{hostId}")
    public String listMyAct(@PathVariable Integer hostId, Model model) {
        model.addAttribute("actList", actSvc.getByHost(hostId));
        model.addAttribute("hostId", hostId); // 供 JS 取得會員編號
        return "front-end/groupactivity/listMyAct";
    }

    @GetMapping("/add")
    public String addActPage() {
        return "front-end/groupactivity/addAct_ajax";
    }

    @GetMapping("/view/{id}")
    public String viewAct(@PathVariable Integer id, Model model) {
        model.addAttribute("actVo", actSvc.getOneAct(id));
        return "front-end/groupactivity/listOneAct";
    }

    @GetMapping("/search")
    public String searchActPage() {
        return "front-end/groupactivity/searchAct";
    }

    @GetMapping("/edit/{id}")
    public String editAct(@PathVariable Integer id, Model model) {
        model.addAttribute("actVo", actSvc.getOneAct(id));
        return "front-end/groupactivity/editAct_ajax";
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
