package com.toiukha.advertisment.controller;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.toiukha.advertisment.model.AdService;
import com.toiukha.advertisment.model.AdVO;
import com.toiukha.store.model.StoreService;
import com.toiukha.store.model.StoreVO;
import com.toiukha.item.model.ItemService;
import com.toiukha.item.model.ItemVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.servlet.ServletOutputStream; // 圖片


@Controller
//@RestController
//@Validated	// 啟用Controller 層的"參數驗證"
@RequestMapping("/advertisment")	//定義Controller 處理的基礎URL路徑
public class AdController {

	@Autowired
	AdService adSvc;	// 自動注入AdService
	
	@Autowired
	StoreService storeSvc;	// 自動注入StoreService
	
	@Autowired
	ItemService itemSvc;	// 自動注入ItemService
	
	/**
	 * 獲取真實的商店資料Map
	 * @return Map<Integer, String> 商店ID和商店名稱的對應Map
	 */
	private Map<Integer, String> getStoreNamesMap() {
		Map<Integer, String> storeNamesMap = new LinkedHashMap<>();
		try {
			List<StoreVO> stores = storeSvc.findAllStores();
			for (StoreVO store : stores) {
				storeNamesMap.put(store.getStoreId(), store.getStoreName());
			}
		} catch (Exception e) {
			// 如果無法獲取資料，回傳預設資料
			System.err.println("無法獲取商店資料: " + e.getMessage());
			storeNamesMap.put(1, "預設商店");
		}
		return storeNamesMap;
	}
	
    /**
     * 檢查商家是否已登入
     */
    private boolean isStoreLoggedIn(HttpSession session) {
        Object storeObj = session.getAttribute("store");
        return storeObj != null;
    }
    
    /**
     * 檢查管理員是否已登入
     */
    private boolean isAdminLoggedIn(HttpSession session) {
        Object adminObj = session.getAttribute("admin");
        return adminObj != null;
    }
    
    /**
     * 檢查會員是否已登入
     */
    private boolean isMemberLoggedIn(HttpSession session) {
        Object memberObj = session.getAttribute("member");
        return memberObj != null;
    }
    
    /**
     * 獲取當前登入的商家ID
     */
    private Integer getCurrentStoreId(HttpSession session) {
        Object storeObj = session.getAttribute("store");
        if (storeObj != null) {
            // 假設 Store 物件有 getStoreId() 方法
            // 實際需要根據您的 StoreVO 類別來調整
            return ((com.toiukha.store.model.StoreVO) storeObj).getStoreId();
        }
        return null;
    }
    
    /**
     * 檢查商家是否有權限操作特定廣告
     */
    private boolean hasPermissionToAd(Integer adId, HttpSession session) {
        if (!isStoreLoggedIn(session)) {
            return false;
        }
        
        Integer currentStoreId = getCurrentStoreId(session);
        if (currentStoreId == null) {
            return false;
        }
        
        AdVO ad = adSvc.getOneAd(adId);
        return ad != null && ad.getStoreId().equals(currentStoreId);
    }
    

	
//	//放入StoreService後要改!!
//	@Autowired(required = false)
//	StoreService storeSvc; // 自動注入 StoreService (用於處理商店資料，例如下拉選單)
	
	// ** 處理 /advertisment/addAd 的 GET 請求，顯示新增廣告的表單頁面 **
	
	@GetMapping("addAd")
	public String addAd(HttpSession session, ModelMap model) {
		// 檢查商家是否已登入
		if (!isStoreLoggedIn(session)) {
			return "redirect:/store/login";
		}
		
		// 添加空的 AdVO 對象，供 Thymeleaf 表單綁定使用
		model.addAttribute("adVO", new AdVO());
		
		// 使用真實的商店資料
		Map<Integer, String> storeNamesMap = getStoreNamesMap();
		model.addAttribute("storeNamesMap", storeNamesMap);
		
		// 獲取當前登入的商家 ID 和商家資訊
		Integer currentStoreId = getCurrentStoreId(session);
		StoreVO currentStore = (StoreVO) session.getAttribute("store");
		
		if (currentStore != null) {
			model.addAttribute("currentStoreId", currentStoreId);
			model.addAttribute("currentStoreName", currentStore.getStoreName());
		}
		
		return "front-end/advertisment/addAd";
	}
	
