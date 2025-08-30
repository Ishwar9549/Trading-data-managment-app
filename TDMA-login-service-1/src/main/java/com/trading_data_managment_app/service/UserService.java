package com.trading_data_managment_app.service;

import com.trading_data_managment_app.dto.UserDto;
import com.trading_data_managment_app.dto.UserLoginRequest;
import com.trading_data_managment_app.dto.UserRegisterRequest;

public interface UserService {
	UserDto login(UserLoginRequest userLoginRequest);

	UserDto register(UserRegisterRequest userRegisterRequest);

	String forgotPassword(String email);

	String resetPassword(String code, String email, String newPassword);
}