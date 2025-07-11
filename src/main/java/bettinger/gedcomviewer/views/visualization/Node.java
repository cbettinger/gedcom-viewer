package bettinger.gedcomviewer.views.visualization;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.batik.svggen.SVGGraphics2D;

import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.model.Structure;

public class Node {

	protected static final int MINIMAL_WIDTH = 200;
	protected static final int MINIMAL_HEIGHT = 100;

	protected static final int PADDING = 10;

	protected static final int PORTRAIT_HEIGHT = MINIMAL_HEIGHT - 2 * PADDING;

	protected static final Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 12);
	protected static final Font BOLD_FONT = DEFAULT_FONT.deriveFont(Font.BOLD);

	protected static final Color UNKNOWN_COLOR = Color.LIGHT_GRAY;
	protected static final Color MALE_COLOR = new Color(173, 215, 229);
	protected static final Color MALE_CLONE_COLOR = new Color(206, 221, 226);
	protected static final Color FEMALE_COLOR = new Color(255, 193, 204);
	protected static final Color FEMALE_CLONE_COLOR = new Color(255, 232, 236);

	protected final SVGGraphics2D g;

	protected final Individual individual;
	protected final boolean isClone;

	protected final Node parent;
	protected final List<Node> children;

	protected final int depth;

	protected int x;
	protected int y;
	protected int width;
	protected int height;

	private int mod;
	private Node thread;
	private int prelim;
	private Node ancestor;
	private int change;
	private int shift;

	protected Image portrait;
	protected int portraitWidth;

	protected List<String> text;
	protected int lineHeight;

	protected Node(final SVGGraphics2D g, final Individual individual) {
		this(g, individual, false);
	}

	protected Node(final SVGGraphics2D g, final Individual individual, final boolean isClone) {
		this(g, individual, isClone, null);
	}

	protected Node(final SVGGraphics2D g, final Individual individual, final boolean isClone, final Node parent) {
		this.g = g;

		this.individual = individual;
		this.isClone = isClone;

		if (parent != null) {
			parent.children.add(this);
		}
		this.parent = parent;
		this.children = new ArrayList<>();

		this.depth = parent == null ? 0 : parent.depth + 1;

		this.mod = 0;
		this.thread = null;
		this.prelim = 0;
		this.ancestor = this;
		this.change = 0;
		this.shift = 0;
	}

	protected void init() {
		text = getTextLines();

		g.setFont(BOLD_FONT);
		final var fontMetrics = g.getFontMetrics();
		final var maximalLineWidth = fontMetrics.stringWidth(text.stream().max(Comparator.comparing(fontMetrics::stringWidth)).orElse(""));
		lineHeight = fontMetrics.getHeight();

		portrait = getPortrait();
		portraitWidth = getPortraitWidth(portrait);

		width = Math.max(MINIMAL_WIDTH, maximalLineWidth + 3 * PADDING + (portrait == null ? 0 : portraitWidth + PADDING));
		height = Math.max(MINIMAL_HEIGHT, text.size() * (lineHeight + PADDING) + 2 * PADDING);
	}

	public Point getPosition() {
		return new Point(x, y);
	}

	public Rectangle getRectangle() {
		return new Rectangle(x, y, width, height);
	}

	public int getWidth() {
		return width;
	}

	public Individual getIndividual() {
		return individual;
	}

	protected Node getParent() {
		return parent;
	}

	protected int getDepth() {
		return depth;
	}

	protected int getChildIndex() {
		return getParent() == null ? 0 : getParent().getChildren().indexOf(this);
	}

	protected boolean hasChildren() {
		return !getChildren().isEmpty();
	}

	protected int getChildCount() {
		return getChildren().size();
	}

	protected Node getChild(final int index) {
		return getChildren().get(index);
	}

	public List<Node> getChildren() {
		return children;
	}

	int getMod() {
		return mod;
	}

	void setMod(final int value) {
		mod = value;
	}

	Node getThread() {
		return thread;
	}

	void setThread(final Node value) {
		thread = value;
	}

	int getPrelim() {
		return prelim;
	}

	void setPrelim(final int value) {
		prelim = value;
	}

	Node getAncestor() {
		return ancestor;
	}

	void setAncestor(final Node value) {
		ancestor = value;
	}

	int getChange() {
		return change;
	}

	void setChange(final int value) {
		change = value;
	}

	int getShift() {
		return shift;
	}

	void setShift(final int value) {
		shift = value;
	}

	public void render(final int x, final int y) {
		this.x = x;
		this.y = y;

		var fillColor = UNKNOWN_COLOR;
		if (isClone) {
			fillColor = individual.isMale() ? MALE_CLONE_COLOR : FEMALE_CLONE_COLOR;
		} else if (individual != null) {
			fillColor = individual.isMale() ? MALE_COLOR : FEMALE_COLOR;
		}

		g.setPaint(fillColor);
		g.fill(getRectangle());

		g.setPaint(Color.BLACK);

		var nextY = y;

		g.setFont(BOLD_FONT);

		renderPortraits();

		final int textPositionX = getTextPositionX();
		for (final var line : text) {
			nextY += (PADDING + lineHeight);
			g.drawString(line, textPositionX, nextY);
			g.setFont(DEFAULT_FONT);
		}
	}

	protected void renderPortraits() {
		if (portrait != null) {
			g.drawImage(portrait, x + PADDING, y + PADDING, portraitWidth, PORTRAIT_HEIGHT, null);
		}
	}

	protected Image getPortrait() {
		Image result = null;

		if (individual != null) {
			final var primaryImage = individual.getPrimaryImage(true);
			if (primaryImage != null && primaryImage.exists()) {
				result = individual.getClippedImage(primaryImage, -1, PORTRAIT_HEIGHT);
			}
		}

		return result;
	}

	protected int getPortraitWidth(final Image image) {
		return image == null ? 0 : image.getWidth(null);
	}

	protected List<String> getTextLines() {
		final List<String> result = new ArrayList<>();

		result.add(getFirstTextLine());

		if (individual != null) {
			final var birthLine = Renderer.formatDateAndPlace(Structure.BIRTH_SIGN, individual.getBirthDate(), individual.getBirthPlace(), individual.getBirthLocation());
			result.add(birthLine);

			if (birthLine.isEmpty()) {
				final var baptismLine = Renderer.formatDateAndPlace(Structure.BAPTISM_SIGN, individual.getBaptismDate(), individual.getBaptismPlace(), individual.getBaptismLocation());
				result.add(baptismLine);
			}

			final var deathLine = Renderer.formatDateAndPlace(Structure.DEATH_SIGN, individual.getDeathDate(), individual.getDeathPlace(), individual.getDeathLocation());
			result.add(deathLine);

			if (deathLine.isEmpty()) {
				final var burialLine = Renderer.formatDateAndPlace(Structure.BURIAL_SIGN, individual.getBurialDate(), individual.getBurialPlace(), individual.getBurialLocation());
				result.add(burialLine);
			}
		}

		return result.stream().filter(l -> !l.isEmpty()).toList();
	}

	protected String getFirstTextLine() {
		return individual == null ? Structure.UNKNOWN_STRING : individual.getNickname().isEmpty() ? individual.getName() : String.format(Format.STRING_WITH_QUOTED_SUFFIX, individual.getName(), individual.getNickname());
	}

	protected int getTextPositionX() {
		return x + PADDING + (portrait == null ? 0 : this.portraitWidth + PADDING);
	}
}
