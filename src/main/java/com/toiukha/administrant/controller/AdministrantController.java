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

import com.toiukha.administrant.model.AdministrantService;
import com.toiukha.administrant.model.AdministrantVO;

@Controller
@RequestMapping("/admins")
public class AdministrantController {

	@Autowired
	private AdministrantService administrantService;

	// 商家首頁
	@GetMapping("/dashboard")
	public String showDashboard() {
		return "back-end/administrant/dashboard";
	}

	// 搜尋頁面
	@GetMapping("/selectPage")
	public String showSelectPage() {
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
//		model.addAttribute("currentPage", "admins");
		return "back-end/administrant/search";
	}
	
	@GetMapping("/listAll")
	public String listAll(Model model) {
	    // 取出所有
	    List<AdministrantVO> admins = administrantService.getAll();

	    model.addAttribute("admins", admins);
//	    model.addAttribute("currentPage", "admins");  
	    return "back-end/administrant/listAll";
	}
	
	// 顯示新增表單
	@GetMapping("/add")
	public String showAddForm(Model model) {
	    model.addAttribute("administrantVO", new AdministrantVO());
	    model.addAttribute("currentPage", "add");
	    return "back-end/administrant/add";
	}

	@PostMapping("/add")
	public String addAdministrant(
	        @Valid @ModelAttribute("administrantVO") AdministrantVO vo,
	        BindingResult result,
	        Model model,
	        RedirectAttributes ra) {

	    // 1. 帳號唯一性檢查
	    if (administrantService.existsByAdminAcc(vo.getAdminAcc())) {
	        result.rejectValue("adminAcc", "error.administrantVO", "帳號已存在，請使用其他帳號");
	    }

	    // 2. 驗證失敗 → 收集錯誤並回到 add 頁
	    if (result.hasErrors()) {
	        List<String> errorMsgs = new ArrayList<>();
	        for (FieldError fe : result.getFieldErrors()) {
	            errorMsgs.add(fe.getDefaultMessage());
	        }
	        model.addAttribute("errorMsgs", errorMsgs);
	        return "back-end/administrant/add";
	    }

	    // 3. 設定建立時間與初始狀態（0＝啟用）
	    vo.setAdminCreatedAt(new Timestamp(System.currentTimeMillis()));
	    vo.setAdminStatus((byte)0);

	    // 4. 呼叫 Service 寫入
	    administrantService.create(vo);
	    ra.addFlashAttribute("successMsg", "新增成功！");
	    return "redirect:/admins/listAll";
	}
	
	//顯示編輯頁面
	@GetMapping("/edit")
    public String showEditForm(
            @RequestParam("adminId") Integer adminId,  
            Model model,
            RedirectAttributes ra) {

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

        // 正常：把 VO 放到 Model，顯示表單
        model.addAttribute("administrantVO", vo);
        return "back-end/administrant/edit";
    }

     //處理「編輯管理員」提交
     
    @PostMapping("/edit")
    public String updateAdministrant(
            @Valid @ModelAttribute("administrantVO") AdministrantVO vo,  // 綁定並驗證
            BindingResult result,                                        // 驗證結果
            Model model,                                                 // 回填錯誤用
            RedirectAttributes ra) {                                     // 需時顯示錯誤才用

        // 驗證失敗 → 收集錯誤並回到編輯表單
        if (result.hasErrors()) {
            List<String> errorMsgs = result.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();
            model.addAttribute("errorMsgs", errorMsgs);
            return "back-end/administrant/edit";
        }

        // 設定更新時間
        vo.setAdminUpdatedAt(new Timestamp(System.currentTimeMillis()));

        // 呼叫 Service 更新資料
        administrantService.update(vo);

        // 更新成功後導回列表
        return "redirect:/admins/listAll";
    }

}
