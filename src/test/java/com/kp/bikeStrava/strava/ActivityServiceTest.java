package com.kp.bikeStrava.strava;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
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

class ActivityServiceTest {

	private final Clock clock = Clock.fixed(Instant.parse("2026-11-20T12:00:00Z"), ZoneOffset.UTC);
	private final StravaClient client = mock(StravaClient.class);
	private final ActivityService service = new ActivityService(client, clock);

	private static ActivityDto activity(long id, String type, String date) {
		return new ActivityDto(id, "a" + id, type, OffsetDateTime.parse(date), 1000, 600, 10);
	}

	@Test
	void keepsOnlyBikeActivitiesSortedNewestFirst() {
		when(client.getActivities(anyLong(), anyLong(), eq(1), anyInt())).thenReturn(List.of(
				activity(1, "Ride", "2025-03-01T10:00:00Z"),
				activity(2, "Run", "2025-04-01T10:00:00Z"),
				activity(3, "GravelRide", "2025-06-01T10:00:00Z"),
				activity(4, "VirtualRide", "2025-07-01T10:00:00Z")));

		List<ActivityDto> result = service.getBikeActivities(2025);

		assertThat(result).extracting(ActivityDto::id).containsExactly(3L, 1L);
	}

	@Test
	void requestsWholeYearRange() {
		service.getBikeActivities(2025);

		long after = Instant.parse("2025-01-01T00:00:00Z").getEpochSecond();
		long before = Instant.parse("2026-01-01T00:00:00Z").getEpochSecond();
		org.mockito.Mockito.verify(client).getActivities(eq(after), eq(before), eq(1), anyInt());
	}

	@Test
	void fetchesNextPageWhenPageIsFull() {
		when(client.getActivities(anyLong(), anyLong(), eq(1), anyInt())).thenReturn(IntStream.rangeClosed(1, 200)
				.mapToObj(i -> activity(i, "Ride", "2026-05-01T10:00:00Z")).toList());
		when(client.getActivities(anyLong(), anyLong(), eq(2), anyInt())).thenReturn(List.of(
				activity(201, "Ride", "2026-04-01T10:00:00Z")));

		assertThat(service.getBikeActivities(2026)).hasSize(201);
	}
}
