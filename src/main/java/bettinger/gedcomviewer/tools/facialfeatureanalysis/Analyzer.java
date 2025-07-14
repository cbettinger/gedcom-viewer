package bettinger.gedcomviewer.tools.facialfeatureanalysis;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;

import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.views.ResultFrame;
import bettinger.gedcomviewer.utils.FileUtils;
import bettinger.gedcomviewer.utils.JSONUtils;
import bettinger.gedcomviewer.utils.PythonUtils;
import bettinger.gedcomviewer.views.MainFrame;
import bettinger.gedcomviewer.views.MainFrame.BackgroundWorker;

public class Analyzer extends BackgroundWorker {
	private final Individual proband;
	private final int depth;
	private final int numberOfPortraits;

	private Map<FacialFeature, AnalysisResult> results;

	public Analyzer(final Individual proband, final int depth, final int numberOfPortraits) {
		MainFrame.getInstance().super(I18N.get("FacialFeatureAnalysis"));

		this.proband = proband;
		this.depth = depth;
		this.numberOfPortraits = numberOfPortraits;
	}

	@Override
	protected URI doInBackground() throws Exception {
		var uri = super.doInBackground();

		try {
			results = analyse();
		} catch (final AnalysisException e) {
			onError(new AnalysisException(String.format(Format.KEY_VALUE, I18N.get("FacialFeatureAnalysisFailed"), e.getMessage())));
		}

		return uri;
	}

	private Map<FacialFeature, AnalysisResult> analyse() throws AnalysisException {
		final TreeMap<FacialFeature, AnalysisResult> result = new TreeMap<>();

		final var inputFilePath = FileUtils.createTempFile();
		if (inputFilePath == null) {
			throw new AnalysisException("Failed to create temp file");
		}

		final var inputFile = JSONUtils.toJSONFile(IndividualDTO.build(proband, depth), inputFilePath.toString());
		if (inputFile == null) {
			throw new AnalysisException("Failed to write input file");
		}

		final String scriptPath = FileUtils.getPath(System.getProperty("user.dir"), "tools", "facialfeatureanalysis", "python", "familyFaceCompare.py");

		List<String> output = null;
		try {
			output = PythonUtils.executeScript(scriptPath, new String[] { inputFile.getAbsolutePath(), Integer.toString(numberOfPortraits), Integer.toString(depth) });
		} catch (final IOException e) {
			throw new AnalysisException(e.getMessage(), e);
		}

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
					result.put(feature, AnalysisResult.fromJSON(jsonResults, feature));
				}
			}
		}

		return result;
	}

	@Override
	protected void onSuccess(final URI uri) {
		super.onSuccess(uri);

		if (results != null) {
			new ResultFrame(proband, depth, results);
		}
	}

	static class AnalysisException extends Exception {
		AnalysisException(final String message) {
			super(message);
		}

		AnalysisException(final String message, final Throwable cause) {
			super(message, cause);
		}
	}
}
