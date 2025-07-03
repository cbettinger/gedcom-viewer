package bettinger.gedcomviewer.views.dialogs;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.utils.HTMLUtils;
import bettinger.gedcomviewer.views.HTMLTextPane;
import bettinger.gedcomviewer.views.MainFrame;

public class AboutDialog extends JDialog {

	public AboutDialog() {
		setTitle(I18N.get("About"));
		setModal(true);

		setLayout(new BorderLayout());

		final var textPane = new HTMLTextPane();
		textPane.setBorder(new EmptyBorder(2 * Constants.TEXT_PANE_MARGIN, 2 * Constants.TEXT_PANE_MARGIN, 2 * Constants.TEXT_PANE_MARGIN, 2 * Constants.TEXT_PANE_MARGIN));
		textPane.setFocusable(false);
		add(new JScrollPane(textPane), BorderLayout.CENTER);

		final var sb = new StringBuilder();

		final var iconURL = MainFrame.getIconURL().toString();
		if (iconURL != null) {
			HTMLUtils.appendImage(sb, iconURL, 100);
		}

		HTMLUtils.appendLineBreaks(sb, 2);
		HTMLUtils.appendH1(sb, Constants.APP_NAME);
		HTMLUtils.appendLine(sb, HTMLUtils.createStrong(String.format(Format.SPACED, I18N.get("Version"), Constants.APP_VERSION)));
		HTMLUtils.appendLineBreak(sb);
		HTMLUtils.appendLine(sb, HTMLUtils.createStrong(String.format(Format.SPACED, "Java", System.getProperty("java.version"))));
		HTMLUtils.appendLineBreak(sb);
		HTMLUtils.appendLine(sb, HTMLUtils.createStrong(HTMLUtils.createLink(Constants.APP_URL, Constants.APP_URL)));
		HTMLUtils.appendLineBreak(sb);
		HTMLUtils.appendLine(sb, HTMLUtils.createStrong(String.format("%s \u00A9 2022 %s & %s", I18N.get("Copyright"), HTMLUtils.createEMailLink("c.bettinger@live.de", "Christian Bettinger"), HTMLUtils.createEMailLink("miriam.bettinger@icloud.com", "Miriam Bettinger"))));
		HTMLUtils.appendLineBreak(sb);
		HTMLUtils.appendLine(sb, "Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.");
		HTMLUtils.appendLineBreak(sb);
		HTMLUtils.appendLine(sb, "THE SOFTWARE IS PROVIDED \"AS IS\" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.");

		HTMLUtils.appendH2(sb, "Third-party software");
		HTMLUtils.appendLine(sb, "See subfolder \"thirdparty\" for a list of third-party software products and media assets that are redistributed and/or used in accordance to their respective license.");

		textPane.setText(sb.toString());

		pack();

		setSize(Constants.DEFAULT_MODAL_DIALOG_WIDTH, Constants.DEFAULT_MODAL_DIALOG_HEIGHT);
		setLocationRelativeTo(MainFrame.getInstance());

		setVisible(true);
	}
}
