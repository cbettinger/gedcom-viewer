package bettinger.gedcomviewer.tools.portraitcomparison.views;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTabbedPane;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.tools.portraitcomparison.model.FacialFeatureAnalysisResult;
import bettinger.gedcomviewer.tools.portraitcomparison.model.FacialFeatures;
import bettinger.gedcomviewer.views.Frame;

public class ResultFrame extends Frame {

    public ResultFrame(final HashMap<FacialFeatures, FacialFeatureAnalysisResult> results) {
        super();
        Logger.getLogger(ResultFrame.class.getName()).log(Level.INFO, results.toString());

        var detailedPane = new JTabbedPane();
        final TreeMap<FacialFeatures, FacialFeatureAnalysisResult> sortedResults = new TreeMap<>(results);
        for (final var enrty : sortedResults.entrySet()) {
            detailedPane.addTab(I18N.get(enrty.getKey().name()), new DetailedResultPane(enrty.getValue()));
        }

        var tabbedPane = new JTabbedPane();
        tabbedPane.addTab(I18N.get("Overview"), new ResultOverviewPane(results));
        tabbedPane.addTab(I18N.get("DetailedView"), detailedPane);

        add(tabbedPane);
        setVisible(true);
    }
}