	/*
	 * 處理新增廣告的 POST 請求，接收表單提交的資料並進行驗證、處理圖片上傳。
	 * @Valid 啟用 AdVO 的 Bean Validation
	 * @RequestParam("adImageFile") MultipartFile adImageFile 接收單一圖片檔案
	 * @RequestParam("storeId") Integer storeId 手動接收 storeId，然後再轉換為 StoreVO
	 */
	
	// 廣告建立
	@PostMapping("/insert")  // 接收表單
	public String insert(
			@RequestParam("adTitle") String adTitle,
			@RequestParam("adStartTime") String adStartTime,
			@RequestParam("adEndTime") String adEndTime,
			@RequestParam("adImage") MultipartFile adImageFile,
			HttpSession session,
			ModelMap model,
	        RedirectAttributes redirectAttributes
			) throws IOException {
		
		// 檢查商家是否已登入
		if (!isStoreLoggedIn(session)) {
			return "redirect:/store/login";
		}
		
		Integer currentStoreId = getCurrentStoreId(session);
		System.out.println("=== 開始處理廣告新增 ===");
		System.out.println("商家ID: " + currentStoreId);
		System.out.println("廣告標題: " + adTitle);
		
		// 手動驗證 adTitle
		if (adTitle == null || adTitle.trim().isEmpty()) {
			model.addAttribute("errorMessage", "廣告描述: 請勿空白");
			AdVO adVO = new AdVO();
			adVO.setAdTitle(adTitle);
			model.addAttribute("adVO", adVO);
			Map<Integer, String> storeNamesMap = getStoreNamesMap();
			model.addAttribute("storeNamesMap", storeNamesMap);
			
			// 重新加入商家資訊
			StoreVO currentStore = (StoreVO) session.getAttribute("store");
			if (currentStore != null) {
				model.addAttribute("currentStoreId", currentStoreId);
				model.addAttribute("currentStoreName", currentStore.getStoreName());
			}
			return "front-end/advertisment/addAd";
		}
		
		if (adTitle.length() < 10 || adTitle.length() > 200) {
			model.addAttribute("errorMessage", "廣告描述: 長度必需在10到200之間");
			AdVO adVO = new AdVO();
			adVO.setAdTitle(adTitle);
			model.addAttribute("adVO", adVO);
			Map<Integer, String> storeNamesMap = getStoreNamesMap();
			model.addAttribute("storeNamesMap", storeNamesMap);
			
			// 重新加入商家資訊
			StoreVO currentStore = (StoreVO) session.getAttribute("store");
			if (currentStore != null) {
				model.addAttribute("currentStoreId", currentStoreId);
				model.addAttribute("currentStoreName", currentStore.getStoreName());
			}
			return "front-end/advertisment/addAd";
		}
		
		// 圖片驗證
		if (adImageFile == null || adImageFile.isEmpty()) {
            model.addAttribute("errorMessage", "廣告圖片: 請上傳照片");
            AdVO adVO = new AdVO();
            adVO.setAdTitle(adTitle);
            model.addAttribute("adVO", adVO);
            Map<Integer, String> storeNamesMap = getStoreNamesMap();
            model.addAttribute("storeNamesMap", storeNamesMap);
            
            // 重新加入商家資訊
            StoreVO currentStore = (StoreVO) session.getAttribute("store");
            if (currentStore != null) {
                model.addAttribute("currentStoreId", currentStoreId);
                model.addAttribute("currentStoreName", currentStore.getStoreName());
            }
            return "front-end/advertisment/addAd";
        }
		 
		// 創建 AdVO 對象
		AdVO adVO = new AdVO();
		
		// 時間處理
		try {
			// 支援兩種格式：'yyyy-MM-dd\'T\'HH:mm' 及 'yyyy-MM-dd HH:mm:ss'
			DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
			DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime startDateTime;
			LocalDateTime endDateTime;
			try {
				startDateTime = LocalDateTime.parse(adStartTime, formatter1);
			} catch (DateTimeParseException e) {
				startDateTime = LocalDateTime.parse(adStartTime, formatter2);
			}
			try {
				endDateTime = LocalDateTime.parse(adEndTime, formatter1);
			} catch (DateTimeParseException e) {
				endDateTime = LocalDateTime.parse(adEndTime, formatter2);
			}
			Timestamp astime = Timestamp.valueOf(startDateTime);
			Timestamp aetime = Timestamp.valueOf(endDateTime);
			// 檢查時間邏輯
			if (aetime.before(astime)) {
				model.addAttribute("errorMessage", "結束時間不能早於開始時間");
				adVO.setAdTitle(adTitle);
				model.addAttribute("adVO", adVO);
				Map<Integer, String> storeNamesMap = getStoreNamesMap();
				model.addAttribute("storeNamesMap", storeNamesMap);
				
				// 重新加入商家資訊
				StoreVO currentStore = (StoreVO) session.getAttribute("store");
				if (currentStore != null) {
					model.addAttribute("currentStoreId", currentStoreId);
					model.addAttribute("currentStoreName", currentStore.getStoreName());
				}
				return "front-end/advertisment/addAd";
			}
			
			// 設置所有 AdVO 屬性
			adVO.setAdTitle(adTitle);
			adVO.setStoreId(currentStoreId); // 強制綁定登入商家
			adVO.setAdCreatedTime(new Timestamp(System.currentTimeMillis()));
			adVO.setAdStartTime(astime);
			adVO.setAdEndTime(aetime);
			adVO.setAdImage(adImageFile.getBytes());
			adVO.setAdStatus(AdVO.STATUS_PENDING); // 新廣告預設為待審核狀態
			System.out.println("時間轉換成功 - 開始: " + astime + ", 結束: " + aetime);
		} catch (DateTimeParseException e) {
			System.out.println("時間格式錯誤: " + e.getMessage());
			System.out.println("收到的開始時間: " + adStartTime);
			System.out.println("收到的結束時間: " + adEndTime);
			model.addAttribute("errorMessage", "時間格式錯誤，請使用正確的日期時間格式");
			adVO.setAdTitle(adTitle);
			model.addAttribute("adVO", adVO);
			Map<Integer, String> storeNamesMap = getStoreNamesMap();
			model.addAttribute("storeNamesMap", storeNamesMap);
			
			// 重新加入商家資訊
			StoreVO currentStore = (StoreVO) session.getAttribute("store");
			if (currentStore != null) {
				model.addAttribute("currentStoreId", currentStoreId);
				model.addAttribute("currentStoreName", currentStore.getStoreName());
			}
			return "front-end/advertisment/addAd";
		}
		
		// 執行新增
		try {
			adSvc.addAd(adVO);
			System.out.println("廣告新增成功");
			redirectAttributes.addFlashAttribute("successMessage", "廣告新增成功！");
		} catch (Exception e) {
			System.out.println("新增失敗: " + e.getMessage());
			model.addAttribute("errorMessage", "新增失敗: " + e.getMessage());
			adVO.setAdTitle(adTitle);
			model.addAttribute("adVO", adVO);
			Map<Integer, String> storeNamesMap = getStoreNamesMap();
			model.addAttribute("storeNamesMap", storeNamesMap);
			
			// 重新加入商家資訊
			StoreVO currentStore = (StoreVO) session.getAttribute("store");
			if (currentStore != null) {
				model.addAttribute("currentStoreId", currentStoreId);
				model.addAttribute("currentStoreName", currentStore.getStoreName());
			}
			return "front-end/advertisment/addAd";
		}

        return "redirect:/advertisment/myAds";
	}
	

	
    @PostMapping("/getOne_For_Update")
    public String getOne_For_Update(@RequestParam("adId") Integer adId, HttpSession session, ModelMap model) {
        return prepareUpdateForm(adId, session, model);
    }
    
