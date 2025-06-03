package bettinger.gedcomviewer.views.visualization;

import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.batik.svggen.SVGGraphics2D;

import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.model.Individual;
import bettinger.gedcomviewer.model.Structure;

class Node {

	static final int MINIMAL_WIDTH = 200;
	static final int MINIMAL_HEIGHT = 100;

	private static final int PADDING = 10;

	private static final int PORTRAIT_HEIGHT = MINIMAL_HEIGHT - 2 * PADDING;

	private static final Color UNKNOWN_COLOR = Color.LIGHT_GRAY;
	private static final Color MALE_COLOR = new Color(173, 215, 229);
	private static final Color MALE_CLONE_COLOR = new Color(206, 221, 226);
	private static final Color FEMALE_COLOR = new Color(255, 193, 204);
	private static final Color FEMALE_CLONE_COLOR = new Color(255, 232, 236);

	int x;
	int y;
	int width;
	int height;

	private final SVGGraphics2D g;

	private final Individual individual;
	private final boolean isClone;

	private final Node parent;
	private final List<Node> children;

	private final int depth;

	private final Image portrait;
	private final int portraitWidth;

	private final List<String> text;
	private final int lineHeight;

	private int mod;
	private Node thread;
	private int prelim;
	private Node ancestor;
	private int change;
	private int shift;

	Node(final SVGGraphics2D g, final Individual individual) {
		this(g, individual, false);
	}

	Node(final SVGGraphics2D g, final Individual individual, final boolean isClone) {
		this(g, individual, isClone, null);
	}

	Node(final SVGGraphics2D g, final Individual individual, final boolean isClone, final Node parent) {
		this.g = g;

		this.individual = individual;
		this.isClone = isClone;

		if (parent != null) {
			parent.children.add(this);
		}
		this.parent = parent;
		this.children = new ArrayList<>();

		this.depth = parent == null ? 0 : parent.getDepth() + 1;

		this.mod = 0;
		this.thread = null;
		this.prelim = 0;
		this.ancestor = this;
		this.change = 0;
		this.shift = 0;

		this.text = getTextLines();

		this.width = MINIMAL_WIDTH;
		this.height = MINIMAL_HEIGHT;

		this.portrait = getPortrait();
		this.portraitWidth = this.portrait == null ? 0 : this.portrait.getWidth(null);

		g.setFont(Renderer.BOLD_FONT);
		final var fontMetrics = g.getFontMetrics();
		final var maximalLineWidth = fontMetrics.stringWidth(text.stream().max(Comparator.comparing(fontMetrics::stringWidth)).orElse(""));
		this.lineHeight = fontMetrics.getHeight();

		this.width = Math.max(MINIMAL_WIDTH, maximalLineWidth + 3 * PADDING + (this.portrait == null ? 0 : this.portraitWidth + PADDING));
		this.height = Math.max(MINIMAL_HEIGHT, text.size() * (lineHeight + PADDING) + 2 * PADDING);
	}

	Point getPosition() {
		return new Point(x, y);
	}

	Rectangle getRectangle() {
		return new Rectangle(x, y, width, height);
	}

	Individual getIndividual() {
		return individual;
	}

	Node getParent() {
		return parent;
	}

	int getDepth() {
		return depth;
	}

	int getChildIndex() {
		return getParent() == null ? 0 : getParent().getChildren().indexOf(this);
	}

	boolean hasChildren() {
		return !getChildren().isEmpty();
	}

	int getChildCount() {
		return getChildren().size();
	}

	Node getChild(final int index) {
		return getChildren().get(index);
	}

	List<Node> getChildren() {
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

	void render(final int x, final int y) {
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

		g.setFont(Renderer.BOLD_FONT);

		if (portrait != null) {
			g.drawImage(portrait, x + PADDING, y + PADDING, portraitWidth, PORTRAIT_HEIGHT, null);
		}

		for (final var line : text) {
			nextY += (PADDING + lineHeight);
			g.drawString(line, x + PADDING + (portrait == null ? 0 : this.portraitWidth + PADDING), nextY);
			g.setFont(Renderer.DEFAULT_FONT);
		}
	}

	private Image getPortrait() {
		Image result = null;

		if (individual != null) {
			final var primaryImage = individual.getPrimaryImage(true);
			if (primaryImage != null) {
				var image = (BufferedImage) primaryImage.getImage();

				final var clip = individual.getImageClip(primaryImage);
				if (clip != null) {
					image = image.getSubimage(clip.x, clip.y, clip.width, clip.height);
				}

				result = image.getScaledInstance(-1, PORTRAIT_HEIGHT, Image.SCALE_FAST);
			}
		}

		return result;
	}

	private List<String> getTextLines() {
		final List<String> result = new ArrayList<>();

		if (individual == null) {
			result.add(Structure.UNKNOWN_STRING);
		} else {
			final var name = individual.getNickname().isEmpty() ? individual.getName() : String.format(Format.STRING_WITH_QUOTED_SUFFIX, individual.getName(), individual.getNickname());
			result.add(name);

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
}
