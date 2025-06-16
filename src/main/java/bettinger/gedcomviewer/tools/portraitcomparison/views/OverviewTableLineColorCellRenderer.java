package bettinger.gedcomviewer.tools.portraitcomparison.views;

import java.awt.Color;
import java.awt.Component;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import bettinger.gedcomviewer.tools.portraitcomparison.model.FacialFeatures;

public class OverviewTableLineColorCellRenderer extends DefaultTableCellRenderer {
    private final Map<FacialFeatures, Color> cellColors;

    public OverviewTableLineColorCellRenderer() {
        super();
        this.cellColors = ResultOverviewPane.getFeatureColors();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

        if (value instanceof FacialFeatures) {
            l.setText("");
            l.setBackground(cellColors.get(value));
        }
        return l;
    }
}
