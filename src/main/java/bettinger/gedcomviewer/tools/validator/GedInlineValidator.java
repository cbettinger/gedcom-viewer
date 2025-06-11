package bettinger.gedcomviewer.tools.validator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import bettinger.gedcomviewer.model.GEDCOM;

public class GedInlineValidator {

	private static final Pattern ISSUE_PATTERN = Pattern.compile("^\\*\\*\\* Line (\\d+):\s+(.*)$");

	private GedInlineValidator() {}

	public static List<Issue> validate(final GEDCOM gedcom) throws FileNotFoundException {

		final var stringWriter = new StringWriter();
		final var printWriter = new PrintWriter(stringWriter);

		new gedinline.main.GedInlineValidator(gedcom.getFile(), printWriter).validate();

		var lines = stringWriter.toString().replace("\r\n", "\n").split("\n");

		return Arrays.asList(lines).stream().filter(l -> !l.isEmpty()).filter(l -> l.startsWith("*** Line")).map(l -> parseIssue(l)).filter(i -> i != null).toList();
	}

	private static Issue parseIssue(final String line) {
		final var matcher = ISSUE_PATTERN.matcher(line);
		if (!matcher.find()) {
			return null;
		}

		return new Issue(Integer.parseInt(matcher.group(1)), matcher.group(2));
	}

	public static class Issue {
		private final int line;
		private final String message;

		Issue(final int line, final String message) {
			this.line = line;
			this.message = message;
		}

		public int getLine() {
			return line;
		}

		public String getMessage() {
			return message;
		}
	}
}
