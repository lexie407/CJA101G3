package com.toiukha.item.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.toiukha.advertisment.model.AdService;
import com.toiukha.item.model.ItemService;
import com.toiukha.item.model.ItemVO;
import com.toiukha.store.model.StoreVO;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/item")
public class ItemController {

	@Autowired
	ItemService itemSvc;
	@Autowired
	AdService adService;
	
	//廠商新增商品
	@GetMapping("addItem_back")
	public String addItem_store(ModelMap model,HttpSession session) {
		Object storeObj = session.getAttribute("store");
	
		StoreVO store = (StoreVO) storeObj;
		int storeId = store.getStoreId();
		session.setAttribute("storeId", storeId);
		
		ItemVO itemVO = new ItemVO();
		itemVO.setStoreId(storeId);  // 在這裡就設定 storeId
		model.addAttribute("itemVO", itemVO);
		model.addAttribute("currentPage", "product");
		model.addAttribute("activeItem", "addItem");
		return "back-end/item/addItem_back";
	}
	@PostMapping("insert_store")
	public String insert_store(@Valid ItemVO itemVO, BindingResult result, ModelMap model,
			@RequestParam("itemImg") MultipartFile[] parts,HttpSession session) throws IOException {

		System.out.println("seccess");
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 去除BindingResult中upFiles欄位的FieldError紀錄 --> 見第172行
		result = removeFieldError(itemVO, result, "itemImg");

		if (parts[0].isEmpty()) { // 使用者未選擇要上傳的圖片時
			model.addAttribute("errorMessage", "商品照片: 請上傳照片");
		} else {
			for (MultipartFile multipartFile : parts) {
				byte[] buf = multipartFile.getBytes();
				itemVO.setItemImg(buf);
			}
		}
		if (result.hasErrors() || parts[0].isEmpty()) {
			return "/item/addItem_back";
		}
		/*************************** 2.開始新增資料 *****************************************/
		// EmpService empSvc = new EmpService();
		
		Object storeObj = session.getAttribute("store");

		StoreVO store = (StoreVO) storeObj;
		int storeId = store.getStoreId();
		session.setAttribute("storeId", storeId);
		
		itemVO.setStoreId(storeId);
		itemVO.setItemStatus(0);
		itemVO.setRepCount(0);
		itemVO.setCreAt(new Timestamp(System.currentTimeMillis()));
		itemVO.setUpdAt(new Timestamp(System.currentTimeMillis()));
		itemSvc.addItem(itemVO);
		/*************************** 3.新增完成,準備轉交(Send the Success view) **************/
		List<ItemVO> list = itemSvc.getAll();
		model.addAttribute("itemListData", list); // for listAllEmp.html 第85行用
		model.addAttribute("success", "- (新增成功)");
		return "redirect:/item/listAllItem_back"; // 新增成功後重導至IndexController_inSpringBoot.java的第58行@GetMapping("/emp/listAllEmp")
	}
	
	@GetMapping("addItem")
	public String addItem(ModelMap model) {
		ItemVO itemVO = new ItemVO();
		model.addAttribute("itemVO", itemVO);
		
		return "back-end/item/addItem";
	}

	@PostMapping("insert")
	public String insert(@Valid ItemVO itemVO, BindingResult result, ModelMap model,
			@RequestParam("itemImg") MultipartFile[] parts) throws IOException {

		System.out.println("seccess");
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 去除BindingResult中upFiles欄位的FieldError紀錄 --> 見第172行
		result = removeFieldError(itemVO, result, "itemImg");

		if (parts[0].isEmpty()) { // 使用者未選擇要上傳的圖片時
			model.addAttribute("errorMessage", "商品照片: 請上傳照片");
		} else {
			for (MultipartFile multipartFile : parts) {
				byte[] buf = multipartFile.getBytes();
				itemVO.setItemImg(buf);
			}
		}
		if (result.hasErrors() || parts[0].isEmpty()) {
			return "back-end/item/addItem";
		}
		/*************************** 2.開始新增資料 *****************************************/
		// EmpService empSvc = new EmpService();
		itemVO.setItemStatus(0);
		itemVO.setRepCount(0);
		itemVO.setCreAt(new Timestamp(System.currentTimeMillis()));
		itemVO.setUpdAt(new Timestamp(System.currentTimeMillis()));
		itemSvc.addItem(itemVO);
		/*************************** 3.新增完成,準備轉交(Send the Success view) **************/
		List<ItemVO> list = itemSvc.getAll();
		model.addAttribute("itemListData", list); // for listAllEmp.html 第85行用
		model.addAttribute("success", "- (新增成功)");
		return "redirect:/back-end/item/listAllItem"; // 新增成功後重導至IndexController_inSpringBoot.java的第58行@GetMapping("/emp/listAllEmp")
	}

