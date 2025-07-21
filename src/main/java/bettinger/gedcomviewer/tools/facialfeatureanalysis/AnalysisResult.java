package bettinger.gedcomviewer.tools.facialfeatureanalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.javatuples.Pair;

import com.fasterxml.jackson.databind.JsonNode;

public class AnalysisResult {
	private static final float EPSILON = 0.000001f;

	private final Map<String, Similarity> similarities;
	private final Map<AncestralLine, Float> lineSimilarities;

	private AnalysisResult(final Map<String, Similarity> similarities, final Map<AncestralLine, Float> lineSimilarities) {
		this.similarities = similarities;
		this.lineSimilarities = lineSimilarities;
	}

	public Map<String, Similarity> getSimilarities() {
		return similarities;
	}

	public Map<AncestralLine, Float> getLineSimilarities() {
		return lineSimilarities;
	}

	public Pair<ArrayList<String>, Float> getMaxSimilarity() {
		final ArrayList<String> idsWithMaxSimilarity = new ArrayList<>();
		Float maxSimilarity = null;

		for (final var entry : similarities.entrySet()) {
			final var id = entry.getKey();
			final var similarity = entry.getValue();

			if (similarity != null) {
				final var avgSimilarity = similarity.getAvgSimilarity();

				if (maxSimilarity == null || avgSimilarity > maxSimilarity) {
					idsWithMaxSimilarity.clear();
					idsWithMaxSimilarity.add(id);
					maxSimilarity = avgSimilarity;
				} else if (Math.abs(avgSimilarity - maxSimilarity) < EPSILON) {
					idsWithMaxSimilarity.add(id);
				}
			}
		}

		return new Pair<>(idsWithMaxSimilarity, maxSimilarity);
	}

	public Pair<ArrayList<AncestralLine>, Float> getMaxLineSimilarity() {
		final ArrayList<AncestralLine> linesWithMaxSimilarity = new ArrayList<>();
		Float maxSimilarity = null;

		for (final var entry : lineSimilarities.entrySet()) {
			final var line = entry.getKey();
			final var avgSimilarity = entry.getValue();

			if (maxSimilarity == null || avgSimilarity > maxSimilarity) {
				linesWithMaxSimilarity.clear();
				linesWithMaxSimilarity.add(line);
				maxSimilarity = avgSimilarity;
			} else if (Math.abs(avgSimilarity - maxSimilarity) < EPSILON) {
				linesWithMaxSimilarity.add(line);
			}
		}

		return new Pair<>(linesWithMaxSimilarity, maxSimilarity);
	}

	public static AnalysisResult fromJSON(final JsonNode json, final FacialFeature facialFeature) {
		final Map<String, Similarity> similarities = new HashMap<>();
		final var similarityProperties = json.get("similarities").get(facialFeature.name()).properties();
		for (final var entry : similarityProperties) {
			similarities.put(entry.getKey(), Similarity.fromJSON(entry.getValue()));
		}

		final Map<AncestralLine, Float> lineSimilarities = new HashMap<>();
		final var lineSimilarityProperties = json.get("line_similarities").get(facialFeature.name()).properties();
		for (final var entry : lineSimilarityProperties) {
			lineSimilarities.put(AncestralLine.parse(entry.getKey()), Float.parseFloat(entry.getValue().asText()));
		}

		return new AnalysisResult(similarities, lineSimilarities);
	}
}
