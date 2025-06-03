package bettinger.gedcomviewer.views.tabs.tablemodels;

import java.time.LocalDateTime;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Date;
import bettinger.gedcomviewer.model.Family;
import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.views.TableModel;

public class FamiliesTableModel extends TableModel<Family> {

	public FamiliesTableModel(final GEDCOM gedcom, final String searchQuery) {
		super(new String[] { I18N.get("No"), I18N.get("Husband"), I18N.get("Wife"), I18N.get("DateOfMarriage"), I18N.get("PlaceOfMarriage"), I18N.get("LastChange") }, gedcom.getFamilies(searchQuery));
	}

	@Override
	public Object getValueAt(final int row, final int col) {
		final var family = getItemAt(row);
		final var husband = family.getHusband();
		final var wife = family.getWife();

		return switch (col) {
			case 0 -> family.getNumber();
			case 1 -> husband == null ? "" : husband.getName();
			case 2 -> wife == null ? "" : wife.getName();
			case 3 -> family.getMarriageDate();
			case 4 -> family.getMarriagePlace();
			case 5 -> family.getLastChange();
			default -> "";
		};
	}

	@Override
	public Class<?> getColumnClass(final int column) {
		return switch (column) {
			case 0 -> Integer.class;
			case 3 -> Date.class;
			case 5 -> LocalDateTime.class;
			default -> super.getColumnClass(column);
		};
	}
}
