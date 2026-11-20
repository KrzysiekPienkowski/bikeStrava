package com.kp.bikeStrava.strava;

import java.time.Clock;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kp.bikeStrava.config.StravaProperties;

@Service
public class ActivityService {

	private static final int PAGE_SIZE = 30;
	/** Zabezpieczenie przed nieskonczona petla. */
	private static final int MAX_PAGES = 20;

	private final StravaClient stravaClient;
	private final StravaProperties properties;
	private final Clock clock;

	public ActivityService(StravaClient stravaClient, StravaProperties properties, Clock clock) {
		this.stravaClient = stravaClient;
		this.properties = properties;
		this.clock = clock;
	}

	/**
	 * Zwraca najnowsze aktywnosci z biezacego roku (max {@code strava.activities-limit}).
	 * Strava zwraca dane od najnowszej, wiec przerywamy po trafieniu na aktywnosc z poprzedniego roku.
	 */
	public List<ActivityDto> getLatestActivitiesThisYear() {
		int year = Year.now(clock).getValue();
		int limit = properties.activitiesLimit();
		List<ActivityDto> result = new ArrayList<>();

		for (int page = 1; page <= MAX_PAGES && result.size() < limit; page++) {
			List<ActivityDto> batch = stravaClient.getActivities(page, PAGE_SIZE);
			if (batch.isEmpty()) {
				break;
			}
			for (ActivityDto activity : batch) {
				if (activity.startDateLocal().getYear() < year) {
					return result;
				}
				if (activity.startDateLocal().getYear() == year) {
					result.add(activity);
					if (result.size() == limit) {
						return result;
					}
				}
			}
		}
		return result;
	}
}
