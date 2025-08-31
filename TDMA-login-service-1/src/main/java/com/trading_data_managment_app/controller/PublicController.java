package com.trading_data_managment_app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.trading_data_managment_app.dto.ForgotPasswordRequest;
import com.trading_data_managment_app.dto.ResetPasswordRequest;
import com.trading_data_managment_app.dto.UserDto;
import com.trading_data_managment_app.dto.UserLoginRequest;
import com.trading_data_managment_app.dto.UserRegisterRequest;
import com.trading_data_managment_app.service.UserService;

@Slf4j
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {

	private final UserService userService;

	@GetMapping("/test")
	public ResponseEntity<Map<String, String>> test() {
		log.info("Public Controller test endpoint is reached");
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "public test is reached"));
	}

	@PostMapping("/register")
	public ResponseEntity<UserDto> userRegister(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
		log.info("PublicController register endpoint called for email: {}", userRegisterRequest.getEmail());
		UserDto registeredUser = userService.register(userRegisterRequest);
		log.info("PublicController user registered successfully: {}");
		return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
	}

	@PostMapping("/login")
	public ResponseEntity<UserDto> userLogin(@Valid @RequestBody UserLoginRequest userLoginRequest) {
		log.info("PublicController Login API called for email: {}", userLoginRequest.getEmail());
		UserDto loggedInUser = userService.login(userLoginRequest);
		log.info("Login successful for email: {}", userLoginRequest.getEmail());
		return ResponseEntity.status(HttpStatus.OK).body(loggedInUser);
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
		log.info("Forgot password endpoint called for email: {}", forgotPasswordRequest.getEmail());
		String otp = userService.forgotPassword(forgotPasswordRequest.getEmail());
		return ResponseEntity.ok(otp);
	}

	@PostMapping("/reset-password")
	public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
		log.info("Reset password endpoint called for email: {}", resetPasswordRequest.getEmail());
		userService.resetPassword(resetPasswordRequest.getCode(), resetPasswordRequest.getEmail(),
				resetPasswordRequest.getNewPassword());
		return ResponseEntity.status(HttpStatus.OK).body("Password updated successfully");
	}
}