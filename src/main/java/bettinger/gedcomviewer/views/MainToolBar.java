package bettinger.gedcomviewer.views;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.google.common.eventbus.Subscribe;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.Events;
import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.model.Record;
import bettinger.gedcomviewer.model.GEDCOM.GEDCOMEvent;
import bettinger.gedcomviewer.views.icons.MaterialIcons;
import jiconfont.swing.IconFontSwing;

class MainToolBar extends ToolBar {

	private final List<JButton> enableOnFileIsLoadedItems;
	private final List<JButton> enableOnIndividualIsSelectedItems;

	private GEDCOM gedcom;
	private Record selectedRecord;

	MainToolBar() {
		enableOnFileIsLoadedItems = new ArrayList<>();
		enableOnIndividualIsSelectedItems = new ArrayList<>();

		gedcom = null;
		selectedRecord = null;

		final var openFileButton = new JButton(IconFontSwing.buildIcon(MaterialIcons.FOLDER_OPEN, Constants.TOOLBAR_ICON_SIZE));
		openFileButton.setToolTipText(String.format(Format.TRAILING_TRIPLE_DOT, I18N.get("Open")));
		openFileButton.setActionCommand("OPEN_FILE");
		openFileButton.addActionListener(this);
		add(openFileButton);

		final var reloadFileButton = new JButton(IconFontSwing.buildIcon(MaterialIcons.REFRESH, Constants.TOOLBAR_ICON_SIZE));
		reloadFileButton.setToolTipText(I18N.get("Reload"));
		reloadFileButton.setActionCommand("RELOAD_FILE");
		reloadFileButton.addActionListener(this);
		add(reloadFileButton);
		enableOnFileIsLoadedItems.add(reloadFileButton);

		addSeparator();

		final var visualizationButtonPrefix = I18N.get("Visualization");

		final var visualizeLineageButton = new JButton(new FlatSVGIcon("icons/lineage.svg", Constants.TOOLBAR_ICON_SIZE, Constants.TOOLBAR_ICON_SIZE));
		visualizeLineageButton.setToolTipText(String.format(Format.KEY_VALUE_TRAILING_TRIPLE_DOT, visualizationButtonPrefix, I18N.get("Lineage")));
		visualizeLineageButton.setActionCommand("VISUALIZE_LINEAGE");
		visualizeLineageButton.addActionListener(this);
		add(visualizeLineageButton);
		enableOnIndividualIsSelectedItems.add(visualizeLineageButton);

		final var visualizeAncestorsButton = new JButton(new FlatSVGIcon("icons/ancestors.svg", Constants.TOOLBAR_ICON_SIZE, Constants.TOOLBAR_ICON_SIZE));
		visualizeAncestorsButton.setToolTipText(String.format(Format.KEY_VALUE_TRAILING_TRIPLE_DOT, visualizationButtonPrefix, I18N.get("AncestorsList")));
		visualizeAncestorsButton.setActionCommand("VISUALIZE_ANCESTORS");
		visualizeAncestorsButton.addActionListener(this);
		add(visualizeAncestorsButton);
		enableOnIndividualIsSelectedItems.add(visualizeAncestorsButton);

		final var visualizeDescendantsButton = new JButton(new FlatSVGIcon("icons/descendants.svg", Constants.TOOLBAR_ICON_SIZE, Constants.TOOLBAR_ICON_SIZE));
		visualizeDescendantsButton.setToolTipText(String.format(Format.KEY_VALUE_TRAILING_TRIPLE_DOT, visualizationButtonPrefix, I18N.get("DescendantsList")));
		visualizeDescendantsButton.setActionCommand("VISUALIZE_DESCENDANTS");
		visualizeDescendantsButton.addActionListener(this);
		add(visualizeDescendantsButton);
		enableOnIndividualIsSelectedItems.add(visualizeDescendantsButton);

		final var visualizeConsanguinsButton = new JButton(new FlatSVGIcon("icons/consanguins.svg", Constants.TOOLBAR_ICON_SIZE, Constants.TOOLBAR_ICON_SIZE));
		visualizeConsanguinsButton.setToolTipText(String.format(Format.KEY_VALUE_TRAILING_TRIPLE_DOT, visualizationButtonPrefix, I18N.get("ConsanguinsList")));
		visualizeConsanguinsButton.setActionCommand("VISUALIZE_CONSANGUINS");
		visualizeConsanguinsButton.addActionListener(this);
		add(visualizeConsanguinsButton);
		enableOnIndividualIsSelectedItems.add(visualizeConsanguinsButton);

		Events.register(new Object() {

			@Subscribe
			void onGedcomEvent(final GEDCOMEvent event) {
				gedcom = event.getGEDCOM();
				updateMenuItems();
			}

			@Subscribe
			void onTabSelectedEvent(final UI.TabSelectedEvent event) {
				gedcom = event.getGEDCOM();
				updateMenuItems();
			}

			@Subscribe
			void onRecordSelectedEvent(final UI.RecordSelectedEvent event) {
				selectedRecord = event.getRecord();
				updateMenuItems();
			}
		});
	}

	private void updateMenuItems() {
		for (final var item : enableOnFileIsLoadedItems) {
			item.setEnabled(gedcom != null && gedcom.isLoaded());
		}

		for (final var item : enableOnIndividualIsSelectedItems) {
			item.setEnabled(gedcom != null && gedcom.isLoaded() && selectedRecord instanceof Individual);
		}
	}
}
