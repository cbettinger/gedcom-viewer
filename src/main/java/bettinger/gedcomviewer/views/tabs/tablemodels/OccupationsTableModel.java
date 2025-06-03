package bettinger.gedcomviewer.views.tabs.tablemodels;

import java.time.LocalDateTime;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.model.Occupation;
import bettinger.gedcomviewer.views.TableModel;

public class OccupationsTableModel extends TableModel<Occupation> {

	public OccupationsTableModel(final GEDCOM gedcom, final String searchQuery) {
		super(new String[] { I18N.get("No"), I18N.get("Name"), I18N.get("Count"), I18N.get("LastChange") }, gedcom.getOccupations(searchQuery));
	}

	@Override
	public Object getValueAt(final int row, final int col) {
		final var occupation = getItemAt(row);
		return switch (col) {
			case 0 -> occupation.getNumber();
			case 1 -> occupation.getName();
			case 2 -> occupation.getCount();
			case 3 -> occupation.getLastChange();
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
