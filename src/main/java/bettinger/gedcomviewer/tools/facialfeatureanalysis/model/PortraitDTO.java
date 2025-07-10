package bettinger.gedcomviewer.tools.facialfeatureanalysis.model;

import java.awt.Rectangle;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
class PortraitDTO {
	@JsonProperty
	private final String filePath;
	@JsonProperty
	private final int[][] boxPoints;

	PortraitDTO(final String filePath, final Rectangle clip) {
		this.filePath = filePath;
		this.boxPoints = getBoxPoints(clip);
	}

	@SuppressWarnings("java:S1168")
	private int[][] getBoxPoints(final Rectangle clip) {
		if (clip == null) {
			return null;
		}

		final int[][] result = new int[2][2];
		result[0][0] = clip.x;
		result[0][1] = clip.y;
		result[1][0] = clip.x + clip.width;
		result[1][1] = clip.y + clip.height;
		return result;
	}
}
