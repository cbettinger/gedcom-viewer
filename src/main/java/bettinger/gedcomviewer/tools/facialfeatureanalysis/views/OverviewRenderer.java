package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.javatuples.Pair;

import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.AnalysisResult;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.FacialFeature;
import bettinger.gedcomviewer.views.visualization.AncestorsRenderer;
import bettinger.gedcomviewer.views.visualization.Node;

class OverviewRenderer extends AncestorsRenderer {

	public static final int STROKE_WIDTH = 3;
	public static final Stroke STROKE = new BasicStroke(STROKE_WIDTH);

	private final Map<FacialFeature, AnalysisResult> results;

	private final Map<FacialFeature, ArrayList<String>> maxSimilarIds;
	private final Map<FacialFeature, ArrayList<String>> maxSimilarLineIds;
	private final Map<Pair<String, String>, Set<FacialFeature>> maxSimilarLineEdges;
	private final Map<FacialFeature, ArrayList<String>> excludedIds;	// TODO: ?

	OverviewRenderer(final Map<FacialFeature, AnalysisResult> results) {
		this.results = results;

		this.maxSimilarIds = new HashMap<>();
		this.maxSimilarLineIds = new HashMap<>();
		this.maxSimilarLineEdges = new HashMap<>();
		this.excludedIds = new HashMap<>();
	}

	@Override
	public void render(final Individual proband, final int generations, final Point offset) {
		for (final var entry : results.entrySet()) {
			final var facialFeature = entry.getKey();
			final var result = entry.getValue();

			maxSimilarIds.put(facialFeature, result.getMaxSimilarity().getValue0());
			maxSimilarLineIds.put(facialFeature, new ArrayList<>());

			final var maxSimilarLines = result.getMaxLineSimilarity().getValue0();
			for (final var maxSimilarLine : maxSimilarLines) {
				final var lineIds = maxSimilarLine.getIds();
				maxSimilarLineIds.get(facialFeature).addAll(lineIds);

				Pair<String, String> edge = new Pair<>(proband.getId(), lineIds.get(0));
				maxSimilarLineEdges.computeIfAbsent(edge, _ -> new HashSet<>());
				maxSimilarLineEdges.get(edge).add(facialFeature);

				final ArrayList<String> excluded = new ArrayList<>();
				for (int i = 0; i < lineIds.size() - 1; i++) {
					final var nextLineId = lineIds.get(i + 1);
					if (result.getSimilarities().containsKey(nextLineId)) {
						excluded.add(nextLineId);
					} else {
						excluded.clear();
					}

					edge = new Pair<>(lineIds.get(i), nextLineId);
					maxSimilarLineEdges.computeIfAbsent(edge, _ -> new HashSet<>());
					maxSimilarLineEdges.get(edge).add(facialFeature);
				}

				excludedIds.computeIfAbsent(facialFeature, _ -> new ArrayList<>());
				excludedIds.get(facialFeature).addAll(excluded);
			}
		}

		super.render(proband, generations, offset);
	}

	@Override
	protected Node createNode(final Individual individual, final boolean isClone, final Node parentNode) {
		final var node = new OverviewNode(this, g, individual, isClone, parentNode, maxSimilarIds);
		node.init();
		return node;
	}

	@Override
	protected void renderEdges() {
		for (final var edge : edges) {
			final var childNode = edge.getValue0();
			final var fatherNode = edge.getValue1();
			final var motherNode = edge.getValue2();

			final boolean considerFather = fatherNode != null && fatherNode.getIndividual() != null;
			final boolean considerMother = motherNode != null && motherNode.getIndividual() != null;

			boolean fatherExcludedEverywhere = true;
			boolean motherExcludedEverywhere = true;

			for (final var entry : maxSimilarLineIds.entrySet()) {
				final var facialFeature = entry.getKey();
				final var lineIds = entry.getValue();

				final var excluded = excludedIds.get(facialFeature);
				if (considerFather && lineIds.contains(fatherNode.getIndividual().getId()) && !excluded.contains(fatherNode.getIndividual().getId())) {
					fatherExcludedEverywhere = false;
				}
				if (considerMother && lineIds.contains(motherNode.getIndividual().getId()) && !excluded.contains(motherNode.getIndividual().getId())) {
					motherExcludedEverywhere = false;
				}
			}

			boolean renderEdgeToFather = considerFather && !fatherExcludedEverywhere && maxSimilarLineEdges.containsKey(new Pair<String, String>(childNode.getIndividual().getId(), fatherNode.getIndividual().getId()));
			boolean renderEdgeToMother = considerMother && !motherExcludedEverywhere && maxSimilarLineEdges.containsKey(new Pair<String, String>(childNode.getIndividual().getId(), motherNode.getIndividual().getId()));

			final Point parentsPoint = renderEdge(fatherNode, motherNode);
			if (parentsPoint != null) {
				if (!renderEdgeToFather || !renderEdgeToMother) {
					g.setPaint(Color.BLACK);
					renderEdge(fatherNode, motherNode);
					g.drawLine(parentsPoint.x, parentsPoint.y, parentsPoint.x, childNode.getPosition().y);
				}	// TODO: necc?

				if (renderEdgeToFather) {
					renderMaxSimilarLineEdge(childNode, fatherNode, parentsPoint, new Pair<>(childNode.getIndividual().getId(), fatherNode.getIndividual().getId()), true);
				}

				if (renderEdgeToMother) {
					renderMaxSimilarLineEdge(childNode, motherNode, parentsPoint, new Pair<>(childNode.getIndividual().getId(), motherNode.getIndividual().getId()), false);
				}
			}
		}
	}

	private void renderMaxSimilarLineEdge(final Node childNode, final Node parentNode, final Point parentsPoint, final Pair<String, String> edge, final boolean ancestorIsMale) {
		final var originalPaint = g.getPaint();
		final var originalStroke = g.getStroke();

		g.setStroke(STROKE);

		final var facialFeatures = maxSimilarLineEdges.get(edge);
		final var parentNodePosition = parentNode.getPosition();

		int i = 0;
		for (final var facialFeature : facialFeatures) {
			if (parentsPoint != null && !excludedIds.get(facialFeature).contains(parentNode.getIndividual().getId())) {
				final int offsetY = i++ * EDGE_OFFSET;
				final int offsetX = ancestorIsMale ? -offsetY - EDGE_OFFSET / 2 : offsetY + EDGE_OFFSET / 2;
				final int endX = ancestorIsMale ? parentNodePosition.x + EDGE_OFFSET : parentNodePosition.x;

				g.setPaint(FacialFeature.getColor(facialFeature));
				g.drawLine(parentsPoint.x + offsetX, parentsPoint.y + offsetY, endX, parentsPoint.y + offsetY);
				g.drawLine(parentsPoint.x + offsetX, parentsPoint.y + offsetY, parentsPoint.x + offsetX, childNode.getPosition().y);
			}
		}

		g.setPaint(originalPaint);
		g.setStroke(originalStroke);
	}
}
