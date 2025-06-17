package bettinger.gedcomviewer.tools.portraitcomparison.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.javatuples.Pair;

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

        final var personSimilarityEntries = personSimilaritiesNode.properties();
        for (final var entry : personSimilarityEntries) {
            personSimilarities.put(entry.getKey(), FacialFeatureSimilarity.fromJSON(entry.getValue()));
        }

        final var pathSimilarityEntries = pathSimilaritiesNode.properties();
        for (final var entry : pathSimilarityEntries) {
            pathSimilarities.put(AncestralLine.fromString(entry.getKey()), Float.parseFloat(entry.getValue().asText()));
        }

        return new FacialFeatureAnalysisResult(personSimilarities, pathSimilarities);
    }

    public Pair<ArrayList<String>, Float> getMaxPersonSimilarity() {
        ArrayList<String> idsWithMaxSim = new ArrayList<>();
        Float maxSimilarity = null;
        for (final var entry : personSimilarities.entrySet()) {
            final var id = entry.getKey();
            final var avgSim = entry.getValue().getAvgSimilarity();
            if (maxSimilarity == null || avgSim > maxSimilarity) {
                idsWithMaxSim.clear();
                idsWithMaxSim.add(id);
                maxSimilarity = avgSim;
            } else if (avgSim == maxSimilarity) {
                idsWithMaxSim.add(id);
            }
        }
        return new Pair<ArrayList<String>, Float>(idsWithMaxSim, maxSimilarity);
    }

    public Pair<ArrayList<AncestralLine>, Float> getMaxPathSimilarity() {
        ArrayList<AncestralLine> pathsWithMaxSim = new ArrayList<>();
        Float maxSimilarity = null;
        for (final var entry : pathSimilarities.entrySet()) {
            final var path = entry.getKey();
            final var avgSim = entry.getValue();
            if (maxSimilarity == null || avgSim > maxSimilarity) {
                pathsWithMaxSim.clear();
                pathsWithMaxSim.add(path);
                maxSimilarity = avgSim;
            } else if (avgSim == maxSimilarity) {
                pathsWithMaxSim.add(path);
            }
        }
        return new Pair<ArrayList<AncestralLine>, Float>(pathsWithMaxSim, maxSimilarity);
    }
}
