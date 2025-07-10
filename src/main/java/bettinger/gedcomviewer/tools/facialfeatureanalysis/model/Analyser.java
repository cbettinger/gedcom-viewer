package bettinger.gedcomviewer.tools.facialfeatureanalysis.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;

import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.utils.FileUtils;
import bettinger.gedcomviewer.utils.JSONUtils;
import bettinger.gedcomviewer.utils.PythonUtils;

public interface Analyser {

	public static Map<FacialFeature, AnalysisResult> analyse(final Individual proband, final int depth, final int numberOfPortraits) throws AnalysisException {
		final TreeMap<FacialFeature, AnalysisResult> results = new TreeMap<>();

		final var inputFilePath = FileUtils.createTempFile();
		if (inputFilePath == null) {
			throw new AnalysisException("Failed to create temp file");
		}

		final var inputFile = JSONUtils.toJSONFile(new PersonInput(proband, 0, depth), inputFilePath.toString());
		if (inputFile == null) {
			throw new AnalysisException("Failed to write input file");
		}

		final String scriptPath = FileUtils.getPath(System.getProperty("user.dir"), "src", "main", "python", "familyFaceCompare.py");

		final List<String> output = PythonUtils.executeScript(scriptPath, new String[] { inputFile.getAbsolutePath(), Integer.toString(numberOfPortraits), Integer.toString(depth) }); // TODO: swap params in python script

		try {
			Files.delete(inputFilePath);
		} catch (final IOException _) {
			// intentionally left blank
		}

		if (output.isEmpty()) {
			throw new AnalysisException("Empty output");
		}

		final var jsonOutput = JSONUtils.fromString(output.getLast());
		if (jsonOutput.get("error") != null) {
			throw new AnalysisException(jsonOutput.get("message").asText());
		}

		if (jsonOutput.get("success") != null) {
			final var outputFilePath = Paths.get(jsonOutput.get("file").asText());

			JsonNode jsonResults = null;
			try {
				jsonResults = JSONUtils.fromString(Files.readString(outputFilePath, StandardCharsets.UTF_8));
				Files.delete(outputFilePath);
			} catch (final IOException e) {
				throw new AnalysisException("Failed to process the results of the facial feature analysis", e);
			}

			if (jsonResults != null) {
				for (final var feature : FacialFeature.values()) {
					results.put(feature, AnalysisResult.fromJSON(jsonResults, feature.name()));
				}
			}
		}

		return results;
	}
}
