package bettinger.gedcomviewer.views.dialogs;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.table.DefaultTableModel;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.Format;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.utils.DateTimeUtils;
import bettinger.gedcomviewer.utils.HTMLUtils;
import bettinger.gedcomviewer.views.AutoFitTable;
import bettinger.gedcomviewer.views.HTMLTextPane;
import bettinger.gedcomviewer.views.MainFrame;

public class PropertiesDialog extends JDialog {

	private final HTMLTextPane authorPane;
	private final HTMLTextPane contributorsPane;
	private final HTMLTextPane metaDataPane;
	private final AutoFitTable statisticsTable;

	public PropertiesDialog(final GEDCOM gedcom) {
		setTitle(I18N.get("Properties"));
		setModal(true);

		setLayout(new BorderLayout());

		this.authorPane = new HTMLTextPane();
		this.contributorsPane = new HTMLTextPane();
		this.metaDataPane = new HTMLTextPane();
		this.statisticsTable = new AutoFitTable();

		final var tabbedPane = new JTabbedPane();
		tabbedPane.addTab(I18N.get("Author"), new JScrollPane(this.authorPane));
		tabbedPane.addTab(I18N.get("Contributors"), new JScrollPane(this.contributorsPane));
		tabbedPane.addTab(I18N.get("Metadata"), new JScrollPane(this.metaDataPane));
		tabbedPane.addTab(I18N.get("Statistics"), new JScrollPane(this.statisticsTable));
		tabbedPane.setSelectedIndex(0);
		add(tabbedPane, BorderLayout.CENTER);

		pack();
		setSize(Constants.DEFAULT_MODAL_DIALOG_WIDTH, Constants.DEFAULT_MODAL_DIALOG_HEIGHT);
		setResizable(false);
		setLocationRelativeTo(MainFrame.getInstance());

		open(gedcom);

		setVisible(true);
	}

	private void open(final GEDCOM gedcom) {
		if (gedcom != null) {
			final var author = gedcom.getAuthor();
			if (author != null) {
				final var authorText = HTMLUtils.addLinks(author.toHTML());
				if (!authorText.isEmpty()) {
					authorPane.setText(authorText);
				}
			}

			final var contributors = gedcom.getContributors();
			if (!contributors.isEmpty()) {
				final var contributorsText = HTMLUtils.createList(contributors, s -> HTMLUtils.addLinks(s.toHTML()), true);
				if (!contributorsText.isEmpty()) {
					contributorsPane.setText(contributorsText);
				}
			}

			final var header = gedcom.getWrappedGedcom().getHeader();
			if (header != null) {
				final var sb = new StringBuilder();

				final var copyrightText = header.getCopyright() == null ? "" : header.getCopyright().replace("<", "&lt;").replace(">", "&gt;");
				final var lastChangeText = header.getDateTime() == null ? "" : DateTimeUtils.format(header.getDateTime());
				final var languageText = header.getLanguage() == null ? "" : header.getLanguage();
				final var characterEncodingText = header.getCharacterSet() == null || header.getCharacterSet().getValue() == null ? "" : header.getCharacterSet().getValue();
				final var gedcomVersionText = header.getGedcomVersion() == null || header.getGedcomVersion().getVersion() == null ? "" : header.getGedcomVersion().getVersion();

				var generatorText = "";
				final var generator = header.getGenerator();
				if (generator != null) {
					final var name = generator.getName();
					final var value = generator.getValue();
					if (name != null && !name.isEmpty()) {
						generatorText = name;

						final var version = generator.getVersion();
						if (version != null && !version.isEmpty()) {
							generatorText += String.format(" %s", version);
						}
					} else if (value != null && !value.isEmpty()) {
						generatorText = value;
					}
				}

				if (!copyrightText.isEmpty()) {
					HTMLUtils.appendLine(sb, String.format(Format.KEY_VALUE, HTMLUtils.createStrong(I18N.get("Copyright")), copyrightText));
				}
				if (!lastChangeText.isEmpty()) {
					HTMLUtils.appendLine(sb, String.format(Format.KEY_VALUE, HTMLUtils.createStrong(I18N.get("LastChange")), lastChangeText));
				}
				if (!languageText.isEmpty()) {
					HTMLUtils.appendLine(sb, String.format(Format.KEY_VALUE, HTMLUtils.createStrong(I18N.get("Language")), languageText));
				}
				if (!characterEncodingText.isEmpty()) {
					HTMLUtils.appendLine(sb, String.format(Format.KEY_VALUE, HTMLUtils.createStrong(I18N.get("CharacterEncoding")), characterEncodingText));
				}
				if (!gedcomVersionText.isEmpty()) {
					HTMLUtils.appendLine(sb, String.format(Format.KEY_VALUE, HTMLUtils.createStrong(I18N.get("GEDCOMVersion")), gedcomVersionText));
				}
				if (!generatorText.isEmpty()) {
					HTMLUtils.appendLine(sb, String.format(Format.KEY_VALUE, HTMLUtils.createStrong(I18N.get("Software")), generatorText));
				}

				metaDataPane.setText(sb.toString());
			}

			statisticsTable.setModel(new DefaultTableModel(new String[][] { { I18N.get("NumberOfIndividuals"), Integer.toString(gedcom.getIndividuals().size()) }, { I18N.get("NumberOfMales"), Integer.toString(gedcom.getIndividuals().stream().filter(i -> i.isMale()).toList().size()) }, { I18N.get("NumberOfFemales"), Integer.toString(gedcom.getIndividuals().stream().filter(i -> i.isFemale()).toList().size()) }, { I18N.get("NumberOfFamilies"), Integer.toString(gedcom.getFamilies().size()) }, { I18N.get("NumberOfLocations"), Integer.toString(gedcom.getLocations().size()) }, { I18N.get("NumberOfMedia"), Integer.toString(gedcom.getMedia().size()) }, { I18N.get("NumberOfSources"), Integer.toString(gedcom.getSources().size()) }, { I18N.get("NumberOfRepositories"), Integer.toString(gedcom.getRepositories().size()) }, { I18N.get("NumberOfNotes"), Integer.toString(gedcom.getNotes().size()) }, { I18N.get("NumberOfSubmitters"), Integer.toString(gedcom.getSubmitters().size()) } }, new String[] { I18N.get("Name"), I18N.get("Value") }));
		}
	}
}
