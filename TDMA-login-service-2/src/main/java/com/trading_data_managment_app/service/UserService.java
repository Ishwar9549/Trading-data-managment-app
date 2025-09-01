package com.trading_data_managment_app.service;

import com.trading_data_managment_app.dto.ChangePasswordRequest;
import com.trading_data_managment_app.dto.UserDto;
import com.trading_data_managment_app.dto.UserLoginRequest;
import com.trading_data_managment_app.dto.UserRegisterRequest;
import com.trading_data_managment_app.dto.VerifyUserRequest;

public interface UserService {
	
	String login(UserLoginRequest userLoginRequest);

	String register(UserRegisterRequest userRegisterRequest);

	String forgotPassword(String email);

	String resetPassword(String code, String email, String newPassword);

	String verifyUser(VerifyUserRequest verifyUserRequest);
	
	String changePassword(ChangePasswordRequest changePasswordRequest);
	
    String otpResend(String email);
}