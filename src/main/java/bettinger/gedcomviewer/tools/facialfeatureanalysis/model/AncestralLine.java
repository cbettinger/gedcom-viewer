package bettinger.gedcomviewer.tools.facialfeatureanalysis.model;

import java.util.Arrays;
import java.util.List;

public class AncestralLine {

    private final String[] ancestorIDs;

    public AncestralLine(final String[] ancestorIDs) {
        this.ancestorIDs = ancestorIDs;
    }

    public String[] getAncestorIDs() {
        return ancestorIDs;
    }

    public static AncestralLine fromString(final String bottomUpLine) {
        List<String> pathArray = Arrays.asList(bottomUpLine.split("\\s*,\\s*"));
        return new AncestralLine(pathArray.toArray(new String[0]));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(ancestorIDs);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AncestralLine) {
            for (int i = 0; i < ancestorIDs.length; i++) {
                if (ancestorIDs[i] != ((AncestralLine) o).ancestorIDs[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