	@GetMapping("/getOne_For_Update")
	public String getOne_ForUpdateViaGet(@RequestParam("adId") Integer adId, HttpSession session, Model model) {
	    return prepareUpdateForm(adId, session, (ModelMap) model);
	}
	
	// 統一的表單準備方法
	private String prepareUpdateForm(Integer adId, HttpSession session, ModelMap model) {
		// 檢查商家是否已登入
		if (!isStoreLoggedIn(session)) {
			return "redirect:/store/login";
		}
		
		// 檢查是否有權限修改此廣告
		if (!hasPermissionToAd(adId, session)) {
			model.addAttribute("errorMessage", "無權限修改此廣告！");
			return "error/403";
		}
		
	    AdVO adVO = adSvc.getOneAd(adId);
	    model.addAttribute("adVO", adVO);

        // 加入時間字串格式以配合 datetime-local 輸入欄位
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String startTimeStr = (adVO.getAdStartTime() != null)
                ? adVO.getAdStartTime().toLocalDateTime().format(formatter)
                : "";
        String endTimeStr = (adVO.getAdEndTime() != null)
                ? adVO.getAdEndTime().toLocalDateTime().format(formatter)
                : "";

        model.addAttribute("startTimeStr", startTimeStr);
        model.addAttribute("endTimeStr", endTimeStr);

        return "front-end/advertisment/update_ad_input";
	}
	
	
	
	
	// 處理更新廣告的 POST 請求，接收表單提交的資料並進行驗證、處理圖片上傳。
	
