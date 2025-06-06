package bettinger.gedcomviewer.tools.portraitcomparison.model;

import java.util.ArrayList;
import java.util.Arrays;

import com.fasterxml.jackson.databind.JsonNode;

import bettinger.gedcomviewer.utils.JSONUtils;

public class AncestralLine {

    private final String[] ancestorIDs;

    public AncestralLine(final String[] ancestorIDs) {
        this.ancestorIDs = ancestorIDs;
    }

    public static AncestralLine fromString(final String bottomUpLine) {
        final JsonNode pathNode = JSONUtils.fromString(bottomUpLine);
        ArrayList<String> pathArray = new ArrayList<>();
        for(JsonNode personID : pathNode) {
            pathArray.add(personID.asText());
        }
        return new AncestralLine((String[])pathArray.toArray());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(ancestorIDs);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AncestralLine) {
            for(int i=0; i<ancestorIDs.length; i++) {
                if (ancestorIDs[i] != ((AncestralLine)o).ancestorIDs[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
