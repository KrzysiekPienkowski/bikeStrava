package com.kp.bikeStrava.strava;

/** Brak (lub niewazny) token - trzeba przejsc autoryzacje pod /api/auth/login. */
public class StravaNotAuthorizedException extends RuntimeException {

	public StravaNotAuthorizedException(String message) {
		super(message);
	}
}
