package bettinger.gedcomviewer.tools.facialfeatureanalysis.model;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.model.Media;

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

		List<Media> portraits = individual.getFacialPortraits();
		for (Media medium : portraits) {
			Rectangle clip = individual.getImageClip(medium);

			String filePath = medium.getFilePath();

			PortraitInput portraitInput = new PortraitInput(filePath);
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
