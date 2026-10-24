
package com.nani.order_management_system.controller;

import com.nani.order_management_system.dto.CreateOrderRequest;
import com.nani.order_management_system.dto.OrderResponse;
import com.nani.order_management_system.security.JwtService;
import com.nani.order_management_system.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    private UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken(
                "test@example.com", null
        );
    }

    @Test
    void createOrderShouldReturn201() throws Exception {
        OrderResponse response = new OrderResponse();
        response.setId(1L);
        response.setTotalAmount(new BigDecimal("1598.00"));
        response.setStatus("PENDING");
        response.setCreatedAt(LocalDateTime.now());
        response.setItems(List.of());

        when(orderService.createOrder(
                any(CreateOrderRequest.class),
                eq("test@example.com")
        )).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {
                                      "productId": 1,
                                      "quantity": 2
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.totalAmount").value(1598.00))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getMyOrdersShouldReturn200() throws Exception {
        OrderResponse response = new OrderResponse();
        response.setId(1L);
        response.setTotalAmount(new BigDecimal("799.00"));
        response.setStatus("PENDING");
        response.setCreatedAt(LocalDateTime.now());
        response.setItems(List.of());

        when(orderService.getMyOrders("test@example.com"))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/orders")
                        .principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getMyOrderByIdShouldReturn200() throws Exception {
        OrderResponse response = new OrderResponse();
        response.setId(1L);
        response.setTotalAmount(new BigDecimal("799.00"));
        response.setStatus("PENDING");
        response.setCreatedAt(LocalDateTime.now());
        response.setItems(List.of());

        when(orderService.getMyOrderById(1L, "test@example.com"))
                .thenReturn(response);

        mockMvc.perform(get("/api/orders/1")
                        .principal(authentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.totalAmount").value(799.00));
    }

    @Test
    void cancelOrderShouldReturn204() throws Exception {
        mockMvc.perform(put("/api/orders/1/cancel")
                        .principal(authentication()))
                .andExpect(status().isNoContent());
    }
}
