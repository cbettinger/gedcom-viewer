package bettinger.gedcomviewer.tools.facialfeatureanalysis.model;

import java.awt.Color;

public enum FacialFeature {
	CHEEKS, CHIN, EYEBROWS, EYES, FACESHAPE, LIPS, NOSE;

	private static final Color CHEEKS_COLOR = Color.decode("#4363d8");
	private static final Color CHIN_COLOR = Color.decode("#42d4f4");
	private static final Color EYEBROWS_COLOR = Color.decode("#f58231");
	private static final Color EYES_COLOR = Color.decode("#911eb4");
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
			case EYES:
				return EYES_COLOR;
			case FACESHAPE:
				return FACESHAPE_COLOR;
			case LIPS:
				return LIPS_COLOR;
			case NOSE:
				return NOSE_COLOR;
		}

		return Color.WHITE;
	}
}
