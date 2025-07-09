package bettinger.gedcomviewer.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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

	public static File toJSONFile(final Object input, final String filePath) {
		final var writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
		final var file = new File(filePath);

		try {
			writer.writeValue(file, input);
		} catch (final IOException e) {
			Logger.getLogger(JSONUtils.class.getName()).log(Level.SEVERE, "Failed to serialize object to JSON file", e);
		}
		return file;
	}

	public static JsonNode fromString(final String input) {
		final var reader = new ObjectMapper();

		try {
			return reader.readTree(input);
		} catch (final JsonProcessingException e) {
			Logger.getLogger(JSONUtils.class.getName()).log(Level.SEVERE, "Failed to deserialize JSON string to JSON node", e);
			return reader.createObjectNode();
		}
	}
}
