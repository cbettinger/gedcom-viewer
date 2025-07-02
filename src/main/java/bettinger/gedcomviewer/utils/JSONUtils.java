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

	public static JsonNode fromString(final String jsonString) {
		final var mapper = new ObjectMapper();
		try {
			return mapper.readTree(jsonString);
		} catch (JsonProcessingException e) {
			Logger.getLogger(JSONUtils.class.getName()).log(Level.SEVERE, "Failed to deserialize JSON string to JSON object", e);
			return mapper.createObjectNode();
		}
	}

	public static File toJSONFile(final Object input, final String filePath) {
		final var writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
		File file = new File(filePath);
		try {
			writer.writeValue(file, input);
		} catch (IOException e) {
			Logger.getLogger(JSONUtils.class.getName()).log(Level.SEVERE, "Failed to create JSON file", e);
		}
		return file;
	}
}
