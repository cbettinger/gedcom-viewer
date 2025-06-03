package bettinger.gedcomviewer.views;

import java.awt.Component;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JPanel;

import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.model.Record;

public interface UI {

	public static class TabSelectedEvent {
		private final Component tab;
		private final GEDCOM gedcom;

		public TabSelectedEvent(final Component tab, final GEDCOM gedcom) {
			this.tab = tab;
			this.gedcom = gedcom;
		}

		public Component getComponent() {
			return tab;
		}

		public GEDCOM getGEDCOM() {
			return gedcom;
		}
	}

	public static class RecordSelectedEvent {
		private final Record r;

		public RecordSelectedEvent(final Record r) {
			this.r = r;
		}

		public Record getRecord() {
			return r;
		}
	}

	public static class SelectRecordCommand {
		private final Record r;

		public SelectRecordCommand(final Record r) {
			this.r = r;
		}

		public Record getRecord() {
			return r;
		}
	}

	public static class LoadFileCommand {
		private final File file;

		public LoadFileCommand(final File file) {
			this.file = file;
		}

		public File getFile() {
			return file;
		}
	}

	public static class SearchCommand {
		private final GEDCOM gedcom;
		private final String query;

		public SearchCommand(final GEDCOM gedcom, final String query) {
			this.gedcom = gedcom;
			this.query = query;
		}

		public GEDCOM getGEDCOM() {
			return gedcom;
		}

		public String getQuery() {
			return query;
		}
	}

	public static JPanel wrap(final JComponent comp) {
		final var result = new JPanel();
		result.add(comp);
		return result;
	}
}
