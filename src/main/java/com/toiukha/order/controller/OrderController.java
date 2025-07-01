package com.toiukha.order.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.toiukha.item.model.ItemService;
import com.toiukha.item.model.ItemVO;
import com.toiukha.members.model.MembersVO;
import com.toiukha.order.model.OrderService;
import com.toiukha.order.model.OrderVO;
import com.toiukha.order.model.OrderWithItemsDTO;
import com.toiukha.orderitems.model.OrderItemsService;
import com.toiukha.orderitems.model.OrderItemsVO;
import com.toiukha.store.model.StoreVO;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/order")
public class OrderController {
	
	@Autowired
	ItemService itemSvc;
	
	@Autowired
	OrderItemsService orderitmeSvc;
	
	@Autowired
	OrderService orderSvc;
	
	@GetMapping("addOrder")
	public String addEmp(ModelMap model) {
		OrderVO orderVO = new OrderVO();
		model.addAttribute("orderVO", orderVO);
		return "back-end/order/addOrder";
	}
	
	@PostMapping("insert")
	public String insert(@Valid OrderVO orderVO,BindingResult result, ModelMap model) {

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		if(result.hasErrors()) {
			return "back-end/order/addOrder";
		}
		/*************************** 2.開始新增資料 *****************************************/
		// EmpService empSvc = new EmpService();
	    orderVO.setCreDate(new Timestamp(System.currentTimeMillis()));
		orderSvc.addOrder(orderVO);
		/*************************** 3.新增完成,準備轉交(Send the Success view) **************/
		List<OrderVO> list = orderSvc.getAll();
		model.addAttribute("orderListData", list); // for listAllEmp.html 第85行用
		model.addAttribute("success", "- (新增成功)");
		return "redirect:/order/listAllOrder"; // 新增成功後重導至IndexController_inSpringBoot.java的第58行@GetMapping("/emp/listAllEmp")
	}
	
	@PostMapping("getOne_For_Update")
	public String getOne_For_Update(@RequestParam("ordId") String ordId, ModelMap model) {
		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		/*************************** 2.開始查詢資料 *****************************************/
		// EmpService empSvc = new EmpService();
		OrderVO orderVO = orderSvc.getOneOrder(Integer.valueOf(ordId));

		/*************************** 3.查詢完成,準備轉交(Send the Success view) **************/
		model.addAttribute("orderVO", orderVO);
		return "back-end/order/update_order_input"; // 查詢完成後轉交update_emp_input.html
	}
	
	@PostMapping("update")
	public String update(@Valid OrderVO orderVO, BindingResult result, ModelMap model){

		/*************************** 1.接收請求參數 - 輸入格式的錯誤處理 ************************/
		if(result.hasErrors()) {
			return "back-end/order/update_order_input";
		}
		/*************************** 2.開始修改資料 *****************************************/
		// EmpService empSvc = new EmpService();
		orderVO.setCreDate(orderSvc.getOneOrder(orderVO.getOrdId()).getCreDate());
		orderSvc.updateOrder(orderVO);

		/*************************** 3.修改完成,準備轉交(Send the Success view) **************/
		model.addAttribute("success", "- (修改成功)");
		orderVO = orderSvc.getOneOrder(Integer.valueOf(orderVO.getOrdId()));
		model.addAttribute("orderVO", orderVO);
		return "back-end/order/listOneOrder"; // 修改成功後轉交listOneEmp.html
	}
	
	@GetMapping("/store_listAllOrder")
	public String storeOrderManage(Model model,HttpSession session) {
	    // 1. 查出該廠商所有商品
		Object storeObj = session.getAttribute("store");
		StoreVO store = (StoreVO) storeObj;
		int storeId = store.getStoreId();
		session.setAttribute("storeId", storeId);
		
	    List<ItemVO> items = itemSvc.findByStoreId(storeId);
	    List<OrderItemsVO> allOrderItems = new ArrayList<>();
	    for (ItemVO item : items) {
	        List<OrderItemsVO> orderItems = orderitmeSvc.findByItemId(item.getItemId());
	        for (OrderItemsVO orderItem : orderItems) {
	            // 補商品名稱
	            orderItem.setItemName(item.getItemName());
	            // 補訂單主檔
	            OrderVO orderVO = orderSvc.getOneOrder(orderItem.getOrdId());
	            orderItem.setOrderVO(orderVO);
				 System.out.println("訂單編號:" + orderItem.getOrdId() +
                               " 商品編號:" + orderItem.getItemId() +
                               " 評分:" + orderItem.getScore() +
                               " 評論:" + orderItem.getContent());
	        }
	        allOrderItems.addAll(orderItems);
	    }
	    model.addAttribute("orderItems", allOrderItems);
	    model.addAttribute("currentPage", "order");
		model.addAttribute("activeItem", "store_listAllOrder");
	    return "back-end/order/store_listAllOrder";
	}
	
	//全部訂單資訊
	@GetMapping("listAllOrder")
	public String listAllOrder(Model model) {
	    return "back-end/order/listAllOrder";
	}

	@ModelAttribute("orderListData")
	protected List<OrderVO> referenceListData(Model model) {
	    List<OrderVO> list = orderSvc.getAll();
	    return list;
	}
	
	//會員登入查看訂單
	@GetMapping("listAllOrder_mem")
	public String listAllOrder_mem(Model model) {
	    return "back-end/order/listAllOrder";
	}
	@ModelAttribute("orderListData_mem")
	protected List<OrderVO> referenceListData1(Model model,HttpSession session) {
		MembersVO member = (MembersVO) session.getAttribute("member");

		int memId = member.getMemId();
		session.setAttribute("memId", memId);

		
		List<OrderVO> list = orderSvc.findByMemId(memId);
	    return list;
	}
	
	// 會員查看已完成訂單及其項目
	@GetMapping("listCompletedOrders")
	public String listCompletedOrders(Model model, HttpSession session) {
		MembersVO member = (MembersVO) session.getAttribute("member");

		int memId = member.getMemId();
		session.setAttribute("memId", memId);
		model.addAttribute("currentPage", "store");
		model.addAttribute("activeItem", "orderList");
		return "front-end/order/listCompletedOrders";
	}
	
	//會員查看訂單項目
	@ModelAttribute("completedOrdersWithItems")
	protected List<OrderWithItemsDTO> referenceCompletedOrdersData(Model model, HttpSession session) {
		MembersVO member = (MembersVO) session.getAttribute("member");

		int memId = member.getMemId();
		session.setAttribute("memId", memId);
		
		List<OrderWithItemsDTO> list = orderSvc.getCompletedOrdersWithItems(memId);
		return list;
	}
}
