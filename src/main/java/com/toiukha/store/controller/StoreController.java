package com.toiukha.store.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.toiukha.store.model.StoreVO;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import com.toiukha.store.model.StoreService;

@Controller
@RequestMapping("/store")
public class StoreController {

	@Autowired
	private StoreService storeService;

	// 顯示註冊頁面
	@GetMapping("/register")
	public String showRegisterPage(Model model) {
		model.addAttribute("storeVO", new StoreVO());
		model.addAttribute("errorMsgs", new ArrayList<String>());
		return "front-end/store/register";
	}

	@PostMapping("/register")
	public String processRegister(@Valid @ModelAttribute("storeVO") StoreVO storeVO, BindingResult result, Model model,
			@RequestParam("storeImg") MultipartFile storeImg) throws IOException {

		/*************************** 1.接收請求參數 - 圖片格式與唯一性檢查 ************************/

		// 去除 BindingResult 中 storeImg 欄位的 FieldError 紀錄
		result = removeFieldError(storeVO, result, "storeImg");

		// 檢查是否為圖片檔案（避免上傳非圖片）
		if (!storeImg.isEmpty()) {
			String contentType = storeImg.getContentType();
			if (contentType == null || !contentType.startsWith("image/")) {
				result.rejectValue("storeImg", "error.storeVO", "請上傳圖片格式檔案（ex:jpg、png）");
			} else {
				storeVO.setStoreImg(storeImg.getBytes());
			}
		}

		// 加入帳號／Email／統一編號 重複錯誤檢查（放在 result 中）
		if (storeService.existsByStoreAcc(storeVO.getStoreAcc())) {
			result.rejectValue("storeAcc", "error.storeVO", "帳號已存在，請使用其他帳號");
		}
		if (storeService.existsByStoreEmail(storeVO.getStoreEmail())) {
			result.rejectValue("storeEmail", "error.storeVO", "Email 已被註冊，請使用其他信箱");
		}
		if (storeService.existsByStoreGui(storeVO.getStoreGui())) {
			result.rejectValue("storeGui", "error.storeVO", "統一編號已被使用");
		}

		// 若欄位驗證失敗（格式 + 唯一鍵錯誤都會列出）
		if (result.hasErrors()) {
			// 用傳統迴圈把 FieldError 的訊息收集到 List<String>
			List<FieldError> fieldErrors = result.getFieldErrors();
			List<String> errorMsgs = new ArrayList<>();
			for (FieldError fe : fieldErrors) {
				// 取出每個欄位錯誤的預設訊息，加入到 errorMsgs
				errorMsgs.add(fe.getDefaultMessage());
			}

			model.addAttribute("errorMsgs", errorMsgs);
			return "front-end/store/register";
		}

		/*************************** 2.開始新增資料 *****************************************/
		storeService.registerStore(storeVO);

		/*************************** 3.新增完成,準備轉交(Send the Success view) **************/
		return "redirect:/store/registerSuccess";
	}

	// 顯示註冊成功通知頁面
	@GetMapping("/registerSuccess")
	public String showRegisterSuccess() {
		return "front-end/store/registerSuccess";
	}

	@GetMapping("/view")
	public String viewProfile(HttpSession session, Model model) {
		// 1. 從 session 取出已登入的商家
		StoreVO store = (StoreVO) session.getAttribute("store");
		if (store == null) {
			// 若尚未登入，跳回商家登入頁
			return "redirect:/store/login";
		}
		// 2. 放到 Model，供 Thymeleaf 渲染
		model.addAttribute("store", store);
		// 3. 回傳對應的檔案
		return "front-end/store/storeProfile";
	}

	// 顯示查詢主頁：把所有商家拿來當 select 選項
	@GetMapping("/selectPage")
	public String showSelectPage(Model model) {
		// 如果你想一開始就載入所有商家用於下拉，這裡呼叫 findAll()
		model.addAttribute("storeList", storeService.findAllStores());
		model.addAttribute("currentPage", "partner");
		return "back-end/store/selectPage";
	}

	// 顯示待審核列表 (storeStatus = 0)
	@GetMapping("/reviewStore")
	public String showReviewStorePage(Model model) {
		// 拿待審核
		List<StoreVO> pending = storeService.findPendingStores();
		model.addAttribute("pendingStores", pending);
		model.addAttribute("currentPage", "partner");
		return "back-end/store/reviewStore";
	}

