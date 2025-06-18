package bettinger.gedcomviewer;

public abstract class Format {

	public static final String LEADING_SLASH = "-%s";

	public static final String TRAILING_SPACE = "%s ";
	public static final String TRAILING_COLON = "%s:";
	public static final String TRAILING_DOT = "%s.";
	public static final String TRAILING_TRIPLE_DOT = "%s…";

	public static final String PADDED = " %s ";
	public static final String BRACKETED = "[%s]";

	public static final String KEY_VALUE = "%s: %s";
	public static final String KEY_VALUE_TRAILING_TRIPLE_DOT = "%s: %s…";

	public static final String TWO_STRINGS = "%s%s";
	public static final String SPACED = "%s %s";
	public static final String DOT_SEPARATED = "%s.%s";
	public static final String SLASH_SEPARATED = "%s/%s";
	public static final String PIPE_SEPARATED = "%s|%s";
	public static final String PADDED_PIPE_SEPARATED = "%s | %s";
	public static final String PADDED_AND_SEPARATED = "%s & %s";

	public static final String STRING_WITH_PARENTHESED_SUFFIX = "%s (%s)";
	public static final String STRING_WITH_QUOTED_SUFFIX = "%s \"%s\"";

	public static final String THREE_STRINGS = "%s%s%s";
	public static final String THREE_SPACED_STRINGS = "%s %s %s";

	public static final String TRAILING_SPACE_COLON = ": ";
	public static final String TRAILING_SPACE_COLON_WITH_SUFFIX = ": %s";
	public static final String TRAILING_SPACE_COMMA = ", ";
	public static final String TRAILING_SPACE_COMMA_WITH_SUFFIX = ", %s";
	public static final String TRAILING_PADDED_PIPE_WITH_SUFFIX =  " | %s";

	private Format() {}
}
