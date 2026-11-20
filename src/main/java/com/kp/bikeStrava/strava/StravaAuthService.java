package com.kp.bikeStrava.strava;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.kp.bikeStrava.config.StravaProperties;

@Service
public class StravaAuthService {

	/** Odswiezamy token chwile przed wygasnieciem. */
	private static final long EXPIRY_MARGIN_SECONDS = 60;

	private final StravaProperties properties;
	private final StravaTokenStore tokenStore;
	private final Clock clock;
	private final RestClient restClient;

	public StravaAuthService(StravaProperties properties, StravaTokenStore tokenStore, Clock clock,
			RestClient.Builder restClientBuilder) {
		this.properties = properties;
		this.tokenStore = tokenStore;
		this.clock = clock;
		this.restClient = restClientBuilder.baseUrl(properties.oauthBaseUrl()).build();
	}

	/** Adres, na ktory przekierowujemy uzytkownika, zeby zezwolil aplikacji na dostep do Stravy. */
	public String buildAuthorizationUrl() {
		return UriComponentsBuilder.fromUriString(properties.oauthBaseUrl() + "/authorize")
				.queryParam("client_id", properties.clientId())
				.queryParam("redirect_uri", properties.redirectUri())
				.queryParam("response_type", "code")
				.queryParam("approval_prompt", "auto")
				.queryParam("scope", properties.scope())
				.build()
				.encode()
				.toUriString();
	}

	/** Wymienia jednorazowy kod z callbacku na access + refresh token i zapisuje je. */
	public void exchangeCode(String code) {
		MultiValueMap<String, String> form = baseForm();
		form.add("code", code);
		form.add("grant_type", "authorization_code");
		tokenStore.save(requestTokens(form));
	}

	/** Zwraca wazny access token, w razie potrzeby odswiezajac go refresh tokenem. */
	public synchronized String getValidAccessToken() {
		TokenResponse tokens = tokenStore.get();
		if (tokens == null || tokens.refreshToken() == null) {
			throw new StravaNotAuthorizedException("Brak tokenu Stravy - wykonaj autoryzacje.");
		}
		long now = clock.instant().getEpochSecond();
		if (tokens.accessToken() == null || tokens.expiresAt() - EXPIRY_MARGIN_SECONDS <= now) {
			MultiValueMap<String, String> form = baseForm();
			form.add("grant_type", "refresh_token");
			form.add("refresh_token", tokens.refreshToken());
			tokens = requestTokens(form);
			tokenStore.save(tokens);
		}
		return tokens.accessToken();
	}

	private MultiValueMap<String, String> baseForm() {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("client_id", properties.clientId());
		form.add("client_secret", properties.clientSecret());
		return form;
	}

	private TokenResponse requestTokens(MultiValueMap<String, String> form) {
		try {
			TokenResponse response = restClient.post()
					.uri("/token")
					.body(form)
					.retrieve()
					.body(TokenResponse.class);
			if (response == null || response.accessToken() == null) {
				throw new IllegalStateException("Strava zwrocila pusta odpowiedz tokenu");
			}
			return response;
		}
		catch (HttpClientErrorException e) {
			throw new StravaNotAuthorizedException(
					"Strava odrzucila zadanie tokenu (" + e.getStatusCode() + "). Wykonaj autoryzacje ponownie.");
		}
	}
}
