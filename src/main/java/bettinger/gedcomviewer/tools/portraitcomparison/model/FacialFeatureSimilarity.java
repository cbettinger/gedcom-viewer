package bettinger.gedcomviewer.tools.portraitcomparison.model;

import com.fasterxml.jackson.databind.JsonNode;

public class FacialFeatureSimilarity {
    private final Float maxSimilarity;
    private final Float avgSimilarity;
    private final String maxMatchTargetFileName;
    private final String maxMatchAncestorFileName;

    public FacialFeatureSimilarity(Float maxSimilarity, Float avgSimilarity, String maxMatchTargetFileName, String maxMatchAncestorFileName) {
        this.maxSimilarity = maxSimilarity;
        this.avgSimilarity = avgSimilarity;
        this.maxMatchTargetFileName = maxMatchTargetFileName;
        this.maxMatchAncestorFileName = maxMatchAncestorFileName;
    }
    
    public Float getMaxSimilarity() {
        return maxSimilarity;
    }

    public Float getAvgSimilarity() {
        return avgSimilarity;
    }

    public String getMaxMatchTargetFileName() {
        return maxMatchTargetFileName;
    }

    public String getMaxMatchAncestorFileName() {
        return maxMatchAncestorFileName;
    }

    public static FacialFeatureSimilarity fromJSON(final JsonNode json) {
        return new FacialFeatureSimilarity(Float.parseFloat(json.get("maxSimilarity").asText()), Float.parseFloat(json.get("avgSimilarity").asText()), json.get("maxMatchImgTarget").asText(), json.get("maxMatchImgAncestor").asText());
    }
}
