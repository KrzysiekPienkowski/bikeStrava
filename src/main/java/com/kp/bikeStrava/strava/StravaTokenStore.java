package com.kp.bikeStrava.strava;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kp.bikeStrava.config.StravaProperties;

/** Przechowuje tokeny w pamieci i w pliku JSON, zeby przetrwaly restart aplikacji. */
@Component
public class StravaTokenStore {

	private final ObjectMapper objectMapper;
	private final Path file;
	private TokenResponse current;

	public StravaTokenStore(StravaProperties properties, ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.file = Path.of(properties.tokenFile());
		this.current = load();
	}

	public synchronized TokenResponse get() {
		return current;
	}

	public synchronized void save(TokenResponse tokens) {
		this.current = tokens;
		try {
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), tokens);
		}
		catch (IOException e) {
			throw new UncheckedIOException("Nie mozna zapisac tokenow do " + file.toAbsolutePath(), e);
		}
	}

	private TokenResponse load() {
		if (!Files.exists(file)) {
			return null;
		}
		try {
			return objectMapper.readValue(file.toFile(), TokenResponse.class);
		}
		catch (IOException e) {
			throw new UncheckedIOException("Nie mozna odczytac tokenow z " + file.toAbsolutePath(), e);
		}
	}
}
