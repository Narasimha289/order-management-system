package com.nani.order_management_system.service;

import com.nani.order_management_system.dto.LoginRequest;
import com.nani.order_management_system.dto.LoginResponse;
import com.nani.order_management_system.dto.UserRequest;
import com.nani.order_management_system.dto.UserResponse;

public interface UserService {

    UserResponse registerUser(UserRequest request);

    LoginResponse login(LoginRequest request);
}