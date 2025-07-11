package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

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

		final var renderer = new OverviewRenderer(proband, results);
		renderer.render(proband, depth + 1);

		final var visualization = new WebViewPanel();
		visualization.setBody(renderer.toString());
		visualization.scrollTo(renderer.getProbandNode().getPosition());
		add(visualization, BorderLayout.CENTER);

		final var sideBar = new JPanel();
		sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS));

		final List<Object[]> tableData = new ArrayList<>();

		for (final var entry : results.entrySet()) {
			final var feature = entry.getKey();
			final var featureResult = entry.getValue();

			final var maxLineSimilarity = featureResult.getMaxLineSimilarity();
			final var maxIndividualSimilarity = featureResult.getMaxIndividualSimilarity();
			tableData.add(new Object[] { I18N.get(feature.name()), feature, String.format("%.2f%%", maxLineSimilarity.getValue1() * 100), String.format("%.2f%%", maxIndividualSimilarity.getValue1() * 100) });
		}

		final var table = new AutoFitTable();
		table.setModel(new DefaultTableModel(tableData.toArray(new Object[0][0]), new String[] { I18N.get("FacialFeature"), I18N.get("LineColor"), I18N.get("MaxPathSimilarity"), I18N.get("MaxSimilarity") }));	// TODO: individualsim/linesim
		table.getColumnModel().getColumn(1).setCellRenderer(facialFeatureCellRenderer);
		sideBar.add(new JScrollPane(table));

		final var info = new JTextArea(String.format("%n%s: %s%n%n%s: %s", I18N.get("MaxPathSimilarity"), I18N.get("MaxPathSimilarityInfo"), I18N.get("MaxSimilarity"), I18N.get("MaxSimilarityInfo")));
		info.setBorder(null); // TODO: necc?
		info.setFocusable(false);
		info.setEditable(false);
		info.setLineWrap(true);
		info.setWrapStyleWord(true);
		sideBar.add(info);

		add(sideBar, BorderLayout.EAST);
	}
}
