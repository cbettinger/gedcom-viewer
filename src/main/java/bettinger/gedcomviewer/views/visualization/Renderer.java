package bettinger.gedcomviewer.views.visualization;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.svg.SVGDocument;

import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.model.Date;
import bettinger.gedcomviewer.model.Family;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.model.Location;
import bettinger.gedcomviewer.model.Structure;
import bettinger.gedcomviewer.utils.SVGUtils;

abstract class Renderer {

	protected static final int LEVEL_DISTANCE = 25;

	private static final int EDGE_OFFSET = 5;
	private static final int EDGE_LABEL_PADDING = 10;

	private static final int MINIMAL_CHILDREN_GAP = 2 * EDGE_LABEL_PADDING;
	private static final int SUBTREE_DISTANCE = 2 * EDGE_LABEL_PADDING;

	protected final SVGDocument doc;
	protected final SVGGraphics2D g;

	protected final Orientation orientation;
	protected final boolean renderRootNode;

	protected Node rootNode;
	protected Individual proband;
	protected Node probandNode;

	protected int generations;

	private final Set<Individual> created;
	private int individualCount;

	private int maximalNodeHeight;
	private int maximalDepth;
	private int minimalX;

	protected Renderer() {
		this(Orientation.BOTTOM_UP, true);
	}

	protected Renderer(final Orientation orientation, final boolean renderRootNode) {
		this(SVGUtils.createDocument(), orientation, renderRootNode);
	}

	protected Renderer(final SVGDocument doc, final Orientation orientation, final boolean renderRootNode) {
		this(doc, SVGUtils.createGraphics(doc), orientation, renderRootNode);
	}

	protected Renderer(final SVGDocument doc, final SVGGraphics2D g, final Orientation orientation, final boolean renderRootNode) {
		this.doc = doc;
		this.g = g;

		this.orientation = orientation;
		this.renderRootNode = renderRootNode;

		this.rootNode = null;
		this.proband = null;
		this.probandNode = null;

		this.generations = 0;

		this.created = new HashSet<>();
		this.individualCount = 0;

		this.maximalNodeHeight = 0;
		this.maximalDepth = 0;
		this.minimalX = Integer.MAX_VALUE;
	}

	public Node getProbandNode() {
		return probandNode;
	}

	protected int getIndividualCount() {
		return renderRootNode ? individualCount : individualCount - 1;
	}

	public void render(final Individual proband) {
		render(proband, 0);
	}

	public void render(final Individual proband, final int generations) {
		render(proband, generations, null);
	}

	public void render(final Individual proband, final int generations, final Point offset) {
		this.proband = proband;

		this.generations = Math.max(0, generations);

		if (this.proband != null) {
			this.rootNode = createNodes();

			layoutNodes();
			setPositions();

			moveToOrigin();
			if (offset != null) {
				moveNodes(offset);
			}

			renderEdges();
			renderNodes();
		}
	}

	protected Node createNodes() {
		return createNodes(proband, null);
	}

	protected Node createNodes(final Individual individual, final Node parentNode) {
		return createNodes(individual, parentNode, 1);
	}

	protected Node createNodes(final Individual individual, final Node parentNode, final int generation) {
		final var node = createNode(individual, parentNode);

		if (individual != null) {
			createChildNodes(individual, node, generation);
		}

		return node;
	}

	protected Node createNode() {
		return createNode(null);
	}

	protected Node createNode(final Individual individual) {
		return createNode(individual, null);
	}

	protected Node createNode(final Individual individual, final Node parentNode) {
		final var isClone = isClone(individual);

		final var node = createNode(individual, isClone, parentNode);

		if (!isClone) {
			individualCount++;
		}

		if (individual != null) {
			created.add(individual);
		}

		if (individual == proband && probandNode == null) {
			probandNode = node;
		}

		maximalNodeHeight = Math.max(maximalNodeHeight, node.height);
		maximalDepth = Math.max(maximalDepth, node.getDepth());

		return node;
	}

	protected Node createNode(final Individual individual, final boolean isClone, final Node parentNode) {
		return new Node(g, individual, isClone, parentNode);
	}

	protected boolean isClone(final Individual individual) {
		return individual != null && created.contains(individual);
	}

	protected void createChildNodes(final Individual individual, final Node node, final int generation) {}

	private void layoutNodes() {
		if (rootNode != null) {
			layoutNodes(rootNode);
		}
	}

