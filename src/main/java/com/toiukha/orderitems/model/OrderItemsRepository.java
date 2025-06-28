package com.toiukha.orderitems.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.List;

public interface OrderItemsRepository extends JpaRepository<OrderItemsVO, OrderItemsVO.CompositeDetail> {

    public static class CompositeDetail implements Serializable {
        // ... 你的內容 ...
    }
    List<OrderItemsVO> findByItemId(Integer itemId);
    List<OrderItemsVO> findByOrdId(Integer ordId);
}
