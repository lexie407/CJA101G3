package com.toiukha.orderitems.controller;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.toiukha.checkmacvalue.EcpayCheckMacValueGenerator;
import com.toiukha.coupon.model.CouponService;
import com.toiukha.coupon.model.CouponVO;
import com.toiukha.memcoupons.model.MemCouponsService;
import com.toiukha.memcoupons.model.MemCouponsVO;
import com.toiukha.order.model.OrderService;
import com.toiukha.order.model.OrderVO;
import com.toiukha.orderitems.model.OrderItemsService;
import com.toiukha.orderitems.model.OrderItemsVO;
import com.toiukha.paymentlog.model.PaymentLogService;
import com.toiukha.paymentlog.model.PaymentLogVO;
import com.toiukha.sentitem.model.SentItemService;
import com.toiukha.sentitem.model.SentItemVO;
import com.toiukha.item.model.ItemService;
import com.toiukha.item.model.ItemVO;
import com.toiukha.members.model.MembersVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/order")
public class OrderItemsController {

	@Autowired
	OrderService orderSvc;

	@Autowired
	OrderItemsService orderitmeSvc;
	
	@Autowired
	MemCouponsService memCouponsSvc;
	
	@Autowired
	CouponService couponSvc;
	
	@Autowired
	PaymentLogService paymentLogSvc;
	
	@Autowired
	SentItemService sentItemSvc;
	
	@Autowired
	ItemService itemSvc;

	@Autowired
	RedisTemplate<String, String> redisTemplate;

	/**
	 * 處理購物車結帳請求。
	 * 此方法會建立訂單、訂單明細，處理優惠券折扣，最後產生導向綠界支付的表單。
	 *
	 * @param memId   會員ID (此參數未使用，直接從session獲取)
	 * @param params1 包含所有請求參數的Map，包括購物車項目和所選優惠券
	 * @param session HTTP 會話，用於獲取會員ID
	 * @return 返回一個包含自動提交表單的HTML字串，將用戶重定向到ECPay付款頁面
	 * @throws Exception 可能拋出異常
	 */
	@PostMapping("/checkout")
	@ResponseBody
	public String checkout(@RequestParam("memId") String memId, @RequestParam Map<String, String> params1,
			HttpSession session, HttpServletRequest request) throws Exception {
		
		String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

		// 結帳前先檢查所有商品庫存是否足夠
		int idx = 0;
		while (true) {
			String itemIdStr = params1.get("items[" + idx + "].itemId");
			String quantityStr = params1.get("items[" + idx + "].quantity");
			if (itemIdStr == null || quantityStr == null) break;
			int itemId = Integer.parseInt(itemIdStr);
			int quantity = Integer.parseInt(quantityStr);
			ItemVO item = itemSvc.getOneItem(itemId);
			if (item == null || item.getStockQuantity() < quantity) {
				return "<script>alert('商品庫存不足，請重新選擇！');window.history.back();</script>";
			}
			idx++;
		}

		// 創建一筆新的訂單紀錄 (OrderVO)
		OrderVO orderVO = new OrderVO();
		MembersVO member = (MembersVO) session.getAttribute("member");
		 if (member == null) {
		        return "redirect:/login"; // 如果沒有登入，重定向到登入頁
		    }
		int memberId = member.getMemId();
		session.setAttribute("memId", memberId);
		
		orderVO.setMemId(memberId);
		orderVO.setCreDate(new Timestamp(System.currentTimeMillis()));
		orderVO.setOrdSta(1); // 設定訂單狀態為 1 (處理中/未付款)
		orderSvc.addOrder(orderVO);
		System.out.println(orderVO.getOrdId());
		session.setAttribute("lastOrderId", orderVO.getOrdId());

		// 遍歷從前端傳來的購物車項目，並逐一存入訂單明細 (OrderItemsVO)
		idx = 0;
		int total = 0; // 初始化訂單原始總金額
		while (true) {
			String itemIdStr = params1.get("items[" + idx + "].itemId");
			String priceStr = params1.get("items[" + idx + "].price");
			String quantityStr = params1.get("items[" + idx + "].quantity");
			if (itemIdStr == null || priceStr == null || quantityStr == null)
				break; // 如果找不到項目，表示遍歷完成

			int itemId = Integer.parseInt(itemIdStr);
			int price = Integer.parseInt(priceStr);
			int quantity = Integer.parseInt(quantityStr);

			total = total + price * quantity; // 累加計算訂單原始總金額
			
			// 創建訂單明細
			OrderItemsVO orderitemsVO = new OrderItemsVO();
			orderitemsVO.setOrdId(orderVO.getOrdId()); // 關聯到剛才創建的訂單
			orderitemsVO.setItemId(itemId);
			orderitemsVO.setOrdTotal(quantity);
			orderitemsVO.setDiscPrice(price);
			orderitemsVO.setOriPrice(0); // 原始價格欄位，此處暫設為0
			orderitmeSvc.addOrderItems(orderitemsVO);

			System.out.println("已存入: " + itemId + ", " + price + ", " + quantity);
			idx++;
		}
		
		// 處理優惠券邏輯
		String selectedCouponId = params1.get("selectedCouponId");
		int couponDiscount = 0; // 初始化優惠券折扣金額
		int couponIdForLog = 0; // 用於紀錄至PaymentLog的優惠券ID
		
		// 檢查前端是否有傳來有效的優惠券ID
		if (selectedCouponId != null && !selectedCouponId.isEmpty()) {
			try {
				int couponId = Integer.parseInt(selectedCouponId);
				CouponVO coupon = couponSvc.getOneCoupon(couponId); // 查詢優惠券資訊
				
				if (coupon != null) {
					couponDiscount = coupon.getDiscValue(); // 獲取折扣金額
					couponIdForLog = coupon.getCouId(); // 記下使用的優惠券ID
					
					// 更新會員優惠券 (MemCoupons) 的狀態為「已使用」
					MemCouponsVO memCoupon = memCouponsSvc.finById(memberId, couponId);
					if (memCoupon != null) {
						memCoupon.setCoupSta(1); // 狀態 1 表示已使用
						memCouponsSvc.updateMemCoupons(memCoupon);
					}
				}
			} catch (NumberFormatException e) {
				// 如果ID格式不正確，則忽略此優惠券
				System.out.println("優惠券ID格式錯誤: " + selectedCouponId);
			}
		}
		
		// 計算最終應付總金額（原始總金額 - 優惠券折扣）
		int finalTotal = Math.max(0, total - couponDiscount);
		System.out.println("原價: " + total + ", 優惠券折抵: " + couponDiscount + ", 最終金額: " + finalTotal);
		
		// --- 開始組裝綠界支付 (ECPay) 所需的參數 ---
		// 1. 組裝參數
		Map<String, String> params = new LinkedHashMap<>();
		params.put("MerchantID", "3002607");
		params.put("MerchantTradeNo", "Test" + System.currentTimeMillis());
		params.put("MerchantTradeDate",
				new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new java.util.Date()));
		params.put("PaymentType", "aio");
		params.put("TotalAmount", String.valueOf(finalTotal)); // 付款金額使用扣除優惠券後的最終金額
		params.put("TradeDesc", "島遊Kha");
		params.put("ItemName", "電子票券");
		params.put("ReturnURL", baseUrl+"/order/notify"); // ECPay 處理完畢後，回調通知的URL
		params.put("OrderResultURL", baseUrl + "/order/checkoutResult?orderId=" + orderVO.getOrdId());// 付款完畢後，瀏覽器自動跳轉的網址

