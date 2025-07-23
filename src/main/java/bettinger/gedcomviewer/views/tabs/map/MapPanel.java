package bettinger.gedcomviewer.views.tabs.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.javatuples.Quintet;

import com.google.common.eventbus.Subscribe;

import bettinger.gedcomviewer.Events;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.Preferences;
import bettinger.gedcomviewer.Preferences.ProbandChangedEvent;
import bettinger.gedcomviewer.model.Fact;
import bettinger.gedcomviewer.model.Family;
import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.model.GEDCOM.GEDCOMEvent;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.utils.JSONUtils;
import bettinger.gedcomviewer.views.WebViewPanel;
import bettinger.gedcomviewer.views.tabs.IRecordCollectionView;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class MapPanel extends WebViewPanel implements IRecordCollectionView {

	private static final double MARGIN = 25.0;
	private static final int PADDING = 10;

	private GEDCOM gedcom;
	private int locationCount;
	private Individual proband;

	private VBox configPane;
	private ToggleGroup radioButtons;
	private RadioButton locationsRadioButton;
	private RadioButton lineageRadioButton;
	private RadioButton ancestorsRadioButton;
	private RadioButton descendantsRadioButton;
	private IndividualsComboBox individualsComboBox;
	private CheckBox pathsCheckBox;

	public MapPanel() {
		super(false, "map");

		setEnabled(false);

		configPane = new VBox();

		locationsRadioButton = new RadioButton(I18N.get("Locations"));
		lineageRadioButton = new RadioButton(I18N.get("Lineage"));
		ancestorsRadioButton = new RadioButton(I18N.get("Ancestors"));
		descendantsRadioButton = new RadioButton(I18N.get("Descendants"));

		radioButtons = new ToggleGroup();
		radioButtons.selectedToggleProperty().addListener(_ -> update());
		locationsRadioButton.setToggleGroup(radioButtons);
		lineageRadioButton.setToggleGroup(radioButtons);
		ancestorsRadioButton.setToggleGroup(radioButtons);
		descendantsRadioButton.setToggleGroup(radioButtons);

		individualsComboBox = new IndividualsComboBox();
		pathsCheckBox = new CheckBox(I18N.get("Paths"));

		individualsComboBox.valueProperty().addListener((ObservableValue<? extends Individual> _, Individual _, Individual newValue) -> {
			proband = newValue;
			update();
		});

		pathsCheckBox.setOnAction(_ -> update());

		configPane.setPadding(new Insets(PADDING, PADDING, PADDING, PADDING));
		configPane.setSpacing(PADDING);
		configPane.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
		configPane.setBorder(new Border(new BorderStroke(Color.gray(0.69), BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(2))));
		configPane.getChildren().addAll(locationsRadioButton, lineageRadioButton, ancestorsRadioButton, descendantsRadioButton, individualsComboBox, pathsCheckBox);

		AnchorPane.setBottomAnchor(configPane, MARGIN);
		AnchorPane.setLeftAnchor(configPane, MARGIN);

		add(configPane);

		Events.register(new Object() {
			@Subscribe
			void onGedcomEvent(final GEDCOMEvent event) {
				gedcom = event.getGEDCOM();
				locationCount = gedcom != null && gedcom.isLoaded() ? gedcom.getLocations().size() : 0;
				proband = gedcom != null && gedcom.isLoaded() ? Preferences.getProband(gedcom) : null;
				reset();
			}

			@Subscribe
			void onProbandChangedEvent(final ProbandChangedEvent event) {
				proband = event.getProband();
				reset();
			}
		});
	}

	@Override
	protected void onLoad() {
		super.onLoad();

		reset();
	}

	private void reset() {
		Platform.runLater(() -> {
			var selectedRadioButton = locationsRadioButton;

			final var view = Preferences.getMapPanelView();
			if (view == View.LINEAGE) {
				selectedRadioButton = lineageRadioButton;
			} else if (view == View.ANCESTORS) {
				selectedRadioButton = ancestorsRadioButton;
			} else if (view == View.DESCENDANTS) {
				selectedRadioButton = descendantsRadioButton;
			}

			radioButtons.selectToggle(selectedRadioButton);

			if (proband != null) {
				individualsComboBox.getSelectionModel().select(proband);
			} else {
				individualsComboBox.getSelectionModel().clearSelection();
			}

			pathsCheckBox.setSelected(Preferences.getMapPanelPathsOption());
		});
	}

	private void update() {
		Platform.runLater(() -> {
			setEnabled(gedcom != null && gedcom.isLoaded());

			final var selectedRadioButton = radioButtons.getSelectedToggle();
			individualsComboBox.setDisable(selectedRadioButton == locationsRadioButton);
			pathsCheckBox.setDisable(selectedRadioButton == locationsRadioButton);

			Preferences.setMapPanelPathsOption(pathsCheckBox.isSelected());

			if (selectedRadioButton == locationsRadioButton) {
				Preferences.setMapPanelView(View.LOCATIONS);
				showLocations();
			} else if (selectedRadioButton == lineageRadioButton) {
				Preferences.setMapPanelView(View.LINEAGE);
				showLineage();
			} else if (selectedRadioButton == ancestorsRadioButton) {
				Preferences.setMapPanelView(View.ANCESTORS);
				showAncestors();
			} else if (selectedRadioButton == descendantsRadioButton) {
				Preferences.setMapPanelView(View.DESCENDANTS);
				showDescendants();
			}
		});
	}

	private void showLocations() {
		Platform.runLater(() -> {
			if (js != null && gedcom != null && gedcom.isLoaded()) {
				js.call("showLocations", JSONUtils.toJSON(gedcom.getLocations()));
			}
		});
	}

	private void showLineage() {
		if (gedcom != null && gedcom.isLoaded() && proband != null) {
			showFacts(proband.getLineage(Preferences.getLineageMode()));
		}
	}

	private void showAncestors() {
		if (gedcom != null && gedcom.isLoaded() && proband != null) {
			showFacts(proband.getAncestorsList());
		}
	}

	private void showDescendants() {
		if (gedcom != null && gedcom.isLoaded() && proband != null) {
			showFacts(proband.getDescendantsList());
		}
	}

	private void showFacts(final List<Quintet<String, Individual, Family, Individual, Integer>> data) {
		Platform.runLater(() -> {
			if (js != null) {
				final var facts = new ArrayList<ArrayList<Fact>>();

				data.forEach(quintet -> {
					final var factsOfIndividual = new ArrayList<Fact>();

					final var individual = quintet.getValue1();

					final var birth = individual.getBirthOrBaptism();
					if (birth != null && birth.getDate() != null && birth.getLocation() != null) {
						factsOfIndividual.add(birth);
					}

					individual.getFacts("RESI").stream().filter(fact -> fact.getDate() != null && fact.getLocation() != null).forEach(factsOfIndividual::add);

					final var death = individual.getDeathOrBurial();
					if (death != null && death.getDate() != null && death.getLocation() != null) {
						factsOfIndividual.add(death);
					}

					final var family = quintet.getValue2();
					if (family != null) {
						final var marriage = family.getMarriage();
						if (marriage != null && marriage.getDate() != null && marriage.getLocation() != null) {
							factsOfIndividual.add(marriage);
						}
					}

					Collections.sort(factsOfIndividual, Comparator.comparing(Fact::getDate));

					facts.add(factsOfIndividual);
				});

				js.call("showFacts", JSONUtils.toJSON(facts), pathsCheckBox.isSelected());
			}
		});
	}

	@Override
	public int getRecordCount() {
		return locationCount;
	}

	public enum View {
		LOCATIONS, LINEAGE, ANCESTORS, DESCENDANTS;
	}
}
