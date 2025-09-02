package com.trading_data_managment_app.service;

import com.trading_data_managment_app.dto.ChangePasswordRequest;
import com.trading_data_managment_app.dto.ForgotPasswordRequest;
import com.trading_data_managment_app.dto.ReSendOtpRequest;
import com.trading_data_managment_app.dto.ResetPasswordRequest;
import com.trading_data_managment_app.dto.UserLoginRequest;
import com.trading_data_managment_app.dto.UserRegisterRequest;
import com.trading_data_managment_app.dto.VerifyUserRequest;

public interface UserService {
	
	String login(UserLoginRequest userLoginRequest);

	String register(UserRegisterRequest userRegisterRequest);

	String forgotPassword(ForgotPasswordRequest forgotPasswordRequest);

	String resetPassword(ResetPasswordRequest resetPasswordRequest);

	String verifyUser(VerifyUserRequest verifyUserRequest);
	
	String changePassword(ChangePasswordRequest changePasswordRequest);
	
    String otpResend(ReSendOtpRequest reSendOtpRequest);
}