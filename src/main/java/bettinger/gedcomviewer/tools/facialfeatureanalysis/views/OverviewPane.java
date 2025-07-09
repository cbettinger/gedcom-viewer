package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.javatuples.Pair;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.model.AncestralLine;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.model.FacialFeatureAnalysisResult;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.model.FacialFeature;
import bettinger.gedcomviewer.views.AutoFitTable;
import bettinger.gedcomviewer.views.WebViewPanel;

public class OverviewPane extends JPanel {

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

	private final WebViewPanel visualization;

	public OverviewPane(final Individual proband, final int numGenerations, final Map<FacialFeature, FacialFeatureAnalysisResult> results) {
		super();
		setLayout(new BorderLayout());

		this.visualization = new WebViewPanel();
		TreeMap<FacialFeature, Pair<ArrayList<AncestralLine>, Float>> maxPathSimilarities = new TreeMap<>();
		TreeMap<FacialFeature, Pair<ArrayList<String>, Float>> maxPersonSimilarities = new TreeMap<>();
		final String[] columns = { I18N.get("FacialFeature"), I18N.get("LineColor"), I18N.get("MaxPathSimilarity"), I18N.get("MaxSimilarity") };
		ArrayList<Object[]> tableData = new ArrayList<>();

		for (final var entry : results.entrySet()) {
			final var feature = entry.getKey();
			final var featureResult = entry.getValue();
			final var maxPathSimilarity = featureResult.getMaxPathSimilarity();
			maxPathSimilarities.put(feature, maxPathSimilarity);
			final var maxPersonSimilarity = featureResult.getMaxPersonSimilarity();
			maxPersonSimilarities.put(feature, maxPersonSimilarity);

			Object[] row = { I18N.get(feature.name()), feature, String.format("%.2f%%", maxPathSimilarity.getValue1() * 100), String.format("%.2f%%", maxPersonSimilarity.getValue1() * 100) };
			tableData.add(row);
		}

		var table = new AutoFitTable();
		Object[][] data = tableData.toArray(new Object[0][0]);
		table.setModel(new DefaultTableModel(data, columns));
		table.getColumnModel().getColumn(1).setCellRenderer(facialFeatureCellRenderer);

		var explanations = new JTextArea();
		explanations.setEditable(false);
		explanations.setLineWrap(true);
		explanations.setText(String.format("\n%s: %s\n\n%s: %s", I18N.get("MaxPathSimilarity"), I18N.get("MaxPathSimilarityOverviewExplanation"), I18N.get("MaxSimilarity"), I18N.get("MaxSimilarityOverviewExplanation")));

		var legend = new JPanel();
		legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
		legend.add(new JScrollPane(table));
		legend.add(explanations);

		add(legend, BorderLayout.EAST);
		add(visualization, BorderLayout.CENTER);
		update(proband, numGenerations, results);
	}

	private void update(final Individual proband, final int numGenerations, final Map<FacialFeature, FacialFeatureAnalysisResult> results) {
		OverviewRenderer renderer = null;

		try {
			renderer = new OverviewRenderer(proband, results);
		} catch (final Exception e) {
			Logger.getLogger(OverviewPane.class.getName()).log(Level.SEVERE, "Failed to create renderer", e);
		}

		if (renderer != null) {
			renderer.render(proband, numGenerations + 1);

			visualization.setBody(renderer.toString());
			visualization.scrollTo(renderer.getProbandNode().getPosition());
		}
	}
}
