package bettinger.gedcomviewer.tools.portraitcomparison.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import org.javatuples.Pair;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.portraitcomparison.model.AncestralLine;
import bettinger.gedcomviewer.tools.portraitcomparison.model.FacialFeatureAnalysisResult;
import bettinger.gedcomviewer.tools.portraitcomparison.model.FacialFeatures;
import bettinger.gedcomviewer.views.AutoFitTable;
import bettinger.gedcomviewer.views.WebViewPanel;

public class ResultOverviewPane extends JPanel {
    private final WebViewPanel visualization;
    
    public ResultOverviewPane(final Individual proband, final int numGenerations, final TreeMap<FacialFeatures, FacialFeatureAnalysisResult> results) {
        super();
        setLayout(new BorderLayout());

        this.visualization = new WebViewPanel();
        TreeMap<FacialFeatures, Pair<ArrayList<AncestralLine>, Float>> maxPathSimilarities = new TreeMap<>();
        TreeMap<FacialFeatures, Pair<ArrayList<String>, Float>> maxPersonSimilarities = new TreeMap<>();
        final String[] columns = {I18N.get("FacialFeature"), I18N.get("LineColor"), I18N.get("MaxPathSimilarity"), I18N.get("MaxSimilarity")};
        ArrayList<Object[]> tableData = new ArrayList<>();

        for (final var entry : results.entrySet()) {
           final var feature = entry.getKey();
           final var featureResult = entry.getValue();
           final var maxPathSimilarity = featureResult.getMaxPathSimilarity();
           maxPathSimilarities.put(feature, maxPathSimilarity);
           final var maxPersonSimilarity = featureResult.getMaxPersonSimilarity();
           maxPersonSimilarities.put(feature, maxPersonSimilarity);

           Object[] row = {I18N.get(feature.name()), feature, maxPathSimilarity.getValue1(), maxPersonSimilarity.getValue1()};
           tableData.add(row);
        }

        var legend = new AutoFitTable();
        Object[][] data = tableData.toArray(new Object[0][0]);
        legend.setModel(new DefaultTableModel(data, columns));
        legend.getColumnModel().getColumn(1).setCellRenderer(new OverviewTableLineColorCellRenderer());

        add(new JScrollPane(legend), BorderLayout.EAST);
        add(visualization, BorderLayout.CENTER);
        update(proband, numGenerations, results);
    }

    private void update(final Individual proband, final int numGenerations, final TreeMap<FacialFeatures, FacialFeatureAnalysisResult> results) {
		ResultOverviewRenderer renderer = null;

		try {
			renderer = new ResultOverviewRenderer(proband, results);
		} catch (final Exception e) {
			Logger.getLogger(ResultOverviewPane.class.getName()).log(Level.SEVERE, "Failed to create renderer", e);
		}

		if (renderer != null) {
			renderer.render(proband, numGenerations+1);

			visualization.setBody(renderer.toString());
			visualization.scrollTo(renderer.getProbandNode().getPosition());
		}
	}

    public static Map<FacialFeatures, Color> getFeatureColors() {
        final Map<FacialFeatures, Color> FEATURE_COLORS = new HashMap<>();
        FEATURE_COLORS.put(FacialFeatures.CHEEKS, Color.decode("#4363d8"));
        FEATURE_COLORS.put(FacialFeatures.CHIN, Color.decode("#42d4f4"));
        FEATURE_COLORS.put(FacialFeatures.EYEBROWS, Color.decode("#f58231"));
        FEATURE_COLORS.put(FacialFeatures.EYES, Color.decode("#a9a9a9"));
        FEATURE_COLORS.put(FacialFeatures.FACESHAPE, Color.decode("#3cb44b"));
        FEATURE_COLORS.put(FacialFeatures.LIPS, Color.decode("#f032e6"));
        FEATURE_COLORS.put(FacialFeatures.NOSE, Color.decode("#dcbeff"));

        return FEATURE_COLORS;
    }
}
