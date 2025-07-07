package bettinger.gedcomviewer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.JComponent;

import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.model.LineageMode;
import bettinger.gedcomviewer.utils.FileUtils;
import bettinger.gedcomviewer.views.tabs.map.MapPanel.View;

public abstract class Preferences {

	private static final String FILENAME = "bettinger/gedcomviewer";

	private static final String LIST_DELIMITER = ";";

	private static final String RECENT_FILES_KEY = "recentFiles";
	private static final int RECENT_FILES_SIZE = 5;

	private static final String LOCALE_KEY = "locale";
	private static final String NAVIGATION_TAB_KEY = "navigationTab";
	private static final String PROBAND_KEY = "proband";
	private static final String LINEAGE_MODE_KEY = "lineageMode";
	private static final String GENERATIONS_KEY_PREFIX = "generations";
	private static final String MAP_PANEL_VIEW_KEY = "mapPanelView";
	private static final String MAP_PANEL_PATHS_KEY = "mapPanelPaths";

	public static void storeRecentFile(final File file) {
		if (file != null) {
			final var recentFilePaths = getRecentFilePaths();

			final var filePath = FileUtils.getPath(file);
			final var index = recentFilePaths.indexOf(filePath);
			if (index != -1) {
				recentFilePaths.remove(index);
			}

			recentFilePaths.add(0, filePath);

			while (recentFilePaths.size() > RECENT_FILES_SIZE) {
				recentFilePaths.remove(recentFilePaths.size() - 1);
			}

			java.util.prefs.Preferences.userRoot().node(FILENAME).put(RECENT_FILES_KEY, String.join(LIST_DELIMITER, recentFilePaths));
		}
	}

	public static List<File> getRecentFiles() {
		return getRecentFilePaths().stream().map(FileUtils::getFile).filter(FileUtils::exists).toList();
	}

	private static List<String> getRecentFilePaths() {
		final var value = java.util.prefs.Preferences.userRoot().node(FILENAME).get(RECENT_FILES_KEY, "");
		return new ArrayList<>(value.isEmpty() ? new ArrayList<>() : Arrays.asList(value.split(LIST_DELIMITER)));
	}

	public static void setLocale(final Locale locale) {
		if (locale != null) {
			java.util.prefs.Preferences.userRoot().node(FILENAME).put(LOCALE_KEY, locale.getLanguage());
		}

		Events.post(new LocaleChangedEvent(locale));
	}

	public static Locale getLocale() {
		return Locale.forLanguageTag(getLanguageTag());
	}

	public static String getLanguageTag() {
		return java.util.prefs.Preferences.userRoot().node(FILENAME).get(LOCALE_KEY, Locale.getDefault().getLanguage());
	}

	public static void setNavigationTab(final int index) {
		if (index >= 0) {
			java.util.prefs.Preferences.userRoot().node(FILENAME).put(NAVIGATION_TAB_KEY, String.valueOf(index));
		}
	}

	public static int getNavigationTab() {
		return Integer.valueOf(java.util.prefs.Preferences.userRoot().node(FILENAME).get(NAVIGATION_TAB_KEY, "0"));
	}

	public static void setProband(final Individual proband) {
		if (proband != null) {
			java.util.prefs.Preferences.userRoot().node(FILENAME).put(PROBAND_KEY, proband.getId());
		}

		Events.post(new ProbandChangedEvent(proband));
	}

	public static Individual getProband(final GEDCOM gedcom) {
		final var id = java.util.prefs.Preferences.userRoot().node(FILENAME).get(PROBAND_KEY, null);
		return gedcom != null && id != null ? (Individual) gedcom.getRecord(id) : null;
	}

	public static void setLineageMode(final LineageMode mode) {
		if (mode != null) {
			java.util.prefs.Preferences.userRoot().node(FILENAME).put(LINEAGE_MODE_KEY, mode.toString());
		}

		Events.post(new LineageModeChangedEvent(mode));
	}

	public static LineageMode getLineageMode() {
		return LineageMode.valueOf(java.util.prefs.Preferences.userRoot().node(FILENAME).get(LINEAGE_MODE_KEY, LineageMode.NAME_LINE.toString()));
	}

	public static void setGenerations(final JComponent component, final int number) {
		if (component != null && number >= 0) {
			java.util.prefs.Preferences.userRoot().node(FILENAME).put(String.format(Format.DOT_SEPARATED, GENERATIONS_KEY_PREFIX, component.getClass().getSimpleName()), String.valueOf(number));
			Events.post(new GenerationsChangedEvent(component, number));
		}
	}

	public static int getGenerations(final JComponent component) {
		return Integer.valueOf(java.util.prefs.Preferences.userRoot().node(FILENAME).get(String.format(Format.DOT_SEPARATED, GENERATIONS_KEY_PREFIX, component.getClass().getSimpleName()), "0"));
	}

	public static void setMapPanelView(final View view) {
		if (view != null) {
			java.util.prefs.Preferences.userRoot().node(FILENAME).put(MAP_PANEL_VIEW_KEY, view.name());
		}
	}

	public static View getMapPanelView() {
		final var view = java.util.prefs.Preferences.userRoot().node(FILENAME).get(MAP_PANEL_VIEW_KEY, null);
		return view != null ? View.valueOf(view) : null;
	}

	public static void setMapPanelPathsOption(final boolean value) {
		java.util.prefs.Preferences.userRoot().node(FILENAME).put(MAP_PANEL_PATHS_KEY, Boolean.toString(value));
	}

	public static boolean getMapPanelPathsOption() {
		return Boolean.valueOf(java.util.prefs.Preferences.userRoot().node(FILENAME).get(MAP_PANEL_PATHS_KEY, "false"));
	}

	private Preferences() {}

	public static class LocaleChangedEvent {
		private final Locale locale;

		public LocaleChangedEvent(final Locale locale) {
			this.locale = locale;
		}

		public Locale getLocale() {
			return locale;
		}
	}

	public static class ProbandChangedEvent {
		private final Individual proband;

		public ProbandChangedEvent(final Individual proband) {
			this.proband = proband;
		}

		public Individual getProband() {
			return proband;
		}
	}

	public static class LineageModeChangedEvent {
		private final LineageMode mode;

		public LineageModeChangedEvent(final LineageMode mode) {
			this.mode = mode;
		}

		public LineageMode getLineageMode() {
			return mode;
		}
	}

	public static class GenerationsChangedEvent {
		private final JComponent component;
		private final int number;

		public GenerationsChangedEvent(final JComponent component, final int number) {
			this.component = component;
			this.number = number;
		}

		public JComponent getComponent() {
			return component;
		}

		public int getNumber() {
			return number;
		}
	}
}
