package com.kp.bikeStrava.strava;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.kp.bikeStrava.config.StravaProperties;

class ActivityServiceTest {

	private final Clock clock = Clock.fixed(Instant.parse("2026-11-20T12:00:00Z"), ZoneOffset.UTC);
	private final StravaProperties props = new StravaProperties("1", "s", "r", "activity:read", "o", "a", 10, "f");
	private final StravaClient client = mock(StravaClient.class);
	private final ActivityService service = new ActivityService(client, props, clock);

	private static ActivityDto activity(long id, String date) {
		return new ActivityDto(id, "a" + id, "Ride", OffsetDateTime.parse(date), 1000, 600, 10);
	}

	@Test
	void returnsOnlyTenNewestFromCurrentYear() {
		List<ActivityDto> page = IntStream.rangeClosed(1, 30)
				.mapToObj(i -> activity(i, "2026-10-01T10:00:00Z")).toList();
		when(client.getActivities(eq(1), anyInt())).thenReturn(page);

		List<ActivityDto> result = service.getLatestActivitiesThisYear();

		assertThat(result).hasSize(10);
		assertThat(result.get(0).id()).isEqualTo(1);
	}

	@Test
	void stopsAtActivitiesFromPreviousYear() {
		when(client.getActivities(eq(1), anyInt())).thenReturn(List.of(
				activity(1, "2026-03-01T10:00:00Z"),
				activity(2, "2026-01-02T10:00:00Z"),
				activity(3, "2025-12-31T10:00:00Z"),
				activity(4, "2025-12-30T10:00:00Z")));

		List<ActivityDto> result = service.getLatestActivitiesThisYear();

		assertThat(result).extracting(ActivityDto::id).containsExactly(1L, 2L);
	}

	@Test
	void fetchesNextPageWhenNeeded() {
		when(client.getActivities(eq(1), anyInt())).thenReturn(IntStream.rangeClosed(1, 6)
				.mapToObj(i -> activity(i, "2026-05-01T10:00:00Z")).toList());
		when(client.getActivities(eq(2), anyInt())).thenReturn(IntStream.rangeClosed(7, 20)
				.mapToObj(i -> activity(i, "2026-04-01T10:00:00Z")).toList());

		assertThat(service.getLatestActivitiesThisYear()).hasSize(10);
	}
}
