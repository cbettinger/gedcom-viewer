package bettinger.gedcomviewer.tools.portraitcomparison.views;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import bettinger.gedcomviewer.tools.portraitcomparison.model.FacialFeatureAnalysisResult;
import bettinger.gedcomviewer.tools.portraitcomparison.model.FacialFeatures;

public class ResultFrame {

    private HashMap<FacialFeatures, FacialFeatureAnalysisResult> results;

    public ResultFrame(final HashMap<FacialFeatures, FacialFeatureAnalysisResult> results) {
        this.results = results;
        Logger.getLogger(ResultFrame.class.getName()).log(Level.INFO, results.toString());
    }
}
