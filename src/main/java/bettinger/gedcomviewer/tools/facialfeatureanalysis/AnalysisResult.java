package bettinger.gedcomviewer.tools.facialfeatureanalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.javatuples.Pair;

import com.fasterxml.jackson.databind.JsonNode;

public class AnalysisResult {
	private final Map<String, Similarity> individualSimilarities;
	private final Map<AncestralLine, Float> lineSimilarities;

	private AnalysisResult(final Map<String, Similarity> individualSimilarities, final Map<AncestralLine, Float> lineSimilarities) {
		this.individualSimilarities = individualSimilarities;
		this.lineSimilarities = lineSimilarities;
	}

	public Map<String, Similarity> getIndividualSimilarities() {
		return individualSimilarities;
	}

	public Map<AncestralLine, Float> getLineSimilarities() {
		return lineSimilarities;
	}

	public Pair<ArrayList<String>, Float> getMaxIndividualSimilarity() {
		final ArrayList<String> idsWithMaxSimilarity = new ArrayList<>();
		Float maxSimilarity = null;

		for (final var entry : individualSimilarities.entrySet()) {
			final var id = entry.getKey();
			final var featureSimilarity = entry.getValue();

			if (featureSimilarity != null) {
				final var avgSimilarity = featureSimilarity.getAvgSimilarity();

				if (maxSimilarity == null || avgSimilarity > maxSimilarity) {
					idsWithMaxSimilarity.clear();
					idsWithMaxSimilarity.add(id);
					maxSimilarity = avgSimilarity;
				} else if (avgSimilarity == maxSimilarity) { // TODO: accept epsilon as below?
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
			} else if (Math.abs(avgSimilarity - maxSimilarity) < 0.000001) {
				linesWithMaxSimilarity.add(line);
			}
		}

		return new Pair<>(linesWithMaxSimilarity, maxSimilarity);
	}

	public static AnalysisResult fromJSON(final JsonNode json, final FacialFeature facialFeature) {
		final Map<String, Similarity> individualSimilarities = new HashMap<>();
		final var individualSimilaritiesEntries = json.get("nodes").get(facialFeature.name()).properties();
		for (final var entry : individualSimilaritiesEntries) {
			individualSimilarities.put(entry.getKey(), Similarity.fromJSON(entry.getValue()));
		}

		final Map<AncestralLine, Float> lineSimilarities = new HashMap<>();
		final var lineSimilaritiesEntries = json.get("pathSimilarities").get(facialFeature.name()).properties();
		for (final var entry : lineSimilaritiesEntries) {
			lineSimilarities.put(AncestralLine.parse(entry.getKey()), Float.parseFloat(entry.getValue().asText()));
		}

		return new AnalysisResult(individualSimilarities, lineSimilarities);
	}
}
