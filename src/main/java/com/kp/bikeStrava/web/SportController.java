package com.kp.bikeStrava.web;

import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kp.bikeStrava.strava.ActivityService;

@RestController
@RequestMapping("/api")
public class SportController {

	private final ActivityService activityService;

	public SportController(ActivityService activityService) {
		this.activityService = activityService;
	}

	/** Kazda aktywnosc w osobnej linii: name | distance w metrach | type. */
	@GetMapping(value = "/sport", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
	public String sport() {
		return activityService.getLatestActivitiesThisYear().stream()
				.map(a -> "%s | %d m | %s".formatted(a.name(), Math.round(a.distance()), a.sportType()))
				.collect(Collectors.joining("\n"));
	}
}
