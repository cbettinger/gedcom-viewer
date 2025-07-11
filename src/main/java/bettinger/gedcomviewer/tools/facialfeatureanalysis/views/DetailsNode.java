package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Image;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.svggen.SVGGraphics2D;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.Similarity;
import bettinger.gedcomviewer.views.visualization.Node;

class DetailsNode extends Node {
	private static final int BORDER_WIDTH = 3;
	private static final Stroke BORDER_STROKE = new BasicStroke(BORDER_WIDTH);

	private final Individual proband;
	private final Similarity similarity;

	private Image probandPortrait;
	private int probandPortraitWidth;

	DetailsNode(final SVGGraphics2D g, final Individual individual, final boolean isClone, final Node parent, final Individual proband, final Similarity similarity) {
		super(g, individual, isClone, parent);

		this.proband = proband;
		this.similarity = similarity;
	}

	@Override
	protected void init() {
		super.init();

		if (individual != null && individual != proband && similarity != null) {
			probandPortrait = getPortrait(proband, similarity);
			probandPortraitWidth = getPortraitWidth(probandPortrait);

			width += probandPortrait == null ? 0 : probandPortraitWidth + PADDING;
		} else {
			probandPortrait = null;
			probandPortraitWidth = 0;
		}
	}

	@Override
	public void render(final int x, final int y) {
		super.render(x, y);

		final var borderColor = getBorderColor(similarity);
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
		return getPortrait(individual, similarity);
	}

	@Override
	protected List<String> getTextLines() {
		final List<String> result = new ArrayList<>();

		result.add(getFirstTextLine());

		if (individual != null) {
			result.add(String.format("%s: %.2f%%", I18N.get("AvgSimilarity"), similarity.getAvgSimilarity() * 100));
			result.add(String.format("%s: %.2f%%", I18N.get("MaxSimilarity"), similarity.getMaxSimilarity() * 100));
		}

		return result;
	}

	@Override
	protected int getTextPositionX() {
		return super.getTextPositionX() + (probandPortrait == null ? 0 : probandPortraitWidth + 2 * PADDING);
	}

	private static Color getBorderColor(final Similarity similarity) {
		if (similarity == null) {
			return null;
		}

		final var r = Math.min(255, (int) (DetailsPane.NO_MATCH_COLOR.getRed() + similarity.getAvgSimilarity() * DetailsPane.PERFECT_MATCH_COLOR.getRed()));
		final var g = Math.min(255, (int) (DetailsPane.NO_MATCH_COLOR.getGreen() + similarity.getAvgSimilarity() * DetailsPane.PERFECT_MATCH_COLOR.getGreen()));
		final var b = Math.min(255, (int) (DetailsPane.NO_MATCH_COLOR.getBlue() + similarity.getAvgSimilarity() * DetailsPane.PERFECT_MATCH_COLOR.getBlue()));
		return new Color(r, g, b, 255);
	}

	private static Image getPortrait(final Individual individual, final Similarity similarity) {
		if (individual != null && similarity != null) {
			final var portraits = individual.getFacialPortraits();
			final var filePath = similarity.getMaxSimilarityAncestorPortrait();
			for (final var portrait : portraits) {
				if (portrait.getFilePath().equals(filePath) && portrait.exists()) {
					return individual.getClippedImage(portrait, -1, PORTRAIT_HEIGHT);
				}
			}
		}

		return null;
	}
}
