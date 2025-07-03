package com.toiukha.administrant.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.toiukha.administrant.model.AdministrantDTO;
import com.toiukha.administrant.model.AdministrantService;
import com.toiukha.administrant.model.AdministrantVO;
import com.toiukha.administrantauth.model.AdministrantAuthVO;
import com.toiukha.managefunction.model.ManageFunctionService;
import com.toiukha.managefunction.model.ManageFunctionVO;

@Controller
@RequestMapping("/admins")
public class AdministrantController {

	@Autowired
	private AdministrantService administrantService;

	@Autowired
	private ManageFunctionService manageFunctionService;

	// 管理員首頁
	@GetMapping("/dashboard")
	public String showDashboard() {
		return "back-end/administrant/dashboard";	
	}

	// 搜尋頁面
	@GetMapping("/selectPage")
	public String showSelectPage(Model model) {
		model.addAttribute("currentPage", "accounts");
		model.addAttribute("currentPage2", "adminSelect");
		return "back-end/administrant/selectPage";
	}

	// 顯示查詢結果（多筆）—— 單一條件
	@GetMapping("/search")
	public String search(@RequestParam(required = false) String adminId,
			@RequestParam(required = false) String adminName, Model model) {

		List<AdministrantVO> results = new ArrayList<>();

		if (adminId != null && !adminId.isBlank()) {
			// 依 ID 精確查一筆
			if (adminId.matches("\\d+")) {
				AdministrantVO a = administrantService.getById(Integer.valueOf(adminId));
				if (a != null) {
					results.add(a);
				}
			} else {
				// 非數字就跳回查詢頁並帶錯誤訊息
				model.addAttribute("errorMsg", "管理員編號必須是數字");
				return "back-end/administrant/selectPage"; // 查詢表單頁面
			}

		} else if (adminName != null && !adminName.isBlank()) {
			// 依名稱模糊查詢
			results = administrantService.findByNameLike(adminName);
		}

		model.addAttribute("admins", results);
		model.addAttribute("currentPage", "accounts");
		return "back-end/administrant/search";
	}

	@GetMapping("/listAll")
	public String listAll(Model model) {
		// 取出所有
		List<AdministrantVO> admins = administrantService.getAll();

		model.addAttribute("admins", admins);
	    model.addAttribute("currentPage", "accounts");
	    model.addAttribute("currentPage2", "adminList");
		return "back-end/administrant/listAll";
	}

	// 顯示新增表單
	@GetMapping("/add")
	public String showAddForm(Model model) {
		model.addAttribute("form", new AdministrantDTO());
		model.addAttribute("allFunctions", manageFunctionService.getAll());
		model.addAttribute("currentPage", "accounts");
		model.addAttribute("currentPage2", "adminAdd");
		return "back-end/administrant/add";
	}

	@PostMapping("/add")
	public String addAdministrant(@Valid @ModelAttribute("form") AdministrantDTO form, BindingResult result,
			Model model, RedirectAttributes ra) {

		// 1. 帳號唯一性檢查
		if (administrantService.existsByAdminAcc(form.getAdminAcc())) {
			result.rejectValue("adminAcc", "error.form", "帳號已存在，請使用其他帳號");
		}

		// 2. 驗證失敗 → 回到表單，並補入 allFunctions
		if (result.hasErrors()) {
			List<String> errorMsgs = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
			model.addAttribute("errorMsgs", errorMsgs);
			model.addAttribute("allFunctions", manageFunctionService.getAll());
			model.addAttribute("currentPage", "accounts");
			model.addAttribute("currentPage2", "adminAdd");
			return "back-end/administrant/add";
		}

		// 3. DTO → VO
		AdministrantVO vo = new AdministrantVO();
		vo.setAdminAcc(form.getAdminAcc());
		vo.setAdminPwd(form.getAdminPwd());
		vo.setAdminName(form.getAdminName());
		vo.setAdminStatus(form.getAdminStatus());
		vo.setAdminCreatedAt(new Timestamp(System.currentTimeMillis()));

		// 4. 處理功能分配：依 form.getManageFuncIds() 建中介實體集合
		for (Integer funcId : form.getManageFuncIds()) {
			AdministrantAuthVO auth = new AdministrantAuthVO();
			auth.setAdministrant(vo);
			ManageFunctionVO func = manageFunctionService.findById(funcId)
					.orElseThrow(() -> new IllegalArgumentException("功能不存在：" + funcId));
			auth.setManageFunction(func);
			vo.getAuths().add(auth);
		}

		// 5. 呼叫 Service 寫入
		administrantService.create(vo);
		ra.addFlashAttribute("successMsg", "新增成功！");
		return "redirect:/admins/listAll";
	}