	@PostMapping("getOne_For_Update")
	public String getOne_For_Update(@RequestParam("itemId") String itemId, ModelMap model) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始查詢資料 *****************************************/
		// EmpService empSvc = new EmpService();
		ItemVO itemVO = itemSvc.getOneItem(Integer.valueOf(itemId));

		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("itemVO", itemVO);
		model.addAttribute("currentPage", "product");
		model.addAttribute("activeItem", "allItem");
		return "back-end/item/update_item_back"; // 查詢完成後轉交update_emp_input.html
	}

	public BindingResult removeFieldError(ItemVO itemVO, BindingResult result, String removedFieldname) {
		List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
				.filter(fieldname -> !fieldname.getField().equals(removedFieldname)).collect(Collectors.toList());
		result = new BeanPropertyBindingResult(itemVO, "itemVO");
		for (FieldError fieldError : errorsListToKeep) {
			result.addError(fieldError);
		}
		return result;
	}
	@PostMapping("update")
	public String update(@Valid ItemVO itemVO, BindingResult result, ModelMap model,
			@RequestParam("itemImg") MultipartFile[] parts) throws IOException {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 去除BindingResult中upFiles欄位的FieldError紀錄 --> 見第172行
		result = removeFieldError(itemVO, result, "itemImg");

		if (parts[0].isEmpty()) { // 使用者未選擇要上傳的新圖片時
			// EmpService empSvc = new EmpService();
			byte[] itemImg = itemSvc.getOneItem(itemVO.getItemId()).getItemImg();
			itemVO.setItemImg(itemImg);
		} else {
			for (MultipartFile multipartFile : parts) {
				byte[] itemImg = multipartFile.getBytes();
				itemVO.setItemImg(itemImg);
			}
		}
		if (result.hasErrors()) {
			// 印出所有欄位錯誤資訊
			
			return "back-end/item/update_item_input";
		}
		/*************************** 2.開始修改資料 *****************************************/
		// EmpService empSvc = new EmpService();
		itemVO.setItemStatus(0);
		itemVO.setCreAt(itemSvc.getOneItem(itemVO.getItemId()).getCreAt());
		itemVO.setUpdAt(new Timestamp(System.currentTimeMillis()));
		itemSvc.updateItem(itemVO);

		/*************************** 3.修改完成,準備轉交(Send the Success view) **************/
		model.addAttribute("success", "- (修改成功)");
		itemVO = itemSvc.getOneItem(Integer.valueOf(itemVO.getItemId()));
		model.addAttribute("itemVO", itemVO);
		return "back-end/item/listOneItem_back"; // 修改成功後轉交listOneEmp.html
	}
	//===================新增促銷===========================================
	@PostMapping("getOne_For_Update_promo")
	public String getOne_For_Update_promo(@RequestParam("itemId") String itemId, ModelMap model) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始查詢資料 *****************************************/
		// EmpService empSvc = new EmpService();
		ItemVO itemVO = itemSvc.getOneItem(Integer.valueOf(itemId));

		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("itemVO", itemVO);
		model.addAttribute("currentPage", "product");
		model.addAttribute("activeItem", "allItem");
		return "back-end/item/update_item_back_addpromo"; // 查詢完成後轉交update_emp_input.html
	}
	
	@PostMapping("update_promo")
	public String update_promo( ItemVO itemVO, BindingResult result, ModelMap model) throws IOException {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 去除BindingResult中upFiles欄位的FieldError紀錄 --> 見第172行
		result = removeFieldError(itemVO, result, "itemImg");

	
		if (result.hasErrors()) {
			System.out.println("表單驗證失敗，錯誤如下：");
			result.getFieldErrors().forEach(error -> {
				System.out.println(error.getField() + ": " + error.getDefaultMessage());
			});
			return "back-end/item/update_item_input";
		}
		/*************************** 2.開始修改資料 *****************************************/
		// EmpService empSvc = new EmpService();
		
		ItemVO originalVO = itemSvc.getOneItem(itemVO.getItemId());
		originalVO.setUpdAt(new Timestamp(System.currentTimeMillis()));
// 只更新促銷相關欄位，其餘欄位維持原值
		originalVO.setDiscPrice(itemVO.getDiscPrice());
		originalVO.setStaTime(itemVO.getStaTime());
		originalVO.setEndTime(itemVO.getEndTime());

// 其他欄位都不用動，直接存回去
		itemSvc.updateItem(originalVO);


		/*************************** 3.修改完成,準備轉交(Send the Success view) **************/
		model.addAttribute("success", "- (修改成功)");
		itemVO = itemSvc.getOneItem(Integer.valueOf(itemVO.getItemId()));
		model.addAttribute("itemVO", itemVO);
		return "back-end/item/listOneItem_back"; // 修改成功後轉交listOneEmp.html
	}
	
	// ========== AJAX 上下架功能 ==========
	@PostMapping("putOn")
	@ResponseBody
	public String putOn(@RequestParam("itemId") Integer itemId) {
		ItemVO item = itemSvc.getOneItem(itemId);
		if (item != null) {
			item.setItemStatus(1); // 1: 上架

			itemSvc.updateItem(item);
			return "success";
		}
		return "fail";
	}
	
	@PostMapping("putOff")
	@ResponseBody
	public String putOff(@RequestParam("itemId") Integer itemId) {
		ItemVO item = itemSvc.getOneItem(itemId);
		if (item != null) {
			item.setItemStatus(2); // 2: 下架
		
			itemSvc.updateItem(item);
			return "success";
		}
		return "fail";
	}
	//==================item 網頁=======================
	//商品列表
	@GetMapping("listAllItem")
	public String front_listAllEmp(Model model) {
		model.addAttribute("ads", adService.getTodayAdList());	// 取得廣告
		model.addAttribute("currentPage", "store");
		model.addAttribute("activeItem", "items");
		return "front-end/item/listAllItem";
	}
	//已上架商品
	@ModelAttribute("itemListData_front") // for select_page.html 第97 109行用 // for listAllEmp.html 第85行用
	protected List<ItemVO> referenceListData1(Model model) {

		List<ItemVO> list = itemSvc.findByItemStatus(1);
		return list;
	}
	
	//管理員商品列表
	@GetMapping("listAllItem_admin")
	public String back_listAllEmp(Model model) {
		model.addAttribute("currentPage", "partner");
		model.addAttribute("activeItem", "allItem");
		model.addAttribute("currentPage2", "allItem");
		return "back-end/item/listAllItem_admin";
	}
	//管理員商品列表
	@ModelAttribute("itemListData_admin") // for select_page.html 第97 109行用 // for listAllEmp.html 第85行用
	protected List<ItemVO> referenceListData2(Model model) {

		List<ItemVO> list = itemSvc.getAll();
		return list;
	}
	//廠商登入後台 全部商品
	@GetMapping("listAllItem_back")
	public String back_listAllEmp1(Model model) {
		model.addAttribute("currentPage", "product");
		model.addAttribute("activeItem", "allItem");
		return "back-end/item/listAllItem_back";
	}
	//廠商登入後台 全部商品
	@ModelAttribute("itemListData_store")
	protected List<ItemVO> referenceListData_byStoreId(Model model, HttpSession session) {
		Object storeObj = session.getAttribute("store");
		if (storeObj == null) {
			return new ArrayList<>(); // 或回傳 null，看你需求
		}
		StoreVO store = (StoreVO) storeObj;
		int storeId = store.getStoreId();
		session.setAttribute("storeId", storeId);
		List<ItemVO> list = itemSvc.findByStoreId(storeId);
		return list;
	}

}

