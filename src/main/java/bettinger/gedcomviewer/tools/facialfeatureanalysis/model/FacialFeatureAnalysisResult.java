package bettinger.gedcomviewer.tools.facialfeatureanalysis.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.javatuples.Pair;

import com.fasterxml.jackson.databind.JsonNode;

public class FacialFeatureAnalysisResult {
    private static final float EPSILON = 0.000001f; 
    
    private final HashMap<String, FacialFeatureSimilarity> personSimilarities;
    private final HashMap<AncestralLine, Float> pathSimilarities;
    private final ArrayList<String> personsWithMaxSimOnBestPaths;
    private final Float maxPersonSimilarityOnBestPath;
    private final Float maxPathSimilarity;
    private final ArrayList<AncestralLine> pathsWithMaxSim;

    public FacialFeatureAnalysisResult(HashMap<String, FacialFeatureSimilarity> personSimilarities, HashMap<AncestralLine, Float> pathSimilarities) {
        this.personSimilarities = personSimilarities;
        this.pathSimilarities = pathSimilarities;

        var maxPathSimilarity = this.findMaxPathSimilarity();
        this.pathsWithMaxSim = maxPathSimilarity.getValue0();
        this.maxPathSimilarity = maxPathSimilarity.getValue1();

        var maxPersonSimOnBestPaths = this.findMaxPersonSimilaritiesOnBestPaths();
        this.personsWithMaxSimOnBestPaths = maxPersonSimOnBestPaths.getValue0();
        this.maxPersonSimilarityOnBestPath = maxPersonSimOnBestPaths.getValue1();
    }

    public HashMap<String, FacialFeatureSimilarity> getPersonSimilarities() {
        return personSimilarities;
    }

    public HashMap<AncestralLine, Float> getPathSimilarities() {
        return pathSimilarities;
    }

    public Float getMaxPersonSimilarityOnBestPath() {
        return maxPersonSimilarityOnBestPath;
    }

    public Float getMaxPathSimilarity() {
        return maxPathSimilarity;
    }

    public ArrayList<String> getPersonsWithMaxSimOnBestPaths() {
        return personsWithMaxSimOnBestPaths;
    }

    public ArrayList<AncestralLine> getPathsWithMaxSim() {
        return pathsWithMaxSim;
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
            final var featureSim = entry.getValue();
            if (featureSim != null) {
                final var avgSim = featureSim.getAvgSimilarity();
                if (maxSimilarity == null || avgSim > maxSimilarity) {
                    idsWithMaxSim.clear();
                    idsWithMaxSim.add(id);
                    maxSimilarity = avgSim;
                } else if (Math.abs(avgSim - maxSimilarity) < EPSILON) {
                    idsWithMaxSim.add(id);
                }
            }
        }
        return new Pair<ArrayList<String>, Float>(idsWithMaxSim, maxSimilarity);
    }

    private Pair<ArrayList<AncestralLine>, Float> findMaxPathSimilarity() {
        ArrayList<AncestralLine> pathsWithMaxSim = new ArrayList<>();
        Float maxSimilarity = null;
        for (final var entry : pathSimilarities.entrySet()) {
            final var path = entry.getKey();
            final var avgSim = entry.getValue();
            if (maxSimilarity == null || avgSim > maxSimilarity) {
                pathsWithMaxSim.clear();
                pathsWithMaxSim.add(path);
                maxSimilarity = avgSim;
            } else if (Math.abs(avgSim - maxSimilarity) < EPSILON) {
                pathsWithMaxSim.add(path);
            }
        }
        return new Pair<ArrayList<AncestralLine>, Float>(pathsWithMaxSim, maxSimilarity);
    }

    private Pair<ArrayList<String>, Float> findMaxPersonSimilaritiesOnBestPaths() {
        ArrayList<String> idsWithMaxSim = new ArrayList<>();
        Float maxSim = null;
        for (var path : pathsWithMaxSim) {
            for (var id : path.getAncestorIDs()) {
                final var featureSim = personSimilarities.get(id);
                if (featureSim != null) {
                    final var avgSim = featureSim.getAvgSimilarity();
                    if (maxSim == null || avgSim > maxSim) {
                        idsWithMaxSim.clear();
                        idsWithMaxSim.add(id);
                        maxSim = avgSim;
                    } else if (Math.abs(avgSim - maxSim) < EPSILON) {
                        idsWithMaxSim.add(id);
                    }
                }
            }
        }
        return new Pair<>(idsWithMaxSim, maxSim);
    }
}