	private void layoutNodes(final Node node) {
		if (!node.hasChildren()) {
			if (node.getChildIndex() > 0) {
				node.setPrelim(node.getParent().getChild(node.getChildIndex() - 1).getPrelim() + getDistance(node, node.getParent().getChild(node.getChildIndex() - 1)));
			} else {
				node.setPrelim(0);
			}
		} else {
			var defaultAncestor = node.getChild(0);

			for (final var child : node.getChildren()) {
				layoutNodes(child);
				defaultAncestor = apportion(child, defaultAncestor);
			}

			executeShifts(node);

			final var midpoint = (node.getChild(0).getPrelim() + node.getChild(node.getChildCount() - 1).getPrelim()) / 2;

			if (node.getChildIndex() > 0) {
				node.setPrelim(node.getParent().getChild(node.getChildIndex() - 1).getPrelim() + getDistance(node, node.getParent().getChild(node.getChildIndex() - 1)));
				node.setMod(node.getPrelim() - midpoint);
			} else {
				node.setPrelim(midpoint);
			}
		}
	}

	private Node apportion(final Node node, Node defaultAncestor) {
		if (node.getChildIndex() > 0) {
			var vinsideright = node;
			var voutsideright = node;
			var vinsideleft = node.getParent().getChild(node.getChildIndex() - 1);
			var voutsideleft = vinsideright.getParent().getChild(0);
			var sumiright = vinsideright.getMod();
			var sumoright = voutsideright.getMod();
			var sumileft = vinsideleft.getMod();
			var sumoleft = voutsideleft.getMod();

			while (nextRight(vinsideleft) != null && nextLeft(vinsideright) != null) {
				vinsideleft = nextRight(vinsideleft);
				vinsideright = nextLeft(vinsideright);
				voutsideleft = nextLeft(voutsideleft);
				voutsideright = nextRight(voutsideright);
				voutsideright.setAncestor(node);

				final var shift = (vinsideleft.getPrelim() + sumileft) - (vinsideright.getPrelim() + sumiright) + getDistance(vinsideleft, vinsideright) + SUBTREE_DISTANCE;
				if (shift > 0) {
					moveSubtree(ancestor(vinsideleft, node, defaultAncestor), node, shift);
					sumiright += shift;
					sumoright += shift;
				}

				sumileft += vinsideleft.getMod();
				sumiright += vinsideright.getMod();
				sumoleft += voutsideleft.getMod();
				sumoright += voutsideright.getMod();
			}

			if (nextRight(vinsideleft) != null && nextRight(voutsideright) == null) {
				voutsideright.setThread(nextRight(vinsideleft));
				voutsideright.setMod(voutsideright.getMod() + sumileft - sumoright);
			}
			if (nextLeft(vinsideright) != null && nextLeft(voutsideleft) == null) {
				voutsideleft.setThread(nextLeft(vinsideright));
				voutsideleft.setMod(voutsideleft.getMod() + sumiright - sumoleft);
				defaultAncestor = node;
			}
		}

		return defaultAncestor;
	}

	private Node nextLeft(final Node node) {
		return node.hasChildren() ? node.getChild(0) : node.getThread();
	}

	private Node nextRight(final Node node) {
		return node.hasChildren() ? node.getChild(node.getChildCount() - 1) : node.getThread();
	}

	private void moveSubtree(final Node winside, final Node woutside, final int shift) {
		final var subtrees = woutside.getChildIndex() - winside.getChildIndex();

		woutside.setChange(woutside.getChange() - (shift / subtrees));
		woutside.setShift(woutside.getShift() + shift);
		winside.setChange(winside.getChange() + (shift / subtrees));
		woutside.setPrelim(woutside.getPrelim() + shift);
		woutside.setMod(woutside.getMod() + shift);
	}

	private Node ancestor(final Node vinsideleft, final Node node, final Node defaultAncestor) {
		return vinsideleft.getAncestor().getParent() == node.getParent() ? vinsideleft.getAncestor() : defaultAncestor;
	}

	private void executeShifts(final Node node) {
		var shift = node.width;
		var change = 0;

		for (var i = node.getChildCount() - 1; i >= 0; i--) {
			final var w = node.getChild(i);
			w.setPrelim(w.getPrelim() + shift);
			w.setMod(w.getMod() + shift);
			change += w.getChange();
			shift = shift + w.getShift() + change;
		}
	}

	private int getDistance(final Node v, final Node w) {
		return (v.width + w.width) / 2 + MINIMAL_CHILDREN_GAP + getEdgeLabelWidth(v, w);
	}

	protected int getEdgeLabelWidth(@SuppressWarnings("java:S1172") final Node v, final Node w) {
		return 0;
	}

	private void setPositions() {
		if (rootNode != null) {
			setPositions(rootNode);
		}
	}

	private void setPositions(final Node node) {
		setPositions(node, 0, 0);
	}

