package bettinger.gedcomviewer.views.tabs.tablemodels;

import java.io.File;
import java.time.LocalDateTime;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.model.Media;
import bettinger.gedcomviewer.views.TableModel;

public class MediaTableModel extends TableModel<Media> {

	public MediaTableModel(final GEDCOM gedcom, final String searchQuery) {
		super(new String[] { I18N.get("No"), I18N.get("Title"), I18N.get("Format"), I18N.get("File"), I18N.get("Type"), I18N.get("Notes"), I18N.get("References"), I18N.get("LastChange") }, gedcom.getMedia(searchQuery));
	}

	@Override
	public Object getValueAt(int row, int col) {
		final var media = getItemAt(row);
		final var type = media.getType();

		return switch (col) {
			case 0 -> media.getNumber();
			case 1 -> media.getTitle();
			case 2 -> media.getFormat();
			case 3 -> media.getFile();
			case 4 -> type == null ? "" : I18N.get(type.toString());
			case 5 -> media.getNotes().size();
			case 6 -> media.getReferences().size();
			case 7 -> media.getLastChange();
			default -> "";
		};
	}

	@Override
	public Class<?> getColumnClass(int column) {
		return switch (column) {
			case 0, 5, 6 -> Integer.class;
			case 3 -> File.class;
			case 7 -> LocalDateTime.class;
			default -> super.getColumnClass(column);
		};
	}
}
