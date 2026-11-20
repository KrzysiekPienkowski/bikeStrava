package com.kp.bikeStrava.web;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kp.bikeStrava.strava.StravaAuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final StravaAuthService authService;

	public AuthController(StravaAuthService authService) {
		this.authService = authService;
	}

	/** Krok 1: przekierowanie do Stravy z prosba o zgode. */
	@GetMapping("/login")
	public ResponseEntity<Void> login() {
		return ResponseEntity.status(HttpStatus.FOUND)
				.location(URI.create(authService.buildAuthorizationUrl()))
				.build();
	}

	/** Krok 2: Strava wraca tutaj z parametrem code (albo error, gdy odmowiono dostepu). */
	@GetMapping("/callback")
	public ResponseEntity<String> callback(@RequestParam(required = false) String code,
			@RequestParam(required = false) String error) {
		if (error != null || code == null) {
			return ResponseEntity.badRequest().body("Autoryzacja nieudana: " + (error != null ? error : "brak kodu"));
		}
		authService.exchangeCode(code);
		return ResponseEntity.ok("Autoryzacja zakonczona. Otworz /api/sport");
	}
}
