package bettinger.gedcomviewer.views.tabs.individuals.navigation.tabs.lineage;

import org.javatuples.Quintet;

import com.google.common.eventbus.Subscribe;

import bettinger.gedcomviewer.Events;
import bettinger.gedcomviewer.Preferences;
import bettinger.gedcomviewer.model.Family;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.views.TableModel;
import bettinger.gedcomviewer.views.tabs.individuals.navigation.NavigationTab;

public class LineageTab extends NavigationTab {

	public LineageTab() {
		Events.register(new Object() {

			@Subscribe
			void onLineageModeChangedEvent(final Preferences.LineageModeChangedEvent event) {
				update();
			}
		});
	}

	@Override
	protected TableModel<Quintet<String, Individual, Family, Individual, Integer>> createTableModel(final Individual proband) {
		return new LineageTableModel(proband.getLineage(Preferences.getLineageMode(), Preferences.getGenerations(this)));
	}
}
