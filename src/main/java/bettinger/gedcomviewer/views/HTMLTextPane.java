package bettinger.gedcomviewer.views;

import java.awt.Color;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.Events;
import bettinger.gedcomviewer.utils.DesktopUtils;
import bettinger.gedcomviewer.utils.HTMLUtils;

public class HTMLTextPane extends JTextPane {

	private static final SimpleAttributeSet HIGHLIGHTING = new SimpleAttributeSet();
	private static final Color HIGHLIGHT_COLOR = new Color(255, 204, 0);

	private static final String LANG_PARAM = "&lang";

	private static final Pattern IMG_PATTERN = Pattern.compile("^<img width=\"\\d+\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private static final int IMG_MAX_WIDTH = 800;
	private static final int IMG_PADDING = 15;

	private final StyleSheet styleSheet;

	public HTMLTextPane() {
		setEditable(false);
		setOpaque(true);
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createEmptyBorder(Constants.TEXT_PANE_MARGIN, Constants.TEXT_PANE_MARGIN, Constants.TEXT_PANE_MARGIN, Constants.TEXT_PANE_MARGIN));
		setContentType("text/html");

		final var editorKit = new HTMLEditorKit();
		this.styleSheet = new StyleSheet();
		styleSheet.addStyleSheet(editorKit.getStyleSheet());
		styleSheet.addRule("h1 { font-size: 120%; margin: 0 0 15px 0; }");
		styleSheet.addRule("h2, h3, h4, h5, h6 { font-size: 100%; margin: 15px 0 0 0; }");
		editorKit.setStyleSheet(styleSheet);
		setEditorKit(editorKit);

		StyleConstants.setBackground(HIGHLIGHTING, HIGHLIGHT_COLOR);

		addHyperlinkListener(event -> {
			if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				final var url = event.getURL();
				if (url != null) {
					try {
						final var uri = url.toURI();
						if ("file".equalsIgnoreCase(uri.getScheme())) {
							DesktopUtils.openFileURI(uri);
						} else {
							DesktopUtils.openURL(url);
						}
					} catch (final URISyntaxException e) {
						Logger.getLogger(HTMLTextPane.class.getName()).log(Level.SEVERE, String.format("Failed to get URI from URL '%s'", url), e);
					}
				} else {
					Events.post(event);
				}
			}
		});
	}

	public StyleSheet getStyleSheet() {
		return this.styleSheet;
	}

	public void setHTML(final String html, final String highlight) {
		setText(setImageWidth(HTMLUtils.addLinks(fixEncoding(html))));

		if (!(highlight == null || highlight.isEmpty())) {
			applyHighlighting(highlight);
		}
	}

	@Override
	public void setText(final String text) {
		super.setText(text);
		setCaretPosition(0);
	}

	public void clear() {
		super.setText("");
	}

	private static String fixEncoding(final String html) {
		return html.replace(LANG_PARAM, HTMLUtils.encode(LANG_PARAM));
	}

	private String setImageWidth(final String html) {
		final var sb = new StringBuffer();

		final var insets = getInsets();
		final var width = Math.min(IMG_MAX_WIDTH, getWidth() - insets.left - insets.right - IMG_PADDING);

		final var imgMatcher = IMG_PATTERN.matcher(html);
		while (imgMatcher.find()) {
			imgMatcher.appendReplacement(sb, String.format("<img width=\"%d\"", width));
		}
		imgMatcher.appendTail(sb);

		return sb.toString();
	}

	private void applyHighlighting(final String highlight) {
		try {
			final var text = getDocument().getText(0, getDocument().getLength());
			final var matcher = Pattern.compile(highlight, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).matcher(text);
			while (matcher.find()) {
				getStyledDocument().setCharacterAttributes(matcher.start(), highlight.length(), HIGHLIGHTING, false);
			}
		} catch (final BadLocationException e) {
			Logger.getLogger(HTMLTextPane.class.getName()).log(Level.SEVERE, "Unable to apply highlighting", e);
		}
	}
}
