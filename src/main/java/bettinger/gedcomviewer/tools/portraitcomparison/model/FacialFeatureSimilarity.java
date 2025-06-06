package bettinger.gedcomviewer.tools.portraitcomparison.model;

import java.awt.Image;

public class FacialFeatureSimilarity {
    private final float maxSimilarity;
    private final float avgSimilarity;
    private final Image targetPersonImage;
    private final Image otherPersonImage;

    public FacialFeatureSimilarity(float maxSimilarity, float avgSimilarity, Image targetPersonImage, Image otherPersonImage) {
        this.maxSimilarity = maxSimilarity;
        this.avgSimilarity = avgSimilarity;
        this.targetPersonImage = targetPersonImage;
        this.otherPersonImage = otherPersonImage;
    }
    
    public float getMaxSimilarity() {
        return maxSimilarity;
    }

    public float getAvgSimilarity() {
        return avgSimilarity;
    }

    public Image getTargetPersonImage() {
        return targetPersonImage;
    }

    public Image getOtherPersonImage() {
        return otherPersonImage;
    }
}
