package com.toiukha.advertisment.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.toiukha.advertisment.model.AdService;
import com.toiukha.advertisment.model.AdVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.servlet.ServletOutputStream; // 圖片


@Controller
@Validated	// 啟用Controller 層的"參數驗證"
@RequestMapping("/advertisment")	//定義Controller 處理的基礎URL路徑
public class AdController {

	@Autowired
	AdService adSvc;	// 自動注入AdService
	
	//放入StoreService後要改!!
	@Autowired(required = false)
	StoreService storeSvc; // 自動注入 StoreService (用於處理商店資料，例如下拉選單)
	
	// ** 處理 /advertisment/addAd 的 GET 請求，顯示新增廣告的表單頁面 **
	
	@GetMapping("addAd")
	public String addAd(ModelMap model) {
		AdVO adVO = new AdVO();
		model.addAttribute("adVO", adVO);
		
		// 確保下拉選單資料也傳遞過去，用於前端的商店下拉選單
		// 假設 storeSvc.getAll() 方法會回傳 List<StoreVO>
//		model.addAttribute("storeListData", storeSvc.getAll());	// 從 @ModelAttribute 獲取

		return "back-end/advertisment/addAd";
	
	}
	
	/*
	 * 處理新增廣告的 POST 請求，接收表單提交的資料並進行驗證、處理圖片上傳。
	 * @Valid 啟用 AdVO 的 Bean Validation
	 * @RequestParam("adImageFile") MultipartFile adImageFile 接收單一圖片檔案
	 * @RequestParam("storeId") Integer storeId 手動接收 storeId，然後再轉換為 StoreVO
	 */
	
