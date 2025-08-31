package com.trading_data_managment_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class TradingDataManagmentAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradingDataManagmentAppApplication.class, args);
		log.info("Trading DataManagment AppApplication started....");
		System.err.println("Trading DataManagment AppApplication started....31-8-2025");
	}
}  