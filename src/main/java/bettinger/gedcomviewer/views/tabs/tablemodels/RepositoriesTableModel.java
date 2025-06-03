package bettinger.gedcomviewer.views.tabs.tablemodels;

import java.time.LocalDateTime;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.model.Repository;
import bettinger.gedcomviewer.views.TableModel;

public class RepositoriesTableModel extends TableModel<Repository> {

	public RepositoriesTableModel(final GEDCOM gedcom, final String searchQuery) {
		super(new String[] { I18N.get("No"), I18N.get("Name"), I18N.get("Notes"), I18N.get("References"), I18N.get("LastChange") }, gedcom.getRepositories(searchQuery));
	}

	@Override
	public Object getValueAt(final int row, final int col) {
		final var repository = getItemAt(row);
		return switch (col) {
			case 0 -> repository.getNumber();
			case 1 -> repository.getName();
			case 2 -> repository.getNotes().size();
			case 3 -> repository.getReferences().size();
			case 4 -> repository.getLastChange();
			default -> "";
		};
	}

	@Override
	public Class<?> getColumnClass(final int column) {
		return switch (column) {
			case 0, 2, 3 -> Integer.class;
			case 4 -> LocalDateTime.class;
			default -> super.getColumnClass(column);
			};
	}
}
