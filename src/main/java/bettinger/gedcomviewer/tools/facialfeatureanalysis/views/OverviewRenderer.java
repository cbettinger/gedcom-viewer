package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Arrays;
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

	private static final float LINE_THICKNESS = 3.5f;
	private static final int LINE_OFFSET = 5;

	private Map<Color, ArrayList<String>> maxSimilarityIndividuals;
	private Map<Pair<String, String>, Set<Color>> maxSimilarityEdges;
	private Map<Color, ArrayList<String>> excludedIndividuals;
	private Map<Color, ArrayList<String>> maxPathIndividuals;

	OverviewRenderer(final Individual proband, final Map<FacialFeature, AnalysisResult> results) {
		this.maxSimilarityIndividuals = new HashMap<>();
		this.maxSimilarityEdges = new HashMap<>();
		this.excludedIndividuals = new HashMap<>();
		this.maxPathIndividuals = new HashMap<>();

		for (final var entry : results.entrySet()) {
			var color = FacialFeature.getColor(entry.getKey());
			var res = entry.getValue();

			var maxPersonSimilarityIDs = res.getMaxIndividualSimilarity().getValue0();
			this.maxSimilarityIndividuals.put(color, maxPersonSimilarityIDs);
			this.maxPathIndividuals.put(color, new ArrayList<>());

			var personSimilarities = res.getIndividualSimilarities();

			var maxSimilarityPaths = res.getMaxLineSimilarity().getValue0();
			for (final var path : maxSimilarityPaths) {
				var pathIDs = path.getIds();
				this.maxPathIndividuals.get(color).addAll(Arrays.asList(pathIDs));

				var tuple = new Pair<String, String>(proband.getId(), pathIDs[0]);
				if (!this.maxSimilarityEdges.containsKey(tuple)) {
					this.maxSimilarityEdges.put(tuple, new HashSet<>());
				}
				this.maxSimilarityEdges.get(tuple).add(color);

				ArrayList<String> exclude = new ArrayList<>();
				for (int i = 0; i < pathIDs.length - 1; i++) {
					if (personSimilarities.get(pathIDs[i + 1]) == null) {
						exclude.add(pathIDs[i + 1]);
					} else {
						exclude.clear();
					}
					tuple = new Pair<String, String>(pathIDs[i], pathIDs[i + 1]);
					if (!this.maxSimilarityEdges.containsKey(tuple)) {
						this.maxSimilarityEdges.put(tuple, new HashSet<>());
					}
					this.maxSimilarityEdges.get(tuple).add(color);
				}
				if (!this.excludedIndividuals.containsKey(color)) {
					this.excludedIndividuals.put(color, new ArrayList<>());
				}
				this.excludedIndividuals.get(color).addAll(exclude);
			}
		}
	}

	@Override
	protected void renderNodes(final Node node) {
		final Stroke defaultStroke = g.getStroke();
		if (renderRootNode || node != rootNode) {
			node.render(node.getPosition().x, node.getPosition().y);
			int colorNum = 0;
			for (var color : maxSimilarityIndividuals.keySet()) {
				if (node.getIndividual() != null && maxSimilarityIndividuals.get(color).contains(node.getIndividual().getId())) {
					int offset = colorNum * (int) LINE_THICKNESS;
					g.setPaint(color);
					g.setStroke(new BasicStroke(LINE_THICKNESS));
					final var rect = node.getRectangle();
					g.drawRect(rect.x - offset, rect.y - offset, rect.width + 2 * offset, rect.height + 2 * offset);
					g.setPaint(Color.BLACK);
					colorNum++;
				}
			}
			g.setStroke(defaultStroke);
		}

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

			for (final var color : maxPathIndividuals.keySet()) {
				var maxPathIds = maxPathIndividuals.get(color);
				var excludedIds = excludedIndividuals.get(color);
				if (considerFather && maxPathIds.contains(fatherNode.getIndividual().getId()) && !excludedIds.contains(fatherNode.getIndividual().getId())) {
					fatherExcludedEverywhere = false;
				}
				if (considerMother && maxPathIds.contains(motherNode.getIndividual().getId()) && !excludedIds.contains(motherNode.getIndividual().getId())) {
					motherExcludedEverywhere = false;
				}
			}

			boolean drawFather = considerFather && !fatherExcludedEverywhere && maxSimilarityEdges.containsKey(new Pair<String, String>(childNode.getIndividual().getId(), fatherNode.getIndividual().getId()));
			boolean drawMother = considerMother && !motherExcludedEverywhere && maxSimilarityEdges.containsKey(new Pair<String, String>(childNode.getIndividual().getId(), motherNode.getIndividual().getId()));

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

	private void renderMaxSimilarityEdge(final Node childNode, final Node parentNode, final Point parentsPoint, final Pair<String, String> tuple, final boolean left) {
		final Paint originalPaint = g.getPaint();
		final Stroke originalStroke = g.getStroke();

		final var edgeColors = maxSimilarityEdges.get(tuple);
		final Point parentNodePosition = parentNode.getPosition();

		int edgeNumber = 0;
		for (final var color : edgeColors) {
			if (parentsPoint != null && !excludedIndividuals.get(color).contains(parentNode.getIndividual().getId())) {
				final int offsetY = LINE_OFFSET * edgeNumber;
				final int offsetX = left ? -offsetY - LINE_OFFSET / 2 : offsetY + LINE_OFFSET / 2;
				final int endX = left ? parentNodePosition.x + LINE_OFFSET : parentNodePosition.x;
				g.setStroke(new BasicStroke(LINE_THICKNESS));
				g.setPaint(color);
				g.drawLine(parentsPoint.x + offsetX, parentsPoint.y + offsetY, endX, parentsPoint.y + offsetY);
				g.drawLine(parentsPoint.x + offsetX, parentsPoint.y + offsetY, parentsPoint.x + offsetX, childNode.getPosition().y);
				edgeNumber++;
			}
		}

		g.setPaint(originalPaint);
		g.setStroke(originalStroke);
	}
}
