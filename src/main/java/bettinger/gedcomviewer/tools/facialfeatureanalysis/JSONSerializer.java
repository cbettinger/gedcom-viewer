package bettinger.gedcomviewer.tools.facialfeatureanalysis;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

abstract class JSONSerializer {
	static Individual build(final bettinger.gedcomviewer.model.Individual individual, final int maxDepth) {
		return new Individual(individual, maxDepth, 0);
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
	private static class Individual {
		@JsonProperty
		private final String id;
		@JsonProperty
		private final List<Portrait> portraits;
		@JsonProperty
		private final Individual father;
		@JsonProperty
		private final Individual mother;

		private Individual(final bettinger.gedcomviewer.model.Individual individual, final int maxDepth, final int depth) {
			this.id = individual.getId();

			this.portraits = new ArrayList<>();
			individual.getFacialPortraits().forEach(portrait -> this.portraits.add(new Portrait(portrait.getFilePath(), individual.getImageClip(portrait))));

			this.father = depth < maxDepth && individual.getFather() != null ? new Individual(individual.getFather(), maxDepth, depth + 1) : null;
			this.mother = depth < maxDepth && individual.getMother() != null ? new Individual(individual.getMother(), maxDepth, depth + 1) : null;
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
	private static class Portrait {
		@JsonProperty
		private final String filePath;
		@JsonProperty
		private final int[][] clip;

		Portrait(final String filePath, final Rectangle clip) {
			this.filePath = filePath;
			this.clip = getClip(clip);
		}

		@SuppressWarnings("java:S1168")
		private int[][] getClip(final Rectangle clip) {
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

	private JSONSerializer() {}
}
