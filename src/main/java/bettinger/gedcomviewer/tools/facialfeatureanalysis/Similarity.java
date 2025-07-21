package bettinger.gedcomviewer.tools.facialfeatureanalysis;

import com.fasterxml.jackson.databind.JsonNode;

public class Similarity {
	private final float avgSimilarity;
	private final float maxSimilarity;
	private final String maxSimilarProbandPortraitFilePath;
	private final String maxSimilarAncestorPortraitFilePath;

	private Similarity(final float avgSimilarity, final float maxSimilarity, final String maxSimilarProbandPortraitFilePath, final String maxSimilarAncestorPortraitFilePath) {
		this.avgSimilarity = avgSimilarity;
		this.maxSimilarity = maxSimilarity;
		this.maxSimilarProbandPortraitFilePath = maxSimilarProbandPortraitFilePath;
		this.maxSimilarAncestorPortraitFilePath = maxSimilarAncestorPortraitFilePath;
	}

	public float getAvgSimilarity() {
		return avgSimilarity;
	}

	public float getMaxSimilarity() {
		return maxSimilarity;
	}

	public String getMaxSimilarProbandPortraitFilePath() {
		return maxSimilarProbandPortraitFilePath;
	}

	public String getMaxSimilarAncestorPortraitFilePath() {
		return maxSimilarAncestorPortraitFilePath;
	}

	public static Similarity fromJSON(final JsonNode json) {
		return json.properties().isEmpty() ? null : new Similarity(Float.parseFloat(json.get("avg_similarity").asText()), Float.parseFloat(json.get("max_similarity").asText()), json.get("max_similar_proband_portrait_filepath").asText(), json.get("max_similar_ancestor_portrait_filepath").asText());
	}
}
