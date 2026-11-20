package com.kp.bikeStrava.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "strava")
public record StravaProperties(
		String clientId,
		String clientSecret,
		String redirectUri,
		String scope,
		String oauthBaseUrl,
		String apiBaseUrl,
		int activitiesLimit,
		String tokenFile) {
}
