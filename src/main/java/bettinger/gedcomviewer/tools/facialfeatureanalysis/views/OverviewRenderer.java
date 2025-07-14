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
	private static final int STROKE_WIDTH = 3;
	private static final Stroke STROKE = new BasicStroke(STROKE_WIDTH);

	private static final int EDGE_OFFSET = 5;

	private final Map<Color, ArrayList<String>> maxIndividualSimilarityIds;
	private final Map<Color, ArrayList<String>> maxLineSimilarityIds;
	private final Map<Pair<String, String>, Set<Color>> maxLineSimilarityEdges;
	private final Map<Color, ArrayList<String>> excludedIndividuals;
	private final Map<FacialFeature, AnalysisResult> results;

	OverviewRenderer(final Individual proband, final Map<FacialFeature, AnalysisResult> results) {
		this.maxIndividualSimilarityIds = new HashMap<>();
		this.maxLineSimilarityIds = new HashMap<>();
		this.maxLineSimilarityEdges = new HashMap<>();
		this.excludedIndividuals = new HashMap<>();
		this.results = results;
	}

	@Override
	public void render(final Individual proband, final int generations, final Point offset) {
		for (final var entry : results.entrySet()) {
			final var color = FacialFeature.getColor(entry.getKey());
			final var result = entry.getValue();

			maxIndividualSimilarityIds.put(color, result.getMaxIndividualSimilarity().getValue0());

			maxLineSimilarityIds.put(color, new ArrayList<>());

			final var maxLineSimilarityLines = result.getMaxLineSimilarity().getValue0();
			for (final var maxLineSimilarityLine : maxLineSimilarityLines) {
				final var maxLineSimilarityLineIds = maxLineSimilarityLine.getIds();
				maxLineSimilarityIds.get(color).addAll(maxLineSimilarityLineIds);

				final Pair<String, String> k1 = new Pair<>(proband.getId(), maxLineSimilarityLineIds.get(0));
				maxLineSimilarityEdges.computeIfAbsent(k1, _ -> new HashSet<>());
				maxLineSimilarityEdges.get(k1).add(color);

				final ArrayList<String> excludedIds = new ArrayList<>();

				for (int i = 0; i < maxLineSimilarityLineIds.size() - 1; i++) {
					if (result.getIndividualSimilarities().get(maxLineSimilarityLineIds.get(i + 1)) == null) {
						excludedIds.add(maxLineSimilarityLineIds.get(i + 1));
					} else {
						excludedIds.clear();
					}

					final Pair<String, String> k2 = new Pair<>(maxLineSimilarityLineIds.get(i), maxLineSimilarityLineIds.get(i + 1));
					maxLineSimilarityEdges.computeIfAbsent(k2, _ -> new HashSet<>());
					maxLineSimilarityEdges.get(k2).add(color);
				}

				excludedIndividuals.computeIfAbsent(color, _ -> new ArrayList<>());
				excludedIndividuals.get(color).addAll(excludedIds);
			}
		}

		super.render(proband, generations, offset);
	}

	@Override
	protected void renderNodes(final Node node) {
		final var originalPaint = g.getPaint();
		final var originalStroke = g.getStroke();

		g.setStroke(STROKE);

		if (renderRootNode || node != rootNode) {
			node.render(node.getPosition().x, node.getPosition().y);

			int colorIndex = 0;
			for (final var borderColor : maxIndividualSimilarityIds.keySet()) {
				if (node.getIndividual() != null && maxIndividualSimilarityIds.get(borderColor).contains(node.getIndividual().getId())) {
					final var border = node.getRectangle();
					final int offset = colorIndex * STROKE_WIDTH;

					g.setPaint(borderColor);
					g.drawRect(border.x - offset, border.y - offset, border.width + 2 * offset, border.height + 2 * offset);

					colorIndex++;
				}
			}
		}

		g.setPaint(originalPaint);
		g.setStroke(originalStroke);

		for (final var child : node.getChildren()) {
			renderNodes(child);
		}
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

			for (final var color : maxLineSimilarityIds.keySet()) {
				final var maxPathIds = maxLineSimilarityIds.get(color);
				final var excludedIndividualsIds = excludedIndividuals.get(color);

				if (considerFather && maxPathIds.contains(fatherNode.getIndividual().getId()) && !excludedIndividualsIds.contains(fatherNode.getIndividual().getId())) {
					fatherExcludedEverywhere = false;
				}
				if (considerMother && maxPathIds.contains(motherNode.getIndividual().getId()) && !excludedIndividualsIds.contains(motherNode.getIndividual().getId())) {
					motherExcludedEverywhere = false;
				}
			}

			boolean drawFather = considerFather && !fatherExcludedEverywhere && maxLineSimilarityEdges.containsKey(new Pair<String, String>(childNode.getIndividual().getId(), fatherNode.getIndividual().getId()));
			boolean drawMother = considerMother && !motherExcludedEverywhere && maxLineSimilarityEdges.containsKey(new Pair<String, String>(childNode.getIndividual().getId(), motherNode.getIndividual().getId()));

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
					renderMaxSimilarityEdge(childNode, fatherNode, parentsPoint, tuple, true);
				}

				if (drawMother) {
					final Pair<String, String> tuple = new Pair<>(childNode.getIndividual().getId(), motherNode.getIndividual().getId());
					renderMaxSimilarityEdge(childNode, motherNode, parentsPoint, tuple, false);
				}
			}
		}
	}

	private void renderMaxSimilarityEdge(final Node childNode, final Node parentNode, final Point parentsPoint, final Pair<String, String> tuple, final boolean maleLine) {
		final var originalPaint = g.getPaint();
		final var originalStroke = g.getStroke();

		g.setStroke(STROKE);

		final var edgeColors = maxLineSimilarityEdges.get(tuple);
		final var parentNodePosition = parentNode.getPosition();

		int edgeIndex = 0;
		for (final var edgeColor : edgeColors) {
			if (parentsPoint != null && !excludedIndividuals.get(edgeColor).contains(parentNode.getIndividual().getId())) {
				final int offsetY = edgeIndex * EDGE_OFFSET;
				final int offsetX = maleLine ? -offsetY - EDGE_OFFSET / 2 : offsetY + EDGE_OFFSET / 2;
				final int endX = maleLine ? parentNodePosition.x + EDGE_OFFSET : parentNodePosition.x;

				g.setPaint(edgeColor);
				g.drawLine(parentsPoint.x + offsetX, parentsPoint.y + offsetY, endX, parentsPoint.y + offsetY);
				g.drawLine(parentsPoint.x + offsetX, parentsPoint.y + offsetY, parentsPoint.x + offsetX, childNode.getPosition().y);

				edgeIndex++;
			}
		}

		g.setPaint(originalPaint);
		g.setStroke(originalStroke);
	}
}
