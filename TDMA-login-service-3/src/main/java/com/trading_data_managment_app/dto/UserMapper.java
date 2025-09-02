package com.trading_data_managment_app.dto;

import org.springframework.stereotype.Component;
import com.trading_data_managment_app.entity.User;

@Component
public class UserMapper {

	public UserDto entityToDto(User user) {
		if (user == null) {
			return null;
		}
		return UserDto.builder().name(user.getName()).email(user.getEmail()).id(user.getId())
				.password(user.getPassword()).build();
	}

	public User dtoToEntity(UserRegisterRequest request) {
		if (request == null) {
			return null;
		}
		return User.builder().name(request.getName()).email(request.getEmail()).password(request.getPassword()).build();
	}
}