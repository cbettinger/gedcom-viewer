package bettinger.gedcomviewer.views.tabs;

import java.awt.BorderLayout;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.google.common.eventbus.Subscribe;

import bettinger.gedcomviewer.Events;
import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.model.GEDCOM.GEDCOMEvent;
import bettinger.gedcomviewer.model.Record;
import bettinger.gedcomviewer.views.AutoFitTable;
import bettinger.gedcomviewer.views.HTMLTextPane;
import bettinger.gedcomviewer.views.TableModel;
import bettinger.gedcomviewer.views.UI;

public abstract class TableContainer extends JPanel implements ListSelectionListener, IRecordCollectionView {

	private final Class<?> tableModelClass;

	protected final AutoFitTable table;
	protected final HTMLTextPane detailsSideBar;

	private String searchQuery;
	private TableModel<Record> tableModel;

	protected TableContainer(final Class<?> tableModelClass) {
		setLayout(new BorderLayout());

		this.tableModelClass = tableModelClass;

		this.table = new AutoFitTable();
		this.table.setAutoCreateRowSorter(true);
		this.table.getSelectionModel().addListSelectionListener(this);

		this.detailsSideBar = new HTMLTextPane();

		this.searchQuery = "";

		Events.register(new Object() {

			@Subscribe
			void onGedcomEvent(final GEDCOMEvent event) {
				update(event.getGEDCOM(), searchQuery);
			}

			@Subscribe
			void onSearchCommand(final UI.SearchCommand event) {
				update(event.getGEDCOM(), event.getQuery());
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void update(final GEDCOM gedcom, final String searchQuery) {
		this.searchQuery = searchQuery;

		try {
			tableModel = (TableModel<Record>) tableModelClass.getConstructor(GEDCOM.class, String.class).newInstance(gedcom, searchQuery);
			table.setModel(tableModel);

			detailsSideBar.clear();
		} catch (final Exception e) {
			Logger.getLogger(TableContainer.class.getName()).log(Level.SEVERE, "Failed to update table", e);
		}
	}

	public void clearSelection() {
		table.clearSelection();
	}

	public void selectItem(final Record item) {
		final var rowIndex = tableModel.getIndexOf(item);
		if (rowIndex >= 0) {
			table.selectRow(rowIndex);
		}
	}

	@Override
	public void valueChanged(final ListSelectionEvent event) {
		final var selectedItem = getSelectedItem();
		if (selectedItem == null) {
			detailsSideBar.clear();
			Events.post(new UI.RecordSelectedEvent(null));
		} else if (!event.getValueIsAdjusting()) {
			detailsSideBar.setHTML(selectedItem.toHTML(), searchQuery);
			Events.post(new UI.RecordSelectedEvent(selectedItem));
		}
	}

	private Record getSelectedItem() {
		final var selectedRowIndex = table.getSelectedRow();
		return selectedRowIndex == -1 ? null : tableModel.getItemAt(table.convertRowIndexToModel(selectedRowIndex));
	}

	@Override
	public int getRecordCount() {
		return tableModel != null ? tableModel.getRowCount() : 0;
	}
}
