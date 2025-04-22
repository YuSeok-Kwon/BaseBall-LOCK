package com.kepg.BaseBallLOCK;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BaseBallLockApplication {

	public static void main(String[] args) {
		SpringApplication.run(BaseBallLockApplication.class, args);
	}

}