	@PostMapping("/update")
	public String update(@RequestParam("adId") Integer adId,
						 @RequestParam("adTitle") String adTitle,
						 @RequestParam("adStatus") String adStatus,
						 @RequestParam("adStartTime") String adStartTime,
						 @RequestParam("adEndTime") String adEndTime,
					 	 @RequestParam("adImageFile") MultipartFile[] parts,
					 	 HttpSession session,
						 ModelMap model) throws IOException { 				
		
		// 檢查商家是否已登入
		if (!isStoreLoggedIn(session)) {
			return "redirect:/store/login";
		}
		
		// 檢查是否有權限修改此廣告
		if (!hasPermissionToAd(adId, session)) {
			model.addAttribute("errorMessage", "無權限修改此廣告！");
			return "error/403";
		}
		
		Integer currentStoreId = getCurrentStoreId(session);
		System.out.println("=== 開始處理廣告更新 ===");
		System.out.println("廣告ID: " + adId);
		System.out.println("廣告標題: " + adTitle);
		System.out.println("廣告狀態: " + adStatus);
		System.out.println("商家ID: " + currentStoreId);
		
		// 手動驗證必要欄位
		if (adTitle == null || adTitle.trim().isEmpty()) {
			model.addAttribute("errorMessage", "廣告描述不能為空");
			// 重新設置 adVO 以便模板渲染
			AdVO adVO = adSvc.getOneAd(adId);
			model.addAttribute("adVO", adVO);
			model.addAttribute("startTimeStr", adStartTime);
			model.addAttribute("endTimeStr", adEndTime);
			return "front-end/advertisment/update_ad_input";
		}
		
		if (adTitle.length() < 10 || adTitle.length() > 200) {
			model.addAttribute("errorMessage", "廣告描述長度必須在10到200個字符之間");
			// 重新設置 adVO 以便模板渲染
			AdVO adVO = adSvc.getOneAd(adId);
			model.addAttribute("adVO", adVO);
			model.addAttribute("startTimeStr", adStartTime);
			model.addAttribute("endTimeStr", adEndTime);
			return "front-end/advertisment/update_ad_input";
		}
		
		if (adStatus == null || adStatus.trim().isEmpty()) {
			model.addAttribute("errorMessage", "請選擇廣告狀態");
			// 重新設置 adVO 以便模板渲染
			AdVO adVO = adSvc.getOneAd(adId);
			model.addAttribute("adVO", adVO);
			model.addAttribute("startTimeStr", adStartTime);
			model.addAttribute("endTimeStr", adEndTime);
			return "front-end/advertisment/update_ad_input";
		}
		
		// 處理時間轉換
		Timestamp astime = null;
		Timestamp aetime = null;
		try {
			// 使用 LocalDateTime 解析前端傳來的時間格式：2025-06-25T16:34
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
			
			System.out.println("原始開始時間: " + adStartTime);
			System.out.println("原始結束時間: " + adEndTime);
			
			LocalDateTime startDateTime = LocalDateTime.parse(adStartTime, formatter);
			LocalDateTime endDateTime = LocalDateTime.parse(adEndTime, formatter);
			
			astime = Timestamp.valueOf(startDateTime);
			aetime = Timestamp.valueOf(endDateTime);
			
			// 檢查時間邏輯
			if (aetime.before(astime)) {
				model.addAttribute("errorMessage", "結束時間不能早於開始時間");
				// 重新設置 adVO 以便模板渲染
				AdVO adVO = adSvc.getOneAd(adId);
				model.addAttribute("adVO", adVO);
				model.addAttribute("startTimeStr", adStartTime);
				model.addAttribute("endTimeStr", adEndTime);
				return "front-end/advertisment/update_ad_input";
			}
			
			System.out.println("時間轉換成功 - 開始: " + astime + ", 結束: " + aetime);
		} catch (DateTimeParseException e) {
			System.out.println("時間格式錯誤: " + e.getMessage());
			System.out.println("收到的開始時間: " + adStartTime);
			System.out.println("收到的結束時間: " + adEndTime);
			model.addAttribute("errorMessage", "時間格式錯誤，請使用正確的日期時間格式");
			// 重新設置 adVO 以便模板渲染
			AdVO adVO = adSvc.getOneAd(adId);
			model.addAttribute("adVO", adVO);
			model.addAttribute("startTimeStr", adStartTime);
			model.addAttribute("endTimeStr", adEndTime);
			return "front-end/advertisment/update_ad_input";
		}
		
		// 手動構建 AdVO 物件
		AdVO adVO = new AdVO();
		adVO.setAdId(adId);
		adVO.setAdTitle(adTitle);
		adVO.setAdStatus(Byte.valueOf(adStatus));
		adVO.setAdStartTime(astime);
		adVO.setAdEndTime(aetime);
		adVO.setStoreId(currentStoreId);

        // 處理圖片更新
        AdVO originalAd = adSvc.getOneAd(adId);
        if (parts[0].isEmpty()) {
            adVO.setAdImage(originalAd.getAdImage());
            System.out.println("保持原有圖片");
        } else {
            adVO.setAdImage(parts[0].getBytes());
            System.out.println("更新圖片，大小: " + parts[0].getSize() + " bytes");
        }
		
        adVO.setAdCreatedTime(originalAd.getAdCreatedTime()); // 保持原有創建時間
        
        // 執行更新
        try {
            System.out.println("準備更新廣告，ID: " + adVO.getAdId());
            System.out.println("更新前檢查 - 標題: " + adVO.getAdTitle());
            System.out.println("更新前檢查 - 狀態: " + adVO.getAdStatus());
            System.out.println("更新前檢查 - 開始時間: " + adVO.getAdStartTime());
            System.out.println("更新前檢查 - 結束時間: " + adVO.getAdEndTime());
            System.out.println("更新前檢查 - 商家ID: " + adVO.getStoreId());
            
            adSvc.updateAd(adVO);
            System.out.println("廣告更新成功");
            
            // 驗證更新是否真的成功
            AdVO updatedAd = adSvc.getOneAd(adVO.getAdId());
            if (updatedAd != null) {
                System.out.println("更新後驗證 - 標題: " + updatedAd.getAdTitle());
                System.out.println("更新後驗證 - 狀態: " + updatedAd.getAdStatus());
            } else {
                System.out.println("警告：更新後無法查詢到廣告");
            }
            
        } catch (Exception e) {
            System.out.println("更新失敗: " + e.getMessage());
            e.printStackTrace(); // 印出完整的錯誤堆疊
            model.addAttribute("errorMessage", "更新失敗: " + e.getMessage());
            // 重新設置 adVO 以便模板渲染
            adVO = adSvc.getOneAd(adId);
            model.addAttribute("adVO", adVO);
            model.addAttribute("startTimeStr", adStartTime);
            model.addAttribute("endTimeStr", adEndTime);
            return "front-end/advertisment/update_ad_input";
        }

        System.out.println("準備重導向到 /advertisment/myAds");
        return "redirect:/advertisment/myAds";
	}
		
		
	// 處理從列表頁面發送的"刪除"請求，根據 **廣告ID (adId)** 刪除廣告資料。
	
