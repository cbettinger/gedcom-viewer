package bettinger.gedcomviewer.tools.facialfeatureanalysis.model;

import com.fasterxml.jackson.databind.JsonNode;

public class Similarity {
	private final float avgSimilarity;
	private final float maxSimilarity;
	private final String maxSimilarityProbandPortrait;
	private final String maxSimilarityAncestorPortrait;

	private Similarity(final float avgSimilarity, final float maxSimilarity, final String maxSimilarityProbandPortrait, final String maxSimilarityAncestorPortrait) {
		this.avgSimilarity = avgSimilarity;
		this.maxSimilarity = maxSimilarity;
		this.maxSimilarityProbandPortrait = maxSimilarityProbandPortrait;
		this.maxSimilarityAncestorPortrait = maxSimilarityAncestorPortrait;
	}

	public float getAvgSimilarity() {
		return avgSimilarity;
	}

	public float getMaxSimilarity() {
		return maxSimilarity;
	}

	public String getMaxSimilarityProbandsPortrait() {
		return maxSimilarityProbandPortrait;
	}

	public String getMaxSimilarityAncestorPortrait() {
		return maxSimilarityAncestorPortrait;
	}

	public static Similarity fromJSON(final JsonNode json) {
		if (json.properties().isEmpty() && json.asText().isEmpty()) {	// TODO: ||?
			return null;
		}

		return new Similarity(Float.parseFloat(json.get("avgSimilarity").asText()), Float.parseFloat(json.get("maxSimilarity").asText()), json.get("maxSimilarityProbandPortrait").asText(), json.get("maxSimilarityAncestorPortrait").asText());
	}
}
