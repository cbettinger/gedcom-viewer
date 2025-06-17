package bettinger.gedcomviewer.tools.portraitcomparison.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.portraitcomparison.model.FacialFeatureAnalysisResult;
import bettinger.gedcomviewer.tools.portraitcomparison.model.FacialFeatures;
import bettinger.gedcomviewer.views.AutoFitTable;

public class ResultOverviewPane extends JPanel {
    
    public ResultOverviewPane(final Individual proband, final int numGenerations, final TreeMap<FacialFeatures, FacialFeatureAnalysisResult> results) {
        super();
        setLayout(new BorderLayout());

        final Map<FacialFeatures, Color> FEATURE_COLORS = ResultOverviewPane.getFeatureColors();

        var visualization = new JPanel();
        TreeMap<FacialFeatures, Object[]> maxPathSimilarities = new TreeMap<>();
        TreeMap<FacialFeatures, Object[]> maxPersonSimilarities = new TreeMap<>();
        final String[] columns = {I18N.get("FacialFeature"), I18N.get("LineColor"), I18N.get("MaxPathSimilarity"), I18N.get("MaxSimilarity")};
        ArrayList<Object[]> tableData = new ArrayList<>();

        for (final var entry : results.entrySet()) {
           final var feature = entry.getKey();
           final var featureResult = entry.getValue();
           final var maxPathSimilarity = featureResult.getMaxPathSimilarity();
           maxPathSimilarities.put(feature, maxPathSimilarity);
           final var maxPersonSimilarity = featureResult.getMaxPersonSimilarity();
           maxPersonSimilarities.put(feature, maxPersonSimilarity);

           Object[] row = {I18N.get(feature.name()), feature, maxPathSimilarity[1], maxPersonSimilarity[1]};
           tableData.add(row);
        }

        var legend = new AutoFitTable();
        Object[][] data = tableData.toArray(new Object[0][0]);
        legend.setModel(new DefaultTableModel(data, columns));
        legend.getColumnModel().getColumn(1).setCellRenderer(new OverviewTableLineColorCellRenderer());

        add(new JScrollPane(legend), BorderLayout.LINE_END);
    }

    public static Map<FacialFeatures, Color> getFeatureColors() {
        final Map<FacialFeatures, Color> FEATURE_COLORS = new HashMap<>();
        FEATURE_COLORS.put(FacialFeatures.CHEEKS, Color.BLUE);
        FEATURE_COLORS.put(FacialFeatures.CHIN, Color.GREEN);
        FEATURE_COLORS.put(FacialFeatures.EYEBROWS, Color.ORANGE);
        FEATURE_COLORS.put(FacialFeatures.EYES, Color.RED);
        FEATURE_COLORS.put(FacialFeatures.FACESHAPE, Color.CYAN);
        FEATURE_COLORS.put(FacialFeatures.LIPS, Color.MAGENTA);
        FEATURE_COLORS.put(FacialFeatures.NOSE, Color.YELLOW);

        return FEATURE_COLORS;
    }
}
