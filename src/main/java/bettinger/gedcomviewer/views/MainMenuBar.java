package bettinger.gedcomviewer.views;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.SystemInfo;
import com.google.common.eventbus.Subscribe;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.Events;
import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.Preferences;
import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.model.GEDCOM.GEDCOMEvent;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.model.LineageMode;
import bettinger.gedcomviewer.model.Record;
import bettinger.gedcomviewer.utils.FileUtils;
import bettinger.gedcomviewer.views.icons.MaterialIcons;
import jiconfont.swing.IconFontSwing;

class MainMenuBar extends JMenuBar implements ActionListener {

	private final JMenuItem recentFilesMenuItem;

	private final List<JMenuItem> enableOnFileIsLoadedItems;
	private final List<JMenuItem> enableOnRecordIsSelectedItems;
	private final List<JMenuItem> enableOnIndividualIsSelectedItems;

	private GEDCOM gedcom;
	private Record selectedRecord;

	@SuppressWarnings("java:S1192")
	MainMenuBar() {
		enableOnFileIsLoadedItems = new ArrayList<>();
		enableOnRecordIsSelectedItems = new ArrayList<>();
		enableOnIndividualIsSelectedItems = new ArrayList<>();

		gedcom = null;
		selectedRecord = null;

		final var fileMenu = new JMenu(I18N.get("File"));
		add(fileMenu);

		final var openFileMenuItem = new JMenuItem(String.format(Format.TRAILING_TRIPLE_DOT, I18N.get("Open")));
		openFileMenuItem.setIcon(IconFontSwing.buildIcon(MaterialIcons.FOLDER_OPEN, Constants.MENU_ICON_SIZE));
		openFileMenuItem.setActionCommand("OPEN_FILE");
		openFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		openFileMenuItem.addActionListener(this);
		fileMenu.add(openFileMenuItem);

		recentFilesMenuItem = new JMenu(I18N.get("RecentFiles"));
		recentFilesMenuItem.setIcon(IconFontSwing.buildIcon(MaterialIcons.REPLAY, Constants.MENU_ICON_SIZE));
		fileMenu.add(recentFilesMenuItem);
		updateRecentFiles();

		final var reloadFileMenuItem = new JMenuItem(I18N.get("Reload"));
		reloadFileMenuItem.setIcon(IconFontSwing.buildIcon(MaterialIcons.REFRESH, Constants.MENU_ICON_SIZE));
		reloadFileMenuItem.setActionCommand("RELOAD_FILE");
		reloadFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		reloadFileMenuItem.addActionListener(this);
		fileMenu.add(reloadFileMenuItem);
		enableOnFileIsLoadedItems.add(reloadFileMenuItem);

		final var propertiesFileMenuItem = new JMenuItem(String.format(Format.TRAILING_TRIPLE_DOT, I18N.get("Properties")));
		propertiesFileMenuItem.setIcon(IconFontSwing.buildIcon(MaterialIcons.DESCRIPTION, Constants.MENU_ICON_SIZE));
		propertiesFileMenuItem.setActionCommand("SHOW_PROPERTIES");
		propertiesFileMenuItem.addActionListener(this);
		fileMenu.add(propertiesFileMenuItem);
		enableOnFileIsLoadedItems.add(propertiesFileMenuItem);

		fileMenu.addSeparator();

		final var saveCopyAsMenuItem = new JMenuItem(String.format(Format.TRAILING_TRIPLE_DOT, I18N.get("SaveCopyAs")));
		saveCopyAsMenuItem.setIcon(IconFontSwing.buildIcon(MaterialIcons.SAVE_ALT, Constants.MENU_ICON_SIZE));
		saveCopyAsMenuItem.setActionCommand("SAVE_COPY_AS");
		saveCopyAsMenuItem.addActionListener(this);
		fileMenu.add(saveCopyAsMenuItem);
		enableOnFileIsLoadedItems.add(saveCopyAsMenuItem);

		fileMenu.addSeparator();

		final var closeFileMenuItem = new JMenuItem(I18N.get("Close"));
		closeFileMenuItem.setIcon(IconFontSwing.buildIcon(MaterialIcons.CLOSE, Constants.MENU_ICON_SIZE));
		closeFileMenuItem.setActionCommand("CLOSE_FILE");
		closeFileMenuItem.addActionListener(this);
		fileMenu.add(closeFileMenuItem);
		enableOnFileIsLoadedItems.add(closeFileMenuItem);

		final var quitMenuItem = new JMenuItem(I18N.get("Quit"));
		quitMenuItem.setIcon(IconFontSwing.buildIcon(MaterialIcons.EXIT_TO_APP, Constants.MENU_ICON_SIZE));
		quitMenuItem.setActionCommand("QUIT");
		quitMenuItem.addActionListener(this);
		fileMenu.add(quitMenuItem);

		final var editMenu = new JMenu(I18N.get("Edit"));
		add(editMenu);

		final var searchMenuItem = new JMenuItem(String.format(Format.TRAILING_TRIPLE_DOT, I18N.get("Search")));
		searchMenuItem.setIcon(IconFontSwing.buildIcon(MaterialIcons.SEARCH, Constants.MENU_ICON_SIZE));
		searchMenuItem.setActionCommand("SEARCH");
		searchMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		searchMenuItem.addActionListener(this);
		editMenu.add(searchMenuItem);
		enableOnFileIsLoadedItems.add(searchMenuItem);

		editMenu.addSeparator();

		final var cutFileMenuItem = new JMenuItem(I18N.get("Cut"));
		cutFileMenuItem.setIcon(IconFontSwing.buildIcon(MaterialIcons.CONTENT_CUT, Constants.MENU_ICON_SIZE));
		cutFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		cutFileMenuItem.addActionListener(new DefaultEditorKit.CutAction());
		editMenu.add(cutFileMenuItem);
		enableOnFileIsLoadedItems.add(cutFileMenuItem);

		final var copyFileMenuItem = new JMenuItem(I18N.get("Copy"));
		copyFileMenuItem.setIcon(IconFontSwing.buildIcon(MaterialIcons.CONTENT_COPY, Constants.MENU_ICON_SIZE));
		copyFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		copyFileMenuItem.addActionListener(new DefaultEditorKit.CopyAction());
		editMenu.add(copyFileMenuItem);
		enableOnFileIsLoadedItems.add(copyFileMenuItem);

		final var pasteFileMenuItem = new JMenuItem(I18N.get("Paste"));
		pasteFileMenuItem.setIcon(IconFontSwing.buildIcon(MaterialIcons.CONTENT_PASTE, Constants.MENU_ICON_SIZE));
		pasteFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		pasteFileMenuItem.addActionListener(new DefaultEditorKit.PasteAction());
		editMenu.add(pasteFileMenuItem);
		enableOnFileIsLoadedItems.add(pasteFileMenuItem);

		final var visualizationMenu = new JMenu(I18N.get("Visualization"));
		add(visualizationMenu);

		final var visualizeLineageMenuItem = new JMenuItem(String.format(Format.TRAILING_TRIPLE_DOT, I18N.get("Lineage")));
		visualizeLineageMenuItem.setIcon(new FlatSVGIcon("icons/lineage.svg", Constants.MENU_ICON_SIZE, Constants.MENU_ICON_SIZE));
		visualizeLineageMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		visualizeLineageMenuItem.setActionCommand("VISUALIZE_LINEAGE");
		visualizeLineageMenuItem.addActionListener(this);
		visualizationMenu.add(visualizeLineageMenuItem);
		enableOnIndividualIsSelectedItems.add(visualizeLineageMenuItem);

		final var visualizeAncestorsMenuItem = new JMenuItem(String.format(Format.TRAILING_TRIPLE_DOT, I18N.get("AncestorsList")));
		visualizeAncestorsMenuItem.setIcon(new FlatSVGIcon("icons/ancestors.svg", Constants.MENU_ICON_SIZE, Constants.MENU_ICON_SIZE));
		visualizeAncestorsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		visualizeAncestorsMenuItem.setActionCommand("VISUALIZE_ANCESTORS");
		visualizeAncestorsMenuItem.addActionListener(this);
		visualizationMenu.add(visualizeAncestorsMenuItem);
		enableOnIndividualIsSelectedItems.add(visualizeAncestorsMenuItem);

		final var visualizeDescendantsMenuItem = new JMenuItem(String.format(Format.TRAILING_TRIPLE_DOT, I18N.get("DescendantsList")));
		visualizeDescendantsMenuItem.setIcon(new FlatSVGIcon("icons/descendants.svg", Constants.MENU_ICON_SIZE, Constants.MENU_ICON_SIZE));
		visualizeDescendantsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		visualizeDescendantsMenuItem.setActionCommand("VISUALIZE_DESCENDANTS");
		visualizeDescendantsMenuItem.addActionListener(this);
		visualizationMenu.add(visualizeDescendantsMenuItem);
		enableOnIndividualIsSelectedItems.add(visualizeDescendantsMenuItem);

		final var visualizeConsanguinsMenuItem = new JMenuItem(String.format(Format.TRAILING_TRIPLE_DOT, I18N.get("ConsanguinsList")));
		visualizeConsanguinsMenuItem.setIcon(new FlatSVGIcon("icons/consanguins.svg", Constants.MENU_ICON_SIZE, Constants.MENU_ICON_SIZE));
		visualizeConsanguinsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		visualizeConsanguinsMenuItem.setActionCommand("VISUALIZE_CONSANGUINS");
		visualizeConsanguinsMenuItem.addActionListener(this);
		visualizationMenu.add(visualizeConsanguinsMenuItem);
		enableOnIndividualIsSelectedItems.add(visualizeConsanguinsMenuItem);

		final var exportMenu = new JMenu(I18N.get("Export"));
		add(exportMenu);

		final var exportRecordMenuItem = new JMenuItem(String.format(Format.TRAILING_TRIPLE_DOT, I18N.get("Record")));
		exportRecordMenuItem.setIcon(new FlatSVGIcon("icons/record.svg", Constants.MENU_ICON_SIZE, Constants.MENU_ICON_SIZE));
		exportRecordMenuItem.setActionCommand("EXPORT_RECORD");
		exportRecordMenuItem.addActionListener(this);
		exportMenu.add(exportRecordMenuItem);
		enableOnRecordIsSelectedItems.add(exportRecordMenuItem);

		final var exportLineageMenuItem = new JMenuItem(String.format(Format.TRAILING_TRIPLE_DOT, I18N.get("Lineage")));
		exportLineageMenuItem.setIcon(new FlatSVGIcon("icons/lineage.svg", Constants.MENU_ICON_SIZE, Constants.MENU_ICON_SIZE));
		exportLineageMenuItem.setActionCommand("EXPORT_LINEAGE");
		exportLineageMenuItem.addActionListener(this);
		exportMenu.add(exportLineageMenuItem);
		enableOnIndividualIsSelectedItems.add(exportLineageMenuItem);

		final var exportAncestorsMenuItem = new JMenuItem(String.format(Format.TRAILING_TRIPLE_DOT, I18N.get("AncestorsList")));
		exportAncestorsMenuItem.setIcon(new FlatSVGIcon("icons/ancestors.svg", Constants.MENU_ICON_SIZE, Constants.MENU_ICON_SIZE));
		exportAncestorsMenuItem.setActionCommand("EXPORT_ANCESTORS");
		exportAncestorsMenuItem.addActionListener(this);
		exportMenu.add(exportAncestorsMenuItem);
		enableOnIndividualIsSelectedItems.add(exportAncestorsMenuItem);

		final var exportDescendantsMenuItem = new JMenuItem(String.format(Format.TRAILING_TRIPLE_DOT, I18N.get("DescendantsList")));
		exportDescendantsMenuItem.setIcon(new FlatSVGIcon("icons/descendants.svg", Constants.MENU_ICON_SIZE, Constants.MENU_ICON_SIZE));
		exportDescendantsMenuItem.setActionCommand("EXPORT_DESCENDANTS");
		exportDescendantsMenuItem.addActionListener(this);
		exportMenu.add(exportDescendantsMenuItem);
		enableOnIndividualIsSelectedItems.add(exportDescendantsMenuItem);

		final var toolsMenu = new JMenu(I18N.get("Tools"));
		add(toolsMenu);

		final var facialFeatureAnalysisMenuItem = new JMenuItem(String.format(Format.TRAILING_TRIPLE_DOT, I18N.get("FacialFeatureAnalysis")));
		facialFeatureAnalysisMenuItem.setIcon(IconFontSwing.buildIcon(MaterialIcons.PSYCHOLOGY_ALT, Constants.MENU_ICON_SIZE));
		facialFeatureAnalysisMenuItem.setActionCommand("FACIAL_FEATURE_ANALYSIS");
		facialFeatureAnalysisMenuItem.addActionListener(this);
		toolsMenu.add(facialFeatureAnalysisMenuItem);
		enableOnIndividualIsSelectedItems.add(facialFeatureAnalysisMenuItem);

		final var viewMenu = new JMenu(I18N.get("View"));
		add(viewMenu);

		final var languages = I18N.getSupportedLocales();
		if (languages.size() > 1) {
			final var languageMenu = new JMenu(I18N.get("Language"));
			languageMenu.setIcon(IconFontSwing.buildIcon(MaterialIcons.LANGUAGE, Constants.MENU_ICON_SIZE));
			viewMenu.add(languageMenu);

			for (final var language : languages.entrySet()) {
				final var localeMenuItem = new JRadioButtonMenuItem(language.getValue());
				localeMenuItem.setIcon(I18N.getLocaleIcon(language.getKey(), Constants.MENU_ICON_SIZE));
				localeMenuItem.addActionListener(x -> I18N.setCurrentLocale(language.getKey()));
				languageMenu.add(localeMenuItem);
			}
		}

		final var lineageMenu = new JMenu(I18N.get("Lineage"));
		lineageMenu.setIcon(IconFontSwing.buildIcon(MaterialIcons.ALT_ROUTE, Constants.MENU_ICON_SIZE));
		viewMenu.add(lineageMenu);

		final var lineageGroup = new ButtonGroup();
		final var lineageMode = Preferences.getLineageMode();

		final var nameLineCheckbox = new JRadioButtonMenuItem(I18N.get("NameLine"), lineageMode == LineageMode.NAME_LINE);
		nameLineCheckbox.addActionListener(x -> Preferences.setLineageMode(LineageMode.NAME_LINE));
		lineageGroup.add(nameLineCheckbox);
		lineageMenu.add(nameLineCheckbox);

		final var maleLineCheckbox = new JRadioButtonMenuItem(I18N.get("MaleLine"), lineageMode == LineageMode.MALE_LINE);
		maleLineCheckbox.addActionListener(x -> Preferences.setLineageMode(LineageMode.MALE_LINE));
		lineageGroup.add(maleLineCheckbox);
		lineageMenu.add(maleLineCheckbox);

		if (!SystemInfo.isMacOS) {
			final var helpMenu = new JMenu(I18N.get("Help"));
			add(helpMenu);

			final var aboutMenuItem = new JMenuItem(String.format(Format.TRAILING_TRIPLE_DOT, I18N.get("About")));
			aboutMenuItem.setIcon(IconFontSwing.buildIcon(MaterialIcons.INFO, Constants.MENU_ICON_SIZE));
			aboutMenuItem.setActionCommand("SHOW_ABOUT");
			aboutMenuItem.addActionListener(this);
			helpMenu.add(aboutMenuItem);
		}

		Events.register(new Object() {

			@Subscribe
			void onGedcomEvent(final GEDCOMEvent event) {
				updateRecentFiles();

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

	private void updateRecentFiles() {
		recentFilesMenuItem.removeAll();

		final var recentFiles = Preferences.getRecentFiles();
		for (final var recentFile : recentFiles) {
			final var recentFileMenuItem = new JMenuItem(FileUtils.getFileName(recentFile));
			recentFileMenuItem.addActionListener(x -> Events.post(new UI.LoadFileCommand(recentFile)));
			recentFilesMenuItem.add(recentFileMenuItem);
		}

		recentFilesMenuItem.setEnabled(!recentFiles.isEmpty());
	}

	private void updateMenuItems() {
		for (final var item : enableOnFileIsLoadedItems) {
			item.setEnabled(gedcom != null && gedcom.isLoaded());
		}

		for (final var item : enableOnRecordIsSelectedItems) {
			item.setEnabled(gedcom != null && gedcom.isLoaded() && selectedRecord != null);
		}

		for (final var item : enableOnIndividualIsSelectedItems) {
			item.setEnabled(gedcom != null && gedcom.isLoaded() && selectedRecord instanceof Individual);
		}
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		Events.post(event);
	}
}
