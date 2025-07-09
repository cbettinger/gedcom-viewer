package bettinger.gedcomviewer.views.tabs.individuals.navigation;

import java.awt.Component;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.google.common.eventbus.Subscribe;

import bettinger.gedcomviewer.Events;
import bettinger.gedcomviewer.model.GEDCOM.GEDCOMEvent;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.views.AutoCompletion;

class IndividualsComboBox extends JComboBox<Individual> {

	private final DefaultComboBoxModel<Individual> model;

	IndividualsComboBox() {
		super(new DefaultComboBoxModel<>());

		model = (DefaultComboBoxModel<Individual>) super.getModel();

		setRenderer(new ListCellRenderer<Individual>() {
			private static final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

			@Override
			public Component getListCellRendererComponent(JList<? extends Individual> list, Individual value, int index, boolean isSelected, boolean cellHasFocus) {
				final var label = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (label != null && value != null) {
					label.setText(value.getNameAndNumber());
				}
				return label;
			}
		});

		setEnabled(false);

		AutoCompletion.addTo(this);

		Events.register(new Object() {

			@Subscribe
			void onGedcomEvent(final GEDCOMEvent event) {
				final var gedcom = event.getGEDCOM();

				setEnabled(gedcom != null && gedcom.isLoaded());

				model.removeAllElements();

				if (gedcom != null && gedcom.isLoaded()) {
					model.addAll(gedcom.getIndividuals().stream().sorted((i1, i2) -> i1.getName().compareTo(i2.getName())).toList());
					setSelectedIndex(-1);
				}
			}
		});
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);

		setEditable(enabled);
	}
}
