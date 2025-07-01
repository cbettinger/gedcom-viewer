package bettinger.gedcomviewer.tools.portraitcomparison.views;

import java.util.TreeMap;

import javax.swing.JTabbedPane;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.portraitcomparison.model.FacialFeatureAnalysisResult;
import bettinger.gedcomviewer.tools.portraitcomparison.model.FacialFeatures;
import bettinger.gedcomviewer.views.Frame;
import bettinger.gedcomviewer.views.MainFrame;

public class ResultFrame extends Frame {

    public ResultFrame(final Individual proband, final int numGenerations, final TreeMap<FacialFeatures, FacialFeatureAnalysisResult> results) {
        super();
        setTitle(String.format("%s: %s", I18N.get("FacialFeatureAnalysis"), proband.getName()));

        var detailedPane = new JTabbedPane();
        for (final var enrty : results.entrySet()) {
            detailedPane.addTab(I18N.get(enrty.getKey().name()), new DetailedResultPane(proband, numGenerations, enrty.getValue()));
        }

        var tabbedPane = new JTabbedPane();
        tabbedPane.addTab(I18N.get("Overview"), new ResultOverviewPane(proband, numGenerations, results));
        tabbedPane.addTab(I18N.get("DetailedView"), detailedPane);

        add(tabbedPane);
        pack();
        setSize(Constants.DEFAULT_FRAME_WIDTH, Constants.DEFAULT_FRAME_HEIGHT);
        setLocationRelativeTo(MainFrame.getInstance());
        setVisible(true);
    }
}
