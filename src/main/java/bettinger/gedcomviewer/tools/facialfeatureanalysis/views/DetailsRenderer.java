package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.AnalysisResult;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.FacialFeature;
import bettinger.gedcomviewer.views.visualization.AncestorsRenderer;
import bettinger.gedcomviewer.views.visualization.Node;

class DetailsRenderer extends AncestorsRenderer {
	private static final int EDGE_WIDTH = 3;
	private static final Stroke EDGE_STROKE = new BasicStroke(EDGE_WIDTH);

	private final FacialFeature facialFeature;
	private final AnalysisResult result;

	private final Map<Pair<String, String>, Float> coloredEdges;
	private final List<String> includedIds;
	private final Map<String, Float> lastIdOfLine;

	DetailsRenderer(final FacialFeature facialFeature, final AnalysisResult result) {
		this.facialFeature = facialFeature;
		this.result = result;

		this.coloredEdges = new HashMap<>();
		this.includedIds = new ArrayList<>();
		this.lastIdOfLine = new HashMap<>();
	}

	FacialFeature getFacialFeature() {
		return facialFeature;
	}

	@Override
	public void render(final Individual proband, final int generations, final Point offset) {
		for (final var entry : result.getLineSimilarities().entrySet()) {
			final var lineIds = entry.getKey().getIds();
			final var lineSimilarity = entry.getValue();

			var edge = new Pair<String, String>(proband.getId(), lineIds.get(0));
			coloredEdges.computeIfAbsent(edge, _ -> 0.0f);
			coloredEdges.put(edge, Math.max(coloredEdges.get(edge), lineSimilarity));

			if (result.getSimilarities().get(lineIds.get(0)) != null) {
				includedIds.add(lineIds.get(0));
			}

			String lastOfPath = null;
			final ArrayList<String> notIncludedIds = new ArrayList<>();

			for (int i = 0; i < lineIds.size() - 1; i++) {
				if (result.getSimilarities().get(lineIds.get(i + 1)) != null) {
					includedIds.addAll(notIncludedIds);
					notIncludedIds.clear();
					includedIds.add(lineIds.get(i + 1));
					lastOfPath = lineIds.get(i + 1);
				} else {
					notIncludedIds.add(lineIds.get(i + 1));
				}

				edge = new Pair<String, String>(lineIds.get(i), lineIds.get(i + 1));
				coloredEdges.computeIfAbsent(edge, _ -> 0.0f);
				coloredEdges.put(edge, Math.max(coloredEdges.get(edge), lineSimilarity));
			}

			lastIdOfLine.put(lastOfPath, lineSimilarity);
		}

		super.render(proband, generations, offset);
	}

	@Override
	protected Node createNode(final Individual individual, final boolean isClone, final Node parentNode) {
		final var node = new DetailsNode(this, g, individual, isClone, parentNode, individual != null ? result.getSimilarities().get(individual.getId()) : null);
		node.init();
		return node;
	}

	@Override
	protected int getEdgeLabelWidth(final Node v, final Node w) {
		var r = super.getEdgeLabelWidth(v, w);

		if (v != null && w != null && v.getIndividual() != null && w.getIndividual() != null && (includedIds.contains(v.getIndividual().getId()) || includedIds.contains(w.getIndividual().getId()))) {
			r = g.getFontMetrics().stringWidth("100.00% 100.0%");
		}

		return r;
	}

	@Override
	protected void renderEdges() {
		for (final var edge : edges) {
			final var childNode = edge.getValue0();
			final var fatherNode = edge.getValue1();
			final var motherNode = edge.getValue2();

			final boolean renderEdgeToFather = fatherNode != null && fatherNode.getIndividual() != null && includedIds.contains(fatherNode.getIndividual().getId()) && coloredEdges.containsKey(new Pair<String, String>(childNode.getIndividual().getId(), fatherNode.getIndividual().getId()));
			final boolean renderEdgeToMother = motherNode != null && motherNode.getIndividual() != null && includedIds.contains(motherNode.getIndividual().getId()) && coloredEdges.containsKey(new Pair<String, String>(childNode.getIndividual().getId(), motherNode.getIndividual().getId()));

			final Point parentsPoint = renderEdge(fatherNode, motherNode);
			if (parentsPoint != null) {
				if (!renderEdgeToFather || !renderEdgeToMother) {
					g.drawLine(parentsPoint.x, parentsPoint.y, parentsPoint.x, childNode.getPosition().y);
				}

				if (renderEdgeToFather) {
					renderMaxSimilarLineEdge(childNode, fatherNode, parentsPoint, true);
				}

				if (renderEdgeToMother) {
					renderMaxSimilarLineEdge(childNode, motherNode, parentsPoint, false);
				}
			}
		}
	}

	private void renderMaxSimilarLineEdge(final Node childNode, final Node parentNode, final Point parentsPoint, final boolean parentIsMale) {
		if (parentsPoint != null) {
			final var edge = new Pair<>(childNode.getIndividual().getId(), parentNode.getIndividual().getId());

			final var originalPaint = g.getPaint();
			final var originalStroke = g.getStroke();

			g.setPaint(FacialFeature.getColor(facialFeature, coloredEdges.get(edge)));
			g.setStroke(EDGE_STROKE);

			final int offsetX = parentIsMale ? -EDGE_WIDTH / 2 : EDGE_WIDTH / 2;
			final int endX = parentIsMale ? parentNode.getPosition().x + parentNode.getWidth() : parentNode.getPosition().x;

			g.drawLine(parentsPoint.x + offsetX, parentsPoint.y, endX, parentsPoint.y);
			g.drawLine(parentsPoint.x + offsetX, parentsPoint.y, parentsPoint.x + offsetX, childNode.getPosition().y);

			g.setPaint(originalPaint);
			g.setStroke(originalStroke);

			if (lastIdOfLine.containsKey(edge.getValue1())) {
				final var label = String.format("%.1f%%", lastIdOfLine.get(edge.getValue1()) * 100);
				final var labelWidth = g.getFontMetrics().stringWidth(label);

				final var lineStartX = parentIsMale ? endX : parentsPoint.x + offsetX;
				final var centerX = lineStartX + Math.abs(parentsPoint.x + offsetX - endX) / 2;

				g.drawString(label, centerX - labelWidth / 2, parentsPoint.y - 2 * EDGE_WIDTH);
			}
		}
	}
}
