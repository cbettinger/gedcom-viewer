package bettinger.gedcomviewer.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;

import bettinger.gedcomviewer.Format;

public abstract class HTMLUtils {

	public static final String DIVIDER = "―";
	public static final String LINE_BREAK = "<br>";
	public static final String SEPARATOR = "&#124;";
	public static final String SPACE = "&emsp;";

	private static final String ANCHOR = "#";

	private static final Pattern URL_PATTERN = Pattern.compile("\\b(?<!href=\")https?://[-A-Z0-9+&@#/%?=~_(|!:,.;]*[-A-Z0-9+&@#/%=~_)|]", Pattern.CASE_INSENSITIVE);
	@SuppressWarnings("all")
	private static final Pattern EMAIL_PATTERN = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])", Pattern.CASE_INSENSITIVE);
	private static final Pattern FILE_PATTERN = Pattern.compile("href='file:([^\s]+)'", Pattern.CASE_INSENSITIVE);

	public static void appendLine(final StringBuilder sb, final String text) {
		appendText(sb, text);
		if (text != null && !text.isEmpty()) {
			appendLineBreak(sb);
		}
	}

	public static void appendLineBreaks(final StringBuilder sb, final int count) {
		for (int i = 0; i < count; i++) {
			appendLineBreak(sb);
		}
	}

	public static void appendLineBreak(final StringBuilder sb) {
		appendText(sb, LINE_BREAK);
	}

	public static void appendSeparator(final StringBuilder sb) {
		appendText(sb, String.format(Format.PADDED, SEPARATOR));
	}

	public static void appendSpace(final StringBuilder sb) {
		appendText(sb, SPACE);
	}

	public static void appendH1(final StringBuilder sb, final String text) {
		appendText(sb, createH1(text));
	}

	public static String createH1(final String text) {
		return createElement("h1", text);
	}

	public static void appendH2(final StringBuilder sb, final String text) {
		appendText(sb, createH2(text));
	}

	public static String createH2(final String text) {
		return createElement("h2", text);
	}

	public static void appendH3(final StringBuilder sb, final String text) {
		appendText(sb, createH3(text));
	}

	public static String createH3(final String text) {
		return createElement("h3", text);
	}

	public static void appendStrong(final StringBuilder sb, final String text) {
		appendText(sb, createStrong(text));
	}

	public static String createStrong(final String text) {
		return createElement("strong", text);
	}

	public static void appendImage(final StringBuilder sb, final String src) {
		appendText(sb, String.format("<img src='%s'>", src));
	}

	public static void appendImage(final StringBuilder sb, final String src, final int width) {
		appendText(sb, String.format("<img width='%s' src='%s'>", Integer.toString(width), src));
	}

	public static void appendDetails(final StringBuilder sb, final String summary, final String details) {
		appendText(sb, String.format("<details><summary>%s</summary>%s</details>", summary, details));
	}

	public static void appendSection(final StringBuilder sb, final String text) {
		appendText(sb, createElement("section", text));
	}

	public static void appendTextAfterLineBreak(final StringBuilder sb, final String text) {
		appendTextAfterLineBreaks(sb, text, 1);
	}

	public static void appendTextAfterLineBreaks(final StringBuilder sb, final String text, final int lineBreakCount) {
		if (text != null && !text.isEmpty()) {
			appendLineBreaks(sb, lineBreakCount);
			sb.append(text);
		}
	}

	public static void appendText(final StringBuilder sb, final String text) {
		if (text != null && !text.isEmpty()) {
			sb.append(text);
		}
	}

	public static String getAnchorFromLink(final String link) {
		return link == null || link.length() < ANCHOR.length() + 1 || !link.startsWith(ANCHOR) ? null : link.substring(1);
	}

	public static String createAnchorLink(final String anchor, final String text) {
		return HTMLUtils.createLink(String.format(Format.TWO_STRINGS, ANCHOR, anchor), text);
	}

	public static String createLink(final String href, final String text) {
		return String.format("<a href='%s'>%s</a>", href, text);
	}

	public static String createDownloadLink(final String href, final String text) {
		return String.format("<a href='%s' download='%s' target='_blank'>%s</a>", href, FileUtils.getFileName(href), text);
	}

	public static String createEMailLink(final String email) {
		return createEMailLink(email, email);
	}

	public static String createEMailLink(final String email, final String text) {
		return String.format("<a href='mailto:%s'>%s</a>", email, text);
	}

	public static void appendUnorderedList(final StringBuilder sb, final List<String> items) {
		appendText(sb, createUnorderedList(items));
	}

	public static String createUnorderedList(final List<String> items) {
		return createElement("ul", String.join(System.lineSeparator(), items.stream().map(item -> String.format("<li>%s</li>", item)).toList()));
	}

	public static String createElement(final String tag, final String text) {
		return String.format("<%s>%s</%s>", tag, text, tag);
	}

	public static String addClassList(final String element, final String... classes) {
		final var classList = String.join(" ", classes);
		final var classAttribute = classList.isEmpty() ? "" : String.format(" class=\"%s\"", classList);
		final var indexOfFirstClosingBracket = element.indexOf(">");
		if (indexOfFirstClosingBracket >= 2) {
			return String.format("%s%s%s", element.substring(0, indexOfFirstClosingBracket), classAttribute, element.substring(indexOfFirstClosingBracket));
		} else {
			return element;
		}
	}

	public static <T> String createList(final List<T> items, final Function<T, String> mapper) {
		return createList(items, mapper, null);
	}

	public static <T> String createList(final List<T> items, final Function<T, String> mapper, final String itemStyle) {
		return createList(items, mapper, itemStyle, false);
	}

	public static <T> String createList(final List<T> items, final Function<T, String> mapper, final boolean addDividers) {
		return createList(items, mapper, null, addDividers);
	}

	public static <T> String createList(final List<T> items, final Function<T, String> mapper, final String itemStyle, final boolean addDividers) {
		final var formatString = addDividers ? String.format("%s%s%s", LINE_BREAK, DIVIDER, LINE_BREAK) : LINE_BREAK;
		var stream = items.stream().map(mapper);
		if (!(itemStyle == null || itemStyle.isEmpty())) {
			stream = stream.map(s -> String.format(Format.SPACED, itemStyle, s));
		}
		return String.join(formatString, stream.toList());
	}

	public static <T> String createSingleLineList(final List<T> items, final Function<T, String> mapper, final String separator) {
		final var stream = items.stream().map(mapper);
		return String.join(String.format(Format.TRAILING_SPACE, separator), stream.toList());
	}

	public static List<String> getDownloadLinks(final String html) {
		final var result = new ArrayList<String>();

		final Matcher fileMatcher = FILE_PATTERN.matcher(html);
		while (fileMatcher.find()) {
			result.add(fileMatcher.group(1));
		}

		return result;
	}

	public static String addLinks(final String html) {
		return addEMailLinks(addWWWLinks(html));
	}

	private static String addWWWLinks(final String html) {
		final var sb = new StringBuffer();

		final Matcher urlMatcher = URL_PATTERN.matcher(html);
		while (urlMatcher.find()) {
			final String url = html.substring(urlMatcher.start(), urlMatcher.end());
			urlMatcher.appendReplacement(sb, String.format("<a href=\"%s\" target=\"blank\">%s</a>", url, url));
		}
		urlMatcher.appendTail(sb);

		return sb.toString();
	}

	private static String addEMailLinks(final String html) {
		final var sb = new StringBuffer();

		final Matcher emailMatcher = EMAIL_PATTERN.matcher(html);
		while (emailMatcher.find()) {
			final String email = html.substring(emailMatcher.start(), emailMatcher.end());
			if (email.startsWith("//www.google.com/maps/")) {	// workaround to not match Google Maps links
				continue;
			}
			emailMatcher.appendReplacement(sb, String.format("<a href=\"mailto:%s\">%s</a>", email, email));
		}
		emailMatcher.appendTail(sb);

		return sb.toString();
	}

	public static String removeLinks(final String html) {
		return removeTags(html, "a");
	}

	public static String removeTags(final String html, final String tagName) {
		return html.replaceAll(String.format("</?%s[^>]*>", tagName), "");
	}

	public static String encode(final String html) {
		try {
			return URLEncoder.encode(html, StandardCharsets.UTF_8.toString());
		} catch (final UnsupportedEncodingException _) {
			return html;
		}
	}

	public static String convertHTMLToString(final String html) {
		return html.replace(LINE_BREAK, System.lineSeparator()).replaceAll("<[^>]*>", "");
	}

	public static String convertStringToHTML(final String text) {
		return text.replace("\r\n", LINE_BREAK).replace("\n", LINE_BREAK);
	}

	public static String eliminateLineBreaks(final String text) {
		return convertStringToHTML(text).replace(LINE_BREAK, " ");
	}

	public static String printPretty(final String html) {
		final var document = Jsoup.parse(html);
		document.outputSettings().prettyPrint(true).outline(true).indentAmount(4);
		return document.html();
	}

	private HTMLUtils() {}
}
