package com.toiukha.members.controller; // 確保這是你的 Controller 實際套件路徑

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.toiukha.members.model.MembersVO;
import com.toiukha.email.EmailService;
import com.toiukha.members.model.MembersService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/members")
public class MembersController {
	@Autowired
	private MembersService membersService;
	 @Autowired
	    private StringRedisTemplate redisTemplate;

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
		MembersVO member = membersService.getByEmail(email);

		/***************************
		 * 2.執行重發驗證信
		 *****************************************/
		
		if (member != null && member.getMemStatus() == 0) {
			
			
			 // 產生唯一驗證碼
			String token = UUID.randomUUID().toString();
			// 存進 Redis，30 分鐘有效
		    redisTemplate.opsForValue()
		        .set("verify:" + token, 
		        		member.getMemId().toString(), 
		             30, 
		             TimeUnit.MINUTES);
			
			
		 //  抓 baseUrl 組 link
            ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest req = attrs.getRequest();
            String baseUrl = req.getScheme() + "://"
                           + req.getServerName() + ":"
                           + req.getServerPort()
                           + req.getContextPath();
            String link = baseUrl + "/members/verifyEmail?token=" + token;

            //  非同步發信
            new Thread(() -> emailService.sendVerificationEmail(email, link))
                .start();
			
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

		// 2. 加入模型供 Thymeleaf 使用
		model.addAttribute("membersVO", member);
		model.addAttribute("currentPage", "account");
		model.addAttribute("activeItem", "profile");

		// 3. 回傳「會員資料檢視」的模板名稱（與檔名一致）
		return "front-end/members/membersProfile";
	}

	// 顯示會員修改頁面
	@GetMapping("/update")
	public String showUpdatePage(HttpSession session, Model model) {
		MembersVO sessionMember = (MembersVO) session.getAttribute("member");
		model.addAttribute("currentPage", "account");
		model.addAttribute("activeItem", "profile");
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
			model.addAttribute("currentPage", "accounts");
			model.addAttribute("activeItem", "profile");
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
		model.addAttribute("currentPage", "accounts");
		model.addAttribute("currentPage2", "selectPage");
		return "back-end/members/selectPage";
	}

	@GetMapping("/searchResults")
	public String showSearchResults(@RequestParam(value = "memStatus", required = false) Byte memStatus,
			@RequestParam(value = "memAcc", required = false) String memAcc,
			@RequestParam(value = "memId", required = false) Integer memId,
			@RequestParam(value = "memName", required = false) String memName,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size, Model model) {

		boolean noCriteria = (memStatus == null) && (memAcc == null || memAcc.isBlank()) && (memId == null)
				&& (memName == null || memName.isBlank());
		if (noCriteria) {
			return "redirect:/members/listAll";
		}

		// 使用 page 和 size 動態分頁
		Pageable pageable = PageRequest.of(page, size, Sort.by("memId").ascending());
		Page<MembersVO> pageResult = membersService.searchByCriteria(memStatus, memAcc, memId, memName, pageable);

		model.addAttribute("page", pageResult);
		model.addAttribute("membersList", pageResult.getContent());

		model.addAttribute("memStatus", memStatus);
		model.addAttribute("memAcc", memAcc);
		model.addAttribute("memId", memId);
		model.addAttribute("memName", memName);
		model.addAttribute("currentPage", "accounts");

		return "back-end/members/searchResults";
	}

	@GetMapping("/listAll")
	public String listAll(Model model) {
		// 一次拿所有會員，交給前端 DataTables 處理
		List<MembersVO> all = membersService.findAllMembers();
		model.addAttribute("membersList", all);
		model.addAttribute("currentPage", "accounts");
		model.addAttribute("currentPage2", "listAll");
		return "back-end/members/listAll";
	}

	@GetMapping("/editMembers")
	public String showEditForm(@RequestParam(value = "memId", required = false) Integer memId, Model model,
			RedirectAttributes ra) {

		// 1. 防呆：没有 memId
		if (memId == null) {
			ra.addFlashAttribute("errorMsg", "必須指定會員 ID");
			return "redirect:/members/listAll"; // 
		}

		// 2. 撈資料
		MembersVO member = membersService.getOneMember(memId);
		if (member == null) {
			ra.addFlashAttribute("errorMsg", "找不到指定的會員");
			return "redirect:/members/listAll";
		}

		// 3. 成功：把 VO 塞入 Model
		model.addAttribute("membersVO", member);
		model.addAttribute("currentPage", "accounts");
		return "back-end/members/editMembers";
	}

	@PostMapping("/editMembers")
	public String updateMember(@Valid @ModelAttribute("membersVO") MembersVO membersVO, BindingResult br, Model model,
			@RequestParam(value = "memAvatarFile", required = false) MultipartFile avatar,
			@RequestParam(value = "memAvatarFrameFile", required = false) MultipartFile frame) throws IOException {

		MembersVO original = membersService.getById(membersVO.getMemId());

		/*************************** 1. 驗證錯誤處理 ***************************/
		if (br.hasErrors()) {
			// 補回畫面沒綁定的欄位，避免空值造成畫面壞掉或資料被清空
			membersVO.setMemAvatar(original.getMemAvatar());
			membersVO.setMemAvatarFrame(original.getMemAvatarFrame());
			membersVO.setMemRegTime(original.getMemRegTime());
			membersVO.setMemUpdatedAt(original.getMemUpdatedAt());

			if (membersVO.getMemStorePoint() == null)
				membersVO.setMemStorePoint(original.getMemStorePoint());
			if (membersVO.getMemLogErrTime() == null)
				membersVO.setMemLogErrTime(original.getMemLogErrTime());

			List<String> errorMsgs = new ArrayList<>();
			for (FieldError error : br.getFieldErrors()) {
				errorMsgs.add(error.getDefaultMessage());
			}
			model.addAttribute("errorMsgs", errorMsgs);
			model.addAttribute("currentPage", "accounts");
			return "back-end/members/editMembers";
		}

		/*************************** 2. 處理上傳圖片（補原圖） ***************************/
		if (avatar != null && !avatar.isEmpty()) {
			membersVO.setMemAvatar(avatar.getBytes());
		} else {
			membersVO.setMemAvatar(original.getMemAvatar());
		}

		if (frame != null && !frame.isEmpty()) {
			membersVO.setMemAvatarFrame(frame.getBytes());
		} else {
			membersVO.setMemAvatarFrame(original.getMemAvatarFrame());
		}

		/*************************** 3. 設定更新時間 ***************************/
		membersVO.setMemUpdatedAt(new Timestamp(System.currentTimeMillis()));

		/*************************** 4. 執行更新並導向查詢結果頁 ***************************/
		membersService.editMember(membersVO);
		return "redirect:/members/searchResults?memId=" + membersVO.getMemId();
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