package com.toiukha.members.controller; // 確保這是你的 Controller 實際套件路徑

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.toiukha.members.model.MembersVO;
import com.toiukha.email.EmailService;
import com.toiukha.members.model.MembersService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/members")
public class MembersController {
	@Autowired
	private MembersService membersService;
	@Autowired
	private EmailService emailService;

	// 顯示註冊會員頁面
	@GetMapping("/register")
	public String showRegisterPage(Model model) {

		model.addAttribute("membersVO", new MembersVO());
		model.addAttribute("errorMsgs", new ArrayList<>());

		return "front-end/members/register";
	}

	// 顯示驗證信發送頁面
	@GetMapping("/verificationSent")
	public String showVerificationSentPage(@RequestParam(value = "email", required = false) String email, Model model) {
		if (email == null || email.isBlank()) {
			// 若沒有 email，代表不是從註冊流程過來的，導回註冊頁
			return "redirect:/members/register";
		}

		model.addAttribute("email", email);
		return "front-end/members/emailVerificationSent";
	}

	@GetMapping("/verifyEmail")
	public String verifyMember(@RequestParam("token") String token, Model model) {
		boolean ok = membersService.verifyAndActivateMember(token);
		if (ok) {
			// 啟用成功
			return "front-end/members/emailVerificationSuccess";
		} else {
			// 啟用失敗（token 無效、過期或找不到會員）
			model.addAttribute("errorMsg", "驗證失敗，連結已失效或不正確。");
			return "front-end/members/emailVerificationFailed";
		}
	}

//      接收註冊表單 POST 請求，處理註冊流程

	@PostMapping("/register")
	public String processRegister(@Valid @ModelAttribute("membersVO") MembersVO membersVO, BindingResult result,
			Model model, @RequestParam("memAvatar") MultipartFile memAvatar) throws IOException {

		/*************************** 1.接收請求參數 - 輸入格式與邏輯錯誤處理 ************************/

		// 去除BindingResult中 memAvatar 欄位的FieldError紀錄
		result = removeFieldError(membersVO, result, "memAvatar");

		// 檢查是否為圖片檔案（避免上傳非圖片）
		if (!memAvatar.isEmpty()) {
			String contentType = memAvatar.getContentType();
			if (contentType == null || !contentType.startsWith("image/")) {
				result.rejectValue("memAvatar", "error.membersVO", "請上傳圖片格式檔案（ex:jpg、png）");
			} else {
				byte[] buf = memAvatar.getBytes();
				membersVO.setMemAvatar(buf);
			}
		}

		// 加入帳號/Email 重複錯誤檢查（放在 result 中）
		if (membersService.existsByMemAcc(membersVO.getMemAcc())) {
			result.rejectValue("memAcc", "error.membersVO", "帳號已存在，請使用其他帳號");
		}
		if (membersService.existsByMemEmail(membersVO.getMemEmail())) {
			result.rejectValue("memEmail", "error.membersVO", "Email 已被註冊，請使用其他信箱");
		}

		// 若欄位驗證失敗（格式 + 唯一鍵錯誤都會列出）
		if (result.hasErrors()) {
			List<String> errorMsgs = result.getFieldErrors().stream().map(FieldError::getDefaultMessage)
					.collect(Collectors.toList());
			model.addAttribute("errorMsgs", errorMsgs);
			return "front-end/members/register";
		}

		/*************************** 2.開始新增資料 *****************************************/
		membersService.registerMember(membersVO);

		/*************************** 3.新增完成,準備轉交(Send the Success view) **************/
		return "redirect:/members/verificationSent?email=" + membersVO.getMemEmail();
	}

	/**
	 * 使用者點擊信件中的驗證連結，啟用帳號
	 */
	@GetMapping("/verify")
	public String verifyEmail(@RequestParam("token") String token, Model model) {
		/*************************** 1.接收請求參數 - 取得 token ************************/
		// token 由 @RequestParam 拿到

		/*************************** 2.執行驗證邏輯 *****************************************/
		boolean success = membersService.verifyAndActivateMember(token);

		/*************************** 3.驗證結果,準備轉交 (Send the view) **************/
		if (success) {
			return "redirect:/login";
		} else {
			model.addAttribute("errorMsg", "驗證連結無效或已過期！");
			return "front-end/members/emailVerificationFailed";
		}
	}

	@PostMapping("/resendVerification")
	public String resendVerification(@RequestParam("email") String email, RedirectAttributes redirectAttr) {

		/*************************** 1.接收請求參數 - 取得 email ************************/
		// email 由 @RequestParam 拿到

		/***************************
		 * 2.執行重發驗證信
		 *****************************************/
		MembersVO member = membersService.getByEmail(email);
		if (member != null && member.getMemStatus() == 0) {
			emailService.sendVerificationEmail(email, member.getMemId());
			redirectAttr.addFlashAttribute("msg", "驗證信已重新發送，請查收！");
		} else {
			redirectAttr.addFlashAttribute("msg", "此信箱尚未註冊或已啟用。");
		}

		/*************************** 3.重發完成,準備轉交(Send the view) **************/
		return "redirect:/members/verificationSent?email=" + email;
	}

	// 會員資料檢視頁面

