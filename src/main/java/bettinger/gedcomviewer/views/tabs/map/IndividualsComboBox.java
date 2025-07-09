package bettinger.gedcomviewer.views.tabs.map;

import com.google.common.eventbus.Subscribe;

import bettinger.gedcomviewer.Events;
import bettinger.gedcomviewer.model.GEDCOM.GEDCOMEvent;
import bettinger.gedcomviewer.model.Individual;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

class IndividualsComboBox extends ComboBox<Individual> {

	public IndividualsComboBox() {
		super();

		setCellFactory(_ -> new ListCell<Individual>() {
			@Override
			protected void updateItem(Individual t, boolean bln) {
				super.updateItem(t, bln);
				setText(t == null ? null : t.getNameAndNumber());
			}
		});

		Events.register(new Object() {

			@Subscribe
			void onGedcomEvent(final GEDCOMEvent event) {
				final var gedcom = event.getGEDCOM();

				Platform.runLater(() -> {
					getItems().clear();
					if (gedcom != null && gedcom.isLoaded()) {
						getItems().addAll(gedcom.getIndividuals().stream().sorted((i1, i2) -> i1.getName().compareTo(i2.getName())).toList());
						getSelectionModel().clearSelection();
					}
				});
			}
		});
	}
}
