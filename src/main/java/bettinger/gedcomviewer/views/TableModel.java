package bettinger.gedcomviewer.views;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public abstract class TableModel<T> extends AbstractTableModel {

	private final String[] columnNames;
	private final List<T> items;

	protected TableModel(final String[] columnNames, final List<T> items) {
		this.columnNames = columnNames;
		this.items = items;
	}

	public List<T> getItems() {
		return items;
	}

	public T getItemAt(final int row) {
		return items.get(row);
	}

	public int getIndexOf(final T item) {
		return items.indexOf(item);
	}

	@Override
	public int getRowCount() {
		return items.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(final int col) {
		return columnNames[col];
	}

	@Override
	public Class<?> getColumnClass(final int column) {
		return String.class;
	}
}
