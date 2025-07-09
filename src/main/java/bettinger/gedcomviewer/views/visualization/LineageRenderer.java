package bettinger.gedcomviewer.views.visualization;

import bettinger.gedcomviewer.Preferences;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.model.LineageMode;

public class LineageRenderer extends AncestorsRenderer {

	@Override
	protected boolean canRecurseFather(final Individual individual) {
		final var father = individual.getFather();
		return super.canRecurseFather(individual) && father != null && (Preferences.getLineageMode() == LineageMode.MALE_LINE || Individual.getNameLineParent(individual) == father);
	}

	@Override
	protected boolean canRecurseMother(final Individual individual) {
		final var mother = individual.getMother();
		return super.canRecurseMother(individual) && mother != null && (Preferences.getLineageMode() == LineageMode.NAME_LINE && Individual.getNameLineParent(individual) == mother);
	}
}
