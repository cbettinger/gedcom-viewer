package bettinger.gedcomviewer.tools.portraitcomparison.model;

import java.awt.Image;

public class FacialFeatureSimilarity {
    private final double maxSimilarity;
    private final double avgSimilarity;
    private final String maxMatchTargetFileName;
    private final String maxMatchAncestorFileName;

    public FacialFeatureSimilarity(double maxSimilarity, double avgSimilarity, String maxMatchTargetFileName, String maxMatchAncestorFileName) {
        this.maxSimilarity = maxSimilarity;
        this.avgSimilarity = avgSimilarity;
        this.maxMatchTargetFileName = maxMatchTargetFileName;
        this.maxMatchAncestorFileName = maxMatchAncestorFileName;
    }
    
    public double getMaxSimilarity() {
        return maxSimilarity;
    }

    public double getAvgSimilarity() {
        return avgSimilarity;
    }

    public String getMaxMatchTargetFileName() {
        return maxMatchTargetFileName;
    }

    public String getMaxMatchAncestorFileName() {
        return maxMatchAncestorFileName;
    }
    }
}
