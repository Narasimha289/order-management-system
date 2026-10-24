
package com.nani.order_management_system.service.impl;

import com.nani.order_management_system.dto.CreateOrderRequest;
import com.nani.order_management_system.dto.OrderItemRequest;
import com.nani.order_management_system.dto.OrderResponse;
import com.nani.order_management_system.entity.Order;
import com.nani.order_management_system.entity.Product;
import com.nani.order_management_system.entity.User;
import com.nani.order_management_system.exception.ResourceNotFoundException;
import com.nani.order_management_system.repository.OrderItemRepository;
import com.nani.order_management_system.repository.OrderRepository;
import com.nani.order_management_system.repository.ProductRepository;
import com.nani.order_management_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.nani.order_management_system.entity.OrderItem;
import static org.mockito.ArgumentMatchers.eq;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        product = new Product();
        product.setId(10L);
        product.setName("Keyboard");
        product.setPrice(new BigDecimal("25.00"));
        product.setQuantity(10);
    }

    @Test
    void createOrderShouldCalculateTotalAndReduceStock() {
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(10L))
                .thenReturn(Optional.of(product));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setId(100L);
                    return order;
                });

        when(orderItemRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderRequest request = createRequest(10L, 2);

        OrderResponse response = orderService.createOrder(
                request, "user@example.com");

        assertEquals(100L, response.getId());
        assertEquals(new BigDecimal("50.00"), response.getTotalAmount());
        assertEquals("PENDING", response.getStatus());
        assertEquals(8, product.getQuantity());
        assertEquals(1, response.getItems().size());

        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).saveAll(anyList());
    }

    @Test
    void createOrderShouldCombineDuplicateProductItems() {
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(10L))
                .thenReturn(Optional.of(product));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setId(101L);
                    return order;
                });

        when(orderItemRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(List.of(
                createItem(10L, 2),
                createItem(10L, 3)
        ));

        OrderResponse response = orderService.createOrder(
                request, "user@example.com");

        assertEquals(new BigDecimal("125.00"), response.getTotalAmount());
        assertEquals(5, product.getQuantity());
        assertEquals(1, response.getItems().size());

        verify(productRepository, times(1)).findById(10L);
    }

    @Test
    void createOrderShouldRejectInsufficientStock() {
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(10L))
                .thenReturn(Optional.of(product));

        CreateOrderRequest request = createRequest(10L, 11);

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(
                        request, "user@example.com")
        );

        assertEquals(10, product.getQuantity());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getMyOrderByIdShouldRejectAnotherUsersOrder() {
        User anotherUser = new User();
        anotherUser.setId(2L);

        Order order = new Order();
        order.setId(100L);
        order.setUser(anotherUser);

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getMyOrderById(
                        100L, "user@example.com")
        );

        verify(orderItemRepository, never()).findByOrder_Id(anyLong());
    }

    @Test
    void cancelOrderShouldRestoreStockAndMarkOrderCancelled() {
        Order order = new Order();
        order.setId(100L);
        order.setUser(user);
        order.setStatus("PENDING");

        OrderItem item = new OrderItem();
        item.setId(200L);
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("25.00"));

        product.setQuantity(8);

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(orderItemRepository.findByOrder_Id(100L))
                .thenReturn(List.of(item));

        orderService.cancelOrder(100L, "user@example.com");

        assertEquals("CANCELLED", order.getStatus());
        assertEquals(10, product.getQuantity());

        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrderShouldRejectAlreadyCancelledOrder() {
        Order order = new Order();
        order.setId(100L);
        order.setUser(user);
        order.setStatus("CANCELLED");

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalStateException.class,
                () -> orderService.cancelOrder(
                        100L, "user@example.com")
        );

        verify(orderItemRepository, never()).findByOrder_Id(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    private CreateOrderRequest createRequest(
            Long productId,
            int quantity
    ) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(List.of(createItem(productId, quantity)));
        return request;
    }

    private OrderItemRequest createItem(
            Long productId,
            int quantity
    ) {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }
}
