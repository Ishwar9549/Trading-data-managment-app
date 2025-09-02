package com.trading_data_managment_app.service;

import java.time.LocalDateTime;
import java.util.Random;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.trading_data_managment_app.dto.ChangePasswordRequest;
import com.trading_data_managment_app.dto.ForgotPasswordRequest;
import com.trading_data_managment_app.dto.ReSendOtpRequest;
import com.trading_data_managment_app.dto.ResetPasswordRequest;
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

		String verifyOtp = generateOtp(4);
		user.setOtp(verifyOtp);
		user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

		User savedUser = userRepository.save(user);

		sendVerifyUserMail(savedUser.getEmail(), verifyOtp, savedUser.getName());

		log.info("User registered successfully with id: {}", savedUser.getId());

		return "User registered successfully with email: " + savedUser.getEmail()
				+ ". Please verify your account using the OTP sent to your email (valid for 5 minutes).";
	}

	@Override
	public String verifyUser(VerifyUserRequest verifyUserRequest) {

		log.info("Attempting to verify user with email: {}", verifyUserRequest.getEmail());

		User user = userRepository.findByEmail(verifyUserRequest.getEmail()).orElseThrow(
				() -> new ResourceNotFoundException("User not found with email: " + verifyUserRequest.getEmail()));

		if (user.getVerified()) {
			throw new RuntimeException("User is already verified.");
		}
		if (!user.getOtp().equals(verifyUserRequest.getOtp())) {
			throw new InvalidCredentialsException("Invalid verification code.");
		}
		if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
			throw new InvalidCredentialsException("OTP has expired. Please resend OTP and try again.");
		}

		user.setVerified(true);
		user.setOtp("null");
		userRepository.save(user);

		log.info("User verified successfully for email: {}", user.getEmail());

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
	public String forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
		log.info("Forgot password request received for email: {}", forgotPasswordRequest.getEmail());

		User user = userRepository.findByEmail(forgotPasswordRequest.getEmail()).orElseThrow(
				() -> new ResourceNotFoundException("User not found with email: " + forgotPasswordRequest.getEmail()));

		if (!user.getVerified()) {
			throw new RuntimeException("User is not verified. Please verify your account first.");
		}

		String otp = generateOtp(4);
		user.setOtp(otp);
		user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
		userRepository.save(user);

		sendOtpMail(forgotPasswordRequest.getEmail(), otp, user.getName());

		log.debug("Generated password reset OTP for {}: {}", forgotPasswordRequest.getEmail(), otp);

		return "OTP has been sent to your email address (valid for 5 minutes).";
	}

	@Override
	public String resetPassword(ResetPasswordRequest resetPasswordRequest) {
		log.info("Reset password request received for email: {}", resetPasswordRequest.getEmail());

		User user = userRepository.findByEmail(resetPasswordRequest.getEmail()).orElseThrow(
				() -> new ResourceNotFoundException("User not found with email: " + resetPasswordRequest.getEmail()));

		if (!user.getVerified()) {
			throw new RuntimeException("User is not verified. Please verify your account first.");
		}
		if (!user.getOtp().equals(resetPasswordRequest.getOtp())) {
			throw new InvalidCredentialsException("Invalid verification code.");
		}
		if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
			throw new InvalidCredentialsException("OTP has expired. Please resend OTP and try again.");
		}

		user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
		user.setOtp("null");
		userRepository.save(user);

		log.debug("Password updated successfully for email: {}", resetPasswordRequest.getEmail());

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
	public String otpResend(ReSendOtpRequest reSendOtpRequest) {
		log.info("OTP resend request received for email: {}", reSendOtpRequest.getEmail());

		User user = userRepository.findByEmail(reSendOtpRequest.getEmail()).orElseThrow(
				() -> new ResourceNotFoundException("User not found with email: " + reSendOtpRequest.getEmail()));

		String verifyOtp = generateOtp(4);
		user.setOtp(verifyOtp);
		user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
		userRepository.save(user);

		if (reSendOtpRequest.getOtpFor().equalsIgnoreCase("Account-Verification")) {
			if (user.getVerified()) {
				throw new RuntimeException("User Account is already verified.");
			}
			sendVerifyUserMail(user.getEmail(), verifyOtp, user.getName());
		} else if (reSendOtpRequest.getOtpFor().equalsIgnoreCase("Password-Reset")) {
			sendOtpMail(user.getEmail(), verifyOtp, user.getName());
		} else {
			throw new RuntimeException("otp for value should be - Account-Verification or Password-Reset");
		}

		log.debug("OTP resent to email: {}", reSendOtpRequest.getEmail());
		return "OTP has been resent to your email address (valid for 5 minutes).";
	}

	private String generateOtp(int length) {
		StringBuilder otp = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			otp.append(random.nextInt(10));
		}
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
				+ "<p>This OTP is valid for <b>5 minutes</b> and will expire afterwards.</p>"
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
				+ "<p>This OTP is valid for <b>5 minutes</b> and will expire afterwards.</p>"
				+ "<p>Please use this OTP to reset your account password.</p>"
				+ "<p style='color:red;'><i>Do not share this OTP with anyone for security reasons.</i></p>"
				+ "<br><p>Regards,<br><b>Trading Data Management App Team</b></p>" + "</div></body></html>";

		emailService.sendEmail(mail, subject, body);
	}
}
