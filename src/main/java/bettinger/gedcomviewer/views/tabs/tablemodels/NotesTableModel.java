package bettinger.gedcomviewer.views.tabs.tablemodels;

import java.time.LocalDateTime;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.model.Note;
import bettinger.gedcomviewer.views.TableModel;

public class NotesTableModel extends TableModel<Note> {

	public NotesTableModel(final GEDCOM gedcom, final String searchQuery) {
		super(new String[] { I18N.get("No"), I18N.get("Text"), I18N.get("References"), I18N.get("LastChange") }, gedcom.getNotes(searchQuery));
	}

	@Override
	public Object getValueAt(final int row, final int col) {
		final var note = getItemAt(row);
		return switch (col) {
			case 0 -> note.getNumber();
			case 1 -> note.getText();
			case 2 -> note.getReferences().size();
			case 3 -> note.getLastChange();
			default -> "";
		};
	}

	@Override
	public Class<?> getColumnClass(final int column) {
		return switch (column) {
			case 0, 2 -> Integer.class;
			case 3 -> LocalDateTime.class;
			default -> super.getColumnClass(column);
		};
	}
}
