package bettinger.gedcomviewer.tools.portraitcomparison.model;

import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;

public class FacialFeatureAnalysisResult {
    private final HashMap<String, FacialFeatureSimilarity> personSimilarities;
    private final HashMap<AncestralLine, Float> pathSimilarities;

    public FacialFeatureAnalysisResult(HashMap<String, FacialFeatureSimilarity> personSimilarities, HashMap<AncestralLine, Float> pathSimilarities) {
        this.personSimilarities = personSimilarities;
        this.pathSimilarities = pathSimilarities;
    }

    public HashMap<String, FacialFeatureSimilarity> getPersonSimilarities() {
        return personSimilarities;
    }

    public HashMap<AncestralLine, Float> getPathSimilarities() {
        return pathSimilarities;
    }

    public static FacialFeatureAnalysisResult fromJSON(final JsonNode json, final String facialFeature) {
        final JsonNode personSimilaritiesNode = json.get("nodes").get(facialFeature);
        final JsonNode pathSimilaritiesNode = json.get("pathSimilarities").get(facialFeature);

        HashMap<String, FacialFeatureSimilarity> personSimilarities = new HashMap<>();
        HashMap<AncestralLine, Float> pathSimilarities = new HashMap<>();

        final var personSimilarityEntries = personSimilaritiesNode.fields();
        while (personSimilarityEntries.hasNext()) {
            final var entry = personSimilarityEntries.next();
            personSimilarities.put(entry.getKey(), FacialFeatureSimilarity.fromJSON(entry.getValue()));
        }

        final var pathSimilarityEntries = pathSimilaritiesNode.fields();
        while (pathSimilarityEntries.hasNext()) {
            final var entry = pathSimilarityEntries.next();
            pathSimilarities.put(AncestralLine.fromString(entry.getKey()), Float.parseFloat(entry.getValue().asText()));
        }

        return new FacialFeatureAnalysisResult(personSimilarities, pathSimilarities);
    }
}
