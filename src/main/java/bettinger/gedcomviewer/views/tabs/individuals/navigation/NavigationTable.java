package bettinger.gedcomviewer.views.tabs.individuals.navigation;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.javatuples.Quintet;

import bettinger.gedcomviewer.Events;
import bettinger.gedcomviewer.model.Family;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.model.Quality;
import bettinger.gedcomviewer.views.AutoFitTable;
import bettinger.gedcomviewer.views.TableModel;
import bettinger.gedcomviewer.views.UI;

public class NavigationTable extends AutoFitTable {

	TableModel<Quintet<String, Individual, Family, Individual, Integer>> tableModel;

	private static final TableCellRenderer qualityRenderer = new DefaultTableCellRenderer() {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			final var component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			if (component instanceof JLabel label && value instanceof Quality quality) {
				Color backgroundColor = TRANSPARENT;
				Color foregroundColor = Color.BLACK;

				if (quality == Quality.PRIMARY) {
					backgroundColor = GREEN;
				} else if (quality == Quality.SECONDARY) {
					backgroundColor = YELLOW;
				} else if (quality == Quality.QUESTIONABLE || quality == Quality.UNRELIABLE) {
					backgroundColor = RED;
				} else if (isSelected) {
					backgroundColor = SELECTION_BACKGROUND;
					foregroundColor = SELECTION_FOREGROUND;
				}

				label.setBackground(backgroundColor);
				label.setForeground(foregroundColor);

				label.setText("");
			}

			return component;
		}
	};

	public NavigationTable() {
		super();

		setDefaultRenderer(Quality.class, qualityRenderer);

		getSelectionModel().addListSelectionListener(this);
	}

	@SuppressWarnings("java:S2177")
	public void setModel(final TableModel<Quintet<String, Individual, Family, Individual, Integer>> tableModel) {
		this.tableModel = tableModel;

		super.setModel(tableModel);
	}

	@Override
	public void valueChanged(final ListSelectionEvent event) {
		super.valueChanged(event);

		final var selectedRowIndex = getSelectedRow();
		if (tableModel != null && !event.getValueIsAdjusting() && selectedRowIndex != -1) {
			Events.post(new UI.SelectRecordCommand(tableModel.getItemAt(convertRowIndexToModel(selectedRowIndex)).getValue1()));
		}
	}
}
