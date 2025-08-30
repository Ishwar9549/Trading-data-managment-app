package com.trading_data_managment_app.service;

import java.util.Random;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.trading_data_managment_app.dto.UserDto;
import com.trading_data_managment_app.dto.UserLoginRequest;
import com.trading_data_managment_app.dto.UserMapper;
import com.trading_data_managment_app.dto.UserRegisterRequest;
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
	private String code = "Fail";
	private final EmailService emailService;

	@Override
	@Transactional
	public UserDto register(UserRegisterRequest userRegisterRequest) {
		log.info("Attempting to register user with email: {}", userRegisterRequest.getEmail());

		if (userRepository.findByEmail(userRegisterRequest.getEmail()).isPresent()) {
			log.warn("Registration failed - email {} already exists", userRegisterRequest.getEmail());
			throw new InvalidCredentialsException("Email already in use: " + userRegisterRequest.getEmail());
		}

		User user = userMapper.dtoToEntity(userRegisterRequest);
		log.debug("Mapped UserRegisterRequest to User entity: {}", user);

		user.setPassword(passwordEncoder.encode(user.getPassword()));
		User savedUser = userRepository.save(user);
		log.info("User registered successfully with id: {}", savedUser.getId());

		return userMapper.entityToDto(savedUser);
	}

	@Override
	public UserDto login(UserLoginRequest userLoginRequest) {

		log.info("Attempting login for email: {}", userLoginRequest.getEmail());

		User user = userRepository.findByEmail(userLoginRequest.getEmail()).orElseThrow(() -> {
			log.warn("Login failed - user not found with email: {}", userLoginRequest.getEmail());
			return new ResourceNotFoundException("User not found with email: " + userLoginRequest.getEmail());
		});

		if (!passwordEncoder.matches(userLoginRequest.getPassword(), user.getPassword())) {
			throw new InvalidCredentialsException("Invalid password");
		}

		log.info("Login successful for email: {}", userLoginRequest.getEmail());
		return userMapper.entityToDto(user);
	}

	@Override
	public String forgotPassword(String email) {
		log.info("Forgot password service method executed for email: {}", email);

		userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

		String otp = generateRandomCode(4);
		String toMail = email;
		String subject = "OTP- for password reset";
		String body = "<html>" + "<body style='font-family: Arial, sans-serif;'>" + "<h2>Password Reset Request</h2>"
				+ "<p>Dear User,</p>" + "<p>Your One-Time Password (OTP) is: <b style='color:blue;'>" + otp + "</b></p>"
				+ "<p>Please use this OTP to reset your password.</p>"
				+ "<p><i>Do not share this OTP with anyone.</i></p>" + "<br>"
				+ "<p>Regards,<br>Trading Data Management App Team</p>" + "</body>" + "</html>";
		emailService.sendEmail(toMail, subject, body);
		log.debug("Generated password reset code for {}: {}", email, otp);

		return "OTP is sent to your mail id";
	}

	@Override
	public String resetPassword(String code, String email, String newPassword) {
		log.info("Reset password service method executed for email: {}", email);

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

		if (this.code.equals(code)) {
			user.setPassword(passwordEncoder.encode(newPassword));
			userRepository.save(user);
			this.code = "Fail";
			log.debug("Password updated successfully for email: {}", email);
		} else {
			throw new InvalidCredentialsException("Code is not correct...");
		}
		return "Password updated successfully";
	}

	private String generateRandomCode(int length) {
		StringBuilder code = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			code.append(random.nextInt(10));
		}
		this.code = code.toString();
		return code.toString();
	}
}