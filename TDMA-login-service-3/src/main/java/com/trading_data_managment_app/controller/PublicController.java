package com.trading_data_managment_app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.trading_data_managment_app.dto.ChangePasswordRequest;
import com.trading_data_managment_app.dto.ForgotPasswordRequest;
import com.trading_data_managment_app.dto.ReSendOtpRequest;
import com.trading_data_managment_app.dto.ResetPasswordRequest;
import com.trading_data_managment_app.dto.UserLoginRequest;
import com.trading_data_managment_app.dto.UserRegisterRequest;
import com.trading_data_managment_app.dto.VerifyUserRequest;
import com.trading_data_managment_app.service.UserService;

@Slf4j
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {

	private final UserService userService;

	@GetMapping("/test")
	public ResponseEntity<Map<String, String>> test() {
		log.info("PublicController: Test endpoint reached");
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "public test is reached"));
	}

	@PostMapping("/register")
	public ResponseEntity<Map<String, String>> userRegister(
			@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
		log.info("PublicController: Register endpoint called for email: {}", userRegisterRequest.getEmail());
		String result = userService.register(userRegisterRequest);
		log.info("PublicController: User registered successfully for email: {}", userRegisterRequest.getEmail());
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("status", result));
	}

	@PostMapping("/verify-user")
	public ResponseEntity<Map<String, String>> verifyUser(@Valid @RequestBody VerifyUserRequest verifyUserRequest) {
		log.info("PublicController: Verify-user endpoint called for email: {}", verifyUserRequest.getEmail());
		String result = userService.verifyUser(verifyUserRequest);
		log.info("PublicController: User verified successfully for email: {}", verifyUserRequest.getEmail());
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", result));
	}

	@PostMapping("/login")
	public ResponseEntity<Map<String, String>> userLogin(@Valid @RequestBody UserLoginRequest userLoginRequest) {
		log.info("PublicController: Login endpoint called for email: {}", userLoginRequest.getEmail());
		String token = userService.login(userLoginRequest);
		log.info("PublicController: Login successful for email: {}", userLoginRequest.getEmail());
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("Token", token));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<Map<String, String>> forgotPassword(
			@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
		log.info("PublicController: Forgot-password endpoint called for email: {}", forgotPasswordRequest.getEmail());
		String result = userService.forgotPassword(forgotPasswordRequest);
		log.info("PublicController: OTP for password reset sent successfully to email: {}",
				forgotPasswordRequest.getEmail());
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", result));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<Map<String, String>> resetPassword(
			@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
		log.info("PublicController: Reset-password endpoint called for email: {}", resetPasswordRequest.getEmail());
		String result = userService.resetPassword(resetPasswordRequest);
		log.info("PublicController: Password reset successfully for email: {}", resetPasswordRequest.getEmail());
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("status", result));
	}

	@PostMapping("/change-password")
	public ResponseEntity<Map<String, String>> changePassword(
			@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
		log.info("PublicController: Change-password endpoint called for email: {}", changePasswordRequest.getEmail());
		String result = userService.changePassword(changePasswordRequest);
		log.info("PublicController: Password changed successfully for email: {}", changePasswordRequest.getEmail());
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("status", result));
	}

	@PostMapping("/re-send-otp")
	public ResponseEntity<String> reSendOtp(@Valid @RequestBody ReSendOtpRequest reSendOtpRequest) {
		log.info("PublicController: Resend-OTP endpoint called for email: {}", reSendOtpRequest.getEmail());
		String result = userService.otpResend(reSendOtpRequest);
		log.info("PublicController: OTP resent successfully to email: {}", reSendOtpRequest.getEmail());
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
}
