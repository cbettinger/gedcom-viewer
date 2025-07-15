package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.batik.svggen.SVGGraphics2D;

import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.views.visualization.Node;

class OverviewNode extends Node {

	private static final int STROKE_WIDTH = 3;
	private static final Stroke STROKE = new BasicStroke(STROKE_WIDTH);

	private final Map<Color, ArrayList<String>> maxIndividualSimilarityIds;

	OverviewNode(final OverviewRenderer renderer, final SVGGraphics2D g, final Individual individual, final boolean isClone, final Node parent, final Map<Color, ArrayList<String>> maxIndividualSimilarityIds) {
		super(renderer, g, individual, isClone, parent);

		this.maxIndividualSimilarityIds = maxIndividualSimilarityIds;
	}

	@Override
	public void render(final int x, final int y) {
		super.render(x, y);

		final var originalPaint = g.getPaint();
		final var originalStroke = g.getStroke();

		g.setStroke(STROKE);

		int colorIndex = 0;
		for (final var entry : maxIndividualSimilarityIds.entrySet()) {
			final var maxPathIds = entry.getValue();
			if (individual != null && maxPathIds.contains(individual.getId())) {
				final var border = getRectangle();
				final int offset = colorIndex * STROKE_WIDTH;

				g.setPaint(entry.getKey());
				g.drawRect(border.x - offset, border.y - offset, border.width + 2 * offset, border.height + 2 * offset);

				colorIndex++;
			}
		}

		g.setPaint(originalPaint);
		g.setStroke(originalStroke);
	}

	@Override
	protected List<String> getTextLines() {
		final List<String> result = new ArrayList<>();
		result.add(getFirstTextLine());
		return result;
	}
}