	@PostMapping("/delete")
	public String delete(@RequestParam("adId") String adId, HttpSession session, ModelMap model) {
		
		// 檢查商家是否已登入
		if (!isStoreLoggedIn(session)) {
			return "redirect:/store/login";
		}
		
		// 檢查是否有權限刪除此廣告
		if (!hasPermissionToAd(Integer.valueOf(adId), session)) {
			model.addAttribute("errorMessage", "無權限刪除此廣告！");
			return "error/403";
		}
		
		Integer currentStoreId = getCurrentStoreId(session);
		System.out.println("=== 開始處理廣告刪除 ===");
		System.out.println("廣告ID: " + adId);
		System.out.println("商家ID: " + currentStoreId);
		
		try {
			adSvc.deleteAd(Integer.valueOf(adId));
			System.out.println("廣告刪除成功");
		} catch (Exception e) {
			System.out.println("刪除失敗: " + e.getMessage());
			model.addAttribute("errorMessage", "刪除失敗: " + e.getMessage());
			return "front-end/advertisment/myAds";
		}
		
		return "redirect:/advertisment/myAds";
	}
	
	
	
    // 商家管理頁（只看自己 storeId 的）
    @GetMapping("/myAds")
    public String viewMyAds(HttpSession session, Model model) {
        // 檢查商家是否已登入
        if (!isStoreLoggedIn(session)) {
            return "redirect:/store/login";
        }
        
        Integer currentStoreId = getCurrentStoreId(session);
        System.out.println("=== 查看我的廣告 ===");
        System.out.println("商家ID: " + currentStoreId);
        
        List<AdVO> list = adSvc.getAllByStoreId(currentStoreId);
        
        model.addAttribute("adListData", list);
        return "front-end/advertisment/myAds";
    }
    