	@PostMapping("insert")
	public String insert(@Valid AdVO adVO, BindingResult result, ModelMap model,
			@RequestParam("adImageFile") MultipartFile adImageFile, // 接收單一圖片檔案
			@RequestParam("storeId") Integer storeId) throws IOException { // 接收 storeId

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 處理 storeId 到 StoreVO 的轉換(處理外鍵)
		if (storeId == null) {
            // 如果 storeId 為 null，則手動添加錯誤到 BindingResult
            result.rejectValue("storeVO", "NotNull.adVO.storeVO", "商店編號: 請勿空白");
        } else {
            StoreVO storeVO = storeSvc.getOneStore(storeId); // 假設 StoreService 有 getOneStore 方法
            if (storeVO == null) {
                result.rejectValue("storeVO", "invalid.adVO.storeVO", "商店編號: 無效的商店ID");
            } else {
                adVO.setStoreVO(storeVO); // 將查詢到的 StoreVO 設定到 AdVO 中
            }
        }
		
		// 處理圖片上傳：如果沒有圖片上傳，則設置錯誤訊息
		if(adImageFile.isEmpty()) {
			model.addAttribute("errorMessage", "廣告圖片: 請上傳照片"); // 使用 model.addAttribute("errorMessage", ...)
		}else {
			adVO.setAdImage(adImageFile.getBytes());
		}
		// 如果你的 AdVO 中沒有直接綁定 MultipartFile 屬性，此方法可以忽略，因為不會產生FieldError
		// result = removeFieldError(adVO, result, "adImageFile"); // 可選：如果需要移除與檔案上傳相關的FieldError

		// *檢查所有錯誤*：(AdVO 自身的驗證錯誤、圖片是否上傳、StoreVO 是否已正確設定)
		if (result.hasErrors() || adImageFile.isEmpty() || adVO.getStoreVO() == null) {
			// 如果有錯誤，重新填充 ModelAttributes 以便下拉選單等能正常顯示
			model.addAttribute("adVO", adVO); // 保留用戶輸入
//			model.addAttribute("storeListData", storeSvc.getAll()); // 重新填充商店列表
			return "back-end/advertisment/addAd"; // 返回新增頁面
//		}

		/*************************** 2.開始新增資料 *****************************************/
		adSvc.addAd(adVO);

		/*************************** 3.新增完成,準備轉交(Send the Success view) **************/
		model.addAttribute("success", "- (新增成功)"); // 設定成功訊息
		// 新增成功後重導至列表頁面，可以避免重新整理時重複提交		
		return "redirect:/advertisment/listAllAd"; // 新增成功後重導至IndexController_inSpringBoot.java的第58行@GetMapping("/emp/listAllEmp")

	}
	
	 //	處理從列表頁面發送的修改請求，根據 **廣告ID (adId)** 查詢單一廣告資料以供修改。

	@PostMapping("getOne_For_Update")
	public String getOne_For_Update(@RequestParam("adId") String adId, ModelMap model) {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// @RequestParam 會自動處理基本類型轉換，這裡直接使用即可
		
		/*************************** 2.開始查詢資料 *****************************************/
		AdVO adVO = adSvc.getOneAd(Integer.valueOf(adId));
		
		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("adVO", adVO);
		
		//確保下拉選單資料也傳遞過去，用於修改頁面的"商店"下拉選單
//		model.addAttribute("storeListData", storeSvc.getAll()));
		return "back-end/advertisment/update_ad_input"; // 轉交到修改表單頁面
	}
		// 處理更新廣告的 POST 請求，接收表單提交的資料並進行驗證、處理圖片上傳。

		@PostMapping("update")
		public String update(@Valid AdVO adVO, BindingResult result, ModelMap model,
						@RequestParam("adImageFile") MultipartFile adImageFile,
						@RequestParam("storeId") Integer storeId) throws IOException { // 接收 storeId			/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
			// 處理 storeId 到 StoreVO 的轉換 (外鍵處理)
			if (storeId == null) {
				result.rejectValue("storeVO", "NotNull.adVO.storeVO", "商店編號: 請勿空白");
		}else {
            StoreVO storeVO = storeSvc.getOneStore(storeId);
            if (storeVO == null) {
                result.rejectValue("storeVO", "invalid.adVO.storeVO", "商店編號: 無效的商店ID");
            } else {
                adVO.setStoreVO(storeVO);
            }
        }
	
			// 處理圖片上傳：如果沒有選擇新圖片，保留舊圖片
		if(adImageFile.isEmpty()) {
			// 從資料庫中讀取原始廣告圖片，確保 adVO.getAdId() 有值 (修改請求的 adVO 應該已帶有 adId)
			AdVO originalAd = adSvc.getOneAd(adVO.getAdId());
			if(originalAd != null) {
				adVO.setAdImage(originalAd.getAdImage());
			}else {
				// 假設沒有原始廣告
				result.rejectValue("adImage", "adImage.missing", "找不到原始廣告圖片");
			}											
		}else {
			adVO.setAdImage(adImageFile.getBytes());
		}

//		result = removeFieldError(adVO, result, "adImageFile")
		// 檢查所有錯誤
		if(result.hasErrors() || adVO.getStoreVO() == null || (adImageFile.isEmpty() && adVO.getAdImage() == null)) {
			// 如果有錯誤，重新填充 ModelAttributes 以便下拉選單等能正常顯示
			model.addAttribute("adVO", adVO);	// 保留使用者輸入
			model.addAttribute("storeListData", storeSvc.getAll());
			return "back-end/advertisment/update_ad_input"; // 返回修改頁面

		}
		/*************************** 2.開始修改資料 *****************************************/
		adSvc.updateAd(adVO); // adVO 包含 adId，用於更新
		
		/*************************** 3.修改完成,準備轉交(Send the Success view) **************/
		model.addAttribute("success", "- (修改成功)");
		
		// 重新查詢更新後的廣告資料，確保顯示的是最新數據
		adVO = adSvc.getOneAd(adVO.getAdId());	// 使用 adId 查詢
		model.addAttribute("afVO", adVO);
		return "back-end/advertisment/listOneAd"; // 修改成功後轉交到顯示單一廣告頁面	
	}
		
		
	// 處理從列表頁面發送的"刪除"請求，根據 **廣告ID (adId)** 刪除廣告資料。
	
	@PostMapping("delete")
	public String delete(@RequestParam("adId") Integer adId, ModelMap model) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		// 無需特殊處理，@RequestParam 會自動轉換
		
		/*************************** 2.開始刪除資料 *****************************************/
		adSvc.deleteAd(adId); // 使用 adId 刪除

		/*************************** 3.刪除完成,準備轉交(Send the Success view) **************/
		List<AdVO> list = adSvc.getAll();
		model.addAttribute("adListData", list); // 重新查詢所有廣告以更新列表
		model.addAttribute("success", "- (刪除成功)");
		return "back-end/advertisment/listAllAd"; // 刪除完成後重導到所有廣告列表頁面

	}
		// 處理 /advertisment/listAllAd 的 GET 請求，顯示所有廣告列表頁面
	@GetMapping("listAllAd")
	public String listAllAd(ModelMap model) {
		List<AdVO> list = adSvc.getAll();
		model.addAttribute("adListData", list);
		return "back-end/advertisment/listAllAd"; // 回傳所有廣告的列表頁面

	}
		