	// 處理「通過」或「駁回」按鈕
	@PostMapping("/reviewStore")
	public String processReview(@RequestParam("storeId") Integer storeId, @RequestParam("action") String action) {
		// action = "pass" → 1；action = "reject" → 2
		byte newStatus = "pass".equals(action) ? (byte) 1 : (byte) 2;
		storeService.updateStatus(storeId, newStatus);
		// 完成後重新載入待審核列表
		return "redirect:/store/reviewStore";
	}

	// 顯示查詢結果（多筆）—— 單一條件
	@GetMapping("/listOne")
	public String listOne(@RequestParam(required = false) String storeId,
			@RequestParam(required = false) String storeAcc, @RequestParam(required = false) String storeStatus,
			@RequestParam(required = false) String storeName, Model model) {

		List<StoreVO> results = new ArrayList<>();

		if (storeId != null && !storeId.isBlank()) {
			// 依 ID 精確查一筆
			StoreVO s = storeService.getById(Integer.valueOf(storeId));
			if (s != null) {
				results.add(s);
			}

		} else if (storeAcc != null && !storeAcc.isBlank()) {
			// 依帳號做「模糊」查詢
			results = storeService.findByAccLike(storeAcc);

		} else if (storeStatus != null && !storeStatus.isBlank()) {
			// 依狀態篩選（精確）
			results = storeService.findByStatus(Byte.valueOf(storeStatus));

		} else if (storeName != null && !storeName.isBlank()) {
			// 依名稱做「模糊」查詢
			results = storeService.findByNameLike(storeName);
		}

		model.addAttribute("stores", results);
		model.addAttribute("currentPage", "partner");
		return "back-end/store/listOne";
	}

	// 顯示所有商家
	@GetMapping("/listAll")
	public String listAllStores(Model model) {
		model.addAttribute("stores", storeService.findAllStores());
		model.addAttribute("currentPage", "partner");
		return "back-end/store/listAll";
	}

	@GetMapping("/editStore")
	public String showEditForm(@RequestParam("storeId") Integer storeId, Model model) {
		StoreVO store = storeService.getById(storeId);
		model.addAttribute("currentPage", "partner");
		model.addAttribute("storeVO", store);
		return "back-end/store/editStore";
	}

	@PostMapping("/editStore")
	public String updateStore(@Valid @ModelAttribute("storeVO") StoreVO storeVO, BindingResult br, Model model,
			@RequestParam(value = "storeImgFile", required = false) MultipartFile imgFile) throws IOException {
		// 1. 驗證失敗時，回到編輯頁並顯示錯誤
		if (br.hasErrors()) {

			// 先撈一次 DB 裡的原始資料
			StoreVO original = storeService.getById(storeVO.getStoreId());
			// 把非表單欄位的值補回去
			storeVO.setStoreImg(original.getStoreImg());
			storeVO.setStoreRegDate(original.getStoreRegDate());
			storeVO.setStoreUpdatedAt(original.getStoreUpdatedAt());

			List<String> errorMsgs = new ArrayList<>();
			for (FieldError error : br.getFieldErrors()) {
				errorMsgs.add(error.getDefaultMessage());
			}
			model.addAttribute("errorMsgs", errorMsgs);
			return "back-end/store/editStore";
		}

		// 2. 處理圖片上傳
		if (imgFile != null && !imgFile.isEmpty()) {
			storeVO.setStoreImg(imgFile.getBytes());
		} else {
			// 沒上傳圖的情況，沿用原圖
			StoreVO original = storeService.getById(storeVO.getStoreId());
			storeVO.setStoreImg(original.getStoreImg());
		}

		// 3. 更新時間
		storeVO.setStoreUpdatedAt(new Timestamp(System.currentTimeMillis()));

		// 4. 執行更新
		storeService.save(storeVO);

		// 5. 重導到單一查詢頁（或列表頁，看你要哪種）
		// 單一查詢，例如：
		// return "redirect:/store/selectPage?storeId=" + storeVO.getStoreId();
		// 或者列表頁：
		return "redirect:/store/listAll";
	}

	// 去除 BindingResult 中某個欄位的 FieldError 紀錄
	private BindingResult removeFieldError(StoreVO storeVO, BindingResult result, String removedFieldname) {
		List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
				.filter(fieldError -> !fieldError.getField().equals(removedFieldname)).collect(Collectors.toList());

		result = new BeanPropertyBindingResult(storeVO, "storeVO");
		for (FieldError fieldError : errorsListToKeep) {
			result.addError(fieldError);
		}
		return result;
	}

}