	// 顯示編輯頁面
	@GetMapping("/edit")
	public String showEditForm(@RequestParam("adminId") Integer adminId, Model model, RedirectAttributes ra) {

		// 防呆：若無 adminId，就導回查詢頁並顯示錯誤
		if (adminId == null) {
			ra.addFlashAttribute("errorMsg", "必須指定管理員 ID");
			return "redirect:/admins/selectPage";
		}

		// 讀取資料，若找不到也跳回查詢頁
		AdministrantVO vo = administrantService.getById(adminId);
		if (vo == null) {
			ra.addFlashAttribute("errorMsg", "找不到指定的管理員");
			return "redirect:/admins/selectPage";
		}

		// ----- 下面開始改成用 DTO -----
		// 1. 建立 DTO 並把 VO 的資料搬過去
		AdministrantDTO form = new AdministrantDTO();
		form.setAdminId(vo.getAdminId()); 
		form.setAdminAcc(vo.getAdminAcc());
		form.setAdminPwd(vo.getAdminPwd());
		form.setAdminName(vo.getAdminName());
		form.setAdminStatus(vo.getAdminStatus());
		// 把原本的功能授權 ID 全搬到 DTO 的清單
		List<Integer> selected = new ArrayList<>();
		for (AdministrantAuthVO auth : vo.getAuths()) {
			selected.add(auth.getManageFunction().getManageFuncId());
		}
		form.setManageFuncIds(selected);

		// 2. 把 DTO 和 allFunctions 放入 Model
		model.addAttribute("form", form);
		model.addAttribute("allFunctions", manageFunctionService.getAll());
		model.addAttribute("currentPage", "accounts");

		return "back-end/administrant/edit";
	}

	// 處理「編輯管理員」提交

	@PostMapping("/edit")
	public String updateAdministrant(
	        @Valid @ModelAttribute("form") AdministrantDTO form,
	        BindingResult result,
	        Model model,
	        RedirectAttributes ra) {

	    // 1. 驗證失敗 → 回到表單並補 allFunctions
	    if (result.hasErrors()) {
	        List<String> errorMsgs = result.getFieldErrors().stream()
	            .map(FieldError::getDefaultMessage)
	            .toList();
	        model.addAttribute("errorMsgs", errorMsgs);
	        model.addAttribute("allFunctions", manageFunctionService.getAll());
	        model.addAttribute("currentPage", "accounts");
	        return "back-end/administrant/edit";
	    }

	    // 2. 先讀出原本的 VO，從 DTO 拿 adminId
	    AdministrantVO vo = administrantService.getById(form.getAdminId());

	    // 3. 更新基本欄位
	    vo.setAdminPwd(form.getAdminPwd());
	    vo.setAdminName(form.getAdminName());
	    vo.setAdminStatus(form.getAdminStatus());
	    vo.setAdminUpdatedAt(new Timestamp(System.currentTimeMillis()));

	    // 4. 重建中介表關聯
	    vo.getAuths().clear();
	    for (Integer funcId : form.getManageFuncIds()) {
	        AdministrantAuthVO auth = new AdministrantAuthVO();
	        auth.setAdministrant(vo);
	        ManageFunctionVO func = manageFunctionService.findById(funcId)
	            .orElseThrow(() -> new IllegalArgumentException("功能不存在：" + funcId));
	        auth.setManageFunction(func);
	        vo.getAuths().add(auth);
	    }

	    // 5. 呼叫 Service 更新
	    administrantService.update(vo);
	    ra.addFlashAttribute("successMsg", "更新成功！");
	    return "redirect:/admins/listAll";
	}

}
