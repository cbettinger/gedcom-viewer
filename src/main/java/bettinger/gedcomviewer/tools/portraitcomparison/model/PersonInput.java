package bettinger.gedcomviewer.tools.portraitcomparison.model;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;

import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.model.Media;

import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class PersonInput {
	@JsonProperty
	private final String id;
	@JsonProperty
	private final List<PortraitInput> portraits;
	@JsonProperty
	private PersonInput father;
	@JsonProperty
	private PersonInput mother;

	public PersonInput(final Individual individual) {
		this.id = individual.getId();
		this.portraits = new ArrayList<>();
		this.mother = null;
		this.father = null;

		Map<Media, Rectangle> portraitData = individual.getPortraits();
		for (Map.Entry<Media, Rectangle> entry : portraitData.entrySet()) {
			Media medium = entry.getKey();
			Rectangle clip = entry.getValue();

			String fileName = medium.getFileName();

			PortraitInput portraitInput = new PortraitInput(fileName);
			if (clip != null) {
				portraitInput.setBoxPoints(clip.x, clip.y, clip.x + clip.width, clip.y + clip.height);
			}

			this.portraits.add(portraitInput);
		}
	}

	public PersonInput(final Individual individual, final int currentDepth, final int maxDepth) {
		this(individual);

		if (currentDepth < maxDepth) {
			Individual father = individual.getFather();
			if (father != null) {
				this.father = new PersonInput(father, currentDepth + 1, maxDepth);
			}
			Individual mother = individual.getMother();
			if (mother != null) {
				this.mother = new PersonInput(mother, currentDepth + 1, maxDepth);
			}
		}
	}
}
