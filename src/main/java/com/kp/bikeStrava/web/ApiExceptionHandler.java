package com.kp.bikeStrava.web;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

import com.kp.bikeStrava.strava.StravaNotAuthorizedException;

@RestControllerAdvice
public class ApiExceptionHandler {

	/** Brak tokenu: przegladarka jest odsylana od razu do logowania w Stravie. */
	@ExceptionHandler(StravaNotAuthorizedException.class)
	public ResponseEntity<Void> notAuthorized() {
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/api/auth/login")).build();
	}

	@ExceptionHandler(RestClientResponseException.class)
	public ResponseEntity<Map<String, Object>> stravaError(RestClientResponseException e) {
		HttpStatus status = e.getStatusCode().value() == 429 ? HttpStatus.TOO_MANY_REQUESTS : HttpStatus.BAD_GATEWAY;
		return ResponseEntity.status(status).body(Map.of(
				"error", "Blad komunikacji ze Strava API",
				"stravaStatus", e.getStatusCode().value()));
	}
}
