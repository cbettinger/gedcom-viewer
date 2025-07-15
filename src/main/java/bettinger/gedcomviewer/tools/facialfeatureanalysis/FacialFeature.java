package bettinger.gedcomviewer.tools.facialfeatureanalysis;

import java.awt.Color;

public enum FacialFeature {
	CHEEKS, CHIN, EYEBROWS, EYESHAPE, FACESHAPE, LIPS, NOSE;

	public static final Color DEFAULT_COLOR = Color.DARK_GRAY;

	private static final Color CHEEKS_COLOR = Color.decode("#4363d8");
	private static final Color CHIN_COLOR = Color.decode("#42d4f4");
	private static final Color EYEBROWS_COLOR = Color.decode("#f58231");
	private static final Color EYESHAPE_COLOR = Color.decode("#911eb4");
	private static final Color FACESHAPE_COLOR = Color.decode("#3cb44b");
	private static final Color LIPS_COLOR = Color.decode("#f032e6");
	private static final Color NOSE_COLOR = Color.decode("#ffe119");

	public static Color getColor(final FacialFeature value) {
		switch (value) {
			case CHEEKS:
				return CHEEKS_COLOR;
			case CHIN:
				return CHIN_COLOR;
			case EYEBROWS:
				return EYEBROWS_COLOR;
			case EYESHAPE:
				return EYESHAPE_COLOR;
			case FACESHAPE:
				return FACESHAPE_COLOR;
			case LIPS:
				return LIPS_COLOR;
			case NOSE:
				return NOSE_COLOR;
		}

		return Color.WHITE;
	}

	public static Color getColor(final FacialFeature value, final float similarity) {
		final var color = getColor(value);

		final int r = Math.min(255, (int) (DEFAULT_COLOR.getRed() + similarity * color.getRed()));
		final int g = Math.min(255, (int) (DEFAULT_COLOR.getGreen() + similarity * color.getGreen()));
		final int b = Math.min(255, (int) (DEFAULT_COLOR.getBlue() + similarity * color.getBlue()));

		return new Color(r, g, b, 255);
	}
}
