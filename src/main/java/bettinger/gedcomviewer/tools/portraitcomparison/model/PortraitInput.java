package bettinger.gedcomviewer.tools.portraitcomparison.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class PortraitInput {
    @JsonProperty
	private final String filePath;

    @JsonProperty
	private final int[][] boxPoints;

    public PortraitInput(final String filePath) {
        this.filePath = filePath;
        this.boxPoints = new int[2][2];
    }

    public PortraitInput(final String filePath, final int xMin, final int yMin, final int xMax, final int yMax) {
        this(filePath);

        this.setBoxPoints(xMin, yMin, xMax, yMax);
    }

    public void setBoxPoints(final int xMin, final int yMin, final int xMax, final int yMax) {
        this.boxPoints[0][0] = xMin;
        this.boxPoints[0][1] = yMin;
        this.boxPoints[1][0] = xMax;
        this.boxPoints[1][1] = yMax;
    }
}
