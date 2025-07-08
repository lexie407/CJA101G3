package com.toiukha.spot.controller.user;

import com.toiukha.spot.service.SpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/spot/user")
public class SpotUserController {
    
    @Autowired
    private SpotService spotService;
    
    @GetMapping("/detail/{id}")
    public String showSpotDetail(@PathVariable Integer id, Model model) {
        var spot = spotService.findById(id);
        if (spot == null) {
            return "redirect:/404error";
        }
        model.addAttribute("spot", spot);
        model.addAttribute("isFavorited", false);
        return "front-end/spot/detail";
    }
} 