package bettinger.gedcomviewer.views.tabs.map;

import java.util.stream.Collectors;

import org.javatuples.Quintet;

import com.google.common.eventbus.Subscribe;

import bettinger.gedcomviewer.Events;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.Preferences;
import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.model.GEDCOM.GEDCOMEvent;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.utils.JSONUtils;
import bettinger.gedcomviewer.views.WebViewPanel;
import bettinger.gedcomviewer.views.tabs.IRecordCollectionView;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
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
	private RadioButton animateFactsRadioButton;

	@SuppressWarnings("unused")
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
		animateFactsRadioButton = new RadioButton(I18N.get("Facts"));	// TODO: label

		radioButtons.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> update());

		locationsRadioButton.setOnAction(event -> showLocations());
		locationsRadioButton.setToggleGroup(radioButtons);

		lineageRadioButton.setOnAction(event -> showLineage());
		lineageRadioButton.setToggleGroup(radioButtons);

		ancestorsRadioButton.setOnAction(event -> showAncestors());
		ancestorsRadioButton.setToggleGroup(radioButtons);

		descendantsRadioButton.setOnAction(event -> showDescendants());
		descendantsRadioButton.setToggleGroup(radioButtons);

		individualsComboBox.valueProperty().addListener((ObservableValue<? extends Individual> observable, Individual oldValue, Individual newValue) -> {
			proband = newValue;

			final var selectedRadioButton = radioButtons.getSelectedToggle();
			if (selectedRadioButton == lineageRadioButton) {
				showLineage();
			} else if (selectedRadioButton == ancestorsRadioButton) {
				showAncestors();
			} else if (selectedRadioButton == descendantsRadioButton) {
				showDescendants();
			}
		});

		animateFactsRadioButton.setOnAction(event -> animateFacts());
		animateFactsRadioButton.setToggleGroup(radioButtons);

		configPane.setPadding(new Insets(PADDING, PADDING, PADDING, PADDING));
		configPane.setSpacing(PADDING);
		configPane.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
		configPane.setBorder(new Border(new BorderStroke(Color.gray(0.69), BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(2))));
		configPane.getChildren().addAll(locationsRadioButton, lineageRadioButton, ancestorsRadioButton, descendantsRadioButton, individualsComboBox, animateFactsRadioButton);

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

		showLocations();
	}

	private void reset() {
		Platform.runLater(() -> {
			radioButtons.selectToggle(locationsRadioButton);

			if (proband != null) {
				individualsComboBox.getSelectionModel().select(proband);
			} else {
				individualsComboBox.getSelectionModel().clearSelection();
			}

			update();

			showLocations();
		});
	}

	private void update() {
		Platform.runLater(() -> {
			setEnabled(gedcom != null && gedcom.isLoaded());
			individualsComboBox.setDisable(radioButtons.getSelectedToggle() == locationsRadioButton || radioButtons.getSelectedToggle() == animateFactsRadioButton);
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
				js.call("showLineage", JSONUtils.toJSON(proband.getLineage(Preferences.getLineageMode()).stream().map(Quintet::getValue1).toList()));
			}
		});
	}

	private void showAncestors() {
		Platform.runLater(() -> {
			if (js != null && gedcom != null && gedcom.isLoaded() && proband != null) {
				js.call("showAncestors", JSONUtils.toJSON(proband.getAncestorsList().stream().collect(Collectors.toMap(Quintet::getValue0, Quintet::getValue1))));
			}
		});
	}

	private void showDescendants() {
		Platform.runLater(() -> {
			if (js != null && gedcom != null && gedcom.isLoaded() && proband != null) {
				js.call("showDescendants", JSONUtils.toJSON(proband.getDescendantsList().stream().map(Quintet::getValue1).toList()));
			}
		});
	}

	private void animateFacts() {
		Platform.runLater(() -> {
			if (js != null && gedcom != null && gedcom.isLoaded()) {
				System.out.println(JSONUtils.toJSON(gedcom.getDatedFacts()));
				js.call("animateFacts", JSONUtils.toJSON(gedcom.getDatedFacts()));
			}
		});
	}

	@Override
	public int getRecordCount() {
		return locationCount;
	}
}