		// 處理 /advertisment/listAllAd 的 GET 請求，顯示所有廣告列表頁面
	@GetMapping("/listAllAd")
	public String listAllAd(ModelMap model) {
		// 只顯示已審核通過且未過期的廣告
		List<AdVO> list = adSvc.getApprovedAds();
		model.addAttribute("adListData", list);
		
		// 使用真實的商店資料
		Map<Integer, String> storeNamesMap = getStoreNamesMap();
		model.addAttribute("storeNamesMap", storeNamesMap);
		
		return "front-end/advertisment/listAllAd"; // 回傳所有廣告的列表頁面
	}
	
	// 處理查看廣告詳情的請求
	@GetMapping("/getOne_For_Display")
	public String getOne_For_Display(@RequestParam("adId") Integer adId, ModelMap model) {
		AdVO adVO = adSvc.getOneAd(adId);
		
		if (adVO == null) {
			model.addAttribute("errorMessage", "查無此廣告資料");
			return "error/404";
		}
		
		// 檢查廣告是否已過期
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		if (adVO.getAdStatus() != null && 
		    adVO.getAdStatus().equals(AdVO.STATUS_APPROVED) && 
		    adVO.getAdEndTime() != null && 
		    adVO.getAdEndTime().before(currentTime)) {
			model.addAttribute("errorMessage", "此廣告已過期");
			return "error/404";
		}
		
		// 使用真實的商店資料
		Map<Integer, String> storeNamesMap = getStoreNamesMap();
		
		model.addAttribute("adVO", adVO);
		model.addAttribute("storeNamesMap", storeNamesMap);
		return "front-end/advertisment/adDetail"; // 回傳廣告詳情頁面
	}
	
