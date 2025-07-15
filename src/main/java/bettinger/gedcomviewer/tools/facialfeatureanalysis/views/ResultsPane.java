package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.views.WebViewPanel;
import bettinger.gedcomviewer.views.visualization.Renderer;
import bettinger.gedcomviewer.views.visualization.VisualizationZoomStatusBar;

abstract class ResultsPane extends JPanel {

	protected final WebViewPanel visualization;
	protected final JPanel sideBar;

	protected ResultsPane(final Renderer renderer, final Individual proband, final int depth) {
		setLayout(new BorderLayout());

		renderer.render(proband, depth + 1);

		final var visualizationContainer = new JPanel();
		visualizationContainer.setLayout(new BorderLayout());

		this.visualization = new WebViewPanel();
		visualization.setBody(renderer.toString());
		visualization.scrollTo(renderer.getProbandNode().getPosition());
		visualizationContainer.add(visualization, BorderLayout.CENTER);

		visualizationContainer.add(new VisualizationZoomStatusBar(visualization), BorderLayout.SOUTH);

		add(visualizationContainer, BorderLayout.CENTER);

		this.sideBar = new JPanel();
		sideBar.setBorder(BorderFactory.createEmptyBorder(Constants.TEXT_PANE_MARGIN, Constants.TEXT_PANE_MARGIN, Constants.TEXT_PANE_MARGIN, Constants.TEXT_PANE_MARGIN));
		sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS));
		add(sideBar, BorderLayout.EAST);
	}
}
