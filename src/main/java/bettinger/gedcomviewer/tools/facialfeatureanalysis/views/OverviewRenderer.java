package bettinger.gedcomviewer.tools.facialfeatureanalysis.views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.javatuples.Pair;

import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.model.FacialFeatureAnalysisResult;
import bettinger.gedcomviewer.tools.facialfeatureanalysis.model.FacialFeature;
import bettinger.gedcomviewer.views.visualization.AncestorsRenderer;
import bettinger.gedcomviewer.views.visualization.Node;

class OverviewRenderer extends AncestorsRenderer {

	private HashMap<Color, ArrayList<String>> maxSimilarityIndividuals;
	private HashMap<Pair<String, String>, Set<Color>> maxSimilarityEdges;
	private final int LINE_OFFSET = 5;
	private final Color DEFAULT_LINE_COLOR = Color.BLACK;
	private final float LINE_THICKNESS = 3.5f;
	private HashMap<Color, ArrayList<String>> excludedIndividuals;
	private HashMap<Color, ArrayList<String>> maxPathIndividuals;

	OverviewRenderer(final Individual proband, final Map<FacialFeature, FacialFeatureAnalysisResult> results) {
		super();
		this.maxSimilarityIndividuals = new HashMap<>();
		this.maxSimilarityEdges = new HashMap<>();
		this.excludedIndividuals = new HashMap<>();
		this.maxPathIndividuals = new HashMap<>();

		for (final var entry : results.entrySet()) {
			var color = FacialFeature.getColor(entry.getKey());
			var res = entry.getValue();

			var maxPersonSimilarityIDs = res.getMaxPersonSimilarity().getValue0();
			this.maxSimilarityIndividuals.put(color, maxPersonSimilarityIDs);
			this.maxPathIndividuals.put(color, new ArrayList<>());

			var personSimilarities = res.getPersonSimilarities();

			var maxSimilarityPaths = res.getMaxPathSimilarity().getValue0();
			for (final var path : maxSimilarityPaths) {
				var pathIDs = path.getAncestorIDs();
				this.maxPathIndividuals.get(color).addAll(Arrays.asList(pathIDs));

				var tuple = new Pair<String, String>(proband.getId(), pathIDs[0]);
				if (!this.maxSimilarityEdges.containsKey(tuple)) {
					this.maxSimilarityEdges.put(tuple, new HashSet<Color>());
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
						this.maxSimilarityEdges.put(tuple, new HashSet<Color>());
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
	protected void renderEdges() {
		for (final var edge : edges) {
			final var rootNode = edge.getValue0();
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

			boolean drawLeft = considerFather && !fatherExcludedEverywhere && maxSimilarityEdges.containsKey(new Pair<String, String>(rootNode.getIndividual().getId(), fatherNode.getIndividual().getId()));
			boolean drawRight = considerMother && !motherExcludedEverywhere && maxSimilarityEdges.containsKey(new Pair<String, String>(rootNode.getIndividual().getId(), motherNode.getIndividual().getId()));

			g.setPaint(Color.WHITE);
			final Point parentsPoint = renderEdge(fatherNode, motherNode);
			if (parentsPoint != null) {
				if (!drawLeft || !drawRight) {
					g.setPaint(DEFAULT_LINE_COLOR);
					renderEdge(fatherNode, motherNode);
					g.drawLine(parentsPoint.x, parentsPoint.y, parentsPoint.x, rootNode.getPosition().y);
				}
				if (drawLeft) {
					final Pair<String, String> tuple = new Pair<String, String>(rootNode.getIndividual().getId(), fatherNode.getIndividual().getId());
					renderMaxSimilarityEdge(rootNode, fatherNode, parentsPoint, tuple, true);
				}
				if (drawRight) {
					final Pair<String, String> tuple = new Pair<String, String>(rootNode.getIndividual().getId(), motherNode.getIndividual().getId());
					renderMaxSimilarityEdge(rootNode, motherNode, parentsPoint, tuple, false);
				}
			}
		}
	}

	private void renderMaxSimilarityEdge(final Node rootNode, final Node parentNode, final Point parentsPoint, final Pair<String, String> tuple, final boolean left) {
		final var edgeColors = maxSimilarityEdges.get(tuple);
		final Point parentNodePosition = parentNode.getPosition();

		final Stroke defaultStroke = g.getStroke();

		int edgeNumber = 0;
		for (final var color : edgeColors) {
			if (parentsPoint != null && !excludedIndividuals.get(color).contains(parentNode.getIndividual().getId())) {
				final int offsetY = LINE_OFFSET * edgeNumber;
				final int offsetX = left ? -offsetY - LINE_OFFSET / 2 : offsetY + LINE_OFFSET / 2;
				final int endX = left ? parentNodePosition.x + LINE_OFFSET : parentNodePosition.x;
				g.setStroke(new BasicStroke(LINE_THICKNESS));
				g.setPaint(color);
				g.drawLine(parentsPoint.x + offsetX, parentsPoint.y + offsetY, endX, parentsPoint.y + offsetY);
				g.drawLine(parentsPoint.x + offsetX, parentsPoint.y + offsetY, parentsPoint.x + offsetX, rootNode.getPosition().y);
				edgeNumber++;
			}
		}
		g.setPaint(DEFAULT_LINE_COLOR);
		g.setStroke(defaultStroke);
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
					final Rectangle rect = node.getRectangle();
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
}
