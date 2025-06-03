package bettinger.gedcomviewer.utils;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class DateTimeUtils {

	private static final Map<String, Month> MONTHS;

	static {
		MONTHS = new HashMap<>();
		MONTHS.put("JAN", Month.JANUARY);
		MONTHS.put("FEB", Month.FEBRUARY);
		MONTHS.put("MAR", Month.MARCH);
		MONTHS.put("APR", Month.APRIL);
		MONTHS.put("MAY", Month.MAY);
		MONTHS.put("JUN", Month.JUNE);
		MONTHS.put("JUL", Month.JULY);
		MONTHS.put("AUG", Month.AUGUST);
		MONTHS.put("SEP", Month.SEPTEMBER);
		MONTHS.put("OCT", Month.OCTOBER);
		MONTHS.put("NOV", Month.NOVEMBER);
		MONTHS.put("DEC", Month.DECEMBER);
	}

	private static final DateTimeFormatter ORIGINAL_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
	private static final DateTimeFormatter ORIGINAL_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
	private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public static org.folg.gedcom.model.DateTime now() {
		final var now = LocalDateTime.now();

		final var result = new org.folg.gedcom.model.DateTime();
		result.setValue(now.format(ORIGINAL_DATE_FORMATTER).toUpperCase());
		result.setTime(now.format(ORIGINAL_TIME_FORMATTER));
		return result;
	}

	public static String format(final org.folg.gedcom.model.DateTime dateTime) {
		final var ldt = parseDateTime(dateTime);
		return ldt == null ? "" : ldt.format(DISPLAY_FORMATTER);
	}

	public static String format(final LocalDateTime dateTime) {
		return dateTime.format(DISPLAY_FORMATTER);
	}

	public static LocalDateTime parseLastChange(final org.folg.gedcom.model.GedcomTag tag) {
		final var changeTag = TagUtils.getChildTag(tag, "CHAN");
		if (changeTag != null) {
			final var dateTag = TagUtils.getChildTag(changeTag, "DATE");
			if (dateTag != null) {
				final var date = dateTag.getValue();
				final var time = TagUtils.parseChildTagValue(dateTag, "TIME");

				if (!(date == null || date.isEmpty() || time == null || time.isEmpty())) {
					return DateTimeUtils.parseDateTime(date, time);
				}
			}
		}

		return null;
	}

	public static LocalDateTime parseDateTime(final org.folg.gedcom.model.Change change) {
		if (change == null) {
			return null;
		}

		return parseDateTime(change.getDateTime());
	}

	public static LocalDateTime parseDateTime(final org.folg.gedcom.model.DateTime dateTime) {
		return parseDateTime(dateTime.getValue(), dateTime.getTime());
	}

	public static LocalDateTime parseDateTime(final String date, final String time) {
		final var dateParts = date.split(" ");
		if (dateParts.length != 3) {
			return null;
		}

		final var timeParts = time.split(":");
		if (timeParts.length != 3) {
			return null;
		}

		final var yyyy = Integer.parseUnsignedInt(dateParts[2]);
		final var mmm = MONTHS.get(dateParts[1]);
		final var dd = Integer.parseUnsignedInt(dateParts[0]);

		final var hh = Integer.parseUnsignedInt(timeParts[0]);
		final var mm = Integer.parseUnsignedInt(timeParts[1]);
		final var ss = Integer.parseUnsignedInt(timeParts[2].substring(0, 2));

		return LocalDateTime.of(yyyy, mmm, dd, hh, mm, ss);
	}

	private DateTimeUtils() {}
}
