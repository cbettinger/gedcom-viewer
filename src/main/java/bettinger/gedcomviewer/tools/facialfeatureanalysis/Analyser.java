package bettinger.gedcomviewer.tools.facialfeatureanalysis;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;

import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.views.ResultsFrame;
import bettinger.gedcomviewer.utils.FileUtils;
import bettinger.gedcomviewer.utils.JSONUtils;
import bettinger.gedcomviewer.utils.PythonUtils;
import bettinger.gedcomviewer.views.MainFrame;
import bettinger.gedcomviewer.views.MainFrame.BackgroundWorker;

public class Analyser extends BackgroundWorker {
	private final Individual proband;
	private final int depth;
	private final int numberOfPortraits;

	public Analyser(final Individual proband, final int depth, final int numberOfPortraits) {
		MainFrame.getInstance().super(I18N.get("FacialFeatureAnalysis"));

		this.proband = proband;
		this.depth = depth;
		this.numberOfPortraits = numberOfPortraits;
	}

	@Override
	protected URI doInBackground() throws Exception {
		var uri = super.doInBackground();

		try {
			final var results = analyse();
			if (!results.isEmpty()) {
				new ResultsFrame(proband, depth, results);
			}
		} catch (final AnalysisException e) {
			onError(new AnalysisException(String.format(Format.KEY_VALUE_PARAGRAPH, I18N.get("FacialFeatureAnalysisFailed"), e.getMessage())));
		}

		return uri;
	}

	private Map<FacialFeature, AnalysisResult> analyse() throws AnalysisException {
		final TreeMap<FacialFeature, AnalysisResult> result = new TreeMap<>();

		final var inputFilePath = FileUtils.createTempFile();
		if (inputFilePath == null) {
			throw new AnalysisException("Failed to create temp file");
		}

		final var inputFile = JSONUtils.toJSONFile(JSONSerializer.build(proband, depth), inputFilePath.toString());
		if (inputFile == null) {
			throw new AnalysisException("Failed to write input file");
		}

		final String scriptPath = FileUtils.getPath(getClass().getClassLoader().getResource("tools/facialfeatureanalysis/main.py"));

		String output = null;
		try {
			output = PythonUtils.executeScript(scriptPath, inputFile.getAbsolutePath(), Integer.toString(numberOfPortraits), Integer.toString(depth));
		} catch (final IOException e) {
			throw new AnalysisException(e.getMessage(), e);
		}

		if (output.isEmpty()) {
			throw new AnalysisException("Empty output");
		}

		final var jsonOutput = JSONUtils.fromString(output);
		if (jsonOutput.get("error") != null) {
			throw new AnalysisException(jsonOutput.get("message").asText());
		}

		if (jsonOutput.get("success") != null) {
			final var outputFilePath = Paths.get(jsonOutput.get("filepath").asText());

			JsonNode jsonResults = null;
			try {
				jsonResults = JSONUtils.fromString(Files.readString(outputFilePath, StandardCharsets.UTF_8));
				Files.delete(outputFilePath);
			} catch (final IOException e) {
				throw new AnalysisException("Failed to process the results of the facial feature analysis", e);
			}

			if (jsonResults != null) {
				for (final var feature : FacialFeature.values()) {
					result.put(feature, AnalysisResult.fromJSON(jsonResults, feature));
				}
			}
		}

		return result;
	}

	private static class AnalysisException extends Exception {
		AnalysisException(final String message) {
			super(message);
		}

		AnalysisException(final String message, final Throwable cause) {
			super(message, cause);
		}
	}
}
