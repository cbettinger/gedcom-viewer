package bettinger.gedcomviewer.tools.facialfeatureanalysis.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class PortraitInput {
    @JsonProperty
    private final String filePath;

    @JsonProperty
    private int[][] boxPoints;

    public PortraitInput(final String filePath) {
        this.filePath = filePath;
        this.boxPoints = null;
    }

    public PortraitInput(final String filePath, final int xMin, final int yMin, final int xMax, final int yMax) {
        this(filePath);

        this.setBoxPoints(xMin, yMin, xMax, yMax);
    }

    public void setBoxPoints(final int xMin, final int yMin, final int xMax, final int yMax) {
        if (boxPoints == null) {
            boxPoints = new int[2][2];
        }
        boxPoints[0][0] = xMin;
        boxPoints[0][1] = yMin;
        boxPoints[1][0] = xMax;
        boxPoints[1][1] = yMax;
    }
}