		//	處理 /advertisment/getOne_For_Display 的 POST 請求，
		//	根據 **廣告ID (adId)** 查詢單一廣告並顯示。
	@PostMapping("getOne_For_Display")
	public String getOne_For_Display(
			
		/***************************1.接收請求參數 - 輸入格式的錯誤處理*************************/
	
			// 使用 @RequestParam 接收 adId，並加入驗證註解
			@jakarta.validation.constraints.NotEmpty(message="廣告編號: 請勿空白") // <<-- 如果 adId 是 String，需要此驗證
			@jakarta.validation.constraints.Digits(integer = 10, fraction = 0, message = "廣告編號: 請填數字-請勿超過{integer}位數")
			@jakarta.validation.constraints.Min(value = 1, message = "廣告編號: 不能小於{value}")
			@RequestParam("adId") String adIdStr, // <<-- 使用 String 接收，以利用 @NotEmpty 驗證
			ModelMap model) {
		
		Integer adId = null;
		try {
			adId = Integer.valueOf(adIdStr); // 將 String 轉換為 Integer
		}catch(NumberFormatException e){
			// 假設轉換失敗，表示輸入不是有效數字，手動添加錯誤訊息
			model.addAttribute("errorMessage", "廣告編號：請填寫有效數字格式");
			
			// 重新填充頁面所需數據
			model.addAttribute("adListData", adSvc.getAll());
			model.addAttribute("adVO", new AdVO());
			model.addAttribute("storeVO", new StoreVO());
			model.addAttribute("storeListData", storeSvc.getAll());
			return "back-end/advertisment/select_page"; // 返回查詢頁面

		}
		
		/***************************2.開始查詢資料*********************************************/
		AdVO adVO = adSvc.getOneAd(adId); // 使用 adId 查詢
		
		// 重新填充列表數據，以便錯誤發生時能回到原頁面並顯示所有列表
		List<AdVO> list = adSvc.getAll();
		model.addAttribute("adListData", list);
		// 空的AdVO 和 Store 列表，為了表單綁定及下拉選單
		model.addAttribute("adVO", new AdVO());			// 提供空的 adVO 給查詢表單
		model.addAttribute("storeVO", new StoreVO());	// 提供空的 storeVO 給查詢表單
		model.addAttribute("storeListData", storeSvc.getAll());		
		
		if(adVO == null) {
			model.addAttribute("errorMessage", "查無資料");
			return "back-end/advertisment/select_page"; // select_page.html 來查詢

		}
		/***************************3.查詢完成,準備轉交(Send the Success view)*****************/
		model.addAttribute("adVO", adVO);
		return "back-end/advertisment/select_page"; // 或 "back-end/ad/listOneAd" 顯示單一廣告詳細
	}
	
