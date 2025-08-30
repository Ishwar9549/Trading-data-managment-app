package com.trading_data_managment_app.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/private")
public class PrivateController {

	@GetMapping("/test")
	public ResponseEntity<Map<String, String>> test() {
		log.info("Private test endpoint is reached");
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "Private test is reached"));
	}
}