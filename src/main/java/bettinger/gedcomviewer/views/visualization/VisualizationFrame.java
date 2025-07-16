package bettinger.gedcomviewer.views.visualization;

import java.awt.BorderLayout;
import java.util.logging.Level;
import java.util.logging.Logger;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.views.Frame;
import bettinger.gedcomviewer.views.MainFrame;
import bettinger.gedcomviewer.views.WebViewPanel;

public class VisualizationFrame extends Frame {

	private static final int MARGIN = 100;

	private final Individual proband;
	private final Class<? extends Renderer> rendererClass;
	private String renderedBody;

	private final WebViewPanel visualization;
	private final VisualizationStatusBar statusBar;

	private VisualizationFrame(final String title, final Individual proband, Class<? extends Renderer> rendererClass) {
		setTitle(String.format(Format.KEY_VALUE, title, proband.getName()));
		setLayout(new BorderLayout());

		this.proband = proband;
		this.rendererClass = rendererClass;
		this.renderedBody = "";

		this.visualization = new WebViewPanel();
		add(visualization, BorderLayout.CENTER);

		add(new VisualizationToolBar(visualization, proband), BorderLayout.NORTH);

		this.statusBar = new VisualizationStatusBar(visualization);
		this.statusBar.addPropertyChangeListener(event -> {
			if (event.getPropertyName().equals(VisualizationStatusBar.PROPERTY_GENERATIONS)) {
				update();
			}
		});
		add(statusBar, BorderLayout.SOUTH);

		update();

		pack();
		setSize(Constants.DEFAULT_FRAME_WIDTH - MARGIN, Constants.DEFAULT_FRAME_HEIGHT - MARGIN);
		setLocationRelativeTo(MainFrame.getInstance());
	}

	private void update() {
		Renderer renderer = null;

		try {
			renderer = rendererClass.getDeclaredConstructor().newInstance();
		} catch (final Exception e) {
			Logger.getLogger(VisualizationFrame.class.getName()).log(Level.SEVERE, "Failed to create renderer", e);
		}

		if (renderer != null) {
			renderer.render(proband, statusBar.getGenerations());
			renderedBody = renderer.toString();

			visualization.setBody(renderedBody);
			visualization.scrollTo(renderer.getProbandNode().getPosition());

			statusBar.setNumberOfIndividuals(renderer.getIndividualCount());
		}
	}

	public static void renderLineage(final Individual proband) {
		new VisualizationFrame(I18N.get("Lineage"), proband, LineageRenderer.class).setVisible(true);
	}

	public static void renderAncestors(final Individual proband) {
		new VisualizationFrame(I18N.get("AncestorsList"), proband, AncestorsRenderer.class).setVisible(true);
	}

	public static void renderDescendants(final Individual proband) {
		new VisualizationFrame(I18N.get("DescendantsList"), proband, DescendantsRenderer.class).setVisible(true);
	}

	public static void renderConsanguins(final Individual proband) {
		new VisualizationFrame(I18N.get("ConsanguinsList"), proband, ConsanguinsRenderer.class).setVisible(true);
	}
}
