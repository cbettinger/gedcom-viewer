package bettinger.gedcomviewer.views.tabs.individuals.navigation;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.google.common.eventbus.Subscribe;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.Events;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.Preferences;
import bettinger.gedcomviewer.model.GEDCOM.GEDCOMEvent;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.views.tabs.individuals.navigation.tabs.ancestors.AncestorsTab;
import bettinger.gedcomviewer.views.tabs.individuals.navigation.tabs.descendants.DescendantsTab;
import bettinger.gedcomviewer.views.tabs.individuals.navigation.tabs.lineage.LineageTab;

public class NavigationSideBar extends JPanel {

	public NavigationSideBar() {
		setLayout(new BorderLayout());

		final var probandComboBox = new IndividualsComboBox();
		probandComboBox.addActionListener(_ -> Preferences.setProband((Individual) probandComboBox.getSelectedItem()));
		add(probandComboBox, BorderLayout.NORTH);

		final var tabbedPane = new JTabbedPane();
		tabbedPane.setBorder(BorderFactory.createEmptyBorder(Constants.BORDER_SIZE, 0, 0, 0));
		tabbedPane.setTabPlacement(SwingConstants.LEFT);
		tabbedPane.addTab(null, new FlatSVGIcon("icons/lineage.svg", Constants.TAB_ICON_SIZE, Constants.TAB_ICON_SIZE), new LineageTab(), I18N.get("Lineage"));
		tabbedPane.addTab(null, new FlatSVGIcon("icons/ancestors.svg", Constants.TAB_ICON_SIZE, Constants.TAB_ICON_SIZE), new AncestorsTab(), I18N.get("AncestorsList"));
		tabbedPane.addTab(null, new FlatSVGIcon("icons/descendants.svg", Constants.TAB_ICON_SIZE, Constants.TAB_ICON_SIZE), new DescendantsTab(), I18N.get("DescendantsList"));
		tabbedPane.setSelectedIndex(Preferences.getNavigationTab());
		tabbedPane.addChangeListener(_ -> Preferences.setNavigationTab(tabbedPane.getSelectedIndex()));
		add(tabbedPane, BorderLayout.CENTER);

		Events.register(new Object() {

			@Subscribe
			void onGedcomEvent(final GEDCOMEvent event) {
				final var gedcom = event.getGEDCOM();
				if (gedcom != null && gedcom.isLoaded()) {
					final var proband = Preferences.getProband(gedcom);
					if (proband != null) {
						probandComboBox.setSelectedItem(proband);
					}
				}
			}
		});
	}
}
