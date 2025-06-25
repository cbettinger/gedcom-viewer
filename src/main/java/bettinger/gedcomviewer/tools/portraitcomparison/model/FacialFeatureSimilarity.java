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
        if (json.properties().isEmpty() && json.asText().isEmpty()) {
            return null;
        }
        Float maxSim = Float.parseFloat(json.get("maxSimilarity").asText());
        Float avgSim = Float.parseFloat(json.get("avgSimilarity").asText());
        return new FacialFeatureSimilarity(maxSim, avgSim, json.get("maxMatchImgTarget").asText(), json.get("maxMatchImgAncestor").asText());
    }
}
