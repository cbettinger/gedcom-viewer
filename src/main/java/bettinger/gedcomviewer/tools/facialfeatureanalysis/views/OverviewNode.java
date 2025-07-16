package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.batik.svggen.SVGGraphics2D;
import org.javatuples.Pair;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.FacialFeature;
import bettinger.gedcomviewer.views.visualization.Node;

class OverviewNode extends Node {

	private final Map<FacialFeature, Pair<ArrayList<String>, Float>> maxSimilarIds;

	OverviewNode(final OverviewRenderer renderer, final SVGGraphics2D g, final Individual individual, final boolean isClone, final Node parent, final Map<FacialFeature, Pair<ArrayList<String>, Float>> maxSimilarIds) {
		super(renderer, g, individual, isClone, parent);

		this.maxSimilarIds = maxSimilarIds;
	}

	@Override
	public void render(final int x, final int y) {
		super.render(x, y);

		final var originalPaint = g.getPaint();
		final var originalStroke = g.getStroke();

		final var border = getRectangle();

		g.setStroke(OverviewRenderer.STROKE);

		int i = 0;
		for (final var entry : maxSimilarIds.entrySet()) {
			if (individual != null && entry.getValue().getValue0().contains(individual.getId())) {
				final int offset = i++ * OverviewRenderer.STROKE_WIDTH;

				g.setPaint(FacialFeature.getColor(entry.getKey()));
				g.drawRect(border.x - offset, border.y - offset, border.width + 2 * offset, border.height + 2 * offset);
			}
		}

		g.setPaint(originalPaint);
		g.setStroke(originalStroke);
	}

	@Override
	protected List<String> getTextLines() {
		final List<String> result = new ArrayList<>();

		result.add(getFirstTextLine());

		for (final var entry : maxSimilarIds.entrySet()) {
			if (individual != null && entry.getValue().getValue0().contains(individual.getId())) {
				result.add(String.format("%s: %.1f%%", I18N.get(entry.getKey().name()), entry.getValue().getValue1() * 100));
			}
		}

		return result;
	}
}
