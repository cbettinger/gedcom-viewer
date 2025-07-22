package bettinger.gedcomviewer.views.tabs.individuals.navigation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.javatuples.Quintet;

import com.google.common.eventbus.Subscribe;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.Events;
import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.Preferences;
import bettinger.gedcomviewer.model.Family;
import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.model.GEDCOM.GEDCOMEvent;
import bettinger.gedcomviewer.views.GenerationsComboBox;
import bettinger.gedcomviewer.views.TableModel;

public abstract class NavigationTab extends JPanel {

	private final NavigationTable table;
	private final GenerationsComboBox generationsComboBox;

	private GEDCOM gedcom;

	protected NavigationTab() {
		this.table = new NavigationTable();

		setLayout(new BorderLayout(0, Constants.BORDER_SIZE));
		setBorder(BorderFactory.createEmptyBorder(0, Constants.BORDER_SIZE, 0, 0));

		add(new JScrollPane(table), BorderLayout.CENTER);

		final var generationsBox = new JPanel();
		generationsBox.setLayout(new FlowLayout(FlowLayout.TRAILING, 0, 0));

		final var label = new JLabel(String.format(Format.TRAILING_COLON, I18N.get("Generations")));
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, Constants.BORDER_SIZE));
		generationsBox.add(label);

		generationsComboBox = new GenerationsComboBox();
		generationsComboBox.addActionListener(x -> Preferences.setGenerations(this, (int) generationsComboBox.getSelectedItem()));
		generationsComboBox.setSelectedItem(Preferences.getGenerations(this));
		generationsBox.add(generationsComboBox);

		add(generationsBox, BorderLayout.SOUTH);

		Events.register(new Object() {

			@Subscribe
			void onGedcomEvent(final GEDCOMEvent event) {
				gedcom = event.getGEDCOM();

				update();
			}

			@Subscribe
			void onProbandChangedEvent(final Preferences.ProbandChangedEvent event) {
				update();
			}

			@Subscribe
			void onGenerationsChangedEvent(final Preferences.GenerationsChangedEvent event) {
				update();
			}
		});
	}

	protected void update() {
		table.setEnabled(gedcom != null && gedcom.isLoaded());

		table.setModel(null);

		if (gedcom != null && gedcom.isLoaded()) {
			final var proband = Preferences.getProband(gedcom);
			if (proband != null) {
				table.setModel(createTableModel(proband));
			}
		}
	}

	protected abstract TableModel<Quintet<String, Individual, Family, Individual, Integer>> createTableModel(@SuppressWarnings("java:S1172") final Individual proband);
}