		params.put("ChoosePayment", "ALL");
		
		// ★ 使用自訂欄位 CustomField1 來傳遞我們系統的訂單ID
		params.put("CustomField1", String.valueOf(orderVO.getOrdId()));
		// ★ 使用自訂欄位 CustomField2 來傳遞使用的優惠券ID
		params.put("CustomField2", String.valueOf(couponIdForLog));

		// 2. 產生 CheckMacValue (交易驗證碼)
		String checkMacValue = EcpayCheckMacValueGenerator.generate(params, "pwFHCqoQZGmho4w6", "EkRm7iFT261dpevs");
		params.put("CheckMacValue", checkMacValue);

		// 3. 產生一個會自動提交的 HTML form
		StringBuilder sb = new StringBuilder();
		sb.append("<form id='ecpay' method='post' action='https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5'>");
		for (Map.Entry<String, String> entry : params.entrySet()) {
			sb.append("<input type='hidden' name='" + entry.getKey() + "' value='" + entry.getValue() + "'/>");
		}
		sb.append("</form>");
		sb.append("<script>document.getElementById('ecpay').submit();</script>");

		// 4. 將這段 HTML 返回給前端，瀏覽器收到後會自動執行 script 並跳轉到 ECPay 付款頁面
		// 購物完成後，刪除購物車內容
		try {
			String cartKey = "cart:" + memId;
			redisTemplate.delete(cartKey);
		} catch (Exception e) {
			System.out.println("刪除購物車失敗: " + e.getMessage());
		}
		return sb.toString();
	}
	 @PostMapping("/notify")
	    @ResponseBody
	    public String ecpayNotify(@RequestParam Map<String, String> params) {
	        try {
	            System.out.println("收到綠界回調通知: " + params);
	            
	            // 1. 驗證CheckMacValue (安全驗證)
	            String receivedCheckMacValue = params.get("CheckMacValue");
	            params.remove("CheckMacValue"); // 移除CheckMacValue後再重新計算
	            
	            // 重新產生CheckMacValue進行比對
	            String calculatedCheckMacValue = EcpayCheckMacValueGenerator.generate(
	                params, 
	                "pwFHCqoQZGmho4w6", // HashKey
	                "EkRm7iFT261dpevs"  // HashIV
	            );
	            
	            // 驗證CheckMacValue是否正確
	            if (!calculatedCheckMacValue.equals(receivedCheckMacValue)) {
	                System.out.println("CheckMacValue驗證失敗");
	                return "0|ErrorMessage"; // 回傳失敗給綠界
	            }
	            
	            // 2. 取得付款結果參數
	            String rtnCode = params.get("RtnCode");           // 交易狀態：1=成功，其他=失敗
	            String merchantTradeNo = params.get("MerchantTradeNo"); // 訂單編號
	            String paymentDate = params.get("PaymentDate");   // 付款時間
	            String paymentType = params.get("PaymentType");   // 付款方式
	            String tradeAmt = params.get("TradeAmt");		  // 付款金額
	            String tradeNo = params.get("TradeNo");          // 綠界交易編號
	            
	            System.out.println("付款結果: " + rtnCode);
	            System.out.println("訂單編號: " + merchantTradeNo);
	            System.out.println("付款時間: " + paymentDate);
	            
	            // 3. 根據付款結果更新訂單狀態
	            if ("1".equals(rtnCode)) {
	                // 付款成功 - 更新訂單狀態
	                try {
	                    // ★ 從 ECPay 通知返回的 CustomField1 欄位取得我們自己的訂單ID
	                    String orderIdStr = params.get("CustomField1");
	                    if (orderIdStr == null || orderIdStr.isEmpty()) {
	                    	System.out.println("CustomField1 為空，無法取得訂單ID");
	                    	return "0|Error"; // 或者其他錯誤處理
	                    }
	                    int orderId = Integer.parseInt(orderIdStr);
	                    
	                    // 更新訂單狀態為已付款
	                    OrderVO order = orderSvc.getOneOrder(orderId);
	                    if (order != null) {
	                        order.setOrdSta(2); // 狀態2表示已付款
	                        orderSvc.updateOrder(order);
	                        System.out.println("訂單狀態已更新為已付款");
	                        
	                        // --- 新增付款紀錄 (PaymentLog) ---
	                        System.out.println("開始新增付款紀錄 (PaymentLog)...");
	                        PaymentLogVO paymentLog = new PaymentLogVO();
	                        
	                        paymentLog.setOrdId(orderId);
	                        paymentLog.setMemId(order.getMemId());
	                        
	                        // 設定優惠券ID (從 CustomField2 取得)
	                        String couponIdStr = params.get("CustomField2");
	                        if (couponIdStr != null && !couponIdStr.isEmpty() && !couponIdStr.equals("0")) {
	                            paymentLog.setCouId(Integer.parseInt(couponIdStr));
	                        }
	                        
	                        // 設定付款金額 (來自ECPay的TradeAmt)
	                        paymentLog.setAmoFin(Integer.parseInt(tradeAmt));
	                        paymentLog.setAmoPaid(Integer.parseInt(tradeAmt));
	                        
	                        // 設定付款時間 (來自ECPay的PaymentDate)
	                        try {
	                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	                            paymentLog.setPaidAt(new Timestamp(sdf.parse(paymentDate).getTime()));
	                        } catch (Exception e) {
	                            System.out.println("解析付款時間失敗，使用當前系統時間");
	                            paymentLog.setPaidAt(new Timestamp(System.currentTimeMillis()));
	                        }
	                        
	                        // 設定付款狀態 (1: 已付款)
	                        paymentLog.setPaySta(1);
	                        
	                        paymentLogSvc.addPaymentLog(paymentLog);
	                        System.out.println("付款紀錄 (PaymentLog) 新增成功 For OrdId: " + orderId);
	                        
	                        // --- 根據訂單明細，建立已發送票券 (SentItem) 紀錄 ---
	                        System.out.println("開始建立已發送票券 (SentItem)...");
	                        List<OrderItemsVO> orderItems = orderitmeSvc.findByOrdId(orderId);
	                        
	                        for (OrderItemsVO itemDetail : orderItems) {
	                            Integer itemId = itemDetail.getItemId();
	                            ItemVO item = itemSvc.getOneItem(itemId);
	                            if (item == null) {
	                                System.out.println("警告：找不到商品資訊 for itemId: " + itemId + "，無法建立 SentItem。");
	                                continue;
	                            }
	                            int quantity = itemDetail.getOrdTotal();
	                            // 建立票券
	                            for (int i = 0; i < quantity; i++) {
	                                SentItemVO sentItem = new SentItemVO();
	                                sentItem.setItemId(itemId);
	                                sentItem.setStoreId(item.getStoreId());
	                                sentItem.setMemId(order.getMemId());
	                                sentItem.setItemStatus(0); // 0: 未使用
	                                sentItemSvc.addSentItem(sentItem);
	                            }
	                            // 扣庫存
	                            int newStock = item.getStockQuantity() - quantity;
	                            item.setStockQuantity(Math.max(0, newStock));
	                            // 若庫存=0，自動下架
	                            if (item.getStockQuantity() <= quantity) {
	                                item.setItemStatus(0); // 0: 下架
	                            }
	                            itemSvc.updateItem(item);
	                        }
	                        System.out.println("已發送票券 (SentItem) 建立完成 for OrdId: " + orderId);
	                    }
	                    
	                } catch (Exception e) {
	                    System.out.println("更新訂單狀態失敗: " + e.getMessage());
	                }
	            } else {
	                // 付款失敗 - 可以記錄失敗原因
	                System.out.println("付款失敗，錯誤代碼: " + rtnCode);
	                // 可以更新訂單狀態為付款失敗
	            }
	            
	            // 4. 回傳成功訊息給綠界 (必須回傳 "1|OK")
	            return "1|OK";
	            
	        } catch (Exception e) {
	            System.out.println("處理綠界回調時發生錯誤: " + e.getMessage());
	            return "0|ErrorMessage";
	        }
	    }

	@PostMapping("/checkoutResult")
	public String checkoutResult(@RequestParam(value = "orderId", required = false) Integer orderId, HttpSession session, Model model) {
	    if (orderId == null) {
	        Object lastOrderId = session.getAttribute("lastOrderId");
	        if (lastOrderId != null) {
	            orderId = (Integer) lastOrderId;
	        }
	    }
	    if (orderId == null) {
	        model.addAttribute("order", null);
	        return "front-end/order/checkoutResult";
	    }
	    OrderVO order = orderSvc.getOneOrder(orderId);
	    if (order != null) {
	        List<OrderItemsVO> orderItems = orderitmeSvc.findByOrdId(orderId);
	        int totalAmount = 0;
	        for (OrderItemsVO item : orderItems) {
	            ItemVO itemVO = itemSvc.getOneItem(item.getItemId());
	            if (itemVO != null) {
	                item.setItemName(itemVO.getItemName());
	            }
	            totalAmount += item.getOrdTotal() * item.getDiscPrice();
	        }
	        // 查詢付款紀錄取得優惠券ID
	        int couponDiscount = 0;
	        Integer couponId = null;
	        PaymentLogVO paymentLog = paymentLogSvc.getByOrdId(orderId);
	        if (paymentLog != null) {
	            couponId = paymentLog.getCouId();
	            if (couponId != null && couponId != 0) {
	                CouponVO coupon = couponSvc.getOneCoupon(couponId);
	                if (coupon != null) {
	                    couponDiscount = coupon.getDiscValue();
	                }
	            }
	        }
	        int finalAmount = Math.max(0, totalAmount - couponDiscount);
	        order.setOrderItems(orderItems);
	        model.addAttribute("totalAmount", totalAmount);
	        model.addAttribute("couponDiscount", couponDiscount);
	        model.addAttribute("finalAmount", finalAmount);
	    }
	    model.addAttribute("order", order);
	    model.addAttribute("currentPage", "store");
	    return "front-end/order/checkoutResult";
	}

	@PostMapping("/commentOrderItem")
	@ResponseBody
	public Map<String, Object> commentOrderItem(
			@RequestParam("ordId") Integer ordId,
			@RequestParam("itemId") Integer itemId,
			@RequestParam("score") Integer score,
			@RequestParam("content") String content, Model model) {
		Map<String, Object> result = new LinkedHashMap<>();
		try {
			// 查詢該訂單項目
			OrderItemsVO orderItem = orderitmeSvc.findById(ordId, itemId);
			if (orderItem == null) {
				result.put("success", false);
				result.put("message", "找不到訂單項目");
				return result;
			}
			// 更新分數與評論
			orderItem.setScore(score);
			orderItem.setContent(content);
			orderitmeSvc.updateOrderItems(orderItem);
			model.addAttribute("currentPage", "store");
			model.addAttribute("activeItem", "orderList");
			result.put("success", true);
		} catch (Exception e) {
			result.put("success", false);
			result.put("message", e.getMessage());
		}
		return result;
		
		
	}
}