	@GetMapping("/view")
	public String viewProfile(HttpSession session, Model model) {
		// 1. 從 session 取出已登入的 member
		MembersVO member = (MembersVO) session.getAttribute("member");
		if (member == null) {
			// 若 session 沒 member，可跳到登入（或拋例外）
			return "redirect:/members/login";
		}

		// 2. （選擇性）重新從 DB 拿一次完整資料
		// member = membersService.getById(member.getMemId());

		// 3. 加入模型供 Thymeleaf 使用
		model.addAttribute("membersVO", member);

		// 4. 回傳「會員資料檢視」的模板名稱（與檔名一致）
		return "front-end/members/membersProfile";
	}

	// 顯示會員修改頁面
	@GetMapping("/update")
	public String showUpdatePage(HttpSession session, Model model) {
		MembersVO sessionMember = (MembersVO) session.getAttribute("member");
		model.addAttribute("membersVO", sessionMember);
		return "front-end/members/update";
	}

	@PostMapping("/update")
	public String updateMember(@Valid @ModelAttribute("membersVO") MembersVO membersVO, BindingResult result,
			Model model, HttpSession session, @RequestParam("memAvatarFile") MultipartFile memAvatarFile,
			@RequestParam("memAvatarFrameFile") MultipartFile memAvatarFrameFile) throws IOException {

		MembersVO sessionMember = (MembersVO) session.getAttribute("member");
		if (sessionMember == null) {
			return "redirect:/members/login";
		}

		// 強制使用 Session 中的 memId & memAcc（防止偽造）
		membersVO.setMemId(sessionMember.getMemId());
		membersVO.setMemAcc(sessionMember.getMemAcc());
		membersVO.setMemRegTime(sessionMember.getMemRegTime());

		membersVO.setMemGroupAuth(sessionMember.getMemGroupAuth());
		membersVO.setMemGroupPoint(sessionMember.getMemGroupPoint());
		membersVO.setMemStatus(sessionMember.getMemStatus());
		membersVO.setMemStoreAuth(sessionMember.getMemStoreAuth());
		membersVO.setMemStorePoint(sessionMember.getMemStorePoint());
		membersVO.setMemPoint(sessionMember.getMemPoint());
		membersVO.setMemLogErrCount(sessionMember.getMemLogErrCount());
		membersVO.setMemLogErrTime(sessionMember.getMemLogErrTime());

		// 處理頭像（若上傳新圖片才更新）
		if (!memAvatarFile.isEmpty() && memAvatarFile.getContentType().startsWith("image/")) {
			membersVO.setMemAvatar(memAvatarFile.getBytes());
		} else {
			membersVO.setMemAvatar(sessionMember.getMemAvatar());
		}

		if (!memAvatarFrameFile.isEmpty() && memAvatarFrameFile.getContentType().startsWith("image/")) {
			membersVO.setMemAvatarFrame(memAvatarFrameFile.getBytes());
		} else {
			membersVO.setMemAvatarFrame(sessionMember.getMemAvatarFrame());
		}

		// 若驗證有錯誤，回填資料並返回表單
		if (result.hasErrors()) {
			List<String> errorMsgs = result.getFieldErrors().stream().map(FieldError::getDefaultMessage)
					.collect(Collectors.toList());
			model.addAttribute("errorMsgs", errorMsgs);
			return "front-end/members/update";
		}

		// 更新會員資料
		membersService.updateMembers(membersVO);

		// 更新 session 裡的資料（避免還是舊資料）
		session.setAttribute("member", membersVO);

		return "redirect:/members/update?success"; // 可以在 update 頁面用參數判斷顯示「修改成功」
	}

	// 顯示email驗證失敗頁面
	@GetMapping("/emailFail")
	public String showEmailVerificationFailed() {
		return "front-end/members/emailVerificationFailed";
	}

	// 顯示後台選擇頁面
	@GetMapping("/selectPage")
	public String showSelectPage(Model model) {
		model.addAttribute("membersList", membersService.findAllMembers());
		return "back-end/members/selectPage";
	}

	@PostMapping("/selectPage")
	public String doSearch(@RequestParam(value = "status", required = false) Integer status,
			@RequestParam(value = "memAcc", required = false) String memAcc,
			@RequestParam(value = "memId", required = false) Integer memId,
			@RequestParam(value = "memName", required = false) String memName, Model model) {

		boolean noCriteria = (status == null) && (memAcc == null || memAcc.isBlank()) && (memId == null)
				&& (memName == null || memName.isBlank());
		if (noCriteria) {
			// 若無任何搜尋條件，直接跳到全部列表
			return "redirect:/members/listAll";
		}

		List<MembersVO> results = membersService.searchByCriteria(status, memAcc, memId, memName);
		model.addAttribute("membersList", results);
		return "back-end/members/searchResults"; // <-- 這裡回傳搜尋結果專用的模板
	}

	// 去除 BindingResult 中某個欄位的 FieldError 紀錄
	private BindingResult removeFieldError(MembersVO membersVO, BindingResult result, String removedFieldname) {
		List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
				.filter(fieldError -> !fieldError.getField().equals(removedFieldname)).collect(Collectors.toList());

		result = new BeanPropertyBindingResult(membersVO, "membersVO");
		for (FieldError fieldError : errorsListToKeep) {
			result.addError(fieldError);
		}
		return result;
	}
}