	/* 處理複合查詢，接收 HttpServletRequest 中的參數 map。
	 * 需要 AdService 中的 getAll(Map<String, String[]> map) 方法配合。
	 * 如果你的 AdService 沒有實現複合查詢，可以移除此方法。
	 */
	
	@PostMapping("listAds_ByCompositeQuery")
	public String listAllAd(HttpServletRequest req, Model model) {
		Map<String, String[]> map = req.getParameterMap();

		List<AdVO> list = adSvc.getAll(map);
		model.addAttribute("adListData", list);
		model.addAttribute("adVO", new AdVO()); // 為了複合查詢的輸入框
		model.addAttribute("storeVO", new StoreVO()); // 為了複合查詢的輸入框
		model.addAttribute("storeListData", storeSvc.getAll()); // 重新填充商店列表
		return "back-end/advertisment/listAllAd"; // 回傳顯示所有廣告的頁面，或一個專門的複合查詢結果頁面
	}
	
	/* 作法一 ? Method used to populate the List Data in view.
	 * 作用類似 EmpController 中的 referenceListData，用於填充商店的下拉選單。
	 * 在前端 Thymeleaf 模板中可能會這樣使用 (假設 StoreVO 有 getStoreName() 方法):
	 * <select th:field="*{storeVO.storeId}">
	 * <option th:each="store : ${storeListData}" th:value="${store.storeId}" th:text="${store.storeName}"></option>
	 * </select>
	 */
	@ModelAttribute("storeListData")
	protected List<StoreVO> referenceStoreListData(){
		
		// 呼叫 StoreService 來取得所有商店的列表( StoreService 中有 getAll() 方法 )
		return storeSvc.getAll();
	}

	
	
	// 統一處理 Controller 層的驗證失敗 (ConstraintViolationException)
	// 參考 EmpnoController 的 handleError
	
	@ExceptionHandler(value = { ConstraintViolationException.class})
    // @ResponseStatus(value = HttpStatus.BAD_REQUEST) // 可選，設定 HTTP 狀態碼為 400 Bad Request
	public ModelAndView handleError(HttpServletRequest req, ConstraintViolationException e, Model model) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        StringBuilder strBuilder = new StringBuilder();
        for (ConstraintViolation<?> violation : violations) {
        	strBuilder.append(violation.getMessage() + "<br>");
        }
        
        // 重新填充列表數據和下拉選單數據，以便錯誤發生時能回到原頁面並顯示所有列表和下拉選單
        List<AdVO> list = adSvc.getAll();
        model.addAttribute("adListData", list);
        model.addAttribute("adVO", new AdVO());	// 為了前端表單綁定
        model.addAttribute("storeVO", new StoreVO());	// 為了前端表單綁定
        model.addAttribute("storeListData", storeSvc.getAll());	// 重新填充商店列表
		
        String message = strBuilder.toString();
        // 假設錯誤時會返回 select_page.html
        return new ModelAndView("back-end/ad/select_page", "errorMessage", "請修正以下錯誤:<br>" + message);

		
	}										
	
	@GetMapping("/DBGifReader")
	public void dbGifReader(@RequestParam("adId") Integer adId, HttpServletResponse res) {
	    if (adId != null) {
	        AdVO adVO = adSvc.getOneAd(adId); // 確保 adSvc.getOneAd(adId) 可以獲取 AdVO
	        if (adVO != null && adVO.getAdImage() != null) {
	            try {
	                res.setContentType("image/gif"); // 或根據你的圖片類型設置，例如 "image/jpeg"
	                ServletOutputStream out = res.getOutputStream();
	                out.write(adVO.getAdImage());
	                out.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	                // 處理錯誤，例如發送 404 或其他錯誤響應
	            }
	        } else {
	            // 如果找不到圖片，可以發送 404 或返回預設圖片
	            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
	        }
	    } else {
	        res.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 缺少 adId 參數
	    }
	}
		
}