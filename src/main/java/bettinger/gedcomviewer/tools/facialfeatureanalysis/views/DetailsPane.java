package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.AnalysisResult;
import bettinger.gedcomviewer.views.WebViewPanel;

public class DetailsPane extends JPanel {
	public DetailsPane(final Individual proband, final int depth, final AnalysisResult result) {
		setLayout(new BorderLayout());

		final var renderer = new DetailsRenderer(result);
		renderer.render(proband, depth + 1);

		final var visualization = new WebViewPanel();
		visualization.setBody(renderer.toString());
		visualization.scrollTo(renderer.getProbandNode().getPosition());
		add(visualization, BorderLayout.CENTER);

		final var sideBar = new JPanel();
		sideBar.setBorder(BorderFactory.createEmptyBorder(Constants.TEXT_PANE_MARGIN, Constants.TEXT_PANE_MARGIN, Constants.TEXT_PANE_MARGIN, Constants.TEXT_PANE_MARGIN));
		sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS));

		sideBar.add(new Gradient(400, 20, DetailsRenderer.NO_MATCH_COLOR, DetailsRenderer.PERFECT_MATCH_COLOR));

		final var info = new JTextArea(String.format("%s: %s%n%n%s: %s%n%n%s: %s", I18N.get("LineSimilarity"), I18N.get("LineSimilarityInfo"), I18N.get("AvgSimilarity"), I18N.get("AvgSimilarityInfo"), I18N.get("MaxSimilarity"), I18N.get("MaxSimilarityDetailsInfo")));
		info.setBorder(BorderFactory.createEmptyBorder(Constants.TEXT_PANE_MARGIN, 0, 0, 0));
		info.setFocusable(false);
		info.setEditable(false);
		info.setLineWrap(true);
		info.setWrapStyleWord(true);
		sideBar.add(info);

		add(sideBar, BorderLayout.EAST);
	}
}
