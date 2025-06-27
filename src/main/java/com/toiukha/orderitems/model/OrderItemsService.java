package com.toiukha.orderitems.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.toiukha.item.model.ItemVO;
import java.util.List;

@Service("orderitemsService")
public class OrderItemsService {

    @Autowired
    OrderItemsRepository repository;

    public void addOrderItems(OrderItemsVO orderItemsVO) {
        repository.save(orderItemsVO);
    }

    public void updateOrderItems(OrderItemsVO orderItemsVO) {
        repository.save(orderItemsVO);
    }

    public OrderItemsVO findById(Integer ordId, Integer itemId) {
        OrderItemsVO.CompositeDetail key = new OrderItemsVO.CompositeDetail(ordId, itemId);
        return repository.findById(key).orElse(null);
    }

    public void deleteById(Integer ordId, Integer itemId) {
        OrderItemsVO.CompositeDetail key = new OrderItemsVO.CompositeDetail(ordId, itemId);
        repository.deleteById(key);
    }

    public List<OrderItemsVO> findByItemId(Integer itemId) {
        return repository.findByItemId(itemId);
    }

    public List<OrderItemsVO> findByOrdId(Integer ordId) {
        return repository.findByOrdId(ordId);
    }
}
