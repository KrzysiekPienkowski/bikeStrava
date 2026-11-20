package com.kp.bikeStrava.web;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;

import com.kp.bikeStrava.strava.ActivityService;

@RestController
@RequestMapping("/api")
public class SportController {

	private static final List<Integer> YEARS = List.of(2025, 2026);

	private final ActivityService activityService;

	public SportController(ActivityService activityService) {
		this.activityService = activityService;
	}

	/**
	 * Strona z przyciskami lat. Po wybraniu roku pod przyciskami wyswietlane sa aktywnosci rowerowe
	 * z tego roku, kazda w osobnej linii: name | distance w metrach | type.
	 */
	@GetMapping(value = "/sport", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
	public String sport(@RequestParam(required = false) Integer year) {
		if (year != null && !YEARS.contains(year)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nieobslugiwany rok: " + year);
		}

		StringBuilder html = new StringBuilder("""
				<!DOCTYPE html>
				<html lang="pl"><head><meta charset="UTF-8"><title>Sport</title>
				<style>
				body{font-family:sans-serif;margin:2em}
				a.btn{display:inline-block;padding:.5em 1.5em;margin-right:.5em;border:1px solid #fc4c02;
				border-radius:4px;color:#fc4c02;text-decoration:none;font-weight:bold}
				a.btn.active{background:#fc4c02;color:#fff}
				</style></head><body>
				""");
		for (int y : YEARS) {
			html.append("<a class=\"btn%s\" href=\"/api/sport?year=%d\">%d</a>%n"
					.formatted(Integer.valueOf(y).equals(year) ? " active" : "", y, y));
		}

		if (year != null) {
			String lines = activityService.getBikeActivities(year).stream()
					.map(a -> "%s | %d m | %s".formatted(a.name(), Math.round(a.distance()), a.sportType()))
					.map(HtmlUtils::htmlEscape)
					.collect(Collectors.joining("\n"));
			html.append("<pre>").append(lines).append("</pre>");
		}
		return html.append("</body></html>").toString();
	}
}
