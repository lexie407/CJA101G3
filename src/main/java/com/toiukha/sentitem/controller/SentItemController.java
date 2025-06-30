package com.toiukha.sentitem.controller;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.toiukha.members.model.MembersVO;
import com.toiukha.sentitem.model.SentItemService;
import com.toiukha.sentitem.model.SentItemVO;

import jakarta.servlet.http.HttpSession;
@Controller
@RequestMapping("/member")
public class SentItemController {

	@Autowired
	SentItemService senitemsvc;
	
    @GetMapping("itemList")
    public String memberTicketList(Model model, HttpSession session) {
    	MembersVO member = (MembersVO) session.getAttribute("member");
		 if (member == null) {
		        return "redirect:/login"; // 如果沒有登入，重定向到登入頁
		    }
		int memId = member.getMemId();
		session.setAttribute("memId", memId);
        List<SentItemVO> ticketList = senitemsvc.findByMemId(memId);
        model.addAttribute("ticketList", ticketList);
        model.addAttribute("currentPage", "store");
        model.addAttribute("activeItem", "itemList");
        return "front-end/sentitem/itemList_member";
    }

    @PostMapping("/useSentItem")
    @ResponseBody
    public Map<String, Object> useSentItem(@RequestParam("sentItemId") Integer sentItemId) {
        Map<String, Object> result = new HashMap<>();
        try {
            SentItemVO vo = senitemsvc.getOneSentItem(sentItemId);
            if (vo == null) {
                result.put("success", false);
                result.put("message", "查無此票券");
                return result;
            }
            vo.setItemStatus(1); // 設為已使用
            senitemsvc.updateSentItem(vo);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
