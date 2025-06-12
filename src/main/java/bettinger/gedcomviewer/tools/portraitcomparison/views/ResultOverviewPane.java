package bettinger.gedcomviewer.tools.portraitcomparison.views;

import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.JPanel;

import bettinger.gedcomviewer.tools.portraitcomparison.model.FacialFeatureAnalysisResult;
import bettinger.gedcomviewer.tools.portraitcomparison.model.FacialFeatures;

public class ResultOverviewPane extends JPanel {
    
    public ResultOverviewPane(final TreeMap<FacialFeatures, FacialFeatureAnalysisResult> results) {
        super();
    }
}
