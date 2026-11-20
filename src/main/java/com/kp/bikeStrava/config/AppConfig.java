package com.kp.bikeStrava.config;

import java.time.Clock;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StravaProperties.class)
public class AppConfig {

	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}
}
