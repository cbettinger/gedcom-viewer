package bettinger.gedcomviewer.views.visualization;

import java.util.ArrayList;
import java.util.List;

import org.apache.batik.svggen.SVGGraphics2D;
import org.javatuples.Quartet;
import org.w3c.dom.svg.SVGDocument;

import bettinger.gedcomviewer.model.Individual;

public class AncestorsRenderer extends Renderer {

	final protected List<Quartet<Node, Node, Node, String>> edges = new ArrayList<>();

	public AncestorsRenderer() {
		super(Orientation.BOTTOM_UP, true);
	}

	AncestorsRenderer(final SVGDocument doc, final SVGGraphics2D g, final boolean renderRootNode) {
		super(doc, g, Orientation.BOTTOM_UP, renderRootNode);
	}

	@Override
	void createChildNodes(final Individual individual, final Node node, final int generation) {
		if (generations != 0 && generation == generations) {
			return;
		}

		final var parents = individual.getParents();
		if (parents != null) {
			final var recurseFather = canRecurseFather(individual);
			final var recurseMother = canRecurseMother(individual);

			final var father = individual.getFather();
			final var mother = individual.getMother();

			final var fatherNode = createNode(father, node);
			final var motherNode = createNode(mother, node);

			edges.add(new Quartet<>(node, fatherNode, motherNode, getEdgeLabel(parents)));

			if (recurseFather) {
				createChildNodes(father, fatherNode, generation + 1);
			}
			if (recurseMother) {
				createChildNodes(mother, motherNode, generation + 1);
			}
		}
	}

	boolean canRecurseFather(final Individual individual) {
		final var father = individual.getFather();
		return father != null && !isClone(father);
	}

	boolean canRecurseMother(final Individual individual) {
		final var mother = individual.getMother();
		return mother != null && !isClone(mother);
	}

	@Override
	int getEdgeLabelWidth(final Node v, final Node w) {
		var result = super.getEdgeLabelWidth(v, w);

		final var vParentNode = v == null ? null : v.getParent();
		final var wParentNode = w == null ? null : w.getParent();

		if (vParentNode != null && wParentNode != null && vParentNode == wParentNode) {
			final var child = vParentNode.getIndividual();
			final var parents = child.getParents();

			if (parents != null) {
				result = g.getFontMetrics().stringWidth(getEdgeLabel(parents));
			}
		}

		return result;
	}

	@Override
	public void renderEdges() {
		super.renderEdges();

		for (final var edge : edges) {
			final var parentsPoint = renderEdge(edge.getValue1(), edge.getValue2(), edge.getValue3());
			if (parentsPoint != null) {
				g.drawLine(parentsPoint.x, parentsPoint.y, parentsPoint.x, edge.getValue0().y);
			}
		}
	}
}