	private void setPositions(final Node node, final int m, final int depth) {
		node.x = node.getPrelim() + m - (node.width / 2);
		minimalX = Math.min(minimalX, node.x);

		node.y = ((orientation == Orientation.BOTTOM_UP ? -1 : 1) * (orientation == Orientation.BOTTOM_UP ? depth : (renderRootNode ? depth : depth - 1)) * (maximalNodeHeight + LEVEL_DISTANCE));

		for (final var child : node.getChildren()) {
			setPositions(child, m + node.getMod(), depth + 1);
		}
	}

	private void moveToOrigin() {
		if (rootNode != null) {
			moveToOrigin(rootNode);
		}
	}

	private void moveToOrigin(final Node node) {
		node.x -= minimalX;
		node.y += ((orientation == Orientation.BOTTOM_UP ? 1 : 0) * maximalDepth * (maximalNodeHeight + LEVEL_DISTANCE));

		for (final var child : node.getChildren()) {
			moveToOrigin(child);
		}
	}

	private void moveNodes(final Point offset) {
		if (rootNode != null) {
			final var probandPosition = getProbandNode().getPosition();
			moveNodes(rootNode, new Point(offset.x - probandPosition.x, offset.y - probandPosition.y));
		}
	}

	private void moveNodes(final Node node, final Point offset) {
		node.x += offset.x;
		node.y += offset.y;

		for (final var child : node.getChildren()) {
			moveNodes(child, offset);
		}
	}

	protected void renderNodes() {
		if (rootNode != null) {
			renderNodes(rootNode);
		}
	}

	protected void renderNodes(final Node node) {
		if (renderRootNode || node != rootNode) {
			node.render(node.x, node.y);
		}

		for (final var child : node.getChildren()) {
			renderNodes(child);
		}
	}

	protected void renderEdges() {
		g.setFont(Node.DEFAULT_FONT);
	}

	protected Point renderEdge(final Node leftNode, final Node rightNode) {
		return renderEdge(leftNode, rightNode, null, 0, true);
	}

	protected Point renderEdge(final Node leftNode, final Node rightNode, final String label) {
		return renderEdge(leftNode, rightNode, label, 0, true);
	}

	protected Point renderEdge(final Node leftNode, final Node rightNode, final int index) {
		return renderEdge(leftNode, rightNode, null, index, false);
	}

	protected Point renderEdge(final Node leftNode, final Node rightNode, final String label, final int index) {
		return renderEdge(leftNode, rightNode, label, index, false);
	}

	protected Point renderEdge(final Node leftNode, final Node rightNode, final String label, final int index, final boolean centerLabel) {
		var centerX = 0;
		var lineY = 0;

		if (leftNode != null && rightNode != null) {
			centerX = leftNode.x + leftNode.width + ((rightNode.x - leftNode.x - leftNode.width) / 2);

			final var labelWidth = g.getFontMetrics().stringWidth(label);
			final var labelX = centerLabel ? centerX - labelWidth / 2 : rightNode.x - EDGE_LABEL_PADDING - labelWidth;
			final var labelY = leftNode.y + Node.MINIMAL_HEIGHT / 2 + (index * EDGE_OFFSET);

			lineY = labelY + EDGE_OFFSET;

			g.drawLine(leftNode.x + leftNode.width, lineY, rightNode.x, lineY);

			if (label != null && !label.isEmpty()) {
				g.drawString(label, labelX, labelY);
			}
		}

		return new Point(centerX, lineY);
	}

	@Override
	public String toString() {
		SVGUtils.setSize(doc, g);

		return SVGUtils.toString(doc);
	}

	protected static String getEdgeLabel(final Family family) {
		return family != null ? formatDateAndPlace(Structure.MARRIAGE_SIGN, family.getMarriageDate(), null, null) : "";
	}

	protected static String formatDateAndPlace(final String sign, final Date date, final String place, final Location location) {
		final var sb = new StringBuilder();

		final var dateStr = date != null ? date.toString() : "";
		final var placeStr = location != null ? location.toString() : (place != null ? place : "");

		if (!dateStr.isEmpty() || !placeStr.isEmpty()) {
			sb.append(String.format(Format.TRAILING_SPACE, sign));
		}

		var wasAppended = false;

		if (!dateStr.isEmpty()) {
			sb.append(dateStr);
			wasAppended = true;
		}

		if (!placeStr.isEmpty()) {
			if (wasAppended) {
				sb.append(" | ");
			}
			sb.append(placeStr);
		}

		return sb.toString();
	}

	protected enum Orientation {
		BOTTOM_UP, TOP_DOWN;
	}
}
