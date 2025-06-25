package bettinger.gedcomviewer.views.tabs.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.javatuples.Quintet;

import com.google.common.eventbus.Subscribe;

import bettinger.gedcomviewer.Events;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.Preferences;
import bettinger.gedcomviewer.model.Fact;
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
	private CheckBox animationCheckBox;

	public MapPanel() {
		super(false, "map");

		setEnabled(false);

		configPane = new VBox();
		radioButtons = new ToggleGroup();
		locationsRadioButton = new RadioButton(I18N.get("Locations"));
		lineageRadioButton = new RadioButton(I18N.get("Lineage"));
		ancestorsRadioButton = new RadioButton(I18N.get("Ancestors"));
		descendantsRadioButton = new RadioButton(I18N.get("Descendants"));
		individualsComboBox = new IndividualsComboBox();
		pathsCheckBox = new CheckBox(I18N.get("Paths"));
		animationCheckBox = new CheckBox(I18N.get("Animation"));

		radioButtons.selectedToggleProperty().addListener(_ -> update());
		locationsRadioButton.setToggleGroup(radioButtons);
		lineageRadioButton.setToggleGroup(radioButtons);
		ancestorsRadioButton.setToggleGroup(radioButtons);
		descendantsRadioButton.setToggleGroup(radioButtons);

		individualsComboBox.valueProperty().addListener((ObservableValue<? extends Individual> _, Individual _, Individual newValue) -> {
			proband = newValue;
			update();
		});

		pathsCheckBox.setOnAction(_ -> update());
		animationCheckBox.setOnAction(_ -> update());

		configPane.setPadding(new Insets(PADDING, PADDING, PADDING, PADDING));
		configPane.setSpacing(PADDING);
		configPane.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
		configPane.setBorder(new Border(new BorderStroke(Color.gray(0.69), BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(2))));
		configPane.getChildren().addAll(locationsRadioButton, lineageRadioButton, ancestorsRadioButton, descendantsRadioButton, individualsComboBox, pathsCheckBox, animationCheckBox);

		AnchorPane.setBottomAnchor(configPane, MARGIN);
		AnchorPane.setLeftAnchor(configPane, MARGIN);

		add(configPane);

		Events.register(new Object() {
			@Subscribe
			void onGedcomEvent(final GEDCOMEvent event) {
				gedcom = event.getGEDCOM();
				proband = Preferences.getProband(gedcom);
				locationCount = gedcom != null && gedcom.isLoaded() ? gedcom.getLocations().size() : 0;
				reset();
			}
		});
	}

	@Override
	protected void onLoad() {
		super.onLoad();

		reset();
	}

	private void reset() {	// TODO: store settings
		Platform.runLater(() -> {
			radioButtons.selectToggle(locationsRadioButton);

			if (proband != null) {
				individualsComboBox.getSelectionModel().select(proband);
			} else {
				individualsComboBox.getSelectionModel().clearSelection();
			}

			pathsCheckBox.setSelected(false);
			animationCheckBox.setSelected(false);
		});
	}

	private void update() {
		Platform.runLater(() -> {
			setEnabled(gedcom != null && gedcom.isLoaded());

			final var selectedRadioButton = radioButtons.getSelectedToggle();

			individualsComboBox.setDisable(selectedRadioButton == locationsRadioButton);
			pathsCheckBox.setDisable(selectedRadioButton == locationsRadioButton || animationCheckBox.isSelected());
			animationCheckBox.setDisable(selectedRadioButton == locationsRadioButton || pathsCheckBox.isSelected());

			if (selectedRadioButton == locationsRadioButton) {
				showLocations();
			} else if (selectedRadioButton == lineageRadioButton) {
				showLineage();
			} else if (selectedRadioButton == ancestorsRadioButton) {
				showAncestors();
			} else if (selectedRadioButton == descendantsRadioButton) {
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
		Platform.runLater(() -> {
			if (js != null && gedcom != null && gedcom.isLoaded() && proband != null) {
				final var facts = new ArrayList<ArrayList<Fact>>();

				proband.getLineage(Preferences.getLineageMode()).forEach(quintet -> {
					final var factsOfIndividual = new ArrayList<Fact>();

					final var individual = quintet.getValue1();

					final var birth = individual.getBirthOrBaptism();
					if (birth != null && birth.getDate() != null && birth.getLocation() != null) {
						factsOfIndividual.add(birth);
					}

					final var death = individual.getDeathOrBurial();
					if (death != null && death.getDate() != null && death.getLocation() != null) {
						factsOfIndividual.add(death);
					}

					individual.getFacts("RESI").stream().filter(fact -> fact.getDate() != null && fact.getLocation() != null).forEach(factsOfIndividual::add);	// TODO: start/end

					final var family = individual == proband ? null : quintet.getValue2();
					if (family != null) {
						final var marriage = family.getMarriage();
						if (marriage != null && marriage.getDate() != null && marriage.getLocation() != null) {
							factsOfIndividual.add(marriage);
						}
					}

					Collections.sort(factsOfIndividual, Comparator.comparing(Fact::getDate));

					facts.add(factsOfIndividual);
				});

				System.out.println(JSONUtils.toJSON(facts));	// TODO: remove

				js.call("showLineage", JSONUtils.toJSON(facts), pathsCheckBox.isSelected(), animationCheckBox.isSelected());
			}
		});
	}

	private void showAncestors() {
		Platform.runLater(() -> {
			if (js != null && gedcom != null && gedcom.isLoaded() && proband != null) {
				js.call("showAncestors", JSONUtils.toJSON(proband.getAncestorsList().stream().collect(Collectors.toMap(Quintet::getValue0, Quintet::getValue1))), pathsCheckBox.isSelected(), animationCheckBox.isSelected());
			}
		});
	}

	private void showDescendants() {
		Platform.runLater(() -> {
			if (js != null && gedcom != null && gedcom.isLoaded() && proband != null) {
				js.call("showDescendants", JSONUtils.toJSON(proband.getDescendantsList().stream().map(Quintet::getValue1).toList()), pathsCheckBox.isSelected(), animationCheckBox.isSelected());
			}
		});
	}

	@Override
	public int getRecordCount() {
		return locationCount;
	}
}
