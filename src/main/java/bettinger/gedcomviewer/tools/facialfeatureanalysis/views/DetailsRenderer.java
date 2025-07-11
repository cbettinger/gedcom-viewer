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

public class DetailsRenderer extends AncestorsRenderer {
	private static final int LINE_WIDTH = 3;

	private final Map<String, Similarity> personSimilarities;
	private Map<Pair<String, String>, Float> coloredEdges;
	private List<String> includedIndividuals;
	private Map<String, Float> lastIndividualsOfPath;

	DetailsRenderer(final AnalysisResult result) {
		this.personSimilarities = result.getIndividualSimilarities();
		this.coloredEdges = new HashMap<>();
		this.includedIndividuals = new ArrayList<>();
		this.lastIndividualsOfPath = new HashMap<>();

		for (final var entry : result.getLineSimilarities().entrySet()) {
			var pathIds = entry.getKey().getIds();
			var similarity = entry.getValue();

			var tuple = new Pair<String, String>(proband.getId(), pathIds.get(0));
			if (!this.coloredEdges.containsKey(tuple)) {
				this.coloredEdges.put(tuple, 0.f);
			}
			this.coloredEdges.put(tuple, Math.max(this.coloredEdges.get(tuple), similarity));
			if (personSimilarities.get(pathIds.get(0)) != null) {
				this.includedIndividuals.add(pathIds.get(0));
			}

			String lastOfPath = null;
			ArrayList<String> notIncluded = new ArrayList<>();
			for (int i = 0; i < pathIds.size() - 1; i++) {
				if (personSimilarities.get(pathIds.get(i + 1)) != null) {
					this.includedIndividuals.addAll(notIncluded);
					notIncluded.clear();
					this.includedIndividuals.add(pathIds.get(i + 1));
					lastOfPath = pathIds.get(i + 1);
				} else {
					notIncluded.add(pathIds.get(i + 1));
				}
				tuple = new Pair<String, String>(pathIds.get(i), pathIds.get(i + 1));
				if (!this.coloredEdges.containsKey(tuple)) {
					this.coloredEdges.put(tuple, 0.f);
				}
				this.coloredEdges.put(tuple, Math.max(this.coloredEdges.get(tuple), similarity));
			}
			this.lastIndividualsOfPath.put(lastOfPath, similarity);
		}
	}

	@Override
	protected void renderEdges() {
		for (final var edge : edges) {
			final var rootNode = edge.getValue0();
			final var fatherNode = edge.getValue1();
			final var motherNode = edge.getValue2();

			final boolean considerFather = fatherNode != null && fatherNode.getIndividual() != null && includedIndividuals.contains(fatherNode.getIndividual().getId());
			final boolean considerMother = motherNode != null && motherNode.getIndividual() != null && includedIndividuals.contains(motherNode.getIndividual().getId());

			boolean drawLeft = considerFather && coloredEdges.containsKey(new Pair<String, String>(rootNode.getIndividual().getId(), fatherNode.getIndividual().getId()));
			boolean drawRight = considerMother && coloredEdges.containsKey(new Pair<String, String>(rootNode.getIndividual().getId(), motherNode.getIndividual().getId()));

			g.setPaint(Color.WHITE);
			final Point parentsPoint = renderEdge(fatherNode, motherNode);
			if (parentsPoint != null) {
				if (!drawLeft || !drawRight) {
					g.setPaint(Color.BLACK);
					renderEdge(fatherNode, motherNode);
					g.drawLine(parentsPoint.x, parentsPoint.y, parentsPoint.x, rootNode.getPosition().y);
				}
				if (drawLeft) {
					final Pair<String, String> tuple = new Pair<>(rootNode.getIndividual().getId(), fatherNode.getIndividual().getId());
					renderColoredEdge(rootNode, fatherNode, parentsPoint, tuple, true);
				}
				if (drawRight) {
					final Pair<String, String> tuple = new Pair<>(rootNode.getIndividual().getId(), motherNode.getIndividual().getId());
					renderColoredEdge(rootNode, motherNode, parentsPoint, tuple, false);
				}
			}
		}
	}

	private void renderColoredEdge(final Node rootNode, final Node parentNode, final Point parentsPoint, final Pair<String, String> tuple, final boolean left) {
		final Point parentNodePosition = parentNode.getPosition();
		float similarity = coloredEdges.get(tuple);

		final Stroke defaultStroke = g.getStroke();

		int red = Math.min(255, (int) (DetailsPane.NO_MATCH_COLOR.getRed() + similarity * DetailsPane.PERFECT_MATCH_COLOR.getRed()));
		int green = Math.min(255, (int) (DetailsPane.NO_MATCH_COLOR.getGreen() + similarity * DetailsPane.PERFECT_MATCH_COLOR.getGreen()));
		int blue = Math.min(255, (int) (DetailsPane.NO_MATCH_COLOR.getBlue() + similarity * DetailsPane.PERFECT_MATCH_COLOR.getBlue()));

		var color = new Color(red, green, blue, 255);

		if (parentsPoint != null) {
			final int offsetX = left ? -LINE_WIDTH / 2 : LINE_WIDTH / 2;
			final int endX = left ? parentNodePosition.x + parentNode.getWidth() : parentNodePosition.x;
			g.setStroke(new BasicStroke(LINE_WIDTH));
			g.setPaint(color);
			g.drawLine(parentsPoint.x + offsetX, parentsPoint.y, endX, parentsPoint.y);
			g.drawLine(parentsPoint.x + offsetX, parentsPoint.y, parentsPoint.x + offsetX, rootNode.getPosition().y);
			g.setPaint(Color.BLACK);
			g.setStroke(defaultStroke);

			if (lastIndividualsOfPath.containsKey(tuple.getValue1())) {
				final var lineStartX = left ? endX : parentsPoint.x + offsetX;
				final var centerX = lineStartX + Math.abs(parentsPoint.x + offsetX - endX) / 2;

				final var label = String.format("%.2f%%", lastIndividualsOfPath.get(tuple.getValue1()) * 100);
				final var labelWidth = g.getFontMetrics().stringWidth(label);
				var labelX = centerX - labelWidth / 2;
				final var labelY = parentsPoint.y - LINE_WIDTH;

				g.drawString(label, labelX, labelY);
			}
		}
	}

	@Override
	protected Node createNode(final Individual individual, final boolean isClone, final Node parentNode) {
		final var node = new DetailsNode(g, individual, isClone, parentNode, proband, personSimilarities.get(individual.getId()));
		node.init();
		return node;
	}

	@Override
	protected int getEdgeLabelWidth(Node v, Node w) {
		int result = 0;
		if (v != null && w != null && v.getIndividual() != null && w.getIndividual() != null && (includedIndividuals.contains(v.getIndividual().getId()) || includedIndividuals.contains(w.getIndividual().getId()))) {
			result = g.getFontMetrics().stringWidth("100.00% 100.0%");
		}
		return result;
	}
}
