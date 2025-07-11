package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

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
		sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS));

		final var colorGradientPane = new JPanel();
		colorGradientPane.setPreferredSize(new Dimension(400, 100));
		colorGradientPane.setLayout(new BoxLayout(colorGradientPane, BoxLayout.X_AXIS));

		colorGradientPane.add(new GradientPanel(DetailsRenderer.PERFECT_MATCH_COLOR, DetailsRenderer.NO_MATCH_COLOR));

		final var colorGradientInfo = new JPanel();
		colorGradientInfo.setLayout(new BorderLayout());
		colorGradientInfo.add(new JLabel("100%"), BorderLayout.NORTH);
		colorGradientInfo.add(new JLabel("0%"), BorderLayout.SOUTH);
		colorGradientPane.add(colorGradientInfo);

		sideBar.add(colorGradientPane);

		final var info = new JTextArea(String.format("%n%s%n%n%s: %s%n%n%s: %s", I18N.get("LineSimilarityInfo"), I18N.get("AvgSimilarity"), I18N.get("AvgSimilarityInfo"), I18N.get("MaxSimilarity"), I18N.get("MaxSimilarityDetailsInfo")));
		info.setBorder(null); // TODO: necc?
		info.setFocusable(false);
		info.setEditable(false);
		info.setLineWrap(true);
		info.setWrapStyleWord(true);
		sideBar.add(info);

		add(sideBar, BorderLayout.EAST);
	}
}
