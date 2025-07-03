package bettinger.gedcomviewer.tools.facialfeatureanalysis.model;

import bettinger.gedcomviewer.model.Individual;

public interface AnalysisStarter {
    public void start(final Individual proband, final int maxDepth, final int maxNumPortraits);
}
