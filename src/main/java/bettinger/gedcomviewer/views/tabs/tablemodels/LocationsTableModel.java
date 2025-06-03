package bettinger.gedcomviewer.views.tabs.tablemodels;

import java.time.LocalDateTime;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.model.Location;
import bettinger.gedcomviewer.views.TableModel;

public class LocationsTableModel extends TableModel<Location> {

	public LocationsTableModel(final GEDCOM gedcom, final String searchQuery) {
		super(new String[] { I18N.get("No"), I18N.get("Name"), I18N.get("Latitude"), I18N.get("Longitude"), I18N.get("Media"), I18N.get("Notes"), I18N.get("References"), I18N.get("LastChange") }, gedcom.getLocations(searchQuery));
	}

	@Override
	public Object getValueAt(final int row, final int col) {
		final var location = getItemAt(row);
		return switch (col) {
			case 0 -> location.getNumber();
			case 1 -> location.getName();
			case 2 -> location.getLatitude();
			case 3 -> location.getLongitude();
			case 4 -> location.getMedia().size();
			case 5 -> location.getNotes().size();
			case 6 -> location.getReferences().size();
			case 7 -> location.getLastChange();
			default -> "";
		};
	}

	@Override
	public Class<?> getColumnClass(final int column) {
		return switch (column) {
			case 0, 2, 3, 4, 5, 6 -> Integer.class;
			case 7 -> LocalDateTime.class;
			default -> super.getColumnClass(column);
		};
	}
}
