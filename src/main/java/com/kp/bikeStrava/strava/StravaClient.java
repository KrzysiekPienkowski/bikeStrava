package com.kp.bikeStrava.strava;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.kp.bikeStrava.config.StravaProperties;

@Component
public class StravaClient {

	private final StravaAuthService authService;
	private final RestClient restClient;

	public StravaClient(StravaProperties properties, StravaAuthService authService,
			RestClient.Builder restClientBuilder) {
		this.authService = authService;
		this.restClient = restClientBuilder.baseUrl(properties.apiBaseUrl()).build();
	}

	/**
	 * Pobiera strone aktywnosci zalogowanego sportowca z podanego zakresu czasu
	 * (epoch seconds, {@code after} wlacznie, {@code before} wylacznie).
	 */
	public List<ActivityDto> getActivities(long after, long before, int page, int perPage) {
		try {
			List<ActivityDto> result = restClient.get()
					.uri(uri -> uri.path("/athlete/activities")
							.queryParam("after", after)
							.queryParam("before", before)
							.queryParam("page", page)
							.queryParam("per_page", perPage)
							.build())
					.headers(h -> h.setBearerAuth(authService.getValidAccessToken()))
					.retrieve()
					.body(new ParameterizedTypeReference<List<ActivityDto>>() {
					});
			return result == null ? List.of() : result;
		}
		catch (HttpClientErrorException.Unauthorized e) {
			throw new StravaNotAuthorizedException("Strava odrzucila token (401). Wykonaj autoryzacje ponownie.");
		}
	}
}
