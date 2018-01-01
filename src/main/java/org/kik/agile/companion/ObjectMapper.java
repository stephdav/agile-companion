package org.kik.agile.companion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class ObjectMapper<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ObjectMapper.class);

	private final Class<T> type;

	private Gson gson = new Gson();

	public ObjectMapper(final Class<T> type) {
		this.type = type;
	}

	public T parse(String json) {
		LOGGER.debug("parse {}", json);
		return gson.fromJson(json, type);
	}
}
