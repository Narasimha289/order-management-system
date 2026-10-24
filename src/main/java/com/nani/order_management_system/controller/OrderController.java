
package com.nani.order_management_system.controller;

import com.nani.order_management_system.dto.CreateOrderRequest;
import com.nani.order_management_system.dto.OrderResponse;
import com.nani.order_management_system.service.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication
    ) {
        return orderService.createOrder(
                request,
                authentication.getName()
        );
    }

    @GetMapping
    public List<OrderResponse> getMyOrders(
            Authentication authentication
    ) {
        return orderService.getMyOrders(authentication.getName());
    }

    @GetMapping("/{orderId}")
    public OrderResponse getMyOrderById(
            @PathVariable Long orderId,
            Authentication authentication
    ) {
        return orderService.getMyOrderById(
                orderId,
                authentication.getName()
        );
    }

    @PutMapping("/{orderId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication
    ) {
        orderService.cancelOrder(
                orderId,
                authentication.getName()
        );
    }
}
