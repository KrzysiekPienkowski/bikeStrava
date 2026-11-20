package com.kp.bikeStrava.strava;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Uproszczona aktywnosc ze Stravy (GET /athlete/activities). Dystans w metrach, czasy w sekundach. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ActivityDto(
		long id,
		String name,
		@JsonProperty("sport_type") String sportType,
		@JsonProperty("start_date_local") OffsetDateTime startDateLocal,
		double distance,
		@JsonProperty("moving_time") int movingTime,
		@JsonProperty("total_elevation_gain") double totalElevationGain) {
}
