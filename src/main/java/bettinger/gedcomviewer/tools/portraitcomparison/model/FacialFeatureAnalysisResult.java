package bettinger.gedcomviewer.tools.portraitcomparison.model;

import java.util.HashMap;

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
}
