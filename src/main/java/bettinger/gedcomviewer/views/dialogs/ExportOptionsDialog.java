package bettinger.gedcomviewer.views.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.HTMLOption;
import bettinger.gedcomviewer.views.MainFrame;

public interface ExportOptionsDialog {

	public static Set<HTMLOption> getPDFOptions() {
		final var embedHighQualityMediaFilesCheckBox = new JCheckBox(I18N.get("EmbedHighQualityMediaFiles"), true);

		final var result = getOptions(embedHighQualityMediaFilesCheckBox);

		if (result != null && !embedHighQualityMediaFilesCheckBox.isSelected()) {
			result.add(HTMLOption.NO_OPEN_MEDIA_LINK);
		}

		return result;
	}

	public static Set<HTMLOption> getHTMLOptions() {
		return getOptions();
	}

	@SuppressWarnings("java:S1168")
	private static Set<HTMLOption> getOptions(final Object... options) {
		final var exportConfidentialDataCheckBox = new JCheckBox(I18N.get("ExportConfidentialData"), false);

		final var mergedOptions = new ArrayList<>(options.length + 1);
		mergedOptions.add(exportConfidentialDataCheckBox);
		mergedOptions.addAll(Arrays.asList(options));

		if (JOptionPane.showOptionDialog(MainFrame.getInstance(), mergedOptions.toArray(new Object[0]), I18N.get("ExportOptions"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.CANCEL_OPTION) {
			return null;
		}

		final var result = HTMLOption.getNone();

		if (!exportConfidentialDataCheckBox.isSelected()) {
			result.add(HTMLOption.NO_CONFIDENTIAL_DATA);
		}

		return result;
	}
}
