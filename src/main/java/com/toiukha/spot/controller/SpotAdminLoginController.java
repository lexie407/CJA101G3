package com.toiukha.spot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/spot")
public class SpotAdminLoginController {

    @GetMapping("/login")
    public String showLoginPage() {
        return "back-end/spot/login-spot-test";
    }
} 