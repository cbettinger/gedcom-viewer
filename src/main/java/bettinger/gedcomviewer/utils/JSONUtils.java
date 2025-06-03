package bettinger.gedcomviewer.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface JSONUtils {

	public static String toJSON(final Object input) {
		final var writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
		try {
			return writer.writeValueAsString(input);
		} catch (final JsonProcessingException e) {
			Logger.getLogger(JSONUtils.class.getName()).log(Level.SEVERE, "Failed to serialize object to JSON string", e);
			return "{}";
		}
	}
}
