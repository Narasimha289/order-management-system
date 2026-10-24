
package com.nani.order_management_system.service;

import com.nani.order_management_system.dto.CreateOrderRequest;
import com.nani.order_management_system.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(
            CreateOrderRequest request,
            String userEmail
    );

    List<OrderResponse> getMyOrders(String userEmail);

    OrderResponse getMyOrderById(Long orderId, String userEmail);

    void cancelOrder(Long orderId, String userEmail);
}
