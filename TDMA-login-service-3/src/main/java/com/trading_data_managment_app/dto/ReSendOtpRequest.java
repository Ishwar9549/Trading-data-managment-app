package com.trading_data_managment_app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReSendOtpRequest {
	@NotBlank(message = "Email cannot be blank")
	@Email(message = "Invalid email format")
	private String email;
	
	@NotBlank(message = "Otp for cannot be blank")
	private String otpFor;
}
