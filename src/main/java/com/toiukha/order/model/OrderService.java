package com.toiukha.order.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.toiukha.item.model.ItemService;
import com.toiukha.item.model.ItemVO;
import com.toiukha.orderitems.model.OrderItemsService;
import com.toiukha.orderitems.model.OrderItemsVO;

@Service("orderService")
public class OrderService {
	
	@Autowired
	OrderRepository repository;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private OrderItemsService orderItemsService;
	
	@Autowired
	private ItemService itemService;
	
	public void addOrder(OrderVO orderVO) {
		repository.save(orderVO);
	}
	
	public void updateOrder(OrderVO orderVO) {
		repository.save(orderVO);
	}
	
	public void deleteEmp(Integer ordId) {
		if (repository.existsById(ordId))
			repository.deleteByEmpno(ordId);
//		    repository.deleteById(empno);
	}
	
	public OrderVO getOneOrder(Integer ordId) {
		Optional<OrderVO> optional = repository.findById(ordId);
//		return optional.get();
		return optional.orElse(null);  // public T orElse(T other) : 如果值存在就回傳其值，否則回傳other的值
	}
	
	public List<OrderVO> getAll() {
		return repository.findAll();
	}
	
	public List<OrderVO> findByMemId(Integer memId) {
		return repository.findByMemId(memId);
	}
	
	// 獲取會員的已完成訂單 (假設訂單狀態 1=已完成)
	public List<OrderVO> findCompletedOrdersByMemId(Integer memId) {
		return repository.findByMemIdAndOrdSta(memId, 1);
	}
	
	// 獲取訂單及其項目詳細資訊
	public OrderWithItemsDTO getOrderWithItems(Integer ordId) {
		OrderVO order = getOneOrder(ordId);
		if (order == null) {
			return null;
		}
		
		// 獲取訂單項目
		List<OrderItemsVO> orderItems = orderItemsService.findByOrdId(ordId);
		
		// 轉換為DTO格式，包含商品資訊
		List<OrderWithItemsDTO.OrderItemDetailDTO> orderItemDetails = orderItems.stream()
			.map(orderItem -> {
				ItemVO item = itemService.getOneItem(orderItem.getItemId());
				return new OrderWithItemsDTO.OrderItemDetailDTO(orderItem, item);
			})
			.collect(Collectors.toList());
		
		return new OrderWithItemsDTO(order, orderItemDetails);
	}
	
	// 獲取會員所有已完成訂單及其項目，依訂單時間新到舊排序
	public List<OrderWithItemsDTO> getCompletedOrdersWithItems(Integer memId) {
		List<OrderVO> completedOrders = findCompletedOrdersByMemId(memId);
		completedOrders.sort((a, b) -> b.getCreDate().compareTo(a.getCreDate())); // 依時間新到舊排序
		return completedOrders.stream()
			.map(order -> getOrderWithItems(order.getOrdId()))
			.collect(Collectors.toList());
	}


}
