package com.trading_data_managment_app.service;

import java.util.Random;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trading_data_managment_app.dto.ChangePasswordRequest;
import com.trading_data_managment_app.dto.UserLoginRequest;
import com.trading_data_managment_app.dto.UserMapper;
import com.trading_data_managment_app.dto.UserRegisterRequest;
import com.trading_data_managment_app.dto.VerifyUserRequest;
import com.trading_data_managment_app.entity.User;
import com.trading_data_managment_app.exception.InvalidCredentialsException;
import com.trading_data_managment_app.exception.ResourceNotFoundException;
import com.trading_data_managment_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	private String otp = "invalid";
	private final EmailService emailService;

	@Override
	@Transactional
	public String register(UserRegisterRequest userRegisterRequest) {

		log.info("Attempting to register user with email: {}", userRegisterRequest.getEmail());

		if (userRepository.findByEmail(userRegisterRequest.getEmail()).isPresent()) {
			log.warn("Registration failed - email {} already exists", userRegisterRequest.getEmail());
			throw new InvalidCredentialsException("Email already in use: " + userRegisterRequest.getEmail());
		}

		User user = userMapper.dtoToEntity(userRegisterRequest);
		log.debug("Mapped UserRegisterRequest to User entity: {}", user);
		user.setVerified(false);
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		User savedUser = userRepository.save(user);
		String verifyOtp = generateOtp(4);
		sendVerifyUserMail(savedUser.getEmail(), verifyOtp, savedUser.getName());
		log.info("User registered successfully with id: {}", savedUser.getId());

		return "User registered successfully with email: " + savedUser.getEmail()
				+ ". Please verify your account using the OTP sent to your email before logging in.";
	}

	@Override
	public String verifyUser(VerifyUserRequest verifyUserRequest) {

		log.info("Attempting to verify user with email: {}", verifyUserRequest.getEmail());

		User user = userRepository.findByEmail(verifyUserRequest.getEmail()).orElseThrow(
				() -> new ResourceNotFoundException("User not found with email: " + verifyUserRequest.getEmail()));

		if (user.getVerified()) {
			throw new RuntimeException("User is already verified.");
		}
		if (!this.otp.equals(verifyUserRequest.getOtp())) {
			throw new InvalidCredentialsException("Invalid verification code.");
		}
		user.setVerified(true);
		userRepository.save(user);
		this.otp = "invalid";
		log.info("User verified successfully.");

		return "User " + user.getEmail() + " has been verified successfully. You can now log in.";
	}

	@Override
	public String login(UserLoginRequest userLoginRequest) {

		log.info("Attempting login for email: {}", userLoginRequest.getEmail());

		User user = userRepository.findByEmail(userLoginRequest.getEmail()).orElseThrow(() -> {
			log.warn("Login failed - user not found with email: {}", userLoginRequest.getEmail());
			return new ResourceNotFoundException("User not found with email: " + userLoginRequest.getEmail());
		});
		if (!user.getVerified()) {
			throw new RuntimeException("User is not verified. Please verify your account before logging in.");
		}
		if (!passwordEncoder.matches(userLoginRequest.getPassword(), user.getPassword())) {
			throw new InvalidCredentialsException("Invalid password.");
		}

		log.info("Login successful for email: {}", userLoginRequest.getEmail());
		return "Login successful.";
	}

	@Override
	public String forgotPassword(String email) {
		log.info("Forgot password request received for email: {}", email);

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

		if (!user.getVerified()) {
			throw new RuntimeException("User is not verified. Please verify your account first.");
		}
		String otp = generateOtp(4);
		sendOtpMail(email, otp, user.getName());

		log.debug("Generated password reset OTP for {}: {}", email, otp);

		return "OTP has been sent to your email address.";
	}

	@Override
	public String resetPassword(String code, String email, String newPassword) {
		log.info("Reset password request received for email: {}", email);

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

		if (!user.getVerified()) {
			throw new RuntimeException("User is not verified. Please verify your account first.");
		}

		if (this.otp.equals(code)) {
			user.setPassword(passwordEncoder.encode(newPassword));
			userRepository.save(user);
			this.otp = "invalid";
			log.debug("Password updated successfully for email: {}", email);
		} else {
			throw new InvalidCredentialsException("Invalid OTP code.");
		}
		return "Password updated successfully.";
	}

	@Override
	public String changePassword(ChangePasswordRequest changePasswordRequest) {
		log.info("Change password request received for email: {}", changePasswordRequest.getEmail());

		User user = userRepository.findByEmail(changePasswordRequest.getEmail()).orElseThrow(
				() -> new ResourceNotFoundException("User not found with email: " + changePasswordRequest.getEmail()));

		if (!user.getVerified()) {
			throw new RuntimeException("User is not verified. Please verify your account first.");
		}
		if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
			throw new InvalidCredentialsException("Old password is incorrect.");
		}
		user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
		userRepository.save(user);
		log.debug("Password changed successfully for email: {}", changePasswordRequest.getEmail());

		return "Password changed successfully.";
	}

	@Override
	public String otpResend(String email) {
		log.info("OTP resend request received for email: {}", email);
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
		String verifyOtp = generateOtp(4);
		sendVerifyUserMail(user.getEmail(), verifyOtp, user.getName());
		sendOtpMail(email, otp, user.getName());
		log.debug("OTP resent to email: {}", email);
		return "OTP has been resent to your email address.";
	}

	private String generateOtp(int length) {
		StringBuilder otp = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			otp.append(random.nextInt(10));
		}
		this.otp = otp.toString();
		return otp.toString();
	}

	private void sendVerifyUserMail(String mail, String otp, String name) {
		String subject = "Account Verification - OTP Code";

		String body = "<html>"
				+ "<body style='font-family: Arial, sans-serif; background-color:#f9f9f9; padding:20px;'>"
				+ "<div style='max-width:600px; margin:auto; background:#fff; padding:20px; border-radius:10px; "
				+ "box-shadow:0 2px 8px rgba(0,0,0,0.1);'>" + "<h2 style='color:#333;'>Email Verification</h2>"
				+ "<p>Dear " + name + ",</p>" + "<p>Thank you for registering with us.</p>"
				+ "<p>Your One-Time Password (OTP) for account verification is: "
				+ "<b style='color:#2563eb; font-size:18px;'>" + otp + "</b></p>"
				+ "<p>Please enter this code in the app to verify your email address.</p>"
				+ "<p style='color:red;'><i>Do not share this OTP with anyone for security reasons.</i></p>"
				+ "<br><p>Regards,<br><b>Trading Data Management App Team</b></p>" + "</div></body></html>";

		emailService.sendEmail(mail, subject, body);
	}

	private void sendOtpMail(String mail, String otp, String name) {
		String subject = "Password Reset - OTP Code";

		String body = "<html>"
				+ "<body style='font-family: Arial, sans-serif; background-color:#f9f9f9; padding:20px;'>"
				+ "<div style='max-width:600px; margin:auto; background:#fff; padding:20px; border-radius:10px; "
				+ "box-shadow:0 2px 8px rgba(0,0,0,0.1);'>" + "<h2 style='color:#333;'>Password Reset Request</h2>"
				+ "<p>Dear " + name + ",</p>" + "<p>Your One-Time Password (OTP) for password reset is: "
				+ "<b style='color:#2563eb; font-size:18px;'>" + otp + "</b></p>"
				+ "<p>Please use this OTP to reset your account password.</p>"
				+ "<p style='color:red;'><i>Do not share this OTP with anyone for security reasons.</i></p>"
				+ "<br><p>Regards,<br><b>Trading Data Management App Team</b></p>" + "</div></body></html>";

		emailService.sendEmail(mail, subject, body);
	}
}
