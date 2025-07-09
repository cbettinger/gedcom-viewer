package bettinger.gedcomviewer.views.visualization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.batik.svggen.SVGGraphics2D;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.w3c.dom.svg.SVGDocument;

import bettinger.gedcomviewer.model.Family;
import bettinger.gedcomviewer.model.Individual;

public class DescendantsRenderer extends Renderer {

	private final Set<Family> recursed = new HashSet<>();

	private final List<Quartet<Node, Node, String, Integer>> spouseEdges = new ArrayList<>();
	private final List<Pair<Node, Node>> childEdges = new ArrayList<>();

	public DescendantsRenderer() {
		super(Orientation.TOP_DOWN, false);
	}

	protected DescendantsRenderer(final SVGDocument doc, final SVGGraphics2D g, final boolean renderRootNode) {
		super(doc, g, Orientation.TOP_DOWN, renderRootNode);
	}

	@Override
	protected Node createNodes() {
		return createNodes(proband, createNode()).getParent();
	}

	@Override
	protected void createChildNodes(final Individual individual, final Node node, final int generation) {
		if (generations != 0 && generation == generations) {
			return;
		}

		int index = 0;
		final var families = individual.getFamilies();
		for (final var family : families) {
			final var recurseFamily = family != null && !recursed.contains(family);

			final var spouseNode = createNode(individual.getSpouse(family), node.getParent());
			spouseEdges.add(new Quartet<>(node, spouseNode, getEdgeLabel(family), index++));

			if (family != null && recurseFamily) {
				final var children = family.getChildren();
				for (final var child : children) {
					final var childNode = createNodes(child, spouseNode, generation + 1);
					childEdges.add(new Pair<>(childNode, spouseNode));
				}
				recursed.add(family);
			}
		}
	}

	@Override
	protected int getEdgeLabelWidth(final Node v, final Node w) {
		var result = super.getEdgeLabelWidth(v, w);

		final var edge = spouseEdges.stream().filter(e -> e.getValue1() == v || e.getValue1() == w).findFirst().orElse(null);
		if (edge != null) {
			result = g.getFontMetrics().stringWidth(edge.getValue2());
		}

		return result;
	}

	@Override
	protected void renderEdges() {
		super.renderEdges();

		for (final var edge : spouseEdges) {
			renderEdge(edge.getValue0(), edge.getValue1(), edge.getValue2(), edge.getValue3());
		}

		for (final var edge : childEdges) {
			final var childNode = edge.getValue0();
			final var parentNode = edge.getValue1();

			final var parentX = parentNode.x + parentNode.width / 2;
			final var parentY = parentNode.y + parentNode.height;
			final var centerY = childNode.y - LEVEL_DISTANCE / 2;
			final var childX = childNode.x + childNode.width / 2;
			final var childY = childNode.y;

			g.drawLine(parentX, parentY, parentX, centerY);
			g.drawLine(parentX, centerY, childX, centerY);
			g.drawLine(childX, centerY, childX, childY);
		}
	}
}