	// 處理查看商店商品列表的請求
	@GetMapping("/storeItems")
	public String viewStoreItems(@RequestParam("storeId") Integer storeId, ModelMap model) {
		try {
			// 獲取商店資訊
			Map<Integer, String> storeNamesMap = getStoreNamesMap();
			String storeName = storeNamesMap.get(storeId);
			
			if (storeName == null) {
				model.addAttribute("errorMessage", "找不到該商店資訊");
				return "error/404";
			}
			
			// 獲取該商店的所有上架商品
			List<ItemVO> storeItems = itemSvc.findByStoreId(storeId);
			
			// 只顯示已上架的商品 (itemStatus = 1)
			List<ItemVO> activeItems = storeItems.stream()
					.filter(item -> item.getItemStatus() != null && item.getItemStatus() == 1)
					.collect(java.util.stream.Collectors.toList());
			
			model.addAttribute("storeItems", activeItems);
			model.addAttribute("storeName", storeName);
			model.addAttribute("storeId", storeId);
			
			return "front-end/advertisment/storeItems"; // 回傳商店商品列表頁面
		} catch (Exception e) {
			model.addAttribute("errorMessage", "載入商店商品時發生錯誤：" + e.getMessage());
			return "error/500";
		}
	}
	
	/*
	 * 【 第二種作法 】 Method used to populate the Map Data in view. 如 : 
	 * <form:select path="storeId" id="storeId" items="${storeMapData}" />
	 */
	@ModelAttribute("storeMapData") //
	protected Map<Integer, String> referenceMapData() {
		// 使用真實的商店資料
		return getStoreNamesMap();
	}
	
