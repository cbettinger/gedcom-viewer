package bettinger.gedcomviewer.tools.facialfeatureanalysis.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import bettinger.gedcomviewer.model.Individual;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
class IndividualDTO {
	@JsonProperty
	private final String id;
	@JsonProperty
	private final List<PortraitDTO> portraits;
	@JsonProperty
	private final IndividualDTO father;
	@JsonProperty
	private final IndividualDTO mother;

	IndividualDTO(final Individual individual, final int maxDepth) {
		this(individual, maxDepth, 0);
	}

	private IndividualDTO(final Individual individual, final int maxDepth, final int depth) {
		this.id = individual.getId();

		this.portraits = new ArrayList<>();
		individual.getFacialPortraits().forEach(portrait -> this.portraits.add(new PortraitDTO(portrait.getFilePath(), individual.getImageClip(portrait))));

		this.father = depth < maxDepth && individual.getFather() != null ? new IndividualDTO(individual.getFather(), maxDepth, depth + 1) : null;
		this.mother = depth < maxDepth && individual.getMother() != null ? new IndividualDTO(individual.getMother(), maxDepth, depth + 1) : null;
	}
}
