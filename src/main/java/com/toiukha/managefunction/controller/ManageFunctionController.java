package com.toiukha.managefunction.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.toiukha.managefunction.model.ManageFunctionService;
import com.toiukha.managefunction.model.ManageFunctionVO;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/manageFunction")
public class ManageFunctionController {

	@Autowired
	private ManageFunctionService manageFunctionService;

	// 顯示搜尋／選擇頁面
	@GetMapping("/selectPage")
	public String showSelectPage(Model model) {
		// 一定要把所有功能丟給前端下拉
		model.addAttribute("manageFunctions", manageFunctionService.getAll());
		return "back-end/manageFunction/selectPage";
	}

	@GetMapping("/search")
	public String search(@RequestParam(name = "manageFuncId", required = false) Integer manageFuncId, Model model) {

		// 一定要先補下拉選單資料
		model.addAttribute("manageFunctions", manageFunctionService.getAll());

		if (manageFuncId == null) {
			model.addAttribute("errorMsg", "請先選擇一個管理功能");
			return "back-end/manageFunction/selectPage";
		}

		Optional<ManageFunctionVO> opt = manageFunctionService.findById(manageFuncId);

		model.addAttribute("selectedFunction", opt.get());
		return "back-end/manageFunction/search";
	}

	// 列出所有管理功能（對應 listAll.html）
	@GetMapping("/listAll")
	public String listAll(Model model) {
		model.addAttribute("manageFunctions", manageFunctionService.getAll());
		return "back-end/manageFunction/listAll";
	}

	// 顯示新增功能表單
	@GetMapping("/add")
	public String showAddForm(Model model) {
		model.addAttribute("manageFunctionVO", new ManageFunctionVO());
		return "back-end/manageFunction/add";
	}

	// 處理新增功能提交
	@PostMapping("/add")
	public String processAdd(@Valid @ModelAttribute("manageFunctionVO") ManageFunctionVO vo, BindingResult result,
			Model model, RedirectAttributes ra) {

		// 1. 驗證失敗 → 收集錯誤並回到 add 頁
		if (result.hasErrors()) {
			List<String> errorMsgs = new ArrayList<>();
			for (FieldError fe : result.getFieldErrors()) {
				errorMsgs.add(fe.getDefaultMessage());
			}
			model.addAttribute("errorMsgs", errorMsgs);
			return "back-end/manageFunction/add";
		}

		// 2. 呼叫 Service 寫入
		manageFunctionService.create(vo);

		// 3. 新增成功後導回列表，並帶一個 flash 視窗訊息
		ra.addFlashAttribute("successMsg", "功能新增成功！");
		return "redirect:/manageFunction/listAll";
	}

	// GET: 顯示「編輯功能」表單
	@GetMapping("/edit")
	public String showEditForm(@RequestParam("manageFuncId") Integer manageFuncId, Model model, RedirectAttributes ra) {

		// 1. 防呆：若參數不存在或找不到該筆，就跳回列表並吐錯
		if (manageFuncId == null) {
			ra.addFlashAttribute("errorMsg", "必須指定要編輯的功能編號");
			return "redirect:/manageFunction/listAll";
		}

		Optional<ManageFunctionVO> opt = manageFunctionService.findById(manageFuncId);
		if (opt.isEmpty()) {
			ra.addFlashAttribute("errorMsg", "找不到指定的管理功能");
			return "redirect:/manageFunction/listAll";
		}

		// 2. 找得到：把 VO 放到 Model，顯示表單
		model.addAttribute("manageFunctionVO", opt.get());
		return "back-end/manageFunction/edit";
	}

	// POST: 處理「編輯功能」送出
	@PostMapping("/edit")
	public String processEdit(@Valid @ModelAttribute("manageFunctionVO") ManageFunctionVO vo, BindingResult result,
			Model model, RedirectAttributes ra) {

		// 1. 驗證失敗 → 收集錯誤並回到 edit 頁
		if (result.hasErrors()) {
			List<String> errorMsgs = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
			model.addAttribute("errorMsgs", errorMsgs);
			return "back-end/manageFunction/edit";
		}

		// 2. 呼叫 Service 更新
		manageFunctionService.update(vo);

		// 3. 成功後跳回列表
		ra.addFlashAttribute("successMsg", "功能已更新");
		return "redirect:/manageFunction/listAll";
	}

}
