package bettinger.gedcomviewer.views;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.time.LocalDateTime;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import bettinger.gedcomviewer.model.Date;
import bettinger.gedcomviewer.utils.DateTimeUtils;
import bettinger.gedcomviewer.utils.FileUtils;

public class AutoFitTable extends JTable {

	protected static final Color TRANSPARENT = new Color(0, 0, 0, 0);

	protected static final Color SELECTION_BACKGROUND = UIManager.getColor("List.selectionBackground");
	protected static final Color SELECTION_FOREGROUND = UIManager.getColor("List.selectionForeground");

	protected static final Color GREEN = new Color(119, 221, 119);
	protected static final Color YELLOW = new Color(253, 253, 150);
	protected static final Color RED = new Color(255, 105, 97);

	private static final int COLUMN_WIDTH_PADDING = 15;

	private static final int MAX_COLUMN_WIDTH_SMALL = 220;
	private static final int MAX_COLUMN_WIDTH_LARGE = 1000;

	private static final int ALIGNMENT = SwingConstants.LEFT;

	private static final TableCellRenderer fileRenderer = new DefaultTableCellRenderer() {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			final var component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			if (value instanceof File file && component instanceof JLabel label) {
				label.setText(FileUtils.getFileName(file));
				label.setForeground(Color.BLACK);
				label.setBackground(file.exists() && !file.isDirectory() ? TRANSPARENT : RED);

				Color backgroundColor = TRANSPARENT;
				Color foregroundColor = Color.BLACK;

				if (!file.exists() || file.isDirectory()) {
					backgroundColor = RED;
				} else if (isSelected) {
					backgroundColor = SELECTION_BACKGROUND;
					foregroundColor = SELECTION_FOREGROUND;
				}

				label.setBackground(backgroundColor);
				label.setForeground(foregroundColor);
			}

			return component;
		}
	};

	private static final TableCellRenderer gedcomDateRenderer = new DefaultTableCellRenderer() {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (value instanceof File gedcomDate) {
				value = gedcomDate.toString();
			}
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	};

	private static final TableCellRenderer dateTimeRenderer = new DefaultTableCellRenderer() {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (value instanceof LocalDateTime timestamp) {
				value = DateTimeUtils.format(timestamp);
			}
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	};

	public AutoFitTable() {
		this(null);
	}

	public AutoFitTable(final TableModel dataModel) {
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getTableHeader().setReorderingAllowed(false);

		setDefaultRenderer(File.class, fileRenderer);
		setDefaultRenderer(Date.class, gedcomDateRenderer);
		setDefaultRenderer(LocalDateTime.class, dateTimeRenderer);
		((DefaultTableCellRenderer) getTableHeader().getDefaultRenderer()).setHorizontalAlignment(ALIGNMENT);

		setDefaultEditor(Object.class, null);

		if (dataModel != null) {
			setModel(dataModel);
		}
	}

	@Override
	public void setModel(final TableModel dataModel) {
		super.setModel(dataModel == null ? new DefaultTableModel() : dataModel);

		if (dataModel != null) {
			dataModel.addTableModelListener(_ -> {
				resizeColumnWidth();
				setPreferredScrollableViewportSize(getPreferredSize());
			});
		}

		resizeColumnWidth();
		setPreferredScrollableViewportSize(getPreferredSize());
	}

	private void resizeColumnWidth() {
		final var columnModel = getColumnModel();

		final var columnCount = getColumnCount();
		final var maxColumWidth = (columnCount <= 5 ? MAX_COLUMN_WIDTH_LARGE : MAX_COLUMN_WIDTH_SMALL);

		for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
			final var column = columnModel.getColumn(columnIndex);

			var headerRenderer = column.getHeaderRenderer();
			if (headerRenderer == null) {
				headerRenderer = getTableHeader().getDefaultRenderer();
			}
			double width = headerRenderer.getTableCellRendererComponent(this, column.getHeaderValue(), false, false, -1, column.getModelIndex()).getPreferredSize().width;

			for (int row = 0, m = getRowCount(); row < m; row++) {
				final var renderer = getCellRenderer(row, columnIndex);
				final var component = prepareRenderer(renderer, row, columnIndex);
				width = Math.max(component.getPreferredSize().width + 1.0, width);
			}

			width += COLUMN_WIDTH_PADDING;

			if (width > maxColumWidth) {
				width = maxColumWidth;
			}

			columnModel.getColumn(columnIndex).setPreferredWidth((int) width);
		}
	}

	@Override
	public Component prepareRenderer(final TableCellRenderer renderer, final int row, final int col) {
		final var component = super.prepareRenderer(renderer, row, col);
		((JLabel) component).setHorizontalAlignment(ALIGNMENT);
		return component;
	}

	public void selectRow(final int rowIndex) {
		final var viewIndex = convertRowIndexToView(rowIndex);
		setRowSelectionInterval(viewIndex, viewIndex);
		scrollRectToVisible(getCellRect(viewIndex, 0, true));
	}
}
