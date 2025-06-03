package bettinger.gedcomviewer.model;

import java.util.HashMap;
import java.util.Map;

public enum Quality {

	UNKNOWN(-1), UNRELIABLE(0), QUESTIONABLE(1), SECONDARY(2), PRIMARY(3);

	private static final Map<Integer, Quality> VALUES = new HashMap<>();

	static {
		for (final var q : values()) {
			VALUES.put(q.getValue(), q);
		}
	}

	private final int value;

	private Quality(final int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static Quality fromValue(final int value) {
		return VALUES.get(value);
	}
}
