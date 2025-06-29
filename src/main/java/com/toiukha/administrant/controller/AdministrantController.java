package com.toiukha.administrant.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.toiukha.administrant.model.AdministrantService;


@Controller
@RequestMapping("/admins")
public class AdministrantController {
	
	@Autowired
    private AdministrantService administrantService;

}
