package bettinger.gedcomviewer.views.tabs.individuals.navigation.tabs.descendants;

import org.javatuples.Quintet;

import bettinger.gedcomviewer.Preferences;
import bettinger.gedcomviewer.model.Family;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.views.TableModel;
import bettinger.gedcomviewer.views.tabs.individuals.navigation.NavigationTab;

public class DescendantsTab extends NavigationTab {
	@Override
	protected TableModel<Quintet<String, Individual, Family, Individual, Integer>> createTableModel(final Individual proband) {
		return new DescendantsTableModel(proband.getDescendantsList(Preferences.getGenerations(this)));
	}
}
