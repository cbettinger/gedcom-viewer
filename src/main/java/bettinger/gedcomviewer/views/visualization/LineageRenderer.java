package bettinger.gedcomviewer.views.visualization;

import bettinger.gedcomviewer.Preferences;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.model.LineageMode;

class LineageRenderer extends AncestorsRenderer {

	@Override
	boolean canRecurseFather(final Individual individual) {
		final var father = individual.getFather();
		return super.canRecurseFather(individual) && father != null && (Preferences.getLineageMode() == LineageMode.MALE_LINE || Individual.getNameLineParent(individual) == father);
	}

	@Override
	boolean canRecurseMother(final Individual individual) {
		final var mother = individual.getMother();
		return super.canRecurseMother(individual) && mother != null && (Preferences.getLineageMode() == LineageMode.NAME_LINE && Individual.getNameLineParent(individual) == mother);
	}
}
