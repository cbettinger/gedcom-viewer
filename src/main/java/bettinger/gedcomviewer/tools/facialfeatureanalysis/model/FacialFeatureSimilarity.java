package bettinger.gedcomviewer.tools.facialfeatureanalysis.model;

import java.awt.Rectangle;

import com.fasterxml.jackson.databind.JsonNode;

public class FacialFeatureSimilarity {
    private final Float avgSimilarity;
    private final String maxMatchTargetFileName;
    private final String maxMatchAncestorFileName;
    private final Rectangle clipTarget;
    private final Rectangle clipAncestor;

    public FacialFeatureSimilarity(Float avgSimilarity, String maxMatchTargetFileName, String maxMatchAncestorFileName, Rectangle clipTarget, Rectangle clipAncestor) {
        this.avgSimilarity = avgSimilarity;
        this.maxMatchTargetFileName = maxMatchTargetFileName;
        this.maxMatchAncestorFileName = maxMatchAncestorFileName;
        this.clipTarget = clipTarget;
        this.clipAncestor = clipAncestor;
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

    public Rectangle getTargetClip() {
        return clipTarget;
    }

    public Rectangle getAncestorClip() {
        return clipAncestor;
    }

    public static FacialFeatureSimilarity fromJSON(final JsonNode json) {
        if (json.properties().isEmpty()) {
            return null;
        }
        Float avgSim = Float.parseFloat(json.get("avgSimilarity").asText());
        JsonNode clipTargetNode = json.get("maxMatchImgTarget").get("box");
        JsonNode clipAncestorNode = json.get("maxMatchImgAncestor").get("box");
        return new FacialFeatureSimilarity(avgSim, json.get("maxMatchImgTarget").get("filename").asText(), json.get("maxMatchImgAncestor").get("filename").asText(), getClipFromJSON(clipTargetNode), getClipFromJSON(clipAncestorNode));
    }

    private static Rectangle getClipFromJSON(final JsonNode clipNode) {
        int x = Integer.parseInt(clipNode.get(0).get(0).asText());
        int y = Integer.parseInt(clipNode.get(0).get(1).asText());
        int width = Integer.parseInt(clipNode.get(1).get(0).asText()) - x;
        int height = Integer.parseInt(clipNode.get(1).get(1).asText()) - y;

        return new Rectangle(x, y, width, height);
    }
}
