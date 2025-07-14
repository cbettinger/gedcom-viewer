package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.AnalysisResult;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.Similarity;
import bettinger.gedcomviewer.views.visualization.AncestorsRenderer;
import bettinger.gedcomviewer.views.visualization.Node;

class DetailsRenderer extends AncestorsRenderer {
	static final Color PERFECT_MATCH_COLOR = Color.GREEN;
	static final Color NO_MATCH_COLOR = Color.DARK_GRAY;

	private static final int EDGE_WIDTH = 3;
	private static final Stroke EDGE_STROKE = new BasicStroke(EDGE_WIDTH);

	private final Map<String, Similarity> individualSimilarities;
	private Map<Pair<String, String>, Float> coloredEdges;
	private List<String> includedIndividualsIds;
	private Map<String, Float> lastIndividualsOfLine;
	private AnalysisResult result;

	DetailsRenderer(final AnalysisResult result) {
		this.individualSimilarities = result.getIndividualSimilarities();
		this.coloredEdges = new HashMap<>();
		this.includedIndividualsIds = new ArrayList<>();
		this.lastIndividualsOfLine = new HashMap<>();
		this.result = result;
	}

	@Override
	public void render(final Individual proband, final int generations, final Point offset) {
		super.render(proband, generations, offset);

		for (final var entry : result.getLineSimilarities().entrySet()) {
			final var lineIds = entry.getKey().getIds();
			final var similarity = entry.getValue();

			final var k1 = new Pair<String, String>(proband.getId(), lineIds.get(0));
			this.coloredEdges.computeIfAbsent(k1, _ -> 0.0f);
			this.coloredEdges.put(k1, Math.max(this.coloredEdges.get(k1), similarity));

			if (individualSimilarities.get(lineIds.get(0)) != null) {
				this.includedIndividualsIds.add(lineIds.get(0));
			}

			String lastOfPath = null;
			final ArrayList<String> notIncluded = new ArrayList<>();

			for (int i = 0; i < lineIds.size() - 1; i++) {
				if (individualSimilarities.get(lineIds.get(i + 1)) != null) {
					this.includedIndividualsIds.addAll(notIncluded);
					notIncluded.clear();
					this.includedIndividualsIds.add(lineIds.get(i + 1));
					lastOfPath = lineIds.get(i + 1);
				} else {
					notIncluded.add(lineIds.get(i + 1));
				}

				final var k2 = new Pair<String, String>(lineIds.get(i), lineIds.get(i + 1));
				this.coloredEdges.computeIfAbsent(k2, _ -> 0.0f);
				this.coloredEdges.put(k1, Math.max(this.coloredEdges.get(k2), similarity));
			}

			this.lastIndividualsOfLine.put(lastOfPath, similarity);
		}
	}

	@Override
	protected Node createNode(final Individual individual, final boolean isClone, final Node parentNode) {
		final var node = new DetailsNode(g, individual, isClone, parentNode, proband, individual != null ? individualSimilarities.get(individual.getId()) : null);
		node.init();
		return node;
	}

	@Override
	protected int getEdgeLabelWidth(final Node v, final Node w) {
		var result = super.getEdgeLabelWidth(v, w);

		if (v != null && w != null && v.getIndividual() != null && w.getIndividual() != null && (includedIndividualsIds.contains(v.getIndividual().getId()) || includedIndividualsIds.contains(w.getIndividual().getId()))) {
			result = g.getFontMetrics().stringWidth("100.00% 100.0%");
		}

		return result;
	}

	@Override
	protected void renderEdges() {
		for (final var edge : edges) {
			final var childNode = edge.getValue0();
			final var fatherNode = edge.getValue1();
			final var motherNode = edge.getValue2();

			final boolean considerFather = fatherNode != null && fatherNode.getIndividual() != null && includedIndividualsIds.contains(fatherNode.getIndividual().getId());
			final boolean considerMother = motherNode != null && motherNode.getIndividual() != null && includedIndividualsIds.contains(motherNode.getIndividual().getId());

			boolean drawFather = considerFather && coloredEdges.containsKey(new Pair<String, String>(childNode.getIndividual().getId(), fatherNode.getIndividual().getId()));
			boolean drawMother = considerMother && coloredEdges.containsKey(new Pair<String, String>(childNode.getIndividual().getId(), motherNode.getIndividual().getId()));

			g.setPaint(Color.WHITE);

			final Point parentsPoint = renderEdge(fatherNode, motherNode);
			if (parentsPoint != null) {
				if (!drawFather || !drawMother) {
					g.setPaint(Color.BLACK);
					renderEdge(fatherNode, motherNode);
					g.drawLine(parentsPoint.x, parentsPoint.y, parentsPoint.x, childNode.getPosition().y);
				}

				if (drawFather) {
					final Pair<String, String> tuple = new Pair<>(childNode.getIndividual().getId(), fatherNode.getIndividual().getId());
					renderColoredEdge(childNode, fatherNode, parentsPoint, tuple, true);
				}

				if (drawMother) {
					final Pair<String, String> tuple = new Pair<>(childNode.getIndividual().getId(), motherNode.getIndividual().getId());
					renderColoredEdge(childNode, motherNode, parentsPoint, tuple, false);
				}
			}
		}
	}

	private void renderColoredEdge(final Node rootNode, final Node parentNode, final Point parentsPoint, final Pair<String, String> tuple, final boolean maleLine) {
		if (parentsPoint != null) {
			final var originalPaint = g.getPaint();
			final var originalStroke = g.getStroke();

			g.setPaint(getSimilarityColor(coloredEdges.get(tuple)));
			g.setStroke(EDGE_STROKE);

			final int offsetX = maleLine ? -EDGE_WIDTH / 2 : EDGE_WIDTH / 2;
			final int endX = maleLine ? parentNode.getPosition().x + parentNode.getWidth() : parentNode.getPosition().x;

			g.drawLine(parentsPoint.x + offsetX, parentsPoint.y, endX, parentsPoint.y);
			g.drawLine(parentsPoint.x + offsetX, parentsPoint.y, parentsPoint.x + offsetX, rootNode.getPosition().y);

			g.setPaint(originalPaint);
			g.setStroke(originalStroke);

			if (lastIndividualsOfLine.containsKey(tuple.getValue1())) {
				final var label = String.format("%.2f%%", lastIndividualsOfLine.get(tuple.getValue1()) * 100);
				final var labelWidth = g.getFontMetrics().stringWidth(label);

				final var lineStartX = maleLine ? endX : parentsPoint.x + offsetX;
				final var centerX = lineStartX + Math.abs(parentsPoint.x + offsetX - endX) / 2;

				g.drawString(label, centerX - labelWidth / 2, parentsPoint.y - EDGE_WIDTH);
			}
		}
	}

	static Color getSimilarityColor(final float similarity) {
		final int r = Math.min(255, (int) (NO_MATCH_COLOR.getRed() + similarity * PERFECT_MATCH_COLOR.getRed()));
		final int g = Math.min(255, (int) (NO_MATCH_COLOR.getGreen() + similarity * PERFECT_MATCH_COLOR.getGreen()));
		final int b = Math.min(255, (int) (NO_MATCH_COLOR.getBlue() + similarity * PERFECT_MATCH_COLOR.getBlue()));
		return new Color(r, g, b, 255);
	}
}
