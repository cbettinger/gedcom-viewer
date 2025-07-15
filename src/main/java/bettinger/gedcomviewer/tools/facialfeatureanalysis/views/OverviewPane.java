package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.AnalysisResult;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.FacialFeature;
import bettinger.gedcomviewer.views.AutoFitTable;
import bettinger.gedcomviewer.views.WebViewPanel;

class OverviewPane extends JPanel {
	private static final TableCellRenderer facialFeatureCellRenderer = new DefaultTableCellRenderer() {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			final var component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

			if (value instanceof FacialFeature facialFeature && component instanceof JLabel label) {
				label.setText("");
				label.setBackground(FacialFeature.getColor(facialFeature));
			}

			return component;
		}
	};

	OverviewPane(final Individual proband, final int depth, final Map<FacialFeature, AnalysisResult> results) {
		setLayout(new BorderLayout());

		final var renderer = new OverviewRenderer(results);
		renderer.render(proband, depth + 1);

		final var visualization = new WebViewPanel();
		visualization.setBody(renderer.toString());
		visualization.scrollTo(renderer.getProbandNode().getPosition());
		add(visualization, BorderLayout.CENTER);

		final var sideBar = new JPanel();
		sideBar.setBorder(BorderFactory.createEmptyBorder(Constants.TEXT_PANE_MARGIN, Constants.TEXT_PANE_MARGIN, Constants.TEXT_PANE_MARGIN, Constants.TEXT_PANE_MARGIN));
		sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS));

		final List<Object[]> tableData = new ArrayList<>();
		for (final var entry : results.entrySet()) {
			final var feature = entry.getKey();
			final var result = entry.getValue();
			tableData.add(new Object[] { I18N.get(feature.name()), feature, String.format("%.1f%%", result.getMaxSimilarity().getValue1() * 100), String.format("%.1f%%", result.getMaxLineSimilarity().getValue1() * 100) });
		}

		final var table = new AutoFitTable();
		table.setModel(new DefaultTableModel(tableData.toArray(new Object[0][0]), new String[] { I18N.get("FacialFeature"), I18N.get("Color"), I18N.get("MaxSimilarity"), I18N.get("MaxLineSimilarity") }));
		table.getColumnModel().getColumn(1).setCellRenderer(facialFeatureCellRenderer);
		sideBar.add(new JScrollPane(table));

		final var info = new JTextArea(String.format("%s%n%n%s%n%n%s%n%n%s", I18N.get("MaxSimilarity"), I18N.get("MaxSimilarPersonInfo"), I18N.get("MaxLineSimilarity"), I18N.get("MaxSimilarLineInfo")));
		info.setBorder(BorderFactory.createEmptyBorder(Constants.TEXT_PANE_MARGIN, 0, 0, 0));
		info.setFocusable(false);
		info.setEditable(false);
		info.setLineWrap(true);
		info.setWrapStyleWord(true);
		sideBar.add(info);

		add(sideBar, BorderLayout.EAST);
	}
}
