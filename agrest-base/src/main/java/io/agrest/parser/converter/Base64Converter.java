package io.agrest.parser.converter;

import javax.ws.rs.core.Response.Status;

import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgException;

public class Base64Converter extends AbstractConverter<byte[]> {

	private static final Base64Converter instance = new Base64Converter();

	public static Base64Converter converter() {
		return instance;
	}

	@Override
	protected byte[] valueNonNull(JsonNode node) {

		if (!node.isTextual()) {
			throw new AgException(Status.BAD_REQUEST, "Expected textual value, got: " + node.asText());
		}
		try {
			return Base64.getDecoder().decode(node.asText());
		} catch (IllegalArgumentException e) {
			throw new AgException(Status.BAD_REQUEST, "Failed to decode Base64 value: " + node.asText(), e);
		}
	}
}
