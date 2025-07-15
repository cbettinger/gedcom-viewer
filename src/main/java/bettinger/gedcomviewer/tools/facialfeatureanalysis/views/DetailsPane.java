package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.AnalysisResult;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.FacialFeature;

class DetailsPane extends ResultsPane {
	DetailsPane(final Individual proband, final int depth, final FacialFeature facialFeature, final AnalysisResult result) {
		super(new DetailsRenderer(facialFeature, result), proband, depth);

		sideBar.add(new Gradient(400, 20, facialFeature));

		final var info = new JTextArea(String.format("%s%n%n%s%n%n%s%n%n%s%n%n%s%n%n%s%n%n", I18N.get("AvgSimilarity"), I18N.get("AvgSimilarityInfo"), I18N.get("MaxSimilarity"), I18N.get("MaxSimilarityInfo"), I18N.get("LineSimilarity"), I18N.get("LineSimilarityInfo")));
		info.setBorder(BorderFactory.createEmptyBorder(Constants.TEXT_PANE_MARGIN, 0, 0, 0));
		info.setFocusable(false);
		info.setEditable(false);
		info.setLineWrap(true);
		info.setWrapStyleWord(true);
		sideBar.add(info);
	}
}
