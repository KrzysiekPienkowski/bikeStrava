package com.kp.bikeStrava.strava;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class ActivityService {

	/** Typy aktywnosci Stravy traktowane jako "bike" (bez wirtualnych jazd). */
	static final Set<String> BIKE_TYPES = Set.of(
			"Ride", "MountainBikeRide", "GravelRide", "EBikeRide", "EMountainBikeRide");

	private static final int PAGE_SIZE = 200;
	/** Zabezpieczenie przed nieskonczona petla. */
	private static final int MAX_PAGES = 50;

	private final StravaClient stravaClient;
	private final Clock clock;

	public ActivityService(StravaClient stravaClient, Clock clock) {
		this.stravaClient = stravaClient;
		this.clock = clock;
	}

	/** Wszystkie aktywnosci rowerowe z podanego roku, od najnowszej. */
	public List<ActivityDto> getBikeActivities(int year) {
		long after = LocalDate.of(year, 1, 1).atStartOfDay(clock.getZone()).toEpochSecond();
		long before = LocalDate.of(year + 1, 1, 1).atStartOfDay(clock.getZone()).toEpochSecond();

		List<ActivityDto> result = new ArrayList<>();
		for (int page = 1; page <= MAX_PAGES; page++) {
			List<ActivityDto> batch = stravaClient.getActivities(after, before, page, PAGE_SIZE);
			batch.stream()
					.filter(a -> BIKE_TYPES.contains(a.sportType()))
					.filter(a -> a.startDateLocal().getYear() == year)
					.forEach(result::add);
			if (batch.size() < PAGE_SIZE) {
				break;
			}
		}
		result.sort(Comparator.comparing(ActivityDto::startDateLocal).reversed());
		return result;
	}
}
