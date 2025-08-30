package bettinger.gedcomviewer.tools.facialfeatureanalysis.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.javatuples.Pair;

import com.fasterxml.jackson.databind.JsonNode;

public class FacialFeatureAnalysisResult {
    private static final float EPSILON = 0.000001f; 
    
    private final HashMap<String, FacialFeatureSimilarity> similaritiesToProband;
    private final HashMap<String, Float> similaritiesToChild;
    private final ArrayList<String> personsWithMaxSimOnBestPaths;
    private final Float maxPersonSimilarityOnBestPath;
    private final ArrayList<AncestralLine> pathsWithMaxSim;

    public FacialFeatureAnalysisResult(HashMap<String, FacialFeatureSimilarity> similariitiesToProband, ArrayList<AncestralLine> bestPaths, HashMap<String, Float> similaritiesToChild) {
        this.similaritiesToProband = similariitiesToProband;
        this.similaritiesToChild = similaritiesToChild;
        this.pathsWithMaxSim = bestPaths;

        var maxPersonSimOnBestPaths = this.findMaxPersonSimilaritiesOnBestPaths();
        this.personsWithMaxSimOnBestPaths = maxPersonSimOnBestPaths.getValue0();
        this.maxPersonSimilarityOnBestPath = maxPersonSimOnBestPaths.getValue1();
    }

    private Pair<ArrayList<String>, Float> findMaxPersonSimilaritiesOnBestPaths() {
        ArrayList<String> idsWithMaxSim = new ArrayList<>();
        Float maxSim = null;
        for (var path : pathsWithMaxSim) {
            for (var id : path.getAncestorIDs()) {
                final var featureSim = similaritiesToProband.get(id);
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

    public HashMap<String, FacialFeatureSimilarity> getSimilaritiesToProband() {
        return similaritiesToProband;
    }

    public HashMap<String, Float> getSimilaritiesToChild() {
        return similaritiesToChild;
    }

    public Float getMaxPersonSimilarityOnBestPath() {
        return maxPersonSimilarityOnBestPath;
    }

    public ArrayList<String> getPersonsWithMaxSimOnBestPaths() {
        return personsWithMaxSimOnBestPaths;
    }

    public ArrayList<AncestralLine> getPathsWithMaxSim() {
        return pathsWithMaxSim;
    }

    public static FacialFeatureAnalysisResult fromJSON(final JsonNode json, final String facialFeature) {
        final JsonNode personSimilaritiesNode = json.get("nodes").get(facialFeature);
        final JsonNode bestPathsNode = json.get("bestPaths").get(facialFeature);
        final JsonNode edgeSimilaritiesNode = json.get("edgeSimilarities").get(facialFeature);

        HashMap<String, FacialFeatureSimilarity> personSimilarities = new HashMap<>();
        ArrayList<AncestralLine> bestPaths = new ArrayList<>();
        HashMap<String, Float> edgeSimilarities = new HashMap<>();

        final var personSimilarityEntries = personSimilaritiesNode.properties();
        for (final var entry : personSimilarityEntries) {
            personSimilarities.put(entry.getKey(), FacialFeatureSimilarity.fromJSON(entry.getValue()));
        }

        for (final var bestPath : bestPathsNode) {
            bestPaths.add(AncestralLine.fromJSON(bestPath));
        }

        final var edgeSimilarityEntries = edgeSimilaritiesNode.properties();
        for (final var entry : edgeSimilarityEntries) {
            edgeSimilarities.put(entry.getKey(), Float.parseFloat(entry.getValue().asText()));
        }

        return new FacialFeatureAnalysisResult(personSimilarities, bestPaths, edgeSimilarities);
    }
}