	// 去除BindingResult中某個欄位的FieldError紀錄
	public BindingResult removeFieldError(AdVO adVO, BindingResult result, String removedFieldname) {
		List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
				.filter(fieldname -> !fieldname.getField().equals(removedFieldname))
				.collect(Collectors.toList());
		result = new BeanPropertyBindingResult(adVO, "adVO");
		for (FieldError fieldError : errorsListToKeep) {
			result.addError(fieldError);
		}
		return result;
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
	
	//時間轉換String to Timestamp
	 public Timestamp datePrase(String date) {
		  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
		  Date prase = null;
		  try {
			  prase = sdf.parse(date);
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
		  Timestamp time = new Timestamp(prase.getTime());
		  return time;
	 }
	
	@GetMapping("/")
	public String home() {
		return "index";
	}
	
	@GetMapping("/test")
	public String test() {
		return "測試頁面 - 應用程式正常運行";
	}
	

	
	// ========== 後台管理功能 ==========
	
	// 後台管理主頁面
	@GetMapping("/admin/dashboard")
	public String adminDashboard(HttpSession session, ModelMap model) {
		// 檢查管理員登入狀態
		if (!isAdminLoggedIn(session)) {
			return "redirect:/admins/login";
		}
		
		// 獲取統計資料
		List<AdVO> pendingAds = adSvc.getPendingAds();
		List<AdVO> approvedAds = adSvc.getApprovedAds();
		
		model.addAttribute("pendingAdsCount", pendingAds.size());
		model.addAttribute("approvedAdsCount", approvedAds.size());
		// 其他統計資料可以從其他 Service 獲取
		model.addAttribute("totalMembersCount", 0); // 暫時設為 0
		model.addAttribute("totalNotificationsCount", 0); // 暫時設為 0
		
		return "back-end/advertisment/adminDashboard";
	}
	
	// 後台廣告審核列表頁面
	@GetMapping("/admin/pending")
	public String adminPendingAds(HttpSession session, ModelMap model) {
		// 檢查管理員登入狀態
		if (!isAdminLoggedIn(session)) {
			return "redirect:/admins/login";
		}
		
		List<AdVO> pendingAds = adSvc.getPendingAds();
		model.addAttribute("pendingAds", pendingAds);
		
		// 使用真實的商店資料
		Map<Integer, String> storeNamesMap = getStoreNamesMap();
		model.addAttribute("storeNamesMap", storeNamesMap);
		
		return "back-end/advertisment/pendingAds";
	}
	
	// 後台已審核廣告列表頁面
	@GetMapping("/admin/reviewed")
	public String adminReviewedAds(HttpSession session, ModelMap model) {
		// 檢查管理員登入狀態
		if (!isAdminLoggedIn(session)) {
			return "redirect:/admins/login";
		}
		
		List<AdVO> approvedAds = adSvc.getApprovedAds();
		List<AdVO> rejectedAds = adSvc.getRejectedAds();
		
		model.addAttribute("approvedAds", approvedAds);
		model.addAttribute("rejectedAds", rejectedAds);
		
		// 使用真實的商店資料
		Map<Integer, String> storeNamesMap = getStoreNamesMap();
		model.addAttribute("storeNamesMap", storeNamesMap);
		
		return "back-end/advertisment/reviewedAds";
	}
	
	// 審核通過廣告
	@PostMapping("/admin/approve")
	public String approveAd(@RequestParam("adId") Integer adId, HttpSession session, RedirectAttributes redirectAttributes) {
		// 檢查管理員登入狀態
		if (!isAdminLoggedIn(session)) {
			return "redirect:/admins/login";
		}
		
		try {
			adSvc.approveAd(adId);
			redirectAttributes.addFlashAttribute("successMessage", "廣告審核通過成功！");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "審核失敗: " + e.getMessage());
		}
		return "redirect:/advertisment/admin/pending";
	}
	
	// 審核拒絕廣告
	@PostMapping("/admin/reject")
	public String rejectAd(@RequestParam("adId") Integer adId, HttpSession session, RedirectAttributes redirectAttributes) {
		// 檢查管理員登入狀態
		if (!isAdminLoggedIn(session)) {
			return "redirect:/admins/login";
		}
		
		try {
			adSvc.rejectAd(adId);
			redirectAttributes.addFlashAttribute("successMessage", "廣告已拒絕！");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "拒絕失敗: " + e.getMessage());
		}
		return "redirect:/advertisment/admin/pending";
	}
	
	// 停用廣告
	@PostMapping("/admin/deactivate")
	public String deactivateAd(@RequestParam("adId") Integer adId, HttpSession session, RedirectAttributes redirectAttributes) {
		// 檢查管理員登入狀態
		if (!isAdminLoggedIn(session)) {
			return "redirect:/admins/login";
		}
		
		try {
			adSvc.deactivateAd(adId);
			redirectAttributes.addFlashAttribute("successMessage", "廣告已停用！");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "停用失敗: " + e.getMessage());
		}
		        return "redirect:/advertisment/admin/reviewed";
    }
    
    /**
     * 獲取活躍廣告列表，用於前端輪播
     * @return JSON格式的活躍廣告列表
     */
    @GetMapping("/getActiveAds")
    @ResponseBody
    public List<AdVO> getActiveAds() {
        try {
            // 直接獲取已審核通過且未過期的廣告
            List<AdVO> approvedAds = adSvc.getApprovedAds();
            
            // 進一步過濾出當前時間有效的廣告（已開始且未結束）
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            
            List<AdVO> activeAds = approvedAds.stream()
                .filter(ad -> ad.getAdStartTime() != null && 
                             ad.getAdStartTime().before(currentTime))
                .limit(10) // 限制最多10個廣告
                .collect(Collectors.toList());
            
            return activeAds;
            
        } catch (Exception e) {
            System.err.println("獲取活躍廣告失敗: " + e.getMessage());
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }
}
