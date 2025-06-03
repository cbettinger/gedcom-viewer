package bettinger.gedcomviewer.views.tabs.tablemodels;

import java.time.LocalDateTime;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.model.Source;
import bettinger.gedcomviewer.views.TableModel;

public class SourcesTableModel extends TableModel<Source> {

	public SourcesTableModel(final GEDCOM gedcom, final String searchQuery) {
		super(new String[] { I18N.get("No"), I18N.get("Abbreviation"), I18N.get("Author"), I18N.get("Title"), I18N.get("Repository"), I18N.get("Media"), I18N.get("Notes"), I18N.get("References"), I18N.get("LastChange") }, gedcom.getSources(searchQuery));
	}

	@Override
	public Object getValueAt(final int row, final int col) {
		final var source = getItemAt(row);
		return switch (col) {
			case 0 -> source.getNumber();
			case 1 -> source.getAbbreviation();
			case 2 -> source.getAuthor();
			case 3 -> source.getTitle();
			case 4 -> source.getRepository() != null && !source.getRepository().getName().isEmpty() ? source.getRepository().getName() : "";
			case 5 -> source.getMedia().size();
			case 6 -> source.getNotes().size();
			case 7 -> source.getReferences().size();
			case 8 -> source.getLastChange();
			default -> "";
		};
	}

	@Override
	public Class<?> getColumnClass(final int column) {
		return switch (column) {
			case 0, 5, 6, 7 -> Integer.class;
			case 8 -> LocalDateTime.class;
			default -> super.getColumnClass(column);
		};
	}
}
