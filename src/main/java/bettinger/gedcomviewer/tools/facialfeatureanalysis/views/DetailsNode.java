package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BasicStroke;
import java.awt.Image;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.svggen.SVGGraphics2D;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.FacialFeature;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.Similarity;
import bettinger.gedcomviewer.views.visualization.Node;

class DetailsNode extends Node {
	private static final int BORDER_WIDTH = 3;
	private static final Stroke BORDER_STROKE = new BasicStroke(BORDER_WIDTH);

	private final Similarity similarity;

	private Image probandPortrait;
	private int probandPortraitWidth;

	DetailsNode(final DetailsRenderer renderer, final SVGGraphics2D g, final Individual individual, final boolean isClone, final Node parent, final Similarity similarity) {
		super(renderer, g, individual, isClone, parent);

		this.similarity = similarity;
	}

	@Override
	public void init() {
		if (individual != null && renderer.getProband() != null && individual != renderer.getProband() && similarity != null) {
			probandPortrait = getPortrait(renderer.getProband(), similarity.getMaxSimilarProbandPortraitFilePath());
			probandPortraitWidth = getPortraitWidth(probandPortrait);
		} else {
			probandPortrait = null;
			probandPortraitWidth = 0;
		}

		super.init();

		width += probandPortrait == null ? 0 : probandPortraitWidth + PADDING;
	}

	@Override
	public void render(final int x, final int y) {
		super.render(x, y);

		final var borderColor = similarity == null ? null : FacialFeature.getColor(((DetailsRenderer) renderer).getFacialFeature(), similarity.getAvgSimilarity());
		if (borderColor != null) {
			final var originalPaint = g.getPaint();
			final var originalStroke = g.getStroke();

			g.setPaint(borderColor);
			g.setStroke(BORDER_STROKE);
			g.drawRect(this.x, this.y, this.width, this.height);

			g.setPaint(originalPaint);
			g.setStroke(originalStroke);
		}
	}

	@Override
	protected void renderPortraits() {
		super.renderPortraits();

		if (probandPortrait != null) {
			g.drawImage(probandPortrait, x + portraitWidth + 2 * PADDING, y + PADDING, probandPortraitWidth, PORTRAIT_HEIGHT, null);
		}
	}

	@Override
	protected Image getPortrait() {
		return similarity == null ? super.getPortrait() : getPortrait(individual, similarity.getMaxSimilarAncestorPortraitFilePath());
	}

	@Override
	protected List<String> getTextLines() {
		final List<String> result = new ArrayList<>();

		result.add(getFirstTextLine());

		if (individual != null && similarity != null) {
			result.add(String.format("%s: %.1f%%", I18N.get("AvgSimilarity"), similarity.getAvgSimilarity() * 100));
			result.add(String.format("%s: %.1f%%", I18N.get("MaxSimilarity"), similarity.getMaxSimilarity() * 100));
		}

		return result;
	}

	@Override
	protected int getTextPositionX() {
		return super.getTextPositionX() + (probandPortrait == null ? 0 : probandPortraitWidth + 2 * PADDING);
	}

	private static Image getPortrait(final Individual individual, final String filePath) {
		if (individual != null && filePath != null && !filePath.isEmpty()) {
			final var portraits = individual.getFacialPortraits();
			for (final var portrait : portraits) {
				if (portrait.getFilePath().equals(filePath) && portrait.exists()) {
					return individual.getClippedImage(portrait, -1, PORTRAIT_HEIGHT);
				}
			}
		}

		return null;
	}
}
