package bettinger.gedcomviewer.tools.validator;

import java.awt.BorderLayout;
import java.io.FileNotFoundException;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JScrollPane;

import bettinger.gedcomviewer.Constants;
import bettinger.gedcomviewer.I18N;
import bettinger.gedcomviewer.model.GEDCOM;
import bettinger.gedcomviewer.tools.validator.GedInlineValidator.Issue;
import bettinger.gedcomviewer.views.AutoFitTable;
import bettinger.gedcomviewer.views.MainFrame;
import bettinger.gedcomviewer.views.TableModel;

public class ValidationDialog extends JDialog {

	public ValidationDialog(final GEDCOM gedcom) throws FileNotFoundException {
		setTitle(I18N.get("Validation"));
		setModal(true);

		setLayout(new BorderLayout());

		final var table = new AutoFitTable();
		add(new JScrollPane(table), BorderLayout.CENTER);

		pack();

		setSize(Constants.DEFAULT_LARGE_MODAL_DIALOG_WIDTH, Constants.DEFAULT_LARGE_MODAL_DIALOG_HEIGHT);
		setLocationRelativeTo(MainFrame.getInstance());

		if (gedcom != null) {
			table.setModel(new IssuesTableModel(GedInlineValidator.execute(gedcom)));
		}
	}

	public void open() {
		setVisible(true);
	}

	private static final class IssuesTableModel extends TableModel<Issue> {

		public IssuesTableModel(final List<Issue> issues) {
			super(new String[] { I18N.get("Row"), I18N.get("Message") }, issues);
		}

		@Override
		public Object getValueAt(final int row, final int col) {
			final var issue = getItemAt(row);

			return switch (col) {
				case 0 -> issue.getLine();
				case 1 -> issue.getMessage();
				default -> "";
			};
		}

		@Override
		public Class<?> getColumnClass(final int column) {
			return switch (column) {
				case 0 -> Integer.class;
				default -> super.getColumnClass(column);
			};
		}
	}
}
