package bettinger.gedcomviewer.model;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;

@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class Date implements Comparable<Date> {

	@SuppressWarnings("java:S5843")
	private static final Pattern DATE_STRING_PATTERN = Pattern.compile("^((BEF|AFT|ABT|CAL|EST|FROM|TO|BET) )?(((\\d{2}) )?(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC) )?(\\d{4})( (TO|AND) (((\\d{2}) )?(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC) )?(\\d{4}))?$");

	private static final List<String> ATTRIBUTES = Arrays.asList("BEF", "AFT", "ABT", "CAL", "EST", "FROM", "TO", "BET", "AND");
	private static final List<String> MONTHS = Arrays.asList("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC");

	private final String raw;
	private final String[] components;
	private final String formatted;
	@JsonProperty
	private String start;
	@JsonProperty
	private String end;

	private Date(final String raw) throws ParseException {
		this.raw = raw == null ? "" : raw;
		this.components = parseComponents(this.raw);
		this.formatted = format(this.components);
		this.start = formatISO(parseStart(this.components));
		this.end = formatISO(parseEnd(this.components));
	}

	public String getRaw() {
		return raw;
	}

	@Override
	public String toString() {
		return formatted;
	}

	@SuppressWarnings("java:S1210")
	@Override
	public int compareTo(final Date o) {
		if (start == null && o.start == null) {
			return 0;
		} else if (start == null) {
			return -1;
		} else if (o.start == null) {
			return 1;
		} else {
			return start.compareTo(o.start);
		}
	}

	public static Date parse(final String raw) {
		try {
			return new Date(raw);
		} catch (final ParseException _) {
			return null;
		}
	}

	private static String[] parseComponents(final String raw) throws ParseException {
		final var matcher = DATE_STRING_PATTERN.matcher(raw);
		if (!matcher.find()) {
			throw new ParseException(String.format("Invalid date string '%s'", raw), 0);
		}

		final String[] result = new String[8];
		result[0] = matcher.group(2);
		result[1] = matcher.group(5);
		result[2] = matcher.group(6);
		result[3] = matcher.group(7);
		result[4] = matcher.group(9);
		result[5] = matcher.group(12);
		result[6] = matcher.group(13);
		result[7] = matcher.group(14);

		if (result[0] != null && ATTRIBUTES.contains(result[0])) {
			result[0] = I18N.get(result[0]);
		}

		if (result[4] != null && ATTRIBUTES.contains(result[4])) {
			result[4] = I18N.get(result[4]);
		}

		if (result[2] != null && MONTHS.contains(result[2])) {
			result[2] = String.format("%02d", 1 + MONTHS.indexOf(result[2]));
		}

		if (result[6] != null && MONTHS.contains(result[6])) {
			result[6] = String.format("%02d", 1 + MONTHS.indexOf(result[6]));
		}

		return result;
	}

	private static String format(final String[] components) {
		if (components == null || components.length != 8) {
			return "";
		}

		final var sb = new StringBuilder();

		if (components[0] != null) {
			sb.append(String.format(Format.TRAILING_SPACE, components[0]));
		}

		if (components[1] != null) {
			sb.append(String.format(Format.TRAILING_DOT, components[1]));
		}
		if (components[2] != null) {
			sb.append(String.format(Format.TRAILING_DOT, components[2]));
		}
		if (components[3] != null) {
			sb.append(components[3]);
		}

		if (components[4] != null) {
			sb.append(String.format(Format.PADDED, components[4]));
		}

		if (components[5] != null) {
			sb.append(String.format(Format.TRAILING_DOT, components[5]));
		}
		if (components[6] != null) {
			sb.append(String.format(Format.TRAILING_DOT, components[6]));
		}
		if (components[7] != null) {
			sb.append(components[7]);
		}

		return sb.toString();
	}

	private static String formatISO(final LocalDate timestamp) {
		return timestamp == null ? "" : timestamp.toString();
	}

	private static LocalDate parseStart(final String[] components) {
		try {
			return LocalDate.of(Integer.parseInt(components[3]), components[2] == null ? 1 : Integer.parseInt(components[2]), components[1] == null ? 1 : Integer.parseInt(components[1]));
		} catch (final Exception _) {
			return null;
		}
	}

	private static LocalDate parseEnd(final String[] components) {
		try {
			return LocalDate.of(Integer.parseInt(components[7]), components[6] == null ? 1 : Integer.parseInt(components[6]), components[5] == null ? 1 : Integer.parseInt(components[5]));
		} catch (final Exception _) {
			return null;
		}
	}
}
