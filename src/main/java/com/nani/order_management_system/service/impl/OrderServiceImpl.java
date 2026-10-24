
package com.nani.order_management_system.service.impl;

import com.nani.order_management_system.dto.CreateOrderRequest;
import com.nani.order_management_system.dto.OrderItemRequest;
import com.nani.order_management_system.dto.OrderItemResponse;
import com.nani.order_management_system.dto.OrderResponse;
import com.nani.order_management_system.entity.Order;
import com.nani.order_management_system.entity.OrderItem;
import com.nani.order_management_system.entity.Product;
import com.nani.order_management_system.entity.User;
import com.nani.order_management_system.exception.ResourceNotFoundException;
import com.nani.order_management_system.repository.OrderItemRepository;
import com.nani.order_management_system.repository.OrderRepository;
import com.nani.order_management_system.repository.ProductRepository;
import com.nani.order_management_system.repository.UserRepository;
import com.nani.order_management_system.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(
            CreateOrderRequest request,
            String userEmail
    ) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Map<Long, Integer> requestedQuantities = new LinkedHashMap<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            if (itemRequest.getProductId() == null
                    || itemRequest.getQuantity() == null
                    || itemRequest.getQuantity() < 1) {
                throw new IllegalArgumentException(
                        "Each item must have a product ID and quantity of at least 1");
            }

            requestedQuantities.merge(
                    itemRequest.getProductId(),
                    itemRequest.getQuantity(),
                    Math::addExact
            );
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus("PENDING");
        order.setTotalAmount(BigDecimal.ZERO);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry
                : requestedQuantities.entrySet()) {

            Long productId = entry.getKey();
            int quantity = entry.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Product not found: " + productId));

            if (product.getQuantity() < quantity) {
                throw new IllegalArgumentException(
                        "Insufficient stock for product: "
                                + product.getName());
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(quantity));

            totalAmount = totalAmount.add(subtotal);

            product.setQuantity(product.getQuantity() - quantity);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(unitPrice);

            orderItems.add(orderItem);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        orderItemRepository.saveAll(orderItems);

        return mapToOrderResponse(savedOrder, orderItems);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return orderRepository.findByUser_Id(user.getId())
                .stream()
                .map(order -> {
                    List<OrderItem> items =
                            orderItemRepository.findByOrder_Id(order.getId());

                    return mapToOrderResponse(order, items);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMyOrderById(
            Long orderId,
            String userEmail
    ) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Order not found");
        }

        List<OrderItem> items =
                orderItemRepository.findByOrder_Id(orderId);

        return mapToOrderResponse(order, items);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Order not found");
        }

        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalStateException(
                    "Only pending orders can be cancelled");
        }

        List<OrderItem> items =
                orderItemRepository.findByOrder_Id(orderId);

        for (OrderItem item : items) {
            Product product = item.getProduct();

            product.setQuantity(
                    Math.addExact(
                            product.getQuantity(),
                            item.getQuantity()
                    )
            );
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }

    private OrderResponse mapToOrderResponse(
            Order order,
            List<OrderItem> orderItems
    ) {
        OrderResponse response = new OrderResponse();

        response.setId(order.getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());

        List<OrderItemResponse> itemResponses = orderItems.stream()
                .map(item -> {
                    OrderItemResponse itemResponse =
                            new OrderItemResponse();

                    itemResponse.setProductId(item.getProduct().getId());
                    itemResponse.setProductName(item.getProduct().getName());
                    itemResponse.setQuantity(item.getQuantity());
                    itemResponse.setUnitPrice(item.getUnitPrice());

                    BigDecimal subtotal = item.getUnitPrice().multiply(
                            BigDecimal.valueOf(item.getQuantity()));

                    itemResponse.setSubtotal(subtotal);

                    return itemResponse;
                })
                .toList();

        response.setItems(itemResponses);

        return response;
    }
}
